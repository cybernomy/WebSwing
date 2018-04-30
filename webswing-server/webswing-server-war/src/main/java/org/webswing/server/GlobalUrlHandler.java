package org.webswing.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.model.s2c.ApplicationInfoMsg;
import org.webswing.server.base.PrimaryUrlHandler;
import org.webswing.server.base.UrlHandler;
import org.webswing.server.common.model.SecuredPathConfig;
import org.webswing.server.common.model.admin.ApplicationInfo;
import org.webswing.server.common.model.admin.InstanceManagerStatus;
import org.webswing.server.common.model.meta.MetaObject;
import org.webswing.server.common.model.rest.LogRequest;
import org.webswing.server.common.model.rest.LogResponse;
import org.webswing.server.common.util.CommonUtil;
import org.webswing.server.model.exception.WsException;
import org.webswing.server.services.config.ConfigurationChangeEvent;
import org.webswing.server.services.config.ConfigurationChangeListener;
import org.webswing.server.services.config.ConfigurationService;
import org.webswing.server.services.resources.ResourceHandlerService;
import org.webswing.server.services.security.api.BuiltInModules;
import org.webswing.server.services.security.api.WebswingAction;
import org.webswing.server.services.security.api.WebswingSecurityConfig;
import org.webswing.server.services.security.login.LoginHandlerService;
import org.webswing.server.services.security.login.SecuredPathHandler;
import org.webswing.server.services.security.modules.SecurityModuleService;
import org.webswing.server.services.swinginstance.SwingInstance;
import org.webswing.server.services.swingmanager.SwingInstanceHolder;
import org.webswing.server.services.swingmanager.SwingInstanceManager;
import org.webswing.server.services.swingmanager.SwingInstanceManagerService;
import org.webswing.server.services.websocket.WebSocketService;
import org.webswing.server.util.LogReaderUtil;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GlobalUrlHandler extends PrimaryUrlHandler implements SwingInstanceHolder, SecuredPathHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalUrlHandler.class);

	private final WebSocketService websocket;
	private final ConfigurationService configService;
	private final SwingInstanceManagerService appFactory;
	private final ResourceHandlerService resourceService;
	private final LoginHandlerService loginService;

	private ServletContext servletContext;

	private Map<String, SwingInstanceManager> instanceManagers = new LinkedHashMap<String, SwingInstanceManager>();

	private final ConfigurationChangeListener changeListener = new ConfigurationChangeListener() {
		@Override
		public void onConfigChanged(ConfigurationChangeEvent e) {
			if ("/".equals(e.getPath())) {
				initConfiguration();
			} else {
				SwingInstanceManager manager = instanceManagers.get(e.getPath());
				if (manager == null) {
					installApplication(e.getNewConfig()).init();
				} else if (manager.isEnabled()) {
					manager.initConfiguration();
				}
			}
		}

		@Override
		public void onConfigDeleted(ConfigurationChangeEvent e) {
			if ("/".equals(e.getPath())) {
				initConfiguration();
			} else {
				SwingInstanceManager manager = instanceManagers.get(e.getPath());
				if (manager != null) {
					uninstallApplication(manager);
				}
			}
		}
	};

	@Inject
	public GlobalUrlHandler(WebSocketService websocket, ConfigurationService config, SwingInstanceManagerService appFactory, ResourceHandlerService resourceService, SecurityModuleService securityService, LoginHandlerService loginService, ServletContext servletContext) {
		super(null, securityService, config);
		this.websocket = websocket;
		this.configService = config;
		this.appFactory = appFactory;
		this.resourceService = resourceService;
		this.loginService = loginService;
		this.servletContext = servletContext;
	}

	public void init() {
		registerChildUrlHandler(websocket.createPlaybackWebSocketHandler(this));

		registerChildUrlHandler(loginService.createLoginHandler(this));
		registerChildUrlHandler(loginService.createLogoutHandler(this));

		registerChildUrlHandler(resourceService.create(this, this));

		loadApplications();
		super.init();
		this.configService.registerChangeListener(this.changeListener);
		if (!InstanceManagerStatus.Status.Running.equals(getStatus().getStatus())) {
			throw new RuntimeException("Failed to start primary handler.");
		}
	}

	public void destroy() {
		this.configService.removeChangeListener(this.changeListener);
		instanceManagers.clear();
		super.destroy();
	}

	public boolean serve(HttpServletRequest req, HttpServletResponse res) {
		try {
			boolean served = super.serve(req, res);
			if (!served) {
				throw new WsException("Not Found.", HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (Exception e) {
			handleException(e, req, res);
		}
		return true;
	}

	@Override
	protected WebswingSecurityConfig getSecurityConfig() {
		log.info("Loading master security module.(" + getConfig().getSecurity() + ").");
		WebswingSecurityConfig secConfig = super.getSecurityConfig();
		if (BuiltInModules.INHERITED.name().equals(secConfig.getModule())) {
			log.error("Master security module INHERITED is not valid. Falling back to default module PROPERTY_FILE.");
			SecuredPathConfig newconfig = getConfig();
			newconfig.getSecurity().put("module", BuiltInModules.PROPERTY_FILE.name());
			secConfig = newconfig.getValueAs("security", WebswingSecurityConfig.class);
		}
		return secConfig;
	}

	public void loadApplications() {
		log.info("Loading configured Applications.");
		synchronized (instanceManagers) {
			for (String path : configService.getPaths()) {
				SecuredPathConfig configPath = configService.getConfiguration(path);
				String pathMapping = toPath(path);
				if (!toPath("/").equals(pathMapping)) {
					SwingInstanceManager childHandler = instanceManagers.get(pathMapping);
					if (childHandler == null) {
						installApplication(configPath);
					} else {
						log.error("Application with path '" + pathMapping + "' already exists! Application skipped.", new IllegalStateException("Invalid Application configuration."));
					}
				}
			}
		}
	}

	public SwingInstanceManager installApplication(SecuredPathConfig swing) {
		log.info("Installing application " + swing.getPath());
		SwingInstanceManager app = appFactory.createApp(this, swing.getPath());
		registerFirstChildUrlHandler(app);
		return app;
	}

	public void uninstallApplication(SwingInstanceManager appToRemove) {
		log.info("Removing application " + appToRemove.getPathMapping());
		appToRemove.destroy();
		removeChildUrlHandler(appToRemove);
	}

	private void handleException(Exception e, HttpServletRequest req, HttpServletResponse res) {
		log.debug("Failed to process request. " + req.getPathInfo(), e);
		try {
			if (e instanceof WsException) {
				if (!res.isCommitted()) {
					WsException wse = (WsException) e;
					res.sendError(wse.getReponseCode(), wse.getLocalizedMessage());
				}
			} else {
				log.error("Failed to process request. " + req.getPathInfo(), e);
				if (!res.isCommitted()) {
					res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					e.printStackTrace(new PrintStream(res.getOutputStream()));
				}
			}
		} catch (IOException e1) {
			log.error("Failed send error response to client. ");
		}

	}

	@Override
	public void registerFirstChildUrlHandler(UrlHandler handler) {
		super.registerFirstChildUrlHandler(handler);
		if (handler instanceof SwingInstanceManager) {
			synchronized (instanceManagers) {
				SwingInstanceManager manager = (SwingInstanceManager) handler;
				instanceManagers.put(manager.getPathMapping(), manager);
			}
		}
	}

	@Override
	public void registerChildUrlHandler(UrlHandler handler) {
		super.registerChildUrlHandler(handler);
		if (handler instanceof SwingInstanceManager) {
			synchronized (instanceManagers) {
				SwingInstanceManager manager = (SwingInstanceManager) handler;
				instanceManagers.put(manager.getPathMapping(), manager);
			}
		}
	}

	@Override
	public void removeChildUrlHandler(UrlHandler handler) {
		super.removeChildUrlHandler(handler);
		if (handler instanceof SwingInstanceManager) {
			synchronized (instanceManagers) {
				SwingInstanceManager manager = (SwingInstanceManager) handler;
				instanceManagers.remove(manager.getPathMapping(), manager);
			}
		}
	}

	protected String getPath() {
		return "";
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public SwingInstance findInstanceBySessionId(String uuid) {
		synchronized (instanceManagers) {
			for (SwingInstanceManager im : instanceManagers.values()) {
				SwingInstance instance;
				if ((instance = im.findInstanceBySessionId(uuid)) != null) {
					return instance;
				}
			}
		}
		return null;
	}

	@Override
	public SwingInstance findInstanceByClientId(String clientId) {
		synchronized (instanceManagers) {
			for (SwingInstanceManager im : instanceManagers.values()) {
				SwingInstance instance;
				if ((instance = im.findInstanceByClientId(clientId)) != null) {
					return instance;
				}
			}
		}
		return null;
	}

	@Override
	public List<SwingInstance> getAllInstances() {
		ArrayList<SwingInstance> result = new ArrayList<>();
		synchronized (instanceManagers) {
			for (SwingInstanceManager im : instanceManagers.values()) {
				result.addAll(im.getAllInstances());
			}
		}
		return result;
	}

	@Override
	public List<SwingInstance> getAllClosedInstances() {
		ArrayList<SwingInstance> result = new ArrayList<>();
		synchronized (instanceManagers) {
			for (SwingInstanceManager im : instanceManagers.values()) {
				result.addAll(im.getAllClosedInstances());
			}
		}
		return result;
	}

	@Override
	public List<SwingInstanceManager> getApplications() {
		ArrayList<SwingInstanceManager> result = new ArrayList<>();
		synchronized (instanceManagers) {
			result.addAll(instanceManagers.values());
		}
		return result;
	}

	@Override
	public URL getWebResource(String resource) {
		if (!isCustomIndexPage() && StringUtils.equals("/index.html", toPath(resource))) {
			resource = System.getProperty(Constants.DEFAULT_WELCOME_PAGE, "/selector/index.html");
		}
		return super.getWebResource(resource);
	}

	private boolean isCustomIndexPage() {
		String customFolder = getConfig().getWebFolder();
		if (StringUtils.isBlank(customFolder)) {
			return false;
		}
		File customFolderFile = resolveFile(customFolder);
		if (customFolderFile.isDirectory() && new File(customFolderFile, "index.html").isFile()) {
			return true;
		}
		return false;
	}

	@GET
	@Path("/apps")
	public List<ApplicationInfoMsg> getApplicationInfo(HttpServletRequest req) throws WsException {
		checkMasterPermission(WebswingAction.rest_getApps);
		List<ApplicationInfoMsg> result = new ArrayList<>();
		for (SwingInstanceManager mgr : getApplications()) {
			if (mgr.isEnabled() && mgr.isUserAuthorized()) {
				ApplicationInfoMsg applicationInfoMsg = mgr.getApplicationInfoMsg();
				if (applicationInfoMsg != null) {
					result.add(applicationInfoMsg);
				}
			}
		}
		return result;
	}

	@GET
	@Path("/info")
	public ApplicationInfo getApplicationInfo() throws WsException {
		checkPermission(WebswingAction.rest_getAppInfo);
		ApplicationInfo app = new ApplicationInfo();
		app.setPath(getPathMapping());
		app.setEnabled(isEnabled());
		app.setUrl(getFullPathMapping());
		app.setName("Server");
		File icon = resolveFile(getConfig().getIcon());
		app.setIcon(CommonUtil.loadImage(icon));
		app.setConfig(getConfig());
		app.setVariables(varSubs.getVariableMap());
		app.setStatus(getStatus());
		return app;
	}

	@GET
	@Path("/rest/paths")
	public List<String> getPaths(HttpServletRequest req) throws WsException {
		checkPermission(WebswingAction.rest_getPaths);
		List<String> result = new ArrayList<>();
		for (SwingInstanceManager appManager : getApplications()) {
			result.add(appManager.getFullPathMapping());
		}
		return result;
	}

	@GET
	@Path("/rest/version")
	public String getVersion() throws WsException {
		return super.getVersion();
	}

	@GET
	@Path("/rest/config")
	public MetaObject getConfigMeta() throws WsException {
		MetaObject meta = super.getConfigMeta();
		return meta;
	}

	@GET
	@Path("/rest/remove")
	public void removeSwingApp(@PathParam("") String path) throws Exception {
		checkMasterPermission(WebswingAction.rest_removeApp);
		if (!StringUtils.isEmpty(path)) {
			SwingInstanceManager swingManager = instanceManagers.get(path);
			if (swingManager != null) {
				if (!swingManager.isEnabled()) {
					configService.removeConfiguration(path);
				} else {
					throw new WsException("Unable to Remove App '" + path + "' while running. Stop the app first");
				}
			}
		} else {
			throw new WsException("Unable to remove App '" + path + "'", HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	@GET
	@Path("/rest/create")
	public void createSwingApp(@PathParam("") String path) throws Exception {
		checkMasterPermission(WebswingAction.rest_createApp);
		if (!StringUtils.isEmpty(path)) {
			SwingInstanceManager swingManager = instanceManagers.get(path);
			if (swingManager == null) {
				Map<String, Object> config=new HashMap<>();
				config.put("enabled",false);
				configService.setConfiguration(path, config);//first create with enabled:false to prevent initiation
				configService.setConfiguration(path,null);//once exists,
			} else {
				throw new WsException("Unable to Create App '" + path + "'. Application already exits.");
			}
		} else {
			throw new WsException("Unable to create App '" + path + "'", HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	@POST
	@Path("/rest/config")
	public void saveConfig(Map<String, Object> config) throws Exception {
		checkMasterPermission(WebswingAction.rest_setConfig);
		config.put("path", "/");
		configService.setConfiguration("/", config);
	}

	@GET
	@Path("/rest/permissions")
	public Map<String, Boolean> getPermissions() throws Exception {
		Map<String, Boolean> perm = super.getPermissions();
		boolean multiApplicationMode = configService.isMultiApplicationMode();
		perm.put("start", isMasterPermited(WebswingAction.rest_getPaths, WebswingAction.rest_getAppInfo, WebswingAction.rest_startApp));
		perm.put("stop", isMasterPermited(WebswingAction.rest_getPaths, WebswingAction.rest_getAppInfo, WebswingAction.rest_stopApp));
		perm.put("remove", multiApplicationMode && isMasterPermited(WebswingAction.rest_getPaths, WebswingAction.rest_getAppInfo, WebswingAction.rest_removeApp));
		perm.put("create", multiApplicationMode && isMasterPermited(WebswingAction.rest_getPaths, WebswingAction.rest_getAppInfo, WebswingAction.rest_createApp));
		perm.put("configEdit", isMasterPermited(WebswingAction.rest_getPaths, WebswingAction.rest_getAppInfo, WebswingAction.rest_getConfig, WebswingAction.rest_setConfig));
		perm.put("logsView", isMasterPermited(WebswingAction.rest_viewLogs));
		return perm;
	}

	@POST
	@Path("/rest/metaConfig")
	public MetaObject getMeta(Map<String, Object> json) throws WsException {
		return super.getMeta(json);
	}

	@GET
	@Path("/rest/variables")
	public Map<String, String> getVariables(@PathParam("") String type) throws WsException {
		return super.getVariables(type);
	}

	@POST
	@Path("/rest/logs")
	public LogResponse getLogs(@PathParam("") String type, LogRequest request) throws WsException {
		checkMasterPermission(WebswingAction.rest_viewLogs);
		if (type.startsWith("/")) {
			type = type.substring(1);
		}
		return LogReaderUtil.readLog(type, request);
	}

	@GET
	@Path("/rest/logs")
	public InputStream downloadLog(@PathParam("") String type) throws WsException {
		checkMasterPermission(WebswingAction.rest_viewLogs);
		if (type.startsWith("/")) {
			type = type.substring(1);
		}
		return LogReaderUtil.getZippedLog(type);
	}

	@GET
	@Path("/rest/CSRFToken")
	public String generateCsrfToken() {
		return super.generateCsrfToken();
	}
}

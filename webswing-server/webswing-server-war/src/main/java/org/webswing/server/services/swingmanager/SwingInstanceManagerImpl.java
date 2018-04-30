package org.webswing.server.services.swingmanager;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.model.c2s.ConnectionHandshakeMsgIn;
import org.webswing.model.s2c.ApplicationInfoMsg;
import org.webswing.model.s2c.SimpleEventMsgOut;
import org.webswing.server.base.PrimaryUrlHandler;
import org.webswing.server.base.UrlHandler;
import org.webswing.server.common.model.SwingConfig;
import org.webswing.server.common.model.admin.ApplicationInfo;
import org.webswing.server.common.model.admin.InstanceManagerStatus;
import org.webswing.server.common.model.admin.Sessions;
import org.webswing.server.common.model.admin.SwingSession;
import org.webswing.server.common.model.meta.MetaObject;
import org.webswing.server.common.util.CommonUtil;
import org.webswing.server.common.util.VariableSubstitutor;
import org.webswing.server.model.exception.WsException;
import org.webswing.server.services.config.ConfigurationService;
import org.webswing.server.services.files.FileTransferHandler;
import org.webswing.server.services.files.FileTransferHandlerService;
import org.webswing.server.services.resources.ResourceHandlerService;
import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.api.AuthorizationConfig;
import org.webswing.server.services.security.api.WebswingAction;
import org.webswing.server.services.security.login.LoginHandlerService;
import org.webswing.server.services.security.modules.SecurityModuleService;
import org.webswing.server.services.stats.StatisticsLogger;
import org.webswing.server.services.stats.StatisticsLoggerService;
import org.webswing.server.services.swinginstance.SwingInstance;
import org.webswing.server.services.swinginstance.SwingInstanceService;
import org.webswing.server.services.websocket.WebSocketConnection;
import org.webswing.server.services.websocket.WebSocketService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SwingInstanceManagerImpl extends PrimaryUrlHandler implements SwingInstanceManager {
	private static final Logger log = LoggerFactory.getLogger(SwingInstanceManagerImpl.class);

	private final String path;
	private final SwingInstanceService instanceFactory;
	private final WebSocketService websocket;
	private final LoginHandlerService loginService;
	private final ResourceHandlerService resourceService;
	private StatisticsLogger statsLogger;
	private FileTransferHandler fileHandler;
	private SwingInstanceSet runningInstances = new SwingInstanceSet();
	private SwingInstanceSet closedInstances = new SwingInstanceSet();

	public SwingInstanceManagerImpl(UrlHandler parent, String path, SwingInstanceService instanceFactory, WebSocketService websocket, FileTransferHandlerService fileService, LoginHandlerService loginService, ResourceHandlerService resourceService, SecurityModuleService securityModuleService, ConfigurationService configService,
			StatisticsLoggerService loggerService) {
		super(parent, securityModuleService, configService);
		this.path = path;
		this.instanceFactory = instanceFactory;
		this.websocket = websocket;
		this.loginService = loginService;
		this.resourceService = resourceService;
		this.statsLogger = loggerService.createLogger();
		this.fileHandler = fileService.create(this);

	}

	@Override
	public void init() {
		registerChildUrlHandler(websocket.createBinaryWebSocketHandler(this, this));
		registerChildUrlHandler(websocket.createJsonWebSocketHandler(this, this));
		registerChildUrlHandler(loginService.createLoginHandler(this));
		registerChildUrlHandler(loginService.createLogoutHandler(this));
		registerChildUrlHandler(fileHandler);
		registerChildUrlHandler(resourceService.create(this, this));
		super.init();
	}

	@Override
	public void destroy() {
		for (SwingInstance i : runningInstances.getAllInstances()) {
			i.kill(0);
		}
		super.destroy();
	}

	@Override
	protected String getPath() {
		return path;
	}

	@Override
	public void connectView(ConnectionHandshakeMsgIn handshake, WebSocketConnection r) {
		try {
			checkAuthorization();
			if (!isEnabled()) {
				throw new WsException("This application is disabled.");
			}
		} catch (WsException e1) {
			log.error("User authorization failed. {}", e1.getMessage());
			r.broadcastMessage(SimpleEventMsgOut.unauthorizedAccess.buildMsgOut());
			return;
		}
		try {
			SwingInstance instance;
			if (handshake.isMirrored()) {
				checkPermissionLocalOrMaster(WebswingAction.websocket_startMirrorView);
				instance = runningInstances.findByInstanceId(handshake.getClientId());
			} else {
				instance = runningInstances.findByInstanceId(handshake, r);
			}
			if (instance != null) {
				instance.connectSwingInstance(r, handshake);
			} else {
				if (handshake.isMirrored()) {
					throw new WsException("Instance not found!");
				} else {
					startSwingInstance(r, handshake);
				}
			}
		} catch (WsException e) {
			log.error("Failed to connect to instance. ", e);
			r.broadcastMessage(SimpleEventMsgOut.configurationError.buildMsgOut());
		}
	}

	private void startSwingInstance(WebSocketConnection r, ConnectionHandshakeMsgIn h) {
		if (r.hasPermission(WebswingAction.websocket_startSwingApplication)) {
			if (!h.isMirrored()) {
				if (!reachedMaxConnections()) {
					try {
						SwingInstance swingInstance = instanceFactory.create(this, fileHandler, h, getSwingConfig(), r);
						runningInstances.add(swingInstance);
					} catch (Exception e) {
						log.error("Failed to create Application instance.", e);
					}
				} else {
					r.broadcastMessage(SimpleEventMsgOut.tooManyClientsNotification.buildMsgOut());
				}
			} else {
				r.broadcastMessage(SimpleEventMsgOut.configurationError.buildMsgOut());
			}
		} else {
			log.error("Authorization error: User " + r.getUser() + " is not authorized to connect to application " + getSwingConfig().getName() + (h.isMirrored() ? " [Mirrored view only available for admin role]" : ""));
		}
	}

	protected void killAll() {
		for (SwingInstance si : runningInstances.getAllInstances()) {
			si.shutdown(true);
		}
	}

	private boolean reachedMaxConnections() {
		if (getSwingConfig().getMaxClients() < 0) {
			return false;
		} else if (getSwingConfig().getMaxClients() == 0) {
			return true;
		} else {
			return runningInstances.size() >= getSwingConfig().getMaxClients();
		}
	}

	public void notifySwingClose(SwingInstance swingInstance) {
		if (!closedInstances.contains(swingInstance)) {
			closedInstances.add(swingInstance);
		}
		runningInstances.remove(swingInstance.getInstanceId());
		swingInstance.logWarningHistory();
		statsLogger.removeInstance(swingInstance.getClientId());
	}

	@Override
	public SwingInstance findInstanceBySessionId(String uuid) {
		return runningInstances.findBySessionId(uuid);
	}

	@Override
	public SwingInstance findInstanceByClientId(String clientId) {
		return runningInstances.findByClientId(clientId);
	}

	@Override
	public List<SwingInstance> getAllInstances() {
		return runningInstances.getAllInstances();
	}

	@Override
	public List<SwingInstance> getAllClosedInstances() {
		return closedInstances.getAllInstances();
	}

	@Override
	public List<SwingInstanceManager> getApplications() {
		return Arrays.asList(this);
	}

	@Override
	public ApplicationInfoMsg getApplicationInfoMsg() {
		if (getConfig().getSwingConfig() == null) {
			return null;
		}
		ApplicationInfoMsg app = new ApplicationInfoMsg();
		app.setName(getSwingConfig().getName());
		app.setUrl(getFullPathMapping());
		File icon = resolveFile(getConfig().getIcon());
		app.setBase64Icon(CommonUtil.loadImage(icon));
		return app;
	}

	@GET
	@Path("/start")
	public void start() throws WsException {
		checkMasterPermission(WebswingAction.rest_startApp);
		if (!isEnabled()) {
			super.initConfiguration();
		}
	}

	@GET
	@Path("/stop")
	public void stop() throws WsException {
		checkMasterPermission(WebswingAction.rest_stopApp);
		if (isEnabled()) {
			super.disable();
		}
	}

	@GET
	@Path("/info")
	public ApplicationInfo getApplicationInfo() throws WsException {
		checkPermissionLocalOrMaster(WebswingAction.rest_getAppInfo);
		ApplicationInfo app = new ApplicationInfo();
		app.setPath(getPathMapping());
		app.setEnabled(isEnabled());
		app.setUrl(getFullPathMapping());
		app.setName(getSwingConfig().getName());
		File icon = resolveFile(getConfig().getIcon());
		app.setIcon(CommonUtil.loadImage(icon));
		app.setConfig(getConfig());
		app.setVariables(varSubs.getVariableMap());
		app.setRunningInstances(runningInstances.size());
		int connected = 0;
		for (SwingInstance si : runningInstances.getAllInstances()) {
			if (si.getSessionId() != null) {
				connected++;
			}
		}
		app.setConnectedInstances(connected);
		app.setFinishedInstances(closedInstances.size());
		int maxRunningInstances = getSwingConfig().getMaxClients();
		app.setMaxRunningInstances(maxRunningInstances);
		app.setStatus(getStatus());
		app.setStats(statsLogger.getSummaryStats());
		app.setWarnings(statsLogger.getSummaryWarnings());
		return app;
	}

	@GET
	@Path("/apps")
	public List<ApplicationInfoMsg> getApplicationInfo(HttpServletRequest req) throws WsException {
		checkPermission(WebswingAction.rest_getApps);
		return Arrays.asList(getApplicationInfoMsg());
	}

	@GET
	@Path("/rest/paths")
	public List<String> getApplications(HttpServletRequest req) throws WsException {
		checkPermissionLocalOrMaster(WebswingAction.rest_getPaths);
		return Arrays.asList(getFullPathMapping());
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

	@POST
	@Path("/rest/config")
	public void setConfig(Map<String, Object> config) throws Exception {
		super.setConfig(config);
	}

	@GET
	@Path("/rest/permissions")
	public Map<String, Boolean> getPermissions() throws Exception {
		return super.getPermissions();
	}

	@GET
	@Path("/rest/sessions")
	public Sessions getSessions() throws WsException {
		checkPermissionLocalOrMaster(WebswingAction.rest_getSession);
		Sessions result = new Sessions();
		for (SwingInstance si : getAllInstances()) {
			result.getSessions().add(si.toSwingSession(false));
		}
		for (SwingInstance si : getAllClosedInstances()) {
			result.getClosedSessions().add(si.toSwingSession(false));
		}
		return result;
	}

	@GET
	@Path("/rest/session")
	public SwingSession getSession(@PathParam("") String id) throws WsException {
		checkPermissionLocalOrMaster(WebswingAction.rest_getSession);
		if (id.startsWith("/")) {
			id = id.substring(1);
		}
		SwingInstance instance = runningInstances.findByInstanceId(id);
		if (instance != null) {
			return instance.toSwingSession(true);
		}
		return null;
	}

	@GET
	@Path("/rest/record")
	public SwingSession startRecording(@PathParam("") String id) throws WsException {
		checkPermissionLocalOrMaster(WebswingAction.rest_startRecording);
		if (id.startsWith("/")) {
			id = id.substring(1);
		}
		SwingInstance instance = runningInstances.findByInstanceId(id);
		if (instance != null) {
			instance.startRecording();
			return instance.toSwingSession(true);
		}
		return null;
	}

	@GET
	@Path("/rest/threadDump")
	public String getThreadDump(@PathParam("") String id, @QueryParam("id") String timestamp) throws WsException {
		checkPermissionLocalOrMaster(WebswingAction.rest_getThreadDump);
		if (id.startsWith("/")) {
			id = id.substring(1);
		}
		SwingInstance instance = runningInstances.findByInstanceId(id);
		if (instance != null) {
			return instance.getThreadDump(timestamp);
		}else{
			List<SwingInstance> instaces = closedInstances.getAllInstances();//closed instances can have multiple instances for same id, need to manually check all
			for(SwingInstance i:instaces){
				if(id.equals(i.getInstanceId())){
					String td = i.getThreadDump(timestamp);
					if(td!=null){
						return td;
					}
				}
			}
			throw new WsException("Not found", 404);
		}
	}

	@POST
	@Path("/rest/threadDump")
	public void requestThreadDump(@PathParam("") String id) throws WsException {
		checkPermissionLocalOrMaster(WebswingAction.rest_requestThreadDump);
		if (id.startsWith("/")) {
			id = id.substring(1);
		}
		SwingInstance instance = runningInstances.findByInstanceId(id);
		if (instance != null) {
			instance.requestThreadDump();
		}
	}

	@DELETE
	@Path("/rest/session")
	public void shutdown(@PathParam("") String id, @QueryParam("force") String forceKill) throws WsException {
		boolean force = Boolean.parseBoolean(forceKill);
		if (force) {
			checkPermissionLocalOrMaster(WebswingAction.rest_sessionShutdown);
		} else {
			checkPermissionLocalOrMaster(WebswingAction.rest_sessionShutdownForce);
		}
		if (id.startsWith("/")) {
			id = id.substring(1);
		}
		SwingInstance instance = runningInstances.findByInstanceId(id);
		if (instance != null) {
			instance.shutdown(force);
		} else {
			throw new WsException("Instance with id " + id + " not found.");
		}
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

	@GET
	@Path("/rest/CSRFToken")
	public String generateCsrfToken() {
		return super.generateCsrfToken();
	}

	@GET
	@Path("/rest/ping")
	public void ping() {
		//responds with 200 OK status
	}

	@Override
	public void logStatValue(String instance, String name, Number value) {
		statsLogger.log(instance, name, value);
	}

	@Override
	public Map<String, Map<Long, Number>> getInstanceStats(String instance) {
		return statsLogger.getInstanceStats(instance);
	}

	@Override
	public Map<String, Number> getInstanceMetrics(String clientId) {
		return statsLogger.getInstanceMetrics(clientId);
	}

	@Override
	public List<String> getInstanceWarnings(String instance) {
		return statsLogger.getInstanceWarnings(instance);
	}

	@Override
	public List<String> getInstanceWarningHistory(String instance) {
		return statsLogger.getInstanceWarningHistory(instance);
	}

	private void checkAuthorization() throws WsException {
		if (!isUserAuthorized()) {
			throw new WsException("User '" + getUser() + "' is not authorized to access application " + getPathMapping(), HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	public boolean isUserAuthorized() {
		AbstractWebswingUser user = getUser();
		if (user == null) {
			return false;
		}
		AuthorizationConfig authorizationConfig = getSecurityConfig().getAuthorizationConfig();
		if (authorizationConfig == null || (authorizationConfig.getRoles().size() == 0 && authorizationConfig.getUsers().size() == 0)) {
			return true;
		} else {
			VariableSubstitutor subs = VariableSubstitutor.forSwingApp(getConfig());
			for (String role : authorizationConfig.getRoles()) {
				String resolvedRole = subs.replace(role);
				if (user.hasRole(resolvedRole)) {
					return true;
				}
			}
			for (String u : authorizationConfig.getUsers()) {
				String resolvedUser = subs.replace(u);
				if (user.getUserId().equals(resolvedUser)) {
					return true;
				}
			}
		}
		return false;
	}

}

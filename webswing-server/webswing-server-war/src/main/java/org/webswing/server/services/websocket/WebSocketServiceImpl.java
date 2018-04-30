package org.webswing.server.services.websocket;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.cpr.AtmosphereInterceptor;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
import org.atmosphere.util.VoidServletConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.base.PrimaryUrlHandler;
import org.webswing.server.base.UrlHandler;
import org.webswing.server.base.WebswingService;
import org.webswing.server.model.exception.WsInitException;
import org.webswing.server.services.playback.RecordingPlaybackUrlHandlerImpl;
import org.webswing.server.services.security.SecurityManagerService;
import org.webswing.server.services.swingmanager.SwingInstanceManager;
import org.webswing.server.util.ServerUtil;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class WebSocketServiceImpl implements WebswingService, WebSocketService {
	private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);
	private static final String WEBSOCKET_MESSAGE_SIZE = System.getProperty(Constants.WEBSOCKET_MESSAGE_SIZE, "65536");
	private static final String WEBSOCKET_THREADPOOL_SIZE = System.getProperty(Constants.WEBSOCKET_THREAD_POOL, "10");
	private static final Class<?>[] jsonInterceptors = new Class<?>[] { AtmosphereResourceLifecycleInterceptor.class, TrackMessageSizeInterceptor.class, SuspendTrackerInterceptor.class };
	private static final Class<?>[] binaryInterceptors = new Class<?>[] { AtmosphereResourceLifecycleInterceptor.class, SuspendTrackerInterceptor.class };
	private static final String BINARY_HANDLER_PATH = "/async/swing-bin";
	private static final String JSON_HANDLER_PATH = "/async/swing";
	private static final String PLAYBACK_HANDLER_WS_PATH = "/async/swing-play";
	private static final String PLAYBACK_HANDLER_PATH = "/playback" + PLAYBACK_HANDLER_WS_PATH;

	private final SecuredAtmosphereFramework framework;
	private final ServletContext context;

	private Map<String, WebSocketAtmosphereHandler> websocketEndpoints = new HashMap<>();

	@Inject
	public WebSocketServiceImpl(ServletContext context, SecurityManagerService securityManager) {
		this.context = context;
		this.framework = new SecuredAtmosphereFramework(securityManager);

	}

	public void start() throws WsInitException {

		Map<String, String> initParams = new HashMap<String, String>();
		//initParams.put("org.atmosphere.websocket.bufferSize", WEBSOCKET_MESSAGE_SIZE);
		initParams.put("org.atmosphere.websocket.maxTextMessageSize", WEBSOCKET_MESSAGE_SIZE);
		initParams.put("org.atmosphere.websocket.maxBinaryMessageSize", WEBSOCKET_MESSAGE_SIZE);
		initParams.put("org.atmosphere.cpr.broadcaster.maxProcessingThreads", WEBSOCKET_THREADPOOL_SIZE);
		initParams.put("org.atmosphere.cpr.broadcaster.maxAsyncWriteThreads", WEBSOCKET_THREADPOOL_SIZE);
		initParams.put("org.atmosphere.cpr.broadcaster.shareableThreadPool", "true");
		initParams.put("org.atmosphere.cpr.scanClassPath", "false");
		initParams.put("org.atmosphere.cpr.AtmosphereFramework.analytics", "false");
		initParams.put("org.atmosphere.cpr.broadcasterCacheClass", "org.atmosphere.cache.UUIDBroadcasterCache");
		initParams.put("org.atmosphere.container.JSR356AsyncSupport.mappingPath", "/{PATH}");

		try {

			framework.init(new VoidServletConfig(initParams) {
				@Override
				public ServletContext getServletContext() {
					return context;
				}
				@Override
				public String getServletName() {
					return "WebswingServlet";
				}
			}, false);

			initParams.put("org.atmosphere.container.JSR356AsyncSupport.mappingPath", ServerUtil.getContextPath(context));

			websocketEndpoints.put(JSON_HANDLER_PATH, new WebSocketAtmosphereHandler());
			websocketEndpoints.put(BINARY_HANDLER_PATH, new WebSocketAtmosphereHandler());
			websocketEndpoints.put(PLAYBACK_HANDLER_PATH, new WebSocketAtmosphereHandler());
			framework.addAtmosphereHandler("*" + JSON_HANDLER_PATH, websocketEndpoints.get(JSON_HANDLER_PATH), instantiate(jsonInterceptors));
			framework.addAtmosphereHandler("*" + BINARY_HANDLER_PATH, websocketEndpoints.get(BINARY_HANDLER_PATH), instantiate(binaryInterceptors));
			framework.addAtmosphereHandler("*" + PLAYBACK_HANDLER_WS_PATH, websocketEndpoints.get(PLAYBACK_HANDLER_PATH), instantiate(binaryInterceptors));

		} catch (ServletException e) {
			throw new WsInitException("Failed to initialize Websocket framework", e);
		}
	}

	public void stop() {
		framework.destroy();
	}

	private List<AtmosphereInterceptor> instantiate(Class<?>[] interceptors) {
		List<AtmosphereInterceptor> result = new ArrayList<AtmosphereInterceptor>();
		for (Class<?> c : interceptors) {
			if (AtmosphereInterceptor.class.isAssignableFrom(c)) {
				try {
					Constructor<?> constructor = c.getConstructor();
					result.add((AtmosphereInterceptor) constructor.newInstance());
				} catch (Exception e) {
					log.error("Failed to initialize Atmosphere Interceptors.", e);
				}
			}
		}
		return result;
	}

	@Override
	public WebSocketUrlHandler createBinaryWebSocketHandler(PrimaryUrlHandler parent, SwingInstanceManager instanceManager) {
		WebSocketUrlHandlerImpl result = new WebSocketUrlHandlerImpl(parent, BINARY_HANDLER_PATH, this, instanceManager);
		WebSocketAtmosphereHandler endpoint = websocketEndpoints.get(BINARY_HANDLER_PATH);
		endpoint.addHandler(result.getFullPathMapping(), result);
		return result;
	}

	@Override
	public WebSocketUrlHandler createJsonWebSocketHandler(PrimaryUrlHandler parent, SwingInstanceManager instanceManager) {
		WebSocketUrlHandlerImpl result = new WebSocketUrlHandlerImpl(parent, JSON_HANDLER_PATH, this, instanceManager);
		WebSocketAtmosphereHandler endpoint = websocketEndpoints.get(JSON_HANDLER_PATH);
		endpoint.addHandler(result.getFullPathMapping(), result);
		return result;
	}

	@Override
	public WebSocketUrlHandler createPlaybackWebSocketHandler(PrimaryUrlHandler parent) {
		RecordingPlaybackUrlHandlerImpl result = new RecordingPlaybackUrlHandlerImpl(parent, PLAYBACK_HANDLER_PATH, this);
		WebSocketAtmosphereHandler endpoint = websocketEndpoints.get(PLAYBACK_HANDLER_PATH);
		endpoint.addHandler(result.getFullPathMapping(), result);
		return result;
	}

	public void serve(WebSocketUrlHandler handler, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		WebSocketAtmosphereHandler endpoint = websocketEndpoints.get(handler.getPathMapping());
		endpoint.addHandler(req, handler);
		framework.doCometSupport(AtmosphereRequest.wrap(req), AtmosphereResponse.wrap(res));
	}

}

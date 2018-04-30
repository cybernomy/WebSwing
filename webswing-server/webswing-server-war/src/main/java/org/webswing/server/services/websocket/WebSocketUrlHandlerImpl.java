package org.webswing.server.services.websocket;

import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.model.MsgIn;
import org.webswing.model.c2s.*;
import org.webswing.model.jslink.JavaEvalRequestMsgIn;
import org.webswing.model.jslink.JsResultMsg;
import org.webswing.model.s2c.AppFrameMsgOut;
import org.webswing.model.s2c.ApplicationInfoMsg;
import org.webswing.model.s2c.SimpleEventMsgOut;
import org.webswing.server.base.AbstractUrlHandler;
import org.webswing.server.base.PrimaryUrlHandler;
import org.webswing.server.base.UrlHandler;
import org.webswing.server.model.EncodedMessage;
import org.webswing.server.model.exception.WsException;
import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.api.WebswingAction;
import org.webswing.server.services.security.login.SecuredPathHandler;
import org.webswing.server.services.stats.StatisticsLogger;
import org.webswing.server.services.swinginstance.SwingInstance;
import org.webswing.server.services.swingmanager.SwingInstanceManager;
import org.webswing.server.util.ServerUtil;

public class WebSocketUrlHandlerImpl implements WebSocketUrlHandler {
	private static final Logger log = LoggerFactory.getLogger(WebSocketUrlHandlerImpl.class);

	private final PrimaryUrlHandler parent;
	private final WebSocketService websocket;
	private final SwingInstanceManager instanceManager;
	private final String path;

	private boolean ready;

	public WebSocketUrlHandlerImpl(PrimaryUrlHandler parent, String path, WebSocketService websocket, SwingInstanceManager instanceManager) {
		this.parent = parent;
		this.path = path;
		this.websocket = websocket;
		this.instanceManager = instanceManager;
	}

	@Override
	public void init() {
		ready = true;
	}

	@Override
	public void destroy() {
		ready = false;
	}

	@Override
	public boolean serve(HttpServletRequest req, HttpServletResponse res) throws WsException {
		return serveDefault(req, res, parent, websocket, log);
	}

	public void onReady(final WebSocketConnection r) {
		if (r.hasPermission(WebswingAction.websocket_connect)) {
			AppFrameMsgOut appInfo = new AppFrameMsgOut();
			ApplicationInfoMsg applicationInfoMsg = instanceManager.getApplicationInfoMsg();
			if (applicationInfoMsg != null) {
				appInfo.setApplications(Arrays.asList(applicationInfoMsg));
			}
			appInfo.setSessionId(r.uuid());
			EncodedMessage encoded = new EncodedMessage(appInfo);
			if (r.isBinary()) {
				r.write(encoded.getProtoMessage());
			} else {
				r.write(encoded.getJsonMessage());
			}
		} else {
			r.broadcastMessage(SimpleEventMsgOut.unauthorizedAccess.buildMsgOut());
			r.disconnect();
		}
	}

	public void onDisconnect(WebSocketConnection r) {
		SwingInstance instance = instanceManager.findInstanceBySessionId(r.uuid());
		if (instance != null) {
			instance.webSessionDisconnected(r.uuid());
		}
	}

	public void onMessage(WebSocketConnection r, Object message) {
		try {
			int length = 0;
			Object decodedMessage = null;
			if (message instanceof String) {
				length = ((String) message).getBytes().length;
				decodedMessage = ServerUtil.decodeJson((String) message);
			} else if (message instanceof byte[]) {
				length = ((byte[]) message).length;
				decodedMessage = ServerUtil.decodeProto((byte[]) message);
			}
			if (decodedMessage != null && decodedMessage instanceof InputEventsFrameMsgIn) {
				InputEventsFrameMsgIn frame = (InputEventsFrameMsgIn) decodedMessage;
				if (frame.getEvents() != null && frame.getEvents().size() > 0) {
					List<InputEventMsgIn> evts = frame.getEvents();
					for (InputEventMsgIn evt : evts) {
						if (evt.getHandshake() != null) {
							instanceManager.connectView(evt.getHandshake(), r);
						} else if (evt.getKey() != null) {
							send(r, evt.getKey());
						} else if (evt.getMouse() != null) {
							send(r, evt.getMouse());
						} else if (evt.getEvent() != null) {
							send(r, evt.getEvent());
						} else if (evt.getTimestamps() != null) {
							send(r, evt.getTimestamps());
						}
					}
				} else if (frame.getPaste() != null) {
					PasteEventMsgIn p = frame.getPaste();
					send(r, p);
				} else if (frame.getCopy() != null) {
					CopyEventMsgIn p = frame.getCopy();
					send(r, p);
				} else if (frame.getSelected() != null) {
					FilesSelectedEventMsgIn p = frame.getSelected();
					send(r, p);
				} else if (frame.getJsResponse() != null) {
					JsResultMsg p = frame.getJsResponse();
					send(r, p);
				} else if (frame.getJavaRequest() != null) {
					JavaEvalRequestMsgIn p = frame.getJavaRequest();
					send(r, p);
				} else if (frame.getPixelsResponse() != null) {
					PixelsAreaResponseMsgIn p = frame.getPixelsResponse();
					send(r, p);
				}
			} else {
				log.error("Unable to decode message: " + message);
			}
			SwingInstance instance = instanceManager.findInstanceBySessionId(r.uuid());
			if (instance != null) {
				instance.logStatValue(StatisticsLogger.INBOUND_SIZE_METRIC, length);
			}
		} catch (Exception e) {
			log.error("Exception while processing websocket message.", e);
		}
	}

	public void onTimeout(WebSocketConnection event) {
	}

	private void send(WebSocketConnection r, MsgIn o) {
		SwingInstance instance = instanceManager.findInstanceBySessionId(r.uuid());
		if (instance != null) {
			instance.sendToSwing(r, o);
		}
	}

	@Override
	public UrlHandler getOwner() {
		return parent;
	}

	@Override
	public void registerFirstChildUrlHandler(UrlHandler handler) {
	}

	@Override
	public void registerChildUrlHandler(UrlHandler handler) {
	}

	@Override
	public void removeChildUrlHandler(UrlHandler Handler) {
	}

	@Override
	public long getLastModified(HttpServletRequest req) {
		return -1;
	}

	@Override
	public String getPathMapping() {
		return AbstractUrlHandler.toPath(path);
	}

	@Override
	public String getFullPathMapping() {
		return parent.getFullPathMapping() + getPathMapping();
	}

	@Override
	public String getSecuredPath() {
		return parent.getSecuredPath();
	}

	@Override
	public UrlHandler getRootHandler() {
		return parent.getRootHandler();
	}

	@Override
	public AbstractWebswingUser getUser() {
		return parent.getUser();
	}

	@Override
	public void checkPermission(WebswingAction action) throws WsException {
		parent.checkPermission(action);
	}

	@Override
	public SecuredPathHandler getSecurityProvider() {
		return parent.getSecurityProvider();
	}

	@Override
	public ServletContext getServletContext() {
		return parent.getServletContext();
	}

	@Override
	public void checkMasterPermission(WebswingAction action) throws WsException {
		parent.checkMasterPermission(action);
	}

	@Override
	public boolean isReady() {
		return ready;
	}
}

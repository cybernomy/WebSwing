package org.webswing.server.services.playback;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.model.c2s.InputEventsFrameMsgIn;
import org.webswing.model.s2c.AppFrameMsgOut;
import org.webswing.model.s2c.SimpleEventMsgOut;
import org.webswing.server.base.AbstractUrlHandler;
import org.webswing.server.base.PrimaryUrlHandler;
import org.webswing.server.base.UrlHandler;
import org.webswing.server.model.EncodedMessage;
import org.webswing.server.model.exception.WsException;
import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.api.WebswingAction;
import org.webswing.server.services.security.login.SecuredPathHandler;
import org.webswing.server.services.websocket.WebSocketConnection;
import org.webswing.server.services.websocket.WebSocketService;
import org.webswing.server.services.websocket.WebSocketUrlHandler;
import org.webswing.server.util.ServerUtil;

public class RecordingPlaybackUrlHandlerImpl implements WebSocketUrlHandler {

	private static final Logger log = LoggerFactory.getLogger(RecordingPlaybackUrlHandlerImpl.class);
	private static Map<String, SessionRecordingPlayback> playbackMap = new HashMap<String, SessionRecordingPlayback>();
	private final PrimaryUrlHandler parent;
	private final WebSocketService websocket;
	private final String path;
	private boolean ready;

	public RecordingPlaybackUrlHandlerImpl(PrimaryUrlHandler parent, String path, WebSocketService websocket) {
		this.parent = parent;
		this.path = path;
		this.websocket = websocket;
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
			if (r.hasPermission(WebswingAction.websocket_startRecordingPlayback)) {
				String file = r.getRequest().getParameter("file");
				File recordingFile = new File(file);
				try {
					recordingFile = new File(recordingFile.getParentFile(), URLEncoder.encode(recordingFile.getName(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					log.error("Could not open recording file: " + recordingFile.getAbsolutePath(), e);

				}
				if (recordingFile.exists() && recordingFile.canRead()) {
					SessionRecordingPlayback playback = new SessionRecordingPlayback(r, recordingFile);
					playbackMap.put(r.uuid(), playback);
					appInfo.setApplications(Arrays.asList(playback.getApplicationInfo()));
				} else {
					log.error("Could not open recording file: " + recordingFile.getAbsolutePath());
				}
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
		SessionRecordingPlayback pbs = playbackMap.remove(r.uuid());
		if (pbs != null) {
			pbs.close();
		}
	}

	public void onMessage(WebSocketConnection r, Object message) {
		try {
			SessionRecordingPlayback pb = playbackMap.get(r.uuid());
			if (pb != null) {
				Object decodedMessage = null;
				if (message instanceof byte[]) {
					decodedMessage = ServerUtil.decodeProto((byte[]) message);
				}
				if (decodedMessage != null && decodedMessage instanceof InputEventsFrameMsgIn) {
					InputEventsFrameMsgIn frame = (InputEventsFrameMsgIn) decodedMessage;
					if (frame.getPlayback() != null) {
						pb.handlePlaybackControl(frame.getPlayback());
					}
				}
			}
		} catch (Exception e) {
			log.error("Exception while processing websocket message.", e);
		}
	}

	public void onTimeout(WebSocketConnection event) {
	}

	@Override
	public UrlHandler getOwner() {
		return this;
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
	public ServletContext getServletContext() {
		return parent.getServletContext();
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
	public void checkMasterPermission(WebswingAction action) throws WsException {
		parent.checkMasterPermission(action);
	}

	@Override
	public boolean isReady() {
		return ready;
	}
}

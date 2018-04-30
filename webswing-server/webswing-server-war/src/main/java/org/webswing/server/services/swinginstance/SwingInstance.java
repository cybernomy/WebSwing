package org.webswing.server.services.swinginstance;

import org.webswing.model.MsgIn;
import org.webswing.model.MsgOut;
import org.webswing.model.c2s.ConnectionHandshakeMsgIn;
import org.webswing.server.common.model.SwingConfig;
import org.webswing.server.common.model.admin.SwingSession;
import org.webswing.server.services.websocket.WebSocketConnection;

public interface SwingInstance {

	String getInstanceId();

	String getSessionId();

	String getMirrorSessionId();

	String getClientId();

	String getUser();

	SwingConfig getAppConfig();

	void connectSwingInstance(WebSocketConnection r, ConnectionHandshakeMsgIn h);

	void sendToWeb(MsgOut o);

	boolean sendToSwing(WebSocketConnection r, MsgIn h);

	void notifyExiting();

	void shutdown(boolean force);

	void kill(int waitMs);

	void webSessionDisconnected(String connectionId);

	void logStatValue(String name, Number value);

	SwingSession toSwingSession(boolean stats);

	void logWarningHistory();

	void startRecording();

	String getThreadDump(String id);

	void requestThreadDump();
}
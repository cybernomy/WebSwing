package org.webswing.server.services.swinginstance;

import org.webswing.model.c2s.ConnectionHandshakeMsgIn;
import org.webswing.server.common.model.SwingConfig;
import org.webswing.server.model.exception.WsException;
import org.webswing.server.services.files.FileTransferHandler;
import org.webswing.server.services.swingmanager.SwingInstanceManager;
import org.webswing.server.services.websocket.WebSocketConnection;

public interface SwingInstanceService {

	SwingInstance create(SwingInstanceManager manager, FileTransferHandler fileHandler, ConnectionHandshakeMsgIn h, SwingConfig config, WebSocketConnection r) throws WsException;
}
package org.webswing.server.services.swinginstance;

import org.webswing.model.c2s.ConnectionHandshakeMsgIn;
import org.webswing.server.common.model.SwingConfig;
import org.webswing.server.model.exception.WsException;
import org.webswing.server.services.files.FileTransferHandler;
import org.webswing.server.services.jvmconnection.JvmConnectionService;
import org.webswing.server.services.swingmanager.SwingInstanceManager;
import org.webswing.server.services.swingprocess.SwingProcessService;
import org.webswing.server.services.websocket.WebSocketConnection;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SwingInstanceServiceImpl implements SwingInstanceService {

	private final JvmConnectionService connectionService;
	private final SwingProcessService processService;

	@Inject
	public SwingInstanceServiceImpl(SwingProcessService processService, JvmConnectionService connectionService) {
		this.processService = processService;
		this.connectionService = connectionService;
	}

	@Override
	public SwingInstance create(SwingInstanceManager manager, FileTransferHandler fileHandler, ConnectionHandshakeMsgIn h, SwingConfig config, WebSocketConnection r) throws WsException {
		return new SwingInstanceImpl(manager, fileHandler, processService, connectionService, h, config, r);
	}

}

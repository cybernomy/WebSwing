package org.webswing.server.services.jvmconnection;

import org.webswing.server.model.exception.WsException;

public interface JvmConnectionService {

	JvmConnection connect(String connectionId, JvmListener listener) throws WsException;
}

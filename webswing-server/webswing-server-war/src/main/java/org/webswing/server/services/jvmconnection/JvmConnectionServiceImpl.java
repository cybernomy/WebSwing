package org.webswing.server.services.jvmconnection;

import org.webswing.server.model.exception.WsException;

import com.google.inject.Singleton;

@Singleton
public class JvmConnectionServiceImpl implements JvmConnectionService {

	@Override
	public JvmConnection connect(String connectionId, JvmListener listener) throws WsException {
		return new JvmConnectionImpl(connectionId, listener);
	}

}

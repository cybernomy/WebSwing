package org.webswing.server.base;

import org.webswing.server.model.exception.WsInitException;

public interface WebswingService{
	void start() throws WsInitException;

	void stop();

}

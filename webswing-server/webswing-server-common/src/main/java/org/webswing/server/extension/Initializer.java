package org.webswing.server.extension;

import org.webswing.server.model.exception.WsInitException;

public interface Initializer {

	void start() throws WsInitException;

}

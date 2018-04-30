package org.webswing.server.services.jvmconnection;

import java.io.Serializable;

public interface JvmConnection {
	void send(Serializable o);

	void close();
}

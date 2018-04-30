package org.webswing.ext.services;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeoutException;

public interface ServerConnectionService {

	void sendObject(Serializable jsonPaintRequest);

	Object sendObjectSync(Serializable o, String correlationId) throws TimeoutException, IOException;

	void disconnect();

	void resetInactivityTimers();

	void messageApiPublish(Serializable o) throws IOException;
}

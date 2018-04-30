package org.webswing.server.services.jvmconnection;

import java.io.Serializable;

public interface JvmListener {

	void onJvmMessage(Serializable o);

}

package org.webswing.server.services.swingmanager;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.webswing.model.c2s.ConnectionHandshakeMsgIn;
import org.webswing.model.s2c.ApplicationInfoMsg;
import org.webswing.server.base.UrlHandler;
import org.webswing.server.common.model.SecuredPathConfig;
import org.webswing.server.services.security.login.SecuredPathHandler;
import org.webswing.server.services.swinginstance.SwingInstance;
import org.webswing.server.services.websocket.WebSocketConnection;

public interface SwingInstanceManager extends SecuredPathHandler, SwingInstanceHolder, UrlHandler {

	File resolveFile(String name);

	SecuredPathConfig getConfig();

	ApplicationInfoMsg getApplicationInfoMsg();

	void connectView(ConnectionHandshakeMsgIn handshake, WebSocketConnection r);

	void notifySwingClose(SwingInstance swingAppInstance);

	void logStatValue(String instance, String name, Number value);

	/**
	 * @return Map &lt; name_of_metric, Map &lt; timestamp, value &gt; &gt;
 	 */
	Map<String, Map<Long, Number>> getInstanceStats(String instance);

	/**
	 * @return Map &lt; name_of_metric, value &gt;
	 */
	Map<String, Number> getInstanceMetrics(String clientId);

	List<String> getInstanceWarnings(String instance);

	List<String> getInstanceWarningHistory(String clientId);

	boolean isUserAuthorized();

	void disable();

}

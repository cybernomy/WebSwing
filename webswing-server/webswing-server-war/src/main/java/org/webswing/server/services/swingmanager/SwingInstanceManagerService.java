package org.webswing.server.services.swingmanager;

import org.webswing.server.base.UrlHandler;

public interface SwingInstanceManagerService {

	SwingInstanceManager createApp(UrlHandler parent, String config);
}

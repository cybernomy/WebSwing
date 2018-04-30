package org.webswing.server.services.websocket;

import org.webswing.server.base.PrimaryUrlHandler;
import org.webswing.server.base.WebswingService;
import org.webswing.server.services.swingmanager.SwingInstanceManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface WebSocketService extends WebswingService {
	
	WebSocketUrlHandler createBinaryWebSocketHandler(PrimaryUrlHandler parent, SwingInstanceManager instanceHolder);

	WebSocketUrlHandler createJsonWebSocketHandler(PrimaryUrlHandler parent, SwingInstanceManager instanceHolder);

	WebSocketUrlHandler createPlaybackWebSocketHandler(PrimaryUrlHandler parent);

	void serve(WebSocketUrlHandler handler, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException;
}

package org.webswing.server.services.websocket;

import org.slf4j.Logger;
import org.webswing.Constants;
import org.webswing.server.base.PrimaryUrlHandler;
import org.webswing.server.base.UrlHandler;
import org.webswing.server.model.exception.WsException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WebSocketUrlHandler extends UrlHandler, WebSocketMessageListener {

	default boolean serveDefault(HttpServletRequest req, HttpServletResponse res, PrimaryUrlHandler parent, WebSocketService websocket, Logger log) throws WsException {
		if (parent.isSameOrigin(req) || parent.isOriginAllowed(req.getHeader("Origin"))) {
			if (parent.validateCsrfToken(req)) {
				try {
					websocket.serve(this, req, res);
					return true;
				} catch (Exception e) {
					log.error("WebSocket failed.", e);
					throw new WsException("WebSocket failed.", e);
				}
			} else {
				WsException e = new WsException("Invalid CSRF token", HttpServletResponse.SC_FORBIDDEN);
				log.error("Websocket connection failed: Invalid CSRF token. Received:" + req.getHeader(Constants.HTTP_ATTR_CSRF_TOKEN_HEADER) + " Expected:" + parent.generateCsrfToken());
				throw e;
			}
		} else {
			WsException e = new WsException("Invalid request origin", HttpServletResponse.SC_FORBIDDEN);
			log.error("Websocket connection failed: Invalid request origin. Origin:" + req.getHeader("Origin") + " Host:" + req.getHeader("Host") + " X-Forwarded-Host:" + req.getHeader("X-Forwarded-Host"));
			throw e;
		}
	}
}

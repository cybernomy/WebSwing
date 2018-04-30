package org.webswing.server.services.websocket;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResource.TRANSPORT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.model.MsgOut;
import org.webswing.server.base.UrlHandler;
import org.webswing.server.model.EncodedMessage;
import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.api.WebswingAction;
import org.webswing.server.util.SecurityUtil;

public class WebSocketConnection {
	private static final Logger log = LoggerFactory.getLogger(WebSocketConnection.class);

	private AtmosphereResource resource;
	private UrlHandler handler;

	public WebSocketConnection(AtmosphereResource resource, UrlHandler handler) {
		this.resource = resource;
		this.handler = handler;
	}

	public boolean isBinary() {
		return resource.forceBinaryWrite();
	}

	public void write(byte[] protoMessage) {
		resource.write(protoMessage);
	}

	public void write(String jsonMessage) {
		resource.write(jsonMessage);
	}

	public String uuid() {
		return resource.uuid();
	}

	public HttpServletRequest getRequest() {
		return resource.getRequest();
	}

	public AbstractWebswingUser getUser() {
		return SecurityUtil.getUser(this);
	}

	public void broadcastMessage(EncodedMessage o) {
		broadcast(isBinary() ? o.getProtoMessage() : o.getJsonMessage());
	}

	public void broadcastMessage(MsgOut o) {
		broadcastMessage(new EncodedMessage(o));
	}

	private void broadcast(Serializable serializable) {
		for (AtmosphereResource r : this.resource.getBroadcaster().getAtmosphereResources()) {
			if (r.uuid().equals(this.resource.uuid())) {
				r.getBroadcaster().broadcast(serializable, r);
			}
		}
	}

	public UrlHandler getHandler() {
		return handler;
	}

	public boolean hasPermission(WebswingAction action) {
		return getUser() == null ? false : getUser().isPermitted(action.name());
	}

	public void disconnect() {
		try {
			resource.close();
		} catch (IOException e) {
			log.error("Failed to close websocket connection " + resource.uuid());
		}
	}
	
	public boolean isWebsocketTransport(){
		return TRANSPORT.WEBSOCKET.equals(resource.transport());
	}

}

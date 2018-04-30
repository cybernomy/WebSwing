package org.webswing.server.services.startup;

import org.webswing.server.extension.Initializer;
import org.webswing.server.model.exception.WsInitException;
import org.webswing.server.services.config.ConfigurationService;
import org.webswing.server.services.jms.JmsService;
import org.webswing.server.services.websocket.WebSocketService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StartupServiceImpl implements StartupService {
	private final JmsService jms;
	private final WebSocketService websocket;
	private final ConfigurationService config;

	@Inject
	public StartupServiceImpl(Initializer ini, JmsService jms, WebSocketService websocket, ConfigurationService config) {
		//Initializer setup invoked in constructor 
		//this.ini = ini;
		this.jms = jms;
		this.websocket = websocket;
		this.config = config;
	}

	public void start() throws WsInitException {
		try {
			jms.start();
			websocket.start();
			config.start();
		} catch (WsInitException e) {
			throw e;
		} catch (Exception e) {
			throw new WsInitException("Failed to start Webswing. " + e.getMessage(), e);
		}
	}

	public void stop() {
		config.stop();
		websocket.stop();
		jms.stop();
	}
}

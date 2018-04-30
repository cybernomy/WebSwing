package org.webswing.server.services.websocket;

import static org.atmosphere.util.IOUtils.readEntirely;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter.OnClose;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter.OnResume;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter.OnSuspend;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.util.IOUtils;
import org.atmosphere.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.server.base.UrlHandler;

class WebSocketAtmosphereHandler implements AtmosphereHandler {

	private static final Logger log = LoggerFactory.getLogger(WebSocketAtmosphereHandler.class);

	private Map<String, WebSocketMessageListener> wsHandler = new HashMap<>();
	private Map<String, WebSocketConnection> connectionMap = new HashMap<String, WebSocketConnection>();

	public WebSocketAtmosphereHandler() {
	}

	public void addHandler(HttpServletRequest req, WebSocketMessageListener h) {
		wsHandler.put(req.getRequestURI(), h);
	}

	public void addHandler(String path, WebSocketMessageListener h) {
		wsHandler.put(path, h);
	}

	public void onReady(final AtmosphereResource r) {
		findHandler(r).onReady(getConnection(r));
	}

	public void onDisconnect(AtmosphereResourceEvent event) {
		findHandler(event.getResource(), false).onDisconnect(getConnection(event));
	}

	public void onMessage(AtmosphereResource r, Object message) {
		findHandler(r).onMessage(getConnection(r), message);

	}

	public void onTimeout(AtmosphereResourceEvent event) {
		findHandler(event.getResource()).onTimeout(getConnection(event));
	}

	private WebSocketConnection getConnection(AtmosphereResourceEvent event) {
		WebSocketConnection c = getConnection(event.getResource());
		return c;
	}

	private WebSocketConnection getConnection(AtmosphereResource r) {
		WebSocketConnection result = null;
		if (connectionMap.containsKey(r.uuid())) {
			result = connectionMap.get(r.uuid());
		} else {
			result = new WebSocketConnection(r, findHandler(r, false).getOwner());
			connectionMap.put(r.uuid(), result);
		}
		return result;
	}

	private WebSocketMessageListener findHandler(AtmosphereResource r) {
		return findHandler(r, true);
	}

	private WebSocketMessageListener findHandler(AtmosphereResource r, boolean close) {
		WebSocketMessageListener h = wsHandler.get(r.getRequest().getRequestURI());
		try {
			if (h != null) {
				if (h.isReady()) {
					return h;
				} else {
					if (!r.isCancelled() && close) {
						r.close();
					}
				}
			} else {
				log.error("No websocket handler found for URI " + r.getRequest().getRequestURI(), new IllegalStateException());
				if (!r.isCancelled() && close) {
					r.close();
				}
			}
		} catch (IOException e) {
			log.error("Failed to close websocket connection", e);
		}
		return new DummyWebSocketMessageListener();
	}

	@Override
	public void destroy() {
	}

	@Override
	public void onRequest(final AtmosphereResource resource) throws IOException {
		AtmosphereRequest request = resource.getRequest();
		String method = request.getMethod();
		boolean polling = Utils.pollableTransport(resource.transport());
		boolean webSocketMessage = Utils.webSocketMessage(resource);

		if (!webSocketMessage && !polling) {
			resource.addEventListener(new OnSuspend() {
				@Override
				public void onSuspend(AtmosphereResourceEvent event) {
					WebSocketAtmosphereHandler.this.onReady(event.getResource());
					resource.removeEventListener(this);
				}
			});

			resource.addEventListener(new OnResume() {
				@Override
				public void onResume(AtmosphereResourceEvent event) {
					resource.removeEventListener(this);
				}
			});

			resource.addEventListener(new OnClose() {
				@Override
				public void onClose(AtmosphereResourceEvent event) {
					WebSocketAtmosphereHandler.this.onDisconnect(event);
				}
			});
		}
		if (method.equalsIgnoreCase("post")) {
			Object body = null;
			body = readEntirely(resource);
			if (body != null && body instanceof String) {
				resource.getRequest().body((String) body);
			} else if (body != null) {
				resource.getRequest().body((byte[]) body);
			}
			onMessage(resource, body);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onStateChange(AtmosphereResourceEvent event) throws IOException {
		AtmosphereResource r = event.getResource();
		AtmosphereResponse response = r.getResponse();
		AtmosphereRequest request = r.getRequest();

		if (event.isCancelled() || event.isClosedByClient()) {
			onDisconnect(event);
		} else if (event.isResumedOnTimeout() || event.isResuming()) {
			onTimeout(event);
		} else if (r.isSuspended()) {
			Object message = event.getMessage();
			boolean writeAsBytes = IOUtils.isBodyBinary(request);
			if (message instanceof List) {
				Iterator<Object> i = ((List<Object>) message).iterator();
				try {
					Object s;
					while (i.hasNext()) {
						s = i.next();
						if (s instanceof String) {
							response.getOutputStream().write(((String) s).getBytes(response.getCharacterEncoding()));
						} else if (s instanceof byte[]) {
							response.getOutputStream().write((byte[]) s);
						} else {
							response.getOutputStream().write(s.toString().getBytes(response.getCharacterEncoding()));
						}
						i.remove();
					}
				} catch (IOException ex) {
					event.setMessage(new ArrayList<String>().addAll((List<String>) message));
					throw ex;
				}
				response.getOutputStream().flush();
			} else {
				response.getOutputStream().write(writeAsBytes ? (byte[]) message : message.toString().getBytes(response.getCharacterEncoding()));
				response.getOutputStream().flush();
			}

			switch (r.transport()) {
			case JSONP:
			case LONG_POLLING:
				r.resume();
				break;
			default:
			}
		}

	}

	public class DummyWebSocketMessageListener implements WebSocketMessageListener {

		@Override
		public void onReady(WebSocketConnection c) {
		}

		@Override
		public void onMessage(WebSocketConnection connection, Object message) {
		}

		@Override
		public void onDisconnect(WebSocketConnection connection) {
		}

		@Override
		public void onTimeout(WebSocketConnection connection) {
		}

		@Override
		public UrlHandler getOwner() {
			return null;
		}

		@Override
		public boolean isReady() {
			return false;
		}

	}
}

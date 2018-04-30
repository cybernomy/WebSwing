package org.webswing.toolkit;

import org.webswing.toolkit.api.WebswingMessagingApi;
import org.webswing.toolkit.api.messaging.WebswingMessage;
import org.webswing.toolkit.api.messaging.WebswingMessageListener;
import org.webswing.toolkit.api.messaging.WebswingTopic;
import org.webswing.toolkit.util.DeamonThreadFactory;
import org.webswing.toolkit.util.Logger;
import org.webswing.toolkit.util.Services;
import org.webswing.toolkit.util.Util;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebswingMessagingApiImpl implements WebswingMessagingApi {
	private ExecutorService messageProcessor = Executors.newSingleThreadExecutor(DeamonThreadFactory.getInstance("Webswing Messaging API processor"));
	private Map<WebswingMessageListener, Class> listeners = new ConcurrentHashMap<WebswingMessageListener, Class>();

	@Override
	public <T> WebswingTopic<T> getSharedTopic(final Class<T> messageType) {
		return new WebswingTopic<T>() {
			@Override
			public void publish(T message) throws IOException {
				if (message != null && message instanceof Serializable) {
					Services.getConnectionService().messageApiPublish((Serializable) message);
				} else {
					throw new IllegalArgumentException("Publishing failed. Message is not Serializable.");
				}
			}

			@Override
			public void subscribe(WebswingMessageListener<T> listener) {
				listeners.put(listener, messageType);
			}

			@Override
			public void unsubscribe(WebswingMessageListener<T> listener) {
				listeners.remove(listener);
			}
		};
	}

	public boolean hasListenerForClass(String msgtype) {
		ClassLoader cl =Util.getWebToolkit().getSwingClassLoader();
		Thread.currentThread().setContextClassLoader(cl);
		try {
			Class<?> msgclass = cl.loadClass(msgtype);
			for (Class<?> registeredclass : listeners.values()) {
				if (registeredclass.isAssignableFrom(msgclass)) {
					return true;
				}
			}
		} catch (ClassNotFoundException e) {
			Logger.warn("WebswingMessagingApiImpl: Failed to process Message. Message class " + msgtype + " not found.");
		}
		return false;
	}

	public void processMessage(final Serializable object) {
		for (WebswingMessageListener l : listeners.keySet()) {
			final WebswingMessageListener listener = l;
			Class regclass = listeners.get(l);
			if (regclass.isInstance(object)) {
				messageProcessor.submit(new Runnable() {
					@Override
					public void run() {
						listener.onMessage(new WebswingMessage() {
							@Override
							public Object getMessage() {
								return object;
							}
						});
					}
				});
			}
		}
	}
}

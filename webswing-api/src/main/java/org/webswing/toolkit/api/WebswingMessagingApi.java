package org.webswing.toolkit.api;

import org.webswing.toolkit.api.messaging.WebswingTopic;

/**
 * Webswing Messaging API used by Swing application for messaging across all swing sessions.
 */

public interface WebswingMessagingApi {

	/**
	 * Creates a reference to a generic message topic shared across all swing sessions.
	 * Reference will only consider messages of defined messageType.
	 * @param messageType  message type this reference is created for
	 * @param <T> message type parameter
	 * @return reference to shared message topic
	 */
	<T> WebswingTopic<T> getSharedTopic(Class<T> messageType);

}

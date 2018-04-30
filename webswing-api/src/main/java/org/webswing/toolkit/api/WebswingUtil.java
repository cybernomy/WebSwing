package org.webswing.toolkit.api;

import java.awt.Toolkit;

/**
 * Helper class to get API instance
 */
public class WebswingUtil {

	/**
	 * @return true if this instance is running inside Webswing app container
	 */
	public static boolean isWebswing() {
		Toolkit t = Toolkit.getDefaultToolkit();
		return t instanceof WebswingApiProvider;
	}

	/**
	 * Api Swing application can utilize to integrate with Webswing's lifecycle and security.
	 * @return Webswing API instance or null if not running in Webswing app container.
	 */
	public static WebswingApi getWebswingApi() {
		if (isWebswing()) {
			return ((WebswingApiProvider) Toolkit.getDefaultToolkit()).getApi();
		} else {
			return null;
		}
	}

	/**
	 * Inter-session messaging api following pub/sub pattern for easy communication of swing instances.
	 * @return Webswing Messaging API instance or null if not running in Webswing app container.
	 */
	public static WebswingMessagingApi getWebswingMessagingApi() {
		if (isWebswing()) {
			return ((WebswingApiProvider) Toolkit.getDefaultToolkit()).getMessagingApi();
		} else {
			return null;
		}
	}

}

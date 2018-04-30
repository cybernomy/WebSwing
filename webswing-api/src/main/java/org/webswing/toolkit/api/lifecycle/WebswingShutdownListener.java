package org.webswing.toolkit.api.lifecycle;

public interface WebswingShutdownListener {

	/**
	 * Invoked when Webswing requests swing application to exit. 
	 * This method should cause this process to exit. (not necessarily in the same thread) 
	 */
	void onShutdown();
}

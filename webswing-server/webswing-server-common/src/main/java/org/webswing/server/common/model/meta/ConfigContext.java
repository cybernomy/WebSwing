package org.webswing.server.common.model.meta;

import java.io.File;
import java.net.URL;

public interface ConfigContext {
	/**
	 * Search for resource using in application's home folder if path is relative
	 * and check for existence. 
	 * @param name name of resource.
	 * @return existing file or null
	 */
	File resolveFile(String name);

	/**
	 * Returns URL of static web resource if exists. 
	 * @param resource resource path relative to web context
	 * @return URL or null 
	 */
	URL getWebResource(String resource);

	/**
	 * Replace variables in in form <code>${variableName}</code> with their respective value
	 * from environment variables or system properties
	 * @param string string with variables
	 * @return  string with variables replaced
	 */
	String replaceVariables(String string);

	/**
	 * @return true if the Context is active (ie. SwingInstanceManager isEnabled)
	 */
	boolean isEnabled();
}

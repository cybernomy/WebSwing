package org.webswing.server.services.security.api;

import org.webswing.server.common.model.meta.ConfigContext;

/**
 * Context for {@link WebswingSecurityModule} stored in {@link WebswingSecurityModuleConfig} that provides access to resources and Webswing specific services.
 */
public interface SecurityContext extends ConfigContext{

	/**
	 * Retrieves object from session if exists.
	 * @param attributeName name of session attribute
	 * @return object if present in session
	 */
	Object getFromSecuritySession(String attributeName);

	/**
	 * Stores object in session.
	 * 
	 * @param attributeName name of attribute
	 * @param value object to store
	 */
	void setToSecuritySession(String attributeName, Object value);

	/**
	 * @return the root URK path where the {@link WebswingSecurityModule} related to this context 
	 * is used.  
	 */
	String getSecuredPath();
}

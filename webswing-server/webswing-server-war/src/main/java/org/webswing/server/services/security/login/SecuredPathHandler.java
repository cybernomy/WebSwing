package org.webswing.server.services.security.login;

import org.webswing.server.common.model.SecuredPathConfig;
import org.webswing.server.services.security.api.WebswingSecurityModule;
import org.webswing.server.services.security.modules.SecurityModuleWrapper;

public interface SecuredPathHandler {

	WebswingSecurityModule get();

	SecuredPathConfig getConfig();

	void initConfiguration();

	String getPathMapping();

	boolean isEnabled();
}

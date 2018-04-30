package org.webswing.server.services.security.modules;

import org.webswing.server.common.util.ConfigUtil;
import org.webswing.server.services.security.api.*;

import com.google.inject.Singleton;
import org.webswing.server.services.security.modules.noaccess.NoAccessSecurityModule;

import java.util.Map;

@Singleton
public class SecurityModuleServiceImpl implements SecurityModuleService {

	public SecurityModuleServiceImpl() {
	}

	public WebswingSecurityModule create(SecurityContext context, WebswingSecurityConfig config) {
		if (BuiltInModules.getSecurityModuleClassName(config.getModule()) != null) {
			return new SecurityModuleWrapper(context, config);
		} else {
			return null;
		}
	}

	public WebswingSecurityModule createNoAccess(String msgKey, SecurityContext context, WebswingSecurityConfig config) {
		Map<String, Object> map = config != null ? config.getConfig() : null;
		WebswingSecurityModuleConfig c = ConfigUtil.instantiateConfig(map, WebswingSecurityModuleConfig.class, context);
		return new NoAccessSecurityModule(msgKey, c);
	}

}

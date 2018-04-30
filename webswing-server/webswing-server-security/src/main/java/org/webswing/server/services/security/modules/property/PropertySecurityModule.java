package org.webswing.server.services.security.modules.property;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.io.ResourceUtils;
import org.apache.shiro.realm.text.PropertiesRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.api.WebswingAuthenticationException;
import org.webswing.server.services.security.modules.AbstractUserPasswordSecurityModule;
import org.webswing.server.services.security.modules.keycloak.KeycloakSecurityModule;

import java.io.File;

public class PropertySecurityModule extends AbstractUserPasswordSecurityModule<PropertySecurityModuleConfig> {
	private static final Logger log = LoggerFactory.getLogger(PropertySecurityModule.class);
	private PropertiesRealm realm;

	public PropertySecurityModule(PropertySecurityModuleConfig config) {
		super(config);
		realm = new PropertiesRealm();
		String fileName = getConfig().getFile();
		String fileNameFull = getConfig().getContext().replaceVariables(fileName);
		File file = getConfig().getContext().resolveFile(fileNameFull);
		if (file == null) {
			throw new RuntimeException("Unable to resolve user properties file: " + fileNameFull);
		}
		realm.setResourcePath(ResourceUtils.FILE_PREFIX + file.getPath());
		realm.onInit();
	}

	@Override
	public void destroy() {
		realm.destroy();
		super.destroy();
	}

	@Override
	public AbstractWebswingUser verifyUserPassword(String user, String password) throws WebswingAuthenticationException {
		AuthenticationInfo authtInfo;
		try {
			authtInfo = realm.getAuthenticationInfo(new UsernamePasswordToken(user, password));
		} catch (AuthenticationException e) {
			throw new WebswingAuthenticationException("Username or password is not valid!", WebswingAuthenticationException.INVALID_USER_OR_PASSWORD, e);
		}
		if (authtInfo == null) {
			throw new WebswingAuthenticationException("User not found!", WebswingAuthenticationException.INVALID_USER_OR_PASSWORD);
		}
		return new ShiroWebswingUser(realm, authtInfo);
	}

}

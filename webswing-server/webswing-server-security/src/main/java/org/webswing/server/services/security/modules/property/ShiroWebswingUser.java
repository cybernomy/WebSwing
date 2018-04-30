package org.webswing.server.services.security.modules.property;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.webswing.server.services.security.api.AbstractWebswingUser;

public class ShiroWebswingUser extends AbstractWebswingUser {

	private AuthorizingRealm authzrealm;
	private AuthenticationInfo authtInfo;

	public ShiroWebswingUser(AuthorizingRealm authzrealm, AuthenticationInfo authtInfo) {
		super();
		this.authzrealm = authzrealm;
		this.authtInfo = authtInfo;
	}

	@Override
	public String getUserId() {
		return (String) this.authtInfo.getPrincipals().getPrimaryPrincipal();
	}

	@Override
	public Map<String, Serializable> getUserAttributes() {
		return Collections.emptyMap();
	}

	@Override
	public boolean hasRole(String role) {
		return authzrealm.hasRole(this.authtInfo.getPrincipals(), role);
	}

}
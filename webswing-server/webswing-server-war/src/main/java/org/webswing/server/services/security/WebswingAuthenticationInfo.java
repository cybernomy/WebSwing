package org.webswing.server.services.security;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;

public class WebswingAuthenticationInfo implements AuthenticationInfo {
	private static final long serialVersionUID = -6244957474366054473L;

	private PrincipalCollection principalCollection;
	private Object credentials;

	public WebswingAuthenticationInfo(PrincipalCollection principalCollection, Object credentials) {
		this.principalCollection = principalCollection;
		this.credentials = credentials;

	}

	@Override
	public PrincipalCollection getPrincipals() {

		return principalCollection;
	}

	@Override
	public Object getCredentials() {
		return credentials;
	}

}

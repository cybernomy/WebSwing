package org.webswing.server.services.security;

import org.apache.shiro.authc.AuthenticationToken;
import org.webswing.server.services.security.api.AbstractWebswingUser;

public class LoginTokenAdapter implements AuthenticationToken {

	private static final long serialVersionUID = -6426250466722804937L;

	private final WebswingPrincipal user;
	private final Object credentials = new Object();

	public LoginTokenAdapter(String securityPath, AbstractWebswingUser user) {
		this.user = new WebswingPrincipal(securityPath, user);
	}

	@Override
	public Object getPrincipal() {
		return user;
	}

	@Override
	public Object getCredentials() {
		return credentials;
	}

}

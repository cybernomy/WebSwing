package org.webswing.server.services.security.extension.api;

import java.io.Serializable;
import java.util.Map;

import org.webswing.server.services.security.api.AbstractWebswingUser;

public class WebswingUserDecorator extends AbstractWebswingUser {

	private final AbstractWebswingUser user;

	public WebswingUserDecorator(AbstractWebswingUser user) {
		this.user = user;
	}

	@Override
	public String getUserId() {
		return user.getUserId();
	}

	@Override
	public Map<String, Serializable> getUserAttributes() {
		return user.getUserAttributes();
	}

	@Override
	public boolean hasRole(String role) {
		return user.hasRole(role);
	}

	@Override
	public boolean isPermitted(String permission) {
		return user.isPermitted(permission);
	}
	
}

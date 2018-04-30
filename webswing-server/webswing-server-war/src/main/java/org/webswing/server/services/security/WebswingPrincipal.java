package org.webswing.server.services.security;

import java.io.Serializable;
import java.util.Map;

import org.webswing.server.services.security.api.AbstractWebswingUser;

public class WebswingPrincipal extends WebswingPermission {
	private AbstractWebswingUser user;

	public WebswingPrincipal(String securityPath, AbstractWebswingUser user) {
		super(securityPath, null);
		this.user = user;
	}

	public String getUserId() {
		return user.getUserId();
	}

	public Map<String, Serializable> getUserAttributes() {
		return user.getUserAttributes();
	}

	public boolean isPermitted(String permission) {
		return user.isPermitted(permission);
	}

	public AbstractWebswingUser getUser() {
		return user;
	}

}

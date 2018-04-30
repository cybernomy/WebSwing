package org.webswing.server.services.security.extension.onetimeurl;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.webswing.server.services.security.api.AbstractWebswingUser;

public class OtpWebswingUser extends AbstractWebswingUser {

	private String userId;
	private Set<String> roles;
	private Set<String> permissions;
	private Map<String, Serializable> attributes;

	public OtpWebswingUser(String userId, Map<String, Serializable> attributes, Set<String> roles, Set<String> permissions) {
		this.userId = userId;
		this.attributes = attributes;
		this.roles = roles;
		this.permissions = permissions;
	}

	@Override
	public String getUserId() {
		return userId;
	}

	@Override
	public Map<String, Serializable> getUserAttributes() {
		return attributes;
	}

	@Override
	public boolean hasRole(String role) {
		return roles != null && roles.contains(role);
	}

	@Override
	public boolean isPermitted(String permission) {
		return super.isPermitted(permission) || (permissions != null && permissions.contains(permission));
	}
}

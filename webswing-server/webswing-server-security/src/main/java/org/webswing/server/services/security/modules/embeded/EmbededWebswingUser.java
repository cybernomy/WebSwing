package org.webswing.server.services.security.modules.embeded;

import org.webswing.server.services.security.api.AbstractWebswingUser;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EmbededWebswingUser extends AbstractWebswingUser {

	String user;
	String password;
	List<String> roles;

	public EmbededWebswingUser(String user, String password, List<String> roles) {
		super();
		this.user = user;
		this.password = password;
		this.roles = roles;
	}

	@Override
	public String getUserId() {
		return user;
	}

	@Override
	public Map<String, Serializable> getUserAttributes() {
		return Collections.emptyMap();
	}

	@Override
	public boolean hasRole(String role) {
		return roles.contains(role);
	}

	public String getPassword() {
		return password;
	}
}
package org.webswing.server.services.security.modules.anonym;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.webswing.server.services.security.api.AbstractWebswingUser;

public class AnonymWebswingUser extends AbstractWebswingUser {
	public static final String anonymUserName = "anonym";

	@Override
	public String getUserId() {
		return anonymUserName;
	}

	@Override
	public Map<String, Serializable> getUserAttributes() {
		return Collections.emptyMap();
	}

	@Override
	public boolean hasRole(String role) {
		return true;
	}

	public boolean isAuthenticated() {
		return true;
	}
};
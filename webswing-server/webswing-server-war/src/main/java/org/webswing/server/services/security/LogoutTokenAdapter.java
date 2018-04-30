package org.webswing.server.services.security;

import org.webswing.server.services.security.api.AbstractWebswingUser;

public class LogoutTokenAdapter extends LoginTokenAdapter {

	public LogoutTokenAdapter(String securityPath, AbstractWebswingUser user) {
		super(securityPath, user);
	}

	private static final long serialVersionUID = 8050122235271589836L;

}

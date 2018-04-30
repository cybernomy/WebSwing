package org.webswing.server.services.security.login;

import org.webswing.server.base.UrlHandler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
@Singleton
public class LoginHandlerServiceImpl implements LoginHandlerService {


	@Inject
	public LoginHandlerServiceImpl() {
	}

	public LoginHandler createLoginHandler(UrlHandler parent) {
		return new LoginHandlerImpl(parent);
	}
	
	public LogoutHandler createLogoutHandler(UrlHandler parent) {
		return new LogoutHandlerImpl(parent);
	}
}

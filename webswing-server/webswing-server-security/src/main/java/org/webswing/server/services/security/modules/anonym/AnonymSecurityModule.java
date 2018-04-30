package org.webswing.server.services.security.modules.anonym;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.api.WebswingAuthenticationException;
import org.webswing.server.services.security.api.WebswingSecurityModuleConfig;
import org.webswing.server.services.security.modules.AbstractSecurityModule;

public class AnonymSecurityModule extends AbstractSecurityModule<WebswingSecurityModuleConfig> {
	public AnonymSecurityModule(WebswingSecurityModuleConfig config) {
		super(config);
	}

	@Override
	protected AbstractWebswingUser authenticate(HttpServletRequest request) throws WebswingAuthenticationException {
		logSuccess(request, AnonymWebswingUser.anonymUserName);
		return new AnonymWebswingUser();
	}

	@Override
	protected void serveLoginPage(HttpServletRequest request, HttpServletResponse response, WebswingAuthenticationException exception) throws IOException {
	}

	@Override
	protected void serveLoginPartial(HttpServletRequest request, HttpServletResponse response, WebswingAuthenticationException exception) throws IOException {
	}

	@Override
	public void doLogout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		sendPartialHtml(request, response, "logoutPartial.html", null);
	}
}

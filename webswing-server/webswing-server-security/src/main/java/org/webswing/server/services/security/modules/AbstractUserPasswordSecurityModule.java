package org.webswing.server.services.security.modules;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.api.WebswingAuthenticationException;
import org.webswing.server.services.security.extension.api.WebswingExtendableSecurityModuleConfig;

/**
 * Helper Abstract Security Module that implements the most common authentication method using username and password. 
 * Login page (full and partial) templates are provided. 
 * @param <T>
 */
public abstract class AbstractUserPasswordSecurityModule<T extends WebswingExtendableSecurityModuleConfig> extends AbstractExtendableSecurityModule<T> {

	public AbstractUserPasswordSecurityModule(T config) {
		super(config);
	}

	@Override
	public void init() {
		super.init();
	}

	public String getPartialTemplateName() {
		return "loginPartial.html";
	}

	public String getUserName(HttpServletRequest request) {
		return request.getParameter("username");
	}

	public String getPassword(HttpServletRequest request) {
		return request.getParameter("password");
	}

	@Override
	protected AbstractWebswingUser authenticate(HttpServletRequest request) throws WebswingAuthenticationException {
		String username = getUserName(request);
		String password = getPassword(request);
		if (username != null || password != null) {
			try {
				AbstractWebswingUser user = verifyUserPassword(username, password);
				if (user != null) {
					logSuccess(request, user.getUserId());
				}
				return user;
			} catch (WebswingAuthenticationException e) {
				logFailure(request, username, e.getMessage());
				throw e;
			}
		} else {
			return null;
		}
	}

	protected void serveLoginPartial(HttpServletRequest request, HttpServletResponse response, WebswingAuthenticationException exception) throws IOException {
		Map<String, Object> variables = new HashMap<>();
		if (exception != null) {
			variables.put("errorMessage", exception.getLocalizedMessage());
		}
		sendPartialHtml(request, response, getPartialTemplateName(), variables);
	}

	/**
	 * Check if username and password is valid. If it is valid return an instance of {@link AbstractWebswingUser}
	 * otherwise throw {@link WebswingAuthenticationException}.
	 * 
	 * @throws WebswingAuthenticationException if authentication failed.
	 */
	public abstract AbstractWebswingUser verifyUserPassword(String user, String password) throws WebswingAuthenticationException;
}

package org.webswing.server.services.security;

import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.webswing.Constants;

public class WebswingWebSessionManager extends DefaultWebSessionManager {

	private static final String WEBSWING_SESSION_ID = "WebswingSessionId";

	public WebswingWebSessionManager() {
		String proxypath = System.getProperty(Constants.REVERSE_PROXY_CONTEXT_PATH, "").replaceAll("[^A-Za-z0-9]", "_");
		Cookie cookie = new SimpleCookie(WEBSWING_SESSION_ID + proxypath);
		cookie.setHttpOnly(true); //more secure, protects against XSS attacks
		cookie.setSecure(Boolean.getBoolean(Constants.HTTPS_ONLY));
		setSessionIdCookie(cookie);
	}
}

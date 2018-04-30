package org.webswing.server.util;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.webswing.server.base.UrlHandler;
import org.webswing.server.services.security.WebswingPrincipal;
import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.api.WebswingAction;
import org.webswing.server.services.websocket.WebSocketConnection;

public class SecurityUtil {

	public static AbstractWebswingUser getUser(UrlHandler urlHandler) {
		return getUser(urlHandler.getSecuredPath());
	}

	public static AbstractWebswingUser getUser(String path) {
		Subject subject = SecurityUtils.getSubject();
		return resolveUser(subject, path);
	}

	public static AbstractWebswingUser getUser(WebSocketConnection connection) {
		Subject subject = SecurityUtils.getSubject();
		UrlHandler urlHandler = connection.getHandler();
		return resolveUser(subject, urlHandler.getSecuredPath());
	}

	private static AbstractWebswingUser resolveUser(Subject subject, String securedPath) {
		if (subject != null && securedPath != null) {
			PrincipalCollection currentPrincipals = subject.getPrincipals();
			if (currentPrincipals != null && subject.isAuthenticated()) {
				AbstractWebswingUser masteradmin = null;
				for (WebswingPrincipal webswingPrincipal : currentPrincipals.byType(WebswingPrincipal.class)) {
					if ("".equals(webswingPrincipal.getSecuredPath()) && webswingPrincipal.isPermitted(WebswingAction.master_admin_access.name())) {
						masteradmin = webswingPrincipal.getUser();
					}
					if (securedPath.equals(webswingPrincipal.getSecuredPath())) {
						return webswingPrincipal.getUser();
					}
				}
				return masteradmin;
			}
		}
		return null;
	}

	public static Object getFromSecuritySession(String attributeName) {
		Subject subject = SecurityUtils.getSubject();
		return subject.getSession().getAttribute(attributeName);
	}

	public static void setToSecuritySession(String attributeName, Object value) {
		Subject subject = SecurityUtils.getSubject();
		subject.getSession().setAttribute(attributeName, value);
	}

}

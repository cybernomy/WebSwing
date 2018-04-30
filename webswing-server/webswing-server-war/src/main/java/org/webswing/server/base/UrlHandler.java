package org.webswing.server.base;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webswing.server.model.exception.WsException;
import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.api.WebswingAction;
import org.webswing.server.services.security.login.SecuredPathHandler;

public interface UrlHandler {

	void init();

	void destroy();

	boolean serve(HttpServletRequest req, HttpServletResponse res) throws WsException;

	void registerFirstChildUrlHandler(UrlHandler handler);

	void registerChildUrlHandler(UrlHandler handler);

	void removeChildUrlHandler(UrlHandler Handler);

	ServletContext getServletContext();

	long getLastModified(HttpServletRequest req);

	String getPathMapping();

	String getFullPathMapping();

	String getSecuredPath();

	UrlHandler getRootHandler();

	AbstractWebswingUser getUser();

	void checkPermission(WebswingAction action) throws WsException;

	void checkMasterPermission(WebswingAction action) throws WsException;

	SecuredPathHandler getSecurityProvider();

}

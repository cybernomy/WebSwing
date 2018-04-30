package org.webswing.server.services.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.webswing.server.base.WebswingService;

public interface SecurityManagerService extends WebswingService {
	public static final String SECURITY_SUBJECT = "webswingSecuritySubject";

	Object secure(SecurableService handler, HttpServletRequest req, HttpServletResponse res);

}

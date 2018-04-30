package org.webswing.server.services.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface SecurableService {
	Object secureServe(HttpServletRequest req, HttpServletResponse res) throws Exception;
}

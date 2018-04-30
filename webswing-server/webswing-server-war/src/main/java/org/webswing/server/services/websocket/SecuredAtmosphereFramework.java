package org.webswing.server.services.websocket;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResponse;
import org.webswing.server.services.security.SecurableService;
import org.webswing.server.services.security.SecurityManagerService;

class SecuredAtmosphereFramework extends AtmosphereFramework implements SecurableService {
	private SecurityManagerService securityManager;

	public SecuredAtmosphereFramework(SecurityManagerService securityManager) {
		this.securityManager = securityManager;
	}

	public org.atmosphere.cpr.Action doCometSupport(AtmosphereRequest req, AtmosphereResponse res) throws IOException, ServletException {
		return (Action) securityManager.secure(this, new AtmosphereRequestWrapper(req), res);
	}

	@Override
	public Object secureServe(HttpServletRequest req, HttpServletResponse res) throws Exception {
		return super.doCometSupport(((AtmosphereRequestWrapper) req).getOriginal(), (AtmosphereResponse) res);
	}

	static class AtmosphereRequestWrapper extends HttpServletRequestWrapper {

		private AtmosphereRequest original;

		public AtmosphereRequestWrapper(AtmosphereRequest original) {
			super(original);
			this.original = original;
		}

		@Override
		public Cookie[] getCookies() {
			Cookie[] cookies = super.getCookies();
			if (cookies == null || cookies.length == 0) {
				String cookieHeader = getHeader("Cookie");
				cookies = parseCookieHeader(cookieHeader);
			}
			return cookies;
		}

		public AtmosphereRequest getOriginal() {
			return original;
		}

		/**
		 * Parse a cookie header into an array of cookies according to RFC 2109.
		 * 
		 * @param header
		 *      Value of an HTTP "Cookie" header
		 */
		public static Cookie[] parseCookieHeader(String header) {

			if ((header == null) || (header.length() < 1))
				return (new Cookie[0]);

			ArrayList<Cookie> cookies = new ArrayList<Cookie>();
			while (header.length() > 0) {
				int semicolon = header.indexOf(';');
				if (semicolon < 0)
					semicolon = header.length();
				if (semicolon == 0)
					break;
				String token = header.substring(0, semicolon);
				if (semicolon < header.length())
					header = header.substring(semicolon + 1);
				else
					header = "";
				try {
					int equals = token.indexOf('=');
					if (equals > 0) {
						String name = token.substring(0, equals).trim();
						String value = token.substring(equals + 1).trim();
						cookies.add(new Cookie(name, value));
					}
				} catch (Throwable e) {
					;
				}
			}

			return ((Cookie[]) cookies.toArray(new Cookie[cookies.size()]));

		}

	}

};
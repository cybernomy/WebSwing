package org.webswing.server.services.security.modules;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheResolver;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.common.util.CommonUtil;
import org.webswing.server.common.util.WebswingObjectMapper;
import org.webswing.server.services.security.api.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Abstract implementation of {@link WebswingSecurityModule} that offers convenience methods for
 * proper handling of Ajax and non-Ajax requests and implementing the default login flow for {@link WebswingSecurityModule#doLogin(HttpServletRequest, HttpServletResponse) doLogin}.
 * </p>
 * <p>
 * By default, Webswing's javascript client use Ajax call for authentication. This implicates that the login page
 * served by the security module should be a partial HTML containing only the login form. On the other hand if user is redirected
 * to the /login url, module should respond with full HTML login page. Two abstract methods: {@link #serveLoginPartial(HttpServletRequest, HttpServletResponse, WebswingAuthenticationException) serveLoginPartial}
 * and {@link #serveLoginPage(HttpServletRequest, HttpServletResponse, WebswingAuthenticationException) serveLoginPage} are called as appropriate for this purpose.
 * </p>
 * <p>
 * For implementing mentioned abstract methods use the {@link AbstractSecurityModule#sendHtml(HttpServletRequest, HttpServletResponse, String, Object) sendHtml} method, which
 * uses Mustache templating syntax to help you respond with dynamic pages.
 * </p>
 * <p>
 * In case the login workflow needs to perform a full redirect (see SAML2 implementation), it has to send a response recognized by the ajax caller.
 * For that purpose use {@link AbstractSecurityModule#sendRedirect(HttpServletRequest, HttpServletResponse, String)} method.
 * </p>
 *
 * @param <T> Interface for reading security module's JSON configuration.
 */

/**
 * @author vikto
 *
 * @param <T>
 */
public abstract class AbstractSecurityModule<T extends WebswingSecurityModuleConfig> implements WebswingSecurityModule {
	private static final Logger auditLog = LoggerFactory.getLogger(WebswingSecurityModule.class);

	/**
	 * Login response message parameter to indicate url redirect.(for Ajax calls)
	 */
	public static final String REDIRECT_URL = "redirectUrl";
	/**
	 * Login request message parameter to indicate redirection upon successful login.
	 */
	public static final String SUCCESS_URL = "successUrl";

	private static final Logger log = LoggerFactory.getLogger(AbstractSecurityModule.class);
	private static final String LOGIN_REQUEST_MSG = "LoginRequestMsg";

	private final T config;
	private final DefaultMustacheFactory mf;
	private final Map<String, Mustache> compiledTemplates = new HashMap<>();

	public AbstractSecurityModule(T config) {
		this.config = config;
		mf = new DefaultMustacheFactory(new MustacheResolver() {

			@Override
			public Reader getReader(String resourceName) {
				URL url = findTemplate(resourceName);
				if (url != null) {
					try {
						InputStream is = url.openStream();
						return new InputStreamReader(is);
					} catch (IOException e) {
						log.error("Failed to open Template from url:" + url);
					}
				}
				return null;
			}
		});
	}

	public T getConfig() {
		return config;
	}

	@Override
	public void init() {
	}

	@Override
	public void destroy() {
		compiledTemplates.clear();
	}

	@Override
	public AbstractWebswingUser doLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			Map<String, Object> msg = getLoginRequest(request);
			if (msg != null) {
				config.getContext().setToSecuritySession(LOGIN_REQUEST_MSG, msg);
			}
			preVerify(request, response);
			AbstractWebswingUser user = authenticate(request);
			if (user != null) {
				postVerify(user, request, response);
				onAuthenticationSuccess(user, request, response);
				return decorateUser(user, request, response);
			}
			onAuthenticationFailed(request, response, null);
		} catch (WebswingAuthenticationException e) {
			onAuthenticationFailed(request, response, e);
		} catch (LoginResponseClosedException e) {
			return null;
		}
		return null;
	}

	@Override
	public void doLogout(HttpServletRequest req, HttpServletResponse res, AbstractWebswingUser user) throws ServletException, IOException {
		doLogout(req, res);
	}

	public void doLogout(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String fullPath = getConfig().getContext().getSecuredPath();
		sendRedirect(req, res, fullPath);
	}

	public void logoutRedirect(HttpServletRequest request, HttpServletResponse response, String logoutUrl) throws IOException {
		if (logoutUrl != null) {
			sendRedirect(request, response, logoutUrl);
		} else {
			sendPartialHtml(request, response, "logoutPartial.html", null);
		}
	}

	@Override
	public void doServeAuthenticated(AbstractWebswingUser user, String path, HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.setStatus(HttpServletResponse.SC_OK);
		res.setHeader("webswingUsername", user.getUserId());
		serveAuthenticated(user, path, req, res);
	}

	protected void serveAuthenticated(AbstractWebswingUser user, String path, HttpServletRequest req, HttpServletResponse res) {
	}

	/**
	 * See {@link AbstractExtendableSecurityModule}
	 */
	protected AbstractWebswingUser decorateUser(AbstractWebswingUser user, HttpServletRequest request, HttpServletResponse response) {
		return user;
	}

	/**
	 * See {@link AbstractExtendableSecurityModule}
	 */
	protected void postVerify(AbstractWebswingUser user, HttpServletRequest request, HttpServletResponse response) throws LoginResponseClosedException, WebswingAuthenticationException {
	}

	/**
	 * See {@link AbstractExtendableSecurityModule}
	 */
	protected void preVerify(HttpServletRequest request, HttpServletResponse response) throws LoginResponseClosedException, WebswingAuthenticationException {
	}

	/**
	 * Check if request has any login credentials. If it does and they are valid return an instance of {@link AbstractWebswingUser}
	 * otherwise throw {@link WebswingAuthenticationException}.
	 * If no credentials are present return null.
	 *
	 * @param request Login request
	 * @return authenticated user or null
	 * @throws WebswingAuthenticationException if authentication failed.
	 */
	protected abstract AbstractWebswingUser authenticate(HttpServletRequest request) throws WebswingAuthenticationException;

	/**
	 * If the login request is not Ajax call and a {@link #SUCCESS_URL} was sent with first request,
	 * send redirect to this url.
	 * @param user authenticated user
	 * @param request login request
	 * @param response login response
	 * @throws IOException if fails to respond
	 */
	protected void onAuthenticationSuccess(AbstractWebswingUser user, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setHeader("webswingUsername", user.getUserId());
		if (!isAjax(request)) {
			Map<String, Object> msg = getLoginRequest(request);
			if (msg != null && msg.containsKey(SUCCESS_URL)) {
				sendRedirect(request, response, (String) msg.get(SUCCESS_URL));
			} else {
				String defaultPath = config.getContext().getSecuredPath();
				sendRedirect(request, response, defaultPath);
			}
		}
	}

	/**
	 * Send {@link HttpServletResponse#SC_UNAUTHORIZED} response status and serve full login page or partial login page if
	 * request was initiated by Ajax call.
	 * @param request
	 * @param response
	 * @param exception
	 * @throws IOException
	 */
	protected void onAuthenticationFailed(HttpServletRequest request, HttpServletResponse response, WebswingAuthenticationException exception) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		if (isAjax(request)) {
			serveLoginPartial(request, response, exception);
		} else {
			serveLoginPage(request, response, exception);
		}
	}

	/**
	 * Respond with full login HTML page. If <code>exception</code> is not null, page should indicate the error message.
	 *  Use {@link #sendHtml(HttpServletRequest, HttpServletResponse, String, Object) sendHtml} helper method.
	 * @param request login request
	 * @param response login response
	 * @param exception null or exception thrown by previous login attempt.
	 * @throws IOException if fails to send response
	 */
	protected void serveLoginPage(HttpServletRequest request, HttpServletResponse response, WebswingAuthenticationException exception) throws IOException {
		serveLoginPartial(request, response, exception);
	}

	/**
	 * Respond with partial login HTML page. If <code>exception</code> is not null, page should indicate the error message.
	 * Use {@link #sendPartialHtml(HttpServletRequest, HttpServletResponse, String, Object) sendHtml} helper method.
	 * @param request login request
	 * @param response login response
	 * @param exception null or exception thrown by previous login attempt.
	 * @throws IOException if fails to send response
	 */
	protected abstract void serveLoginPartial(HttpServletRequest request, HttpServletResponse response, WebswingAuthenticationException exception) throws IOException;

	/**
	 * Detect if request have been sent using Ajax call or regular browser request
	 * @param request login request
	 * @return true if ajax header is found
	 */
	protected boolean isAjax(HttpServletRequest request) {
		String requestedWithHeader = request.getHeader("X-Requested-With");
		if (requestedWithHeader != null && requestedWithHeader.equals("XMLHttpRequest")) {
			return true;
		}
		return false;
	}

	/**
	 * Sends 302 redirect response or redirect JSON message if request is Ajax call.
	 * @param request login request
	 * @param response login response
	 * @param url Redirect URL
	 * @throws IOException if fails to send response
	 */
	protected void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
		if (isAjax(request)) {
			Map<String, Object> message = new HashMap<>();
			message.put(REDIRECT_URL, url);
			try {
				WebswingObjectMapper.get().writeValue(response.getOutputStream(), message);
			} catch (Exception e) {
				throw new IOException("Failed to send login redirect message", e);
			}
		} else {
			sendHttpRedirect(request, response, url);
		}
	}

	/**
	 * Processes the template ad sends the result HTML or if request is Ajax call, sends the processed
	 * template HTML as JSON response.
	 * @param request login request
	 * @param response login response
	 * @param template path to template file.(See {@link #findTemplate(String)})
	 * @param variables java POJO object or instance of Map, that will be used to replace variables in template files.
	 * @throws IOException if fails to send response
	 */
	protected void sendHtml(HttpServletRequest request, HttpServletResponse response, String template, Object variables) throws IOException {
		Object[] extendedVars = new Object[] { variables, getDefaultVariables(request) };
		if (isAjax(request)) {
			Map<String, Object> message = new HashMap<>();
			try {
				Writer w = new StringWriter();
				processTemplate(w, template, extendedVars);
				message.put("partialHtml", w.toString());
				WebswingObjectMapper.get().writeValue(response.getOutputStream(), message);
			} catch (Exception e) {
				throw new IOException("Failed to send login template message", e);
			}
		} else {
			Writer w = new OutputStreamWriter(response.getOutputStream());
			processTemplate(w, template, extendedVars);
		}
	}

	/**
	 * If request is not Ajax call, template is wrapped into default HTML page.
	 * If request is Ajax call, sends the template in JSON response.
	 * @param request login request
	 * @param response login response
	 * @param template path to partial template file.(See {@link #findTemplate(String)})
	 * @param variables java POJO object or instance of Map, that will be used to replace variables in template files.
	 * @throws IOException if fails to send response
	 */
	protected void sendPartialHtml(HttpServletRequest request, HttpServletResponse response, String template, Object variables) throws IOException {
		Map<String, String> defaultVars = getDefaultVariables(request);
		Object[] extendedVars = new Object[] { variables, defaultVars };
		if (isAjax(request)) {
			sendHtml(request, response, template, variables);
		} else {
			Writer w = new OutputStreamWriter(response.getOutputStream());
			Writer tempw = new StringWriter();
			processTemplate(tempw, template, extendedVars);
			defaultVars.put("partialHtml", Base64.getEncoder().encodeToString(tempw.toString().getBytes()));
			processTemplate(w, "default.html", extendedVars);
		}
	}

	private Map<String, String> getDefaultVariables(HttpServletRequest request) {
		Map<String, String> result = new HashMap<>();
		result.put("requestBaseUrl", getBaseUrl(request));
		return result;
	}

	private String getBaseUrl(HttpServletRequest req) {
		String proto = req.getHeader("X-Forwarded-Proto");
		if (proto == null) {
			proto = req.getRequestURL().toString().startsWith("https") ? "https" : "http";
		}
		String host = req.getHeader("X-Forwarded-Host");
		if (host == null) {
			host = req.getServerName();
			int port = req.getServerPort();
			if (port != 80 && port != 443) {
				host += ":" + port;
			}
		}
		String result = proto + "://" + host + config.getContext().getSecuredPath();
		return result;
	}

	/**
	 * Default implementation uses Mustache style templates. Override this method if other template
	 * framework is needed.
	 * @param w processed output will be written here
	 * @param template path to template file.(See {@link #findTemplate(String)})
	 * @param variables java POJO object or instance of Map, that will be used to replace variables in template files.
	 * @throws IOException
	 */
	protected void processTemplate(Writer w, String template, Object[] variables) throws IOException {
		try {
			if (template != null && w != null) {
				Mustache mustache = compileTemplate(template);
				mustache.execute(w, variables);
				w.flush();
			}
		} finally {
			if (w != null) {
				w.close();
			}
		}
	}

	private Mustache compileTemplate(String name) throws IOException {
		Mustache mustache = compiledTemplates.get(name);
		if (mustache == null) {
			mustache = mf.compile(name);
			compiledTemplates.put(name, mustache);
		}
		return mustache;
	}

	/**
	 * Defines where to look for template files.
	 * Default implementation looks for template in web resource first, and if not found
	 * it tries to load it from classpath of Security Module.
	 * @param name path to template
	 * @return template URL
	 */
	public URL findTemplate(String name) {
		URL url = getConfig().getContext().getWebResource(name);
		if (url == null) {
			url = getClass().getClassLoader().getResource(name);
		}
		return url;
	}

	/**
	 * Reads the initial Ajax login request message from request or from session.
	 * @param request login request
	 * @return login request parameter map
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getLoginRequest(HttpServletRequest request) {
		if (isAjax(request) && "application/json".equals(request.getContentType())) {
			try {
				if (request.getAttribute(LOGIN_REQUEST_MSG) == null) {
					Map<String, Object> loginRequest = WebswingObjectMapper.get().readValue(request.getReader(), Map.class);
					request.setAttribute(LOGIN_REQUEST_MSG, loginRequest);
				}
				return (Map<String, Object>) request.getAttribute(LOGIN_REQUEST_MSG);
			} catch (Exception e) {
				log.debug("Failed to read login request data.", e);
			}
		}
		return (Map<String, Object>) getConfig().getContext().getFromSecuritySession(LOGIN_REQUEST_MSG);
	}

	/**
	 * @return JSON serializer.
	 */
	public static WebswingObjectMapper getMapper() {
		return WebswingObjectMapper.get();
	}

	public void logSuccess(HttpServletRequest r, String user) {
		String path = getConfig().getContext().getSecuredPath();
		path = StringUtils.isEmpty(path) ? "/" : path;
		String module = this.getClass().getName();
		auditLog("SUCCESS", r, path, module, user, "");
	}

	public void logFailure(HttpServletRequest r, String user, String reason) {
		String path = getConfig().getContext().getSecuredPath();
		path = StringUtils.isEmpty(path) ? "/" : path;
		String module = this.getClass().getName();
		auditLog("FAILED", r, path, module, user, reason);
	}

	public String replaceVar(String s) {
		return getConfig().getContext().replaceVariables(s);
	}

	public static void auditLog(String status, HttpServletRequest r, String path, String module, String username, String reason) {

		String protocol = r.getScheme();
		String ipAddress = r.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = r.getRemoteAddr();
		}
		auditLog.info("{} | {} | {} | {} | {} | {} | {}", new Object[] { status, username, reason, path, protocol, ipAddress, module });
	}

	public static void sendHttpRedirect(HttpServletRequest req, HttpServletResponse resp, String relativeUrl) throws IOException {
		String proto = req.getHeader("X-Forwarded-Proto");
		String host = req.getHeader("X-Forwarded-Host");
		if (StringUtils.startsWithIgnoreCase(relativeUrl, "http://") || StringUtils.startsWithIgnoreCase(relativeUrl, "https://")) {
			resp.sendRedirect(relativeUrl);
		} else if (StringUtils.isNotEmpty(proto) && StringUtils.isNotEmpty(host)) {
			if (!StringUtils.startsWith(relativeUrl, "/")) {
				String requestPath = getContextPath(req.getServletContext()) + CommonUtil.toPath(req.getPathInfo());
				String requestPathBase = requestPath.startsWith("/") ? requestPath : "/" + requestPath;
				requestPathBase = requestPath.substring(0, requestPath.lastIndexOf("/") + 1);
				relativeUrl = requestPathBase + relativeUrl;
			}
			resp.sendRedirect(proto + "://" + host + relativeUrl);
		} else {
			resp.sendRedirect(relativeUrl);
		}
	}

	public static String getContextPath(ServletContext ctx) {
		String contextPath = ctx.getContextPath();
		String contextPathExplicit = System.getProperty(Constants.REVERSE_PROXY_CONTEXT_PATH);
		if (contextPathExplicit != null) {
			return CommonUtil.toPath(contextPathExplicit);
		} else if (contextPath != null && !contextPath.equals("/") && !contextPath.equals("")) {
			return CommonUtil.toPath(contextPath);
		} else {
			return "";
		}
	}

}

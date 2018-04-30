package org.webswing.server.base;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.server.common.util.CommonUtil;
import org.webswing.server.common.util.WebswingObjectMapper;
import org.webswing.server.model.exception.WsException;
import org.webswing.server.services.security.SecurableService;
import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.api.WebswingAction;
import org.webswing.server.services.security.login.SecuredPathHandler;
import org.webswing.server.util.SecurityUtil;
import org.webswing.server.util.ServerUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractUrlHandler implements UrlHandler, SecurableService {
	private static final Logger log = LoggerFactory.getLogger(AbstractUrlHandler.class);

	private static final String GET = "GET";
	private static final String PUT = "PUT";
	private static final String POST = "POST";
	private static final String DELETE = "DELETE";

	private final UrlHandler parent;
	private final LinkedList<UrlHandler> childHandlers = new LinkedList<UrlHandler>();

	public AbstractUrlHandler(UrlHandler parent) {
		this.parent = parent;

	}

	@Override
	public void init() {
		synchronized (childHandlers) {
			for (UrlHandler handler : childHandlers) {
				try {
					handler.init();
				} catch (Exception e) {
					log.error("Failed to initialize child handler: " + handler.getClass().getName(), e);
				}
			}
		}
	}

	@Override
	public void destroy() {
		synchronized (childHandlers) {
			for (UrlHandler handler : childHandlers) {
				try {
					handler.destroy();
				} catch (Exception e) {
					log.error("Failed to destroy child handler: " + handler.getClass().getName(), e);
				}
			}
			childHandlers.clear();
		}
	}

	@Override
	public boolean serve(HttpServletRequest req, HttpServletResponse res) throws WsException {
		String pathinfo = getPathInfo(req);
		List<UrlHandler> localHandlerList = new LinkedList<UrlHandler>(childHandlers);
		for (UrlHandler child : localHandlerList) {
			if (isSubPath(toPath(child.getPathMapping()), pathinfo)) {
				boolean served = child.serve(req, res);
				if (served) {
					return true;
				}
			}
		}
		//Rest processor
		return serveRestMethod(req, res);
	}

	protected boolean serveRestMethod(HttpServletRequest req, HttpServletResponse res) throws WsException {
		String httpMethod = req.getMethod();
		String path = getPathInfo(req);
		Method method = findRestHandlerMethod(httpMethod, path);
		if (method != null) {
			method.setAccessible(true);
			Object[] args = resolveRestParameters(method, req);
			Object result;
			try {
				result = method.invoke(this, args);
			} catch (InvocationTargetException e1) {
				result = e1.getTargetException();
			} catch (Exception e) {
				result = e;
			}
			try {
				writeRestResponse(method, req, res, result);
				return true;
			} catch (WsException e) {
				throw e;
			}
		}
		return false;
	}

	@Override
	public Object secureServe(HttpServletRequest req, HttpServletResponse res) throws WsException {
		return serve(req, res);
	}

	public String getFullPathMapping() {
		String handlerPath = toPath(getPathMapping());
		if (this.parent != null) {
			String parentMapping = parent.getFullPathMapping();
			handlerPath = parentMapping + handlerPath;
		} else {
			handlerPath = ServerUtil.getContextPath(getServletContext()) + handlerPath;
		}
		return handlerPath;
	}

	public String getPathInfo(HttpServletRequest req) {
		String fullHandlerPath = getFullPathMapping();
		String requestPath = toPath(ServerUtil.getContextPath(getServletContext()) + req.getPathInfo());
		if (isSubPath(fullHandlerPath, requestPath)) {
			return toPath(requestPath.substring(fullHandlerPath.length()));
		} else {
			return "/";
		}
	}

	protected abstract String getPath();

	public String getPathMapping() {
		String path = toPath(getPath());
		return path;
	}

	public boolean isSubPath(String subpath, String path) {
		return CommonUtil.isSubPath(subpath, path);
	}

	public static String toPath(String path) {
		return CommonUtil.toPath(path);
	}

	public void registerFirstChildUrlHandler(UrlHandler handler) {
		childHandlers.addFirst(handler);
	}

	@Override
	public void registerChildUrlHandler(UrlHandler handler) {
		childHandlers.add(handler);
	}

	@Override
	public void removeChildUrlHandler(UrlHandler handler) {
		if (childHandlers.contains(handler)) {
			childHandlers.remove(handler);
			handler.destroy();
		}
	}

	public ServletContext getServletContext() {
		return parent.getServletContext();
	}

	public String getSecuredPath() {
		if (SecuredPathHandler.class.isAssignableFrom(this.getClass())) {
			SecuredPathHandler provider = (SecuredPathHandler) this;
			if (provider.get() != null) {
				return getFullPathMapping();
			}
		}
		if (parent == null) {
			return getFullPathMapping();
		} else {
			return parent.getSecuredPath();
		}
	}

	public SecuredPathHandler getSecurityProvider() {
		if (SecuredPathHandler.class.isAssignableFrom(this.getClass())) {
			SecuredPathHandler provider = (SecuredPathHandler) this;
			if (provider.get() != null) {
				return provider;
			}
		}
		if (parent == null) {
			return (SecuredPathHandler) this;
		} else {
			return parent.getSecurityProvider();
		}
	}

	public long getLastModified(HttpServletRequest req) {
		return -1;
	}

	public AbstractWebswingUser getUser() {
		return SecurityUtil.getUser(this);
	}

	public AbstractWebswingUser getMasterUser() {
		return SecurityUtil.getUser(getRootHandler().getSecuredPath());
	}

	@Override
	public UrlHandler getRootHandler() {
		if (parent != null) {
			return parent.getRootHandler();
		} else {
			return this;
		}
	}

	@Override
	public void checkPermission(WebswingAction action) throws WsException {
		AbstractWebswingUser user = getUser();
		checkPermission(user, action);
	}

	@Override
	public void checkMasterPermission(WebswingAction action) throws WsException {
		AbstractWebswingUser user = getMasterUser();
		checkPermission(user, action);
	}

	public void checkPermissionLocalOrMaster(WebswingAction a) throws WsException {
		try {
			checkPermission(a);
		} catch (WsException e) {
			checkMasterPermission(a);
		}
	}

	private void checkPermission(AbstractWebswingUser user, WebswingAction action) throws WsException {
		if (user != null) {
			if (user.isPermitted(action.name())) {
				return;
			}
		}
		throw new WsException("User '" + user + "' is not allowed to execute action '" + action + "'", HttpServletResponse.SC_UNAUTHORIZED);
	}

	private Method findRestHandlerMethod(String httpMethod, String path2) {
		Class<? extends Annotation> methodAnnotation = httpMethod2RestAnnotation(httpMethod);
		Method match = null;
		if (methodAnnotation != null) {
			int extent = path2.length();
			for (Method method : this.getClass().getDeclaredMethods()) {
				if (method.getAnnotation(methodAnnotation) != null) {
					Path pathAnnotation;
					if ((pathAnnotation = method.getAnnotation(Path.class)) != null) {
						if (isSubPath(pathAnnotation.value(), path2)) {
							if (path2.length() - pathAnnotation.value().length() < extent) {
								match = method;
								extent = path2.length() - pathAnnotation.value().length();
							}
						}
					}
				}
			}
		}
		return match;
	}

	private static Class<? extends Annotation> httpMethod2RestAnnotation(String httpMethod) {
		if (httpMethod.equals(GET)) {
			return javax.ws.rs.GET.class;
		} else if (httpMethod.equals(PUT)) {
			return javax.ws.rs.PUT.class;
		} else if (httpMethod.equals(POST)) {
			return javax.ws.rs.POST.class;
		} else if (httpMethod.equals(DELETE)) {
			return javax.ws.rs.DELETE.class;
		} else {
			return null;
		}
	}

	private Object[] resolveRestParameters(Method method, HttpServletRequest req) {
		Object[] result = new Object[method.getParameterCount()];
		Parameter[] params = method.getParameters();
		for (int i = 0; i < params.length; i++) {
			QueryParam paramAnno;
			if (params[i].getType().isAssignableFrom(HttpServletRequest.class)) {
				result[i] = req;
			} else if ((paramAnno = params[i].getAnnotation(javax.ws.rs.QueryParam.class)) != null) {
				result[i] = req.getParameter(paramAnno.value());
			} else if (params[i].getAnnotation(javax.ws.rs.PathParam.class) != null && String.class.isAssignableFrom(params[i].getType())) {
				String path2 = method.getAnnotation(Path.class).value();
				result[i] = getPathInfo(req).substring(path2.length());
			} else if (String.class.isAssignableFrom(params[i].getType())) {
				try {
					result[i] = IOUtils.toString(req.getReader());
				} catch (IOException e) {
					result[i] = null;
				}
			} else {
				try {
					String content = IOUtils.toString(req.getReader());
					result[i] = WebswingObjectMapper.get().readValue(content, params[i].getType());
				} catch (IOException e) {
					result[i] = null;
				}
			}
		}
		return result;
	}

	private void writeRestResponse(Method method, HttpServletRequest req, HttpServletResponse res, Object result) throws WsException {
		if (result != null && result instanceof Throwable) {
			if (result instanceof WsException) {
				throw (WsException) result;
			} else {
				log.error("Invocation of REST method failed.", (Throwable) result);
				throw new WsException("Invocation of REST method failed.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else {
			res.setStatus(HttpServletResponse.SC_OK);
			res.setHeader("Cache-Control", "no-store, must-revalidate");
			res.setHeader("Expires", "0");
			if (!method.getReturnType().equals(Void.TYPE) && result != null) {
				try {
					if (method.getReturnType().equals(String.class)) {
						IOUtils.write(result.toString(), res.getOutputStream());
					} else if (method.getReturnType().equals(InputStream.class)) {
						IOUtils.copy((InputStream) result, res.getOutputStream());
					} else {
						WebswingObjectMapper.get().writerWithDefaultPrettyPrinter().writeValue(res.getOutputStream(), result);
					}
				} catch (Exception e) {
					log.error("Failed to serialize REST execution result." + req.getRequestURI(), e);
					throw new WsException("Serializing of REST result failed.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			}
		}
	}
}

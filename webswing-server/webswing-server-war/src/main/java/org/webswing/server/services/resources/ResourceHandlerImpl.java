package org.webswing.server.services.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.server.base.AbstractUrlHandler;
import org.webswing.server.base.UrlHandler;
import org.webswing.server.common.util.CommonUtil;
import org.webswing.server.model.exception.WsException;
import org.webswing.server.services.security.api.SecurityContext;
import org.webswing.server.util.ServerUtil;

public class ResourceHandlerImpl extends AbstractUrlHandler implements ResourceHandler {
	private static final Logger log = LoggerFactory.getLogger(ResourceHandlerImpl.class);

	private SecurityContext context;

	public ResourceHandlerImpl(UrlHandler parent, SecurityContext context) {
		super(parent);
		this.context = context;
	}

	@Override
	protected String getPath() {
		return "";
	}

	@Override
	public boolean serve(HttpServletRequest req, HttpServletResponse res) throws WsException {
		try {
			if (req.getMethod().equals("GET") || req.getMethod().equals("PUT")) {
				return lookup(req).respondGet(req, res);
			} else if (req.getMethod().equals("HEAD")) {
				return lookup(req).respondHead(req, res);
			}
			return false;
		} catch (IOException e) {
			throw new WsException("Failed to process resource.", e);
		}
	}

	public static interface LookupResult {
		public boolean respondGet(HttpServletRequest req, HttpServletResponse resp) throws IOException;

		public boolean respondHead(HttpServletRequest req, HttpServletResponse resp) throws IOException;

		public long getLastModified();
	}

	public static class ErrorResult implements LookupResult {
		protected final int statusCode;
		protected final String message;

		public ErrorResult(int statusCode, String message) {
			this.statusCode = statusCode;
			this.message = message;
		}

		public long getLastModified() {
			return -1;
		}

		public boolean respondGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			return false;
		}

		public boolean respondHead(HttpServletRequest req, HttpServletResponse resp) {
			return false;
		}
	}

	public static class RedirectResult implements LookupResult {

		private String path;

		public RedirectResult(String path) {
			if (path.startsWith("/")) {
				path = path.substring(1);
			}
			this.path = path;
		}

		public long getLastModified() {
			return -1;
		}

		public boolean respondGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			ServerUtil.sendHttpRedirect(req, resp, path);
			return true;
		}

		public boolean respondHead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			ServerUtil.sendHttpRedirect(req, resp, path);
			return true;
		}
	}

	public static class ResourceUrl implements LookupResult {
		protected final URLConnection url;
		private String mime;

		public ResourceUrl(String mime, URLConnection url) {
			this.mime = mime;
			this.url = url;
		}

		public long getLastModified() {
			return this.url.getLastModified();
		}

		protected void setHeaders(HttpServletResponse resp) {
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setHeader("Cache-Control", "public, max-age=120");
			resp.setDateHeader("Last-Modified", getLastModified());
			resp.setContentType(mime);
			if (url.getContentLength() >= 0)
				resp.setContentLength(url.getContentLength());
		}

		public boolean respondGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			setHeaders(resp);
			long ims = req.getDateHeader("If-Modified-Since");
			if (ims != -1 && Math.abs(ims - getLastModified()) < 1000) { //modification timestamp is same rounded to seconds
				resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			} else {
				final OutputStream os = resp.getOutputStream();
				CommonUtil.transferStreams(url.getInputStream(), os);
			}
			return true;
		}

		public boolean respondHead(HttpServletRequest req, HttpServletResponse resp) {
			setHeaders(resp);
			return true;
		}
	}

	@Override
	public long getLastModified(HttpServletRequest req) {
		return lookup(req).getLastModified();
	}

	protected LookupResult lookup(HttpServletRequest req) {
		LookupResult r = (LookupResult) req.getAttribute("lookupResult-" + getFullPathMapping());
		if (r == null) {
			r = lookupNoCache(req);
			req.setAttribute("lookupResult-" + getFullPathMapping(), r);
		}
		return r;
	}

	protected LookupResult lookupNoCache(HttpServletRequest req) {
		String path = getPathInfo(req);
		return lookupNoCache(req, path);
	}

	protected LookupResult lookupNoCache(HttpServletRequest req, String path) {
		if (path.equals("")) {
			path = "/index.html";
		}
		if (isForbidden(path))
			return new ErrorResult(HttpServletResponse.SC_FORBIDDEN, "Forbidden");

		URL url = context.getWebResource(path + "/index.html");//check if this is folder with default index
		if (url != null && !req.getPathInfo().endsWith("/")) {
			return new RedirectResult(path + "/");
		}
		if (url == null) {
			url = context.getWebResource(path);
		}
		if (url == null) {
			return new ErrorResult(HttpServletResponse.SC_NOT_FOUND, "Not found");
		}

		String mimeType = getMimeType(url.getPath());
		try {
			return new ResourceUrl(mimeType, url.openConnection());
		} catch (IOException e) {
			log.error("Failed to serve path " + path + " with resource " + url.toString(), e);
			return new ErrorResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	protected boolean isForbidden(String path) {
		String lpath = path.toLowerCase();
		return lpath.startsWith("/web-inf/") || lpath.startsWith("/meta-inf/");
	}

	protected String getMimeType(String path) {
		String mime = getServletContext().getMimeType(path);
		return mime != null ? mime : "application/octet-stream";
	}

}

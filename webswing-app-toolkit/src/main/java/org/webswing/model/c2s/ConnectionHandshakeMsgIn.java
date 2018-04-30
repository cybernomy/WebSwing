package org.webswing.model.c2s;

import java.util.List;

import org.webswing.model.MsgIn;

public class ConnectionHandshakeMsgIn implements MsgIn {

	private static final long serialVersionUID = -7143564320373144470L;

	private String clientId;
	private String sessionId;
	private String viewId;
	private Integer desktopWidth;
	private Integer desktopHeight;
	private String applicationName;
	private String documentBase;
	private String locale;
	private String url;
	private boolean mirrored;
	private boolean directDrawSupported;
	private List<ParamMsg> params;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Integer getDesktopWidth() {
		return desktopWidth;
	}

	public void setDesktopWidth(Integer desktopWidth) {
		this.desktopWidth = desktopWidth;
	}

	public Integer getDesktopHeight() {
		return desktopHeight;
	}

	public void setDesktopHeight(Integer desktopHeight) {
		this.desktopHeight = desktopHeight;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public boolean isMirrored() {
		return mirrored;
	}

	public void setMirrored(boolean mirrored) {
		this.mirrored = mirrored;
	}

	public boolean isDirectDrawSupported() {
		return directDrawSupported;
	}

	public void setDirectDrawSupported(boolean directDrawSupported) {
		this.directDrawSupported = directDrawSupported;
	}

	public String getDocumentBase() {
		return documentBase;
	}

	public void setDocumentBase(String documentBase) {
		this.documentBase = documentBase;
	}

	public List<ParamMsg> getParams() {
		return params;
	}

	public void setParams(List<ParamMsg> params) {
		this.params = params;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getViewId() {
		return viewId;
	}

	public void setViewId(String viewId) {
		this.viewId = viewId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}

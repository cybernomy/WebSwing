package org.webswing.server.services.config;

import org.webswing.server.common.model.SecuredPathConfig;

public class ConfigurationChangeEvent {
	private String path;
	private SecuredPathConfig newConfig;

	public ConfigurationChangeEvent(String path, SecuredPathConfig newConfig) {
		super();
		this.path = path;
		this.newConfig = newConfig;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public SecuredPathConfig getNewConfig() {
		return newConfig;
	}

	public void setNewConfig(SecuredPathConfig newConfig) {
		this.newConfig = newConfig;
	}

}

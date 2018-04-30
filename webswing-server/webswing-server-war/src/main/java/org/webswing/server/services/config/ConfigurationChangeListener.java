package org.webswing.server.services.config;

public interface ConfigurationChangeListener {

	void onConfigChanged(ConfigurationChangeEvent e);

	void onConfigDeleted(ConfigurationChangeEvent e);
}
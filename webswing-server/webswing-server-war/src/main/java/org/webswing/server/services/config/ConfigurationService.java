package org.webswing.server.services.config;

import java.util.List;
import java.util.Map;

import org.webswing.server.base.WebswingService;
import org.webswing.server.common.model.SecuredPathConfig;
import org.webswing.server.common.model.meta.ConfigContext;
import org.webswing.server.common.model.meta.MetaObject;
import org.webswing.server.model.exception.WsException;

public interface ConfigurationService extends WebswingService {

	SecuredPathConfig getConfiguration(String path);

	List<String> getPaths();

	void setConfiguration(String path, Map<String, Object> securedPathConfig) throws Exception;

	void removeConfiguration(String path) throws Exception;

	void registerChangeListener(ConfigurationChangeListener listener);

	void removeChangeListener(ConfigurationChangeListener listener);

	MetaObject describeConfiguration(String string, Map<String, Object> json, ConfigContext ctx) throws WsException;

	boolean isMultiApplicationMode(); 
}

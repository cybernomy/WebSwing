package org.webswing.server.extension;

import java.util.List;
import java.util.Map;

import org.webswing.server.common.model.SecuredPathConfig;
import org.webswing.server.common.model.meta.ConfigContext;
import org.webswing.server.common.model.meta.MetaObject;
import org.webswing.server.model.exception.WsException;
import org.webswing.server.model.exception.WsInitException;

public interface ConfigurationProvider {

	List<String> getPaths();

	Map<String, Object> getConfiguration(String path);

	void saveConfiguration(String path, Map<String, Object> configuration) throws Exception;

	void removeConfiguration(String path) throws Exception;

	void validateConfiguration(String path, Map<String, Object> c) throws Exception;

	SecuredPathConfig toSecuredPathConfig(String path, Map<String, Object> configuration);

	MetaObject describeConfiguration(String path, Map<String, Object> json, ConfigContext ctx, ClassLoader cl) throws WsException;

	Map<String, Object> createDefaultConfiguration(String path);

	boolean isMultiApplicationMode();

}

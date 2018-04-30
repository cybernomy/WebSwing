package org.webswing.server.extension;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.common.model.SecuredPathConfig;
import org.webswing.server.common.model.meta.ConfigContext;
import org.webswing.server.common.model.meta.MetaObject;
import org.webswing.server.common.util.CommonUtil;
import org.webswing.server.common.util.ConfigUtil;
import org.webswing.server.common.util.WebswingObjectMapper;
import org.webswing.server.model.exception.WsException;
import org.webswing.server.model.exception.WsInitException;
import org.webswing.toolkit.util.DeamonThreadFactory;

public class DefaultConfigurationProvider implements ConfigurationProvider {
	private static final Logger log = LoggerFactory.getLogger(DefaultConfigurationProvider.class);

	private ScheduledExecutorService configReloader = Executors.newSingleThreadScheduledExecutor(DeamonThreadFactory.getInstance("Webswing Config Monitor"));
	protected Map<String, Object> configuration = new HashMap<String, Object>();
	private long fileLastModified;
	private ConfigurationUpdateHandler updateHandler;

	public DefaultConfigurationProvider() throws WsInitException {
		loadConfiguration();
		int interval = Integer.getInteger(Constants.CONFIG_RELOAD_INTERVAL_MS, 1000);
		if (interval > 0) {
			configReloader.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					try {
						reloadConfiguration();
					} catch (Throwable t) {
						log.error("Failed to reload configuration.", t);
					}
				}
			}, interval, interval, TimeUnit.MILLISECONDS);
		}
	}

	public DefaultConfigurationProvider(ConfigurationUpdateHandler updateHandler) throws WsInitException {//used via reflection
		this();
		this.updateHandler = updateHandler;
	}

	@Override
	public List<String> getPaths() {
		return new ArrayList<String>(configuration.keySet());
	}

	@Override
	public Map<String, Object> getConfiguration(String path) {
		Map<String, Object> pathConfig = (Map<String, Object>) configuration.get(path);
		return cloneJsonObject(pathConfig);
	}

	private Map<String, Object> cloneJsonObject(Map<String, Object> obj) {
		if (obj != null && obj instanceof Map) {
			try {
				String serialized = WebswingObjectMapper.get().writeValueAsString(obj);
				return WebswingObjectMapper.get().readValue(serialized, Map.class);
			} catch (IOException e) {
				log.error("Failed to clone configuration object", e);
			}
		}
		return obj;
	}

	@Override
	public SecuredPathConfig toSecuredPathConfig(String path, Map<String, Object> configuration) {
		HashMap<String, Object> copy = configuration == null ? new HashMap<String, Object>() : new HashMap<String, Object>(configuration);
		copy.put("path", CommonUtil.toPath(path));
		SecuredPathConfig pathConfig = ConfigUtil.instantiateConfig(copy, SecuredPathConfig.class);
		return pathConfig;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void saveConfiguration(String path, Map<String, Object> configuration) throws Exception {
		try {
			File configFile = getConfigFile();
			Map<String, Object> json = WebswingObjectMapper.get().readValue(configFile, Map.class);
			configuration.put("path", path);
			json.put(path, configuration);
			synchronized (this) {
				WebswingObjectMapper.get().writerWithDefaultPrettyPrinter().writeValue(configFile, json);
				this.fileLastModified = 0;
				reloadConfiguration();
			}
		} catch (Exception e) {
			log.error("Failed to save Webswing configuration :", e);
			throw new Exception("Failed to save Webswing configuration :", e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void removeConfiguration(String path) throws Exception {
		try {
			File configFile = getConfigFile();
			Map<String, Object> json = WebswingObjectMapper.get().readValue(configFile, Map.class);
			json.remove(path);
			synchronized (this) {
				WebswingObjectMapper.get().writerWithDefaultPrettyPrinter().writeValue(configFile, json);
				this.fileLastModified = 0;
				reloadConfiguration();
			}
		} catch (Exception e) {
			log.error("Failed to save Webswing configuration :", e);
			throw new Exception("Failed to save Webswing configuration :", e);
		}
	}

	@Override
	public void validateConfiguration(String path, Map<String, Object> configuration) throws Exception {
		SecuredPathConfig c = toSecuredPathConfig(path, configuration);
		try {
			WebswingObjectMapper.get().writeValueAsString(c);
		} catch (Exception e) {
			throw new WsException("Configuration Json is not valid.", e);
		}
	}

	@Override
	public MetaObject describeConfiguration(String path, Map<String, Object> json, ConfigContext ctx, ClassLoader cl) throws WsException {
		if (json == null) {
			json = getConfiguration(path);
		}
		SecuredPathConfig securedPathConfig = toSecuredPathConfig(path, json);
		try {
			MetaObject result = ConfigUtil.getConfigMetadata(securedPathConfig, cl, ctx);
			result.setData(json);
			return result;
		} catch (Exception e) {
			log.error("Failed to generate configuration descriptor.", e);
			throw new WsException("Failed to generate configuration descriptor.");
		}
	}

	@SuppressWarnings("unchecked")
	protected synchronized void loadConfiguration() throws WsInitException {
		try {
			File config = getConfigFile();
			if (config != null && config.exists()) {
				if (isConfigFileModified(config, this.fileLastModified)) {
					this.fileLastModified = config.lastModified();
					Map<String, Object> json = WebswingObjectMapper.get().readValue(config, Map.class);
					configuration = fixPaths(config, json);
				}
			} else {
				if (this.fileLastModified != -1) {
					this.fileLastModified = -1;
					throw new WsInitException("Configuration file " + (config != null ? config.getPath() : null) + " does not exist!");
				}
			}
		} catch (Exception e) {
			throw new WsInitException("Webswing application configuration failed to load:", e);
		}
	}

	private Map<String, Object> fixPaths(File config, Map<String, Object> json) throws IOException {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		boolean changed = false;
		for (String key : json.keySet()) {
			String path = CommonUtil.toPath(key).toLowerCase();
			path = StringUtils.isEmpty(path) ? "/" : path;
			result.put(path, json.get(key));
			if (!StringUtils.equals(path, key)) {
				changed = true;
			}
		}
		if (changed) {
			try {
				WebswingObjectMapper.get().writerWithDefaultPrettyPrinter().writeValue(config, result);
			} catch (IOException e) {
				log.error("Failed save fixed paths in configuration file. ", e);
			}
		}
		return result;
	}

	private boolean isConfigFileModified(File config, long lastModified) {
		long currentLastModified = config.lastModified();
		if (currentLastModified != lastModified) {
			return true;
		} else {
			return false;
		}
	}

	protected File getConfigFile() throws WsInitException {
		return CommonUtil.getConfigFile();
	}

	@Override
	public Map<String, Object> createDefaultConfiguration(String path) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("path", path);
		return result;
	}

	@Override
	public boolean isMultiApplicationMode() {
		return true;
	}

	private void reloadConfiguration() throws WsInitException {
		if (updateHandler != null) {
			Map<String, Object> oldConfig = configuration;
			loadConfiguration();
			Map<String, Object> newConfig = configuration;
			if (oldConfig != newConfig) {
				notifyChanges(oldConfig, newConfig);
			}
		}
	}

	private void notifyChanges(Map<String, Object> oldConfig, Map<String, Object> newConfig) {
		for (String path : newConfig.keySet()) {
			if (ObjectUtils.notEqual(oldConfig.get(path), newConfig.get(path))) {
				if (newConfig.get(path) instanceof Map) {
					try {
						//update
						updateHandler.notifyConfigChanged(path, toSecuredPathConfig(path, (Map<String, Object>) newConfig.get(path)));
						log.info("App configuration for path '" + path + "' changed.");
					} catch (Exception e) {
						log.error("Failed to update app configuration for path '" + path + "'.", e);
					}
				} else {
					log.warn("Path '" + path + "' has invalid configuration value.");
				}
			}
			oldConfig.remove(path);
		}
		for (String path : oldConfig.keySet()) {
			try {
				//delete
				updateHandler.notifyConfigDeleted(path);
				log.error("Deleted app configuration '" + path + "'.");
			} catch (Exception e) {
				log.error("Failed to delete app configuration'" + path + "'.", e);
			}
		}
	}

}

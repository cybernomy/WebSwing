package org.webswing.server.services.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.common.model.SecuredPathConfig;
import org.webswing.server.common.model.meta.ConfigContext;
import org.webswing.server.common.model.meta.MetaObject;
import org.webswing.server.common.util.CommonUtil;
import org.webswing.server.extension.ConfigurationProvider;
import org.webswing.server.extension.ConfigurationUpdateHandler;
import org.webswing.server.extension.DefaultConfigurationProvider;
import org.webswing.server.extension.ExtensionClassLoader;
import org.webswing.server.model.exception.WsException;
import org.webswing.server.model.exception.WsInitException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class ConfigurationServiceImpl implements ConfigurationService, ConfigurationUpdateHandler {
	private static final Logger log = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

	private final ExtensionClassLoader extensionLoader;
	private int interval;
	private ConfigurationProvider provider;
	private List<ConfigurationChangeListener> changeListeners = new ArrayList<>();

	@Inject
	public ConfigurationServiceImpl(ExtensionClassLoader extensionLoader) {
		this.extensionLoader = extensionLoader;
		this.interval = Integer.getInteger(Constants.CONFIG_RELOAD_INTERVAL_MS, 1000);
	}

	@Override
	public void start() throws WsInitException {
		String providerClassName = System.getProperty(Constants.CONFIG_PROVIDER, DefaultConfigurationProvider.class.getName());
		try {
			Class<?> providerClass = extensionLoader.loadClass(providerClassName);
			try {
				Constructor<?> constructor = providerClass.getDeclaredConstructor(ConfigurationUpdateHandler.class);
				provider = (ConfigurationProvider) constructor.newInstance(this);
			} catch (NoSuchMethodException e) {
				provider = (ConfigurationProvider) providerClass.newInstance();
			}
		} catch (Exception e) {
			throw new WsInitException("Could not instantiate configuration provider " + providerClassName, e);
		}

	}

	@Override
	public void stop() {
		synchronized (changeListeners) {
			changeListeners.clear();
		}
	}

	@Override
	public SecuredPathConfig getConfiguration(String path) {
		path = asPath(path);
		Map<String, Object> configuration = provider.getConfiguration(path);
		return provider.toSecuredPathConfig(path, configuration);
	}

	public List<String> getPaths() {
		return provider.getPaths();
	}

	@Override
	public void setConfiguration(String path, Map<String, Object> configuration) throws Exception {
		path = asPath(path);
		if (configuration == null) {
			configuration = provider.createDefaultConfiguration(path);
		}
		provider.validateConfiguration(path, configuration);
		provider.saveConfiguration(path, configuration);
	}

	@Override
	public void removeConfiguration(String path) throws Exception {
		provider.removeConfiguration(asPath(path));
	}

	public void registerChangeListener(ConfigurationChangeListener listener) {
		synchronized (changeListeners) {
			changeListeners.add(listener);
		}
	}

	public void removeChangeListener(ConfigurationChangeListener listener) {
		synchronized (changeListeners) {
			changeListeners.remove(listener);
		}
	}

	@Override
	public void notifyConfigChanged(String path, SecuredPathConfig newCfg) {
		synchronized (changeListeners) {
			for (ConfigurationChangeListener listener : changeListeners) {
				if (listener != null) {
					listener.onConfigChanged(new ConfigurationChangeEvent(asPath(path), newCfg));
				}
			}
		}
	}

	@Override
	public void notifyConfigDeleted(String path) {
		synchronized (changeListeners) {
			for (ConfigurationChangeListener listener : changeListeners) {
				if (listener != null) {
					listener.onConfigDeleted(new ConfigurationChangeEvent(asPath(path), null));
				}
			}
		}
	}

	@Override
	public MetaObject describeConfiguration(String path, Map<String, Object> json, ConfigContext ctx) throws WsException {
		return provider.describeConfiguration(asPath(path), json, ctx, extensionLoader);
	}

	private String asPath(String path) {
		String p = CommonUtil.toPath(path);
		if (StringUtils.isBlank(p)) {
			p = "/";
		}
		return p;
	}

	@Override
	public boolean isMultiApplicationMode() {
		return provider.isMultiApplicationMode();
	}
}

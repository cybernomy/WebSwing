package org.webswing.server;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.extension.DefaultInitializer;
import org.webswing.server.extension.ExtensionClassLoader;
import org.webswing.server.extension.Initializer;
import org.webswing.server.services.config.ConfigurationService;
import org.webswing.server.services.config.ConfigurationServiceImpl;
import org.webswing.server.services.files.FileTransferHandlerService;
import org.webswing.server.services.files.FileTransferHandlerServiceImpl;
import org.webswing.server.services.jms.JmsService;
import org.webswing.server.services.jms.JmsServiceImpl;
import org.webswing.server.services.jvmconnection.JvmConnectionService;
import org.webswing.server.services.jvmconnection.JvmConnectionServiceImpl;
import org.webswing.server.services.resources.ResourceHandlerService;
import org.webswing.server.services.resources.ResourceHandlerServiceImpl;
import org.webswing.server.services.security.SecurityManagerService;
import org.webswing.server.services.security.SecurityManagerServiceImpl;
import org.webswing.server.services.security.login.LoginHandlerService;
import org.webswing.server.services.security.login.LoginHandlerServiceImpl;
import org.webswing.server.services.security.modules.SecurityModuleService;
import org.webswing.server.services.security.modules.SecurityModuleServiceImpl;
import org.webswing.server.services.startup.StartupService;
import org.webswing.server.services.startup.StartupServiceImpl;
import org.webswing.server.services.stats.StatisticsLoggerService;
import org.webswing.server.services.stats.StatisticsLoggerServiceImpl;
import org.webswing.server.services.swinginstance.SwingInstanceService;
import org.webswing.server.services.swinginstance.SwingInstanceServiceImpl;
import org.webswing.server.services.swingmanager.SwingInstanceManagerService;
import org.webswing.server.services.swingmanager.SwingInstanceManagerServiceImpl;
import org.webswing.server.services.swingprocess.SwingProcessService;
import org.webswing.server.services.swingprocess.SwingProcessServiceImpl;
import org.webswing.server.services.websocket.WebSocketService;
import org.webswing.server.services.websocket.WebSocketServiceImpl;

import com.google.inject.AbstractModule;

public class WebswingServerModule extends AbstractModule {
	private static final Logger log = LoggerFactory.getLogger(WebswingServerModule.class);

	@Override
	protected void configure() {
		initializeDefaultSystemProperties();

		//extendables
		bindSingletonExtension(Initializer.class, Constants.EXTENSTION_INITIALIZER, DefaultInitializer.class);
		bindSingletonExtension(ExtensionClassLoader.class, Constants.EXTENSTION_CLASSLOADER, ExtensionClassLoader.class);

		bind(GlobalUrlHandler.class);

		bind(StartupService.class).to(StartupServiceImpl.class);
		bind(WebSocketService.class).to(WebSocketServiceImpl.class);
		bind(JmsService.class).to(JmsServiceImpl.class);
		bind(ConfigurationService.class).to(ConfigurationServiceImpl.class);
		bind(SwingInstanceManagerService.class).to(SwingInstanceManagerServiceImpl.class);
		bind(SwingInstanceService.class).to(SwingInstanceServiceImpl.class);
		bind(JvmConnectionService.class).to(JvmConnectionServiceImpl.class);
		bind(SwingProcessService.class).to(SwingProcessServiceImpl.class);
		bind(FileTransferHandlerService.class).to(FileTransferHandlerServiceImpl.class);
		bind(ResourceHandlerService.class).to(ResourceHandlerServiceImpl.class);
		bind(StatisticsLoggerService.class).to(StatisticsLoggerServiceImpl.class);

		bind(SecurityManagerService.class).to(SecurityManagerServiceImpl.class);
		bind(SecurityModuleService.class).to(SecurityModuleServiceImpl.class);
		bind(LoginHandlerService.class).to(LoginHandlerServiceImpl.class);
	}

	private void initializeDefaultSystemProperties() {
		try {
			InputStream propFile = StartupServiceImpl.class.getClassLoader().getResourceAsStream("webswing.properties");
			Properties p = new Properties(System.getProperties());
			p.load(propFile);
			// set the system properties
			for (Map.Entry<Object, Object> prop : p.entrySet()) {
				if(!System.getProperties().containsKey(prop.getKey()))
					System.getProperties().put(prop.getKey(), prop.getValue());
			}

		} catch (Exception e) {
			log.error("Exception occurred during initialization of System Properties", e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void bindSingletonExtension(Class<T> target, String extensionClassProp, Class<? extends T> defaultClass) {
		Class<? extends T> result = defaultClass;
		String extensionClassName = System.getProperty(extensionClassProp);
		if (extensionClassName != null) {
			try {
				Class<?> extensionClass = WebswingServerModule.class.getClassLoader().loadClass(extensionClassName);
				result = (Class<? extends T>) extensionClass;
			} catch (Exception e) {
				log.error("Failed to load extension class " + extensionClassName + ". Falling back to default " + defaultClass.getName());
			}
		}

		if (result != target) {
			bind(target).to(result).asEagerSingleton();
		}
	}

}

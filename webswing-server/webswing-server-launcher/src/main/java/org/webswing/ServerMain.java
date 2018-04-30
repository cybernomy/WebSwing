package org.webswing;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.webswing.toolkit.util.Logger;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {

	public static void main(String[] args) throws Exception {
		Configuration config = ConfigurationImpl.parse(args);
		System.out.println(config.toString());
		System.setProperty(Constants.SERVER_EMBEDED_FLAG, "true");
		System.setProperty(Constants.SERVER_PORT, config.getHttpPort());
		System.setProperty(Constants.SERVER_HOST, config.getHost());
		boolean isHttpsOnly = config.isHttps() && !config.isHttp();
		System.setProperty(Constants.HTTPS_ONLY, System.getProperty(Constants.HTTPS_ONLY, ""+isHttpsOnly));
		if (config.getConfigFile() != null) {
			File configFile = new File(config.getConfigFile());
			if (configFile.exists()) {
				System.setProperty(Constants.CONFIG_FILE_PATH, configFile.toURI().toString());
			} else {
				Logger.error("Webswing configuration file " + config.getConfigFile() + " not found. Using default location.");
			}
		}

		Server server = new Server();

		List<Connector> connectors = new ArrayList<Connector>();
		if (config.isHttp()) {
			HttpConfiguration http_config = new HttpConfiguration();
			if (config.isHttps()) {
				http_config.setSecurePort(Integer.parseInt(config.getHttpsPort()));
			}
			ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
			http.setPort(Integer.parseInt(config.getHttpPort()));
			http.setHost(config.getHost());
			connectors.add(http);
		}
		if (config.isHttps()) {
			if (config.isHttps() && config.getTruststore() != null && !config.getTruststore().isEmpty() && config.getKeystore() != null && config.getKeystore().isEmpty()) {
				Logger.error("SSL configuration is invalid. Please specify the location of truststore and keystore files.");
			} else {
				if (!new File(config.getTruststore()).exists()) {
					Logger.error("SSL configuration is invalid. Truststore file " + new File(config.getTruststore()).getAbsolutePath() + " does not exist.");
				} else if (!new File(config.getKeystore()).exists()) {
					Logger.error("SSL configuration is invalid. Keystore file " + new File(config.getKeystore()).getAbsolutePath() + " does not exist.");
				} else {
					SslContextFactory sslContextFactory = new SslContextFactory();
					sslContextFactory.setKeyStorePath(config.getKeystore());
					sslContextFactory.setKeyStorePassword(config.getKeystorePassword());
					sslContextFactory.setTrustStorePath(config.getTruststore());
					sslContextFactory.setTrustStorePassword(config.getTruststorePassword());
					sslContextFactory.setNeedClientAuth(false);

					HttpConfiguration https_config = new HttpConfiguration();
					SecureRequestCustomizer src = new SecureRequestCustomizer();
					https_config.addCustomizer(src);

					ServerConnector https = new ServerConnector(server, sslContextFactory, new HttpConnectionFactory(https_config));
					https.setPort(Integer.parseInt(config.getHttpsPort()));
					https.setHost(config.getHost());
					connectors.add(https);
				}
			}
		}

		server.setConnectors(connectors.toArray(new Connector[connectors.size()]));

		// enable jmx
		MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
		server.addEventListener(mbContainer);
		server.addBean(mbContainer);

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/");
		webapp.setWar(System.getProperty(Constants.WAR_FILE_LOCATION));
		webapp.setTempDirectory(new File(URI.create(System.getProperty(Constants.TEMP_DIR_PATH))));
		webapp.setPersistTempDirectory(true);
		webapp.setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern", "");
		webapp.setThrowUnavailableOnStartupException(true);
		server.setHandler(webapp);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			server.stop();
		}
	}
}

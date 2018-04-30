package org.webswing;

public abstract class Configuration {

	private static Configuration singleton = new ConfigurationImpl();

	public abstract String getHost();

	public abstract boolean isHttp();

	public abstract String getHttpPort();

	public abstract boolean isHttps();

	public abstract String getHttpsPort();

	public abstract String getTruststore();

	public abstract String getTruststorePassword();

	public abstract String getKeystore();

	public abstract String getKeystorePassword();

	public abstract String getConfigFile();

	public static Configuration getInstance() {
		return singleton;
	}

}

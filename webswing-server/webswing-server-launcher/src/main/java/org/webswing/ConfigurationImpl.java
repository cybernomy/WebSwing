package org.webswing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.webswing.toolkit.util.Logger;

public class ConfigurationImpl extends Configuration {

	private static final String PREFIX = System.getProperty(Constants.BRANDING_PREFIX, "org.webswing");

	private String host = "localhost";

	private boolean http = true;
	private String httpPort = "8080";

	private boolean https = false;
	private String httpsPort = "8443";
	private String truststore;
	private String truststorePassword;
	private String keystore;
	private String keystorePassword;

	private String configFile;

	public static Configuration parse(String[] args) {
		ConfigurationImpl cimpl = (ConfigurationImpl) Configuration.getInstance();
		// create the command line parser
		CommandLineParser parser = new PosixParser();

		// create the Options
		Options options = new Options();
		options.addOption("h", "host", true, "Local interface address where the web server will listen. (localhost)");
		options.addOption("p", "port", true, "Http port where the web server will listen. If 0 http is disabled. (8080)");

		options.addOption("s", "sslport", true, "Https port where the web server will listen. If 0 http is disabled. (0)");
		options.addOption("ts", "truststore", true, "Truststore file location for ssl configuration ");
		options.addOption("tp", "truststorepwd", true, "Truststore password");
		options.addOption("ks", "keystore", true, "Keystore file location for ssl configuration");
		options.addOption("kp", "keystorepwd", true, "Keystore password");

		options.addOption("t", "temp", true, "The folder where temp folder will be created for the server. (./tmp)");
		options.addOption("tc", "tempclean", true, "Delete the content of temp folder. (true)");
		
		options.addOption("d", true, "Create new temp folder for every instance (false)");

		options.addOption("j", "jetty", true, "Jetty startup configuration file. (./jetty.properties)");
		options.addOption("c", "config", true, "Configuration file name. (<webswing-server.war path>/webswing.config)");

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			// read jetty.properties config file
			if (line.getOptionValue('j') != null) {
				cimpl.readPropertyFile(line.getOptionValue('j'));
			}

			// override configuration
			if (line.getOptionValue('h') != null) {
				cimpl.setHost(line.getOptionValue('h'));
			}

			if (line.getOptionValue('p') != null) {
				String value = line.getOptionValue('p');
				cimpl.setHttp(value.equals("0") ? false : true);
				cimpl.setHttpPort(value);
			}

			if (line.getOptionValue('s') != null) {
				String value = line.getOptionValue('s');
				cimpl.setHttp(value.equals("0") ? false : true);
				cimpl.setHttpPort(value);
			}

			if (line.getOptionValue("ts") != null) {
				String value = line.getOptionValue("ts");
				cimpl.setTruststore(value);
			}

			if (line.getOptionValue("tp") != null) {
				String value = line.getOptionValue("tp");
				cimpl.setTruststorePassword(value);
			}

			if (line.getOptionValue("ks") != null) {
				String value = line.getOptionValue("ks");
				cimpl.setKeystore(value);
			}

			if (line.getOptionValue("kp") != null) {
				String value = line.getOptionValue("kp");
				cimpl.setKeystorePassword(value);
			}

			if (line.getOptionValue('c') != null) {
				cimpl.setConfigFile(line.getOptionValue('c'));
			}

			// NOTE: -d and -t are parsed in main.Main
		} catch (ParseException exp) {
			Logger.debug(exp.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(PREFIX, options);
		} catch (IOException e) {
			Logger.error("Server configuration failed.", e);
		}
		return cimpl;
	}

	private void readPropertyFile(String filename) throws IOException {
		Properties prop = new Properties();
		InputStream inputStream = new FileInputStream(new File(filename));
		prop.load(inputStream);
		setHost(prop.getProperty(PREFIX + ".server.host"));

		setHttp(Boolean.parseBoolean(prop.getProperty(PREFIX + ".server.http")));
		setHttpPort(prop.getProperty(PREFIX + ".server.http.port"));

		setHttps(Boolean.parseBoolean(prop.getProperty(PREFIX + ".server.https")));
		setHttpsPort(prop.getProperty(PREFIX + ".server.https.port"));
		setTruststore(prop.getProperty(PREFIX + ".server.https.truststore"));
		setTruststorePassword(prop.getProperty(PREFIX + ".server.https.truststore.password"));
		setKeystore(prop.getProperty(PREFIX + ".server.https.keystore"));
		setKeystorePassword(prop.getProperty(PREFIX + ".server.https.keystore.password"));
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public boolean isHttp() {
		return http;
	}

	public void setHttp(boolean http) {
		this.http = http;
	}

	public String getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(String httpPort) {
		this.httpPort = httpPort;
	}

	public boolean isHttps() {
		return https;
	}

	public void setHttps(boolean https) {
		this.https = https;
	}

	public String getHttpsPort() {
		return httpsPort;
	}

	public void setHttpsPort(String httpsPort) {
		this.httpsPort = httpsPort;
	}

	public String getTruststore() {
		return truststore;
	}

	public void setTruststore(String truststore) {
		this.truststore = truststore;
	}

	public String getTruststorePassword() {
		return truststorePassword;
	}

	public void setTruststorePassword(String truststorePassword) {
		this.truststorePassword = truststorePassword;
	}

	public String getKeystorePassword() {
		return keystorePassword;
	}

	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	public String getKeystore() {
		return keystore;
	}

	public void setKeystore(String keystore) {
		this.keystore = keystore;
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	@Override
	public String toString() {
		return "########################Server Configuration ################################\n" + " host=" + host + "\n http=" + http + "\n httpPort=" + httpPort + "\n https=" + https + "\n httpsPort=" + httpsPort + "\n truststore=" + truststore + "\n truststorePassword=***" + "\n keystore=" + keystore + "\n keystorePassword=***"
				+ "\n configFile=" + configFile + "\n########################Server Configuration End#############################\n";
	}

}

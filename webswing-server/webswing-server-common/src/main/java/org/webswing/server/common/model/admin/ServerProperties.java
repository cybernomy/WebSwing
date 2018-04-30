package org.webswing.server.common.model.admin;

import java.io.Serializable;

public class ServerProperties implements Serializable {

	private static final long serialVersionUID = 3055086762377696323L;
	private String host;
	private String port;
	private String configFile;
	private String tempFolder;
	private String jmsServerUrl;
	private String warLocation;
	private String userProps;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public String getTempFolder() {
		return tempFolder;
	}

	public void setTempFolder(String tempFolder) {
		this.tempFolder = tempFolder;
	}

	public String getJmsServerUrl() {
		return jmsServerUrl;
	}

	public void setJmsServerUrl(String jmsServerUrl) {
		this.jmsServerUrl = jmsServerUrl;
	}

	public String getWarLocation() {
		return warLocation;
	}

	public void setWarLocation(String warLocation) {
		this.warLocation = warLocation;
	}

	public String getUserProps() {
		return userProps;
	}

	public void setUserProps(String userProps) {
		this.userProps = userProps;
	}

}

package org.webswing.server.extension;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.Constants;
import org.webswing.server.model.exception.WsInitException;

import main.Main;

public class DefaultInitializer implements Initializer {
	private static final Logger log = LoggerFactory.getLogger(DefaultInitializer.class);

	public DefaultInitializer() {
		try {
			start();
		} catch (WsInitException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void start() throws WsInitException {
		// initialize temp folder (create/delete old content)
		File tempDir = Main.getTempDir();
		log.info("Using Temp folder:" + tempDir.getAbsolutePath());

		// initialize root dir
		File root = Main.getRootDir();
		log.info("Using Root folder:" + root.getAbsolutePath());

		// verify war file and convert to URI
		validatePropertyFilePath(Constants.WAR_FILE_LOCATION, null);
		log.info("Using War file:" + System.getProperty(Constants.WAR_FILE_LOCATION));

		// verify config file and convert to URI
		validatePropertyFilePath(Constants.CONFIG_FILE_PATH, Constants.DEFAULT_CONFIG_FILE_NAME);
		log.info("Using Config file:" + System.getProperty(Constants.CONFIG_FILE_PATH));
	}

	protected void validatePropertyFilePath(String propertyName, String defaultValue) throws WsInitException {
		try {
			String configFilePath = getValidURI(System.getProperty(propertyName, defaultValue));
			System.setProperty(propertyName, configFilePath);
		} catch (FileNotFoundException e) {
			throw new WsInitException("Invalid system property " + propertyName + ": " + e.getMessage());
		}
	}

	protected String getValidURI(String pathOrUri) throws FileNotFoundException {
		if (pathOrUri != null) {
			try {
				URI uri = URI.create(pathOrUri);
				if (new File(uri).exists()) {
					return pathOrUri;
				} else {
					throw new FileNotFoundException("File " + uri.toString() + "not found.");
				}
			} catch (IllegalArgumentException e) {
				File relativeConfigFile = new File(Main.getRootDir(), pathOrUri);
				File absoluteConfigFile = new File(pathOrUri);
				if (relativeConfigFile.exists()) {
					return relativeConfigFile.toURI().toString();
				} else if (absoluteConfigFile.exists()) {
					return absoluteConfigFile.toURI().toString();
				} else {
					throw new FileNotFoundException("File " + relativeConfigFile.getAbsolutePath() + " or " + absoluteConfigFile.getAbsolutePath() + " not found.");
				}
			}
		}
		throw new FileNotFoundException("Path not specified.");
	}
}

package org.webswing.applet;

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.webswing.Constants;
import org.webswing.toolkit.util.Logger;

public class WebAppletStub implements AppletStub {

	private AppletContainer container;
	private Map<String, String> props;

	public WebAppletStub(AppletContainer ac, Map<String, String> props) {
		this.container = ac;
		this.props = props;
	}

	@Override
	public boolean isActive() {
		return container.isActive();
	}

	@Override
	public URL getDocumentBase() {
		String docBase = System.getProperty(Constants.SWING_START_SYS_PROP_APPLET_DOCUMENT_BASE);
		try {
			return new URL(docBase);
		} catch (MalformedURLException e) {
			Logger.error("Applet DocumentBase property is invalid " + docBase + ":", e);
			return null;
		}
	}

	@Override
	public URL getCodeBase() {
		String currentDir = System.getProperty("user.dir");
		try {
			return new File(currentDir).toURI().toURL();
		} catch (MalformedURLException e) {
			Logger.error("Applet CodeBase is invalid " + currentDir + ":", e);
			return null;
		}
	}

	@Override
	public String getParameter(String name) {
		return props.get(name);
	}

	@Override
	public AppletContext getAppletContext() {
		return container.getContext();
	}

	@Override
	public void appletResize(int width, int height) {
		container.setSize(width, height);
	}

}

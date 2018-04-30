package org.webswing.applet;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AudioClip;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.webswing.toolkit.util.Logger;
import org.webswing.toolkit.util.Util;

import sun.awt.image.URLImageSource;
import sun.misc.Ref;

@SuppressWarnings({ "restriction", "deprecation" })
public class WebAppletContext implements AppletContext {

	private static Map<URL, AppletImageRef> imageRefs = new HashMap<URL, WebAppletContext.AppletImageRef>();
	private AppletContainer container;

	public WebAppletContext(AppletContainer appletContainer) {
		this.container = appletContainer;
	}

	@Override
	public AudioClip getAudioClip(URL url) {
		return null;// not supported
	}

	public Image getImage(URL paramURL) {
		return ((Image) getCachedImageRef(paramURL).get());
	}

	static AppletImageRef getCachedImageRef(URL url) {
		synchronized (imageRefs) {
			AppletImageRef imageRef = (AppletImageRef) imageRefs.get(url);
			if (imageRef == null) {
				imageRef = new AppletImageRef(url);
				imageRefs.put(url, imageRef);
			}
			return imageRef;
		}
	}

	@Override
	public Applet getApplet(String name) {
		throw null;
	}

	@Override
	public Enumeration<Applet> getApplets() {
		Vector<Applet> applets = new Vector<Applet>();
		applets.addElement(container.getApplet());
		return applets.elements();
	}

	@Override
	public void showDocument(URL url) {
		try {
			Util.getWebToolkit().getPaintDispatcher().notifyOpenLinkAction(url.toURI());
		} catch (URISyntaxException e) {
			Logger.error("AppletContext.showDocument failed.", e);
		}
	}

	@Override
	public void showDocument(URL url, String target) {
		try {
			Util.getWebToolkit().getPaintDispatcher().notifyOpenLinkAction(url.toURI());
		} catch (URISyntaxException e) {
			Logger.error("AppletContext.showDocument failed.", e);
		}
	}

	@Override
	public void showStatus(String status) {
		Logger.info("Applet status: " + status);
	}

	@Override
	public void setStream(String key, InputStream stream) throws IOException {
		// do nothing
	}

	@Override
	public InputStream getStream(String key) {
		// do nothing
		return null;
	}

	@Override
	public Iterator<String> getStreamKeys() {
		// do nothing
		return null;
	}
	
	public AppletContainer getContainer(){
		return container;
	}

	static class AppletImageRef extends Ref {
		URL url;

		AppletImageRef(URL paramURL) {
			this.url = paramURL;
		}

		public void flush() {
			super.flush();
		}

		public Object reconstitute() {
			Image localImage = Toolkit.getDefaultToolkit().createImage(new URLImageSource(this.url));
			return localImage;
		}
	}
}

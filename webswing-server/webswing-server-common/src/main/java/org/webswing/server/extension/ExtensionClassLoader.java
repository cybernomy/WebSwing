package org.webswing.server.extension;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class ExtensionClassLoader extends URLClassLoader {

	private ClassLoader webClassLoader;

	public ExtensionClassLoader(URL[] urls, ClassLoader webClassLoaders) {
		super(urls, ClassLoader.getSystemClassLoader());
		this.webClassLoader = webClassLoaders;
	}

	public ExtensionClassLoader() {
		this(new URL[0], ExtensionClassLoader.class.getClassLoader());
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			return webClassLoader.loadClass(name);
		} catch (ClassNotFoundException e) {
			return super.loadClass(name,resolve);
		}
	}

	public void close() throws IOException {
		webClassLoader = null;
	}

	public URL getResource(String name) {
		URL url = super.getResource(name);
		if (url == null) {
			url = webClassLoader.getResource(name);
		}
		return url;
	}

}
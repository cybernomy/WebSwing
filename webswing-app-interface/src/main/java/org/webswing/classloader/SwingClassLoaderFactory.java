package org.webswing.classloader;

import java.net.URL;
import org.webswing.ext.services.SwingClassLoaderFactoryService;

public class SwingClassLoaderFactory implements SwingClassLoaderFactoryService {

	private static SwingClassLoaderFactory impl;

	public static SwingClassLoaderFactory getInstance() {
		if (impl == null) {
			impl = new SwingClassLoaderFactory();
		}
		return impl;
	}

	private SwingClassLoaderFactory() {
	}

	@Override
	public ClassLoader createSwingClassLoader(URL[] classpath, ClassLoader parent) {
		return new SwingClassloader(classpath, parent);
	}
}

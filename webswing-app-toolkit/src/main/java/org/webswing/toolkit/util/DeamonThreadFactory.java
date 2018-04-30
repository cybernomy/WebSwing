package org.webswing.toolkit.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DeamonThreadFactory implements ThreadFactory {

	private final String name;

	private DeamonThreadFactory(String name) {
		this.name = name;
	}

	public static ThreadFactory getInstance(String name) {
		return new DeamonThreadFactory(name);
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        t.setName(name);
        return t;
	}
}

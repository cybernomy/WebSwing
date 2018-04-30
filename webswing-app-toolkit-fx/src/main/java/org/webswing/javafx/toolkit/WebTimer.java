package org.webswing.javafx.toolkit;

import com.sun.glass.ui.Timer;
import org.webswing.toolkit.util.DeamonThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WebTimer extends Timer {
	static ScheduledExecutorService timer ;
	private ScheduledFuture<?> future;

	protected WebTimer(final Runnable runnable) {
		super(runnable);
	}

	@Override
	protected long _start(final Runnable runnable, int period) {
		if (timer == null) {
			timer = Executors.newSingleThreadScheduledExecutor(DeamonThreadFactory.getInstance("Webswing FX Timer"));
		}
		future= timer.scheduleAtFixedRate(runnable, 0, period, TimeUnit.MILLISECONDS);
		return 1; // need something non-zero to denote success.
	}

	@Override
	protected long _start(Runnable runnable) {
		throw new RuntimeException("vsync timer not supported");
	}

	@Override
	protected void _stop(long timer) {
		if (future != null) {
			future.cancel(false);
			future = null;
		}
	}
}

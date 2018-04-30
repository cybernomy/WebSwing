package org.webswing.toolkit;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.peer.KeyboardFocusManagerPeer;
import java.lang.reflect.Method;

import org.webswing.toolkit.extra.WindowManager;
import org.webswing.toolkit.util.Logger;

import sun.awt.CausedFocusEvent;
import sun.awt.SunToolkit;

@SuppressWarnings("restriction")
public class WebKeyboardFocusManagerPeer implements KeyboardFocusManagerPeer {

	@Override
	public void clearGlobalFocusOwner(Window activeWindow) {
	}

	@Override
	public Component getCurrentFocusOwner() {
		return WindowManager.getInstance().getActiveWindow().getFocusOwner();
	}

	@Override
	public Window getCurrentFocusedWindow() {
		return WindowManager.getInstance().getActiveWindow();
	}

	@Override
	public void setCurrentFocusOwner(Component comp) {

	}

	@Override
	public void setCurrentFocusedWindow(Window win) {

	}

	public static int shouldNativelyFocusHeavyweight(Window heavyweight, Component descendant, boolean temporary, boolean focusedWindowChangeAllowed, long time, CausedFocusEvent.Cause cause) {
		try {
			Method m2 = KeyboardFocusManager.class.getDeclaredMethod("shouldNativelyFocusHeavyweight", Component.class, Component.class, Boolean.TYPE, Boolean.TYPE, Long.TYPE, CausedFocusEvent.Cause.class);
			m2.setAccessible(true);
			Integer result2 = (Integer) m2.invoke(null, heavyweight, descendant, temporary, focusedWindowChangeAllowed, time, cause);
			return result2;
		} catch (Exception e) {
			Logger.debug("Failed to invoke processSynchronousLightweightTransfer on KeyboardFocusManager. Check your java version.", e);
			return 0;
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean deliverFocus(Component heavyweight, Component descendant, boolean temporary, boolean focusedWindowChangeAllowed, long time, CausedFocusEvent.Cause cause) {
		if (heavyweight == null) {
			heavyweight = descendant;
		}

		Component c = WindowManager.getInstance().getActiveWindow().getFocusOwner();
		CausedFocusEvent focusEvent;
		if ((c != null) && (c.getPeer() == null)) {
			c = null;
		}
		if (c != null) {
			focusEvent = new CausedFocusEvent(c, CausedFocusEvent.FOCUS_LOST, false, heavyweight, cause);

			SunToolkit.postEvent(SunToolkit.targetToAppContext(c), focusEvent);
		}

		focusEvent = new CausedFocusEvent(heavyweight, CausedFocusEvent.FOCUS_GAINED, false, c, cause);

		SunToolkit.postEvent(SunToolkit.targetToAppContext(heavyweight), focusEvent);
		return true;
	}

}

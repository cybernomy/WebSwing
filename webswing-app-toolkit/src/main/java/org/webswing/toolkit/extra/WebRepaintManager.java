package org.webswing.toolkit.extra;

import java.applet.Applet;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

import org.webswing.toolkit.util.Util;

public class WebRepaintManager extends RepaintManager {

	private RepaintManager delegate;
	private Map<Container, Rectangle> dirty = new HashMap<Container, Rectangle>();

	public WebRepaintManager(RepaintManager delegate) {
		if (delegate != null) {
			this.delegate = delegate;
		}
	}

	public void setDelegate(RepaintManager delegate) {
		if (delegate != null) {
			this.delegate = delegate;
		}
	}

	@Override
	public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
		addDirtyRegionPrivate(c, x, y, w, h);
	}

	@Override
	public void addDirtyRegion(Window window, int x, int y, int w, int h) {
		addDirtyRegionPrivate(window, x, y, w, h);
	}

	@Override
	public void addDirtyRegion(Applet applet, int x, int y, int w, int h) {
		addDirtyRegionPrivate(applet, x, y, w, h);
	}

	private void addDirtyRegionPrivate(Container c, int x, int y, int w, int h) {
		synchronized (delegate) {
			Rectangle r = dirty.get(c);
			if (r != null) {
				SwingUtilities.computeUnion(x, y, w, h, r);
			} else {
				dirty.put(c, new Rectangle(x, y, w, h));
			}
		}
	}

	@Override
	public Rectangle getDirtyRegion(JComponent aComponent) {
		Rectangle r;
		synchronized (delegate) {
			r = dirty.get(aComponent);
		}
		if (r == null)
			return new Rectangle(0, 0, 0, 0);
		else
			return new Rectangle(r);
	}

	@Override
	public void markCompletelyClean(JComponent component) {
		synchronized (delegate) {
			dirty.remove(component);
		}
	}

	public static void processDirtyComponents() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (RepaintManager.currentManager(null) instanceof WebRepaintManager) {
					((WebRepaintManager) RepaintManager.currentManager(null)).process();
				} else {
					WebRepaintManager webRepaintManager = new WebRepaintManager(RepaintManager.currentManager(null));
					RepaintManager.setCurrentManager(webRepaintManager);
					for (Window w : Window.getWindows()) {
						if (w.isShowing()) {
							webRepaintManager.addDirtyRegion(w, w.getX(), w.getY(), w.getWidth(), w.getHeight());
						}
					}
					webRepaintManager.process();
				}
			}
		});
	}

	public void process() {
		synchronized (delegate) {
			for (Container c : dirty.keySet()) {
				Rectangle r = dirty.get(c);
				if (c instanceof JComponent) {
					Panel p = Util.findHwComponentParent((JComponent) c);
					if (p != null) {
						for (Component chld : p.getComponents()) {
							delegate.addDirtyRegion((JComponent) chld, 0, 0, chld.getWidth(), chld.getHeight());
						}
					} else {
						delegate.addDirtyRegion((JComponent) c, r.x, r.y, r.width, r.height);
					}
				} else if (c instanceof Window) {
					delegate.addDirtyRegion((Window) c, r.x, r.y, r.width, r.height);
				} else if (c instanceof Applet) {
					delegate.addDirtyRegion((Applet) c, r.x, r.y, r.width, r.height);
				}
			}
			dirty.clear();
		}
	}

	@Override
	public void addInvalidComponent(JComponent invalidComponent) {
		delegate.addInvalidComponent(invalidComponent);
	}

	@Override
	public void removeInvalidComponent(JComponent component) {
		delegate.removeInvalidComponent(component);
	}

	/**
	 * {@inheritDoc}
	 */
	public Dimension getDoubleBufferMaximumSize() {
		return delegate.getDoubleBufferMaximumSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getOffscreenBuffer(Component c, int proposedWidth, int proposedHeight) {
		return delegate.getOffscreenBuffer(c, proposedWidth, proposedHeight);
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getVolatileOffscreenBuffer(Component c, int proposedWidth, int proposedHeight) {
		return delegate.getVolatileOffscreenBuffer(c, proposedWidth, proposedHeight);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCompletelyDirty(JComponent component) {
		return delegate.isCompletelyDirty(component);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDoubleBufferingEnabled() {
		return delegate.isDoubleBufferingEnabled();
	}

	/**
	 * {@inheritDoc}
	 */
	public void markCompletelyDirty(JComponent component) {
		delegate.markCompletelyDirty(component);
	}

	/**
	 * {@inheritDoc}
	 */
	public void paintDirtyRegions() {
		delegate.paintDirtyRegions();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDoubleBufferingEnabled(boolean flag) {
		delegate.setDoubleBufferingEnabled(flag);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDoubleBufferMaximumSize(Dimension d) {
		delegate.setDoubleBufferMaximumSize(d);
	}

	/**
	 * {@inheritDoc}
	 */
	public void validateInvalidComponents() {
		delegate.validateInvalidComponents();
	}

}

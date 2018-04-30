package org.webswing.toolkit.extra;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.webswing.common.WindowActionType;

public class WindowEventHandler {

	private Map<Window, Rectangle> previousSize = new HashMap<Window, Rectangle>();
	private boolean lockedOnEvent;
	private WindowActionType lockEventType;
	private Point referenceMouseLocation;
	private Window window;
	private static final Dimension DEFAULT_MINIMUM_WINDOW_SIZE = new Dimension(100, 25);

	public void handle(WindowActionType wat, MouseEvent e) {
		if (!lockedOnEvent) {
			if (MouseEvent.MOUSE_PRESSED == e.getID() && ((MouseEvent) e).getButton() == 1) {
				lockedOnEvent = true;
				lockEventType = wat;
				referenceMouseLocation = e.getPoint();
				window = (Window) (e.getSource() instanceof Window ? e.getSource() : SwingUtilities.windowForComponent((Component) e.getSource()));
			}
		} else {
			switch (lockEventType) {
			case minimize:
				if (MouseEvent.MOUSE_RELEASED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					Rectangle o = w.getBounds();
					resizeWindow(w, 0, 0);
					if (o.getSize().equals(w.getSize()) && previousSize.containsKey(w)) {
						// restore previous size
						o = previousSize.get(w);
						moveWindow(w, o.x, o.y);
						resizeWindow(w, o.width, o.height);
					} else {
						Rectangle r = w.getBounds();
						// move to the middle of original size
						moveWindow(w, (o.x + (o.width / 2)) - (r.width / 2), (o.y + (o.height / 2)) - (r.height / 2));
					}
					if (w instanceof JFrame) {
						((JFrame) w).setExtendedState(JFrame.NORMAL);
					}
					previousSize.put(w, o);
					lockedOnEvent = false;
				}
				break;
			case maximize:
				if (MouseEvent.MOUSE_RELEASED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					if (wat.equals(WindowActionType.maximize)) {
						Window w = (Window) e.getSource();
						Rectangle o = w.getBounds();
						Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
						if (o.x == 0 && o.y == 0 && o.width == size.width && o.height == size.height && previousSize.containsKey(w)) {
							// restore previous size
							o = previousSize.get(w);
							moveWindow(w, o.x, o.y);
							resizeWindow(w, o.width, o.height);
							if (w instanceof JFrame) {
								((JFrame) w).setExtendedState(JFrame.NORMAL);
							}
						} else {
							if (w instanceof JFrame) {
								((JFrame) w).setExtendedState(JFrame.MAXIMIZED_BOTH);
							} else {
								moveWindow(w, 0, 0);
								resizeWindow(w, size.width, size.height);
							}
						}
						previousSize.put(w, o);
					}
					lockedOnEvent = false;
				}
				break;
			case close:
				if (MouseEvent.MOUSE_RELEASED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					if (wat.equals(WindowActionType.close)) {
						Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new WindowEvent((Window) e.getSource(), WindowEvent.WINDOW_CLOSING));
					}
					lockedOnEvent = false;
				}
				break;
			case move:
				if (MouseEvent.MOUSE_RELEASED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					previousSize.remove(w);
					lockedOnEvent = false;
				}
				if (MouseEvent.MOUSE_DRAGGED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					moveWindow(w, e.getXOnScreen() - referenceMouseLocation.x, e.getYOnScreen() - referenceMouseLocation.y);
				}
				if (e.getSource() instanceof JFrame) {
					((JFrame) e.getSource()).setExtendedState(JFrame.NORMAL);
				}
				break;
			case resizeUniTopLeft:
				if (MouseEvent.MOUSE_RELEASED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					previousSize.remove(w);
					lockedOnEvent = false;
				}
				if (MouseEvent.MOUSE_DRAGGED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					Rectangle wb = w.getBounds();
					Dimension wMinSize = getMinimumWindowSize(w);
					Point stopPoint = new Point(wb.x + wb.width - wMinSize.width, wb.y + wb.height - wMinSize.height);
					int x = (int) Math.min(e.getXOnScreen(), stopPoint.getX());
					int y = (int) Math.min(e.getYOnScreen(), stopPoint.getY());
					int width = wb.x + wb.width - x;
					int height = wb.y + wb.height - y;
					resizeAndMoveWindow(w, x, y, width, height);
				}
				if (e.getSource() instanceof JFrame) {
					((JFrame) e.getSource()).setExtendedState(JFrame.NORMAL);
				}
				break;
			case resizeUniTopRight:
				if (MouseEvent.MOUSE_RELEASED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					previousSize.remove(w);
					lockedOnEvent = false;
				}
				if (MouseEvent.MOUSE_DRAGGED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					Rectangle wb = w.getBounds();
					Dimension wMinSize = getMinimumWindowSize(w);
					Point stopPoint = new Point(wb.x + wMinSize.width, wb.y + wb.height - wMinSize.height);
					int x = wb.x;
					int y = (int) Math.min(e.getYOnScreen(), stopPoint.getY());
					int width = e.getXOnScreen() - wb.x;
					int height = wb.y + wb.height - y;
					resizeAndMoveWindow(w, x, y, width, height);
				}
				if (e.getSource() instanceof JFrame) {
					((JFrame) e.getSource()).setExtendedState(JFrame.NORMAL);
				}
				break;
			case resizeUniBottomLeft:
				if (MouseEvent.MOUSE_RELEASED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					previousSize.remove(w);
					lockedOnEvent = false;
				}
				if (MouseEvent.MOUSE_DRAGGED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					Rectangle wb = w.getBounds();
					Dimension wMinSize = getMinimumWindowSize(w);
					Point stopPoint = new Point(wb.x + wb.width - wMinSize.width, wb.y + wMinSize.height);
					int x = (int) Math.min(e.getXOnScreen(), stopPoint.getX());
					int y = wb.y;
					int width = wb.x + wb.width - x;
					int height = e.getYOnScreen() - wb.y;
					resizeAndMoveWindow(w, x, y, width, height);
				}
				if (e.getSource() instanceof JFrame) {
					((JFrame) e.getSource()).setExtendedState(JFrame.NORMAL);
				}
				break;
			case resizeUniBottomRight:
				if (MouseEvent.MOUSE_RELEASED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					previousSize.remove(w);
					lockedOnEvent = false;
				}
				if (MouseEvent.MOUSE_DRAGGED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					resizeWindow(w, e.getXOnScreen() - w.getX(), e.getYOnScreen() - w.getY());
				}
				if (e.getSource() instanceof JFrame) {
					((JFrame) e.getSource()).setExtendedState(JFrame.NORMAL);
				}
				break;
			case resizeRight:
				if (MouseEvent.MOUSE_RELEASED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					previousSize.remove(w);
					lockedOnEvent = false;
				}
				if (MouseEvent.MOUSE_DRAGGED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					resizeWindow(w, e.getXOnScreen() - w.getX(), w.getSize().height);
				}
				if (e.getSource() instanceof JFrame) {
					((JFrame) e.getSource()).setExtendedState(JFrame.NORMAL);
				}
				break;
			case resizeLeft:
				if (MouseEvent.MOUSE_RELEASED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					previousSize.remove(w);
					lockedOnEvent = false;
				}
				if (MouseEvent.MOUSE_DRAGGED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					Rectangle wb = w.getBounds();
					Dimension wMinSize = getMinimumWindowSize(w);
					int x = (int) Math.min(e.getXOnScreen(), wb.x + wb.width - wMinSize.width);
					int width = wb.x + wb.width - x;
					resizeAndMoveWindow(w, x, wb.y, width, wb.height);
				}
				if (e.getSource() instanceof JFrame) {
					((JFrame) e.getSource()).setExtendedState(JFrame.NORMAL);
				}
				break;
			case resizeBottom:
				if (MouseEvent.MOUSE_RELEASED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					previousSize.remove(w);
					lockedOnEvent = false;
				}
				if (MouseEvent.MOUSE_DRAGGED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					resizeWindow(w, w.getSize().width, e.getYOnScreen() - w.getY());
				}
				if (e.getSource() instanceof JFrame) {
					((JFrame) e.getSource()).setExtendedState(JFrame.NORMAL);
				}
				break;
			case resizeTop:
				if (MouseEvent.MOUSE_RELEASED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					previousSize.remove(w);
					lockedOnEvent = false;
				}
				if (MouseEvent.MOUSE_DRAGGED == e.getID() && ((MouseEvent) e).getButton() == 1) {
					Window w = (Window) e.getSource();
					Rectangle wb = w.getBounds();
					Dimension wMinSize = getMinimumWindowSize(w);
					int y = (int) Math.min(e.getYOnScreen(), wb.y + wb.height - wMinSize.height);
					int height = wb.y + wb.height - y;
					resizeAndMoveWindow(w, wb.x, y, wb.width, height);
				}
				if (e.getSource() instanceof JFrame) {
					((JFrame) e.getSource()).setExtendedState(JFrame.NORMAL);
				}
				break;
			default:
				lockedOnEvent = false;
			}
		}
	}

	private Dimension getMinimumWindowSize(Window w) {
		return w.getMinimumSize();
	}

	public void moveWindow(Window w, int x, int y) {
		w.setLocation(x, y);
	}

	public void resizeWindow(final Window w, int width, int height) {
		if (w instanceof JFrame) {
			((JFrame) w).setExtendedState(JFrame.NORMAL);
		}
		final Dimension originalSize = w.getSize();
		final Dimension newSize = new Dimension(width, height);
		validateSize(w, newSize);
		if (!originalSize.equals(newSize)) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					w.setSize(newSize);
					WindowManager.getInstance().requestRepaintAfterMove(w, new Rectangle(w.getX(), w.getY(), originalSize.width, originalSize.height));
				}
			});
		}
	}

	public void resizeAndMoveWindow(final Window w, final int x, final int y, int width, int height) {
		if (w instanceof JFrame) {
			((JFrame) w).setExtendedState(JFrame.NORMAL);
		}
		final Dimension originalSize = w.getSize();
		final Dimension newSize = new Dimension(width, height);
		validateSize(w, newSize);
		if (!originalSize.equals(newSize)) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					w.setSize(newSize);
					w.setLocation(x, y);
					WindowManager.getInstance().requestRepaintAfterMove(w, new Rectangle(w.getX(), w.getY(), originalSize.width, originalSize.height));
				}
			});
		}
	}

	private void validateSize(Window w, Dimension newSize) {
		Dimension min = w.getMinimumSize();
		newSize.width = Math.max(min.width, newSize.width);
		newSize.width = Math.max(DEFAULT_MINIMUM_WINDOW_SIZE.width, newSize.width);
		newSize.height = Math.max(min.height, newSize.height);
		newSize.height = Math.max(DEFAULT_MINIMUM_WINDOW_SIZE.height, newSize.height);
	}

	public boolean isEventHandlingLocked() {
		if (lockedOnEvent && (window == null || !window.isEnabled() || !window.isShowing())) {
			lockedOnEvent = false;
		}
		return lockedOnEvent;
	}

	public Window getLockedToWindow() {
		if (isEventHandlingLocked()) {
			return window;
		}
		return null;
	}

}

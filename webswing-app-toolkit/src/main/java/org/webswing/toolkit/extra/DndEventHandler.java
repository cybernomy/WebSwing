package org.webswing.toolkit.extra;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Window;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.webswing.toolkit.WebDragSourceContextPeer;
import org.webswing.toolkit.WebDropTargetContextPeer;
import org.webswing.toolkit.util.Logger;
import org.webswing.toolkit.util.Util;

@SuppressWarnings("restriction")
public class DndEventHandler {

	private WebDropTargetContextPeer dropTarget;
	private long[] formats;
	private WebDragSourceContextPeer dragSource;
	private int sourceActions;
	private int lastDropTargetAction = 0;
	private boolean dropped;
	private boolean finished = true;
	private static Cursor cursor = Cursor.getDefaultCursor();

	public void processMouseEvent(Window w, AWTEvent e) {
		if (e instanceof MouseEvent) {
			MouseEvent me = (MouseEvent) e;
			int modifiers = (me.getModifiersEx() & (MouseEvent.CTRL_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK)) | MouseEvent.BUTTON1_DOWN_MASK;
			int currentDropAction = WebDragSourceContextPeer.convertModifiersToDropAction(modifiers, sourceActions);
			lastDropTargetAction = dropTarget.handleEnterMessage(w, me.getX(), me.getY(), currentDropAction, sourceActions, formats, 123123123);
			if (e.getID() == MouseEvent.MOUSE_RELEASED) {
				//dragSource.dragMouseMoved(currentDropAction, modifiers, me.getXOnScreen(), me.getYOnScreen());
				//dragSource.dragEnter(currentDropAction, modifiers, me.getXOnScreen(), me.getYOnScreen());
				//dragSource.dragMotion(currentDropAction, modifiers, me.getXOnScreen(), me.getYOnScreen());
				//lastDropTargetAction = dropTarget.handleMotionMessage(w, me.getX(), me.getY(), currentDropAction, sourceActions, formats, 123123123);
				dragEnd(w, e, lastDropTargetAction != 0, lastDropTargetAction);
			} else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
				dropTarget.handleMotionMessage(w, me.getX(), me.getY(), currentDropAction, sourceActions, formats, 123123123);
				updateCursor();
				Util.getWebToolkit().getPaintDispatcher().notifyCursorUpdate(cursor);
			}
		} else if (e instanceof KeyEvent) {
			if (e.getID() == KeyEvent.KEY_PRESSED && ((KeyEvent) e).getKeyCode() == KeyEvent.VK_ESCAPE) {
				dragEnd(w, e, false, 0);
			}
		}
	}

	private void updateCursor() {
		try {
			switch (lastDropTargetAction) {
			case TransferHandler.NONE:
				cursor = Cursor.getSystemCustomCursor("Invalid.32x32");
				break;
			case TransferHandler.COPY:
				cursor = Cursor.getSystemCustomCursor("CopyDrop.32x32");
				break;
			case TransferHandler.LINK:
				cursor = Cursor.getSystemCustomCursor("LinkDrop.32x32");
				break;
			case TransferHandler.MOVE:
			case TransferHandler.COPY_OR_MOVE:
			default:
				cursor = Cursor.getSystemCustomCursor("MoveDrop.32x32");
				break;
			}
		} catch (Exception e) {
			Logger.debug("Failed to load DnD cursor", e);
		}
	}

	public void dragStart(WebDragSourceContextPeer dragSource, Transferable transferable, int actions, long[] formats) {
		this.dragSource = dragSource;
		this.formats = formats;
		this.sourceActions = actions;
		dropTarget = WebDropTargetContextPeer.getWebDropTargetContextPeer();
		this.dropped = false;
		this.finished = false;
	}

	private void dragEnd(Window w, AWTEvent e, boolean supported, int dropAction) {
		if (!finished) {
			if (e instanceof MouseEvent) {
				MouseEvent me = ((MouseEvent) e);
				if (w != null) {
					if (supported && !dropped) {
						dropTarget.setDragSource(dragSource);
						dropTarget.handleDropMessage(w, me.getX(), me.getY(), lastDropTargetAction, this.sourceActions, formats, 123123123);
						dropped = true;
					} else {
						dropTarget.handleExitMessage(w, 123123123);
						dragSource.dragFinished(false, dropAction, me.getX(), me.getY());
					}
				}
			} else {
				dropTarget.handleExitMessage(w, 123123123);
				dragSource.dragFinished(false, dropAction, 0, 0);
			}
			finished = true;
		}
		sourceActions = 0;
		lastDropTargetAction = TransferHandler.NONE;
		cursor = Cursor.getDefaultCursor();
	}

	public static Cursor getCurrentDropTargetCursorName() {
		return cursor;
	}

	public boolean isDndInProgress() {
		return !finished;
	}

}

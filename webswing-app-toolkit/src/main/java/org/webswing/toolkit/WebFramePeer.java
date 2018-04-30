package org.webswing.toolkit;

import org.webswing.toolkit.extra.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.peer.FramePeer;

public class WebFramePeer extends WebWindowPeer implements FramePeer {

	private int state;

	public WebFramePeer(Frame t) {
		super(t);
		state = t.getExtendedState();
	}

	public void setMenuBar(MenuBar paramMenuBar) {
	}

	public void setState(int paramInt) {
		state = paramInt;
		if (state == Frame.MAXIMIZED_BOTH) {
			final JFrame f = (JFrame) target;
			f.setLocation(0, 0);
			final Dimension originalSize = f.getSize();
			final Dimension newSize = Toolkit.getDefaultToolkit().getScreenSize();
			if (!originalSize.equals(newSize)) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						f.setSize(newSize);
						WindowManager.getInstance().requestRepaintAfterMove(f, new Rectangle(f.getX(), f.getY(), originalSize.width, originalSize.height));
					}
				});
			}
		}
	}

	public int getState() {
		return state;
	}

	public void setMaximizedBounds(Rectangle paramRectangle) {
	}

	public void setBoundsPrivate(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
	}

	public Rectangle getBoundsPrivate() {
		return null;
	}

	public void emulateActivation(boolean b) {

	}

	@Override
	public void show() {
		super.show();
		setState(state);//maximize if necessary
	}

}

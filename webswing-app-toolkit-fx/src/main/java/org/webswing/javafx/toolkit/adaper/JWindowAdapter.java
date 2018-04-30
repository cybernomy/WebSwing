package org.webswing.javafx.toolkit.adaper;

import javax.swing.JDialog;
import javax.swing.JWindow;
import java.awt.Window;

public class JWindowAdapter extends JDialog implements WindowAdapter {
	public JWindowAdapter(Window parent) {
		super(parent);
		setUndecorated(true);
		setFocusableWindowState(false);
		setType(java.awt.Window.Type.POPUP);
	}

	@Override
	public Window getThis() {
		return this;
	}

}

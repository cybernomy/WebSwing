package org.webswing.javafx.toolkit.adaper;

import javax.swing.JDialog;
import java.awt.Window;

public class JDialogAdapter extends JDialog implements WindowAdapter{
	public JDialogAdapter(Window parent, boolean titled) {
		super(parent);
		if (!titled) {
			setUndecorated(true);
		}
	}

	@Override
	public Window getThis() {
		return this;
	}
}

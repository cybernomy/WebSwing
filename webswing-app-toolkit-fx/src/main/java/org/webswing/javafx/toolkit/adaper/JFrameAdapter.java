package org.webswing.javafx.toolkit.adaper;

import javax.swing.JFrame;
import java.awt.Window;

public class JFrameAdapter extends JFrame implements WindowAdapter {
	public JFrameAdapter(boolean titled) {
		super();
		if (!titled) {
			setUndecorated(true);
		}
	}

	@Override
	public Window getThis() {
		return this;
	}

	@Override
	public void setModal(boolean b) {
		//do nothing
	}
}

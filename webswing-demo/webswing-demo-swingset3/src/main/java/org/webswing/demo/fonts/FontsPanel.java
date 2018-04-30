package org.webswing.demo.fonts;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class FontsPanel extends JPanel {

	public FontsPanel(String initialText,Font f) {
		JLabel theLabel = new JLabel(initialText) {
			public Dimension getPreferredSize() {
				return new Dimension(600, 300);
			}

			public Dimension getMinimumSize() {
				return new Dimension(600, 300);
			}

			public Dimension getMaximumSize() {
				return new Dimension(600, 300);
			}
		};
		theLabel.setFont(f);
		theLabel.setVerticalAlignment(SwingConstants.CENTER);
        theLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(theLabel);
	}

}

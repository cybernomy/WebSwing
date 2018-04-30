package org.webswing.demo.applet;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sun.swingset3.DemoProperties;

@DemoProperties(value = "Applet info", category = "Webswing", description = "Demonstrates applets.", sourceFiles = { "org/webswing/demo/applet/AppletDemo.java", "org/webswing/demo/applet/SwingSet3Applet.java" })
public class AppletDemo extends JPanel {
	private static final long serialVersionUID = 8121827449962804404L;
	Applet a = SwingSet3Applet.applet;

	public AppletDemo() {
		if (a != null) {
			setLayout(new BorderLayout());
			JPanel infopanel = new JPanel(new GridLayout(7, 1));
			infopanel.add(new JLabel("Name:" + a.getName()));
			infopanel.add(new JLabel("Document base:" + a.getDocumentBase().toString()));
			infopanel.add(new JLabel("Code base:" + a.getCodeBase().toString()));
			infopanel.add(new JLabel("Applet info:" + a.getAppletInfo()));
			infopanel.add(new JLabel("Code base:" + a.getAppletInfo()));
			infopanel.add(new JLabel("param1:" + a.getParameter("param1")));
			infopanel.add(new JLabel("param2:" + a.getParameter("param2")));
			infopanel.setBorder(BorderFactory.createTitledBorder("Applet Information"));
			add(BorderLayout.CENTER, infopanel);
		} else {
			add(new JLabel("This Demo is only available in applet mode."));
		}
	}
}

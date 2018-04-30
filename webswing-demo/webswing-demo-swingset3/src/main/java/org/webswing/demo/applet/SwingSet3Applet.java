package org.webswing.demo.applet;

import java.awt.BorderLayout;
import java.lang.reflect.Method;

import javax.swing.JApplet;
import javax.swing.JPanel;

import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;

import com.sun.swingset3.SwingSet3;

public class SwingSet3Applet extends JApplet {

	protected static JApplet applet = null;
	private SwingSet3Wrapper swingset3 = new SwingSet3Wrapper();

	public SwingSet3Applet() {
		applet = this;
		SwingSet3Wrapper ssw = new SwingSet3Wrapper();
		ssw.startup();
		add(ssw.panel);
	}

	public static class SwingSet3Wrapper extends SwingSet3 {
		private static JPanel panel = new JPanel();

		protected void startup() {
			ApplicationContext appCtx = getContext();
			appCtx.setApplicationClass(SwingSet3Wrapper.class);
			try {
				Method setApplication = ApplicationContext.class.getDeclaredMethod("setApplication", Application.class);
				setApplication.setAccessible(true);
				setApplication.invoke(appCtx, this);
			} catch (Exception e) {
				e.printStackTrace();
			}

			initialize(new String[0]);
			configureDefaults();
			panel.setLayout(new BorderLayout());
			panel.add(BorderLayout.CENTER, createMainPanel());
			applyDefaults();
		};
	};

}

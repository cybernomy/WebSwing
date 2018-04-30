package org.webswing.demo.jslink;

import com.sun.swingset3.DemoProperties;
import com.sun.swingset3.utilities.RoundedBorder;
import com.sun.swingset3.utilities.RoundedPanel;
import com.sun.swingset3.utilities.Utilities;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.webswing.toolkit.api.WebswingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@DemoProperties(value = "JsLink", category = "Webswing", description = "Demonstrates javascritp interface.", sourceFiles = { "org/webswing/demo/jslink/JsLinkDemo.java", "org/webswing/demo/jslink/resources/hello.js", "org/webswing/demo/jslink/resources/echo.js", "org/webswing/demo/jslink/resources/callback.js",
		"org/webswing/demo/jslink/resources/javaReference.js" })
public class JsLinkDemo extends JPanel {
	//REPLACED WITH Webswing API interface
	// static UrlChanageService urlService = new UrlChanageService();

	public static class DummyService {
		private String dummyString = "dummy";

		public String echoString(String echo) {
			return "Echo at " + new Date() + ": " + dummyString + " | " + echo;
		}

		public void setDummyString(String dummyString) {
			this.dummyString = dummyString;
		}

		public void displayMessage(String msg) {
			displayResult(msg);
		}

		public boolean isThisMe(DummyService service) {
			return service == this;
		}

		public void methodWithCallback(JSObject callback) {
			JOptionPane.showMessageDialog(null, "swing processing complete, invoking js callback... ", "Done!", JOptionPane.INFORMATION_MESSAGE);
			callback.call("call", new Object[] { null, "some swing processing result" });
		}
	}

	public static class UrlChanageService {

		public UrlChanageService() {
			if (WebswingUtil.isWebswing()) {
				JSObject global = JSObject.getWindow(null);
				global.setMember("UrlChanageService", this);//expose this object to javascript (window.UrlChanageService)
				global.eval(loadContent("resources/urlChangeListener.js"));//register javascript listener to url change
			}
		}

		//invoked from swing application to force url change in browser
		public void changeUrl(String newPath) {
			JSObject global = JSObject.getWindow(null);
			global.eval("window.location = '#" + newPath + "'");
		}

		//invoked by the javascript event when url changes
		public void onUrlChanged(String newUrl) {
			displayResult("UrlChanged to: " + newUrl);
		}
	}

	private static Map<String, String> snippets = new HashMap<String, String>();
	private static DummyService service = new DummyService();

	static {
		snippets.put("Hello world", loadContent("resources/hello.js"));
		snippets.put("Echo", loadContent("resources/echo.js"));
		snippets.put("Callback", loadContent("resources/callback.js"));
		snippets.put("Java object reference", loadContent("resources/javaReference.js"));
	}

	public JsLinkDemo() {

		setLayout(new BorderLayout());
		if (System.getProperty("webswing.clientId") != null) {

			//urlService.changeUrl("HelloWorldUrl");

            /* EXPOSE JAVA SERVICE TO JAVASCRIPT GLOBAL OBJECT*/
			JSObject global = JSObject.getWindow(null);
			global.setMember("dummyService", service);
			/* *********************************************  */

			JComboBox snippetSelector = new JComboBox(snippets.keySet().toArray(new String[snippets.size()]));
			add(BorderLayout.PAGE_START, snippetSelector);
			final JTextArea snipetEditor = new JTextArea();
			add(BorderLayout.CENTER, snipetEditor);
			JButton execute = new JButton("Evaluate JavaScript");
			add(BorderLayout.PAGE_END, execute);

			snippetSelector.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					String selected = (String) e.getItem();
					String js = snippets.get(selected);
					snipetEditor.setText(js);
				}
			});
			snippetSelector.setSelectedIndex(1);

			execute.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						JSObject global = JSObject.getWindow(null);
						Object res = global.eval(snipetEditor.getText());
						if (res != null) {
							displayResult(res);
						}
					} catch (JSException e1) {
						displayErrorMessage("Evaluation of javascript failed.", e1);
					}
				}
			});

		} else {
			add(BorderLayout.NORTH, new JLabel("This area only works in webswing session."));
		}

	}

	protected static void displayResult(Object res) {
		JPanel messagePanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Result type: " + res.getClass());
		messagePanel.add(label);
		if (res instanceof String) {
			RoundedPanel panel = new RoundedPanel(new BorderLayout());
			panel.setBorder(new RoundedBorder());

			// remind(aim): provide way to allow user to see exception only if desired
			JTextArea exceptionText = new JTextArea();
			exceptionText.setText("Value (toString):" + res.toString());
			exceptionText.setBorder(new RoundedBorder());
			exceptionText.setOpaque(false);
			exceptionText.setBackground(Utilities.deriveColorHSB(UIManager.getColor("Panel.background"), 0, 0, -.2f));
			JScrollPane scrollpane = new JScrollPane(exceptionText);
			scrollpane.setPreferredSize(new Dimension(600, 240));
			panel.add(scrollpane);
			messagePanel.add(panel, BorderLayout.SOUTH);
		}
		JOptionPane.showMessageDialog(null, messagePanel, "Result", JOptionPane.INFORMATION_MESSAGE);

	}

	protected void displayErrorMessage(String message, Exception ex) {
		JPanel messagePanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel(message);
		messagePanel.add(label);
		if (ex != null) {
			RoundedPanel panel = new RoundedPanel(new BorderLayout());
			panel.setBorder(new RoundedBorder());

			// remind(aim): provide way to allow user to see exception only if desired
			StringWriter writer = new StringWriter();
			ex.printStackTrace(new PrintWriter(writer));
			JTextArea exceptionText = new JTextArea();
			exceptionText.setText("Cause of error:\n" + writer.getBuffer().toString());
			exceptionText.setBorder(new RoundedBorder());
			exceptionText.setOpaque(false);
			exceptionText.setBackground(Utilities.deriveColorHSB(UIManager.getColor("Panel.background"), 0, 0, -.2f));
			JScrollPane scrollpane = new JScrollPane(exceptionText);
			scrollpane.setPreferredSize(new Dimension(600, 240));
			panel.add(scrollpane);
			messagePanel.add(panel, BorderLayout.SOUTH);
		}
		JOptionPane.showMessageDialog(null, messagePanel, "Error", JOptionPane.ERROR_MESSAGE);

	}

	private static String loadContent(String string) {
		try {
			InputStream in = JsLinkDemo.class.getResourceAsStream(string);
			InputStreamReader is = new InputStreamReader(in);
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(is);
			String read = br.readLine();

			while (read != null) {
				//System.out.println(read);
				sb.append(read + "\n");
				read = br.readLine();
			}

			return sb.toString();
		} catch (IOException e) {
			return e.getMessage();
		}
	}
}

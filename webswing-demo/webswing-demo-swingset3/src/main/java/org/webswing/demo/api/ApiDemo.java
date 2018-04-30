package org.webswing.demo.api;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.webswing.toolkit.api.WebswingApiException;
import org.webswing.toolkit.api.WebswingUtil;
import org.webswing.toolkit.api.lifecycle.WebswingShutdownListener;
import org.webswing.toolkit.api.messaging.WebswingMessage;
import org.webswing.toolkit.api.messaging.WebswingMessageListener;
import org.webswing.toolkit.api.messaging.WebswingTopic;
import org.webswing.toolkit.api.security.UserEvent;
import org.webswing.toolkit.api.security.WebswingUser;
import org.webswing.toolkit.api.security.WebswingUserListener;

import com.sun.swingset3.DemoProperties;
import com.sun.swingset3.utilities.RoundedBorder;
import com.sun.swingset3.utilities.RoundedPanel;
import com.sun.swingset3.utilities.Utilities;

import static sun.security.pkcs.PKCS8Key.version;

@DemoProperties(value = "Webswing API", category = "Webswing", description = "Demonstrates Websiwng API.", sourceFiles = { "org/webswing/demo/api/ApiDemo.java" })
public class ApiDemo extends JPanel {

	private static final long serialVersionUID = 7886167850035120240L;
	JTextField usertextBox;

	public ApiDemo() {
		if (WebswingUtil.isWebswing()) {
			setLayout(new BorderLayout());
			JPanel infopanel = new JPanel(new GridLayout(7, 1));
			infopanel.add(new JLabel("isWebswing:" + WebswingUtil.isWebswing()));
			JButton getUserButton = new JButton("getPrimaryUser()");
			getUserButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					WebswingUser user = WebswingUtil.getWebswingApi().getPrimaryUser();
					if (user == null) {
						displayMessage("No user is connected");
					} else {
						displayMessage(user);
					}
				}
			});
			infopanel.add(getUserButton);
			JButton hasRoleButton = new JButton("primaryUserHasRole(role)");
			hasRoleButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					String role = JOptionPane.showInputDialog("Role:");
					Boolean hasRole;
					try {
						hasRole = WebswingUtil.getWebswingApi().primaryUserHasRole(role);
						if (hasRole == null) {
							displayMessage("No user is connected");
						} else {
							displayMessage("Result for role '" + role + "':" + hasRole);
						}
					} catch (WebswingApiException e1) {
						displayErrorMessage("failed to resolve role:", e1);
					}

				}
			});
			infopanel.add(hasRoleButton);
			JButton isPermittedButton = new JButton("primaryUserIsPermitted(permission)");
			isPermittedButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					String permission = JOptionPane.showInputDialog("Permission:");
					Boolean permitted;
					try {
						permitted = WebswingUtil.getWebswingApi().primaryUserIsPermitted(permission);
						if (permitted == null) {
							displayMessage("No user is connected");
						} else {
							displayMessage("Result for permission '" + permission + "':" + permitted);
						}
					} catch (WebswingApiException e1) {
						displayErrorMessage("failed to resolve permission:", e1);
					}

				}
			});
			infopanel.add(isPermittedButton);
			JButton exitButton = new JButton("notifyShutdown(3000ms)");
			exitButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					WebswingUtil.getWebswingApi().notifyShutdown(3000);
				}
			});
			infopanel.add(exitButton);
			JButton versionButton = new JButton("getWebswingVersion()");
			versionButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					String version = WebswingUtil.getWebswingApi().getWebswingVersion();
					if (version == null) {
						displayMessage("No version information available");
					} else {
						displayMessage(version);
					}
				}
			});
			infopanel.add(versionButton);
			JButton messageApiBtn = new JButton("publish Message");
			messageApiBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					WebswingTopic<TestMsg> topic = WebswingUtil.getWebswingMessagingApi().getSharedTopic(TestMsg.class);
					try {
						topic.publish(new TestMsg("Msg from " + System.getProperty("webswing.clientId")));
					} catch (IOException e1) {
						displayErrorMessage("Failed to publish message to all sessions", e1);
					}

				}
			});
			infopanel.add(messageApiBtn);
			infopanel.setBorder(BorderFactory.createTitledBorder("Webswing API Demonstration."));
			add(BorderLayout.EAST, infopanel);
			final JTextArea text = new JTextArea(50, 20);
			WebswingUtil.getWebswingApi().addUserConnectionListener(new WebswingUserListener() {

				@Override
				public void onPrimaryUserDisconnected(UserEvent evt) {
					text.append("onUserDisconnected(" + evt.getUser() + ")\n");

				}

				@Override
				public void onPrimaryUserConnected(UserEvent evt) {
					text.append("onUserConnected(" + evt.getUser() + ")\n");
				}

				@Override
				public void onMirrorViewDisconnected(UserEvent evt) {
					text.append("onMirrorViewDisconnected(" + evt.getUser() + ")\n");

				}

				@Override
				public void onMirrorViewConnected(UserEvent evt) {
					text.append("onMirrorViewConnected(" + evt.getUser() + ")\n");

				}
			});

			WebswingTopic<TestMsg> topic = WebswingUtil.getWebswingMessagingApi().getSharedTopic(TestMsg.class);
			topic.subscribe(new WebswingMessageListener<TestMsg>() {
				@Override
				public void onMessage(WebswingMessage<TestMsg> message) {
					text.append(System.getProperty("webswing.clientId") + " received: " + message.getMessage().getMessage());
				}
			});

			WebswingUtil.getWebswingApi().addShutdownListener(new WebswingShutdownListener() {

				@Override
				public void onShutdown() {
					text.append("onShutdown():faking my death.\n");
					System.out.println("onShutdown():faking my death.");
				}
			});

			JScrollPane pane = new JScrollPane(text);
			add(BorderLayout.WEST, pane);
		} else {
			add(new JLabel("Webswing API can only be used when swing is running inside Webswing."));
		}
	}

	protected void displayMessage(Object... message) {
		JPanel messagePanel = new JPanel(new BorderLayout());
		RoundedPanel panel = new RoundedPanel(new BorderLayout());
		panel.setBorder(new RoundedBorder());

		StringWriter writer = new StringWriter();
		for (Object m : message) {
			if (m instanceof String) {
				writer.write((String) m);
			} else {
				writer.write(ReflectionToStringBuilder.toString(m, ToStringStyle.MULTI_LINE_STYLE));
			}
			writer.write("\n\n");
		}
		JTextArea exceptionText = new JTextArea();
		exceptionText.setText(writer.getBuffer().toString());
		exceptionText.setBorder(new RoundedBorder());
		exceptionText.setOpaque(false);
		exceptionText.setBackground(Utilities.deriveColorHSB(UIManager.getColor("Panel.background"), 0, 0, -.2f));
		JScrollPane scrollpane = new JScrollPane(exceptionText);
		scrollpane.setPreferredSize(new Dimension(600, 240));
		panel.add(scrollpane);
		messagePanel.add(panel, BorderLayout.SOUTH);
		JOptionPane.showMessageDialog(null, messagePanel, "Info", JOptionPane.INFORMATION_MESSAGE);

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
}

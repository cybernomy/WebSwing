package org.webswing.demo.files;

import com.sun.swingset3.DemoProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

@DemoProperties(value = "Files", category = "Webswing", description = "Demonstrates file handling.", sourceFiles = { "org/webswing/demo/files/FilesDemo.java" })
public class FilesDemo extends JPanel {

	protected JComboBox multiSelection = new JComboBox(new String[] { "True", "False" });
	protected JComboBox mode = new JComboBox(new String[] { "Files", "Directories", "Both" });

	public FilesDemo() {
		super(new BorderLayout());

		JPanel body = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JScrollPane pane = new JScrollPane(body);
		add("Center", pane);

		JPanel msPanel = new JPanel(new GridLayout(1, 2, 20, 0));
		msPanel.add(new JLabel("MultiSelection:"));
		msPanel.add(multiSelection);

		JPanel smPanel = new JPanel(new GridLayout(1, 2, 20, 0));
		smPanel.add(new JLabel("Selection mode:"));
		smPanel.add(mode);

		JPanel panel = new JPanel(new GridLayout(4, 2, 20, 0));
		panel.add(msPanel);
		panel.add(smPanel);
		panel.add(new JPanel());
		JButton filesBtn = new JButton("Open File Chooser");
		filesBtn.addActionListener(new OpenFileDialog(this));
		panel.add(filesBtn);
		//save dialog
		panel.add(new JPanel());
		JButton saveBtn = new JButton("Open Save Dialog");
		saveBtn.addActionListener(new SaveFileDialog(this));
		panel.add(saveBtn);
		//native file dialog
		panel.add(new JPanel());
		JButton fileDialog = new JButton("Native File Dialog");
		fileDialog.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Window w = SwingUtilities.getWindowAncestor(getParent());
				FileDialog fd = new FileDialog((Frame) w, "Choose a file", FileDialog.SAVE);
				fd.setFile("test.xml");
				fd.setVisible(true);
				String filename = fd.getDirectory()+fd.getFile();
				if (filename == null)
					System.out.println("You cancelled the choice");
				else {
					try {
						PrintWriter writer = new PrintWriter(new File(filename));
						writer.println("<hello><world/></hello>");
						writer.close();
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
				}

			}
		});
		panel.add(fileDialog);

		add("South", panel);
	}

	public static void main(String[] args) {
		final JFrame f = new JFrame("Print UI Example");
		f.getContentPane().add(new FilesDemo());
		f.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		f.pack();
		f.setVisible(true);
	}
}

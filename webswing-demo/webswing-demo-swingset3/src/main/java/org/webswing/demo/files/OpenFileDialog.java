package org.webswing.demo.files;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

public class OpenFileDialog implements ActionListener {

	private FilesDemo demo;

	public OpenFileDialog(FilesDemo demo) {
		this.demo = demo;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(Boolean.parseBoolean((String) demo.multiSelection.getSelectedItem()));
		String mode = (String) demo.mode.getSelectedItem();
		if ("Files".equals(mode)) {
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

		} else if ("Directories".equals(mode)) {
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		} else {
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		}

		int returnVal = fc.showOpenDialog((Component) e.getSource());

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
				Desktop.getDesktop().open(file);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}

}

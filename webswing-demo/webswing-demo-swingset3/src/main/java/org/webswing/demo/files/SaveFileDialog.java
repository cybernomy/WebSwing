package org.webswing.demo.files;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;

import javax.swing.JFileChooser;

public class SaveFileDialog implements ActionListener {

	private FilesDemo demo;

	public SaveFileDialog(FilesDemo demo) {
		this.demo = demo;
	}	

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		fc.setSelectedFile(new File("test.txt"));
		int returnVal = fc.showSaveDialog((Component) e.getSource());

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
				file.createNewFile();
				PrintWriter writer = new PrintWriter(file);
				Thread.sleep(3000);
				writer.println("This file should have 10 lines:");
				writer.flush();
				for (int i = 0; i < 10; i++) {
					Thread.sleep(i*100);
					writer.print("line "+(i+1)+":");
					for (int j = 0; j < 1024; j++) {
						writer.print("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
						writer.print("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
						writer.print("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
						writer.print("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
						writer.print("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
						writer.print("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
						writer.print("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
						writer.print("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
						writer.print("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
					}
					writer.println("");
					writer.flush();
				}
				writer.close();
				System.out.println("sending the file to Browser:");
				Desktop.getDesktop().open(file);
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}
	
}

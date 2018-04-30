package org.webswing.demo.dnd;

/*
 * DragPictureDemo.java requires the following files:
 *     Picture.java
 *     DTPicture.java
 *     PictureTransferHandler.java
 *     images/Maya.jpg
 *     images/Anya.jpg
 *     images/Laine.jpg
 *     images/Cosmo.jpg
 *     images/Adele.jpg
 *     images/Alexi.jpg
 */
import java.io.*;
import java.net.MalformedURLException;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sun.swingset3.DemoProperties;

@DemoProperties(value = "Picture DnD", category = "Webswing", description = "Demonstrates Drag and drop functionality", sourceFiles = { "org/webswing/demo/dnd/DragPictureDemo.java" })
public class DragPictureDemo extends JPanel {

	DTPicture pic1, pic2, pic3, pic4, pic5, pic6, pic7, pic8, pic9, pic10, pic11;
	JLabel pic12;
	static String mayaString = "1";
	static String anyaString = "2";
	static String laineString = "3";
	static String cosmoString = "4";
	static String adeleString = "5";
	static String alexiString = "6";
	PictureTransferHandler picHandler;
	private TransferHandler picExportHandler;

	public DragPictureDemo() {
		super(new BorderLayout());
		picHandler = new PictureTransferHandler();
		picExportHandler = new PictureExportHandler();
		JPanel mugshots = new JPanel(new GridLayout(4, 3));
		pic1 = new DTPicture(createImageIcon("images/" + mayaString + ".jpg", mayaString).getImage());
		pic1.setTransferHandler(picHandler);
		mugshots.add(pic1);
		pic2 = new DTPicture(createImageIcon("images/" + anyaString + ".jpg", anyaString).getImage());
		pic2.setTransferHandler(picHandler);
		mugshots.add(pic2);
		pic3 = new DTPicture(createImageIcon("images/" + laineString + ".jpg", laineString).getImage());
		pic3.setTransferHandler(picHandler);
		mugshots.add(pic3);
		pic4 = new DTPicture(createImageIcon("images/" + cosmoString + ".jpg", cosmoString).getImage());
		pic4.setTransferHandler(picHandler);
		mugshots.add(pic4);
		pic5 = new DTPicture(createImageIcon("images/" + adeleString + ".jpg", adeleString).getImage());
		pic5.setTransferHandler(picHandler);
		mugshots.add(pic5);
		pic6 = new DTPicture(createImageIcon("images/" + alexiString + ".jpg", alexiString).getImage());
		pic6.setTransferHandler(picHandler);
		mugshots.add(pic6);

		//These six components with no pictures provide handy
		//drop targets.
		pic7 = new DTPicture(null);
		pic7.setTransferHandler(picHandler);
		mugshots.add(pic7);
		pic8 = new DTPicture(null);
		pic8.setTransferHandler(picHandler);
		mugshots.add(pic8);
		pic9 = new DTPicture(null);
		pic9.setTransferHandler(picHandler);
		mugshots.add(pic9);
		pic10 = new DTPicture(null);
		pic10.setTransferHandler(picHandler);
		mugshots.add(pic10);
		pic11 = new DTPicture(null);
		pic11.setTransferHandler(picHandler);
		mugshots.add(pic11);
		pic12 = new JLabel("Import/Export image...");
		pic12.setBorder(BorderFactory.createRaisedBevelBorder());
		pic12.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser fileChooser= new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.setFileFilter(new FileNameExtensionFilter("My Images","png","jpg"));
				int result = fileChooser.showOpenDialog(null);
				if(result == JFileChooser.APPROVE_OPTION){
					File[] files = fileChooser.getSelectedFiles();
					for (int i = 0; i < files.length; i++) {
						File file = files[i];
						try {
							ImageIcon importImageIcon = importImageIcon(file, "import "+i);
							if(i<11){
								DTPicture pic=(DTPicture) DragPictureDemo.class.getDeclaredField("pic"+(i+1)).get(DragPictureDemo.this);
								pic.setImage(importImageIcon.getImage());
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}else{
					JOptionPane.showConfirmDialog(null, "Import cancelled.");
				}
				
			}
		});
		pic12.setTransferHandler(picExportHandler);
		mugshots.add(pic12);

		setPreferredSize(new Dimension(450, 630));
		add(mugshots, BorderLayout.CENTER);
		JButton xorModeTestButton=new JButton("XorMode drag");
		xorModeTestButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new CubicCurveMouse();
			}
		});
		add(xorModeTestButton, BorderLayout.NORTH);
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path, String description) {
		java.net.URL imageURL = DragPictureDemo.class.getResource("resources/" + path);
		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		} else {
			return new ImageIcon(imageURL, description);
		}
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. 
	 * @throws MalformedURLException */
	protected static ImageIcon importImageIcon(File path, String description) throws MalformedURLException {
		java.net.URL imageURL = path.toURI().toURL();
		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		} else {
			return new ImageIcon(imageURL, description);
		}
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("DragPictureDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the menu bar and content pane.
		DragPictureDemo demo = new DragPictureDemo();
		demo.setOpaque(true); //content panes must be opaque
		frame.setContentPane(demo);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
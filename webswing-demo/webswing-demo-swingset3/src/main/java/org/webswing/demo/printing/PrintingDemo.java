package org.webswing.demo.printing;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.webswing.demo.dnd.DragPictureDemo;

import com.sun.swingset3.DemoProperties;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;
import net.sf.jasperreports.view.JasperViewer;

@DemoProperties(value = "Printing", category = "Webswing", description = "Demonstrates printing abilities of websinwg", sourceFiles = { "org/webswing/demo/printing/PaginationExample.java", "org/webswing/demo/printing/PrintableExample.java", "org/webswing/demo/printing/PrintJobExample.java" })
public class PrintingDemo extends JPanel {
	private static final long serialVersionUID = 8550928872207603286L;
	protected JComboBox orientation = new JComboBox(new String[] { "Portrait", "Landscape" });

	public PrintingDemo() {
		super(new BorderLayout());
		JTextArea text = new JTextArea(50, 20);
		for (int i = 1; i <= 50; i++) {
			text.append("Line " + i + "\n");
		}
		JScrollPane pane = new JScrollPane(text);
		add("Center", pane);
		JButton printButton = new JButton("Simple Print 1");
		printButton.setToolTipText("using Printable interface");
		printButton.addActionListener(new PrintableExample(this));
		JButton print2Button = new JButton("Simple Print 2");
		print2Button.setToolTipText("using the Toolkit.getPrintJob() call");
		print2Button.addActionListener(new PrintJobExample(this));
		JButton printButton2 = new JButton("Simple Print Pages");
		printButton2.addActionListener(new PaginationExample(this));
		JPanel panel = new JPanel(new GridLayout(2, 5, 15, 0));
		panel.add(orientation);
		panel.add(printButton);
		panel.add(print2Button);
		panel.add(printButton2);
		
		JButton jasperButton = new JButton("Jasper Reports viewer");
		jasperButton.setToolTipText("open jasper report viewer window");
		jasperButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JasperViewer.viewReport( PrintingDemo.class.getResourceAsStream("resources/FirstJasper.jrprint"), false,false);
				} catch (JRException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		panel.add(jasperButton);
		add("South", panel);
	}

	public static void main(String[] args) {
		final JFrame f = new JFrame("Print UI Example");
		f.getContentPane().add(new PrintingDemo());
		f.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		f.pack();
		f.setVisible(true);
	}
}

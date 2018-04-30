package org.webswing.demo.fonts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

import com.sun.swingset3.DemoProperties;

@DemoProperties(value = "Fonts", category = "Webswing", description = "Demonstrates displaying of custom fonts")
public class FontsDemo extends JPanel {
	private static final boolean INITIAL_STATE = false;
	private static final String MODE_NATIVE = "Mode: native rendering";
	private static final String MODE_IMG = "Mode: BufferedImage";

	private static final long serialVersionUID = 6309330628696876261L;
	private JPanel content;
	private Font comboFont = UIManager.getFont("ComboBox.font");
	private JComboBox familyCombo;
	private String html;

	public FontsDemo() {
		super(new BorderLayout());

		try {
			html = convertStreamToString(FontsDemo.class.getResourceAsStream("resources/label.html"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		JPanel control = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final String[] families = ge.getAvailableFontFamilyNames();
		Font[] fonts = new Font[families.length];
		int comboFontSize = comboFont.getSize();
		for (int i = 0; i < fonts.length; i++) {
			fonts[i] = new Font(families[i], Font.PLAIN, comboFontSize);
		}
		familyCombo = new JComboBox(fonts);
		final JToggleButton toggle = new JToggleButton("Toggle Rendering mode");
		familyCombo.setToolTipText("Font family");
		familyCombo.setRenderer(new FontListCellRenderer());
		familyCombo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Font font = (Font) familyCombo.getSelectedItem();
				familyCombo.setFont(font.canDisplayUpTo(font.getName()) == -1 ? font : comboFont);
				renderContent(toggle.isSelected());
			}
		});
		final JLabel mode = new JLabel(MODE_NATIVE);
		toggle.setSelected(INITIAL_STATE);
		toggle.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean selected = toggle.isSelected();
				if (selected) {
					mode.setText(MODE_IMG);
				} else {
					mode.setText(MODE_NATIVE);
				}
				renderContent(selected);
			}

		});
		control.add(familyCombo);
		control.add(toggle);
		control.add(mode);
		content = new JPanel();
		renderContent(INITIAL_STATE);
		add(control, BorderLayout.NORTH);
		add(content, BorderLayout.CENTER);
	}

	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private void renderContent(boolean isBufferedImage) {
		content.removeAll();
		FontsPanel panel = new FontsPanel(html,familyCombo.getFont());
		if(isBufferedImage){
			BufferedImage img = new BufferedImage(600, 300, BufferedImage.TYPE_INT_ARGB);
			Graphics g = img.getGraphics();
			panel.setSize(new Dimension(600,300));
			panel.layout();
			panel.paint(g);
			g.dispose();
			JLabel jLabel = new JLabel(new ImageIcon(img));
			content.add(jLabel);
		}else{
			content.add(panel);
		}
		content.invalidate();
		content.validate();
		content.repaint();
	}

	public static void main(String[] args) {
		final JFrame f = new JFrame("Fonts UI Example");
		f.getContentPane().add(new FontsDemo());
		f.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		f.pack();
		f.setVisible(true);
	}

	public static class FontListCellRenderer extends DefaultListCellRenderer {

		private final Font defaultFont = super.getFont();
		private final Color symbolColor;

		public FontListCellRenderer() {
			this(Color.RED);
		}

		public FontListCellRenderer(Color symbolColor) {
			if (symbolColor == null) {
				this.symbolColor = Color.RED;
			} else {
				this.symbolColor = symbolColor;
			}
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Font font = (Font) value;
			setText(font.getName());
			if (isSelected) {
				setForeground(list.getSelectionForeground());
				setBackground(list.getSelectionBackground());
			} else {
				setForeground(list.getForeground());
				setBackground(list.getBackground());
			}
			if (font.canDisplayUpTo(font.getName()) == -1) {
				setFont(font);
			} else {
				setForeground(symbolColor);
				setFont(defaultFont);
			}
			return this;
		}
	}

}

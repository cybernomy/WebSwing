package org.webswing.demo.dnd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.sun.swingset3.DemoProperties;
import com.sun.swingset3.demos.table.OscarCellRenderers.RowRenderer;
import org.webswing.toolkit.api.WebswingUtil;
import org.webswing.toolkit.api.clipboard.BrowserTransferable;
import org.webswing.toolkit.api.clipboard.PasteRequestContext;
import org.webswing.toolkit.api.url.WebswingUrlState;
import org.webswing.toolkit.api.url.WebswingUrlStateChangeEvent;
import org.webswing.toolkit.api.url.WebswingUrlStateChangeListener;

@DemoProperties(value = "Clipboard", category = "Webswing", description = "Demonstrates Clipboard integration.", sourceFiles = { "org/webswing/demo/dnd/ClipboardDemo.java" })
public class ClipboardDemo extends JPanel {

	private final JTable table1;
	private Color[] rowColors;

	public ClipboardDemo() {
		setLayout(new BorderLayout());
		JPanel panel = new JPanel(new GridLayout(2, 1));
		DefaultTableModel model1 = new ClipboardTableModel(0, 4);
		model1.addRow(new Object[] { createImage("resources/images/ClipboardDemo.gif"), "Simple text", "<p> Formated <b>HTML</b> text</p>", createFiles(2) });
		model1.addRow(new Object[] { null, null, null, null });
		model1.addRow(new Object[] { null, "Simple text", null, null });
		model1.addRow(new Object[] { null, null, "<p> Formated <b>HTML</b> text</p>", null });
		model1.addRow(new Object[] { createImage("resources/images/ClipboardDemo.gif"), null, null, null });
		model1.addRow(new Object[] { null, null, null, createFiles(1) });
		table1 = createTable(model1);
		if (WebswingUtil.isWebswing()) {
			setSelectionFromPath();
			WebswingUtil.getWebswingApi().addUrlStateChangeListener(new WebswingUrlStateChangeListener() {
				@Override
				public void onUrlStateChange(WebswingUrlStateChangeEvent event) {
					setSelectionFromPath();
				}
			});

		}
		table1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int i = table1.getSelectedRow();
				if (WebswingUtil.isWebswing()) {
					WebswingUrlState state = WebswingUtil.getWebswingApi().getUrlState();
					state.getParameters().put("row", i + "");
					WebswingUtil.getWebswingApi().setUrlState(state, false);
				}
			}
		});
		JScrollPane scrollpane1 = new JScrollPane(table1);
		scrollpane1.setBorder(BorderFactory.createTitledBorder("Copy Table"));
		panel.add(scrollpane1);
		add(panel, BorderLayout.NORTH);
	}

	public JTable createTable(TableModel tableModel) {

		//<snip>Create JTable
		final JTable clipboardTable = new JTable(tableModel);
		//</snip>

		//</snip>Set JTable display properties
		clipboardTable.setColumnModel(createColumnModel());
		clipboardTable.setAutoCreateRowSorter(true);
		clipboardTable.setRowHeight(26);
		clipboardTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		clipboardTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		clipboardTable.setIntercellSpacing(new Dimension(0, 0));
		//</snip>

		//<snip>Initialize preferred size for table's viewable area
		Dimension viewSize = new Dimension();
		viewSize.width = clipboardTable.getColumnModel().getTotalColumnWidth();
		viewSize.height = 10 * clipboardTable.getRowHeight();
		clipboardTable.setPreferredScrollableViewportSize(viewSize);
		//</snip>

		//<snip>Customize height and alignment of table header
		JTableHeader header = clipboardTable.getTableHeader();
		header.setPreferredSize(new Dimension(30, 26));
		TableCellRenderer headerRenderer = header.getDefaultRenderer();
		if (headerRenderer instanceof JLabel) {
			((JLabel) headerRenderer).setHorizontalAlignment(JLabel.CENTER);
		}
		//</snip>
		final JPopupMenu pm = new JPopupMenu();
		pm.add(new CopyAction(clipboardTable));
		pm.add(new PasteAction(clipboardTable));
		pm.add(new CutAction(clipboardTable));
		pm.add(new PasteSpecialAction(clipboardTable));
		pm.add(new PasteFromBrowserAction(clipboardTable));
		pm.add(new PasteFromBrowserDialogAction(clipboardTable));
		pm.add(new CopyToBrowserAction(clipboardTable));
		clipboardTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.isPopupTrigger()) {
					highlightRow(e);
					doPopup(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					highlightRow(e);
					doPopup(e);
				}
			}

			protected void doPopup(MouseEvent e) {
				pm.show(e.getComponent(), e.getX(), e.getY());
			}

			protected void highlightRow(MouseEvent e) {
				JTable table = (JTable) e.getSource();
				Point point = e.getPoint();
				int row = table.rowAtPoint(point);
				int col = table.columnAtPoint(point);

				table.setRowSelectionInterval(row, row);
				table.setColumnSelectionInterval(col, col);
			}

		});

		KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
		//Identifying the copy KeyStroke user can modify this
		//to copy on some other Key combination.
		KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
		KeyStroke pasteSpecial = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK, false);
		KeyStroke cut = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK, false);
		//Identifying the Paste KeyStroke user can modify this
		//to copy on some other Key combination.
		clipboardTable.registerKeyboardAction(new CopyAction(clipboardTable), "Copy", copy, JComponent.WHEN_FOCUSED);
		clipboardTable.registerKeyboardAction(new PasteAction(clipboardTable), "Paste", paste, JComponent.WHEN_FOCUSED);
		clipboardTable.registerKeyboardAction(new PasteSpecialAction(clipboardTable), "PasteSpecial", pasteSpecial, JComponent.WHEN_FOCUSED);
		clipboardTable.registerKeyboardAction(new CutAction(clipboardTable), "Cut", cut, JComponent.WHEN_FOCUSED);

		return clipboardTable;
	}

	private void setSelectionFromPath() {
		WebswingUrlState state = WebswingUtil.getWebswingApi().getUrlState();
		if (state != null && state.getPath() != null && state.getPath().equals("Clipboard")) {
			String r = state.getParameters().get("row");
			if (r != null) {
				try {
					int i = Integer.parseInt(r);
					table1.setRowSelectionInterval(i, i);
				} catch (NumberFormatException e) {
				}
			}
		}
	}

	//<snip>Initialize table columns
	protected TableColumnModel createColumnModel() {
		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();

		TableColumn column = new TableColumn();
		column.setModelIndex(0);
		column.setHeaderValue("Image");
		column.setCellRenderer(new ImageRenderer());
		column.setPreferredWidth(30);
		columnModel.addColumn(column);

		column = new TableColumn();
		column.setModelIndex(1);
		column.setHeaderValue("Plain text");
		column.setCellRenderer(new TextRenderer());
		columnModel.addColumn(column);

		column = new TableColumn();
		column.setModelIndex(2);
		column.setHeaderValue("HTML");
		column.setCellRenderer(new HTMLRenderer());
		columnModel.addColumn(column);

		column = new TableColumn();
		column.setModelIndex(3);
		column.setHeaderValue("Files");
		column.setCellRenderer(new FilesRenderer());
		column.setPreferredWidth(50);
		columnModel.addColumn(column);

		return columnModel;
	}

	private Color[] getTableRowColors() {
		if (rowColors == null) {
			rowColors = new Color[2];
			rowColors[0] = UIManager.getColor("Table.background");
			rowColors[1] = new Color((int) (rowColors[0].getRed() * .9), (int) (rowColors[0].getGreen() * .9), (int) (rowColors[0].getBlue() * .9));
		}
		return rowColors;
	}

	protected static Image createImage(String path) {
		java.net.URL imageURL = ClipboardDemo.class.getResource(path);
		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		} else {
			try {
				return Toolkit.getDefaultToolkit().getImage(imageURL);
			} catch (Exception e) {
				System.err.println("Image cannot be loaded: " + path);
				return null;
			}
		}
	}

	protected static List<File> createFiles(int number) {
		List<File> result = new ArrayList<File>();
		File file;
		try {
			int count = 0;
			file = new File(ClipboardDemo.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			for (File f : new File(file.getParent()).listFiles()) {
				if (count < number) {
					try {
						result.add(f);
						count++;
					} catch (Exception e) {
						System.err.println("Image cannot be loaded: " + f.getAbsolutePath());
						continue;
					}
				}
			}
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		return result;
	}

	public static class ImageRenderer extends ClipboardRenderer {

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			TableModel model = table.getModel();
			Image img = (Image) model.getValueAt(table.convertRowIndexToModel(row), 0);
			if (img != null) {
				setIcon(new ImageIcon(img));
				setText("");
			} else {
				setIcon(null);
				setText("-");
			}
			return this;
		}
	}

	private static Object[] getRowData(JTable table, int row) {
		Object[] res = new Object[4];
		for (int i = 0; i < res.length; i++) {
			res[i] = table.getValueAt(row, i);
		}
		return res;
	}

	public static class FilesRenderer extends ClipboardRenderer {

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			TableModel model = table.getModel();
			List<File> files = (List<File>) model.getValueAt(table.convertRowIndexToModel(row), 3);
			if (files != null) {
				StringBuffer sb = new StringBuffer();
				for (File f : files) {
					sb.append(f.getAbsolutePath() + "\n");
				}
				setText(files.size() + " files.");
				setToolTipText(sb.toString());
			} else {
				setText("-");
			}
			return this;
		}
	}

	public static class TextRenderer extends ClipboardRenderer {

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			TableModel model = table.getModel();
			String string = (String) model.getValueAt(table.convertRowIndexToModel(row), 1);
			setText(string == null ? "-" : string);
			return this;
		}
	}

	public static class HTMLRenderer extends ClipboardRenderer {

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			TableModel model = table.getModel();
			String string = (String) model.getValueAt(table.convertRowIndexToModel(row), 2);
			setText(string == null ? "-" : string);
			return this;
		}

	}

	public static class ClipboardRenderer extends RowRenderer {
	}

	public class ClipboardTableModel extends DefaultTableModel {
		public ClipboardTableModel(int r, int c) {
			super(r, c);
		}

		@Override
		public boolean isCellEditable(int row, int column) {

			return false;
		}

	}

	class CopyAction extends AbstractAction {

		private JTable table;

		public CopyAction(JTable table) {
			this.table = table;
			putValue(NAME, "Copy");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int row = table.getSelectedRow();
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			cb.setContents(new RowTransferable(getRowData(table, row)), null);
		}

	}

	class CopyToBrowserAction extends AbstractAction {

		private JTable table;

		public CopyToBrowserAction(JTable table) {
			this.table = table;
			putValue(NAME, "Copy to Browser");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int row = table.getSelectedRow();
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			cb.setContents(new RowTransferable(getRowData(table, row)), null);
			WebswingUtil.getWebswingApi().sendClipboard();
		}

	}

	class CutAction extends AbstractAction {

		private JTable table;

		public CutAction(JTable table) {
			this.table = table;
			putValue(NAME, "Cut");

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int row = table.getSelectedRow();

			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			try {
				cb.setContents(new RowTransferable(getRowData(table, row)), null);
				((ClipboardTableModel) table.getModel()).removeRow(row);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	class PasteAction extends AbstractAction {

		private JTable table;

		public PasteAction(JTable tbl) {

			putValue(NAME, "Paste");

			table = tbl;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			ClipboardTableModel model = (ClipboardTableModel) table.getModel();
			if (cb.isDataFlavorAvailable(RowTransferable.CELL_DATA_FLAVOR)) {
				try {
					Object[] value = (Object[]) cb.getData(RowTransferable.CELL_DATA_FLAVOR);
					model.addRow(value);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				Object[] value = new Object[4];
				try {
					if (cb.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
						Image v = (Image) cb.getData(DataFlavor.imageFlavor);
						value[0] = v;
					}
					if (cb.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
						String v = (String) cb.getData(DataFlavor.stringFlavor);
						value[1] = v;
					}
					if (cb.isDataFlavorAvailable(new DataFlavor("text/html;class=java.lang.String"))) {
						String v = (String) cb.getData(new DataFlavor("text/html;class=java.lang.String"));
						value[2] = v;
					}
					if (cb.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
						List<File> v = (List<File>) cb.getData(DataFlavor.javaFileListFlavor);
						value[3] = v;
					}
				} catch (Exception x) {
					System.out.println("Paste exception:");
					x.printStackTrace();
				}
				model.addRow(value);
			}

		}

	}

	class PasteSpecialAction extends AbstractAction {

		private JTable table;

		public PasteSpecialAction(JTable tbl) {

			putValue(NAME, "Paste Special (tostring)");
			table = tbl;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			ClipboardTableModel model = (ClipboardTableModel) table.getModel();
			Object[] value = new Object[4];
			try {
				if (cb.isDataFlavorAvailable(RowTransferable.CELL_DATA_FLAVOR)) {
					Object[] cell = (Object[]) cb.getData(RowTransferable.CELL_DATA_FLAVOR);
					value[1] = Arrays.asList(cell).toString();
				} else {
					value[1] = "";
					if (cb.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
						Image v = (Image) cb.getData(DataFlavor.imageFlavor);
						value[1] += v.toString();
					}
					if (cb.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
						String v = (String) cb.getData(DataFlavor.stringFlavor);
						value[1] += v;
					}
					if (cb.isDataFlavorAvailable(new DataFlavor("text/html;class=java.lang.String"))) {
						String v = (String) cb.getData(new DataFlavor("text/html;class=java.lang.String"));
						value[1] += v.toString();
					}
					if (cb.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
						List<File> v = (List<File>) cb.getData(DataFlavor.javaFileListFlavor);
						value[1] += v.toString();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				value[1] = ex.getMessage();
			}
			model.addRow(value);
		}
	}

	class PasteFromBrowserAction extends AbstractAction {

		private JTable table;

		public PasteFromBrowserAction(JTable tbl) {
			putValue(NAME, "Paste from Browser");
			table = tbl;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final JDialog pasteDialog = new JDialog((JFrame) null, true);
			JPanel panel = new JPanel();
			panel.setFocusable(true);
			panel.add(new JLabel("Press CTRL+V to send local clipboard."));
			pasteDialog.getContentPane().add(panel);
			KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
			panel.registerKeyboardAction(new PasteAction(table) {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(WebswingUtil.getWebswingApi().getBrowserClipboard(), null);
						super.actionPerformed(e);
					} finally {
						pasteDialog.setVisible(false);
					}
				}
			}, "Paste Browser", paste, JComponent.WHEN_FOCUSED);
			pasteDialog.pack();
			pasteDialog.setLocationRelativeTo(null);  // *** this will center your app ***
			pasteDialog.setVisible(true);
			panel.requestFocus();
		}
	}

	class PasteFromBrowserDialogAction extends AbstractAction {

		private JTable table;

		public PasteFromBrowserDialogAction(JTable tbl) {
			putValue(NAME, "Paste from Browser Dialog");
			table = tbl;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (WebswingUtil.isWebswing()) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						PasteRequestContext ctx = new PasteRequestContext();
						ctx.setTitle("Please paste content");
						ctx.setMessage("User ctrl+v or paste from context menu to input field below.");
						BrowserTransferable transferable = WebswingUtil.getWebswingApi().getBrowserClipboard(ctx);
						if (transferable != null) {
							Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
							new PasteAction(table1).actionPerformed(null);
						}
					}
				}).start();

			}
		}
	}

	public static class RowTransferable implements Transferable {

		public static final DataFlavor CELL_DATA_FLAVOR = new DataFlavor(Object.class, "application/x-cell-value");

		private Object[] cellValue;

		public RowTransferable(Object[] cellValue) {
			this.cellValue = cellValue;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			List<DataFlavor> flavors = new ArrayList<DataFlavor>();
			if (cellValue[0] != null) {
				flavors.add(DataFlavor.imageFlavor);
			}
			if (cellValue[1] != null) {
				flavors.add(DataFlavor.stringFlavor);
			}
			if (cellValue[2] != null) {
				try {
					flavors.add(new DataFlavor("text/html;class=java.lang.String"));
				} catch (Exception e) {
				}
			}
			if (cellValue[3] != null) {
				flavors.add(DataFlavor.javaFileListFlavor);
			}
			flavors.add(CELL_DATA_FLAVOR);
			return flavors.toArray(new DataFlavor[flavors.size()]);
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return Arrays.asList(getTransferDataFlavors()).contains(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			if (DataFlavor.imageFlavor.equals(flavor)) {
				return cellValue[0];
			}
			if (DataFlavor.stringFlavor.equals(flavor)) {
				return cellValue[1];
			}
			try {
				if (new DataFlavor("text/html;class=java.lang.String").equals(flavor)) {
					return cellValue[2];
				}
			} catch (ClassNotFoundException e) {
			}
			if (DataFlavor.javaFileListFlavor.equals(flavor)) {
				return cellValue[3];
			}
			return cellValue;
		}

	}

}

package org.webswing.demo.dnd;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.TransferHandler;

public class PictureExportHandler extends TransferHandler {
	DTPicture sourcePic;
	boolean shouldRemove;

	public boolean importData(JComponent c, Transferable t) {
		Image image;
		if (canImport(c, t.getTransferDataFlavors())) {
			try {
				JFileChooser fileChooser = new JFileChooser();
				int result = fileChooser.showSaveDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					image = (Image) t.getTransferData(PictureTransferHandler.pictureFlavor);
					File exportTo = fileChooser.getSelectedFile();
					BufferedImage i = new BufferedImage(image.getWidth(null),image.getHeight(null),BufferedImage.TYPE_3BYTE_BGR);
					Graphics g = i.getGraphics();
					g.drawImage(image, 0, 0, null);
					g.dispose();
					Thread.sleep(1000);
					ImageIO.write( i, "jpg", exportTo);
					return true;
				} else {
					return false;
				}
			} catch (UnsupportedFlavorException ufe) {
				System.out.println("importData: unsupported data flavor");
			} catch (Exception ioe) {
				ioe.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public boolean importData(TransferSupport support) {
		return support.getComponent() instanceof JComponent
				? importData((JComponent) support.getComponent(), support.getTransferable()) : false;
	}

	public boolean canImport(JComponent c, DataFlavor[] flavors) {
		for (int i = 0; i < flavors.length; i++) {
			if (PictureTransferHandler.pictureFlavor.equals(flavors[i])) {
				return true;
			}
		}
		return false;
	}

}

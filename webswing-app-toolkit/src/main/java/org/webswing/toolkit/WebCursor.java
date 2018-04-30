package org.webswing.toolkit;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;

import org.webswing.toolkit.util.WaitingImageObserver;

public class WebCursor extends Cursor {

	private static final long serialVersionUID = -7369225579337480240L;
	private Image image;
	private Point hotSpot;

	public WebCursor(Image cursor, Point hotSpot, String name) {
		super(name);
		this.image = cursor;
		this.hotSpot = hotSpot;
	}

	public BufferedImage getImage() {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		} else {
			new WaitingImageObserver(image).waitImageLoaded();
			BufferedImage bimg = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics g = bimg.getGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
			image = bimg;
			return bimg;
		}
	}

	public Point getHotSpot() {
		return hotSpot;
	}

}

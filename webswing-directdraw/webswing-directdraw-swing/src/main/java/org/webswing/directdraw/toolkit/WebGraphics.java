package org.webswing.directdraw.toolkit;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;

import org.webswing.directdraw.util.WaitingImageObserver;
import org.webswing.directdraw.util.XorModeComposite;

public class WebGraphics extends AbstractVectorGraphics {

	WebImage thisImage;
	private DrawInstructionFactory dif;
	private boolean disposed = false;
	private int id;
	private boolean onDemandTexturePaint;

	public WebGraphics(WebImage webImage) {
		super(new Dimension(webImage.getWidth(null), webImage.getHeight(null)));
		this.thisImage = webImage;
		this.dif = thisImage.getContext().getInstructionFactory();
		this.id = this.thisImage.getNextGraphicsId();
	}

	public WebGraphics(WebGraphics g) {
		super(g);
		this.thisImage = g.thisImage;
		this.dif = thisImage.getContext().getInstructionFactory();
		this.id = this.thisImage.getNextGraphicsId();
	}

	@Override
	public void draw(Shape s) {
		if (getStroke() instanceof BasicStroke) {
			if (onDemandTexturePaint) {
				thisImage.addInstruction(this, dif.setPaint(createTexture(s, getPaint())));
			}
			thisImage.addInstruction(this, dif.draw(s, getClip()));
		} else {
			fill(getStroke().createStrokedShape(s));
		}
	}

	private Paint createTexture(Shape s, Paint paint) {
		Rectangle bounds = s.getBounds();
		BufferedImage img = new BufferedImage(Math.max(1,bounds.width), Math.max(1,bounds.height), paint.getTransparency() == Paint.OPAQUE ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		g.translate(-bounds.x, -bounds.y);
		g.setPaint(paint);
		g.fill(s.getBounds());
		g.dispose();
		return new TexturePaint(img, bounds);
	}

	private BufferedImage toBufferedImage(RenderedImage image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}
		BufferedImage bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = bufferedImage.createGraphics();
		graphics.drawRenderedImage(image, new AffineTransform());
		return bufferedImage;
	}

	protected boolean writeImage(Image image, AffineTransform transform, ImageObserver observer) {
		if (image instanceof WebImage || image instanceof VolatileWebImageWrapper) {
			WebImage wi = image instanceof WebImage ? (WebImage) image : ((VolatileWebImageWrapper) image).getWebImage();
			if (wi.isDirty()) {
				thisImage.addInstruction(this, dif.drawWebImage(wi.extractReadOnlyWebImage(false), transform, null, null, getClip()));
			}
			return true;
		} else {
			ImageConvertResult result = toBufferedImage(image, observer);
			thisImage.addInstruction(this, dif.drawImage(result.image, transform, null, null, getClip()));
			return result.status;
		}
	}

	@Override
	public void fill(Shape s) {
		if (onDemandTexturePaint) {
			thisImage.addInstruction(this, dif.setPaint(createTexture(s, getPaint())));
		}
		thisImage.addInstruction(this, dif.fill(s, getClip()));
	}

	@Override
	protected void writeImage(RenderedImage image, AffineTransform transform) {
		thisImage.addInstruction(this, dif.drawImage(toBufferedImage(image), transform, null, null, getClip()));
	}

	@Override
	protected boolean writeImage(Image image, AffineTransform transform, Rectangle2D crop, Color bgcolor, ImageObserver observer) {
		if (image instanceof WebImage || image instanceof VolatileWebImageWrapper) {
			WebImage wi = image instanceof WebImage ? (WebImage) image : ((VolatileWebImageWrapper) image).getWebImage();
			if (wi.isDirty()) {
				thisImage.addInstruction(this, dif.drawWebImage(wi.extractReadOnlyWebImage(false), transform, crop, bgcolor, getClip()));
			}
			return true;
		} else {
			ImageConvertResult result = toBufferedImage(image, observer);
			thisImage.addInstruction(this, dif.drawImage(result.image, transform, crop, bgcolor, getClip()));
			return result.status;
		}
	}

	private ImageConvertResult toBufferedImage(Image image, ImageObserver observer) {
		if (image instanceof BufferedImage) {
			return new ImageConvertResult(true, (BufferedImage) image);
		}
		try {
			new WaitingImageObserver(image).waitImageLoaded();//magic
		} catch (Exception e) {
			//ignore
		}
		BufferedImage bufferedImage = new BufferedImage(image.getWidth(observer), image.getHeight(observer), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = bufferedImage.createGraphics();
		try {
			return new ImageConvertResult(graphics.drawImage(image, 0, 0, observer), bufferedImage);
		} finally {
			graphics.dispose();
		}
	}

	@Override
	protected void writeString(String string, double x, double y) {
		Font font = getFont();
		if (thisImage.getContext().requestFont(font)) {
			int width = getFontMetrics().stringWidth(string);
			thisImage.addInstruction(this, dif.drawString(string, x, y, width, getClip()));
		} else {
			thisImage.addInstruction(this, dif.drawGlyphList(string, font, x, y, getTransform(), getClip()));
		}
	}

	@Override
	protected void writeTransform(AffineTransform transform) {
		thisImage.addInstruction(this, dif.transform(transform));

	}

	@Override
	protected void writePaint(Paint paint) {
		try {
			this.onDemandTexturePaint = false;
			thisImage.addInstruction(this, dif.setPaint(paint));
		} catch (UnsupportedOperationException e) {
			this.onDemandTexturePaint = true;
		}
	}

	@Override
	public void writeStroke(Stroke stroke) {
		if (stroke instanceof BasicStroke) {
			thisImage.addInstruction(this, dif.setStroke((BasicStroke) stroke));
		}
	}

	@Override
	protected void writeComposite(Composite composite) {
		if (composite instanceof AlphaComposite) {
			AlphaComposite ac = (AlphaComposite) composite;
			thisImage.addInstruction(this, dif.setComposite(ac));
		} else if (composite instanceof XorModeComposite) {
			thisImage.addInstruction(this, dif.setXorMode(((XorModeComposite) composite).getXorColor()));
		}
	}

	@Override
	protected void writeFont(Font font) {
		if (thisImage.getContext().requestFont(font)) {
			thisImage.addInstruction(this, dif.setFont(font));
		}
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		Point2D abs = getTransform().transform(new Point(x, y), null);
		int absx = (int) abs.getX();
		int absy = (int) abs.getY();
		thisImage.addInstruction(this, dif.copyArea(absx + dx, absy + dy, width, height, dx, dy, getClip()));
	}

	@Override
	public Graphics create() {
		return new WebGraphics(this);
	}

	@Override
	public void setPaintMode() {
		setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
	}

	@Override
	public void setXORMode(Color c1) {
		setComposite(new XorModeComposite(c1));
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	}

	public boolean isDisposed() {
		return disposed;
	}

	@Override
	public void dispose() {
		if (!disposed) {
			thisImage.addInstruction(this, dif.disposeGraphics(this));
			thisImage.dispose(this);
			disposed = true;
		}
	}

	public int getId() {
		return id;
	}

	private static class ImageConvertResult {

		public final boolean status;
		public final BufferedImage image;

		public ImageConvertResult(boolean status, BufferedImage image) {
			this.status = status;
			this.image = image;
		}
	}
}

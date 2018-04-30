package org.webswing.services.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;

import javax.print.attribute.standard.MediaSizeName;

import org.freehep.graphicsbase.util.UserProperties;
import org.freehep.graphicsio.PageConstants;
import org.freehep.graphicsio.pdf.PDFGraphics2D;
import org.webswing.ext.services.PdfService;
import org.webswing.toolkit.WebPrinterJob;
import org.webswing.toolkit.WebPrinterJobWrapper;
import org.webswing.toolkit.util.Util;

public class PdfServiceImpl implements PdfService {

	private static PdfServiceImpl impl;

	public static PdfServiceImpl getInstance() {
		if (impl == null) {
			impl = new PdfServiceImpl();
		}
		return impl;
	}

	@Override
	public Graphics2D createPDFGraphics(OutputStream out, PageFormat format) {
		UserProperties props = createPdfProperties(format);
		PDFGraphics2D graphics = new WebPdfGraphics2D(out, getPageDimension(props));
		graphics.setProperties(props);
		graphics.setMultiPage(true);
		graphics.startExport();
		return graphics;
	}

	@Override
	public void startPagePDFGraphics(Graphics2D pdfGrapthics, PageFormat format) {
		try {
			PDFGraphics2D g = (PDFGraphics2D) pdfGrapthics;
			UserProperties props = createPdfProperties(format);
			g.setProperties(props);
			g.openPage(getPageDimension(props), null);
		} catch (IOException e) {
			((PDFGraphics2D) pdfGrapthics).endExport();
			throw new RuntimeException(e);
		}
	}

	private Dimension getPageDimension(UserProperties props) {
		Dimension d = PageConstants.getSize(props.getProperty(PDFGraphics2D.PAGE_SIZE), props.getProperty(PDFGraphics2D.ORIENTATION));
		Insets m = PageConstants.getMargins(props.getPropertyInsets(PDFGraphics2D.PAGE_MARGINS), props.getProperty(PDFGraphics2D.ORIENTATION));
		Dimension result = new Dimension();
		result.setSize(d.getWidth() - m.left - m.right, d.getHeight() - m.top - m.bottom);
		return result;
	}

	private UserProperties createPdfProperties(PageFormat format) {
		UserProperties props = new UserProperties();
		String orientation = format.getOrientation() == PageFormat.LANDSCAPE ? PageConstants.LANDSCAPE : PageConstants.PORTRAIT;
		String size = resolveSize(format);
		String margin = resolveMargins(format);
		props.setProperty(PDFGraphics2D.ORIENTATION, orientation);
		props.setProperty(PDFGraphics2D.PAGE_SIZE, size);
		props.setProperty(PDFGraphics2D.FIT_TO_PAGE, false);
		props.setProperty(PDFGraphics2D.PAGE_MARGINS, margin);
		props.setProperty(PDFGraphics2D.AUTHOR, "Webswing.org");
		props.setProperty(PDFGraphics2D.KEYWORDS, "Page:" + size + " Orientation:" + orientation + " Margins:" + margin);
		return props;
	}

	private String resolveMargins(PageFormat format) {
		DecimalFormat nf = new DecimalFormat("#");
		String separator = ", ";
		String top = nf.format(format.getImageableY());
		String left = nf.format(format.getImageableX());
		String bottom = nf.format(format.getHeight() - format.getImageableHeight() - format.getImageableY());
		String right = nf.format(format.getWidth() - format.getImageableWidth() - format.getImageableX());
		return top + separator + left + separator + bottom + separator + right;
	}

	private String resolveSize(PageFormat format) {
		Paper p = format.getPaper();
		MediaSizeName size = WebPrinterJobWrapper.findMedia(p.getWidth(), p.getHeight());
		if (size == MediaSizeName.ISO_A3) {
			return PageConstants.A3;
		} else if (size == MediaSizeName.ISO_A4) {
			return PageConstants.A4;
		} else if (size == MediaSizeName.ISO_A5) {
			return PageConstants.A5;
		} else if (size == MediaSizeName.ISO_A6) {
			return PageConstants.A6;
		} else if (size == MediaSizeName.NA_LETTER) {
			return PageConstants.LETTER;
		} else if (size == MediaSizeName.NA_LEGAL) {
			return PageConstants.LEGAL;
		} else if (size == MediaSizeName.EXECUTIVE) {
			return PageConstants.EXECUTIVE;
		} else if (size == MediaSizeName.LEDGER) {
			return PageConstants.LEDGER;
		} else {
			return PageConstants.A4;
		}
	}

	@Override
	public void endPagePDFGraphics(Graphics2D pdfGrapthics) {
		try {
			((PDFGraphics2D) pdfGrapthics).setTransform(new AffineTransform(1, 0, 0, 1, 0, 0));
			((PDFGraphics2D) pdfGrapthics).closePage();
		} catch (IOException e) {
			((PDFGraphics2D) pdfGrapthics).endExport();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void closePDFGraphics(Graphics2D pdfGrapthics) {
		((PDFGraphics2D) pdfGrapthics).endExport();
	}

	public static class WebPdfGraphics2D extends PDFGraphics2D {
		public WebPdfGraphics2D(File file, Dimension size) throws FileNotFoundException {
			super(file, size);
		}

		public WebPdfGraphics2D(File file, Component component) throws FileNotFoundException {
			super(file, component);
		}

		public WebPdfGraphics2D(OutputStream ros, Dimension size) {
			super(ros, size);
		}

		public WebPdfGraphics2D(OutputStream ros, Component component) {
			super(ros, component);
		}

		protected WebPdfGraphics2D(WebPdfGraphics2D graphics, boolean doRestoreOnDispose) {
			super(graphics, doRestoreOnDispose);
		}

		@Override
		public GraphicsConfiguration getDeviceConfiguration() {
			return Util.getWebToolkit().getGraphicsConfig();
		}

		public Graphics create() {
			try {
				writeGraphicsSave();
			} catch (IOException e) {
				handleException(e);
			}
			return new WebPdfGraphics2D(this, true);
		}

		public Graphics create(double x, double y, double width, double height) {
			try {
				writeGraphicsSave();
			} catch (IOException e) {
				handleException(e);
			}
			PDFGraphics2D graphics = new WebPdfGraphics2D(this, true);
			graphics.translate(x, y);
			graphics.clipRect(0, 0, width, height);
			return graphics;
		}

	};
}

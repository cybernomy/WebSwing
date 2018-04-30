package org.webswing.ext.services;

import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.io.OutputStream;

public interface PdfService {

    Graphics2D createPDFGraphics(OutputStream out,  PageFormat format);

    void startPagePDFGraphics(Graphics2D pdfGrapthics, PageFormat pageFormat2);

    void endPagePDFGraphics(Graphics2D pdfGrapthics);

    void closePDFGraphics(Graphics2D pdfGrapthics);

}

package org.webswing.demo.printing;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import java.awt.Graphics;
import java.awt.JobAttributes;
import java.awt.PageAttributes;
import java.awt.PageAttributes.MediaType;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class PrintJobExample implements ActionListener {

	PrintingDemo frameToPrint;
	private PrintJob printjob;

	public void actionPerformed(ActionEvent e) {
		PrintJob localPrintJob = this.printjob;
		if (localPrintJob == null) {
			JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(frameToPrint);
			PageAttributes pageAttrs = new PageAttributes();
			boolean protrait = "Portrait".equals(frameToPrint.orientation.getSelectedItem());
			pageAttrs.setOrientationRequested(protrait ? 3 : 4);
			localPrintJob = Toolkit.getDefaultToolkit().getPrintJob(topFrame, "Test", null, pageAttrs);
		}
		if (localPrintJob == null) {
			return;
		}
		for (int i = 0; i < 3; i++) {
			Graphics localGraphics = localPrintJob.getGraphics();
			frameToPrint.print(localGraphics);
			localGraphics.dispose();
		}
		localPrintJob.end();
	}

	public PrintJobExample(PrintingDemo f) {
		frameToPrint = f;

	}

}

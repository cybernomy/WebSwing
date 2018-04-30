package org.webswing.model.internal;

import java.io.File;

import org.webswing.model.MsgInternal;

public class PrinterJobResultMsgInternal implements MsgInternal {

	private static final long serialVersionUID = 6352518694214860256L;
	private File pdfFile;
	private String id;
	private String clientId;

	public File getPdfFile() {
		return pdfFile;
	}

	public void setPdfFile(File pdfFile) {
		this.pdfFile = pdfFile;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

}

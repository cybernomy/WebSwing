package org.webswing.model.c2s;

import org.webswing.model.MsgIn;

public class UploadEventMsgIn implements MsgIn {

	private static final long serialVersionUID = -7188733550212761231L;

	private String fileName;
	private String tempFileLocation;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getTempFileLocation() {
		return tempFileLocation;
	}

	public void setTempFileLocation(String tempFileLocation) {
		this.tempFileLocation = tempFileLocation;
	}

}

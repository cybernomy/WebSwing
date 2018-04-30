package org.webswing.model.internal;

import java.io.File;

import org.webswing.model.MsgInternal;

public class OpenFileResultMsgInternal implements MsgInternal {

	private static final long serialVersionUID = 2490892979442744806L;
	private File file;
	private String clientId;
	private boolean waitForFile;
	private String overwriteDetails;

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public boolean isWaitForFile() {
		return waitForFile;
	}

	public void setWaitForFile(boolean waitForFile) {
		this.waitForFile = waitForFile;
	}

	public String getOverwriteDetails() {
		return overwriteDetails;
	}

	public void setOverwriteDetails(String overwriteDetails) {
		this.overwriteDetails = overwriteDetails;
	}

}

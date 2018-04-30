package org.webswing.model.c2s;

import java.util.List;

import org.webswing.model.MsgIn;

public class FilesSelectedEventMsgIn implements MsgIn {

	private static final long serialVersionUID = 75198619L;
	private List<String> files;

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

}
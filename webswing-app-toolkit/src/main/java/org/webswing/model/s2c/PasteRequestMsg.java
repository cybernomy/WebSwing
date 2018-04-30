package org.webswing.model.s2c;

import org.webswing.model.MsgOut;

public class PasteRequestMsg implements MsgOut {
	private String title;
	private String message;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}

package org.webswing.demo.api;

import java.io.Serializable;

public class TestMsg implements Serializable {

	private String message;

	public TestMsg(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}

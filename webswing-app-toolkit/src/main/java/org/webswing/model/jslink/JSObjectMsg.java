package org.webswing.model.jslink;

import org.webswing.model.Msg;

public class JSObjectMsg implements Msg {
	private static final long serialVersionUID = -5961292110459877432L;
	private String id;

	public JSObjectMsg() {
	}

	public JSObjectMsg(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}

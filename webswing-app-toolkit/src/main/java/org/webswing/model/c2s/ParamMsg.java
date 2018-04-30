package org.webswing.model.c2s;

import org.webswing.model.Msg;

public class ParamMsg implements Msg {

	private static final long serialVersionUID = 3451224547948314923L;

	private String name;
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}

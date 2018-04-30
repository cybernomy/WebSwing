package org.webswing.model.jslink;

import java.util.List;

import org.webswing.model.Msg;

public class JavaObjectRefMsg implements Msg {
	private static final long serialVersionUID = -1260785304443300962L;
	private String id;
	private List<String> methods;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getMethods() {
		return methods;
	}

	public void setMethods(List<String> methods) {
		this.methods = methods;
	}

}

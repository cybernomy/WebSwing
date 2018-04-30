package org.webswing.model.jslink;

import java.util.List;

import org.webswing.model.MsgOut;

public class JsParamMsg implements MsgOut {
	private static final long serialVersionUID = 5967292655638399242L;

	private String primitive;
	private JSObjectMsg jsObject;
	private JavaObjectRefMsg javaObject;
	private List<JsParamMsg> array;

	public String getPrimitive() {
		return primitive;
	}

	public void setPrimitive(String primitive) {
		this.primitive = primitive;
	}

	public JSObjectMsg getJsObject() {
		return jsObject;
	}

	public void setJsObject(JSObjectMsg jsObject) {
		this.jsObject = jsObject;
	}

	public JavaObjectRefMsg getJavaObject() {
		return javaObject;
	}

	public void setJavaObject(JavaObjectRefMsg javaObject) {
		this.javaObject = javaObject;
	}

	public List<JsParamMsg> getArray() {
		return array;
	}

	public void setArray(List<JsParamMsg> array) {
		this.array = array;
	}

}

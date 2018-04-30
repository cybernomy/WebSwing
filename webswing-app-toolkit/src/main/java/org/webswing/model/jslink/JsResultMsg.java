package org.webswing.model.jslink;

import org.webswing.model.MsgIn;
import org.webswing.model.SyncMsg;

public class JsResultMsg implements MsgIn, SyncMsg {
	private static final long serialVersionUID = 8331062247770059042L;

	private String correlationId;
	private String error;
	private JsParamMsg value;

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public JsParamMsg getValue() {
		return value;
	}

	public void setValue(JsParamMsg value) {
		this.value = value;
	}

}

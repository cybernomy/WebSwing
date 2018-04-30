package org.webswing.model.internal;

import java.io.Serializable;
import java.util.UUID;

import org.webswing.model.MsgInternal;
import org.webswing.model.SyncMsg;

public class ApiCallMsgInternal implements SyncMsg, MsgInternal {
	public enum ApiMethod {
		HasRole,IsPermitted
	}

	private static final long serialVersionUID = 4470097170102688942L;
	private String correlationId = UUID.randomUUID().toString();
	private ApiMethod method;
	private Serializable[] args;
	private Serializable result;

	public ApiMethod getMethod() {
		return method;
	}

	public void setMethod(ApiMethod method) {
		this.method = method;
	}

	public Serializable[] getArgs() {
		return args;
	}

	public void setArgs(Serializable[] args) {
		this.args = args;
	}

	public Serializable getResult() {
		return result;
	}

	public void setResult(Serializable result) {
		this.result = result;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

}

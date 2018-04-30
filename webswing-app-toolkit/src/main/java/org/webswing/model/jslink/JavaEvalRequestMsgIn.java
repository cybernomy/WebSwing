package org.webswing.model.jslink;

import java.util.List;

import org.webswing.model.MsgIn;
import org.webswing.model.SyncMsg;

public class JavaEvalRequestMsgIn implements MsgIn, SyncMsg {
	private static final long serialVersionUID = 7491354280941799313L;

	private String correlationId;
	private String objectId;
	private String method;
	private List<JsParamMsg> params;

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public List<JsParamMsg> getParams() {
		return params;
	}

	public void setParams(List<JsParamMsg> params) {
		this.params = params;
	}

}

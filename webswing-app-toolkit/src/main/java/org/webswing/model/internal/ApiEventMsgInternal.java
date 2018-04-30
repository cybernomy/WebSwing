package org.webswing.model.internal;

import java.io.Serializable;

import org.webswing.model.MsgInternal;

public class ApiEventMsgInternal implements MsgInternal {
	private static final long serialVersionUID = -6912899913199681417L;

	public enum ApiEventType {
		UserConnected,
		UserDisconnected,
		MirrorViewConnected,
		MirrorViewDisconnected
	}

	private ApiEventType event;
	private Serializable[] args;

	public ApiEventMsgInternal(ApiEventType event, Serializable... args) {
		super();
		this.event = event;
		this.args = args;
	}

	public ApiEventType getEvent() {
		return event;
	}

	public void setEvent(ApiEventType event) {
		this.event = event;
	}

	public Serializable[] getArgs() {
		return args;
	}

	public void setArgs(Serializable[] args) {
		this.args = args;
	}

}

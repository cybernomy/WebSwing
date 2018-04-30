package org.webswing.model.internal;

import org.webswing.model.MsgInternal;

public class ThreadDumpMsgInternal implements MsgInternal {
	private long timestamp;
	private String dump;
	private String reason;

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getDump() {
		return dump;
	}

	public void setDump(String dump) {
		this.dump = dump;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}

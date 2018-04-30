package org.webswing.server.common.model.rest;

public class LogRequest {
	boolean backwards;
	long offset;
	long max;

	public boolean isBackwards() {
		return backwards;
	}
	public void setBackwards(boolean backwards) {
		this.backwards = backwards;
	}
	public long getOffset() {
		return offset;
	}
	public void setOffset(long offset) {
		this.offset = offset;
	}
	public long getMax() {
		return max;
	}
	public void setMax(long max) {
		this.max = max;
	}
	
	
}

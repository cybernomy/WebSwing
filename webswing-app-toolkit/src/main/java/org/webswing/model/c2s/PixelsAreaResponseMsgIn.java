package org.webswing.model.c2s;

import org.webswing.model.MsgIn;
import org.webswing.model.SyncMsg;

public class PixelsAreaResponseMsgIn implements MsgIn, SyncMsg {
	private String correlationId;
	private String pixels;

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public String getPixels() {
		return pixels;
	}

	public void setPixels(String pixels) {
		this.pixels = pixels;
	}
}

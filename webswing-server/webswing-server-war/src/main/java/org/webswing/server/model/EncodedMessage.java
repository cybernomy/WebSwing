package org.webswing.server.model;

import org.webswing.model.MsgOut;
import org.webswing.server.util.ServerUtil;

public class EncodedMessage {
	private MsgOut message;
	private String jsonMessage;
	private byte[] protoMessage;

	public EncodedMessage(MsgOut message) {
		this.message = message;
	}

	public String getJsonMessage() {
		if (jsonMessage == null) {
			jsonMessage = ServerUtil.encode2Json(message);
		}
		return jsonMessage;
	}

	public byte[] getProtoMessage() {
		if (protoMessage == null) {
			protoMessage = ServerUtil.encode2Proto(message);
		}
		return protoMessage;
	}

	public int getLength(boolean binary) {
		if (binary) {
			byte[] m = getProtoMessage();
			return m == null ? 0 : m.length;
		} else {
			String m = getJsonMessage();
			return m == null ? 0 : m.getBytes().length;
		}
	}
}

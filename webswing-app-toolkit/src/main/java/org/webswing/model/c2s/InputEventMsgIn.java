package org.webswing.model.c2s;

import org.webswing.model.MsgIn;

public class InputEventMsgIn implements MsgIn {

	private static final long serialVersionUID = -6578454357790442182L;
	private ConnectionHandshakeMsgIn handshake;
	private KeyboardEventMsgIn key;
	private MouseEventMsgIn mouse;
	private SimpleEventMsgIn event;
	private TimestampsMsgIn timestamps;

	public ConnectionHandshakeMsgIn getHandshake() {
		return handshake;
	}

	public void setHandshake(ConnectionHandshakeMsgIn handshake) {
		this.handshake = handshake;
	}

	public KeyboardEventMsgIn getKey() {
		return key;
	}

	public void setKey(KeyboardEventMsgIn key) {
		this.key = key;
	}

	public MouseEventMsgIn getMouse() {
		return mouse;
	}

	public void setMouse(MouseEventMsgIn mouse) {
		this.mouse = mouse;
	}

	public SimpleEventMsgIn getEvent() {
		return event;
	}

	public void setEvent(SimpleEventMsgIn event) {
		this.event = event;
	}

	public TimestampsMsgIn getTimestamps() {
		return timestamps;
	}

	public void setTimestamps(TimestampsMsgIn timestamps) {
		this.timestamps = timestamps;
	}

}

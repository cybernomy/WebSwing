package org.webswing.model.c2s;

import org.webswing.model.MsgIn;

public class PlaybackCommandMsgIn implements MsgIn {
	private static final long serialVersionUID = -5390735227809092937L;

	public static enum PlaybackCommand {
		reset, play, stop, step, step10, step100;
	}

	private PlaybackCommand command;

	public PlaybackCommand getCommand() {
		return command;
	}

	public void setCommand(PlaybackCommand command) {
		this.command = command;
	}

}

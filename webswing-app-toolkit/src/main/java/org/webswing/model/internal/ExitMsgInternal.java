package org.webswing.model.internal;

import org.webswing.model.MsgInternal;

public class ExitMsgInternal  implements MsgInternal{

	private static final long serialVersionUID = -8007742149401885272L;
	
	private int waitForExit;

	
	public int getWaitForExit() {
		return waitForExit;
	}

	public void setWaitForExit(int waitForExit) {
		this.waitForExit = waitForExit;
	}
	
}

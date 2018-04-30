package org.webswing.model.s2c;

import java.util.ArrayList;
import java.util.List;

import org.webswing.model.MsgOut;
import org.webswing.model.jslink.JsEvalRequestMsgOut;
import org.webswing.model.jslink.JsResultMsg;

public class AppFrameMsgOut implements MsgOut {

	private static final long serialVersionUID = 6019708608380425820L;
	private List<ApplicationInfoMsg> applications;
	private LinkActionMsg linkAction;
	private WindowMoveActionMsg moveAction;
	private CursorChangeEventMsg cursorChange;
	private CopyEventMsg copyEvent;
	private PasteRequestMsg pasteRequest;
	private FileDialogEventMsg fileDialogEvent;
	private List<WindowMsg> windows;
	private WindowMsg closedWindow;
	private SimpleEventMsgOut event;
	private PixelsAreaRequestMsgOut pixelsRequest;
	private JsEvalRequestMsgOut jsRequest;
	private JsResultMsg javaResponse;
	private PlaybackInfoMsg playback;
	private String sessionId;
	private String startTimestamp = "" + System.currentTimeMillis();
	private String sendTimestamp;

	public AppFrameMsgOut() {
	}
	
	public WindowMsg getOrCreateWindowById(String guid) {
		if (windows != null) {
			for (WindowMsg w : windows) {
				if (w.getId().equals(guid)) {
					return w;
				}
			}
		} else {
			windows = new ArrayList<WindowMsg>();
		}
		WindowMsg window = new WindowMsg();
		window.setId(guid);
		windows.add(window);
		return window;
	}

	public PasteRequestMsg getPasteRequest() {
		return pasteRequest;
	}

	public void setPasteRequest(PasteRequestMsg pasteRequest) {
		this.pasteRequest = pasteRequest;
	}

	public List<ApplicationInfoMsg> getApplications() {
		return applications;
	}

	public void setApplications(List<ApplicationInfoMsg> applications) {
		this.applications = applications;
	}

	public LinkActionMsg getLinkAction() {
		return linkAction;
	}

	public void setLinkAction(LinkActionMsg linkAction) {
		this.linkAction = linkAction;
	}

	public WindowMoveActionMsg getMoveAction() {
		return moveAction;
	}

	public void setMoveAction(WindowMoveActionMsg moveAction) {
		this.moveAction = moveAction;
	}

	public CursorChangeEventMsg getCursorChange() {
		return cursorChange;
	}

	public void setCursorChange(CursorChangeEventMsg cursorChange) {
		this.cursorChange = cursorChange;
	}

	public CopyEventMsg getCopyEvent() {
		return copyEvent;
	}

	public void setCopyEvent(CopyEventMsg copyEvent) {
		this.copyEvent = copyEvent;
	}

	public FileDialogEventMsg getFileDialogEvent() {
		return fileDialogEvent;
	}

	public void setFileDialogEvent(FileDialogEventMsg fileDialogEvent) {
		this.fileDialogEvent = fileDialogEvent;
	}

	public List<WindowMsg> getWindows() {
		return windows;
	}

	public void setWindows(List<WindowMsg> windows) {
		this.windows = windows;
	}

	public WindowMsg getClosedWindow() {
		return closedWindow;
	}

	public void setClosedWindow(WindowMsg closedWindow) {
		this.closedWindow = closedWindow;
	}

	public SimpleEventMsgOut getEvent() {
		return event;
	}

	public void setEvent(SimpleEventMsgOut event) {
		this.event = event;
	}

	public JsEvalRequestMsgOut getJsRequest() {
		return jsRequest;
	}

	public void setJsRequest(JsEvalRequestMsgOut jsRequest) {
		this.jsRequest = jsRequest;
	}

	public JsResultMsg getJavaResponse() {
		return javaResponse;
	}

	public void setJavaResponse(JsResultMsg javaResponse) {
		this.javaResponse = javaResponse;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public PlaybackInfoMsg getPlayback() {
		return playback;
	}

	public void setPlayback(PlaybackInfoMsg playback) {
		this.playback = playback;
	}

	public String getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(String startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public String getSendTimestamp() {
		return sendTimestamp;
	}

	public void setSendTimestamp(String sendTimestamp) {
		this.sendTimestamp = sendTimestamp;
	}

	public PixelsAreaRequestMsgOut getPixelsRequest() {
		return pixelsRequest;
	}

	public void setPixelsRequest(PixelsAreaRequestMsgOut pixelsRequest) {
		this.pixelsRequest = pixelsRequest;
	}
}

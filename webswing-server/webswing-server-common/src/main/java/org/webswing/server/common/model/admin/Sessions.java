package org.webswing.server.common.model.admin;

import java.util.ArrayList;
import java.util.List;

public class Sessions {
	private List<SwingSession> sessions = new ArrayList<SwingSession>();
	private List<SwingSession> closedSessions = new ArrayList<SwingSession>();

	public List<SwingSession> getSessions() {
		return sessions;
	}

	public void setSessions(List<SwingSession> sessions) {
		this.sessions = sessions;
	}

	public List<SwingSession> getClosedSessions() {
		return closedSessions;
	}

	public void setClosedSessions(List<SwingSession> closedSessions) {
		this.closedSessions = closedSessions;
	}

}

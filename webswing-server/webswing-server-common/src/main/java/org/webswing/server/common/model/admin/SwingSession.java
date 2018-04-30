package org.webswing.server.common.model.admin;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SwingSession implements Serializable {

	private static final long serialVersionUID = 147477596803123012L;
	private String id;
	private String user;
	private String userIp;
	private String userOs;
	private String userBrowser;
	private String application;
	private Date startedAt;
	private Date endedAt;
	private Boolean connected;
	private Boolean applet;
	private Date disconnectedSince;
	private Boolean recorded;
	private String recordingFile;
	private Map<String, Map<Long, Number>> stats;
	private Map<String, Number> metrics;
	private SwingInstanceStatus status;
	private List<String> warnings;
	private List<String> warningHistory;
	private Map<Long,String> threadDumps;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public Boolean getConnected() {
		return connected;
	}

	public void setConnected(Boolean connected) {
		this.connected = connected;
	}

	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}

	public Date getDisconnectedSince() {
		return disconnectedSince;
	}

	public void setDisconnectedSince(Date disconnectedSince) {
		this.disconnectedSince = disconnectedSince;
	}

	public Map<String, Map<Long, Number>> getStats() {
		return stats;
	}

	public void setStats(Map<String, Map<Long, Number>> state) {
		this.stats = state;
	}

	public Date getEndedAt() {
		return endedAt;
	}

	public void setEndedAt(Date endedAt) {
		this.endedAt = endedAt;
	}

	public Boolean getApplet() {
		return applet;
	}

	public void setApplet(Boolean applet) {
		this.applet = applet;
	}

	public Boolean getRecorded() {
		return recorded;
	}

	public void setRecorded(Boolean recorded) {
		this.recorded = recorded;
	}

	public String getRecordingFile() {
		return recordingFile;
	}

	public void setRecordingFile(String recordingFile) {
		this.recordingFile = recordingFile;
	}

	public SwingInstanceStatus getStatus() {
		return status;
	}

	public void setStatus(SwingInstanceStatus status) {
		this.status = status;
	}

	public Map<String, Number> getMetrics() {
		return metrics;
	}

	public void setMetrics(Map<String, Number> metrics) {
		this.metrics = metrics;
	}

	public void setWarnings(List<String> instanceWarnings) {
		this.warnings = instanceWarnings;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public String getUserIp() {
		return userIp;
	}

	public void setUserIp(String userIp) {
		this.userIp = userIp;
	}

	public String getUserOs() {
		return userOs;
	}

	public void setUserOs(String userOs) {
		this.userOs = userOs;
	}

	public String getUserBrowser() {
		return userBrowser;
	}

	public void setUserBrowser(String userBrowser) {
		this.userBrowser = userBrowser;
	}

	public List<String> getWarningHistory() {
		return warningHistory;
	}

	public void setWarningHistory(List<String> warningHistory) {
		this.warningHistory = warningHistory;
	}

	public Map<Long, String> getThreadDumps() {
		return threadDumps;
	}

	public void setThreadDumps(Map<Long, String> threadDumps) {
		this.threadDumps = threadDumps;
	}
}

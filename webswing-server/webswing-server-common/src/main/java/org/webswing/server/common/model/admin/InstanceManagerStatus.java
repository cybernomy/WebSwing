package org.webswing.server.common.model.admin;

import java.io.Serializable;

public class InstanceManagerStatus implements Serializable {

	private static final long serialVersionUID = -5972092124551316038L;

	public enum Status {
		Starting,
		Running,
		Stopped,
		Stopping,
		Error;
	}

	private Status status = Status.Stopped;
	private String error;
	private String errorDetails;

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getErrorDetails() {
		return errorDetails;
	}

	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
	}

}

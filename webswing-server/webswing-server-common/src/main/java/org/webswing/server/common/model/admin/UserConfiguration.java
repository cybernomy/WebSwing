package org.webswing.server.common.model.admin;

public class UserConfiguration {
	private String users;

	public UserConfiguration() {
	}

	public UserConfiguration(String users) {
		super();
		this.users = users;
	}

	public String getUsers() {
		return users;
	}

	public void setUsers(String users) {
		this.users = users;
	}

}

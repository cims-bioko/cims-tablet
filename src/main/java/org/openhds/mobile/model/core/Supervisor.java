package org.openhds.mobile.model.core;

import java.io.Serializable;

/**
 * A Supervisor is someone capable of downloading partial forms
 */
public class Supervisor implements Serializable {

	private long id;
	private String name;
	private String password;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
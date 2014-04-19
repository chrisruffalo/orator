package com.github.chrisruffalo.orator.model;

import java.util.Date;

public class Lock {

	private String id;
	
	private String owner;
	
	private Date at;
	
	private Date until;
	
	// this is the session key to look inside for the
	// details of the lock and NOT NOT NOT NOT NOT the
	// key required to deactivate the lock.
	private String key;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Date getAt() {
		return at;
	}

	public void setAt(Date at) {
		this.at = at;
	}

	public Date getUntil() {
		return until;
	}

	public void setUntil(Date until) {
		this.until = until;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}	
	
}

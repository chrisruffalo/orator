package com.github.chrisruffalo.orator.model;

public class ReadingSession {

	private String id;
	
	private String bookId;
	
	private String sessionName;
	
	private long secondsOffset;
	
	private String owner;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSessionName() {
		return sessionName;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	public long getSecondsOffset() {
		return secondsOffset;
	}

	public void setSecondsOffset(long secondsOffset) {
		this.secondsOffset = secondsOffset;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}	
	
}

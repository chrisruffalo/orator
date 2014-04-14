package com.github.chrisruffalo.orator.model;

import com.google.gson.annotations.Expose;

public class ReadingSession {

	@Expose
	private String id;
	
	@Expose
	private String bookId;
	
	@Expose
	private String sessionName;
	
	@Expose
	private long secondsOffset;
	
	@Expose
	private String owner;
	
	private AudioBook book;

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

	public AudioBook getBook() {
		return book;
	}

	public void setBook(AudioBook book) {
		this.book = book;
	}	
}

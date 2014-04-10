package com.github.chrisruffalo.orator.model;

import java.util.List;

public class AudioBook {

	private String id;
	
	private String title;
	
	private String author;
	
	private boolean hidden;
	
	private String owner;
	
	private List<BookFile> bookFiles;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public List<BookFile> getBookFiles() {
		return bookFiles;
	}

	public void setBookFiles(List<BookFile> bookFiles) {
		this.bookFiles = bookFiles;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

}

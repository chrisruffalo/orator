package com.github.chrisruffalo.orator.model;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.annotations.Expose;

public class AudioBook {

	private String id;
	
	private String title;
	
	private String author;
	
	private boolean hidden;
	
	private String owner;
	
	@Expose
	private List<BookTrack> bookTracks;
	
	public AudioBook() {
		this.bookTracks = new LinkedList<BookTrack>();
	}

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

	public List<BookTrack> getBookTracks() {
		return bookTracks;
	}

	public void setBookTracks(List<BookTrack> bookTracks) {
		this.bookTracks = bookTracks;
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

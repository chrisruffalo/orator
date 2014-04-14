package com.github.chrisruffalo.orator.model;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.annotations.Expose;

public class AudioBook {

	@Expose
	private String id;
	
	@Expose
	private String title;
	
	@Expose
	private String author;
	
	@Expose
	private boolean hidden;
	
	@Expose
	private long time;
	
	@Expose
	private long size;
	
	@Expose
	private String owner;
	
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

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void calculateStats() {
		long time = 0;
		long size = 0;
		for(BookTrack track : this.getBookTracks()) {
			time += track.getLengthSeconds();
			size += track.getBytesSize();
		}
		this.time = time;
		this.size = size;
	}

	public BookTrack getBookTrack(String trackId) {
		if(trackId == null || trackId.isEmpty()) {
			return null;
		}
		
		for(BookTrack track : this.bookTracks) {
			if(trackId.equalsIgnoreCase(track.getId())) {
				return track;
			}
		}
		return null;
	}

}

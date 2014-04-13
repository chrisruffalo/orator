package com.github.chrisruffalo.orator.model;

public class BookTrack {

	private String id;
	
	private String fileName;
	
	private String path;
	
	private long bitsPerSecond;
	
	private long bytesSize;
	
	private int lengthSeconds;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getBitsPerSecond() {
		return bitsPerSecond;
	}

	public void setBitsPerSecond(long bitsPerSecond) {
		this.bitsPerSecond = bitsPerSecond;
	}

	public long getBytesSize() {
		return bytesSize;
	}

	public void setBytesSize(long bytesSize) {
		this.bytesSize = bytesSize;
	}

	public int getLengthSeconds() {
		return lengthSeconds;
	}

	public void setLengthSeconds(int lengthSeconds) {
		this.lengthSeconds = lengthSeconds;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
}

package com.github.chrisruffalo.orator.exceptions;

public class OratorFileReadException extends OratorRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OratorFileReadException(String message) {
		super(message);
	}
	
	public OratorFileReadException(String message, Throwable cause) {
		super(message, cause);
	}

}

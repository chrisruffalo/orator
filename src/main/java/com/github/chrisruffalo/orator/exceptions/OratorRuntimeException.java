package com.github.chrisruffalo.orator.exceptions;

public class OratorRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public OratorRuntimeException(String message) {
		super(message);
	}
	
	public OratorRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	
}

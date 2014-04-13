package com.github.chrisruffalo.orator.core.util;

import java.util.UUID;

public final class IdUtil {

	private IdUtil() {
		
	}
	
	public static String get() {
		String id = UUID.randomUUID().toString();
		id = id.replaceAll("-", "");
		id = id.toLowerCase();
		return id;
	}
	
}

package com.github.chrisruffalo.orator.core.util;

import org.apache.shiro.subject.Subject;

import com.google.common.base.Objects;

public final class SubjectUtil {

	private SubjectUtil() {
		
	}
	
	public static boolean is(Subject subject, String username) {
		return subject != null && subject.getPrincipal() != null && Objects.equal(username, subject.getPrincipal());
	}
	
	public static String name(Subject subject) {
		if(subject == null || subject.getPrincipal() == null) {
			return null;
		}
		return subject.getPrincipal().toString();
	}
	
}

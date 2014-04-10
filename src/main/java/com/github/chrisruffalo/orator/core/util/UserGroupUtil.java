package com.github.chrisruffalo.orator.core.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;

import com.github.chrisruffalo.orator.exceptions.OratorRuntimeException;

public final class UserGroupUtil {

	private UserGroupUtil() {
		
	}
	
	public static UserPrincipal systemUser() {
		String systemUser = System.getProperty("user.name");
		
		UserPrincipalLookupService service = FileSystems.getDefault().getUserPrincipalLookupService();
		try {
			UserPrincipal userPrincipal = service.lookupPrincipalByName(systemUser);
			return userPrincipal;
		} catch (IOException e) {
			throw new OratorRuntimeException("Could not look up principal for user '" + String.valueOf(systemUser) + "'", e);
		}
	}

}

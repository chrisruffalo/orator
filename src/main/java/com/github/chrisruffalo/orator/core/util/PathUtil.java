package com.github.chrisruffalo.orator.core.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.github.chrisruffalo.orator.exceptions.OratorFileReadException;
import com.google.common.base.Optional;


public final class PathUtil {

	private PathUtil() {
		
	}
	
	public static Path getDirectoryPath(String type, Path targetPath) {
		return PathUtil.getDirectoryPath(type, targetPath, true);
	}
	
	public static Path getDirectoryPath(String type, Path targetPath, boolean create) {
		Path path = targetPath.normalize();
		if(create && Files.exists(path) && !Files.isDirectory(path)) {
			try {
				boolean result = Files.deleteIfExists(path);
				if(!result) {
					Optional<OratorFileReadException> option = PathUtil.analyzePermissions(path);
					if(option.isPresent()) {
						throw option.get();
					}
				}
			} catch (IOException e) {
				Optional<OratorFileReadException> option = PathUtil.analyzePermissions(path);
				if(option.isPresent()) {
					throw option.get();
				} else {
					throw new OratorFileReadException("There is a file at path '" + path.toString() + "' that is blocking the creation of a [" + type + "] directory", e);
				}
			}
		}		
		if(create && !Files.exists(path)) {
			try {
				path = Files.createDirectories(path);
			} catch (IOException e) {
				Optional<OratorFileReadException> option = PathUtil.analyzePermissions(path);
				if(option.isPresent()) {
					throw option.get();
				} else {
					throw new OratorFileReadException("A directory at '" + path.toString() + "' for [" + type + "] could not be created", e);
				}				
			}
		}
		
		return path;
	}
	
	public static Optional<OratorFileReadException> analyzePermissions(Path path) {
		Optional<OratorFileReadException> optionalException = Optional.absent();
		boolean isDir = Files.isDirectory(path);
		
		UserPrincipal systemUser = UserGroupUtil.systemUser();
		
		try {
			Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
			UserPrincipal fileOwner = Files.getOwner(path);

			List<String> missingPermissionStrings = new LinkedList<String>();
			
			// check user name
			if(fileOwner.getName().equals(systemUser)) {
				// make sure that we can read and write the file
				if(!permissions.contains(PosixFilePermission.OWNER_READ)) {
					missingPermissionStrings.add("owner read");
				} 
				if(!permissions.contains(PosixFilePermission.OWNER_WRITE)) {
					missingPermissionStrings.add("owner write");
				}
				if(isDir && !permissions.contains(PosixFilePermission.OWNER_EXECUTE)) {
					missingPermissionStrings.add("owner view");
				}
			/*} else if(OWNER GROUP IS SAME AS CURRENT USER GROUP) { // todo: group permissions
				if(!permissions.contains(PosixFilePermission.GROUP_READ)) {
					missingPermissionStrings.add("group read");
				} 
				if(!permissions.contains(PosixFilePermission.GROUP_WRITE)) {
					missingPermissionStrings.add("group write");
				}
				if(isDir && !permissions.contains(PosixFilePermission.GROUP_EXECUTE)) {
					missingPermissionStrings.add("group view");
				} */
			} else {
				// check permissions for OTHERS
				if(!permissions.contains(PosixFilePermission.OTHERS_READ)) {
					missingPermissionStrings.add("others read");
				} 
				if(!permissions.contains(PosixFilePermission.OTHERS_WRITE)) {
					missingPermissionStrings.add("others write");
				}
				if(isDir && !permissions.contains(PosixFilePermission.OTHERS_EXECUTE)) {
					missingPermissionStrings.add("other view");
				}
			}
			
			// if there are permissions that are required, then we need to create an error
			// message (exception) for the caller to analyze
			if(!missingPermissionStrings.isEmpty()) {
				StringBuilder builder = new StringBuilder();
				
				builder.append("The path '");
				builder.append(path.toString());
				builder.append("' was missing the permissions: ");
				builder.append(StringUtils.join(missingPermissionStrings, ", "));
				builder.append(" which might be needed for access.  Correct the permission problems and try again.");
				
				// create exception
				optionalException = Optional.of(new OratorFileReadException(builder.toString()));
			}
			
		} catch (IOException e) {
			optionalException = Optional.of(new OratorFileReadException("Could not get permissions for file at path '" + String.valueOf(path) + "'; check permissions ", e));
		}
		
	
		return optionalException;
	}
	
}

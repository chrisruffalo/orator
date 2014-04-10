package com.github.chrisruffalo.orator.core.providers;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;

import com.github.chrisruffalo.orator.core.util.PathUtil;

@ApplicationScoped
public class ReadingSessionProvider {

	private static final String SESSION_PATH = "sessions";
	
	@Inject
	private Configuration configuration;
	
	public Path getSessionsPath() {
		String homePath = this.configuration.getString(ConfigurationProvider.KEY_HOME_DIR, ConfigurationProvider.DEFAULT_HOME_DIR);
		Path path = Paths.get(homePath, ReadingSessionProvider.SESSION_PATH);
		return PathUtil.getDirectoryPath("sessions", path);
	}
	
	public Path getUserSessionDir(String userName) {
		Path sessions = this.getSessionsPath();
		Path userSessions = sessions.resolve(userName);
		return PathUtil.getDirectoryPath("user sessions", userSessions);		
	}
	
	
}

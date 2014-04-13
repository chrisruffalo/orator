package com.github.chrisruffalo.orator.core.providers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;

import com.github.chrisruffalo.eeconfig.annotations.Logging;
import com.github.chrisruffalo.orator.core.util.IdUtil;
import com.github.chrisruffalo.orator.model.AudioBook;
import com.github.chrisruffalo.orator.model.ReadingSession;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@RequestScoped
public class UserReadingSessionProvider {

	@Inject
	@Logging
	private Logger logger;
	
	@Inject
	private Subject subject;
	
	@Inject
	private ReadingSessionProvider reading;
	
	@Inject
	private AudioBookProvider books;
	
	public List<ReadingSession> getSessions() {
		String userName = this.subject.getPrincipal().toString();
		Path toSessions = this.reading.getUserSessionDir(userName);
		
		// get files
		List<ReadingSession> readingSessions = new LinkedList<ReadingSession>(); 
		DirectoryStream.Filter<Path> jsonFileFilter = new DirectoryStream.Filter<Path>() {
			@Override
			public boolean accept(Path entry) throws IOException {
				return Files.isRegularFile(entry) && entry.endsWith(".json");
			}		
		};
		
		// stream list of files
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(toSessions, jsonFileFilter)) {
			for(Path file : stream) {
				ReadingSession session = this.getSession(file);
				if(session != null) {
					readingSessions.add(session);
				}
			}
		} catch (IOException e) {
			this.logger.error("Error while reading sessions for user '{}': {}", userName, e.getMessage());
		}
		
		// todo: sort by most recent used first, then by creation time
		
		return readingSessions;
	}	
	
	public ReadingSession startSession(String bookId) {
		AudioBook book = this.books.getBook(bookId);
		if(book == null) {
			// todo: do better
			throw new WebApplicationException(404);
		}
		
		// create id
		String id = IdUtil.get();

		// get username
		String userName = this.subject.getPrincipal().toString();
		
		// create session
		ReadingSession session = new ReadingSession();
		session.setId(id);
		session.setBookId(bookId);
		session.setOwner(userName);
		session.setSecondsOffset(0);
		session.setSessionName("New Session");
		
		// write session to file
		Path toSessions = this.reading.getUserSessionDir(userName);
		Path sessionPath = toSessions.resolve("session-" + id + ".json");
		try (BufferedWriter writer = Files.newBufferedWriter(sessionPath, Charset.defaultCharset())) {
			Gson gson = new Gson();
			gson.toJson(session, writer);
		} catch (IOException e) {
			this.logger.error("Error while writing new session for user '{}': {}", userName, e.getMessage());
			throw new WebApplicationException(500);
		}		
		
		return session;
	}
	
	public boolean lockSession(String id) {
		ReadingSession session = this.getSession(id);
		if(session == null) {
			return false;
		}
		// get username
		String userName = this.subject.getPrincipal().toString();
		
		// path to lock
		Path toSessions = this.reading.getUserSessionDir(userName);
		Path lockPath = toSessions.resolve("session-" + id + ".lock");
		
		// a lock exists.. eventually we will need to garbage collect
		// these here, with some sort of session time out
		if(Files.exists(lockPath)) {
			return false;
		}
		
		// write
		try {
			Files.write(lockPath, String.valueOf(System.currentTimeMillis()).getBytes());
			return true;
		} catch (IOException e) {
			this.logger.error("Could not write lock file for session id '{}': {}", id, e.getMessage());
		}
		
		// if it falls to here then we couldn't lock the file
		return false;
	}
	
	public ReadingSession getSession(String id) {
		// get username
		String userName = this.subject.getPrincipal().toString();
		
		// write session to file
		Path toSessions = this.reading.getUserSessionDir(userName);
		Path sessionPath = toSessions.resolve("session-" + id + ".json");
		
		ReadingSession session = this.getSession(sessionPath);
		return session;
	}
	
	private ReadingSession getSession(Path sessionPath) {
		try(Reader reader = Files.newBufferedReader(sessionPath, Charset.defaultCharset())){
			Gson gson = new Gson();
			ReadingSession session = gson.fromJson(reader, ReadingSession.class);
			return session;
		} catch (JsonSyntaxException jsex) { 
			// warn
			this.logger.warn("The book file '{}' has malformed json and could not be opened", sessionPath.toString());
		} catch (IOException e) {
			// warn
			this.logger.warn("The book file '{}' could not be read", sessionPath.toString());
		}
		return null;
	}
}

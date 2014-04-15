package com.github.chrisruffalo.orator.server.services.impl;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.chrisruffalo.orator.core.providers.UserReadingSessionProvider;
import com.github.chrisruffalo.orator.model.ReadingSession;

@Path("/secured/reading")
@Produces({MediaType.APPLICATION_JSON})
public class UserReadingSessionService {

	@Inject
	private UserReadingSessionProvider provider;
	
	@GET
	@Path("/sessions")
	public List<ReadingSession> getSessions() {
		return this.provider.getSessions();
	}
	
	@GET
	@Path("/{bookId}/start") 
	public ReadingSession startSession(@PathParam("bookId") String bookId){
		return this.provider.startSession(bookId);
	}
	
	@GET
	@Path("/{sessionId}") 
	public ReadingSession getSession(@PathParam("sessionId") String sessionId){
		return this.provider.getSession(sessionId);
	}
	
	@DELETE
	@Path("/{sessionId}/delete") 
	public ReadingSession deleteSession(@PathParam("sessionId") String sessionId){
		// todo: implement
		return null;
	}
}

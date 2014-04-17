package com.github.chrisruffalo.orator.server.services.impl;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.github.chrisruffalo.eeconfig.annotations.Logging;
import com.github.chrisruffalo.orator.core.providers.UserReadingSessionProvider;
import com.github.chrisruffalo.orator.model.ReadingSession;

@Path("/secured/reading")
@Produces({MediaType.APPLICATION_JSON})
public class UserReadingSessionService {

	@Inject
	private UserReadingSessionProvider provider;
	
	@Inject
	@Logging
	private Logger logger;
	
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
	public List<ReadingSession> deleteSession(@PathParam("sessionId") String sessionId){
		return this.provider.deleteSession(sessionId);
	}	
	
	@PUT
	@Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
	@Path("/{sessionId}/update/{trackId}")
	public ReadingSession updateSession(@PathParam("sessionId") String sessionId, @PathParam("trackId") String trackId, String seconds) {
		// get seconds
		long secondsParsed = 0;
		try {
			secondsParsed = (new BigDecimal(seconds)).longValue();
		} catch (Exception ex) {
			this.logger.warn("Could not parse seconds offset to update session info: {}", ex.getLocalizedMessage());
		}
		
		// no change if seconds is less than 1
		if(secondsParsed < 1) {
			return this.provider.getSession(sessionId);
		}
		
		// change and return
		return this.provider.updateSession(sessionId, trackId, secondsParsed);
	}
}

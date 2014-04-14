package com.github.chrisruffalo.orator.server.services.impl;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.github.chrisruffalo.orator.core.providers.AudioBookProvider;
import com.github.chrisruffalo.orator.model.AudioBook;

@Path("/secured/books")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class AudioBookService {

	@Inject
	private AudioBookProvider provider;
	
	@GET
	@Path("/all")
	@Produces({MediaType.APPLICATION_JSON})
	public List<AudioBook> getBooks() {
		return this.provider.getBooks();
	}
	
	@GET
	@Path("/{bookId}")
	public AudioBook getBook(@PathParam("bookId") String bookId) {
		return this.provider.getBook(bookId);
	}
	
	@DELETE
	@Path("/{bookId}/delete")
	public List<AudioBook> deleteBook(@PathParam("bookId") String bookId) {
		return this.provider.deleteBook(bookId);
	}

	@DELETE
	@Path("/{bookId}/deleteTrack/{trackId}")
	public AudioBook deleteTrack(@PathParam("bookId") String bookId, @PathParam("trackId") String trackId) {
		return this.provider.deleteTrack(bookId, trackId);
	}
	
	@POST
	@PUT
	@Path("/save")
	public AudioBook saveBook(AudioBook book, @QueryParam("updateTracks") @DefaultValue("true") Boolean updateTracks) {
		return this.provider.saveBook(book, updateTracks);
	}
	
}

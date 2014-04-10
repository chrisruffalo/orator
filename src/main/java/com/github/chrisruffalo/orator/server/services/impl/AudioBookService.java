package com.github.chrisruffalo.orator.server.services.impl;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.chrisruffalo.orator.core.providers.AudioBookProvider;
import com.github.chrisruffalo.orator.model.AudioBook;

@Path("/secured/books")
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
	@Produces({MediaType.APPLICATION_JSON})
	public AudioBook getBook(@PathParam("bookId") String bookId) {
		return this.provider.getBook(bookId);
	}

	@GET
	@Path("/save")
	public AudioBook saveBook(AudioBook book) {
		return this.provider.saveBook(book);
	}
	
}

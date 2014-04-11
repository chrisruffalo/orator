package com.github.chrisruffalo.orator.core.providers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.configuration.Configuration;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;

import com.github.chrisruffalo.eeconfig.annotations.Logging;
import com.github.chrisruffalo.orator.core.util.PathUtil;
import com.github.chrisruffalo.orator.core.util.SubjectUtil;
import com.github.chrisruffalo.orator.model.AudioBook;
import com.google.gson.Gson;

@ApplicationScoped
public class AudioBookProvider {

	private static final String BOOKS_PATH = "books";
	
	@Inject
	private Configuration configuration;
	
	@Inject
	@Logging
	private Logger logger;
	
	@Inject
	private Subject subject;
	
	public AudioBook getBook(String bookId) {
		// go to books dir
		String homePath = this.configuration.getString(ConfigurationProvider.KEY_HOME_DIR, ConfigurationProvider.DEFAULT_HOME_DIR);
		Path path = Paths.get(homePath, AudioBookProvider.BOOKS_PATH);
		Path bookPath = path.resolve(bookId);
		bookPath = PathUtil.getDirectoryPath("books", bookPath);
		
		// look for file
		Path bookDescriptor = bookPath.resolve("book.json");
		
		// if it doesn't exist or isn't a real file, return null
		if(!Files.exists(bookDescriptor) || !Files.isRegularFile(bookDescriptor)) {
			this.logger.warn("there is a book (or empty directory) with missing metadata at: '{}'", bookDescriptor.toString());
			// todo: scan?
			return null;
		}

		// read and put in gson
		try {
			Reader reader = Files.newBufferedReader(bookDescriptor, Charset.defaultCharset());
			Gson gson = new Gson();
			AudioBook book = gson.fromJson(reader, AudioBook.class);
			// do null thing if book is hidden and owner is not the current user principal
			if(book.isHidden() && !SubjectUtil.is(this.subject, book.getOwner())) {
				return null;
			}
			return book;
		} catch (IOException e) {
			// or error out
			this.logger.error("Could not read audio book descriptor at path: {}", e.getLocalizedMessage(), e);
			return null;
		}
	}
	
	public AudioBook saveBook(AudioBook book) {
		
		// do nothing
		if(book == null) {
			return book;
		}
		
		// create id if it is missing
		String id = book.getId();
		if(id == null || id.isEmpty()) {
			id = UUID.randomUUID().toString();
			id = id.replaceAll("-", "");
			id.toLowerCase();
			book.setId(id);
		}

		// get username
		String userName = this.subject.getPrincipal().toString();

		// go to books dir
		String homePath = this.configuration.getString(ConfigurationProvider.KEY_HOME_DIR, ConfigurationProvider.DEFAULT_HOME_DIR);
		Path path = Paths.get(homePath, AudioBookProvider.BOOKS_PATH);
		Path bookPath = path.resolve(id);
		bookPath = PathUtil.getDirectoryPath("books", bookPath);
		
		// look for file
		Path bookDescriptor = bookPath.resolve("book.json");
		try {
			Files.deleteIfExists(bookDescriptor);
		} catch (IOException e) {
			this.logger.error("Error while deleting old book descriptor for user '{}': {}", userName, e.getMessage());
			throw new WebApplicationException(500);
		}
		
		// save book to file
		try (BufferedWriter writer = Files.newBufferedWriter(bookDescriptor, Charset.defaultCharset())) {
			Gson gson = new Gson();
			gson.toJson(book, writer);
		} catch (IOException e) {
			this.logger.error("Error while saving book for user '{}': {}", userName, e.getMessage());
			throw new WebApplicationException(500);
		}
		
		return book;
	}
	
	public List<AudioBook> getBooks() {
		// go to books dir
		String homePath = this.configuration.getString(ConfigurationProvider.KEY_HOME_DIR, ConfigurationProvider.DEFAULT_HOME_DIR);
		Path path = Paths.get(homePath, AudioBookProvider.BOOKS_PATH);
		Path booksPath = PathUtil.getDirectoryPath("books", path);
		
		// get files
		List<AudioBook> bookList = new LinkedList<AudioBook>(); 
		DirectoryStream.Filter<Path> directoryFilter = new DirectoryStream.Filter<Path>() {
			@Override
			public boolean accept(Path entry) throws IOException {
				return Files.isDirectory(entry);
			}		
		};
		
		// stream list of directories
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(booksPath, directoryFilter)) {
			for(Path file : stream) {
				String id = file.getFileName().toString();
				AudioBook book = this.getBook(id);				
				if(book != null) {
					bookList.add(book);
				}
			}
		} catch (IOException e) {
			this.logger.error("Error while reading books from path '{}': {}", booksPath.toString(), e.getMessage());
		}
		
		return bookList;
	}
	
}

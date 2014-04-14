package com.github.chrisruffalo.orator.core.providers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;

import com.github.chrisruffalo.eeconfig.annotations.Logging;
import com.github.chrisruffalo.orator.core.util.AudioMetadataUtil;
import com.github.chrisruffalo.orator.core.util.IdUtil;
import com.github.chrisruffalo.orator.core.util.PathUtil;
import com.github.chrisruffalo.orator.core.util.SubjectUtil;
import com.github.chrisruffalo.orator.exceptions.OratorRuntimeException;
import com.github.chrisruffalo.orator.model.AudioBook;
import com.github.chrisruffalo.orator.model.BookTrack;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
	
	public AudioBook saveBook(AudioBook book, boolean updateTracks) {
		// do nothing
		if(book == null) {
			return book;
		}
		
		// create id if it is missing
		String id = book.getId();
		if(id == null || id.isEmpty()) {
			id = IdUtil.get();
			book.setId(id);
		}

		// update username if it doesn't exist
		String exsistingUserName = book.getOwner();
		// get username
		String userName = SubjectUtil.name(this.subject);
		if(exsistingUserName == null || exsistingUserName.isEmpty()) {
			book.setOwner(userName);
		} else if(book.isHidden() && !SubjectUtil.is(this.subject, exsistingUserName)) {
			throw new OratorRuntimeException("User " + userName + " cannot save a HIDDEN book that belongs to user " + exsistingUserName);
		}
		
		// update book stats when updating tracks
		if(updateTracks) { 
			book.calculateStats();
		}

		// go to books dir
		Path bookPath = this.getBookPath(id);

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
			GsonBuilder builder = new GsonBuilder();
			if(!updateTracks) {
				builder.excludeFieldsWithoutExposeAnnotation();
			}				
			Gson gson = builder.create();
			gson.toJson(book, writer);
		} catch (IOException e) {
			this.logger.error("Error while saving book for user '{}': {}", userName, e.getMessage());
			throw new WebApplicationException(500);
		}

		return book;
	}
	
	public AudioBook saveBook(AudioBook book) {
		return this.saveBook(book, true);
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
	
	public boolean addBookTrack(String bookId, String fileName, String contentType, InputStream bookFileStream) {
		if(fileName == null || fileName.isEmpty()) {
			return false;
		}
			
		AudioBook book = this.getBook(bookId);
		if(book == null || book.getId() == null || book.getId().isEmpty()) {
			return false;
		}

		// create new track
		BookTrack track = new BookTrack();
		String trackId = IdUtil.get();
		
		//set metadata
		track.setId(trackId);
		track.setFileName(fileName);
		track.setPath(trackId + "_" + fileName); // we do this to prevent future name conflicts.  the file name never changes.
		track.setContentType(contentType);
		
		// write book to  book path
		Path bookPath = this.getBookPath(bookId);
		Path filePath = bookPath.resolve(track.getPath());
		
		// write file
		try(OutputStream output = Files.newOutputStream(filePath)) {
			ByteStreams.copy(bookFileStream, output);
		} catch (IOException e) {
			this.logger.warn("Could not write file: {} (reason: {})", filePath.toString(), e.getLocalizedMessage());
					
			// delete, the act of opening the stream creates the file 
			try {
				Files.deleteIfExists(filePath);
			} catch (IOException e1) {
				// it doesn't matter
			}
			return false;
		}
		
		// process metadata (and set it)
		AudioMetadataUtil.metadata(track, filePath);
		
		// file size
		try {
			track.setBytesSize(Files.size(filePath));
		} catch (IOException e) {
			// what do?
			e.printStackTrace();
		}
		
		// add track to book
		book.getBookTracks().add(track);
		
		// save book metadata once everything else is successful
		this.saveBook(book);
		
		return true;
	}
	
	public AudioBook deleteTrack(String bookId, String trackId) {
		// if no track provided, return null
		if(trackId == null || trackId.isEmpty()) {
			return null;
		}
		
		// get and check book
		AudioBook book = this.getBook(bookId);
		if(book == null || book.getId() == null || book.getId().isEmpty()) {
			return null;
		}

		Iterator<BookTrack> iterator = book.getBookTracks().iterator();
		while(iterator.hasNext()) {
			BookTrack track = iterator.next();
			// if the track is found
			if(trackId.equalsIgnoreCase(track.getId())) {
				// remove from iterator
				iterator.remove();
				
				// save (so that it disappears from the metadata)
				this.saveBook(book);
				
				// get path to track
				Path bookPath = this.getBookPath(bookId);
				Path trackPath = bookPath.resolve(track.getPath());
				
				// delete with result
				try {
					Files.deleteIfExists(trackPath);
				} catch (IOException e) {
					this.logger.warn("Could not delete book file at: {}", trackPath);
				}
				
				// deleted
				return book;
			}
		}
		
		// nothing deleted
		return book;
	}
	
	public List<AudioBook> deleteBook(String bookId) {
		Path bookPath = this.getBookPath(bookId);
		
		try {
			FileUtils.deleteDirectory(bookPath.toFile());
		} catch (IOException e) {
			this.logger.error("Could not delete path '{}' with error: {}", bookPath, e.getMessage(), e);
		}
		
		return this.getBooks();
	}
	
	public Path getBookPath(String bookId) {
		String homePath = this.configuration.getString(ConfigurationProvider.KEY_HOME_DIR, ConfigurationProvider.DEFAULT_HOME_DIR);
		Path path = Paths.get(homePath, AudioBookProvider.BOOKS_PATH);
		Path bookPath = path.resolve(bookId);
		bookPath = PathUtil.getDirectoryPath("books", bookPath);
		bookPath = bookPath.normalize();
		return bookPath;
	}
	
}

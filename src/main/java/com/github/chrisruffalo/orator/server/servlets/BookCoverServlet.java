package com.github.chrisruffalo.orator.server.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.github.chrisruffalo.eeconfig.annotations.Logging;
import com.github.chrisruffalo.orator.core.providers.AudioBookProvider;
import com.github.chrisruffalo.orator.model.AudioBook;
import com.google.common.io.ByteStreams;

@WebServlet(
	displayName="cover", 
	asyncSupported=true, 
	description="upload audio book covers", 
	name="cover", 
	urlPatterns={
		"/services/secured/cover",
		"/services/secured/cover*"
	}
)
public class BookCoverServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Inject
	private AudioBookProvider provider;
	
	@Inject
	@Logging
	private Logger logger;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String bookId = request.getParameter("bookId");
		if(bookId == null || bookId.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "a valid book id is required to get a book cover");
			return;
		}

		// get what the file name needs to be
		AudioBook book = this.provider.getBook(bookId);
		if(book == null) {
			this.logger.error("The book id:{} was not found", bookId);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "a valid (existing) book id is required to download a cover");
			return;
		}
		
		// get byte channel
		ByteChannel coverChannel = this.provider.getCover(bookId);
		
		// get output stream
		OutputStream outputStream = response.getOutputStream();
		
		// if no channel found, redirect to asset
		if(coverChannel == null) {
			this.logger.warn("no cover set for book id:{}", bookId);
			response.sendRedirect(request.getContextPath() + "/assets/book.jpg");
			return;
		}
		
		// get output channel
		WritableByteChannel output = Channels.newChannel(outputStream);
		
		// write
		long length = ByteStreams.copy(coverChannel, output);
		
		// done
		long size = Files.size(this.provider.getBookPath(bookId));
		if(length < size) {
			this.logger.warn("only wrote {} of {} bytes for book id:{}", length, size, bookId);
			response.sendRedirect(request.getContextPath() + "/assets/book.jpg");
		}
		
		return;
	}	
	
}

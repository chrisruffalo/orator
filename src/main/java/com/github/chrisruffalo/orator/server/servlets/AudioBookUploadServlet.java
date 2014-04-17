package com.github.chrisruffalo.orator.server.servlets;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;

import com.github.chrisruffalo.eeconfig.annotations.Logging;
import com.github.chrisruffalo.orator.core.providers.AudioBookProvider;
import com.github.chrisruffalo.orator.model.AudioBook;

@WebServlet(
	displayName="audioBookUpload", 
	asyncSupported=true, 
	description="upload audio books", 
	name="audioBookUpload", 
	urlPatterns={
		"/services/secured/audioBookUpload",
		"/services/secured/audioBookUpload*"
	}
)
// originally from: https://github.com/danialfarid/angular-file-upload/blob/master/demo/src/com/df/angularfileupload/FileUpload.java
public class AudioBookUploadServlet extends HttpServlet {

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
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String bookId = req.getParameter("bookId");
		if(bookId == null || bookId.isEmpty()) {
			res.sendError(HttpServletResponse.SC_BAD_REQUEST, "a book id is required to upload an audiobook file");
			return;
		}

		// get what the file name needs to be
		AudioBook book = this.provider.getBook(bookId);
		if(book == null) {
			this.logger.error("The book id:{} was not found", bookId);
			res.sendError(HttpServletResponse.SC_BAD_REQUEST, "a book valid (existing) book id is required to upload an audiobook file");
			return;
		}
		
		try {
			if (req.getHeader("Content-Type") != null && req.getHeader("Content-Type").startsWith("multipart/form-data")) {
				
				ServletFileUpload upload = new ServletFileUpload();
				upload.setFileSizeMax(2147483648l); //2GB, some files are big!

				FileItemIterator iterator = upload.getItemIterator(req);

				while (iterator.hasNext()) {
					FileItemStream item = iterator.next();
					
					// this is a file
					if(item.getName() != null) {
						String fileName = item.getName();
						InputStream bookStream = item.openStream();
						String contentType = item.getContentType();
						
						this.logger.trace("Got book stream for fileName: {}", fileName);
						
						if(bookStream != null) {						
							// add book track to book
							this.provider.addBookTrack(bookId, fileName, contentType, bookStream);
							
							// wrote
							this.logger.trace("Wrote book stream for fileName: {}", fileName);
						}
					} 					
				}
			}
			// write response
			res.getWriter().write("done");
		} catch (Exception ex) {
			this.logger.warn("The stream was ended unexpectedly, probably a user-abort");
			//throw new ServletException(ex);
		}
	}	
}

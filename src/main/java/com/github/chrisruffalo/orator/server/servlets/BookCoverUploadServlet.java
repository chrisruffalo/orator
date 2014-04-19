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
	displayName="audioBookCoverUpload", 
	asyncSupported=true, 
	description="upload audio book cover", 
	name="audioBookCoverUpload", 
	urlPatterns={
		"/services/secured/audioBookCoverUpload",
		"/services/secured/audioBookCoverUpload*"
	}
)
// originally from: https://github.com/danialfarid/angular-file-upload/blob/master/demo/src/com/df/angularfileupload/FileUpload.java
public class BookCoverUploadServlet extends HttpServlet {

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
			res.sendError(HttpServletResponse.SC_BAD_REQUEST, "a book id is required to upload an cover file");
			return;
		}

		// get what the file name needs to be
		AudioBook book = this.provider.getBook(bookId);
		if(book == null) {
			this.logger.error("The book id:{} was not found", bookId);
			res.sendError(HttpServletResponse.SC_BAD_REQUEST, "a book valid (existing) book id is required to upload a cover file");
			return;
		}
		
		try {
			if (req.getHeader("Content-Type") != null && req.getHeader("Content-Type").startsWith("multipart/form-data")) {
				
				ServletFileUpload upload = new ServletFileUpload();
				upload.setFileSizeMax(52428800); //50MB, don't go crazy on image sizes

				FileItemIterator iterator = upload.getItemIterator(req);

				while (iterator.hasNext()) {
					FileItemStream item = iterator.next();
					
					// this is a file
					if(item.getName() != null) {
						InputStream coverStream = item.openStream();
						String contentType = item.getContentType();
						
						if(coverStream != null) {						
							// add book track to book
							boolean result = this.provider.addCover(bookId, contentType, coverStream);
							if(!result) {
								this.logger.error("Error while writing cover file for book id:{}", bookId);
								res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal error while adding cover file " + item.getName());
								return;			
							}
							
							// wrote
							this.logger.trace("Wrote cover for book id:{}", bookId);
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

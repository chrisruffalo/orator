package com.github.chrisruffalo.orator.server.servlets;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.github.chrisruffalo.eeconfig.annotations.Logging;
import com.github.chrisruffalo.orator.server.servlets.support.BookStreamHandler;

@WebServlet(
	displayName="oration", 
	asyncSupported=true, 
	description="orates books", 
	name="oration", 
	urlPatterns={
		"/orate",
		"/orate*"
	}
)
public class BookStreamServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Inject
	@Logging
	private Logger logger;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.execute(request, response);
	}	

	/**
	 * Implement request logic
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// create async context		
		AsyncContext asyncContext = request.startAsync();
	
		// create stream handler for the book
		BookStreamHandler handler = asyncContext.createListener(BookStreamHandler.class);
		handler.init(asyncContext);
		asyncContext.addListener(handler);	
	}	
}

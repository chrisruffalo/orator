package com.github.chrisruffalo.orator.server.servlets.support;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ReadListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

import com.github.chrisruffalo.eeconfig.annotations.Logging;
import com.github.chrisruffalo.orator.core.providers.AudioBookProvider;
import com.github.chrisruffalo.orator.core.providers.UserReadingSessionProvider;
import com.github.chrisruffalo.orator.model.AudioBook;
import com.github.chrisruffalo.orator.model.ReadingSession;

@RequestScoped
public class BookStreamHandler implements ReadListener, WriteListener, AsyncListener {
	
	static final String QUERY_PARAM_SESSION_ID = "sessionId";
	
	private AsyncContext context;
	
	private final CountDownLatch waitForReadLatch;
	
	private final byte[] reusableReadBuffer;
	
	private boolean writing;
	
	private AudioBook book;
	
	private ReadingSession session;
	
	@Inject
	private AudioBookProvider bookProvider;
	
	@Inject
	private UserReadingSessionProvider sessionProvider;
	
	@Inject
	private Principal principal;
	
	@Inject
	@Logging
	private Logger logger;
	
	public BookStreamHandler() {
		// set up
		this.waitForReadLatch = new CountDownLatch(1);
		this.reusableReadBuffer = new byte[256];
		this.writing = false;
	}
	
	public void init(AsyncContext asyncContext) throws IOException {
		// read values from session
		this.logger.trace("starting initialization...");
		
		// set up context
		this.context = asyncContext;
		
		// in/out
		ServletRequest request = this.context.getRequest();
		ServletResponse response = this.context.getResponse();
		
		// set up listeners
		request.getInputStream().setReadListener(this);
		response.getOutputStream().setWriteListener(this);
		
		// if no content is provided, unlock the writer without waiting
		if(request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			if("GET".equalsIgnoreCase(httpRequest.getMethod())) {
				this.waitForReadLatch.countDown();
			}
		}
		if(0 >= request.getContentLength()) {
			this.waitForReadLatch.countDown();
		}
		
		// configure read handler from session
		String sessionId = request.getParameter(BookStreamHandler.QUERY_PARAM_SESSION_ID);
		
		// check for valid values
		if(sessionId == null || sessionId.isEmpty()) {
			this.logger.warn("Attempted to start a streaming/reading session for user '{}' with null sessionId", this.principal.getName());
			this.context.complete();
			return;
		}
		
		ReadingSession session = this.sessionProvider.getSession(sessionId);
		if(session == null) {
			this.logger.warn("Attempted to start a streaming/reading session for user '{}' with but no session '{}' was found", this.principal.getName(), sessionId);
			this.context.complete();
			return;
		}
		
		AudioBook book = this.bookProvider.getBook(session.getBookId());
		if(book == null) {
			this.logger.warn("Attempted to start a streaming/reading session for user '{}' but no attached book was found", this.principal.getName());
			this.context.complete();
			return;
		}
		
		// now save values so we can stream the book
		this.session = session;
		this.book = book;
		
		// read values from session
		this.logger.trace("initialized");
	}

	@Override
	public void onWritePossible() throws IOException {
		this.logger.trace("waiting for read to finish...");
		
		// wait until read is done...
		try {
			this.waitForReadLatch.await(1, TimeUnit.MINUTES);
			this.writing = true;
		} catch (InterruptedException e) {
			this.context.complete();
			this.logger.error("Could not start writing book stream");
			return;
		}
		
		// write
		this.context.getResponse().getOutputStream().write(this.session.getId().getBytes());
		
		this.logger.trace("done writing");
		
		// done writing
		this.context.complete();
	}

	@Override
	public void onAllDataRead() throws IOException {
		// decrement the latch to allow writing to start
		this.waitForReadLatch.countDown();
	}

	@Override
	public void onDataAvailable() throws IOException {
		// noop read, data goes NOWHERE, buffer is only allocated once for "speed"
		ServletRequest request = this.context.getRequest();
		InputStream stream = request.getInputStream();
		
		// drain
		while(stream.read(this.reusableReadBuffer) > 0) {
			// reading !
		}
	}

	@Override
	public void onError(Throwable arg0) {
		String mode = this.writing ? "writing" : "reading";
		this.logger.error("Error while {}: {}", mode, arg0.getLocalizedMessage());		
		if(!this.writing) {
			// end context if not writing
			this.context.complete();
		}
	}

	@Override
	public void onComplete(AsyncEvent arg0) throws IOException {
		this.logger.trace("complete");
	}

	@Override
	public void onError(AsyncEvent arg0) throws IOException {
		this.logger.error("error: {}", arg0.getThrowable().getLocalizedMessage());
		
		// on context error (async error event) close the context
		this.context.complete();
	}

	@Override
	public void onStartAsync(AsyncEvent arg0) throws IOException {
		this.logger.trace("starting...");
	}

	@Override
	public void onTimeout(AsyncEvent arg0) throws IOException {
		// no op
	}

}

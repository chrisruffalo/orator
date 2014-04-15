package com.github.chrisruffalo.orator.server.servlets.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ReadListener;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;

import com.github.chrisruffalo.eeconfig.annotations.Logging;
import com.github.chrisruffalo.orator.core.providers.AudioBookProvider;
import com.github.chrisruffalo.orator.core.providers.UserReadingSessionProvider;
import com.github.chrisruffalo.orator.core.util.SubjectUtil;
import com.github.chrisruffalo.orator.model.AudioBook;
import com.github.chrisruffalo.orator.model.BookTrack;
import com.github.chrisruffalo.orator.model.ReadingSession;

// with help from: http://balusc.blogspot.com/2009/02/fileservlet-supporting-resume-and.html
@RequestScoped
public class BookStreamHandler implements ReadListener, WriteListener, AsyncListener {
	// Parameter keys
	private static final String QUERY_PARAM_SESSION_ID = "sessionId";
	private static final String QUERY_PARAM_TRACK_ID = "trackId";
	
	// Constants ----------------------------------------------------------------------------------

    private static final int DEFAULT_BUFFER_SIZE = 10240; // ..bytes = 10KB.
    //private static final long DEFAULT_EXPIRE_TIME = 604800000L; // ..ms = 1 week.
    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";
	
    /**
     * This class represents a byte range.
     */
    protected class Range {
	    long start;
	    long end;
	    long length;
	    long total;
	
	    /**
	     * Construct a byte range.
	     * @param start Start of the byte range.
	     * @param end End of the byte range.
	     * @param total Total length of the byte source.
	     */
	    public Range(long start, long end, long total) {
	        this.start = start;
	        this.end = end;
	        this.length = end - start + 1;
	        this.total = total;
	    }
    }

	
	private AsyncContext context;
	
	private final CountDownLatch waitForReadLatch;
	
	private final byte[] reusableReadBuffer;
	
	private boolean writing;
	
	//private AudioBook book;
	
	//private ReadingSession session;
	
	//private String trackId;
	
	private Path trackPath;
	
	private BookTrack track;
	
	private RandomAccessFile trackFile;
	
	private Queue<Range> dataRanges;

	private Range currentRange;
	
	private int ranges;
	
	@Inject
	private AudioBookProvider bookProvider;
	
	@Inject
	private UserReadingSessionProvider sessionProvider;
	
	@Inject
	private Subject subject;
	
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
		HttpServletRequest request = (HttpServletRequest)this.context.getRequest();
		HttpServletResponse response = (HttpServletResponse)this.context.getResponse();
		
		// set up listeners
		request.getInputStream().setReadListener(this);
		response.getOutputStream().setWriteListener(this);
		
		// configure read handler from session
		String sessionId = request.getParameter(BookStreamHandler.QUERY_PARAM_SESSION_ID);
		String trackId = request.getParameter(BookStreamHandler.QUERY_PARAM_TRACK_ID);
		String userName = SubjectUtil.name(this.subject);		
		
		// check for valid values
		if(sessionId == null || sessionId.isEmpty()) {
			this.logger.warn("Attempted to start a streaming/reading session for user '{}' with null sessionId", userName);
			this.context.complete();
			return;
		}
		
		ReadingSession session = this.sessionProvider.getSession(sessionId);
		if(session == null) {
			this.logger.warn("Attempted to start a streaming/reading session for user '{}' with but no session '{}' was found", userName, sessionId);
			this.context.complete();
			return;
		}
		
		AudioBook book = this.bookProvider.getBook(session.getBookId());
		if(book == null) {
			this.logger.warn("Attempted to start a streaming/reading session for user '{}' but no attached book was found", userName);
			this.context.complete();
			return;
		}
		
		// no tracks = no play
		if(book.getBookTracks() == null || book.getBookTracks().isEmpty()) {
			this.logger.warn("Attempted to start a streaming/reading session for user '{}' but the book (id:{}) had no track information", userName, book.getId());
			this.context.complete();
			return;
		}
		
		// get track id of first track and start from there
		if(trackId == null || trackId.isEmpty()) {
			trackId = book.getBookTracks().get(0).getId();
		}
		
		// get track
		BookTrack track = book.getBookTrack(trackId);
		
		// get path to track
		Path bookPath = this.bookProvider.getBookPath(book.getId());
		Path trackPath = bookPath.resolve(track.getPath());
		
		if(!Files.exists(trackPath) || !Files.isRegularFile(trackPath)) {
			this.logger.warn("Attempted to start a streaming/reading session for user '{}' but track ({}) for book (id:{}) had no track data (files)", new Object[]{
				userName,
				trackId,
				book.getId()
			});
			this.context.complete();
			return;
		}
		
		// log
		this.logger.info("Starting streaming session for user '{}' (book: {})", userName, book.getId());
		
		// now save values so we can stream the book
		//this.session = session;
		//this.book = book;
		this.trackPath = trackPath;
		this.track = track;
		this.trackFile = new RandomAccessFile(this.trackPath.toFile(), "r");
		
		// let the response headers show that we support byte ranges
		response.addHeader("Accept-Ranges", "bytes");
		response.setHeader("Content-Disposition", "inline ;filename=\"" + track.getPath() + "\"");
		response.setContentType(track.getContentType());
		response.getOutputStream().flush(); // flush header?
				
		// if no content is provided, unlock the writer without waiting
		if("GET".equalsIgnoreCase(request.getMethod())) {
			this.waitForReadLatch.countDown();
		}
		if(0 >= request.getContentLength()) {
			this.waitForReadLatch.countDown();
		}
		
		// get base length of file
		long length = Files.size(this.trackPath);
		
		// calculate start and stop bytes
		Range full = new Range(0, length - 1, length);
        this.dataRanges = (Queue<Range>)new LinkedList<Range>();
        this.currentRange = null;

        // Validate and process Range and If-Range headers.
        String range = request.getHeader("Range");
        if (range != null) {
        	this.logger.info("processing range header: '{}'", range);
        	
            // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
            if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
                response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }

            // process byte ranges
            if (this.dataRanges.isEmpty()) {
                for (String part : range.substring(6).split(",")) {
                    // assuming a file with length of 100, the following examples returns bytes at:
                    // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100)
                    long start = BookStreamHandler.sublong(part, 0, part.indexOf("-"));
                    long end = BookStreamHandler.sublong(part, part.indexOf("-") + 1, part.length());

                    if (start == -1) {
                        start = length - end;
                        end = length - 1;
                    } else if (end == -1 || end > length - 1) {
                        end = length - 1;
                    }

                    // check if Range is syntactically valid. If not, then return 416.
                    if (start > end) {
                        response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                        response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return;
                    }

                    this.logger.info("adding range starting at: {}", start);
                    
                    // add range
                    this.dataRanges.add(new Range(start, end, length));
                }
                
                if(this.dataRanges.isEmpty()) {
                	this.logger.info("adding full range");
                	this.dataRanges.add(full);
                }
            }
        } else {
        	this.logger.info("no range provided: using full range");
        	this.dataRanges.add(full);
        }
        this.ranges = this.dataRanges.size();
        this.logger.info("ranges added: {}", this.ranges);		
        
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
		
		// response
		HttpServletResponse response = (HttpServletResponse)this.context.getResponse();
		ServletOutputStream outputStream = (ServletOutputStream)response.getOutputStream();
		
		if(this.currentRange == null) {
			this.currentRange = this.dataRanges.poll();
			
			// show that range is partial
			if(this.ranges > 1) {
				// responses
				response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.	
				response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
				// set start boundary on stream
				outputStream.println();
				outputStream.println("--" + MULTIPART_BOUNDARY);
				outputStream.println("Content-Type: " + this.track.getContentType());
				outputStream.println("Content-Range: bytes " + this.currentRange.start + "-" + this.currentRange.end + "/" + this.currentRange.total);
			} else {
				// set single range response header
				long size = Files.size(this.trackPath);
				this.logger.info("wanting to write a total of: {} bytes", size);
				response.setContentLengthLong(size);
				response.setHeader("Content-Range", "bytes " + this.currentRange.start + "-" + this.currentRange.end + "/" + this.currentRange.total);	
			}
		}

		// do write
		long length = this.currentRange.total;
		this.logger.info("writing {} bytes", length);
		BookStreamHandler.copy(this.trackFile, outputStream, this.currentRange.start, length);
		this.currentRange.start += length; // advance length by copied amount
		this.logger.info("range is now {} - {} of {}", new Object[]{this.currentRange.start, this.currentRange.end, this.currentRange.total});

		// range is ending
		if(this.currentRange.start >= this.currentRange.end) {
			this.currentRange = null;
			// print boundary close if required
			if(this.ranges > 1) {
				outputStream.println();
				outputStream.println("--" + MULTIPART_BOUNDARY + "--");
			}
		}

		// flush, just in case
		outputStream.flush();
		
		this.logger.info("flushed!");
		
		// finished when current range is complete and there are no more data ranges
		if(this.currentRange == null && (this.dataRanges == null || this.dataRanges.isEmpty())) {
			
			this.logger.info("mark complete!");
			
			this.context.complete();
		}		
		
		this.logger.info("current call done!");
		
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
		this.finishBookStream();
	}

	@Override
	public void onError(AsyncEvent arg0) throws IOException {
		this.logger.error("error: {}", arg0.getThrowable().getLocalizedMessage());
		
		// close input stream
		this.finishBookStream();
		
		// on context error (async error event) close the context
		this.context.complete();
	}

	@Override
	public void onStartAsync(AsyncEvent arg0) throws IOException {
		this.logger.trace("starting...");
	}

	@Override
	public void onTimeout(AsyncEvent arg0) throws IOException {
		this.finishBookStream();
	}
	
	private void finishBookStream() {
		
	}

	// ============================= HELPERS ============================= 
	
	/**
     * Returns a substring of the given string value from the given begin index to the given end
     * index as a long. If the substring is empty, then -1 will be returned
     * @param value The string value to return a substring as long for.
     * @param beginIndex The begin index of the substring to be returned as long.
     * @param endIndex The end index of the substring to be returned as long.
     * @return A substring of the given string value as long or -1 if substring is empty.
     */
    private static long sublong(String value, int beginIndex, int endIndex) {
        String substring = value.substring(beginIndex, endIndex);
        return (substring.length() > 0) ? Long.parseLong(substring) : -1;
    }
    
    /**
     * Copy the given byte range of the given input to the given output.
     * @param input The input to copy the given range to the given output for.
     * @param output The output to copy the given range from the given input for.
     * @param start Start of the byte range.
     * @param length Length of the byte range.
     * @throws IOException If something fails at I/O level.
     */
    private static void copy(RandomAccessFile input, OutputStream output, long start, long length) throws IOException {
    	int bufferSize = BookStreamHandler.DEFAULT_BUFFER_SIZE;
    	if(length < bufferSize) {
    		bufferSize = (int)length;
    	}
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;

        if (input.length() == length) {
            // Write full range.
            while ((read = input.read(buffer)) > 0) {
                output.write(buffer, 0, read);
            }
        } else {
            // Write partial range.
            input.seek(start);
            long toRead = length;

            while ((read = input.read(buffer)) > 0) {
                if ((toRead -= read) > 0) {
                    output.write(buffer, 0, read);
                } else {
                    output.write(buffer, 0, (int) toRead + read);
                    break;
                }
            }
        }
    }
}

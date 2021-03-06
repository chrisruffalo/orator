package com.github.chrisruffalo.orator.server.servlets;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
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

/**
 * A file servlet supporting MP3 streaming for audio books
 *
 * @author BalusC
 * @link http://balusc.blogspot.com/2009/02/fileservlet-supporting-resume-and.html
 */
@WebServlet(
	displayName="oration", 
	asyncSupported=true, 
	description="orates books", 
	name="oration", 
	urlPatterns={
		"/services/secured/orate",
		"/services/secured/orate*"
	}
)
// idea/basic implementation from : http://balusc.blogspot.com/2009/02/fileservlet-supporting-resume-and.html
public class AudioBookStreamServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Parameter keys
	private static final String QUERY_PARAM_SESSION_ID = "sessionId";
	private static final String QUERY_PARAM_TRACK_ID = "trackId";
	
	// Constants ----------------------------------------------------------------------------------

	private static final int DEFAULT_BUFFER_SIZE = 32768; // in bytes, 32kb
    private static final long DEFAULT_EXPIRE_TIME = 604800000L; // in ms, 1 week
    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

    // Actions ------------------------------------------------------------------------------------

    // injections
    @Inject
    private Subject subject;
    
    @Inject
    @Logging
    private Logger logger;
    
	@Inject
	private AudioBookProvider bookProvider;
	
	@Inject
	private UserReadingSessionProvider sessionProvider;
    
    /**
     * Initialize the servlet.
     * @see HttpServlet#init().
     */
    public void init() throws ServletException {
    	
    }

    /**
     * Process HEAD request. This returns the same headers as GET request, but without content.
     * @see HttpServlet#doHead(HttpServletRequest, HttpServletResponse).
     */
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        // Process request without content.
        processRequest(request, response, false);
    }

    /**
     * Process GET request.
     * @see HttpServlet#doGet(HttpServletRequest, HttpServletResponse).
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        // Process request with content.
        processRequest(request, response, true);
    }

    /**
     * Process the actual request.
     * @param request The request to be processed.
     * @param response The response to be created.
     * @param content Whether the request body should be written (GET) or not (HEAD).
     * @throws IOException If something fails at I/O level.
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response, boolean content) throws IOException {
    	// configure read handler from session
		String sessionId = request.getParameter(AudioBookStreamServlet.QUERY_PARAM_SESSION_ID);
		String trackId = request.getParameter(AudioBookStreamServlet.QUERY_PARAM_TRACK_ID);
		String userName = SubjectUtil.name(this.subject);		
		
		// check for valid values
		if(sessionId == null || sessionId.isEmpty()) {
			this.logger.warn("Attempted to start a streaming/reading session for user '{}' with null sessionId", userName);
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "null session could not be found");
			return;
		}
		
		ReadingSession session = this.sessionProvider.getSession(sessionId);
		if(session == null) {
			this.logger.warn("Attempted to start a streaming/reading session for user '{}' with but no session '{}' was found", userName, sessionId);
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "session " + sessionId + " was not found");
			return;
		}
		
		AudioBook book = this.bookProvider.getBook(session.getBookId());
		if(book == null) {
			this.logger.warn("Attempted to start a streaming/reading session for user '{}' but no attached book was found", userName);
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "book " + session.getBookId() + " was not found");
			return;
		}
		
		// no tracks = no play
		if(book.getBookTracks() == null || book.getBookTracks().isEmpty()) {
			this.logger.warn("Attempted to start a streaming/reading session for user '{}' but the book (id:{}) had no track information", userName, book.getId());
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "book " + book.getId() + " has no data");
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
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "track " + trackId + " has no data");
			return;
		}
		
		// log
		this.logger.info("Starting streaming session for user '{}' (book: {} - {})", userName, book.getId(), book.getTitle());    	
        
        // Check if file actually exists in filesystem.
        if (!Files.exists(trackPath) || !Files.isRegularFile(trackPath)) {
            // Do your thing if the file appears to be non-existing.
            // Throw an exception, or send 404, or show default/warning page, or just ignore it.
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Prepare some variables. The ETag is an unique identifier of the file.
        String fileName = track.getPath();
        long length = track.getBytesSize();
        long lastModified = Files.getLastModifiedTime(trackPath).toMillis();
        String eTag = fileName + "_" + length + "_" + lastModified;
        long expires = System.currentTimeMillis() + DEFAULT_EXPIRE_TIME;

        // Validate request headers for caching ---------------------------------------------------

        // If-None-Match header should contain "*" or ETag. If so, then return 304.
        String ifNoneMatch = request.getHeader("If-None-Match");
        if (ifNoneMatch != null && matches(ifNoneMatch, eTag)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            response.setHeader("ETag", eTag); // Required in 304.
            response.setDateHeader("Expires", expires); // Postpone cache with 1 week.
            this.logger.warn("ended session with a NOT_MODIFIED");
            return;
        }

        // If-Modified-Since header should be greater than LastModified. If so, then return 304.
        // This header is ignored if any If-None-Match header is specified.
        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        if (ifNoneMatch == null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModified) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            response.setHeader("ETag", eTag); // Required in 304.
            response.setDateHeader("Expires", expires); // Postpone cache with 1 week.
            this.logger.warn("ended session with a NOT_MODIFIED");
            return;
        }

        // Validate request headers for resume ----------------------------------------------------

        // If-Match header should contain "*" or ETag. If not, then return 412.
        String ifMatch = request.getHeader("If-Match");
        if (ifMatch != null && !matches(ifMatch, eTag)) {
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
            return;
        }

        // If-Unmodified-Since header should be greater than LastModified. If not, then return 412.
        long ifUnmodifiedSince = request.getDateHeader("If-Unmodified-Since");
        if (ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModified) {
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
            return;
        }

        // Validate and process range -------------------------------------------------------------

        // Prepare some variables. The full Range represents the complete file.
        Range full = new Range(0, length - 1, length);
        List<Range> ranges = new ArrayList<Range>();

        // Validate and process Range and If-Range headers.
        String range = request.getHeader("Range");
        if (range != null) {

            // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
            if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
                response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }

            // If-Range header should either match ETag or be greater then LastModified. If not,
            // then return full file.
            String ifRange = request.getHeader("If-Range");
            if (ifRange != null && !ifRange.equals(eTag)) {
                try {
                    long ifRangeTime = request.getDateHeader("If-Range"); // Throws IAE if invalid.
                    if (ifRangeTime != -1 && ifRangeTime + 1000 < lastModified) {
                    	this.logger.trace("request full range: {} to {} / {}", full.start, full.end, full.total);
                        ranges.add(full);
                    }
                } catch (IllegalArgumentException ignore) {
                    ranges.add(full);
                }
            }

            // If any valid If-Range header, then process each part of byte range.
            if (ranges.isEmpty()) {
                for (String part : range.substring(6).split(",")) {
                    // Assuming a file with length of 100, the following examples returns bytes at:
                    // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
                    long start = sublong(part, 0, part.indexOf("-"));
                    long end = sublong(part, part.indexOf("-") + 1, part.length());

                    if (start == -1) {
                        start = length - end;
                        end = length - 1;
                    } else if (end == -1 || end > length - 1) {
                        end = length - 1;
                    }

                    // Check if Range is syntactically valid. If not, then return 416.
                    if (start > end) {
                        response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                        response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return;
                    }

                    // Add range.
                    this.logger.trace("request range: {} to {} / {}", start, end, length);
                    ranges.add(new Range(start, end, length));
                }
            }
        }


        // Prepare and initialize response --------------------------------------------------------

        // Get content type by file name and set default GZIP support and content disposition.
        String contentType = track.getContentType();
        String disposition = "inline";

        // If content type is unknown, then set the default value.
        // For all content types, see: http://www.w3schools.com/media/media_mimeref.asp
        // To add new content types, add new mime-mapping entry in web.xml.
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // Initialize response.
        response.reset();
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setHeader("Content-Disposition", disposition + ";filename=\"" + fileName + "\"");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("ETag", eTag);
        response.setDateHeader("Last-Modified", lastModified);
        response.setDateHeader("Expires", expires);


        // Send requested file (part(s)) to client ------------------------------------------------

        // Prepare streams.
        OutputStream output = null;

        long written = 0;
        try {
            output = response.getOutputStream();

            if (ranges.isEmpty() || ranges.get(0) == full) {

                // Return full file.
                Range r = full;
                response.setContentType(contentType);
                response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);

                if (content) {
                    response.setHeader("Content-Length", String.valueOf(r.length));

                    // Copy full range.
                    written += nioCopy(trackPath, output, r);
                }

            } else if (ranges.size() == 1) {

                // Return single part of file.
                Range r = ranges.get(0);
                response.setContentType(contentType);
                response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
                response.setHeader("Content-Length", String.valueOf(r.length));
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                if (content) {
                    // Copy single part range.
                    written += nioCopy(trackPath, output, r);
                }

            } else {

                // Return multiple parts of file.
                response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                if (content) {
                    // Cast back to ServletOutputStream to get the easy println methods.
                    ServletOutputStream sos = (ServletOutputStream) output;

                    // Copy multi part range.
                    for (Range r : ranges) {
                        // Add multipart boundary and header fields for every range.
                        sos.println();
                        sos.println("--" + MULTIPART_BOUNDARY);
                        sos.println("Content-Type: " + contentType);
                        sos.println("Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total);

                        // Copy single part range of multi part range.
                        written += nioCopy(trackPath, output, r);
                    }

                    // End with multipart boundary.
                    sos.println();
                    sos.println("--" + MULTIPART_BOUNDARY + "--");
                }
            }
        } finally {
            // Gently close streams.
            close(output);
        }
        
        this.logger.trace("finished (wrote {} bytes)", written);
    }

    // Helpers (can be refactored to public utility class) ----------------------------------------

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
     * Returns true if the given match header matches the given value.
     * @param matchHeader The match header.
     * @param toMatch The value to be matched.
     * @return True if the given match header matches the given value.
     */
    private static boolean matches(String matchHeader, String toMatch) {
        String[] matchValues = matchHeader.split("\\s*,\\s*");
        Arrays.sort(matchValues);
        return Arrays.binarySearch(matchValues, toMatch) > -1
            || Arrays.binarySearch(matchValues, "*") > -1;
    }
    
    /**
     * Uses NIO to copy a file into the output buffer
     * 
     * @param inputFile the file to copy
     * @param output the output stream to copy to
     * @param start start byte offset of the file
     * @param length end length
     * @return the number of bytes written
     * @throws IOException
     */
    private static long nioCopy(Path inputFile, OutputStream output, Range range) throws IOException {
    	// no bytes written
    	long written = 0;
    	
    	// open file channel
		try  (FileChannel inputChannel = FileChannel.open(inputFile, StandardOpenOption.READ)) {

			// create writable channel from output stream
			WritableByteChannel outputChannel = Channels.newChannel(output);
			
			//LoggerFactory.getLogger("nio-copy").info("writing {} to {} ({})", range.start, range.end, range.length);
			
			// transfer the start to end of the input channel to the output channel
			written = inputChannel.transferTo(range.start, range.length, outputChannel);
			
			//LoggerFactory.getLogger("nio-copy").info("wrote {}", written);

		} catch (IOException ex) {
			// todo: warn
			throw ex;
		} catch (Exception ex) {
			throw ex;
		}
		
		// return written length
		return written;
    }

    /**
     * Close the given resource.
     * @param resource The resource to be closed.
     */
    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException ignore) {
                // Ignore IOException. If you want to handle this anyway, it might be useful to know
                // that this will generally only be thrown when the client aborted the request.
            }
        }
    }

    // Inner classes ------------------------------------------------------------------------------

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

}

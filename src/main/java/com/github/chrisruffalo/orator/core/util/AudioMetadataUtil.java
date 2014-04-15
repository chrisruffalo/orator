package com.github.chrisruffalo.orator.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.chrisruffalo.orator.model.BookTrack;

public final class AudioMetadataUtil {
	
	private static ContentHandler HANDLER = new DefaultHandler();
	
	private AudioMetadataUtil() {
		
	}
	
	/**
	 * Determines and sets the metadata on the given book track
	 * 
	 * @param track
	 */
	public static void metadata(BookTrack track, Path fullPath) {
		// set up tika 
		ContentHandler handler = AudioMetadataUtil.HANDLER;
		Metadata metadata = new Metadata();
		ParseContext parseCtx = new ParseContext();
		
		// get correct parser
		final Parser parser = new AutoDetectParser();
		
		// get file size
		long size = 0;
		try {
			size = Files.size(fullPath);
		} catch (IOException e1) {
			// nothing to do
		}
		
		// parse file
		try (InputStream fileInputStream = Files.newInputStream(fullPath)) {
			parser.parse(fileInputStream, handler, metadata, parseCtx);
			
			// show all metadata
			//for(String key : metadata.names()) {
			//	LoggerFactory.getLogger("metadata").info("{} => {}", key, metadata.getValues(key));
			//}
			
			// find duration
			String lengthString = metadata.get(XMPDM.DURATION);
			BigDecimal length = new BigDecimal(lengthString);
			length = length.divide(BigDecimal.valueOf(1000l)); // run time is in milliseconds, convert to seconds
			long duration = length.longValue();
			track.setLengthSeconds(duration);
			
			// calcluate bitrate
			if(size > 0 && duration > 0) {
				long sizeInBits = size * 8;
				long bitrate = sizeInBits / duration;
				track.setBitsPerSecond(bitrate);
			}
			
		} catch (IOException e) {
			// nothing to do here, just won't have metadata
		} catch (SAXException | TikaException e) {
			// error with parser (?)
			e.printStackTrace();
		}
		
	}

}

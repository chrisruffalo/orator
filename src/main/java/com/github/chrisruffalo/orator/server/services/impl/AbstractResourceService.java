package com.github.chrisruffalo.orator.server.services.impl;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

import org.slf4j.Logger;

import com.github.chrisruffalo.eeconfig.annotations.Logging;
import com.github.chrisruffalo.orator.exceptions.OratorRuntimeException;

public abstract class AbstractResourceService {
	

	@Context
	private HttpServletRequest request;

	@Inject
	@Logging
	private Logger logger;

	@GET
	@Path("/{id}/qr") 
	public Response qr(@PathParam("id") String id) {
		
		// throw an error if the request cannot be serviced
		if(this.request == null) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("an internal error occurred while completing this request").build();
		}

		// create good response
		ResponseBuilder builder = Response.ok();

		// provided type
		String type = null;
		if(this instanceof AudioBookService) {
			type = "book";
		} else if(this instanceof UserReadingSessionService) {
			type = "session";
		} else {
			throw new OratorRuntimeException("Service type not implemented");
		}		
		
		String fullRequestUrl = this.request.getRequestURL().toString();

		// get referer and try that
		String referer = this.request.getHeader("referer");

		final String address;
		if(referer != null) {
			address = String.format("%s#/%s/%s", referer, type, id);
		} else {
			try {
				URI uri = new URI(fullRequestUrl);
				String pathToJanus = this.request.getContextPath();

				// create port string only if port is evident (greater than 0) 
				String portString = uri.getPort() <= 0 ? "" : ":" + uri.getPort();

				// calculate full address
				address = String.format("%s://%s%s%s/index.jsp#/%s/%s", 
					uri.getScheme().toLowerCase(), 
					uri.getHost(), 
					portString,
					pathToJanus, 
					type, 
					id
				);
			} catch (URISyntaxException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity("could not determine path for server resource").build();
			}		
		}

		// generate QR code as PNG
		QRCode code = QRCode.from(address).to(ImageType.PNG).withSize(250, 250);

		// use entity as response output
		byte[] output = code.stream().toByteArray();

		// and set up response content
		builder.header("Content-Disposition", "attachment; filename=\"" + type + "-" + id + ".png\"");
		builder.type("image/png");
		builder.header("Content-Length", output.length);

		// set response output
		builder.entity(output);

		// log trace for later debugging
		this.logger.trace("uri for {}:{} at: {}", new Object[]{type, id, address});

		// return good response
		return builder.build();
	}
	
}

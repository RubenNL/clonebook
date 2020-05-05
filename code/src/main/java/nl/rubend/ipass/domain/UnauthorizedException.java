package nl.rubend.ipass.domain;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class UnauthorizedException extends WebApplicationException {
	public UnauthorizedException(String errorMessage) {super(Response.status(Response.Status.UNAUTHORIZED).entity(errorMessage).type(MediaType.TEXT_PLAIN).build());}
}

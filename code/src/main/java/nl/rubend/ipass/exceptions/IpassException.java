package nl.rubend.ipass.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class IpassException extends RuntimeException {//extends WebApplicationException {
	//public IpassException(String errorMessage) {super(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).type(MediaType.TEXT_PLAIN).build());}
	public IpassException(String errorMessage) {super(errorMessage);}
}

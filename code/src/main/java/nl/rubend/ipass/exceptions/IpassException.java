package nl.rubend.ipass.exceptions;

public class IpassException extends RuntimeException {//extends WebApplicationException {
	//public IpassException(String errorMessage) {super(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).type(MediaType.TEXT_PLAIN).build());}
	public IpassException(String errorMessage) {super(errorMessage);}
}

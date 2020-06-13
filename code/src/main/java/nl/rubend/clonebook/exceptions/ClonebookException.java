package nl.rubend.clonebook.exceptions;

public class ClonebookException extends RuntimeException {//extends WebApplicationException {
	//public IpassException(String errorMessage) {super(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).type(MediaType.TEXT_PLAIN).build());}
	public ClonebookException(String errorMessage) {super(errorMessage);}
}

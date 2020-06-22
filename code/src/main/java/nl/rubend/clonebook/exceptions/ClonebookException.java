package nl.rubend.clonebook.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;

public class ClonebookException extends WebApplicationException {
	private String message;
	public ClonebookException(Response.Status code,String message) {
		super(Response.status(code).entity(new AbstractMap.SimpleEntry<String,String>("error",message)).type(MediaType.APPLICATION_JSON).build());
		this.message=message;
	}
	public ClonebookException(String message) {
		this(Response.Status.BAD_REQUEST,message);
		this.message=message;
	}
	public ClonebookException(Response.Status code) {
		super(Response.status(code).build());
		this.message=code.toString();
	}
	@Override
	public String getMessage() {
		return message;
	}
}

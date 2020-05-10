package nl.rubend.ipass.domain;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class NotFoundException  extends RuntimeException {
	public NotFoundException(String errorMessage) {super(errorMessage);}
}

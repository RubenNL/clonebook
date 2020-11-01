package nl.rubend.clonebook.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "BAD REQUEST")
public class ClonebookException extends RuntimeException {
	public ClonebookException(String msg) {
		super(msg);
	}
	public ClonebookException(String second,String message) {
		super(message);
	}
}

package nl.rubend.ipass.domain;

public class NotFoundException  extends RuntimeException {
	public NotFoundException(String errorMessage) {super(errorMessage);}
}

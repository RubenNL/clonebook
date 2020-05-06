package nl.rubend.ipass.domain;

public class UnauthorizedException extends Exception {
	public UnauthorizedException(String errorMessage) {super(errorMessage);}
}

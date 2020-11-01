package nl.rubend.clonebook.security.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

@ToString
public class Login {
	public String email;
	public String password;
	@JsonProperty("long")
	public String ingelogdBlijven;
}

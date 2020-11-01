package nl.rubend.clonebook.security.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class Registration {
	@Email
	public String email;
	@JsonProperty("g-recaptcha-response")
	public String recaptchaResponse;
}

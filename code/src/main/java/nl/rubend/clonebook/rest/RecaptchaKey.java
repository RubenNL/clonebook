package nl.rubend.clonebook.rest;

import nl.rubend.clonebook.utils.Recaptcha;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("recaptchaKey")
public class RecaptchaKey {
	@GET
	public Response getKey() {
		return Response.ok(Recaptcha.getKey()).build();
	}
}

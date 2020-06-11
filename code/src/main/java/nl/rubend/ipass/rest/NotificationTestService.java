package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.PushReceiver;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/notification")
public class NotificationTestService {
	@GET
	public Response getKey() {
		return Response.ok(PushReceiver.getPublicKeyString()).build();
	}
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response sendNotification(@FormParam("endpoint") String endpoint, @FormParam("key") String key, @FormParam("auth") String auth) {
		new PushReceiver(endpoint,key,auth).sendNotification("test");
		return Response.ok().build();
	}
}

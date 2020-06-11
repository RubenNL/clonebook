package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.PushReceiver;
import nl.rubend.ipass.security.SecurityBean;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;

@Path("/notification")
public class NotificationService {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getKey() {
		return Response.ok(new AbstractMap.SimpleEntry<>("key",PushReceiver.getPublicKeyString())).build();
	}
	@POST
	@RolesAllowed("user")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response sendNotification(@BeanParam SecurityBean securityBean, @FormParam("endpoint") String endpoint, @FormParam("key") String key, @FormParam("auth") String auth) {
		new PushReceiver(endpoint,key,auth,securityBean.getSender()).sendNotification("done!");
		return Response.ok().build();
	}
}

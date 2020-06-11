package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.PushReceiver;
import nl.rubend.ipass.security.SecurityBean;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;

@Path("/notification")
@RolesAllowed("user")
@Produces(MediaType.APPLICATION_JSON)
public class NotificationService {
	@GET
	public Response getKey() {
		return Response.ok(new AbstractMap.SimpleEntry<>("key",PushReceiver.getPublicKeyString())).build();
	}
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response sendNotification(@BeanParam SecurityBean securityBean, @FormParam("endpoint") String endpoint, @FormParam("key") String key, @FormParam("auth") String auth) {
		new PushReceiver(endpoint,key,auth,securityBean.getSender()).sendNotification("Meldingen ingesteld!");
		return Response.ok().build();
	}
	@DELETE
	@Path("/{auth}")
	public Response deletePushReceiver(@BeanParam SecurityBean securityBean, @PathParam("auth") String auth) {
		PushReceiver receiver=PushReceiver.getReceiver(auth);
		if(receiver==null) throw new NotFoundException();
		if(!receiver.getUser().equals(securityBean.getSender())) throw new ForbiddenException("Niet toegestaan om andere listeners te verwijderen!");
		receiver.delete();
		return Response.ok().build();
	}
	@POST
	@Path("/{auth}")
	public Response sendTestNotif(@BeanParam SecurityBean securityBean, @PathParam("auth") String auth) {
		PushReceiver receiver=PushReceiver.getReceiver(auth);
		if(receiver==null) throw new NotFoundException();
		if(!receiver.getUser().equals(securityBean.getSender())) throw new ForbiddenException("Niet toegestaan om andere listeners te verwijderen!");
		receiver.sendNotification("testNotif!");
		return Response.ok().build();
	}

}

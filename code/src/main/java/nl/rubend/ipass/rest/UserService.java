package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.HashMap;
import java.util.Map;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserService {
	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response newPasswordMail(@FormParam("email") String email) {
		User user=User.getUserByEmail(email);
		if(user==null) user=new User(email);
		user.sendPasswordForgottenUrl();
		return Response.ok(new HashMap<String,String>()).build();
	}
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/newPassword")
	public Response use(@FormParam("code") String code, @FormParam("password") String password) {
		User user=NewPassword.use(code);
		user.setPassword(password);
		return Response.ok().build();
	}
	@GET
	@Path("/settings")
	@RolesAllowed("user")
	public Response getSettings(@Context SecurityContext securityContext) {
		User user = (User) securityContext.getUserPrincipal();
		Map<String,String> map=new HashMap<>();
		map.put("email",user.getEmail());
		map.put("name",user.getName());
		return Response.ok(map).build();
	}
	@POST
	@Path("/settings")
	@RolesAllowed("user")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response setProfile(@FormParam("name") String name, @FormParam("email") String email, @Context SecurityContext securityContext) {
		User user = (User) securityContext.getUserPrincipal();
		if (!user.getEmail().equals(email)) user.setEmail(email);
		if (!user.getName().equals(name)) user.setName(name);
		return Response.ok().build();
	}
	@GET
	@Path("/lidAanvragen")
	public Response getLidAanvragen(@Context SecurityContext securityContext) {
		User user = (User) securityContext.getUserPrincipal();
		return Response.ok(user.getLidAanvragenOpPaginas()).build();
	}
	@GET
	@RolesAllowed("user")
	@Path("/{userId}")
	public Response publicUserProfile(@Context SecurityContext securityContext, @PathParam("userId") String userId) {
		User user= (User) securityContext.getUserPrincipal();
		User requested=User.getUserById(userId);
		if(requested==null) throw new NotFoundException("Gebruiker niet gevonden.");
		//if(requested.getPrivatePage().isLid(user)) return Response.ok(requested).build();
		//else throw new ForbiddenException("Niet eigen profiel");
		return Response.ok(requested).build();//Iedereen mag het publieke profiel bekijken.
	}
}

package nl.rubend.clonebook.rest;

import nl.rubend.clonebook.domain.*;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.security.SecurityBean;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;
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
		NewPassword newPassword=NewPassword.use(code);
		User user=newPassword.getUser();
		try {
			user.setPassword(password);
		} catch(IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new AbstractMap.SimpleEntry<String,String>("error",e.getMessage())).build();
		}
		newPassword.delete();
		return Response.ok().build();
	}
	@GET
	@Path("/{userId}/settings")
	@RolesAllowed("user")
	public Response getSettings(@BeanParam SecurityBean securityBean) {
		User user = securityBean.allowedUser();
		Map<String,String> map=new HashMap<>();
		map.put("email",user.getEmail());
		map.put("name",user.getName());
		return Response.ok(map).build();
	}
	@POST
	@Path("/{userId}/settings")
	@RolesAllowed("user")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response setProfile(@FormParam("name") String name, @FormParam("email") String email, @BeanParam SecurityBean securityBean) {
		User user = securityBean.allowedUser();
		if (!user.getEmail().equals(email)) user.setEmail(email);
		if (!user.getName().equals(name)) user.setName(name);
		return Response.ok().build();
	}
	@GET
	@Path("/{userId}/lidAanvragen")
	@RolesAllowed("user")
	public Response getLidAanvragen(@BeanParam SecurityBean securityBean) {
		return Response.ok(securityBean.allowedUser().getLidAanvragenOpPaginas()).build();
	}
	@GET
	@RolesAllowed("user")
	@Path("/{userId}")
	public Response publicUserProfile(@BeanParam SecurityBean securityBean) {
		return Response.ok(securityBean.getRequested()).build();
	}
	@DELETE
	@RolesAllowed("user")
	@Path("/{userId}/sessions")
	public Response resetSessions(@BeanParam SecurityBean securityBean) {
		securityBean.allowedUser().logoutAll();
		return Response.ok(true).build();
	}
	@POST
	@RolesAllowed("user")
	@Path("/{userId}/notif")
	public Response sendTestNotif(@BeanParam SecurityBean securityBean) {
		securityBean.getSender().sendToUser("testNotif!");
		return Response.ok().build();
	}
}

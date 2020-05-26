package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.Page;
import nl.rubend.ipass.domain.User;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.HashMap;
import java.util.Map;

@Path("/page")
@RolesAllowed("user")
@Produces(MediaType.APPLICATION_JSON)
public class PageService {
	@GET
	@Path("/{pageId}")
	public Response publicPage(@PathParam("pageId") String pageId, @Context SecurityContext securityContext) {
		User user= (User) securityContext.getUserPrincipal();
		Page page;
		try {
			page = Page.getPage(pageId);
		} catch(NotFoundException e) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		if(page.isLid(user)) return Response.ok(page).build();
		Map<String,String> response=new HashMap<>();
		response.put("id",page.getId());
		response.put("name",page.getName());
		return Response.status(Response.Status.FORBIDDEN).entity(response).build();
	}
	@GET
	@Path("/{pageId}/leden")
	public Response leden(@PathParam("pageId") String pageId,@Context SecurityContext securityContext) {
		Page page=Page.getPage(pageId);
		User user= (User) securityContext.getUserPrincipal();
		if(!page.isLid(user)) return Response.status(Response.Status.FORBIDDEN).build();
		else return Response.ok(page.getLeden()).build();
	}
	@POST
	@Path("/{pageId}/name")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response setName(@FormParam("name") String name, @PathParam("pageId") String pageId,@Context SecurityContext securityContext) {
		Page page=Page.getPage(pageId);
		User user= (User) securityContext.getUserPrincipal();
		if(!page.getOwnerId().equals(user.getId())) return Response.status(Response.Status.FORBIDDEN).build();
		page.setName(name);
		return Response.ok().build();
	}
}

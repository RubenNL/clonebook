package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.Page;
import nl.rubend.ipass.domain.User;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.AbstractMap;

@Path("/page")
@RolesAllowed("user")
@Produces(MediaType.APPLICATION_JSON)
public class PageService {
	@GET
	@Path("/{pageId}")
	public Response publicPage(@PathParam("pageId") String pageId) {
		Page page=Page.getPage(pageId);
		return Response.ok(page).build();
	}
	@GET
	@Path("/{pageId}/posts")
	public Response posts(@PathParam("pageId") String pageId,@Context SecurityContext securityContext) {
		Page page=Page.getPage(pageId);
		User user= (User) securityContext.getUserPrincipal();
		if(!page.isLid(user)) return Response.status(Response.Status.FORBIDDEN).build();
		else return Response.ok(page.getPosts()).build();
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

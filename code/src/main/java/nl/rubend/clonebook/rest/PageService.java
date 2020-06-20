package nl.rubend.clonebook.rest;

import nl.rubend.clonebook.domain.Media;
import nl.rubend.clonebook.domain.Page;
import nl.rubend.clonebook.domain.User;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.security.SecurityBean;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

@Path("/page")
@RolesAllowed("user")
@Produces(MediaType.APPLICATION_JSON)
public class PageService {
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response createPage(@BeanParam SecurityBean securityBean,@FormParam("name") String name) {
		Page page=new Page(securityBean.getSender(),name);
		page.addLid(securityBean.getSender());
		return Response.ok(new AbstractMap.SimpleEntry<>("id", page.getId())).build();
	}
	@GET
	@Path("/{pageId}")
	public Response publicPage(@BeanParam Bean bean, @BeanParam SecurityBean securityBean) {
		User user= securityBean.getSender();
		if(bean.existsThrows().isLid()) return Response.ok(bean.getPage()).build();
		Map<String,Object> response=new HashMap<>();
		response.put("id",bean.getPage().getId());
		response.put("name",bean.getPage().getName());
		response.put("request",bean.getPage().hasLidAanvraagVanUser(user));
		return Response.status(Response.Status.FORBIDDEN).entity(response).build();
	}
	@GET
	@Path("/{pageId}/name")
	public Response name(@BeanParam Bean bean) {
		return Response.ok(bean.existsThrows().getPage().getName()).build();
	}
	@GET
	@Path("/{pageId}/leden")
	public Response leden(@BeanParam Bean bean) {
		return Response.ok(bean.onlyPageLid().getPage().getLeden()).build();
	}
	@POST
	@Path("/{pageId}/image")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response setPicture(@FormParam("image") String imageId, @BeanParam Bean bean) {
		if(!bean.existsThrows().isAdmin()) return Response.status(Response.Status.FORBIDDEN).build();
		bean.getPage().setLogo(Media.getMedia(imageId));
		return Response.ok(true).build();
	}
	@POST
	@Path("/{pageId}/name")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response setName(@FormParam("name") String name, @BeanParam Bean bean) {
		if(!bean.existsThrows().isAdmin()) return Response.status(Response.Status.FORBIDDEN).build();
		bean.getPage().setName(name);
		return Response.ok().build();
	}
	@GET
	@Path("/{pageId}/lidAanvraag")
	public Response getLidAanvraag(@BeanParam Bean bean, @BeanParam SecurityBean securityBean) {
		User user=securityBean.getSender();
		return Response.ok(bean.existsThrows().getPage().hasLidAanvraagVanUser(user)).build();
	}
	@POST
	@Path("/{pageId}/lidAanvraag")
	public Response addLidAanvraag(@BeanParam Bean bean, @BeanParam SecurityBean securityBean) {
		User user=securityBean.getSender();
		bean.existsThrows().getPage().addLidAanvraag(user);
		return Response.ok(true).build();
	}
	@DELETE
	@Path("/{pageId}/lidAanvraag/{userId}")
	public Response removeLidAanvraag(@BeanParam Bean bean, @BeanParam SecurityBean securityBean) {
		if(securityBean.isAllowed() || bean.isAdmin()) {
			bean.existsThrows().getPage().removeLidAanvraag(securityBean.getRequested());
			return Response.ok(true).build();
		} else throw new ForbiddenException();
	}
	@POST
	@Path("/{pageId}/acceptLid/{userId}")
	public Response acceptLidAanvraag(@BeanParam Bean bean, @BeanParam SecurityBean securityBean) {
		if(!bean.existsThrows().isAdmin()) throw new ForbiddenException();
		bean.getPage().acceptUser(securityBean.getRequested());
		return Response.ok(true).build();
	}
	static class Bean {
		@PathParam("pageId") String pageId;
		@BeanParam SecurityBean securityBean;
		Page page;
		Page getPage() {
			if(page==null) page=Page.getPage(pageId);
			return page;
		}
		boolean isAdmin() {
			return getPage().getOwner().equals(securityBean.getSender());
		}
		boolean isLid() {
			return getPage().isLid(securityBean.getSender());
		}
		Bean existsThrows() {
			if(getPage()==null) throw new ClonebookException(Response.Status.NOT_FOUND,"pagina niet gevonden");
			return this;
		}
		Bean onlyPageLid() {
			if(!existsThrows().isLid()) throw new ClonebookException(Response.Status.FORBIDDEN,"geen toegang");
			return this;
		}
	}
}

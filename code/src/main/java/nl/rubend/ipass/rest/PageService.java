package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.Media;
import nl.rubend.ipass.domain.Page;
import nl.rubend.ipass.domain.User;
import nl.rubend.ipass.security.SecurityBean;

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
	@GET
	@Path("/{pageId}")
	public Response publicPage(@BeanParam Bean bean, @BeanParam SecurityBean securityBean) {
		User user= securityBean.getSender();
		if(bean.getPage()==null) return Response.status(Response.Status.NOT_FOUND).build();
		if(bean.getPage().isLid(user)) return Response.ok(bean.getPage()).build();
		Map<String,String> response=new HashMap<>();
		response.put("id",bean.getPage().getId());
		response.put("name",bean.getPage().getName());
		return Response.status(Response.Status.FORBIDDEN).entity(response).build();
	}
	@GET
	@Path("/{pageId}/name")
	public Response name(@BeanParam Bean bean) {
		return Response.ok(bean.getPage().getName()).build();
	}
	@GET
	@Path("/{pageId}/leden")
	public Response leden(@BeanParam Bean bean,@BeanParam SecurityBean securityBean) {
		if(bean.getPage()==null) return Response.status(Response.Status.NOT_FOUND).build();
		if(!bean.isLid()) return Response.status(Response.Status.FORBIDDEN).build();
		else return Response.ok(bean.getPage().getLeden()).build();
	}
	@POST
	@Path("/{pageId}/image")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response setPicture(@FormParam("image") String imageId, @BeanParam Bean bean) {
		if(!bean.isAdmin()) return Response.status(Response.Status.FORBIDDEN).build();
		bean.getPage().setLogo(Media.getMedia(imageId));
		return Response.ok(true).build();
	}
	@POST
	@Path("/{pageId}/name")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response setName(@FormParam("name") String name, @BeanParam Bean bean) {
		if(bean.getPage()==null) return Response.status(Response.Status.NOT_FOUND).build();
		if(!bean.isAdmin()) return Response.status(Response.Status.FORBIDDEN).build();
		bean.getPage().setName(name);
		return Response.ok().build();
	}
	@GET
	@Path("/{pageId}/lidAanvraag")
	public Response getLidAanvraag(@BeanParam Bean bean, @BeanParam SecurityBean securityBean) {
		User user=securityBean.getSender();
		return Response.ok(bean.getPage().hasLidAanvraagVanUser(user)).build();
	}
	@POST
	@Path("/{pageId}/lidAanvraag")
	public Response addLidAanvraag(@BeanParam Bean bean, @BeanParam SecurityBean securityBean) {
		User user=securityBean.getSender();
		if(bean.isLid()) return Response.status(Response.Status.CONFLICT).entity(new AbstractMap.SimpleEntry<String,String>("error","al lid")).build();
		if(bean.getPage().hasLidAanvraagVanUser(user)) return Response.status(Response.Status.CONFLICT).entity(new AbstractMap.SimpleEntry<String,String>("error","al lidaanvraag verstuurd")).build();
		bean.getPage().addLidAanvraag(user);
		return Response.ok(true).build();
	}
	@DELETE
	@Path("/{pageId}/lidAanvraag/{userId}")
	public Response removeLidAanvraag(@BeanParam Bean bean, @BeanParam SecurityBean securityBean) {
		if(securityBean.isAllowed() || bean.isAdmin()) {
			bean.getPage().removeLidAanvraag(securityBean.getRequested());
			return Response.ok(true).build();
		} else throw new ForbiddenException();
	}
	@POST
	@Path("/{pageId}/acceptLid/{userId}")
	public Response acceptLidAanvraag(@BeanParam Bean bean, @BeanParam SecurityBean securityBean) {
		if(!bean.isAdmin()) throw new ForbiddenException();
		bean.getPage().acceptUser(securityBean.getRequested());
		return Response.ok(true).build();
	}
	static class Bean {
		@PathParam("pageId") String pageId;
		@BeanParam SecurityBean securityBean;
		Page getPage() {
			return Page.getPage(pageId);
		}
		boolean isAdmin() {
			return getPage().getOwner().equals(securityBean.getSender());
		}
		boolean isLid() {
			return getPage().isLid(securityBean.getSender());
		}
	}
}

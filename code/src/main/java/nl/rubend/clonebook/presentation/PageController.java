package nl.rubend.clonebook.presentation;

import nl.rubend.clonebook.data.SpringMediaRepository;
import nl.rubend.clonebook.data.SpringPageRepository;
import nl.rubend.clonebook.domain.Page;
import nl.rubend.clonebook.domain.User;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.security.SecurityBean;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/page")
@RolesAllowed("user")
public class PageController {
	private static SpringPageRepository repository;
	private final SpringMediaRepository mediaRepository;

	public PageController(SpringPageRepository repo, SpringMediaRepository mediaRepository) {
		repository = repo;
		this.mediaRepository = mediaRepository;
	}

	@PostMapping
	public Response createPage(@BeanParam SecurityBean securityBean, @RequestBody NewPageDTO pageDTO) {
		Page page=new Page();
		page.setOwner(securityBean.getSender());
		page.setName(pageDTO.name);
		page.addLid(securityBean.getSender());
		page=repository.save(page);
		return Response.ok(new AbstractMap.SimpleEntry<>("id", page.getId())).build();
	}
	@GetMapping("/{pageId}")
	public Response publicPage(@PathVariable String pageId, @AuthenticationPrincipal User user) {
		Bean bean=new Bean(pageId,user);
		if(bean.existsThrows().isLid()) return Response.ok(bean.getPage()).build();
		Map<String,Object> response=new HashMap<>();
		response.put("id",bean.getPage().getId());
		response.put("name",bean.getPage().getName());
		response.put("request",bean.getPage().hasLidAanvraagVanUser(user));
		response.put("blocked",bean.getPage().isBlocked(user));
		return Response.status(Response.Status.FORBIDDEN).entity(response).build();
	}
	@DeleteMapping("/{pageId}")
	public Response deletePage(@PathVariable String pageId, @AuthenticationPrincipal User user) {
		Bean bean=new Bean(pageId,user);
		if(!bean.existsThrows().isAdmin()) return Response.status(Response.Status.FORBIDDEN).build();
		repository.delete(bean.getPage());
		return Response.ok().build();
	}
	@GetMapping("/{pageId}/name")
	public Response name(@PathVariable String pageId, @AuthenticationPrincipal User user) {
		Bean bean=new Bean(pageId,user);
		return Response.ok(bean.existsThrows().getPage().getName()).build();
	}
	@GetMapping("/{pageId}/leden")
	public Response leden(@PathVariable String pageId, @AuthenticationPrincipal User user) {
		Bean bean=new Bean(pageId,user);
		return Response.ok(bean.onlyPageLid().getPage().getLeden()).build();
	}
	@PostMapping("/{pageId}/image")
	public Response setPicture(@PathVariable String pageId,@AuthenticationPrincipal User user,@RequestBody SetImageDTO setImageDTO) {
		Bean bean=new Bean(pageId,user);
		if(!bean.existsThrows().isAdmin()) return Response.status(Response.Status.FORBIDDEN).build();
		Page page=bean.getPage();
		page.setLogo(mediaRepository.getOne(setImageDTO.imageId));
		repository.save(page);
		return Response.ok(true).build();
	}
	@PostMapping("/{pageId}/name")
	public Response setName(@PathVariable String pageId,@AuthenticationPrincipal User user,@RequestBody SetNameDTO setNameDTO) {
		Bean bean=new Bean(pageId,user);
		if(!bean.existsThrows().isAdmin()) return Response.status(Response.Status.FORBIDDEN).build();
		Page page=bean.getPage();
		page.setName(setNameDTO.name);
		repository.save(page);
		return Response.ok().build();
	}
	@GetMapping("/{pageId}/lidAanvraag")
	public Response getLidAanvraag(@PathVariable String pageId,@AuthenticationPrincipal User user) {
		Bean bean=new Bean(pageId,user);
		return Response.ok(bean.existsThrows().getPage().hasLidAanvraagVanUser(user)).build();
	}
	@PostMapping("/{pageId}/lidAanvraag")
	public Response addLidAanvraag(@PathVariable String pageId, @AuthenticationPrincipal User user) {
		Bean bean=new Bean(pageId,user);
		Page page=bean.existsThrows().getPage();
		page.addLidAanvraag(user);
		repository.save(page);
		bean.getPage();
		return Response.ok(true).build();
	}
	@DeleteMapping("/{pageId}/lid/{userId}")
	public Response removeLid(@PathVariable String pageId, @PathVariable String userId, @AuthenticationPrincipal User user) {
		Bean bean=new Bean(pageId,user);
		if(userId.equals(user.getId()) || bean.isAdmin()) {
			Page page=bean.existsThrows().getPage();
			page.removeLid(user);
			repository.save(page);
			return Response.ok(true).build();
		} else throw new ForbiddenException();
	}
	/*@POST
	@Path("/{pageId}/block/{userId}")
	public Response blockUser(@BeanParam Bean bean, @BeanParam SecurityBean securityBean) {
		if(securityBean.isAllowed() || bean.isAdmin()) {
			bean.existsThrows().getPage().blockUnblockLid(securityBean.getRequested(),true);
			return Response.ok(true).build();
		} else throw new ForbiddenException();
	}
	@DELETE
	@Path("/{pageId}/block/{userId}")
	public Response unblockUser(@BeanParam Bean bean, @BeanParam SecurityBean securityBean) {
		if(securityBean.isAllowed() || bean.isAdmin()) {
			bean.existsThrows().getPage().blockUnblockLid(securityBean.getRequested(),false);
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
	@GET
	@Path("/{pageId}/blocked")
	public Response getBlocked(@BeanParam Bean bean) {
		if(!bean.existsThrows().isAdmin()) throw new ForbiddenException();
		return Response.ok(bean.getPage().getBlocked()).build();
	}*/
	/*@GET
	@Path("/{pageId}/before/{date}")
	public Response getChatBefore(@BeanParam Bean bean, @PathParam("date") String before) {
		return Response.ok(postRepository.getPostsLimit(bean.onlyPageLid().getPage().getPostsLimit()).build();
	}*/
	static class Bean {
		Bean(String pageId,User user) {
			this.pageId=pageId;
			this.user=user;
		}
		User user;
		String pageId;
		Page page;
		Page getPage() {
			if(page==null) page=repository.getOne(pageId);
			return page;
		}
		boolean isAdmin() {
			return getPage().getOwner().equals(user);
		}
		boolean isLid() {
			return getPage().isLid(user);
		}
		Bean existsThrows() {
			if(getPage()==null) throw new ClonebookException("NOT_FOUND");
			return this;
		}
		Bean onlyPageLid() {
			if(!existsThrows().isLid()) throw new ClonebookException("FORBIDDEN","geen toegang");
			return this;
		}
	}
	static class NewPageDTO {
		public String name;
	}
	static class SetImageDTO {
		public String imageId;
	}
	static class SetNameDTO {
		public String name;
	}
}
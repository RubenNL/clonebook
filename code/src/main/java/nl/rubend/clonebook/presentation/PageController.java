/*package nl.rubend.clonebook.presentation;

import nl.rubend.clonebook.data.SpringMediaRepository;
import nl.rubend.clonebook.data.SpringPageRepository;
import nl.rubend.clonebook.domain.Page;
import nl.rubend.clonebook.domain.User;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.security.SecurityBean;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/page")
@RolesAllowed("user")
public class PageController {
	private final SpringPageRepository repository;
	private final SpringMediaRepository mediaRepository;

	public PageController(SpringPageRepository repository, SpringMediaRepository mediaRepository) {
		this.repository = repository;
		this.mediaRepository = mediaRepository;
	}

	@PostMapping
	public EntityModel<Page> createPage(@BeanParam SecurityBean securityBean, @RequestBody NewPageDTO pageDTO) {
		Page page=new Page();
		page.setOwner(securityBean.getSender());
		page.setName(pageDTO.name);
		page.addLid(securityBean.getSender());
		page=repository.save(page);
		return Response.ok(new AbstractMap.SimpleEntry<>("id", page.getId())).build();
	}
	@GetMapping("/{pageId}")
	public Response publicPage(@PathVariable String pageId, @BeanParam SecurityBean securityBean) {
		Bean bean=new Bean(pageId,securityBean);
		User user= securityBean.getSender();
		if(bean.existsThrows().isLid()) return Response.ok(bean.getPage()).build();
		Map<String,Object> response=new HashMap<>();
		response.put("id",bean.getPage().getId());
		response.put("name",bean.getPage().getName());
		response.put("request",bean.getPage().hasLidAanvraagVanUser(user));
		response.put("blocked",bean.getPage().isBlocked(user));
		return Response.status(Response.Status.FORBIDDEN).entity(response).build();
	}
	@DeleteMapping("/{pageId}")
	public Response deletePage(@PathVariable String pageId, @BeanParam SecurityBean securityBean) {
		Bean bean=new Bean(pageId,securityBean);
		if(!bean.existsThrows().isAdmin()) return Response.status(Response.Status.FORBIDDEN).build();
		bean.getPage().delete();
		return Response.ok().build();
	}
	@GetMapping("/{pageId}/name")
	public Response name(@PathVariable String pageId, @BeanParam SecurityBean securityBean) {
		Bean bean=new Bean(pageId,securityBean);
		return Response.ok(bean.existsThrows().getPage().getName()).build();
	}
	@GetMapping("/{pageId}/leden")
	public Response leden(@PathVariable String pageId, @BeanParam SecurityBean securityBean) {
		Bean bean=new Bean(pageId,securityBean);
		return Response.ok(bean.onlyPageLid().getPage().getLeden()).build();
	}
	@PostMapping("/{pageId}/image")
	public Response setPicture(@PathVariable String pageId,@BeanParam SecurityBean securityBean,@RequestBody SetImageDTO setImageDTO) {
		Bean bean=new Bean(pageId,securityBean);
		if(!bean.existsThrows().isAdmin()) return Response.status(Response.Status.FORBIDDEN).build();
		Page page=bean.getPage();
		page.setLogo(mediaRepository.getOne(setImageDTO.imageId));
		repository.save(page);
		return Response.ok(true).build();
	}
	@PostMapping("/{pageId}/name")
	public Response setName(@PathVariable String pageId,@BeanParam SecurityBean securityBean,@RequestBody SetNameDTO setNameDTO) {
		Bean bean=new Bean(pageId,securityBean);
		if(!bean.existsThrows().isAdmin()) return Response.status(Response.Status.FORBIDDEN).build();
		Page page=bean.getPage();
		page.setName(setNameDTO.name);
		repository.save(page);
		return Response.ok().build();
	}
	@GetMapping("/{pageId}/lidAanvraag")
	public Response getLidAanvraag(@PathVariable String pageId,@BeanParam SecurityBean securityBean) {
		Bean bean=new Bean(pageId,securityBean);
		User user=securityBean.getSender();
		return Response.ok(bean.existsThrows().getPage().hasLidAanvraagVanUser(user)).build();
	}
	@PostMapping("/{pageId}/lidAanvraag")
	public Response addLidAanvraag(@PathVariable String pageId, @BeanParam SecurityBean securityBean) {
		Bean bean=new Bean(pageId,securityBean);
		User user=securityBean.getSender();
		bean.existsThrows().getPage().addLidAanvraag(user);
		return Response.ok(true).build();
	}
	/*@DeleteMapping("/{pageId}/lid/{userId}")
	public Response removeLid(@PathVariable String pageId, Principal principal) {
		principal.
		Bean bean=new Bean(pageId,securityBean);
		if(securityBean.isAllowed() || bean.isAdmin()) {
			bean.existsThrows().getPage().removeLid(securityBean.getRequested());
			return Response.ok(true).build();
		} else throw new ForbiddenException();
	}
	@PostMapping("/{pageId}/block/{userId}")
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
	}
	@GET
	@Path("/{pageId}/before/{date}")
	public Response getChatBefore(@BeanParam Bean bean, @PathParam("date") String before) {
		return Response.ok(bean.onlyPageLid().getPage().getPostsLimit(new Date(Long.parseLong(before)),10)).build();
	}*/
	/*static class Bean {
		Bean(String pageId,SecurityBean securityBean) {
			this.pageId=pageId;
			this.securityBean=securityBean;
		}
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
			if(getPage()==null) throw new ClonebookException(Response.Status.NOT_FOUND);
			return this;
		}
		Bean onlyPageLid() {
			if(!existsThrows().isLid()) throw new ClonebookException(Response.Status.FORBIDDEN,"geen toegang");
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

}*/
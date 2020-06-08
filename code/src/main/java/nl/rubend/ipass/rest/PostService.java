package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.*;
import nl.rubend.ipass.security.SecurityBean;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.AbstractMap;
import java.util.List;
import java.util.Objects;

@Path("/post")
@Produces(MediaType.APPLICATION_JSON)
public class PostService {
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response newPost(@BeanParam SecurityBean securityBean, @FormParam("pageId") String pageId, @FormParam("repliedTo") String repliedToId, @FormParam("text") String text, @FormParam("file") List<String> files) {
		User user= securityBean.getSender();
		Page page=Page.getPage(pageId);
		if(page==null) return Response.status(Response.Status.NOT_FOUND).build();
		if(!page.isLid(user)) return Response.status(Response.Status.FORBIDDEN).build();
		Post post=new Post(user,page,repliedToId.equals("")?null:Post.getPost(repliedToId),text);
		for(String fileID:files) {
			post.addFile(Objects.requireNonNull(Media.getMedia(fileID)));
		}
		return Response.ok(post).build();
	}
	@GET
	@Path("/{postId}")
	public Response getPost(@BeanParam Bean bean,@BeanParam SecurityBean securityBean) {
		User user= securityBean.getSender();
		if(bean.getPost()==null) throw new NotFoundException("post niet gevonden");
		if(!bean.isLid()) return Response.status(Response.Status.FORBIDDEN).entity(new AbstractMap.SimpleEntry<String,String>("pageId",bean.getPost().getPageId())).build();
		return Response.ok(bean.getPost()).build();
	}
	@DELETE
	@Path("/{postId}")
	public Response deletePost(@BeanParam Bean bean,@BeanParam SecurityBean securityBean) {
		User user= securityBean.getSender();
		if(bean.getPost()==null) throw new NotFoundException("Pagina niet gevonden");
		if(!(bean.getPost().getUser().equals(user) || bean.isAdmin())) return Response.status(Response.Status.FORBIDDEN).build();
		bean.getPost().delete();
		return Response.ok().build();
	}
	@POST
	@Path("/{postId}/vote")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response vote(@BeanParam Bean bean,@FormParam("vote") String vote,@BeanParam SecurityBean securityBean) {
		User user= securityBean.getSender();
		if(bean.getPost()==null) throw new NotFoundException("Pagina niet gevonden");
		bean.onlyLid();
		if(vote.equals("up")) new Vote(user,bean.getPost(),1);
		else if(vote.equals("down")) new Vote(user,bean.getPost(),-1);
		else throw new javax.ws.rs.BadRequestException();
		return Response.ok().build();
	}
	static class Bean {
		@PathParam("postId") String postId;
		@BeanParam SecurityBean securityBean;
		Post getPost() {
			return Post.getPost(postId);
		}
		Page getPage() {
			return getPost().getPage();
		}
		boolean isLid() {
			return getPage().isLid(securityBean.getSender());
		}
		void onlyLid() {
			if(!isLid()) throw new ForbiddenException("geen toegang");
		}
		boolean isAdmin() {
			return getPage().getOwner().equals(securityBean.getSender());
		}
	}
}

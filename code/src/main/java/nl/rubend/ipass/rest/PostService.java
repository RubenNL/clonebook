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
	public Response getPost(@PathParam("postId") String postId,@BeanParam SecurityBean securityBean) {
		User user= securityBean.getSender();
		Post post=Post.getPost(postId);
		if(post==null) throw new NotFoundException("post niet gevonden");
		if(!post.getPage().isLid(user)) return Response.status(Response.Status.FORBIDDEN).entity(new AbstractMap.SimpleEntry<String,String>("pageId",post.getPageId())).build();
		return Response.ok(post).build();
	}
	@DELETE
	@Path("/{postId}")
	public Response deletePost(@PathParam("postId") String postId,@BeanParam SecurityBean securityBean) {
		User user= securityBean.getSender();
		Post post=Post.getPost(postId);
		if(post==null) throw new NotFoundException("Pagina niet gevonden");
		if(!(post.getUser().equals(user) || post.getPage().getOwner().equals(user))) return Response.status(Response.Status.UNAUTHORIZED).build();
		post.delete();
		return Response.ok().build();
	}
	@POST
	@Path("/{postId}/vote")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response vote(@PathParam("postId") String postId,@FormParam("vote") String vote,@BeanParam SecurityBean securityBean) {
		User user= securityBean.getSender();
		Post post=Post.getPost(postId);
		if(post==null) throw new NotFoundException("Pagina niet gevonden");
		if(!post.getPage().isLid(user)) return Response.status(Response.Status.FORBIDDEN).build();
		if(vote.equals("up")) new Vote(user,post,1);
		else if(vote.equals("down")) new Vote(user,post,-1);
		else throw new javax.ws.rs.BadRequestException();
		return Response.ok().build();
	}

}

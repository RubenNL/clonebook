package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.Page;
import nl.rubend.ipass.domain.Post;
import nl.rubend.ipass.domain.User;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/post")
@Produces(MediaType.APPLICATION_JSON)
public class PostService {
	@POST
	public Response newPost(@Context SecurityContext securityContext, @FormParam("pageId") String pageId, @FormParam("repliedTo") String repliedToId, @FormParam("text") String text) {
		User user= (User) securityContext.getUserPrincipal();
		Page page=Page.getPage(pageId);
		if(!page.isLid(user)) return Response.status(Response.Status.UNAUTHORIZED).build();
		Post post=new Post(user,page,repliedToId.equals("")?null:Post.getPost(repliedToId),text);
		return Response.ok(post).build();
	}
	@GET
	@Path("/{postId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response publicUserProfile(@PathParam("postId") String postId,@Context SecurityContext securityContext) {
		User user= (User) securityContext.getUserPrincipal();
		Post post=Post.getPost(postId);
		if(!post.getPage().isLid(user)) return Response.status(Response.Status.UNAUTHORIZED).build();
		return Response.ok(post).build();
	}
	@DELETE
	@Path("/{postId}")
	public Response deletePost(@PathParam("postId") String postId,@Context SecurityContext securityContext) {
		User user= (User) securityContext.getUserPrincipal();
		Post post=Post.getPost(postId);
		if(!(post.getUser().equals(user) || post.getPage().getOwner().equals(user))) return Response.status(Response.Status.UNAUTHORIZED).build();
		post.delete();
		return Response.ok().build();
	}

}

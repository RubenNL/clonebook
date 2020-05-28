package nl.rubend.ipass.rest;

import nl.rubend.ipass.domain.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;

@Path("/post")
@Produces(MediaType.APPLICATION_JSON)
public class PostService {
	@POST
	public Response newPost(@Context SecurityContext securityContext, @FormParam("pageId") String pageId, @FormParam("repliedTo") String repliedToId, @FormParam("text") String text, @FormParam("file") List<String> files) {
		User user= (User) securityContext.getUserPrincipal();
		Page page=Page.getPage(pageId);
		if(page==null) return Response.status(Response.Status.NOT_FOUND).build();
		if(!page.isLid(user)) return Response.status(Response.Status.UNAUTHORIZED).build();
		Post post=new Post(user,page,repliedToId.equals("")?null:Post.getPost(repliedToId),text);
		for(String fileID:files) {
			post.addFile(Media.getMedia(fileID));
		}
		return Response.ok(post).build();
	}
	@GET
	@Path("/{postId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response getPost(@PathParam("postId") String postId,@Context SecurityContext securityContext) {
		User user= (User) securityContext.getUserPrincipal();
		Post post=Post.getPost(postId);
		if(post==null) throw new NotFoundException("pagina niet gevonden");
		if(!post.getPage().isLid(user)) return Response.status(Response.Status.UNAUTHORIZED).build();
		return Response.ok(post).build();
	}
	@DELETE
	@Path("/{postId}")
	public Response deletePost(@PathParam("postId") String postId,@Context SecurityContext securityContext) {
		User user= (User) securityContext.getUserPrincipal();
		Post post=Post.getPost(postId);
		if(post==null) throw new NotFoundException("Pagina niet gevonden");
		if(!(post.getUser().equals(user) || post.getPage().getOwner().equals(user))) return Response.status(Response.Status.UNAUTHORIZED).build();
		post.delete();
		return Response.ok().build();
	}
	@POST
	@Path("/{postId}/vote")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response vote(@PathParam("postId") String postId,@FormParam("vote") String vote,@Context SecurityContext securityContext) {
		User user= (User) securityContext.getUserPrincipal();
		Post post=Post.getPost(postId);
		if(post==null) throw new NotFoundException("Pagina niet gevonden");
		if(!post.getPage().isLid(user)) return Response.status(Response.Status.UNAUTHORIZED).build();
		if(vote.equals("up")) new Vote(user,post,1);
		else if(vote.equals("down")) new Vote(user,post,-1);
		else throw new javax.ws.rs.BadRequestException();
		return Response.ok().build();
	}

}

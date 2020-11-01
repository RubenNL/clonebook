package nl.rubend.clonebook.presentation;

import nl.rubend.clonebook.data.SpringMediaRepository;
import nl.rubend.clonebook.data.SpringPageRepository;
import nl.rubend.clonebook.data.SpringPostRepository;
import nl.rubend.clonebook.data.SpringVoteRepository;
import nl.rubend.clonebook.domain.*;
import nl.rubend.clonebook.security.SecurityBean;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.List;
import java.util.Objects;

@Component
@Path("/post")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
public class PostController {
	private static SpringPostRepository repository;
	private final SpringPageRepository pageRepository;
	private final SpringMediaRepository mediaRepository;
	private final SpringVoteRepository voteRepository;

	public PostController(SpringPostRepository repository, SpringPageRepository pageRepository, SpringMediaRepository mediaRepository, SpringVoteRepository voteRepository) {
		this.repository = repository;
		this.pageRepository = pageRepository;
		this.mediaRepository = mediaRepository;
		this.voteRepository = voteRepository;
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response newPost(@BeanParam SecurityBean securityBean, @FormParam("pageId") String pageId, @FormParam("repliedTo") String repliedToId, @FormParam("text") String text, @FormParam("file") List<String> files) {
		User user= securityBean.getSender();
		Page page=pageRepository.getOne(pageId);
		if(page==null) return Response.status(Response.Status.NOT_FOUND).build();
		if(!page.isLid(user)) return Response.status(Response.Status.FORBIDDEN).build();
		Post post=new Post();
		post.setUser(user);
		post.setPage(page);
		post.setRepliedTo(repliedToId.equals("")?null:repository.getOne(repliedToId));
		post.setText(text);
		for(String fileID:files) {
			post.addFile(Objects.requireNonNull(mediaRepository.getOne(fileID)));
		}
		post=repository.save(post);
		return Response.ok(post).build();
	}
	@GET
	@Path("/{postId}")
	public Response getPost(@BeanParam Bean bean,@BeanParam SecurityBean securityBean) {
		if(bean.getPost()==null) throw new NotFoundException("post niet gevonden");
		if(!bean.isLid()) return Response.status(Response.Status.FORBIDDEN).entity(new AbstractMap.SimpleEntry<String,String>("pageId",bean.getPost().getId())).build();
		return Response.ok(bean.getPost()).build();
	}
	@DELETE
	@Path("/{postId}")
	public Response deletePost(@BeanParam Bean bean,@BeanParam SecurityBean securityBean) {
		User user= securityBean.getSender();
		if(bean.getPost()==null) throw new NotFoundException("Pagina niet gevonden");
		if(!(bean.getPost().getUser().equals(user) || bean.isAdmin())) return Response.status(Response.Status.FORBIDDEN).build();
		repository.delete(bean.getPost());
		return Response.ok().build();
	}
	@POST
	@Path("/{postId}/vote")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response vote(@BeanParam Bean bean,@FormParam("vote") String vote,@BeanParam SecurityBean securityBean) {
		User user= securityBean.getSender();
		if(bean.getPost()==null) throw new NotFoundException("Pagina niet gevonden");
		bean.onlyLid();
		Vote voteAction;
		if(vote.equals("up")) voteAction=new Vote(user,bean.getPost(),1);
		else if(vote.equals("down")) voteAction=new Vote(user,bean.getPost(),-1);
		else throw new javax.ws.rs.BadRequestException();
		voteRepository.save(voteAction);
		return Response.ok(new AbstractMap.SimpleEntry<String,Integer>("punten",bean.getPost().getVoteTotal())).build();
	}
	static class Bean {
		@PathParam("postId") String postId;
		@BeanParam SecurityBean securityBean;
		Post getPost() {
			return repository.getOne(postId);
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
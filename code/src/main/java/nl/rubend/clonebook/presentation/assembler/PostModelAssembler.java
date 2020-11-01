/*package nl.rubend.clonebook.presentation.assembler;

import nl.rubend.clonebook.domain.Post;
import nl.rubend.clonebook.presentation.PostController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PostModelAssembler implements RepresentationModelAssembler<Post, EntityModel<Post>> {
	@Override
	public EntityModel<Post> toModel(Post post) {
		return EntityModel.of(post,
				linkTo(methodOn(PostController.class).one(post.getId())).withSelfRel(),
				linkTo(methodOn(PostController.class).all()).withRel("posts"));
	}
}*/
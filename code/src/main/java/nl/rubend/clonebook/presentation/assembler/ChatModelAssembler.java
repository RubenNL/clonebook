package nl.rubend.clonebook.presentation.assembler;

import nl.rubend.clonebook.domain.Chat;
import nl.rubend.clonebook.presentation.ChatController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ChatModelAssembler implements RepresentationModelAssembler<Chat, EntityModel<Chat>> {
	@Override
	public EntityModel<Chat> toModel(Chat chat) {
		return EntityModel.of(chat,
				linkTo(methodOn(ChatController.class).one(null,chat.getId())).withSelfRel());
	}
}

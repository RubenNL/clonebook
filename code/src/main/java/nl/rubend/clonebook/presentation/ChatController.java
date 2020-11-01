package nl.rubend.clonebook.presentation;

import nl.rubend.clonebook.data.SpringChatMessageRepository;
import nl.rubend.clonebook.data.SpringChatRepository;
import nl.rubend.clonebook.data.SpringUserRepository;
import nl.rubend.clonebook.domain.Chat;
import nl.rubend.clonebook.domain.User;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.presentation.assembler.ChatModelAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/chat")
@RolesAllowed("user")
public class ChatController {
	private final SpringChatRepository repository;
	private final SpringChatMessageRepository messageRepository;
	private final SpringUserRepository userRepository;
	private final ChatModelAssembler assembler;

	public ChatController(SpringChatRepository repository, SpringChatMessageRepository messageRepository, SpringUserRepository userRepository, ChatModelAssembler assembler) {
		this.repository = repository;
		this.messageRepository = messageRepository;
		this.userRepository = userRepository;
		this.assembler = assembler;
	}
	@GetMapping("/{chatId}")
	public EntityModel<Chat> one(@AuthenticationPrincipal User user,@PathVariable String chatId) {
		Chat chat=repository.getOne(chatId);
		if(!chat.getUsers().contains(user)) throw new ClonebookException("NOT ALLOWED","niet toegestaan!");
		return assembler.toModel(chat);
	}
	@GetMapping
	public CollectionModel<EntityModel<Chat>> allByUser(@AuthenticationPrincipal User user) {
		List<EntityModel<Chat>> customers = repository.findByUser(user).stream()
				.map(assembler::toModel)
				.collect(Collectors.toList());
		return CollectionModel.of(customers, linkTo(methodOn(ChatController.class).allByUser(null)).withSelfRel());
	}
	/*@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response newChat(@BeanParam SecurityBean securityBean,@FormParam("user") String otherUser) {
		Chat chat = new Chat(securityBean.getSender(), userRepository.getOne(otherUser));
		return Response.ok(new AbstractMap.SimpleEntry<String, String>("id", chat.getId())).build();
	}
	@GET
	@Path("/{chatId}/{before}")
	public Response getChatAfter(@BeanParam Bean bean,@PathParam("before") String before) {
		return Response.ok(messageRepository.getMessagesBefore(bean.onlyLid().getChat(), LocalDateTime.parse(before))).build();
	}
	@POST
	@Path("/{chatId}")
	public Response sendMessage(@BeanParam Bean bean, @BeanParam SecurityBean securityBean,@FormParam("message") String message) {
		bean.onlyLid().getChat().sendMessage(securityBean.getSender(),message);
		return Response.ok().build();
	}
	@Component
	static class Bean {
		private final SpringChatRepository repository;
		@PathParam("chatId") String chatId;
		@BeanParam SecurityBean securityBean;

		Bean(SpringChatRepository repository) {
			this.repository = repository;
		}

		Chat getChat() {
			return repository.getOne(chatId);
		}
		boolean hasAccess() {
			return getChat().getUsers().contains(securityBean.getSender());
		}
		Bean onlyLid() {
			if(!hasAccess()) throw new ClonebookException(Response.Status.FORBIDDEN,"geen toegang");
			return this;
		}
	}*/
}
package nl.rubend.clonebook.rest;

import nl.rubend.clonebook.domain.Chat;
import nl.rubend.clonebook.domain.Page;
import nl.rubend.clonebook.domain.Post;
import nl.rubend.clonebook.domain.User;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.security.SecurityBean;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.Date;

@Path("/chat")
@RolesAllowed("user")
@Produces(MediaType.APPLICATION_JSON)
public class ChatService {
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response newChat(@BeanParam SecurityBean securityBean,@FormParam("user") String otherUser) {
		Chat chat=new Chat(securityBean.getSender(), User.getUserById(otherUser));
		return Response.ok(new AbstractMap.SimpleEntry<String,String>("id",chat.getId())).build();
	}
	@GET
	@Path("/{chatId}")
	public Response getChat(@BeanParam Bean bean) {
		return Response.ok(bean.onlyLid().getChat()).build();
	}
	@GET
	@Path("/{chatId}/{before}")
	public Response getChatAfter(@BeanParam Bean bean,@PathParam("before") String before) {
		return Response.ok(bean.onlyLid().getChat().getMessagesBefore(new Date(Long.parseLong(before)))).build();
	}
	@POST
	@Path("/{chatId}")
	public Response sendMessage(@BeanParam Bean bean, @BeanParam SecurityBean securityBean,@FormParam("message") String message) {
		bean.onlyLid().getChat().sendMessage(securityBean.getSender(),message);
		return Response.ok().build();
	}
	static class Bean {
		@PathParam("chatId") String chatId;
		@BeanParam SecurityBean securityBean;
		Chat getChat() {
			return Chat.getChat(chatId);
		}
		boolean hasAccess() {
			return getChat().getUsers().contains(securityBean.getSender());
		}
		Bean onlyLid() {
			if(!hasAccess()) throw new ClonebookException(Response.Status.FORBIDDEN,"geen toegang");
			return this;
		}
	}
}

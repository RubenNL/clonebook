package nl.rubend.clonebook.domain;
import lombok.*;
import nl.rubend.clonebook.UUIDGenerator;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.websocket.WebSocket;
import org.hibernate.annotations.GenericGenerator;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Chat {
	@Id
	@GeneratedValue(generator= UUIDGenerator.generatorName)
	@GenericGenerator(name = UUIDGenerator.generatorName, strategy = "nl.rubend.clonebook.UUIDGenerator")
	private String id;
	@ManyToMany
	private List<User> users;
	@OneToMany(mappedBy="chat",cascade=CascadeType.PERSIST)
	private List<ChatMessage> messages;
	public Chat(User user1,User user2) {
		if (user1.equals(user2)) throw new ClonebookException("minimaal 2 gebruikers nodig!");
		users.add(user1);
		users.add(user2);
	}
	public void sendMessage(User sender,String message) {
		User receiver=null;
		if(!users.contains(sender)) throw new ClonebookException("FORBIDDEN","niet-deelnemer mag geen chat sturen.");
		for(User user:users) {
			if(!user.equals(sender)) receiver=user;
		}
		if(receiver==null) throw new IllegalStateException("geen andere gebruiker in chat!");
		User finalReceiver = receiver;
		new Thread(() -> {
			JsonObjectBuilder builder = Json.createObjectBuilder();
			builder.add("from", sender.getId());
			builder.add("title", sender.getName());
			builder.add("chatId", this.id);
			builder.add("message", message);
			builder.add("type", "chat");
			WebSocket.sendToUser(finalReceiver, builder.build().toString());
		}).start();
		new Thread(() -> {
			JsonObjectBuilder builder = Json.createObjectBuilder();
			builder.add("dest",finalReceiver.getId());
			builder.add("title", finalReceiver.getName());
			builder.add("chatId", this.id);
			builder.add("message",message);
			builder.add("type","chat");
			WebSocket.sendToUser(sender,builder.build().toString());
		}).start();
		ChatMessage chatMessage=new ChatMessage();
		chatMessage.setChat(this);
		chatMessage.setUser(sender);
		chatMessage.setMessage(message);
		messages.add(chatMessage);
	}
}

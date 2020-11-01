package nl.rubend.clonebook.websocket;

import nl.rubend.clonebook.data.SpringChatRepository;
import nl.rubend.clonebook.domain.Chat;
import nl.rubend.clonebook.domain.User;
import org.springframework.stereotype.Component;

import javax.json.*;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@ServerEndpoint("/ws/{id}")
@Component
public class WebSocket {
	private final SpringChatRepository chatRepository;
	private static HashMap<String,User> waiting=new HashMap<>();//Lijst met op dit moment verbinding makende clients
	private static HashMap<User, ArrayList<Session>> connected=new HashMap<>();//user->sessies, voor alle uitloggen.
	private static HashMap<String,User> links=new HashMap<>();

	public WebSocket(SpringChatRepository chatRepository) {
		this.chatRepository = chatRepository;
	}

	public static String addWaiting(User user) {
		String id=UUID.randomUUID().toString();
		waiting.put(id,user);
		return id;
	}
	public static void sendToUser(User user,String message) {
		if(connected.get(user)==null) {
			user.sendToUser("CHAT",message);
			return;
		}
		for(Session session:connected.get(user)) {
			try {
				session.getBasicRemote().sendText(message);
			} catch (IOException e) {
				e.printStackTrace();
				try {
					session.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		if(connected.get(user)==null) user.sendToUser("/#chat",message);
	}
	public static void logoutAll(User user) {
		sendToUser(user,"logoutAll");
		for(Session session:connected.get(user)) {
			try {
				session.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		connected.remove(user);
	}
	@OnOpen
	public void onOpen(Session session,@PathParam("id") String id) {
		try {
			if(waiting.get(id)==null) {
				session.getBasicRemote().sendText("niet ingelogd");
				session.close();
				return;
			}
			User user=waiting.get(id);
			links.put(id,user);
			if(!connected.containsKey(user)) connected.put(user, new ArrayList<>());
			connected.get(user).add(session);
			waiting.remove(id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@OnMessage
	public void onMessage(@PathParam("id") String id,Session session,String message) {
		User user=links.get(id);
		JsonStructure structure = Json.createReader(new StringReader(message)).read();
		if(structure.getValueType()!= JsonValue.ValueType.OBJECT) return;
		JsonObject data=(JsonObject) structure;
		switch(data.getString("type")) {
			case "chat":
				chatRepository.getOne(data.getString("id")).sendMessage(user,data.getString("message"));
				break;
		}
	}
	@OnClose
	public void onClose(@PathParam("id") String id,Session session) {
		User user=links.get(id);
		connected.get(user).remove(session);
		if(connected.get(user).size()==0) connected.remove(user);
	}
}
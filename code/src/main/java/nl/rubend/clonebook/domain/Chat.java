package nl.rubend.clonebook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.utils.SqlInterface;
import nl.rubend.clonebook.websocket.WebSocket;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Chat {
	private String id;
	public static Chat getChat(String id) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT * FROM chat WHERE ID=?");
			statement.setString(1, id);
			ResultSet set = statement.executeQuery();
			set.next();
			return new Chat(set.getString("ID"));
		} catch (SQLException e) {
			throw new ClonebookException(Response.Status.NOT_FOUND,"Chat niet gevonden");
		}
	}
	public static ArrayList<Chat> getChats(User user) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT chatID FROM user_chat WHERE userID=?");
			statement.setString(1,user.getId());
			ResultSet set=statement.executeQuery();
			ArrayList<Chat> response=new ArrayList<>();
			while(set.next()) {
				response.add(Chat.getChat(set.getString("chatID")));
			}
			return response;
		} catch (SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
	}
	private Chat(String id) {
		this.id=id;
	}
	private void addUser(User user) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO user_chat(userID,chatID) VALUES (?,?)");
			statement.setString(1, user.getId());
			statement.setString(2, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	public Chat(User user1,User user2) {
		this.id=UUID.randomUUID().toString();
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO chat(ID) VALUES (?)");
			statement.setString(1, this.id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
		addUser(user1);
		addUser(user2);
	}
	public ArrayList<User> getUsers() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT userID FROM user_chat WHERE chatID=?");
			statement.setString(1,this.id);
			ResultSet set=statement.executeQuery();
			ArrayList<User> response=new ArrayList<>();
			while(set.next()) {
				response.add(User.getUserById(set.getString("userID")));
			}
			return response;
		} catch (SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
	}
	public String getId() {
		return this.id;
	}
	@JsonProperty("name")
	public String getNameForUser(User current) {
		for (User found : getUsers()) if (current != found) return found.getName();
		throw new ClonebookException(Response.Status.NOT_FOUND, "geen andere user gevonden in chat.");
	}
	public ArrayList<ChatMessage> getMessagesBefore(Date date) {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT chatID,userID,message,date FROM chatMessage WHERE date < ? AND chatID=? ORDER BY date DESC limit 10");
			statement.setTimestamp(1, new Timestamp(date.getTime()));
			statement.setString(2,this.id);
			ResultSet set=statement.executeQuery();
			ArrayList<ChatMessage> response=new ArrayList<>();
			while(set.next()) {
				response.add(new ChatMessage(set.getString("chatID"),set.getString("userID"),set.getTimestamp("date"),set.getString("message")));
			}
			return response;
		} catch (SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
	}
	@JsonProperty("messages")
	public ArrayList<ChatMessage> getLastMessages() {
		return getMessagesBefore(new Date(Long.parseLong("253402128000000")));//9999-12-30 00:00:00.0, zou ook geen problemen met tijdzones moeten geven.
	}
	public void sendMessage(User sender,String message) {
		User receiver=null;
		for(User user:getUsers()) {
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
		new Thread(() -> {
			new ChatMessage(this,sender,message);
		}).start();
	}
}

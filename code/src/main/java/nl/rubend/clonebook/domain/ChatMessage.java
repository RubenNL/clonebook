package nl.rubend.clonebook.domain;

import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.utils.SqlInterface;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class ChatMessage {
	private String chatId;
	private String userId;
	private String message;
	private Date date;
	public ChatMessage(String chatId,String userId,Date date,String message) {
		this.chatId=chatId;
		this.userId=userId;
		this.message=message;
		this.date=date;
	}
	public ChatMessage(Chat chat,User user,String message) {
		this(chat.getId(),user.getId(),new Date(System.currentTimeMillis()),message);
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO chatMessage(chatID,userID,message,date) VALUES (?,?,?,?)");
			statement.setString(1, this.chatId);
			statement.setString(2, this.userId);
			statement.setString(3, this.message);
			statement.setTimestamp(4, new Timestamp(this.date.getTime()));
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	public Date getDate() {
		return this.date;
	}
	public String getUserId() {
		return this.userId;
	}
	public String getMessage() {
		return this.message;
	}
}

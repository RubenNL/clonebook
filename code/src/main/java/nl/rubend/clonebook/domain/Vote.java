package nl.rubend.clonebook.domain;

import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.utils.SqlInterface;

import javax.ws.rs.core.Response;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Vote {
	private String postId;
	private String userId;
	private int vote;
	public Vote(String userId,String postId,int vote) {
		this.postId = postId;
		this.userId = userId;
		this.vote=vote;

	}
	public Vote(User user, Post post, int vote) {
		this(user.getId(),post.getId(),vote);
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("select vote from vote where userID=? and postID=?");
			statement.setString(1, userId);
			statement.setString(2,postId);
			ResultSet set=statement.executeQuery();
			if(set.next()) {
				if(set.getInt("vote")==vote) this.delete();
				else this.set();
			} else this.create();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	public Post getPost() {
		return Post.getPost(postId);
	}
	public User getUser() {
		return User.getUserById(userId);
	}
	private void set() {
		this.vote=vote;
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("UPDATE vote SET vote = ? WHERE userID=? AND postID=?");
			statement.setInt(1, vote);
			statement.setString(2, userId);
			statement.setString(3, postId);
			statement.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new ClonebookException(ex.getMessage());
		}
	}
	private void create() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO vote(userID,postID,vote) VALUES (?,?,?)");
			statement.setString(1, this.userId);
			statement.setString(2,this.postId);
			statement.setInt(3, this.vote);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
	private void delete() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("DELETE FROM vote WHERE userID=? AND postID=?");
			statement.setString(1,this.userId);
			statement.setString(2,this.postId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ClonebookException(e.getMessage());
		}
	}
}

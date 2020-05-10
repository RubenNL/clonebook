package nl.rubend.ipass.domain;

import nl.rubend.ipass.utils.SqlInterface;

import java.sql.PreparedStatement;
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
			PreparedStatement statement = SqlInterface.prepareStatement("INSERT INTO vote(userID,postID,vote) VALUES (?,?,?)");
			statement.setString(1, this.userId);
			statement.setString(2,this.postId);
			statement.setInt(3, this.vote);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IpassException(e.getMessage());
		}
	}
	public Post getPost() {
		return Post.getPost(postId);
	}
	public User getUser() {
		return User.getUserById(userId);
	}
}

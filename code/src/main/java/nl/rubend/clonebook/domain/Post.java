package nl.rubend.clonebook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.rubend.clonebook.UUIDGenerator;
import nl.rubend.clonebook.exceptions.ClonebookException;
import nl.rubend.clonebook.utils.SqlInterface;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Post {
	@Id
	@GeneratedValue(generator= UUIDGenerator.generatorName)
	@GenericGenerator(name = UUIDGenerator.generatorName, strategy = "nl.rubend.clonebook.UUIDGenerator")
	private String id;
	@ManyToOne
	private User user;
	@ManyToOne
	private Page page;
	@ManyToOne
	private Post repliedTo;
	private String text;
	private Date date;
	@OneToMany(mappedBy="repliedTo")
	private List<Post> replies;
	@OneToMany(mappedBy="post")
	private List<Media> media;
	@OneToMany(mappedBy="post")
	@JsonIgnore
	private List<Vote> votes;
	public void addFile(Media media) {
		this.media.add(media);
	}
	public int getVoteTotal() {
		return 0;
	}
	/*
	TODO votetotal
	public int getVoteTotal() {
		try {
			PreparedStatement statement = SqlInterface.prepareStatement("SELECT SUM(vote) as sum FROM vote WHERE postID=?");
			statement.setString(1, id);
			ResultSet set = statement.executeQuery();
			set.next();
			return set.getInt("sum");
		} catch (SQLException e) {
			throw new ClonebookException(e.getMessage());
		}
	}*/
}
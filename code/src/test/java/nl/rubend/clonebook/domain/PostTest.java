package nl.rubend.clonebook.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class PostTest {
	static Page page;
	static User user;
	static Post post;
	@BeforeAll
	static void init() {
		user=User.getUserByEmail("example3@example.com");
		if(user!=null) user.delete();
		user=new User("example3@example.com");
		page=user.getPrivatePage();
		post=new Post(user,page,null,"post");
	}
	@Test
	void subPosts() {
		Post first=new Post(user,page,null,"first");
		assertDoesNotThrow(first::getChildren);
		Post second=new Post(user,page,first,"second");
		ArrayList<Post> testArray=new ArrayList<>();
		testArray.add(second);
		assertEquals(first.getChildren(),testArray,"eerste zou als child de tweede post moeten hebben.");
		first.delete();
		assertNull(Post.getPost(second.getId()),"replies zouden weg moeten zijn na verwijderen erboven");
	}
	@Test
	void getUser() {
		assertEquals(post.getUser(),user,"eigenaar zou hetzelfde moeten blijven.");
	}
	@Test
	void getPage() {
		assertEquals(post.getPage(),page,"Page zou hetzelfde moeten blijven.");
	}
}
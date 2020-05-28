package nl.rubend.ipass.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PageTest {
	static Page page;
	static User user1;
	static User user2;
	@BeforeAll
	static void init() {
		user1=User.getUserByEmail("example1@example.com");
		if(user1!=null) user1.delete();
		user1=new User("example1@example.com");
		page=user1.getPrivatePage();
		user2=User.getUserByEmail("example2@example.com");
		if(user2!=null) user2.delete();
		user2=new User("example2@example.com");
	}
	@Test
	void lidTest() {
		assertTrue(page.isLid(user1),"eigen user zou altijd lid moeten zijn.");
		assertFalse(page.isLid(user2),"nieuwe gebruiker zou niet lid moeten zijn van andere pagina.");
		page.addLid(user2);
		assertTrue(page.isLid(user2),"nu zou de gebruiker wel lid moeten zijn.");
		page.removeLid(user2);
		assertFalse(page.isLid(user2),"nieuwe gebruiker zou niet lid meer moeten zijn van andere pagina.");
		assertTrue(page.isLid(user1),"andere gebruiker nog wel.");
	}
	@Test
	void ownerTest() {
		assertEquals(page.getOwner(),user1,"pagina zou van user1 moeten zijn.");
		page.transferOwner(user2);
		assertEquals(page.getOwner(),user2,"pagina zou nu van user2 moeten zijn.");
		page.transferOwner(user1);
		assertEquals(page.getOwner(),user1,"pagina zou weer user1 moeten zijn.");
		page.removeLid(user2);//Om de test hierboven successvol te laten verlopen...
	}
}

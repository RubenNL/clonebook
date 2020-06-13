package nl.rubend.clonebook.domain;

import nl.rubend.clonebook.exceptions.ClonebookException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

public class LidTest {
	static {
		User test=User.getUserByEmail("example4@example.com");
		if(test!=null) test.delete();
		test=User.getUserByEmail("example5@example.com");
		if(test!=null) test.delete();
	}
	User owner=new User("example4@example.com");
	User user=new User("example5@example.com");
	Page page=owner.getPrivatePage();
	@Test
	void test() {
		page.addLidAanvraag(user);
		ArrayList<HashMap<String,String>> aanvragen=owner.getLidAanvragenOpPaginas();
		assertEquals(1,aanvragen.size(),"zou maar 1 resultaat moeten zijn.");
		HashMap<String,String> aanvraag=aanvragen.get(0);
		assertEquals(user.getName(),aanvraag.get("userName"),"naam zou hetzelfde moeten zijn.");
		assertEquals(user.getId(),aanvraag.get("userId"),"userId zou hetzelfde moeten zijn.");
		assertEquals(page.getName(),aanvraag.get("pageName"),"pageName zou hetzelfde moeten zijn.");
		assertEquals(page.getId(),aanvraag.get("pageId"),"pageId zou hetzelfde moeten zijn.");
		assertThrows(ClonebookException.class,()->page.addLidAanvraag(user),"zou geen nieuwe lidaanvraag mogen doen als er al een is.");
		page.acceptUser(user);
		assertTrue(user.getPages().contains(page),"pagina zou nu in users pages moeten zitten.");
		assertTrue(page.isLid(user),"user zou nu lid moeten zijn.");
		assertThrows(ClonebookException.class,()->page.addLidAanvraag(user),"zou geen nieuwe lidaanvraag mogen doen als gebruiker al lid is.");
	}
}

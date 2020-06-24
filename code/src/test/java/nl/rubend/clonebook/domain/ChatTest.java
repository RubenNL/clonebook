package nl.rubend.clonebook.domain;

import nl.rubend.clonebook.exceptions.ClonebookException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChatTest {
	static User user1;
	static User user2;
	static User user3;
	@BeforeAll
	static void init() {
		user1=User.getUserByEmail("example6@example.com");
		if(user1!=null) user1.delete();
		user1=new User("example6@example.com");
		user2=User.getUserByEmail("example7@example.com");
		if(user2!=null) user2.delete();
		user2=new User("example7@example.com");
		user3=User.getUserByEmail("example8@example.com");
		if(user3!=null) user3.delete();
		user3=new User("example8@example.com");
	}
	@Test void chatTest() throws InterruptedException {
		Chat chat=new Chat(user1,user2);
		assertEquals(new ArrayList<ChatMessage>(),chat.getMessagesBefore(new Date(Long.parseLong("253402128000000"))),"zou een lege lijst moeten zijn");
		chat.sendMessage(user1,"bericht1");
		assertEquals(1,chat.getMessagesBefore(new Date(Long.parseLong("253402128000000"))).size(),"aantal zou nu 1 moeten zijn");
		chat.sendMessage(user1,"bericht2");
		chat.sendMessage(user1,"bericht3");
		chat.sendMessage(user1,"bericht4");
		chat.sendMessage(user1,"bericht5");
		chat.sendMessage(user1,"bericht6");
		chat.sendMessage(user1,"bericht7");
		chat.sendMessage(user1,"bericht8");
		chat.sendMessage(user1,"bericht9");
		chat.sendMessage(user1,"bericht10");
		chat.sendMessage(user1,"bericht11");
		chat.sendMessage(user1,"bericht12");
		assertEquals(10,chat.getMessagesBefore(new Date(Long.parseLong("253402128000000"))).size(),"zou maar 10 berichten moeten geven");
		ArrayList<ChatMessage> messages=chat.getMessagesBefore(new Date(Long.parseLong("253402128000000")));
		ChatMessage lastMessage=messages.get(0);
		assertEquals(user1.getId(),lastMessage.getUserId(),"zou allemaal van gebruiker 1 verstuurd moeten zijn");
		assertEquals("bericht12",lastMessage.getMessage(),"laatste zou het laatste bericht moeten zijn.");
		ChatMessage firstMessage=messages.get(9);
		assertEquals("bericht3",firstMessage.getMessage(),"eerste zou bericht3 moeten zijn.");
		ArrayList<ChatMessage> before2=chat.getMessagesBefore(firstMessage.getDate());
		assertEquals(2,before2.size(),"zou alleen bericht1 en bericht2 moeten zijn");
		assertThrows(ClonebookException.class,()->chat.sendMessage(user3,"test"),"zou een fout moeten geven, een niet-deelnemer zou geen berichten mogen sturen.");
		assertThrows(ClonebookException.class,()->chat.sendMessage(user2,""),"lege berichten zijn niet toegestaan");
	}
}

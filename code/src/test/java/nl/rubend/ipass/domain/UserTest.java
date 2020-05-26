package nl.rubend.ipass.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.NotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
	User user;
	@BeforeEach
	void addTestUser() {
		try {
			user=User.getUserByEmail("test@example.com");
		} catch (NotFoundException e) {
			System.out.println("gebruiker bestond nog niet, geen probleem.");
		}
		if(user!=null) assertDoesNotThrow(()->user.delete(),"delete functie zou geen fouten moeten geven");
		assertDoesNotThrow(()->user=new User("test@example.com"),"nieuwe gebruiker aanmaken zou geen fouten moeten geven, oude is net verwijderd.");
	}
	@Test
	void newAccountGenerate() {
		String recoveryCode=new NewPassword(user).getCode();
		assertNotNull(recoveryCode,"code should not be empty");
		User user2=NewPassword.use(recoveryCode);
		assertEquals(user,user2,"after recovery both users should be the same");
		assertThrows(
				IpassException.class,
				() -> NewPassword.use(recoveryCode),
				"Code zou niet opnieuw gebruikt moeten kunnen worden"
		);
	}
	@Test
	void newPasswordTestGoed() {
		assertDoesNotThrow(()->user.setPassword("12345678"),"zou geen foutmelding moeten geven bij een goed wachtwoord");
	}
	@Test
	void newPasswordTestShort() {
		assertThrows(IpassException.class,()->user.setPassword("1234567"),"zou een foutmelding moeten geven bij een tekort wachtwoord");
	}
	@Test
	void wachtwoordVerify() {
		user.setPassword("1234567890");
		assertTrue(user.checkPassword("1234567890"),"correct wachtwoord zou moeten werken");
		assertFalse(user.checkPassword("1234567891"),"incorrect wachtwoord zou niet moeten werken");
	}
}
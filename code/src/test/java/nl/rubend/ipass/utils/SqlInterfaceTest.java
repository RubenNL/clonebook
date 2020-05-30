package nl.rubend.ipass.utils;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class SqlInterfaceTest {

	@Test
	void checkSqliteConnection() {
		assertDoesNotThrow(() -> {
			ResultSet query=SqlInterface.prepareStatement("SELECT YEAR(CURDATE()) AS year").executeQuery();
			query.next();
			assertEquals(2020,query.getInt("year"),"Test zou 2020 moeten geven.");
		});
	}
}
package nl.rubend.ipass.utils;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class SqlInterfaceTest {

	@Test
	void checkSqliteConnection() {
		assertDoesNotThrow(() -> {
			ResultSet query=SqlInterface.executeQuery("SELECT YEAR(CURDATE()) AS year");
			query.next();
			assertEquals(2020,query.getInt("year"),"Test zou 2020 moeten geven.");
		});
	}
}
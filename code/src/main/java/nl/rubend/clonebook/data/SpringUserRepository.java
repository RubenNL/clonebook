package nl.rubend.clonebook.data;

import nl.rubend.clonebook.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringUserRepository extends JpaRepository<User,String> {
	@Query("SELECT users from Users users WHERE users.email=:email")
	Optional<User> findByEmail(@Param("email") String email);
}
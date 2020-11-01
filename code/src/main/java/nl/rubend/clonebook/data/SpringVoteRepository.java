package nl.rubend.clonebook.data;

import nl.rubend.clonebook.domain.User;
import nl.rubend.clonebook.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringVoteRepository extends JpaRepository<Vote,String> {
	List<Vote> findByUser(User user);
}

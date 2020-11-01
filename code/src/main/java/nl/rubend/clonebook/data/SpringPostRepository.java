package nl.rubend.clonebook.data;

import nl.rubend.clonebook.domain.Post;
import nl.rubend.clonebook.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringPostRepository extends JpaRepository<Post,String> {
	List<Post> findByUser(User user);
	@Query("SELECT post FROM Post post WHERE post.date > :date AND :limit=:limit ORDER BY post.date ASC") //TODO limit
	List<Post> getPostsLimit(@Param("date") LocalDateTime date,@Param("limit") int limit);
}

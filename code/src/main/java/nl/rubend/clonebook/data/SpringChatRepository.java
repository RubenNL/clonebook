package nl.rubend.clonebook.data;

import nl.rubend.clonebook.domain.Chat;
import nl.rubend.clonebook.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringChatRepository extends JpaRepository<Chat,String>{
	@Query("SELECT chat from Chat chat WHERE :user member of chat.users")
	List<Chat> findByUser(@Param("user") User user);
}

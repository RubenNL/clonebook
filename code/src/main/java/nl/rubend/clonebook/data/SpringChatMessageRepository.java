package nl.rubend.clonebook.data;

import nl.rubend.clonebook.domain.Chat;
import nl.rubend.clonebook.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringChatMessageRepository extends JpaRepository<ChatMessage,String> {
	@Query("SELECT chatMessage from ChatMessage chatMessage WHERE chatMessage.chat= :chat AND chatMessage.date < :date")
	List<ChatMessage> getMessagesBefore(@Param("chat") Chat chat, @Param("date") LocalDateTime date);
}

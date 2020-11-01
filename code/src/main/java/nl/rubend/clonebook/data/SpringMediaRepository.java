package nl.rubend.clonebook.data;

import nl.rubend.clonebook.domain.Chat;
import nl.rubend.clonebook.domain.Media;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringMediaRepository extends JpaRepository<Media,String> {}

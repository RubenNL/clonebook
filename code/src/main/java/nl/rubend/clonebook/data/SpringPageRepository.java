package nl.rubend.clonebook.data;

import nl.rubend.clonebook.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringPageRepository extends JpaRepository<Page,String> {
}

package nl.rubend.clonebook.data;

import nl.rubend.clonebook.domain.NewPassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringNewPasswordRepository extends JpaRepository<NewPassword,String> { }
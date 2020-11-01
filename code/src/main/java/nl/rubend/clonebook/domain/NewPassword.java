package nl.rubend.clonebook.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nl.rubend.clonebook.UUIDGenerator;
import nl.rubend.clonebook.exceptions.ClonebookException;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
public class NewPassword {
	@Id
	@GeneratedValue(generator= UUIDGenerator.generatorName)
	@GenericGenerator(name = UUIDGenerator.generatorName, strategy = "nl.rubend.clonebook.UUIDGenerator")
	private String code;
	private LocalDateTime validUntil;
	@ManyToOne
	private User user;
	@PrePersist
	private void setValidUntil() {
		this.validUntil=LocalDateTime.now().plusHours(1);
	}
	public User use() throws ClonebookException {
		if (validUntil.isBefore(LocalDateTime.now())) return user;
		else throw new ClonebookException("GONE","Code is niet meer geldig.");
	}
}
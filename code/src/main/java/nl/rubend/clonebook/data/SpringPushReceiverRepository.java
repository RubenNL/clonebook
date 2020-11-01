package nl.rubend.clonebook.data;

import nl.rubend.clonebook.domain.PushReceiver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringPushReceiverRepository extends JpaRepository<PushReceiver,String> { }

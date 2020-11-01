package nl.rubend.clonebook.security;

import nl.rubend.clonebook.data.SpringUserRepository;
import nl.rubend.clonebook.domain.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class UserService implements UserDetailsService {
	private final SpringUserRepository userRepository;


	public UserService(SpringUserRepository repository) {
		this.userRepository = repository;
	}

	@Override
	public User loadUserByUsername(String username) {
		return this.userRepository.findByEmail(username)
				.orElseThrow(() -> new UsernameNotFoundException(username));
	}
}

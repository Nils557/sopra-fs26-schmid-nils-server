package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;
import java.util.UUID;

import java.time.LocalDateTime;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;

	public UserService(@Qualifier("userRepository") UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> getUsers() {
		return this.userRepository.findAll();
	}

	public User createUser(User newUser) {
		newUser.setToken(UUID.randomUUID().toString());
		newUser.setStatus(UserStatus.OFFLINE);
		newUser.setCreationDate(LocalDateTime.now());

		if (newUser.getName() == null) {
			newUser.setName(newUser.getUsername());
		}

		checkIfUserExists(newUser);

		//No empty password field
		if (newUser.getPassword() == null || newUser.getPassword().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a password!");
		}

		// saves the given entity but data is only persisted in the database once
		// flush() is called
		newUser = userRepository.save(newUser);
		userRepository.flush();

		log.debug("Created Information for User: {}", newUser);
		newUser.setStatus(UserStatus.ONLINE);
		return newUser;
	}

	public User loginUser(User userInput) {
		User userByUsername = userRepository.findByUsername(userInput.getUsername());
		if  (userByUsername == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Username not found. Please register first.");
		}

		if (!userByUsername.getPassword().equals(userInput.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong password!");
		}

		userByUsername.setStatus(UserStatus.ONLINE);

		return userRepository.save(userByUsername);
	}

	public void logoutUser(Long id) {
		User user = userRepository.findById(id).orElse(null);
		if (user != null) {
			user.setStatus(UserStatus.OFFLINE);
			userRepository.save(user);
		}
	}

	public void updatePassword(Long id, String newPassword, String token) {
		User userById = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		if (token == null || !userById.getToken().equals(token)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
				"You are not allowed to change this password!");
		}

		userById.setPassword(newPassword);
		userRepository.save(userById);
		userRepository.flush();
	}

	/**
	 * This is a helper method that will check the uniqueness criteria of the
	 * username and the name
	 * defined in the User entity. The method will do nothing if the input is unique
	 * and throw an error otherwise.
	 *
	 * @param userToBeCreated
	 * @throws org.springframework.web.server.ResponseStatusException
	 * @see User
	 */
	private void checkIfUserExists(User userToBeCreated) {
		User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
		User userByName = userRepository.findByName(userToBeCreated.getName());

		String baseErrorMessage = "This username is already taken.";
		if (userByUsername != null && userByName != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format(baseErrorMessage, "username and the name", "are"));
		} else if (userByUsername != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "username", "is"));
		} else if (userByName != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "name", "is"));
		}
	}
}

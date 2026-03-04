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

	//prints debug/info to the console during runtime
	private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;

	//Constructor injection — @Qualifier specifies WHICH repository bean to inject
    //(matches the "userRepository" name from @Repository("userRepository"))
	public UserService(@Qualifier("userRepository") UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	//get all users
	public List<User> getUsers() {
		return this.userRepository.findAll(); //built-in: SELECT * FROM users
	}

	//create a new user
	public User createUser(User newUser) {
		//Auto-generate a unique token for authentication (random UUID string)
		newUser.setToken(UUID.randomUUID().toString());
		newUser.setStatus(UserStatus.OFFLINE);
		newUser.setCreationDate(LocalDateTime.now());
		//if no name is given -> username = name
		if (newUser.getName() == null) {
			newUser.setName(newUser.getUsername());
		}
		//check if the username exists else error 404 duplicate
		checkIfUserExists(newUser);

		//No empty password field
		if (newUser.getPassword() == null || newUser.getPassword().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a password!");
		}
		//if no bio is given set it to "" (for Postman)
		if (newUser.getBio() == null) {
				newUser.setBio(""); 
			}

		// saves the given entity but data is only persisted in the database once
		// flush() is called, forces the SQL to execute immediately
		newUser = userRepository.save(newUser);
		userRepository.flush();
		
		//log the creationdate set the user to online
		log.debug("Created Information for User: {}", newUser);
		newUser.setStatus(UserStatus.ONLINE);
		return newUser;
	}
	//login an existing user
	public User loginUser(User userInput) {
		User userByUsername = userRepository.findByUsername(userInput.getUsername());
		if  (userByUsername == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Username not found. Please register first.");
		}
		//password check
		if (!userByUsername.getPassword().equals(userInput.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong password!");
		}

		userByUsername.setStatus(UserStatus.ONLINE);

		return userRepository.save(userByUsername);
	}

	//logout user
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


	public void updateUserProfile(Long id, User userInput) {
    User userById = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    if (userInput.getUsername() != null && !userInput.getUsername().isBlank()) {
        userById.setUsername(userInput.getUsername());
    }
    
    if (userInput.getBio() != null) {
        userById.setBio(userInput.getBio());
    }

    userRepository.save(userById);
    userRepository.flush(); //lika actually sending it
}

	public User getUserById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
					String.format("User with ID %d was not found", id)));
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

package ch.uzh.ifi.hase.soprafs26.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;


@DataJpaTest
public class UserRepositoryIntegrationTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void findByName_success() {
		// given
		User user = new User();
		user.setBio("Hi");
		user.setUsername("firstname@lastname");
		user.setStatus(UserStatus.OFFLINE);
		user.setToken("1");
		user.setCreationDate(LocalDateTime.now());
		user.setName("Test");
		user.setPassword("1234");

		entityManager.persist(user);
		entityManager.flush();

		// when
		User found = userRepository.findByName(user.getName());

		// then
		assertNotNull(found.getId());
		assertEquals(found.getName(), user.getName());
		assertEquals(found.getUsername(), user.getUsername());
		assertEquals(found.getToken(), user.getToken());
		assertEquals(found.getStatus(), user.getStatus());
	}

	@Test
	public void findByName_returnsNull_ifNameDoesNotExist() {

	//Create a real User object and populate it with test data
    //Notice: we are NOT setting a name that matches what we'll search for
    User user = new User();
    user.setName("Name that exists"); // the name that EXISTS in the DB
    user.setUsername("username");
    user.setPassword("password");
    user.setToken("1");
    user.setStatus(UserStatus.OFFLINE);
    user.setBio("Hi");
    user.setCreationDate(LocalDateTime.now());

	//Actually save the user to the in-memory test database
    entityManager.persist(user); // inserts the user into the DB
    entityManager.flush(); //// forces the SQL to execute immediately, ensuring the data is written before we query

    //Try to find a user by a name that was NEVER saved
    //"NameThatIiNotinUse" does not exist in the DB
    User found = userRepository.findByName("NameThatIsNotInUse");

    //Assert that the result is null —
    //because no user with that name exists in the DB
    assertNull(found, "Null because no username was found");
}
}

package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;



/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Test
	public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
		// given
		User user = new User();
		user.setName("Firstname Lastname");
		user.setUsername("firstname@lastname");
		user.setStatus(UserStatus.OFFLINE);

		List<User> allUsers = Collections.singletonList(user);

		// this mocks the UserService -> we define above what the userService should
		// return when getUsers() is called
		given(userService.getUsers()).willReturn(allUsers);

		// when
		MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

		// then
		mockMvc.perform(getRequest).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].username", is(user.getUsername())))
				.andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
	}

	@Test
	public void createUser_validInput_userCreated() throws Exception {
		// given
		User user = new User();
		user.setId(1L);
		user.setName("Test User");
		user.setUsername("testUsername");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setName("Test User");
		userPostDTO.setUsername("testUsername");

		given(userService.createUser(Mockito.any())).willReturn(user);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())));
	}

		//My Tests
		//post user error:409
		@Test
		public void createUser_invalidInput_errormessage() throws Exception {

		//Create a DTO (Data Transfer Object) that simulates the JSON body
    	//the client would send in the POST request
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setName("Test User");
		userPostDTO.setUsername("testUsername");

		//Tell Mockito: when createUser() is called with ANY argument,
    	//don't actually run the real method. Instead throw a 409 CONFLICT
    	//This simulates the scenario where the username already exists in the DB
		given(userService.createUser(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "This username is already taken."));


    	//Build a fake HTTP POST request to the "/users" endpoint,
    	//with Content-Type: application/json,
    	//and the userPostDTO object serialized as the JSON body
		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

    	//Fire the request through the mock MVC (simulates a real HTTP call
    	//without starting a server), then assert that the response
    	//status code is 409 CONFLICT matching the exception we set up above
		mockMvc.perform(postRequest)
				.andExpect(status().isConflict());
	}

	@Test
	public void getUser_validId_returnsUser() throws Exception {
		//Create a fake User object and populate it with test data
    	//This represents the user we expect the service to return
		User user = new User();
		user.setId(1L); //id: 1 (Long = 64Bit)
		user.setUsername("testUsername");
		user.setStatus(UserStatus.ONLINE);
		user.setName("testname");
		user.setCreationDate(LocalDateTime.now());
		user.setBio("Test");

		//Tell Mockito: when getUserById() is called with ANY long value,
    	//skip the real service logic and just return our fake user above
		given(userService.getUserById(Mockito.anyLong())).willReturn(user);

		//Build a fake HTTP GET request to "/users/1"
    	//with Content-Type: application/json
		MockHttpServletRequestBuilder getRequest = get("/users/1")
			.contentType(MediaType.APPLICATION_JSON);
		
		//Fire the request and chain multiple assertions:
		mockMvc.perform(getRequest)
				.andDo(print()) //prints full request/response to console (useful for debugging)
				.andExpect(status().isOk()) //200
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())));
	}


	@Test
	public void user_canNot_be_found() throws Exception {
		//Tell Mockito: when getUserById() is called with ANY long value,
    	//throw a 404 NOT FOUND exception with the message "User not found"
    	//This simulates the scenario where no user exists for the given ID
		given(userService.getUserById(Mockito.anyLong()))
			.willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		// 2. Build a fake HTTP GET request to "/users/1"
		MockHttpServletRequestBuilder getRequest = get("/users/1")
				.contentType(MediaType.APPLICATION_JSON);

		//Fire the request and assert:
		mockMvc.perform(getRequest)
				.andDo(print()) 
				.andExpect(status().isNotFound()); //404
	}


	//Now Tests to update a user
	//204 No content -> Update done without a response

	@Test
	public void user_update_username_no_response() throws Exception {

		//Create a fake User object with just a username set
    	//This represents the update payload the client sends
		User updateUser = new User();
		updateUser.setUsername("Testuser");

		//Tell Mockito: when updateUserProfile() is called with ANY two arguments,
    	//do absolutely nothing (don't throw, don't return anything)
    	//update methods returns void 
		Mockito.doNothing()
				.when(userService).updateUserProfile(Mockito.any(), Mockito.any());

		//Build a fake HTTP PUT request to "/users/1"
    	//with Content-Type: application/json
    	//and the updateUser object serialized as the JSON body
		MockHttpServletRequestBuilder putRequest = put("/users/1")
			    .contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(updateUser));

		mockMvc.perform(putRequest)
			.andExpect(status().isNoContent()); //204

	}

	/**
	 * Helper Method to convert userPostDTO into a JSON string such that the input
	 * can be processed
	 * Input will look like this: {"name": "Test User", "username": "testUsername"}
	 * 
	 * @param object
	 * @return string
	 */
	private String asJsonString(final Object object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JacksonException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("The request body could not be created.%s", e.toString()));
		}
	}
}
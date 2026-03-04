package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.servlet.http.HttpServletResponse;


import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController //marks it as Rest Controller
public class UserController {

	private final UserService userService;

	UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/users")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody //methodes return data not views
	// public same as global in python
	public List<UserGetDTO> getAllUsers() { // -> gives back a list that only holdes UserGetDTO objects
		// fetch all users in the internal representation
		List<User> users = userService.getUsers();
		//list is an interface (add,get...) Arraylist concret class -> using an array internally
		List<UserGetDTO> userGetDTOs = new ArrayList<>(); //<> Java infers the type from the left side

		// convert each user to the API representation
		for (User user : users) { // : means "in"
			userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user)); //INSTANCE = there is only one shared instance of the mapper
		}
		return userGetDTOs;
	}

	@PostMapping("/users") //listen to HTTP Post requests, on www.localhost:3000/users
	@ResponseStatus(HttpStatus.CREATED) //give back 201 Created
	// @ResponseBody -> Spring converts User object -> JSON and sends it
	@ResponseBody //without this Spring thinks you are returning a viewname like a HTML Template (page.tsx)

	//returns not a list it returns a aingle UerGetDTO
	//@RequestBody -> Reads the HTTP request body and converts it from JSON
	//converts into UserPostDTO
	public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO, HttpServletResponse response) {
		//DTOMapper interface responsible for converting between entities and DTOs
		//INSTANCE -> same as public instance variable
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
		User createdUser = userService.createUser(userInput);
		//Add the user's token to the response header (used for authentication)
		response.addHeader("Authorization", createdUser.getToken());	
		//Expose the Authorization header to the browser/frontend (CORS requirement)
		response.addHeader("Access-Control-Expose-Headers", "Authorization");

		//Convert the saved User -> UserGetDTO and return it as JSON
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
	}
 
	//logs in an existing user
	@PostMapping("/login")
	@ResponseBody
	public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO, HttpServletResponse response) {

		//Convert incoming DTO -> internal User entity
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
		//Pass to service to validate credentials and fetch the user
		User user = userService.loginUser(userInput);

		//Send the token back in the Authorization header
		response.addHeader("Authorization", user.getToken());
		response.addHeader("Access-Control-Expose-Headers", "Authorization");
		//Return the user as a DTO
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
	}

	//logs out the user by ID
	@PostMapping("/logout/{id}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	//@PathVariable extracts the {id} value from the URL (e.g. /logout/5 → id = 5)
	public void logoutUser(@PathVariable("id") Long id) {
		userService.logoutUser(id); //delegates to service, no response body needed
	}

	//Updates Userprofile
	@PutMapping("/users/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT) //returns 204 NO CONTENT (success, no body)
	@ResponseBody
	public void updateUser(@PathVariable("id") Long id, //extracts id from URL
						@RequestBody UserPostDTO userPostDTO, //reads update data from request body
						@RequestHeader(value = "Authorization", required = false) String token) { //reads token from request header
		
		//Convert incoming DTO -> internal User entity
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
		
		//If a new password was provided, update it separately (with token auth check)
		if (userPostDTO.getPassword() != null) {
			userService.updatePassword(id, userPostDTO.getPassword(), token);
		}
		//Update the rest of the user's profile (username, bio, etc.)
		userService.updateUserProfile(id, userInput);
	}
	
	//returns a single user by ID
	@GetMapping("/users/{userId}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO getUser(@PathVariable("userId") Long userId) {
		//Fetch the user from the service by ID (throws 404 if not found)
		User user = userService.getUserById(userId);
		//Convert User -> UserGetDTO and return as JSON
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
	}
}

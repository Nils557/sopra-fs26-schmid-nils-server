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
@RestController
public class UserController {

	private final UserService userService;

	UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/users")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<UserGetDTO> getAllUsers() {
		// fetch all users in the internal representation
		List<User> users = userService.getUsers();
		List<UserGetDTO> userGetDTOs = new ArrayList<>();

		// convert each user to the API representation
		for (User user : users) {
			userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
		}
		return userGetDTOs;
	}

	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody

	public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO, HttpServletResponse response) {
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
		User createdUser = userService.createUser(userInput);
		response.addHeader("Authorization", createdUser.getToken());	
		response.addHeader("Access-Control-Expose-Headers", "Authorization");
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
	}

	@PostMapping("/login")
	@ResponseBody
	public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO, HttpServletResponse response) {
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
		User user = userService.loginUser(userInput);
		response.addHeader("Authorization", user.getToken());
		response.addHeader("Access-Control-Expose-Headers", "Authorization");
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
	}

	@PostMapping("/logout/{id}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public void logoutUser(@PathVariable("id") Long id) {
		userService.logoutUser(id);
	}

	@PutMapping("/users/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ResponseBody
	public void updateUser(@PathVariable("id") Long id, 
						@RequestBody UserPostDTO userPostDTO, 
						@RequestHeader(value = "Authorization", required = false) String token) {
		
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
		
		if (userPostDTO.getPassword() != null) {
			userService.updatePassword(id, userPostDTO.getPassword(), token);
		}
		
		userService.updateUserProfile(id, userInput);
	}
	

	@GetMapping("/users/{userId}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO getUser(@PathVariable Long userId) {
		User user = userService.getUserById(userId);
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
	}
}

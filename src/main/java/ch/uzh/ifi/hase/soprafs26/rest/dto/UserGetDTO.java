package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;

public class UserGetDTO {

	private Long id;
	//private String name;
	private String username;
	private UserStatus status;
	private String bio;
	//private String password;
	private String creation_date;
	//private String token;

	//public String getToken() {
	//	return token;
	//}

	//public void setToken(String token) {
	//	this.token = token;
	//}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	//public String getName() {
	//	return name;
	//}

	//public void setName(String name) {
	//	this.name = name;
	//}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	//public String getPassword() {
	//	return password; 
	//}

	//public void setPassword(String password) {
	//	this.password = password;
	//}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getCreation_date() {
		return creation_date;
	}

	public void setCreation_date(String creationDate) {
		this.creation_date = creationDate;
	}
}

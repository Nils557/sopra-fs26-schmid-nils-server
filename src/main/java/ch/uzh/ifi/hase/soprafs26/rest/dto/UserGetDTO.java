package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;

public class UserGetDTO {

	private Long id;
	private String username;
	private UserStatus status;
	private String bio;
	private LocalDateTime creation_date;
	//Name was deleted, password and Token are in header

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public LocalDateTime getCreation_date() {
		return creation_date;
	}

	public void setCreation_date(LocalDateTime creationDate) {  // ← String to LocalDateTime
		this.creation_date = creationDate;
	}
}

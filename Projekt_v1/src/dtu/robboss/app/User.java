package dtu.robboss.app;

import java.util.HashSet;

public class User {

	private String username, fullName, password;
	private HashSet<UserMessage> userMessages; // TODO maybe think more about this name? "user" is redundant?

	// TODO new constructor? taking a ResultSet as argument?
	// or just think about which arguments User should have
	public User(String name, String username, String password) {
		this.fullName = name;
		this.username = username;
		this.password = password;
		this.userMessages = new HashSet<>();
	}

	public String toString(){
		return fullName + ", username: " + username + ", password: " + password;
	}
	
	/////////////////////////
	// GETTERS AND SETTERS //
	/////////////////////////

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	

	public String getFullname() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}

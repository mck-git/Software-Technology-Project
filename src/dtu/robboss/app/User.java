package dtu.robboss.app;

public class User {

	private String username;
	private String password;
	private String cpr;

	public User(String username, String password, String cpr) {
		this.username = username;
		this.password = password;
		this.cpr = cpr;

	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

}

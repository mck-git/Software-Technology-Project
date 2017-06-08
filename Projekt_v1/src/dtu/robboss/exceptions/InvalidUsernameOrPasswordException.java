package dtu.robboss.exceptions;

public class InvalidUsernameOrPasswordException extends Exception {

	private static final long serialVersionUID = 1L;
	public String getMessage(){
		return "Username must consist of lowercase letters. <br> Passwords must consist of lower- and/or uppercase letters. <br> Full name cannot be empty.";
	}
}

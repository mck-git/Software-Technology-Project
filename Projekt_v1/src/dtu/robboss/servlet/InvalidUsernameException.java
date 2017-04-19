package dtu.robboss.servlet;

public class InvalidUsernameException extends Exception {
	public String getMessage(){
		return "ERROR: username must be all lowercase letters, max 100";
	}
}

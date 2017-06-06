package dtu.robboss.exceptions;

public class InvalidUsernameException extends Exception {

	private static final long serialVersionUID = 1L;
	public String getMessage(){
		return "ERROR: username must be all lowercase letters, max 100";
	}
}

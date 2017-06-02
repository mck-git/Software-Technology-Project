package dtu.robboss.exceptions;

public class UserNotfoundException extends Exception {
	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return "No user with given username found.";
	}
	
}

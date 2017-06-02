package dtu.robboss.exceptions;

public class UserNotLoggedInException extends Exception {
	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return "Correct user is not logged in";
	}
	
}

package dtu.robboss.app;

public class UserNotLoggedInException extends Exception {

	@Override
	public String getMessage() {
		return "Correct user is not logged in";
	}
	
}

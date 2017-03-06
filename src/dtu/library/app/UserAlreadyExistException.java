package dtu.library.app;

public class UserAlreadyExistException extends Exception {
	
	@Override
	public String getMessage() {
		return "User already exists";
	}
	
}

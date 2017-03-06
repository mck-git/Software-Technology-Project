package dtu.library.app;

public class AdminNotLoggedInException extends Exception {
	@Override
	public String getMessage() {
		return "Admin not logged in";
	}
}

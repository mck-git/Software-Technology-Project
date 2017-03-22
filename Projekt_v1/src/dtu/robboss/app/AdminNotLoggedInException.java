package dtu.robboss.app;

public class AdminNotLoggedInException extends Exception {
	@Override
	public String getMessage() {
		return "Admin not logged in";
	}
}

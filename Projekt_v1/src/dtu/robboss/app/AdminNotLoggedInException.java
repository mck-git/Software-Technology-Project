package dtu.robboss.app;

public class AdminNotLoggedInException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return "Admin not logged in";
	}
}

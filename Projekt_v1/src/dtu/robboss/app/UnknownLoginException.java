package dtu.robboss.app;

public class UnknownLoginException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return "Unkown login: Username / Password incorrect";
	}
	
}

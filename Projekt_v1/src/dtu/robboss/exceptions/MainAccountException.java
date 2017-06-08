package dtu.robboss.exceptions;

public class MainAccountException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String getMessage() {
		return "Not valid action on main account";
	}

}

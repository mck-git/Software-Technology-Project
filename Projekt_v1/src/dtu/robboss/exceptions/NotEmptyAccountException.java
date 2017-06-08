package dtu.robboss.exceptions;

public class NotEmptyAccountException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String getMessage(){
		return "Account is not empty";
	}
	
}

package dtu.robboss.exceptions;

public class InvalidCreditException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String getMessage(){
		return "Invalid credit.";
	}

}

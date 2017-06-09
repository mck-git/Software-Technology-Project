package dtu.robboss.exceptions;

public class AccountException extends Exception {
	String specification;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AccountException(String specification){
		this.specification = specification;
	}
	
	public String getMessage(){
		return "Account error: " + specification;
	}

}

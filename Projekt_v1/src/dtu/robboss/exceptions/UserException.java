package dtu.robboss.exceptions;

public class UserException extends Exception {
	String specification;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UserException(String specification){
		this.specification = specification;
	}
	
	public String getMessage(){
		return "User error: " + specification;
	}

}
package dtu.robboss.exceptions;

public class UnknownLoginException extends Exception {
	String specification;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UnknownLoginException(String specification){
		this.specification = specification;
	}
	
	public String getMessage(){
		return "Login error: " + specification;
	}

}
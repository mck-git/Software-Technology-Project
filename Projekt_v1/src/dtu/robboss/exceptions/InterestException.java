package dtu.robboss.exceptions;

public class InterestException extends Exception {
	String specification;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public InterestException(String specification){
		this.specification = specification;
	}
	
	public String getMessage(){
		return "Interest error: " + specification;
	}

}
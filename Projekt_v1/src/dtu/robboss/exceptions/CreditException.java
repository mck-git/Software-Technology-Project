package dtu.robboss.exceptions;

public class CreditException extends Exception {
	String specification;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CreditException(String specification){
		this.specification = specification;
	}
	
	public String getMessage(){
		return "Credit error: " + specification;
	}

}
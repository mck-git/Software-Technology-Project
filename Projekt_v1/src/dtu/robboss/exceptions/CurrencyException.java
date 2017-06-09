package dtu.robboss.exceptions;

public class CurrencyException extends Exception {
	String specification;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CurrencyException(String specification){
		this.specification = specification;
	}
	
	public String getMessage(){
		return "Currency error: " + specification;
	}

}
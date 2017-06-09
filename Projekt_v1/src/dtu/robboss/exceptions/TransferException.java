package dtu.robboss.exceptions;

public class TransferException extends Exception {
	String specification;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public TransferException(String specification){
		this.specification = specification;
	}
	
	public String getMessage(){
		return "Transfer error: " + specification;
	}

}
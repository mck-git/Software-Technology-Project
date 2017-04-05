package dtu.robboss.app;

public class TransferException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return "Invalid transfer.";
	}
	
}

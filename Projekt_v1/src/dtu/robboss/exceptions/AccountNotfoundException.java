package dtu.robboss.exceptions;

public class AccountNotfoundException extends Exception {
	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return "No account with given account id found.";
	}
	
}

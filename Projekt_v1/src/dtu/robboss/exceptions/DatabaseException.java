package dtu.robboss.exceptions;

public class DatabaseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String getMessage(){
		return "An error occured in the database. A rollback is conducted.";
	}

}

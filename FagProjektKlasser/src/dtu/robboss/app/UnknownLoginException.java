package dtu.robboss.app;

public class UnknownLoginException extends Exception {

	@Override
	public String getMessage() {
		return "Unkown login: Username / Password incorrect";
	}
	
}

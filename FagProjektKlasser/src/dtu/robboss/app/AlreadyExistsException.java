package dtu.robboss.app;

public class AlreadyExistsException extends Exception {
	
	private String object;

	public AlreadyExistsException(String object){
		this.object = object;
	}
	
	@Override
	public String getMessage() {
		return object + " already exists";
	}
	
}

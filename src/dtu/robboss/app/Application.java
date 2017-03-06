package dtu.robboss.app;

import java.util.ArrayList;
import java.util.List;

public class Application {

	Database database = new Database();
	private boolean adminLoggedIn = false;

	String adminUserName = "admin", adminPassWord = "admin";
	
	private User userLoggedIn = null;
	
	//////////////////
	// LOGIN LOGOUT //
	//////////////////
	
	public void login(String user, String pass) throws UnknownLoginException {
		
		boolean loginFound = false;
		
		// Checks if login info matches a user in the database
		for(User u : database.users){
			if(user.equals(u.getUsername()) && pass.equals(u.getPassword())){
				userLoggedIn = u;
				loginFound = true;
				break;
			}
		}
		
		if (user.equals(adminUserName) && pass.equals(adminPassWord))
			adminLoggedIn = true;
		
		else if(!loginFound){
			throw new UnknownLoginException();
		}
		
	}
	
	public boolean adminLoggedIn() {
		return adminLoggedIn;
	}
	
	public boolean userLoggedIn() {
		return userLoggedIn != null;
	}

	public void logOut() {
		userLoggedIn = null;
		adminLoggedIn = false;
	}

	/////////////////////
	// USER MANAGEMENT //
	/////////////////////
	
	public Object userCount() {

		return database.userCount();
	}

	public void createNewUser(User user) throws AdminNotLoggedInException, UserAlreadyExistException {
		if (!adminLoggedIn)
			throw new AdminNotLoggedInException();
		
		if(database.contains(user)) 
			throw new UserAlreadyExistException();
		
		database.add(user);
	}

	public void deleteUser(User user) throws AdminNotLoggedInException {
		if (!adminLoggedIn)
			throw new AdminNotLoggedInException();
		
		database.remove(user);

	}


	

}

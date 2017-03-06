package dtu.library.app;

import java.util.ArrayList;
import java.util.List;

public class Application {

	Database database = new Database();
	private boolean adminLoggedIn = false;

	String adminUserName = "admin", adminPassWord = "admin";

	public boolean adminLoggedIn() {
		return adminLoggedIn;
	}

	public void login(String user, String pass) {
		if (user.equals(adminUserName) && pass.equals(adminPassWord))
			adminLoggedIn = true;
	}

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

	public void adminLogOut() {
		adminLoggedIn = false;

	}

}

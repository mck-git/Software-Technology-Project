package dtu.robboss.app;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class BankApplication {

	public DatabaseProtocol database;
	private boolean adminLoggedIn = false;

	String adminUserName = "admin", adminPassWord = "admin";

	private User userLoggedIn = null;

	public BankApplication(DataSource ds1){
		database = new DatabaseProtocol(ds1);
	}
	
	
	//////////////////
	// LOGIN LOGOUT //
	//////////////////

	public void login(String user, String pass) throws UnknownLoginException {

		boolean loginFound = false;

		// Checks if login info matches a user in the database
		for (User u : database.users) {
			if (user.equals(u.getUsername()) && pass.equals(u.getPassword())) {
				userLoggedIn = u;
				loginFound = true;
				break;
			}
		}

		if (user.equals(adminUserName) && pass.equals(adminPassWord))
			adminLoggedIn = true;

		else if (!loginFound) {
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

	public int userCount() {

		try {
			return database.userCount();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return -1;
	}

	public void createNewUser(User user) throws AdminNotLoggedInException, AlreadyExistsException {
		if (!adminLoggedIn)
			throw new AdminNotLoggedInException();

		if (database.containsUser(user))
			throw new AlreadyExistsException("User");

		database.addUser(user);
	}

	public void deleteUser(User user) throws AdminNotLoggedInException {
		if (!adminLoggedIn)
			throw new AdminNotLoggedInException();

		database.removeUser(user);

	}

	public User getUser(String username) {
		for (User u : database.users) {
			if (u.getUsername().equals(username)) {
				return u;
			}
		}
		return null;
	}

	////////////////////////
	// ACCOUNT MANAGEMENT //
	////////////////////////

	public void createNewAccount(Account account) throws AdminNotLoggedInException, AlreadyExistsException {
		if (!adminLoggedIn)
			throw new AdminNotLoggedInException();

		if (database.containsAccount(account))
			throw new AlreadyExistsException("Account");

		database.addAccount(account);
	}

	//TODO implement this if bragging
//	public Object accountCount() {
//		return database.accountCount();
//	}

	public void deleteAccount(Account account) throws AdminNotLoggedInException {
		if (!adminLoggedIn)
			throw new AdminNotLoggedInException();

		account.getUser().removeAccount(account);
		database.removeAccount(account);
	}

	public Account getAccount(String accountNumber) {
		for (Account a : database.accounts) {
			if (a.getAccountNumber().equals(accountNumber)) 
				return a;
		}
		return null;
	}

	//////////////////////////////
	// USER-ACCOUNT INTERACTION //
	//////////////////////////////

	public void setUserMainAccount(User user, Account newMain) throws AdminNotLoggedInException {

		if (!adminLoggedIn) {
			throw new AdminNotLoggedInException();
		}

		user.setMainAccount(newMain);
	}

	public void changeBalanceUser(User u, int amount) {

		u.getMainAccount().changeBalance(amount);

	}

	public void transferMoneyUser(User source, User target, int amount) throws UserNotLoggedInException {
		if (!(userLoggedIn == source)) {
			throw new UserNotLoggedInException();
		}

		source.getMainAccount().changeBalance(-amount);
		target.getMainAccount().changeBalance(amount);

	}

	public void changeBalanceAccount(String accountNumber, int amount) {

		getAccount(accountNumber).changeBalance(amount);

	}

}

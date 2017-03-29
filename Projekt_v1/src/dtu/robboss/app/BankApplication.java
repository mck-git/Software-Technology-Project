package dtu.robboss.app;

import java.sql.SQLException;

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

	public User login(String username, String pass) throws UnknownLoginException {

		
		User loggedInUser = getUser(username);
		
		if ( loggedInUser == null || !pass.equals(loggedInUser.getPassword().trim())) 
			throw new UnknownLoginException();

		
		if (loggedInUser.getUsername().equals(adminUserName) && loggedInUser.getPassword().equals(adminPassWord))
			adminLoggedIn = true;

		return loggedInUser;
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

	public void createNewUser(String fullname, String username, String password, String cpr) throws AdminNotLoggedInException, AlreadyExistsException {
		User newUser = new User(fullname, username, password);
		newUser.setCpr(cpr);
		
		if (!adminLoggedIn)
			throw new AdminNotLoggedInException();

		if (database.containsUser(newUser))
			throw new AlreadyExistsException("User");

		database.addUser(newUser);
	}

	public void deleteUser(User user) throws AdminNotLoggedInException {
		//TODO: Administer admin.
//		if (!adminLoggedIn)
//			throw new AdminNotLoggedInException();

		database.removeUser(user);

	}

	public User getUser(String username) {
		return database.getUser(username);
		
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
		
		return database.getAccount(accountNumber);
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

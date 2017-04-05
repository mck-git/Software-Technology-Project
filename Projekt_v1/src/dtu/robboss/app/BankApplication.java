package dtu.robboss.app;

import java.sql.SQLException;

import javax.sql.DataSource;

public class BankApplication {

	public DatabaseProtocol database;
	private boolean adminLoggedIn = true;

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

		userLoggedIn = loggedInUser;
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

	public void createUser(String fullname, String username, String password, String cpr) throws AdminNotLoggedInException, AlreadyExistsException {
		User newUser = new User(fullname, username, password);
		newUser.setCpr(cpr);
		
		if (!adminLoggedIn)
			throw new AdminNotLoggedInException();

		database.addUser(newUser);
		createAccount(newUser, true);
		
	}

	public void deleteUser(User user) throws AdminNotLoggedInException {
		//TODO: Administer admin.
//		if (!adminLoggedIn)
//			throw new AdminNotLoggedInException();
		
		//TODO: Sanitation - brugeren må kun slettes hvis alle balance på konti er 0.
		
		
		database.removeUser(user);

	}

	public User getUser(String username) {
		User userFromDatabase = database.getUser(username);
		refreshAccountsForUser(userFromDatabase);
		return userFromDatabase;
		
	}

	////////////////////////
	// ACCOUNT MANAGEMENT //
	////////////////////////

	public void createAccount(User user, boolean main) throws AdminNotLoggedInException {
		if (!adminLoggedIn)
			throw new AdminNotLoggedInException();
		database.addAccount(user, main);
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

	public void transferFromAccountToUser(Account sourceAccount, String targetUsername, String transferAmount) throws UserNotLoggedInException, TransferException, UserNotfoundException, AccountNotfoundException {

		User targetUser = getUser(targetUsername);
		if(targetUser == null)
			throw new UserNotfoundException();
		
		transferFromAccountToAccount(sourceAccount, targetUser.getMainAccount().getAccountNumber(), transferAmount);
		
	}
	
	public void transferFromAccountToAccount(Account sourceAccount, String targetAccountID, String transferAmount) throws UserNotLoggedInException, TransferException, AccountNotfoundException {
		if (userLoggedIn != sourceAccount.getUser()) {
			throw new UserNotLoggedInException();
		}
		
		double amount = Double.parseDouble(transferAmount);
		
		if(sourceAccount.getBalance() < amount || amount <= 0 || sourceAccount.getAccountNumber().equals(targetAccountID))
			throw new TransferException();
		
		System.out.println("TAID: "+targetAccountID);	
		
		Account targetAccount = database.getAccount(targetAccountID);
		if(targetAccount == null)
			throw new AccountNotfoundException();
		
		database.transferFromAccountToAccount(sourceAccount, targetAccount, amount);
		
	}

	public void changeBalanceAccount(String accountNumber, int amount) {

		getAccount(accountNumber).changeBalance(amount);

	}

	public void refreshAccountsForUser(User user) {
		
		user.getAccounts().clear();
		database.addAccountsToUser(user);
		
	}

}

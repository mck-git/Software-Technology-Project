package dtu.robboss.app;

import java.sql.SQLException;

import javax.sql.DataSource;

public class BankApplication {

	public DatabaseProtocol database;
	private Admin adminLoggedIn = null;
	private User userLoggedIn = null;


	public BankApplication(DataSource ds1){
		database = new DatabaseProtocol(ds1);
	}
	
	//////////////////
	// LOGIN LOGOUT //
	//////////////////

	public User login(String username, String pass) throws UnknownLoginException {

		
		//checks if user login is customer 
		User loggedInUser = getUser(username);		
		if (loggedInUser != null && pass.equals(loggedInUser.getPassword().trim())) 
			return loggedInUser;
			
		//checks if user login is admin
		Admin loggedInAdmin = getAdmin(username);
		if (loggedInUser != null && pass.equals(loggedInUser.getPassword().trim())) 
			return loggedInAdmin;
		
		throw new UnknownLoginException();	
	}


	public boolean adminLoggedIn() {
		return adminLoggedIn != null;
	}

	public boolean userLoggedIn() {
		return userLoggedIn != null;
	}

	public void logOut() {
		userLoggedIn = null;
		adminLoggedIn = null;
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

	public void createCustomer(String fullname, String username, String password, String cpr) throws AdminNotLoggedInException, AlreadyExistsException {
		Customer newCustomer = new Customer(fullname, username, password);
		newCustomer.setCpr(cpr);
		
		if (adminLoggedIn == null)
			throw new AdminNotLoggedInException();

		database.addUser(newCustomer);
		createAccount(newCustomer, true);
		
	}

	public void deleteUser(User user) throws AdminNotLoggedInException {
		//TODO: Administer admin.
//		if (!adminLoggedIn)
//			throw new AdminNotLoggedInException();
		
		//TODO: Sanitation - brugeren må kun slettes hvis alle balance på konti er 0.
		
		
		database.removeUser(user);

	}

	public User getUser(String username) {
		return database.getCustomer(username);

	}
	private Admin getAdmin(String username) {
		return database.getAdmin(username);
	}

	////////////////////////
	// ACCOUNT MANAGEMENT //
	////////////////////////

	public void createAccount(User user, boolean main) throws AdminNotLoggedInException {
		if (adminLoggedIn == null)
			throw new AdminNotLoggedInException();
		database.addAccount(user, main);
	}

	//TODO implement this if bragging
//	public Object accountCount() {
//		return database.accountCount();
//	}

	public void deleteAccount(Account account) throws AdminNotLoggedInException {
		if (adminLoggedIn == null)
			throw new AdminNotLoggedInException();

		account.getCustomer().removeAccount(account);
		database.removeAccount(account);
	}

	public Account getAccount(String accountNumber) {
		
		return database.getAccount(accountNumber);
	}

	//////////////////////////////
	// USER-ACCOUNT INTERACTION //
	//////////////////////////////

	public void setCustomerMainAccount(Customer customer, Account newMain) throws AdminNotLoggedInException {

		if (adminLoggedIn == null) {
			throw new AdminNotLoggedInException();
		}

		customer.setMainAccount(newMain);
	}

//	public void changeBalanceUser(User u, int amount) {
//
//		u.getMainAccount().changeBalance(amount);
//
//	}

//	public void transferMoneyUser(User source, User target, int amount) throws UserNotLoggedInException {
//		if (!(userLoggedIn == source)) {
//			throw new UserNotLoggedInException();
//		}
//
//		source.getMainAccount().changeBalance(-amount);
//		target.getMainAccount().changeBalance(amount);
//
//	}

	public void changeBalanceAccount(String accountNumber, int amount) {

		getAccount(accountNumber).changeBalance(amount);

	}

	public void refreshAccountsForCustomer(Customer customer) {
		
		customer.getAccounts().clear();
		database.addAccountsToUser(customer);
		
	}

}

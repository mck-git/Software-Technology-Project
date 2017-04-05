package dtu.robboss.app;

import java.sql.SQLException;

import javax.sql.DataSource;

public class BankApplication {

	public DatabaseProtocol database;
	private User userLoggedIn = null;


	public BankApplication(DataSource ds1){
		database = new DatabaseProtocol(ds1);
	}
	
	//////////////////
	// LOGIN LOGOUT //
	//////////////////

	public User login(String username, String pass) throws UnknownLoginException {

		
		User loggedInUser = getUser(username);
		
		//checks if user login is customer
		if (loggedInUser != null && pass.equals(loggedInUser.getPassword().trim())) 
			return loggedInUser;
			
		// If login failed, throw exception
		throw new UnknownLoginException();	
	}
	
	
	//////////////////
	// LOGIN CHECKS //
	//////////////////
	
	public boolean userLoggedIn() {
		return userLoggedIn != null;
	}

	public boolean adminLoggedIn() {
		return userLoggedIn instanceof Admin;
	}

	public boolean customerLoggedIn(){
		return userLoggedIn instanceof Customer;
	}
	

	public void logOut() {
		userLoggedIn = null;
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

	public void createCustomer(String fullname, String username, String password, String cpr) throws AlreadyExistsException {
		Customer newCustomer = new Customer(fullname, username, password);
		newCustomer.setCpr(cpr);
		
		database.addCustomer(newCustomer);
		createAccount(newCustomer, true);
		
	}

	public void createAdmin(String fullname, String username, String password, String cpr) throws AlreadyExistsException {
		Admin newAdmin = new Admin(fullname, username, password);
		
		database.addAdmin(newAdmin);
	}
	
	
	
	public void deleteUser(User user) throws AdminNotLoggedInException {
		//TODO: Administer admin.
//		if (!adminLoggedIn)
//			throw new AdminNotLoggedInException();
		
		//TODO: Sanitation - brugeren må kun slettes hvis alle balance på konti er 0.
	
		database.removeUser(user);

	}

	private User getUser(String username) {
		return database.getUser(username);
	}
	
	public Customer getCustomer(String username) {
		return database.getCustomer(username);

	}
	private Admin getAdmin(String username) {
		return database.getAdmin(username);
	}

	
	////////////////////////
	// ACCOUNT MANAGEMENT //
	////////////////////////

	public void createAccount(User user, boolean main) {
		database.addAccount(user, main);
	}

	//TODO implement this if bragging
//	public Object accountCount() {
//		return database.accountCount();
//	}

	public void deleteAccount(Account account) {
		
		account.getCustomer().removeAccount(account);
		database.removeAccount(account);
	}

	public Account getAccount(String accountNumber) {
		
		return database.getAccount(accountNumber);
	}

	//////////////////////////////
	// USER-ACCOUNT INTERACTION //
	//////////////////////////////

	public void setCustomerMainAccount(Customer customer, Account newMain) {
		customer.setMainAccount(newMain);
	}

	public void changeBalanceUser(Customer c, int amount) {

		c.getMainAccount().changeBalance(amount);

	}

	public void transferMoneyUser(Customer source, Customer target, int amount) throws UserNotLoggedInException {
		if (!(userLoggedIn == source)) {
			throw new UserNotLoggedInException();
		}

		source.getMainAccount().changeBalance(-amount);
		target.getMainAccount().changeBalance(amount);

	}

	public void changeBalanceAccount(String accountNumber, int amount) {

		getAccount(accountNumber).changeBalance(amount);

	}

	public void refreshAccountsForCustomer(Customer customer) {
		
		customer.getAccounts().clear();
		database.addAccountsToUser(customer);
		
	}

}

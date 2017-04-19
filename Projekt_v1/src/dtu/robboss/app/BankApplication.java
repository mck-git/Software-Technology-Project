package dtu.robboss.app;

import java.sql.SQLException;
import java.util.ArrayList;

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

		userLoggedIn = getUser(username);
		
		//checks if user login is customer
		if (userLoggedIn != null && pass.equals(userLoggedIn.getPassword().trim())) 
			return userLoggedIn;
			
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

	public void createAdmin(String fullname, String username, String password) throws AlreadyExistsException {
		Admin newAdmin = new Admin(fullname, username, password);
		
		database.addAdmin(newAdmin);
	}
	
	
	
	public void deleteUser(User user) {
		//TODO: Administer admin.
//		if (!adminLoggedIn)
//			throw new AdminNotLoggedInException();
		
		//TODO: Sanitation - brugeren må kun slettes hvis alle balance på konti er 0.
	
		database.removeUser(user);

	}

	public User getUser(String username) {
		return database.getUser(username);
	}
	
	public Customer getCustomer(String username) throws UserNotfoundException {
		Customer customerFromDatabase = database.getCustomer(username);
		
		if(customerFromDatabase == null)
			throw new UserNotfoundException();
		
		refreshAccountsForCustomer(customerFromDatabase);
		
		return customerFromDatabase;

	}
	
	private Admin getAdmin(String username) {
		return database.getAdmin(username);
	}
	
	////////////////////////
	// ACCOUNT MANAGEMENT //
	////////////////////////

	public void createAccount(Customer customer, boolean main) {
		database.addAccount(customer, main);
	}

	public void deleteAccount(Account account) {
		
		account.getCustomer().removeAccount(account);
		database.removeAccount(account);
	}

	public Account getAccount(String accountNumber) {
		
		return database.getAccount(accountNumber);
	}
	
	public ArrayList<Account> getAccountsByUser(String username) {
		if(username.equals(""))
			return null;
		
		return database.getAccountsByUser(username);
	}

	//////////////////////////////
	// USER-ACCOUNT INTERACTION //
	//////////////////////////////

	public void setMainAccount(Customer customer, Account newMain) {
		customer.setMainAccount(newMain);
	}
	
	public void transferFromAccountToCustomer(Account sourceAccount, String targetUsername, String transferAmount) throws UserNotLoggedInException, TransferException, UserNotfoundException, AccountNotfoundException {
		Customer targetCustomer = getCustomer(targetUsername);
		transferFromAccountToAccount(sourceAccount, targetCustomer.getMainAccount().getAccountNumber(), transferAmount);
	}	
	
	public void transferFromAccountToAccount(Account sourceAccount, String targetAccountID, String transferAmount) throws UserNotLoggedInException, TransferException, AccountNotfoundException {
		if (userLoggedIn != sourceAccount.getCustomer()) {
			throw new UserNotLoggedInException();
		}
		
		double amount = Double.parseDouble(transferAmount);
		
		if(sourceAccount.getBalance() < amount || amount <= 0 || sourceAccount.getAccountNumber().equals(targetAccountID))
			throw new TransferException();
		
		Account targetAccount = database.getAccount(targetAccountID);
		if(targetAccount == null) 
			throw new AccountNotfoundException();
		
		database.transferFromAccountToAccount(sourceAccount, targetAccount, amount);
		
	}	

	public void refreshAccountsForCustomer(Customer customer) {
		
		customer.getAccounts().clear();
		database.addAccountsToLocalCustomer(customer);
		
	}

}

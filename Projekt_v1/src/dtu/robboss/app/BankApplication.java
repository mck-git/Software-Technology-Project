package dtu.robboss.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.sql.DataSource;

import dtu.robboss.exceptions.AccountException;
import dtu.robboss.exceptions.AlreadyExistsException;
import dtu.robboss.exceptions.CreditException;
import dtu.robboss.exceptions.CurrencyException;
import dtu.robboss.exceptions.DatabaseException;
import dtu.robboss.exceptions.InterestException;
import dtu.robboss.exceptions.TransferException;
import dtu.robboss.exceptions.UnknownLoginException;
import dtu.robboss.exceptions.UserException;

public class BankApplication {

	public DatabaseProtocol database;
	private User userLoggedIn = null;

	public BankApplication(DataSource ds1) {
		database = new DatabaseProtocol(ds1);
	}

	//////////////////
	// LOGIN LOGOUT //
	//////////////////

	/**
	 * Logs in user with the given username, if username and password match in
	 * the database. User can be Customer or Admin.
	 * 
	 * @param username
	 * @param pass
	 * @return User corresponding to the login information
	 * @throws UnknownLoginException
	 *             : If login fails.
	 * @throws UserException 
	 * @throws DatabaseException 
	 */
	public User login(String username, String pass) throws UnknownLoginException, UserException, DatabaseException {

		if (username.equals(""))
			throw new UnknownLoginException("username is empty");

		// Gets user with the given username from the Database
		userLoggedIn = getUserByUsername(username);

		// checks if correct login information
		// If login failed, throw exception
		if (userLoggedIn == null || !pass.equals(userLoggedIn.getPassword().trim()))
			throw new UnknownLoginException("wrong password");

		return userLoggedIn;
	}

	/**
	 * Returns true if userLoggedIn is an Admin
	 * 
	 * @return
	 */
	public boolean adminLoggedIn() {
		return userLoggedIn instanceof Admin;
	}

	/**
	 * Returns true if userLoggedIn is a Customer
	 * 
	 * @return
	 */
	public boolean customerLoggedIn() {
		return userLoggedIn instanceof Customer;
	}

	/**
	 * Sets userLoggedIn to be null
	 */
	public void logOut() {
		userLoggedIn = null;
	}

	/////////////////////
	// USER MANAGEMENT //
	/////////////////////

	/**
	 * If currency is valid - set currency for customer both locally and
	 * externally (database)
	 * 
	 * @param customer
	 *            : Customer object to set currency for
	 * @param currency
	 *            : Currency enum to set for customer. Valid currencies are:
	 *            DKK, EUR, USD, GBP, JPY.
	 * @throws DatabaseException 
	 */
	public void setCurrency(Customer customer, Currency currency) throws DatabaseException {

		if (customer == null) {
			System.out.println("setCurrency -> customer is null");
			return;
		}

		if (!isValidCurrency(currency)) {
			System.out.println("setCurrency -> currency is invalid");
			return;
		}

		// If currency is valid - set currency for customer both locally and
		// externally (database)
		database.setCurrency(customer, currency);
		customer.setCurrency(currency);
	}

	/**
	 * 
	 * @return The number of customers in the database. If unsuccessful -
	 *         returns -1.
	 * @throws DatabaseException 
	 */
	public int customerCount() throws DatabaseException {
		return database.customerCount();
	}

	/**
	 * Creates customer with given information both locally and externally (in
	 * the database).
	 * 
	 * @param fullname
	 * @param username
	 * @param password
	 * @param currency
	 * @throws AlreadyExistsException
	 * @throws CurrencyException 
	 * @throws UserException 
	 * @throws DatabaseException 
	 */
	public void createCustomer(String fullname, String username, String password, Currency currency)
			throws AlreadyExistsException, CurrencyException, UserException, DatabaseException {

		if (!newUserHasValidParameters(fullname, username, password)) {
			System.out.println("createCustomer");
			throw new UserException("invalid username, password or empty full name");
		}

		if (!isValidCurrency(currency)) {
			System.out.println("createCustomer -> invalid currency");
			throw new CurrencyException("invalid currency");
		}

		// Create account locally and in database
		Customer newCustomer = new Customer(fullname, username, password, currency);
		database.addCustomer(newCustomer);

		// Create a new account for the created customer. New account is set to
		// Main.
		createAccount(newCustomer, "MAIN");
	}

	/**
	 * Checks if parameters are valid when creating a new customer.
	 * 
	 * @param fullname
	 * @param username
	 * @param password
	 * @param currency
	 * @return True if parameters are valid
	 */
	private boolean newUserHasValidParameters(String fullname, String username, String password) {
		if (fullname == null || fullname.equals("")) {
			System.out.println("creating user -> invalid full name");
			return false;
		}

		if (username == null || username.equals("") || username.length() > 20) {
			System.out.println("creating user -> invalid username");
			return false;
		}

		if (password == null || password.equals("")) {
			System.out.println("creating user -> invalid password");
			return false;
		}

		if (fullname == null || fullname.equals("")) {
			System.out.println("creating user -> invalid name");
			return false;
		}

		return true;
	}

	/**
	 * Checks if the given enum Valuta is a valid currency
	 * 
	 * @param currency
	 *            : Valid currencies are: DKK, EUR, USD, GBP, JPY.
	 * @return True if currency is valid.
	 */
	private boolean isValidCurrency(Currency currency) {
		if (currency == null)
			return false;

		return (currency.name().equals("DKK") || currency.name().equals("EUR") || currency.name().equals("USD")
				|| currency.name().equals("GBP") || currency.name().equals("JPY"));
	}

	public void createAdmin(String fullname, String username, String password) throws AlreadyExistsException, UserException, DatabaseException {

		if (!newUserHasValidParameters(fullname, username, password)) {
			System.out.println("createAdmin");
			throw new UserException("invalid username, password or empty full name");
		}

		Admin newAdmin = new Admin(fullname, username, password);

		database.addAdmin(newAdmin);
	}

	/**
	 * Removes given user in Database. Only removes customer, if all accounts
	 * belonging to the customer have balance 0.
	 * 
	 * @param user
	 * @throws AccountException 
	 * @throws UserException 
	 * @throws DatabaseException 
	 */
	public void removeUser(User user, boolean admin) throws AccountException, UserException, DatabaseException {
		if (user == null) {
			System.out.println("removeUser -> user is null");
			throw new UserException("user not found");
		}

		if (user instanceof Customer) {
			// If user is a customer, refresh all accounts belonging to the
			// customer.
			refreshAccountsForCustomer((Customer) user);

			// Go through all accounts for given customer, and check if balance
			// is 0
			for (Account account : ((Customer) user).getAccounts()) {
				if (!admin && account.getBalance() != 0) {
					System.out.println("removeUser -> Customer has account balance different from 0");
					throw new AccountException("balance is not zero");
				}
			}
		}

		// Remove the user.
		database.removeUser(user);

	}

	/**
	 * Gets user with the given username from the database.
	 * 
	 * @param username
	 * @return
	 * @throws UserException 
	 * @throws DatabaseException 
	 */
	public User getUserByUsername(String username) throws UserException, DatabaseException {
		if (username == null || username.equals("")) {
			System.out.println("getUserByUsername -> username is null or empty string");
			throw new UserException("unspecified username");
		}

		User foundUser = database.getUserByUsername(username);

		if(foundUser == null)
			throw new UserException("user not found");
		
		if (foundUser instanceof Customer) {
			refreshAccountsForCustomer((Customer) foundUser);

			return foundUser;
		}

		return foundUser;
	}

	////////////////////////
	// ACCOUNT MANAGEMENT //
	////////////////////////

	// ##############################
	// account: create and delete
	// ##############################

	/**
	 * Creates an account in the database, given a customer.
	 * 
	 * @param customer
	 *            : object of type Customer. All accounts must belong to a
	 *            customer.
	 * @param type
	 *            : whether the account is of type main.
	 * @throws DatabaseException 
	 */

	// TODO: sanitize for customer, must exist

	public void createAccount(Customer customer, String type) throws DatabaseException {

		if (customer == null)
			System.out.println("createAccount -> Customer is null");

		else if (customer.getMainAccount() != null && type.equals("MAIN"))
			System.out.println("createAccount -> new account is Main and customer already has a main account");

		// TODO: check database if customer has main account
		else
			database.addAccount(customer, type);
	}

	/**
	 * Deletes the account in the database. Requires that the account balance is
	 * 0, and it must not be the customer's main account.
	 * 
	 * @param account
	 *            : account object representing the account to be deleted in the
	 *            database
	 * @throws AccountException 
	 * @throws DatabaseException 
	 */
	public void removeAccount(Account account) throws AccountException, DatabaseException {
		if (account == null) {
			System.out.println("removeAccount -> account is null");
			throw new AccountException("account not found");
		}

		if (account.getBalance() != 0) {
			System.out.println("removeAccount -> account balance not zero");
			throw new AccountException("balance is not zero");
		}

		if (account.getType().equals("MAIN")) {
			System.out.println("removeAccount -> account is main account");
			throw new AccountException("cannot perform action on main account");
		}

		account.getCustomer().removeAccount(account);
		database.removeAccount(account);
		System.out.println("removeAccount -> Removed account");

	}

	// ##############################
	// account: getters
	// ##############################

	/**
	 * Fetches the account with the given ID from the database (if it exists).
	 * If if returns null, no account exists in the database with the given ID.
	 * 
	 * @param accountID
	 *            : unique String ID for accounts in database.
	 * @throws AccountException 
	 */
	public Account getAccountByID(String accountID) throws AccountException {

		// accountID must be integer
		try {
			Integer.parseInt(accountID);
			return database.getAccount(accountID);

		} catch (Exception e) {
			throw new AccountException("invalid account id");
		}

	}

	/**
	 * Returns all accounts belonging to the customer with the given username.
	 * 
	 * @param username
	 *            : unique identifier for users in database.s
	 * @return ArrayList with Account objects
	 * @throws DatabaseException 
	 */

	public ArrayList<Account> getAccountsByUsername(String username) throws DatabaseException {
		if (username.equals(""))
			return null;

		return database.getAccountsByUsername(username);
	}

	// ##############################
	// account: setters
	// ##############################
	/**
	 * Sets a new account to main, both locally (Customer object) and externally
	 * (in database).
	 * 
	 * @param customer
	 *            : Customer object, whose main account is changed
	 * @param newMain
	 *            : Account object, corresponding to the new main account for
	 *            the given customer
	 * @throws DatabaseException 
	 */
	public void setNewMainAccount(Customer customer, Account newMain) throws DatabaseException {

		if (customer == null) {
			System.out.println("setNewMainAccount -> customer is null");
			return;
		}

		if (newMain == null) {
			System.out.println("setNewMainAccount -> newMain is null");
			return;
		}

		if (!newMain.getCustomer().equals(customer)) {
			System.out.println("setNewMainAccount -> newMain does not belong to given customer");
			return;
		}

		Account oldMain = customer.getMainAccount();
		customer.setMainAccount(newMain);
		database.setNewMainAccount(oldMain, newMain);

	}

	/**
	 * Sets interest for account with the given accountID, if the interest is
	 * greater than - or equal to - zero.
	 * 
	 * @param accountID
	 *            : unique ID for accounts in database.
	 * @param interest
	 *            : double value to set interest for in the database. 1.05 -> 5%
	 * @throws InterestException 
	 * @throws DatabaseException 
	 */
	public void setInterest(String accountID, double interest) throws InterestException, DatabaseException  {
		if (interest < 0 || accountID == null || accountID.equals("")) {
			System.out.println("setInterest -> invalid interest or accountID");
			throw new InterestException("subzero interest or invalid account id");
		}

		database.setInterest(accountID, interest);
	}

	/**
	 * Sets credit for account with the given accountID, if the credit is
	 * greater than - or equal to - zero.
	 * 
	 * @param accountID
	 *            : unique ID for accounts in database.
	 * @param credit
	 *            : double value to set credit for in the database.
	 * @throws CreditException 
	 * @throws DatabaseException 
	 */
	public void setCredit(String accountID, double credit) throws CreditException, DatabaseException  {
		if (credit < 0)
			throw new CreditException("credit cannot be negative");
			
			database.setCredit(accountID, credit);
		
	}

	//////////////////////////////
	// USER-ACCOUNT INTERACTION //
	//////////////////////////////

	/**
	 * Sets main account locally for the given customer to the new main account.
	 * 
	 * @param customer
	 * @param newMain
	 */
	public void setMainAccount(Customer customer, Account newMain) {
		if (customer == null) {
			System.out.println("setMainAccount -> customer is null");
			return;
		}

		customer.setMainAccount(newMain);
	}

	// ##########################
	// TRANSFER MONEY
	// ##########################

	/**
	 * If successful, transfers money from sourceAccount to target customer's
	 * main account
	 * 
	 * @param sourceAccount
	 *            : account to send from
	 * @param targetUsername
	 *            : username of target customer
	 * @param transferAmount
	 *            : how much to transfer
	 * @param message
	 *            : String typed by the sender. Shows up in transaction history
	 *            table.
	 * @throws TransferException 
	 * @throws AccountException 
	 * @throws UserException 
	 * @throws DatabaseException 
	 * 
	 */
	public void transferFromAccountToCustomer(Account sourceAccount, String targetUsername, double transferAmount,
			String message) throws UserException, AccountException, TransferException, DatabaseException
			 {
			User targetUser = getUserByUsername(targetUsername);
			
			if(targetUser == null || targetUser instanceof Admin)
				throw new UserException("target customer not found");
			
			Customer targetCustomer = (Customer) targetUser;
			transferFromAccountToAccount(sourceAccount, targetCustomer.getMainAccount(), transferAmount, message);


	}

	/**
	 * If successful, transfers money from sourceAccount to targetAccount. The
	 * transfer details are added to the transaction history through
	 * addTransactionToTH.
	 * 
	 * @param sourceAccount
	 *            : account to send from
	 * @param targetAccount
	 *            : account to send to
	 * @param transferAmount
	 *            : how much to transfer
	 * @throws UserException 
	 * @throws AccountException 
	 * @throws TransferException 
	 * @throws DatabaseException 
	 * 
	 */
	public void transferFromAccountToAccount(Account sourceAccount, Account targetAccount, double transferAmount,
			String message) throws UserException, AccountException, TransferException, DatabaseException {

		if (userLoggedIn == null) {
			System.out.println("transferFromAccountToAccount -> userLoggedIn is null");
			throw new UserException("no user is logged in");
		}

		if (sourceAccount == null) {
			System.out.println("transferFromAccountToAccount -> sourceAccount is null");
			throw new AccountException("source account not found");
		}

		if (targetAccount == null) {
			System.out.println("transferFromAccountToAccount -> targetAccount is null");
			throw new AccountException("target account not found");
		}

		if (!userLoggedIn.getUsername().equals(sourceAccount.getCustomer().getUsername())) {
			throw new UserException("user cannot send from source account");
		}

		if (sourceAccount.getBalance() + sourceAccount.getCredit() < transferAmount || transferAmount <= 0
				|| sourceAccount.getAccountID().equals(targetAccount.getAccountID()))
			throw new TransferException("not enough funds");

		// external: complete the transfer in the database
		database.transferFromAccountToAccount(sourceAccount, targetAccount, transferAmount);

		// add transaction to transaction history
		addTransactionToTH(sourceAccount, targetAccount, transferAmount, message);

	}

	// ##########################
	// TRANSACTION HISTORY
	// ##########################

	/**
	 * Gets the transaction history for all accounts belonging to the given
	 * customer.
	 * 
	 * @param customer
	 *            : customer whose transaction history is returned
	 * 
	 * @return an ArrayList of TransactionHistoryElement objects. Each object
	 *         corresponds to one transaction.
	 * @throws DatabaseException 
	 */
	public List<TransactionHistoryElement> getTransactionHistory(Customer customer) throws DatabaseException {

		if (customer == null) {
			System.out.println("getTransactionHistory -> customer is null");
			return null;
		}

		return database.getTransactionHistory(customer);
	}

	/**
	 * Constructs a transaction history element using information from from
	 * 'transferFromAccountToAccount'.
	 * 
	 * @param sourceAccount
	 *            : source account
	 * @param targetAccount
	 *            : target account
	 * @param transferAmount
	 *            : amount sent
	 * @param message
	 *            : message for the transaction
	 * @throws DatabaseException 
	 */
	private void addTransactionToTH(Account sourceAccount, Account targetAccount, Double transferAmount,
			String message) throws DatabaseException {

		Calendar date = new GregorianCalendar();

		// date format for database: YYYY/MM/DD/hh/mm

		// Making sure that all values has correct length
		String year = "" + date.get(Calendar.YEAR);
		String month = "" + (date.get(Calendar.MONTH) + 1 < 10 ? "0" + (date.get(Calendar.MONTH) + 1)
				: date.get(Calendar.MONTH) + 1);
		String day = "" + (date.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + date.get(Calendar.DAY_OF_MONTH)
				: date.get(Calendar.DAY_OF_MONTH));
		String hour = "" + (date.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + date.get(Calendar.HOUR_OF_DAY)
				: date.get(Calendar.HOUR_OF_DAY));
		String minute = ""
				+ (date.get(Calendar.MINUTE) < 10 ? "0" + date.get(Calendar.MINUTE) : date.get(Calendar.MINUTE));

		// Finalize date string
		String dateFormated = year + "/" + month + "/" + day + "-" + hour + ":" + minute;

		// Construct TransactionHistoryElement
		String sourceID = sourceAccount.getAccountID();
		String targetID = targetAccount.getAccountID();
		String sourceUsername = sourceAccount.getCustomer().getUsername();
		String targetUsername = targetAccount.getCustomer().getUsername();
		double sourceBalance = sourceAccount.getBalance();
		double targetBalance = targetAccount.getBalance();

		TransactionHistoryElement the = new TransactionHistoryElement(dateFormated, sourceID, targetID, sourceUsername,
				targetUsername, sourceBalance, targetBalance, transferAmount, message);

		// Send information to database protocol
		database.addTransactionToHistory(the);

	}

	/**
	 * Clears all accounts for the given customer, and adds all accounts
	 * belonging to the customer in the database. Also sets Main Account for the
	 * customer through the database protocol.
	 * 
	 * @param customer
	 * @throws DatabaseException 
	 */
	public void refreshAccountsForCustomer(Customer customer) throws DatabaseException {

		if (customer == null) {
			System.out.println("refreshAccountsForCustomer -> customer is null");
			return;
		}

		customer.getAccounts().clear();
		database.addAccountsToLocalCustomer(customer);

	}

	/**
	 * Gets all accounts in the database
	 * 
	 * @return All accounts as a List of accounts
	 * @throws DatabaseException 
	 */
	public List<Account> getAllAccounts() throws DatabaseException {

		return database.getAllAccounts();

	}

	// #########################
	// BATCH JOBS
	// #########################

	/**
	 * Applies interest to all accounts in the database.
	 * @throws DatabaseException 
	 */
	public void applyInterestToAllAccounts() throws DatabaseException {

		// 1: get all accounts
		List<Account> allAccounts = database.getAllAccounts();

		// 2update balances locally according to interest
		// and update in database

		for (Account account : allAccounts) {
			double newBalance = account.getBalance() * account.getInterest();
			database.setAccountBalance(account, newBalance);
		}
	}

	/**
	 * Stores all transactions that are more than 7 days old in the Transaction
	 * Archive table.
	 * @throws DatabaseException 
	 */
	public void storeOldTransactionsInArchive() throws DatabaseException {

		// 1 Get all transactions in Transaction History
		// 2 Find ID interval for old transactions
		// 3.a Delete old transactions from Transaction History
		// 3.b Insert old transactions into Archive table
		database.moveOldTransactionsToArchive();
	}
	
	// ######################
	// DATABASE CONNECTION
	// ######################
	
	public void startDatabaseConnection(){
		database.startConnection();
	}
	
	public void closeDatabaseConnection(){
		database.closeConnection();
	}


}

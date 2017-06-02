package dtu.robboss.app;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.sql.DataSource;

public class BankApplication {

	public DatabaseProtocol database;
	private User userLoggedIn = null;

	public BankApplication(DataSource ds1) {
		database = new DatabaseProtocol(ds1);
	}

	//////////////////
	// LOGIN LOGOUT //
	//////////////////

	public User login(String username, String pass) throws UnknownLoginException {

		userLoggedIn = getUser(username);

		// checks if user login is customer
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

	public boolean customerLoggedIn() {
		return userLoggedIn instanceof Customer;
	}

	public void logOut() {
		userLoggedIn = null;
	}

	/////////////////////
	// USER MANAGEMENT //
	/////////////////////

	public void setCurrency(Customer customer, Valuta currency) {

		customer.setCurrency(currency);

		if (currency.name().equals("DKK") || currency.name().equals("EUR") || currency.name().equals("USD")
				|| currency.name().equals("GBP") || currency.name().equals("JPY"))
			database.setCurrency(customer, currency);

	}

	public int userCount() {

		try {
			return database.userCount();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return -1;
	}

	public void createCustomer(String fullname, String username, String password, Valuta currency)
			throws AlreadyExistsException {
		Customer newCustomer = new Customer(fullname, username, password, currency);
		newCustomer.setCurrency(currency);

		database.addCustomer(newCustomer);
		createAccount(newCustomer, true);

		// Creates TH table TODO: OLD
		// database.addTransactionHistoryTable(newCustomer);
	}

	public void createAdmin(String fullname, String username, String password) throws AlreadyExistsException {
		Admin newAdmin = new Admin(fullname, username, password);

		database.addAdmin(newAdmin);
	}

	public void deleteUser(User user) {
		// TODO: Administer admin.
		// if (!adminLoggedIn)
		// throw new AdminNotLoggedInException();

		// TODO: Sanitation - brugeren må kun slettes hvis alle balance på
		// konti er 0.

		database.removeUser(user);

	}

	public User getUser(String username) {
		return database.getUser(username);
	}

	public Customer getCustomer(String username) throws UserNotfoundException {
		Customer customerFromDatabase = database.getCustomer(username);

		if (customerFromDatabase == null)
			throw new UserNotfoundException();

		refreshAccountsForCustomer(customerFromDatabase);

		return customerFromDatabase;

	}

	private Admin getAdmin(String username) throws UserNotfoundException {
		Admin adminFromDatabase = database.getAdmin(username);

		if (adminFromDatabase == null)
			throw new UserNotfoundException();

		return adminFromDatabase;
	}

	////////////////////////
	// ACCOUNT MANAGEMENT //
	////////////////////////

	public void createAccount(Customer customer, boolean main) {
		database.addAccount(customer, main);
	}

	public void deleteAccount(Account account) {
		// Account has to have a balance of 0 and it can't be the users main
		// account.
		// TODO should this check be in DefaultServelet instead?
		if (account.getBalance() == 0 && !account.getType().equals("MAIN")) {
			account.getCustomer().removeAccount(account);
			database.removeAccount(account);
			System.out.println("BankApplication::deleteAccount -> Deleted account");
		} else {
			System.out.println("BankApplication::deleteAccount -> Account not deleted.");
		}
	}

	public Account getAccount(String accountNumber) {

		return database.getAccount(accountNumber);
	}

	public ArrayList<Account> getAccountsByUser(String username) {
		if (username.equals(""))
			return null;

		return database.getAccountsByUser(username);
	}

	public void setNewMainAccount(Customer customer, Account newMain) {

		Account oldMain = customer.getMainAccount();
		database.setNewMainAccount(oldMain, newMain);
		customer.setMainAccount(newMain);

	}

	//////////////////////////////
	// USER-ACCOUNT INTERACTION //
	//////////////////////////////

	public void setMainAccount(Customer customer, Account newMain) {
		customer.setMainAccount(newMain);
	}

	public void transferFromAccountToCustomer(Account sourceAccount, String targetUsername, double transferAmount,
			String message)
			throws UserNotLoggedInException, TransferException, UserNotfoundException, AccountNotfoundException {
		Customer targetCustomer = getCustomer(targetUsername);
		transferFromAccountToAccount(sourceAccount, targetCustomer.getMainAccount().getAccountNumber(), transferAmount,
				message);
	}

	/**
	 * 
	 * @param sourceAccount
	 * @param targetAccountID
	 * @param transferAmount
	 * @throws UserNotLoggedInException
	 * @throws TransferException
	 * @throws AccountNotfoundException
	 */
	public void transferFromAccountToAccount(Account sourceAccount, String targetAccountID, double transferAmount,
			String message) throws UserNotLoggedInException, TransferException, AccountNotfoundException {
		if (!userLoggedIn.getUsername().equals(sourceAccount.getCustomer().getUsername())) {
			throw new UserNotLoggedInException();
		}

		if (sourceAccount.getBalance() + sourceAccount.getCredit() < transferAmount || transferAmount <= 0
				|| sourceAccount.getAccountNumber().equals(targetAccountID))
			throw new TransferException();

		Account targetAccount = database.getAccount(targetAccountID);
		if (targetAccount == null)
			throw new AccountNotfoundException();

		database.transferFromAccountToAccount(sourceAccount, targetAccount, transferAmount);
		addTransactionToTH(sourceAccount, targetAccountID, transferAmount, message);

	}

	public List<String[]> getTransactionHistory(Customer customer) {
		return database.getTransactionHistory(customer);
	}

	private void addTransactionToTH(Account from, String to, Double amount, String message) {

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

		String dateFormated = year + "/" + month + "/" + day + "-" + hour + ":" + minute;

		database.addTransactionToTH(dateFormated, from, to, amount, message);

	}

	public void refreshAccountsForCustomer(Customer customer) {

		customer.getAccounts().clear();
		database.addAccountsToLocalCustomer(customer);

	}
	
	public List<Account> getAllAccounts() {
	
		return database.getAllAccounts();
		
	}

	public void applyInterest() {
		
		// 1: get all accounts
		
		// 2: update balances locally according to interest
		
		// 3: update in database
		
		
	}

}

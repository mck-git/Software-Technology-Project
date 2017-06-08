package dtu.robboss.app;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.sql.DataSource;

import dtu.robboss.exceptions.AlreadyExistsException;

public class DatabaseProtocol {
	private DataSource dataSource;
	private Connection con = null;

	/**
	 * database protocol is constructed (in BankApplication) with a dataSource,
	 * used to communicate with the database. For details on dataSource object,
	 * see DefaultServlet.
	 * 
	 * @param dataSource
	 */
	public DatabaseProtocol(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	////////////////
	// Connection //
	////////////////

	private Statement startStatement(){
		try {
			return con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void closeConnection() {
		try {
				con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void startConnection() {
		try {
			if(con.isClosed())
				con = dataSource.getConnection("DTU02", "FAGP2017");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}



	////////////////////
	// CUSTOMER COUNT //
	////////////////////

	/**
	 * Counts how many customers there are in the database.
	 * 
	 * @return integer corresponding to number of customers. Returns -1 if the
	 *         count fails.
	 * 
	 * @throws SQLException
	 */
	public int customerCount() {
		try {
			Statement stmt = startStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS USERCOUNT FROM DTUGRP04.CUSTOMERS");

			if (rs.next()) {
				int userCount = Integer.parseInt(rs.getString("USERCOUNT"));
				closeConnection();
				return userCount;
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	///////////
	// USERS //
	///////////

	/**
	 * Checks if the database contains the given user.
	 * 
	 * @param user
	 *            : user to search for.
	 * @return : true if user is found otherwise false.
	 */
	public boolean containsUser(User user) {

		if (user == null)
			return false;

		User userCheck = getUserByUsername(user.getUsername());
		return !(userCheck == null);
	}

	/**
	 * Removes given user from database and all accounts associated with the
	 * user. Uses given user's username to search through database in tables
	 * CUSTOMERS, ADMINS and ACCOUNTS.
	 * 
	 * Transaction history and transaction archive are unchanged.
	 * 
	 * @param user
	 *            - user to be removed from database.
	 */
	public void removeUser(User user) {

		try {

			Statement stmt = startStatement();

			if (user instanceof Customer) {
				stmt.executeUpdate("DELETE FROM DTUGRP04.ACCOUNTS WHERE USERNAME = '" + user.getUsername() + "'");
				stmt.executeUpdate("DELETE FROM DTUGRP04.CUSTOMERS WHERE USERNAME = '" + user.getUsername() + "'");

			} else
				stmt.executeUpdate("DELETE FROM DTUGRP04.ADMINS WHERE USERNAME = '" + user.getUsername() + "'");

			stmt.close();
		} catch (SQLException e) {
			System.out.println("Could not remove user.");
			e.printStackTrace();
		}

	}

	/**
	 * Finds user in the database with the given username. Searches through both
	 * USERS and ADMINS tables. Searches through customers first. If no customer
	 * is found, searches through admins. If no admin is found, returns null.
	 * 
	 * @param username
	 *            - Username of user we want to find
	 */
	public User getUserByUsername(String username) {

		Customer customer = getCustomer(username);
		if (!(customer == null)) {
			return customer;
		}

		return getAdmin(username);

	}

	// ################
	// CUSTOMERS
	// ################

	/**
	 * Adds given customer object to database in CUSTOMERS table. Parameters
	 * stored (columns in table): USERNAME, FULLNAME, PASSWORD, CURRENCY
	 * 
	 * @param customer
	 *            - customer to be added.
	 * @throws AlreadyExistsException
	 *             - if a user with given username already exists in database.
	 */
	public void addCustomer(Customer customer) throws AlreadyExistsException {
		// CUSTOMERS columns: USERNAME, FULLNAME, PASSWORD

		if (containsUser(customer))
			throw new AlreadyExistsException("User");

		try {
			Statement stmt = startStatement();

			stmt.executeUpdate("INSERT INTO DTUGRP04.CUSTOMERS (USERNAME, FULLNAME, PASSWORD, CURRENCY) VALUES('"
					+ customer.getUsername() + "', '" + customer.getFullName() + "', '" + customer.getPassword()
					+ "', '" + customer.getCurrency() + "')");

			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fetches customer from database
	 * 
	 * @param username
	 * @return User Object
	 * @throws SQLException
	 */
	public Customer getCustomer(String username) {

		if (username == null || username.equals("")) {
			System.out.println("getCustomer -> invalid username");
			return null;
		}

		try {
			Statement stmt = startStatement();

			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.CUSTOMERS WHERE USERNAME = '" + username + "'");
			if (rs.next()) {
				// if such a user exists

				Valuta currency = Valuta.currencyStringToEnum(rs.getString("CURRENCY"));
				if (currency == null) {
					System.out.println("getCustomer -> invalid currency");
					stmt.close();
					return null;
				}

				Customer customer = new Customer(rs.getString("FULLNAME"), rs.getString("USERNAME"),
						rs.getString("PASSWORD"), currency);
				stmt.close();
				return customer;
			}

			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Sets the preferred currency for customer. When the customer logs in, the
	 * userpage shows money in this currency. However: all values are stored
	 * internally (and in the database) in DKK
	 * 
	 * @param customer
	 *            : the customer whose option is set
	 * @param currency
	 *            : enum of type Valuta, corresponding to the currencies DKK,
	 *            EUR, USD, GDP, JPY
	 */
	public void setCurrency(Customer customer, Valuta currency) {

		if (customer == null) {
			
			System.out.println("setCurrency -> customer is null");
			return;
		}

		if (currency == null) {
			System.out.println("setCurrency -> currency is null");
			return;
		}

		try {
			
			Statement stmt = startStatement();

			stmt.executeUpdate("UPDATE DTUGRP04.CUSTOMERS SET CURRENCY = '" + currency.name() + "' WHERE USERNAME = '"
					+ customer.getUsername() + "'");
			
			stmt.close();
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

	// ################
	// ADMIN
	// ################

	/**
	 * Adds given admin to database in ADMINS table. Parameters in ADMINS table:
	 * USERNAME, FULLNAME, PASSWORD
	 * 
	 * @param admin
	 *            - admin to be added.
	 * @throws AlreadyExistsException
	 *             - if a user (admin or customer) with given username already
	 *             exists in database.
	 * 
	 *             Sanitization already occurred in BankApplication, so further
	 *             sanitization in DatabaseProtocol is not needed
	 */
	public void addAdmin(Admin admin) throws AlreadyExistsException {

		if (containsUser(admin))
			throw new AlreadyExistsException("User");
		try {
			Statement stmt = startStatement();
			stmt.executeUpdate("INSERT INTO DTUGRP04.ADMINS (USERNAME, FULLNAME, PASSWORD) VALUES('"
					+ admin.getUsername() + "', '" + admin.getFullName() + "', '" + admin.getPassword() + "')");
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fetches admin from database if it exists
	 * 
	 * @param username
	 *            : username for the admin searched for in the database
	 * @return Admin object
	 * @throws SQLException
	 */
	public Admin getAdmin(String username) {

		if (username == null || username.equals("")) {
			System.out.println("getAdmin -> username invalid");
			return null;
		}

		try {
			Statement stmt = startStatement();

			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.ADMINS WHERE USERNAME = '" + username + "'");
			if (rs.next()) {
				Admin admin = new Admin(rs.getString("FULLNAME"), rs.getString("USERNAME"), rs.getString("PASSWORD"));
				stmt.close();
				return admin;
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	//////////////
	// ACCOUNTS //
	//////////////

	/**
	 * Checks if the database contains the given account.
	 * 
	 * @param account
	 * @return : returns true if account is found.
	 */
	public boolean containsAccount(Account account) {
		if (account == null)
			return false;

		Account accountCheck = getAccount(account.getAccountID());
		return !(accountCheck == null);
	}

	/**
	 * Adds account to database in ACCOUNTS table. This does not take an Account
	 * as parameter as the AccountID is unknown until the account is generated
	 * in the database. Accounts are generated with values as 0.0, and added to
	 * the costumer given.
	 * 
	 * // ACCOUNTS columns in database: ID (unique), USERNAME, TYPE, BALANCE,
	 * CREDIT, INTEREST
	 * 
	 * @param customer
	 * @param main
	 */
	public void addAccount(Customer customer, boolean main) {

		if (customer == null || customer.getUsername() == null || customer.getUsername().equals("")) {
			System.out.println("addAccount -> customer is invalid");
			return;
		}

		try {
			Statement stmt = startStatement();

			stmt.executeUpdate("INSERT INTO DTUGRP04.ACCOUNTS " + "(USERNAME, TYPE, BALANCE, CREDIT, INTEREST)"
					+ "VALUES ('" + customer.getUsername() + "', '" + (main ? "MAIN" : "NORMAL") + "', 0, 0 , 1.05)");

			stmt.close();
		} catch (SQLException e) {
			System.out.println("Could not create account");
			// e.printStackTrace();
		}
	}

	/**
	 * Removes given account from database. Uses given account's ID to search
	 * through the database.
	 * 
	 * @param account
	 *            - account to be removed.
	 */
	public void removeAccount(Account account) {

		if (account == null) {
			System.out.println("removeAccount -> account is null");
			return;
		}

		try {
			Statement stmt = startStatement();

			stmt.executeUpdate("DELETE FROM DTUGRP04.ACCOUNTS WHERE ID = '" + account.getAccountID() + "'");

			stmt.close();
		} catch (SQLException e) {
			System.out.println("Could not remove account.");
			// e.printStackTrace();
		}

	}

	// ####################
	// GETTERS
	// ####################

	/**
	 * Returns account from table ACCOUNTS in the database with the given
	 * account number, if it exists. Else returns null.
	 * 
	 * @param accountID
	 * @return
	 */
	public Account getAccount(String accountID) {

		if (accountID == null || accountID.equals("")) {
			System.out.println("getAccount -> invalid accountID");
			return null;
		}
		try {
			Statement stmt = startStatement();

			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.ACCOUNTS WHERE ID = '" + accountID + "'");
			if (rs.next()) {
				Customer customer = getCustomer(rs.getString("USERNAME"));
				Account account = new Account(customer, accountID, rs.getDouble("BALANCE"), rs.getDouble("CREDIT"),
						rs.getString("TYPE"), rs.getDouble("INTEREST"));

				stmt.close();
				return account;
			}
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * Returns all accounts from ACCOUNTS in the database.
	 * 
	 * @param accountID
	 * @return an ArrayList with all accounts in the database. If no accounts
	 *         are in the table, it returns an empty list
	 */
	public List<Account> getAllAccounts() {
		List<Account> allAccounts = new ArrayList<Account>();

		try {
			Statement stmt = startStatement();

			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.ACCOUNTS");

			while (rs.next()) {
				Customer customer = getCustomer(rs.getString("USERNAME"));
				Account account = new Account(customer, rs.getString("ID"), rs.getDouble("BALANCE"),
						rs.getDouble("CREDIT"), rs.getString("TYPE"), rs.getDouble("INTEREST"));

				allAccounts.add(account);
			}

			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return allAccounts;

	}

	/**
	 * Used to return all accounts belonging to a given customer.
	 * 
	 * @param accountID
	 * @return an ArrayList with all accounts in the database belonging to the
	 *         user. If the customer has no accounts (or does not exist), it
	 *         returns an empty list.
	 */

	public ArrayList<Account> getAccountsByUsername(String username) {

		ArrayList<Account> accounts = new ArrayList<Account>();

		if (username == null || username.equals("")) {
			System.out.println("getAccountsByUsername -> invalid username");
			return accounts;
		}

		try {
			Statement stmt = startStatement();

			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.ACCOUNTS WHERE USERNAME = '" + username + "'");
			Customer customer = getCustomer(username);
			while (rs.next()) {

				Account account = new Account(customer, rs.getString("ID"), rs.getDouble("BALANCE"),
						rs.getDouble("CREDIT"), rs.getString("TYPE"), rs.getDouble("INTEREST"));
				accounts.add(account);
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return accounts;

	}

	// ####################
	// SETTERS
	// ####################

	/**
	 * 
	 * @param oldMain
	 *            an account of type main in the database belonging to a user.
	 * @param newMain
	 *            an account belonging to the same user. (checked in
	 *            BankApplication)
	 * 
	 *            Sanitization occured in BankApplication. This method is only
	 *            run through BankApplication.setNewMainAccount method.
	 */
	public void setNewMainAccount(Account oldMain, Account newMain) {

		oldMain.setType("NORMAL");
		newMain.setType("MAIN");


		try {
			
			Statement stmt = startStatement();

			stmt.executeUpdate("UPDATE DTUGRP04.ACCOUNTS SET TYPE = '" + oldMain.getType() + "' WHERE ID = '"
					+ oldMain.getAccountID() + "'");
			stmt.executeUpdate("UPDATE DTUGRP04.ACCOUNTS SET TYPE = '" + newMain.getType() + "' WHERE ID = '"
					+ newMain.getAccountID() + "'");
			
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Error in DatabaseProtocol::selectNewMainAccount");
		}

	}

	/**
	 * Sets interest for the account in the database, in ACCOUNTS table.
	 * Sanitization occured in BankApplication.setInterest method.
	 * 
	 * @param accountID
	 * @param interest
	 *            1.05 corresponds to 5% interest
	 */
	public void setInterest(String accountID, double interest) {

		try {
			Statement stmt = startStatement();
			stmt.executeUpdate(
					"UPDATE DTUGRP04.ACCOUNTS SET INTEREST = '" + interest + "' WHERE ID = '" + accountID + "'");

			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets credit for the account in the database, in ACCOUNTS table.
	 * Sanitization occured in BankApplication.setCredit method.
	 * 
	 * @param accountID
	 * @param credit
	 */
	public void setCredit(String accountID, double credit) {

		try {
			Statement stmt = startStatement();

			stmt.executeUpdate("UPDATE DTUGRP04.ACCOUNTS SET CREDIT = '" + credit + "' WHERE ID = '" + accountID + "'");

			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	// ####################
	// TRANSACTIONS
	// ####################

	/**
	 * Transfers the balances of accounts sourceAccount and targetAccount in the
	 * database. Sanitization occurred in
	 * BankApplication.transferFromAccountToAccount.
	 * 
	 * @param sourceAccount
	 *            : account to send from
	 * @param targetAccount
	 *            : account to send to
	 * @param transferAmount
	 *            : how much to transfer
	 * 
	 */
	public void transferFromAccountToAccount(Account sourceAccount, Account targetAccount, double transferAmount) {

		sourceAccount.changeBalance(-transferAmount);
		targetAccount.changeBalance(transferAmount);


		try {
			Statement stmt = startStatement();

			stmt.executeUpdate("UPDATE DTUGRP04.ACCOUNTS SET BALANCE = '" + sourceAccount.getBalance()
					+ "' WHERE ID = '" + sourceAccount.getAccountID() + "'");
			stmt.executeUpdate("UPDATE DTUGRP04.ACCOUNTS SET BALANCE = '" + targetAccount.getBalance()
					+ "' WHERE ID = '" + targetAccount.getAccountID() + "'");

			stmt.close();
		} catch (SQLException e) {

			sourceAccount.changeBalance(transferAmount);
			targetAccount.changeBalance(-transferAmount);

			System.out.println("Error in DatabaseProtocol::transferFromAccountToAccount");
		}
	}

	/**
	 * Used to update account balances when applying interest in batch job.
	 * 
	 * @param account
	 * @param newBalance
	 */
	public void setAccountBalance(Account account, double newBalance) {

		if (account == null) {
			System.out.println("setAccountBalance -> account is null");
			return;
		}


		account.setBalance(newBalance);
		try {
			
			Statement stmt = startStatement();

			stmt.executeUpdate("UPDATE DTUGRP04.ACCOUNTS SET BALANCE = '" + account.getBalance() + "' WHERE ID = '"
					+ account.getAccountID() + "'");

			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	// #########################
	// ADDING TO LOCAL FIELDS
	// #########################

	/**
	 * Fetches all accounts from the database associated with given customer.
	 * Also adds these accounts to local customer instance.
	 * 
	 * @param customer
	 *            - customer whose accounts to fetch.
	 */
	public void addAccountsToLocalCustomer(Customer customer) {

		if (customer == null) {
			System.out.println("addAccountsToLocalCustomer -> customer is null");
			return;
		}
		try {
			Statement stmt = startStatement();

			ResultSet rs = stmt
					.executeQuery("SELECT * FROM DTUGRP04.ACCOUNTS WHERE USERNAME = '" + customer.getUsername() + "'");

			while (rs.next()) {
				Account newAccount = new Account(customer, rs.getString("ID"), rs.getDouble("BALANCE"),
						rs.getDouble("CREDIT"), rs.getString("TYPE"), rs.getDouble("INTEREST"));
				if (rs.getString("TYPE").trim().equals("MAIN")) {
					customer.setMainAccount(newAccount);
				}
			}
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Error: DatabaseProtocol::addAccountsToLocalCostumer");
			// e.printStackTrace();
		}

	}

	/////////////////////////
	// TRANSACTION HISTORY //
	/////////////////////////

	/**
	 * Given a TransactionHistoryElement, store the parameter values in the
	 * database in table TRANSACTIONHISTORY.
	 *
	 * @param the
	 *            : TransactionHistoryElement, used to store information about a
	 *            transaction
	 */
	public void addTransactionToHistory(TransactionHistoryElement the) {
		// TABLECOLUMNS: DATE, FROMACCOUNT, TOACCOUNT, FROMUSER, TOUSER,
		// FROMBALANCE, TOBALANCE, AMOUNT, MESSAGE

		try {
			// extract info from TrasactionHistoryElement
			String date = the.getDate();
			String sourceAccountID = the.getSourceAccountID();
			String targetAccountID = the.getTargetAccountID();
			String sourceUsername = the.getSourceUsername();
			String targetUsername = the.getTargetUsername();
			double sourceBalance = the.getSourceBalance();
			double targetBalance = the.getTargetBalance();
			double transferAmount = the.getTransferAmount();
			String message = the.getMessage();

			Statement stmt = startStatement();
			// update database
			stmt.executeUpdate(
					"INSERT INTO DTUGRP04.TRANSACTIONHISTORY (DATE, FROMACCOUNT, TOACCOUNT, FROMUSER, TOUSER, FROMBALANCE, TOBALANCE, AMOUNT, MESSAGE) VALUES('"
							+ date + "', '" + sourceAccountID + "', '" + targetAccountID + "', '" + sourceUsername
							+ "', '" + targetUsername + "', '" + sourceBalance + "', '" + targetBalance + "', '"
							+ transferAmount + "', '" + message + "')");

			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Gets all transaction history element for the given customer. Contents are
	 * shown to the customer in a table in userpage.
	 * 
	 * @param customer
	 * @return An ArrayList of TransactionHistoryElement objects. If no elements
	 *         are found (or SQL query fails), returns empty list.
	 */
	public List<TransactionHistoryElement> getTransactionHistory(Customer customer) {

		List<TransactionHistoryElement> transactionHistory = new ArrayList<>();

		try {

			Statement stmt = startStatement();

			ResultSet th = stmt.executeQuery("SELECT * FROM DTUGRP04.TRANSACTIONHISTORY WHERE FROMUSER = '"
					+ customer.getUsername() + "' OR TOUSER = '" + customer.getUsername() + "'");
			while (th.next()) {

				TransactionHistoryElement element = new TransactionHistoryElement(th.getString("DATE"),
						th.getString("FROMACCOUNT"), th.getString("TOACCOUNT"), th.getString("FROMUSER"),
						th.getString("TOUSER"), th.getDouble("FROMBALANCE"), th.getDouble("TOBALANCE"),
						th.getDouble("AMOUNT"), th.getString("MESSAGE"));
				transactionHistory.add(element);
			}

			stmt.close();
			return transactionHistory;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return transactionHistory;
	}

	// ########################
	// TRANSACTION ARCHIVE
	// ########################

	/**
	 * Moves all transactions from the Transaction History table to the
	 * Transaction Archive, if they are older than 7 days.
	 */
	public void moveOldTransactionsToArchive() {

		try {
			Statement stmt = startStatement();

			// SELECT ALL ENTRIES IN TRANSACTION HISTORY
			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.TRANSACTIONHISTORY");
			Calendar cal = new GregorianCalendar();
			Calendar limit = new GregorianCalendar();

			// Sets the 7 day limit for elements to be moved
			limit.set(Calendar.DAY_OF_YEAR, limit.get(Calendar.DAY_OF_YEAR) - 7);

			List<TransactionHistoryElement> transactionsToArchive = new ArrayList<TransactionHistoryElement>();

			while (rs.next()) {
				String date = rs.getString("DATE");
				// 0123456789
				// date format for database: YYYY/MM/DD/hh/mm

				int year = Integer.parseInt(date.substring(0, 4));
				cal.set(Calendar.YEAR, year);

				int month = Integer.parseInt(date.substring(5, 7));
				cal.set(Calendar.MONTH, month);

				int day = Integer.parseInt(date.substring(8, 10));
				cal.set(Calendar.DAY_OF_MONTH, day);

				// If element is before the limit, convert to
				// TransactionHistoryElement and add to list
				if (cal.before(limit)) {
					String dateString = rs.getString("DATE");
					String fromAccountID = rs.getString("FROM");
					String toAccountID = rs.getString("TO");
					String fromUser = rs.getString("FROMUSER");
					String toUser = rs.getString("TOUSER");
					double fromBalance = rs.getDouble("FROMBALANCE");
					double toBalance = rs.getDouble("TOBALANCE");
					double amount = rs.getDouble("AMOUNT");

					String msg = rs.getString("MESSAGE");

					transactionsToArchive.add(new TransactionHistoryElement(dateString, fromAccountID, toAccountID,
							fromUser, toUser, fromBalance, toBalance, amount, msg));
				}

			}

			stmt.close();

			stmt = startStatement();

			// Add the TransactionHistoryElements added to the list
			// transactionsToArchive
			// - add the entries to Transaction Archive in database.
			for (TransactionHistoryElement the : transactionsToArchive) {
				// DATABASE FORMAT: DATE, FROMACCOUNT, TOACCOUNT, FROMUSER,
				// TOUSER, FROMBALANCE, TOBALANCE, AMOUNT, MESSAGE
				stmt.executeUpdate(
						"INSERT INTO DTUGRP04.TRANSACTIONARCHIVE (DATE, FROMACCOUNT, TOACCOUNT, FROMUSER, TOUSER, FROMBALANCE, TOBALANCE, AMOUNT, MESSAGE) "
								+ "VALUES" + "('" + the.getDate() + "', '" + the.getSourceAccountID() + "', '"
								+ the.getTargetAccountID() + "', '" + the.getSourceUsername() + "', '"
								+ the.getTargetUsername() + "', '" + the.getSourceBalance() + "', '"
								+ the.getTargetBalance() + "', '" + the.getTransferAmount() + "', '" + the.getMessage()
								+ "')");
			}

			stmt.close();
			stmt = startStatement();

			// Delete all entries in Transaction History that correspond to
			// TransactionHistoryElements in transactions list.
			for (TransactionHistoryElement the : transactionsToArchive) {
				stmt.executeUpdate("DELETE FROM DTUGRP04.TRANSACTIONHISTORY WHERE DATE = '" + the.getDate() + "'");
			}

			stmt.close();

		} catch (Exception e) {
			// If anything goes wrong
			e.printStackTrace();
		}

	}

}
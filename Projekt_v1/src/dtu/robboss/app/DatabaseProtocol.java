package dtu.robboss.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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


	public void closeConnection() {
		try {
			if(!con.getAutoCommit()){
				con.commit();
			}
			
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void startConnection() {
		try {
			con = dataSource.getConnection("DTU02", "FAGP2017");
			con.setAutoCommit(false);
			
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
			
			PreparedStatement customerCountPstmt = con.prepareStatement("SELECT COUNT(*) AS USERCOUNT FROM DTUGRP04.CUSTOMERS");
			ResultSet rs = customerCountPstmt.executeQuery();
			if (rs.next()) {
				int userCount = Integer.parseInt(rs.getString("USERCOUNT"));
				closeConnection();
				return userCount;
			}
			customerCountPstmt.close();
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

			if (user instanceof Customer) {
				PreparedStatement deleteCustomerPstmt = con.prepareStatement("DELETE FROM DTUGRP04.CUSTOMERS WHERE USERNAME = ?");
				PreparedStatement deleteAccountsPstmt = con.prepareStatement("DELETE FROM DTUGRP04.ACCOUNTS WHERE USERNAME = ?");
				
				deleteCustomerPstmt.setString(1, user.getUsername());
				deleteAccountsPstmt.setString(1, user.getUsername());
				
				deleteCustomerPstmt.executeUpdate();
				deleteAccountsPstmt.executeUpdate();
				
				deleteCustomerPstmt.close();
				deleteAccountsPstmt.close();
				

			} else{
				PreparedStatement deleteAdminPstmt = con.prepareStatement("DELETE FROM DTUGRP04.ADMINS WHERE USERNAME = ?");
				deleteAdminPstmt.setString(1, user.getUsername());
				deleteAdminPstmt.executeUpdate();
				deleteAdminPstmt.close();
			}

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
			PreparedStatement addCustomerPstmt = con.prepareStatement("INSERT INTO DTUGRP04.CUSTOMERS (USERNAME, FULLNAME, PASSWORD, CURRENCY) VALUES(?,?,?,?)");
			
			addCustomerPstmt.setString(1, customer.getUsername());
			addCustomerPstmt.setString(2, customer.getFullName());
			addCustomerPstmt.setString(3, customer.getPassword());
			addCustomerPstmt.setString(4, customer.getCurrency().name());

			addCustomerPstmt.executeUpdate();
			
			addCustomerPstmt.close();
			
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
			PreparedStatement getCustomerPstmt = con.prepareStatement("SELECT * FROM DTUGRP04.CUSTOMERS WHERE USERNAME = ?");
			getCustomerPstmt.setString(1, username);
			
			ResultSet rs = getCustomerPstmt.executeQuery();
			
			if (rs.next()) {
				// if such a user exists

				Valuta currency = Valuta.currencyStringToEnum(rs.getString("CURRENCY"));
				if (currency == null) {
					System.out.println("getCustomer -> invalid currency");
					getCustomerPstmt.close();
					return null;
				}

				Customer customer = new Customer(rs.getString("FULLNAME"), rs.getString("USERNAME"),
						rs.getString("PASSWORD"), currency);
				getCustomerPstmt.close();
				return customer;
			}

			getCustomerPstmt.close();

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

			PreparedStatement updateCurrencyPstmt = con.prepareStatement("UPDATE DTUGRP04.CUSTOMERS SET CURRENCY = ? WHERE USERNAME = ?");
			
			updateCurrencyPstmt.setString(1, currency.name());
			updateCurrencyPstmt.setString(2, customer.getUsername());
			
			updateCurrencyPstmt.executeUpdate();
			
			updateCurrencyPstmt.close();
			
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
			PreparedStatement insertAdminPstmt = con.prepareStatement("INSERT INTO DTUGRP04.ADMINS (USERNAME, FULLNAME, PASSWORD) VALUES(?,?,?)");
			
			insertAdminPstmt.setString(1, admin.getUsername());
			insertAdminPstmt.setString(2, admin.getFullName());
			insertAdminPstmt.setString(3, admin.getPassword());
			
			insertAdminPstmt.executeUpdate();
			
			insertAdminPstmt.close();
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
			PreparedStatement selectAdminPstmt = con.prepareStatement("SELECT * FROM DTUGRP04.ADMINS WHERE USERNAME = ?");
			selectAdminPstmt.setString(1, username);
			ResultSet rs = selectAdminPstmt.executeQuery();
			
			if (rs.next()) {
				Admin admin = new Admin(rs.getString("FULLNAME"), rs.getString("USERNAME"), rs.getString("PASSWORD"));
				selectAdminPstmt.close();
				return admin;
			}
			selectAdminPstmt.close();
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
			PreparedStatement insertAccountPstmt = con.prepareStatement("INSERT INTO DTUGRP04.ACCOUNTS (USERNAME, TYPE, BALANCE, CREDIT, INTEREST) VALUES (?,?,?,?,?)");

			insertAccountPstmt.setString(1, customer.getUsername());
			insertAccountPstmt.setString(2, main? "MAIN" : "NORMAL");
			insertAccountPstmt.setDouble(3, main? 100.0 : 0.0);
			insertAccountPstmt.setDouble(4, 0.0);
			insertAccountPstmt.setDouble(5, 1.05);
			
			insertAccountPstmt.executeUpdate();
			
			insertAccountPstmt.close();
			
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
			
			PreparedStatement deleteAccountPstmt = con.prepareStatement("DELETE FROM DTUGRP04.ACCOUNTS WHERE ID = ?");

			deleteAccountPstmt.setString(1, account.getAccountID());
			
			deleteAccountPstmt.executeUpdate();
			
			deleteAccountPstmt.close();
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
			PreparedStatement selectAccountPstmt = con.prepareStatement("SELECT * FROM DTUGRP04.ACCOUNTS WHERE ID = ?");
			
			selectAccountPstmt.setString(1, accountID);
			
			ResultSet rs = selectAccountPstmt.executeQuery();
			
			if (rs.next()) {
				Customer customer = getCustomer(rs.getString("USERNAME"));
				Account account = new Account(customer, accountID, rs.getDouble("BALANCE"), rs.getDouble("CREDIT"),
						rs.getString("TYPE"), rs.getDouble("INTEREST"));

				selectAccountPstmt.close();
				return account;
			}
			selectAccountPstmt.close();

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
			PreparedStatement selectAccountsPstmt = con.prepareStatement("SELECT * FROM DTUGRP04.ACCOUNTS");
			
			ResultSet rs = selectAccountsPstmt.executeQuery();

			while (rs.next()) {
				Customer customer = getCustomer(rs.getString("USERNAME"));
				Account account = new Account(customer, rs.getString("ID"), rs.getDouble("BALANCE"),
						rs.getDouble("CREDIT"), rs.getString("TYPE"), rs.getDouble("INTEREST"));

				allAccounts.add(account);
			}

			selectAccountsPstmt.close();
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
			PreparedStatement selectAccountsPstmt = con.prepareStatement("SELECT * FROM DTUGRP04.ACCOUNTS WHERE USERNAME = ?");

			selectAccountsPstmt.setString(1, username);
			
			ResultSet rs = selectAccountsPstmt.executeQuery();
			
			Customer customer = getCustomer(username);
			while (rs.next()) {

				Account account = new Account(customer, rs.getString("ID"), rs.getDouble("BALANCE"),
						rs.getDouble("CREDIT"), rs.getString("TYPE"), rs.getDouble("INTEREST"));
				accounts.add(account);
			}
			selectAccountsPstmt.close();
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

			
			PreparedStatement updateAccountTypePstmt = con.prepareStatement("UPDATE DTUGRP04.ACCOUNTS SET TYPE = ? WHERE ID = ?");
			
			updateAccountTypePstmt.setString(1, oldMain.getType());
			updateAccountTypePstmt.setString(2, oldMain.getAccountID());
			
			updateAccountTypePstmt.executeUpdate();
			
			updateAccountTypePstmt.setString(1, newMain.getType());
			updateAccountTypePstmt.setString(2, newMain.getAccountID());
			
			updateAccountTypePstmt.executeUpdate();
			
			updateAccountTypePstmt.close();
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
			PreparedStatement updateInterestPstmt = con.prepareStatement("UPDATE DTUGRP04.ACCOUNTS SET INTEREST = ? WHERE ID = ?");
			
			updateInterestPstmt.setDouble(1, interest);
			updateInterestPstmt.setString(2, accountID);
			
			updateInterestPstmt.executeUpdate();
			
			updateInterestPstmt.close();
			
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
			PreparedStatement updateCreditPstmt = con.prepareStatement("UPDATE DTUGRP04.ACCOUNTS SET CREDIT = ? WHERE ID = ?");

			updateCreditPstmt.setDouble(1, credit);
			updateCreditPstmt.setString(2, accountID);
			
			updateCreditPstmt.executeUpdate();
			
			updateCreditPstmt.close();
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
			PreparedStatement updateBalancePstmt = con.prepareStatement("UPDATE DTUGRP04.ACCOUNTS SET BALANCE = ? WHERE ID = ?");
			
			updateBalancePstmt.setDouble(1, sourceAccount.getBalance());
			updateBalancePstmt.setString(2, sourceAccount.getAccountID());
			updateBalancePstmt.executeUpdate();
			
			updateBalancePstmt.setDouble(1, targetAccount.getBalance());
			updateBalancePstmt.setString(2, targetAccount.getAccountID());
			updateBalancePstmt.executeUpdate();

			updateBalancePstmt.close();
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

			PreparedStatement updateBalancePstmt = con.prepareStatement("UPDATE DTUGRP04.ACCOUNTS SET BALANCE = ? WHERE ID = ?");

			updateBalancePstmt.setDouble(1, account.getBalance());
			updateBalancePstmt.setString(2, account.getAccountID());
			
			updateBalancePstmt.executeUpdate();
			
			updateBalancePstmt.close();
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
			PreparedStatement selectAccountsPstmt = con.prepareStatement("SELECT * FROM DTUGRP04.ACCOUNTS WHERE USERNAME = ?");
			selectAccountsPstmt.setString(1, customer.getUsername());

			ResultSet rs = selectAccountsPstmt.executeQuery();
			
			while (rs.next()) {
				Account newAccount = new Account(customer, rs.getString("ID"), rs.getDouble("BALANCE"),
						rs.getDouble("CREDIT"), rs.getString("TYPE"), rs.getDouble("INTEREST"));
				
				if (rs.getString("TYPE").trim().equals("MAIN")) {
					customer.setMainAccount(newAccount);
				}
			}
			selectAccountsPstmt.close();
		} catch (SQLException e) {
			System.out.println("Error: DatabaseProtocol::addAccountsToLocalCostumer");
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
			
			PreparedStatement insertThePstmt = con.prepareStatement("INSERT INTO DTUGRP04.TRANSACTIONHISTORY "
					+ "(DATE, FROMACCOUNT, TOACCOUNT, FROMUSER, TOUSER, FROMBALANCE, TOBALANCE, AMOUNT, MESSAGE) VALUES(?,?,?,?,?,?,?,?,?)");
			
			insertThePstmt.setString(1, the.getDate());
			insertThePstmt.setString(2, the.getSourceAccountID());
			insertThePstmt.setString(3, the.getTargetAccountID());
			insertThePstmt.setString(4, the.getSourceUsername());
			insertThePstmt.setString(5, the.getTargetUsername());
			insertThePstmt.setDouble(6, the.getSourceBalance());
			insertThePstmt.setDouble(7, the.getTargetBalance());
			insertThePstmt.setDouble(8, the.getTransferAmount());
			insertThePstmt.setString(9, the.getMessage());
			
			insertThePstmt.executeUpdate();
			
			insertThePstmt.close();
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

			PreparedStatement selectThePstmt = con.prepareStatement("SELECT * FROM DTUGRP04.TRANSACTIONHISTORY WHERE FROMUSER = ? OR TOUSER = ?");
			
			selectThePstmt.setString(1, customer.getUsername());
			selectThePstmt.setString(2, customer.getUsername());
			
			ResultSet th = selectThePstmt.executeQuery();
			
			
			while (th.next()) {

				TransactionHistoryElement element = new TransactionHistoryElement(th.getString("DATE"),
						th.getString("FROMACCOUNT"), th.getString("TOACCOUNT"), th.getString("FROMUSER"),
						th.getString("TOUSER"), th.getDouble("FROMBALANCE"), th.getDouble("TOBALANCE"),
						th.getDouble("AMOUNT"), th.getString("MESSAGE"));
				transactionHistory.add(element);
			}

			selectThePstmt.close();
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
			
			PreparedStatement selectThePstmt = con.prepareStatement("SELECT * FROM DTUGRP04.TRANSACTIONHISTORY");
			
			
			
			// SELECT ALL ENTRIES IN TRANSACTION HISTORY
			ResultSet rs = selectThePstmt.executeQuery();
			
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

			selectThePstmt.close();

			
			PreparedStatement insertTheToArchive = con.prepareStatement("INSERT INTO DTUGRP04.TRANSACTIONARCHIVE "
					+ "(DATE, FROMACCOUNT, TOACCOUNT, FROMUSER, TOUSER, FROMBALANCE, TOBALANCE, AMOUNT, MESSAGE) VALUES(?,?,?,?,?,?,?,?,?)");
			// Add the TransactionHistoryElements added to the list
			// transactionsToArchive
			// - add the entries to Transaction Archive in database.
			for (TransactionHistoryElement the : transactionsToArchive) {
				// DATABASE FORMAT: DATE, FROMACCOUNT, TOACCOUNT, FROMUSER,
				// TOUSER, FROMBALANCE, TOBALANCE, AMOUNT, MESSAGE
				
				
				insertTheToArchive.setString(1, the.getDate());
				insertTheToArchive.setString(2, the.getSourceAccountID());
				insertTheToArchive.setString(3, the.getTargetAccountID());
				insertTheToArchive.setString(4, the.getSourceUsername());
				insertTheToArchive.setString(5, the.getTargetUsername());
				insertTheToArchive.setDouble(6, the.getSourceBalance());
				insertTheToArchive.setDouble(7, the.getTargetBalance());
				insertTheToArchive.setDouble(8, the.getTransferAmount());
				insertTheToArchive.setString(9, the.getMessage());
				insertTheToArchive.executeUpdate();
				
			}

			insertTheToArchive.close();
			
			PreparedStatement deleteThePstmt = con.prepareStatement("DELETE FROM DTUGRP04.TRANSACTIONHISTORY WHERE DATE = ?");

			// Delete all entries in Transaction History that correspond to
			// TransactionHistoryElements in transactions list.
			for (TransactionHistoryElement the : transactionsToArchive) {
				deleteThePstmt.setString(1, the.getDate());
				deleteThePstmt.executeUpdate();
			}

			deleteThePstmt.close();

		} catch (Exception e) {
			// If anything goes wrong
			e.printStackTrace();
		}

	}

}
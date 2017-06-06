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
	private Statement stmt = null;

	////////////
	// AMOUNT //
	////////////

	public DatabaseProtocol(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public int userCount() throws SQLException {
		startConnection();
		// TODO: OLD: USERS -> CUSTOMERS
		ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS USERCOUNT FROM DTUGRP04.CUSTOMERS");

		if (rs.next()) {
			int userCount = Integer.parseInt(rs.getString("USERCOUNT"));
			closeConnection();
			return userCount;
		}
		closeConnection();
		return -1;
	}

	///////////////
	// SEARCHING //
	///////////////

	/**
	 * Checks if database contains a user with given users username. Checks both
	 * CUSTOMERS and ADMINS tables.
	 * 
	 * @param user
	 *            - user to search for.
	 * @return - true if user is found otherwise false.
	 */
	public boolean containsUser(User user) {
		User userCheck = getUser(user.getUsername());
		return !(userCheck == null);
	}

	public boolean containsAccount(Account account) {
		Account accountCheck = getAccount(account.getAccountNumber());
		return !(accountCheck == null);
	}

	////////////////////
	// ADD AND REMOVE //
	////////////////////

	public void addTransactionToTH(String date, Account from, Account to, Double amount, String message) {
		// TABLECOLUMNS: DATE, FROMACCOUNT, TOACCOUNT, FROMUSER, TOUSER, FROMBALANCE, TOBALANCE, AMOUNT, MESSAGE

		try {
			startConnection();
			stmt.executeUpdate(
					"INSERT INTO DTUGRP04.TRANSACTIONHISTORY (DATE, FROMACCOUNT, TOACCOUNT, FROMUSER, TOUSER, FROMBALANCE, TOBALANCE, AMOUNT, MESSAGE) VALUES('" 
			+ date + "', '"
			+ from.getAccountNumber() + "', '" 
			+ to.getAccountNumber() + "', '" 
			+ from.getCustomer().getUsername() + "', '" 
			+ to.getCustomer().getUsername() + "', '" 
			+ from.getBalance() + "', '" 
			+ to.getBalance() + "', '" 			
			+ amount + "', '" 
			+ message + "')");

		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
		}
		closeConnection();

	}
	
public void storeOldTransactionsInArchive() {
		
		try {
			startConnection();
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.TRANSACTIONHISTORY");
			Calendar cal = new GregorianCalendar();
			Calendar limit = new GregorianCalendar();
			
			limit.set(Calendar.DAY_OF_YEAR, limit.get(Calendar.DAY_OF_YEAR)-7);
			
			List<TransactionHistoryElement> transactions = new ArrayList<TransactionHistoryElement>();
			
			while(rs.next()) {
				String date = rs.getString("DATE");
				// 							 0123456789
				// date format for database: YYYY/MM/DD/hh/mm
				
				int year = Integer.parseInt(date.substring(0, 4));
				cal.set(Calendar.YEAR, year);

				int month = Integer.parseInt(date.substring(5,7));
				cal.set(Calendar.MONTH, month);
				
				int day = Integer.parseInt(date.substring(8, 10));
				cal.set(Calendar.DAY_OF_MONTH, day);
				
				if ( cal.before(limit) ) {
					String dateString = rs.getString("DATE");
					int fromAccountID = rs.getInt("FROM");
					int toAccountID = rs.getInt("TO");
					String fromUser = rs.getString("FROMUSER");
					String toUser = rs.getString("TOUSER");
					double fromBalance = rs.getDouble("FROMBALANCE");
					double toBalance = rs.getDouble("TOBALANCE");
					double amount = rs.getDouble("AMOUNT");
					
					String msg = rs.getString("MESSAGE");
					
					transactions.add(new TransactionHistoryElement(dateString, fromAccountID, toAccountID, fromUser, toUser, fromBalance, toBalance, amount, msg));
				}
					
			}
			closeConnection();
			
			startConnection();
			
			for (TransactionHistoryElement the : transactions) {
				//DATABASE FORMAT: DATE, FROMACCOUNT, TOACCOUNT, FROMUSER, TOUSER, FROMBALANCE, TOBALANCE, AMOUNT, MESSAGE
				stmt.executeUpdate("INSERT INTO DTUGRP04.TRANSACTIONARCHIVE (DATE, FROMACCOUNT, TOACCOUNT, FROMUSER, TOUSER, FROMBALANCE, TOBALANCE, AMOUNT, MESSAGE) "
						+ "VALUES"
						+ "('" + the.getDate()+ "', '" 
						+ the.getFromAccountID() + "', '" 
						+ the.getToAccountID() + "', '" 
						+ the.getFromUserName() + "', '" 
						+ the.getToUserName() + "', '" 
						+ the.getFromBalance() + "', '" 
						+ the.getToBalance() + "', '" 
						+ the.getAmount() + "', '" 
						+ the.getMessage() + "')");			
			}
			
			closeConnection();
			
			startConnection();
			
			for (TransactionHistoryElement the : transactions) {
				stmt.executeUpdate("DELETE FROM DTUGRP04.TRANSACTIONHISTORY WHERE DATE = '" + the.getDate() + "'");			
			}
			
			
			closeConnection();
			
		} catch (Exception e) {
			closeConnection();
			e.printStackTrace();
		}
		
	}

	// TODO: OLD
	// public void removeTransactionHistoryTable(Customer cos) {
	// String tableSQL = "DROP TABLE DTUGRP04." + cos.getUsername() + "TH ";
	// startConnection();
	// try {
	// stmt.executeUpdate(tableSQL);
	// } catch (SQLException e) {
	// e.printStackTrace();
	// closeConnection();
	// }
	// closeConnection();
	// }

	// TODO: OLD
	// /**
	// * @throws SQLException
	// *
	// */
	// public void addTransactionHistoryTable(Customer cos) {
	// // Transaction history = TH
	// String tableSQL = "CREATE TABLE DTUGRP04." + cos.getUsername() + "TH " +
	// "( DATE CHAR(16) NOT NULL, " +
	// " FROM INTEGER NOT NULL, " +
	// " TO INTEGER NOT NULL, " +
	// " AMOUNT DOUBLE NOT NULL, " +
	// " MESSAGE VARCHAR(140))" +
	// " IN DTUGRP04.DTUGRP04" +
	// " AUDIT NONE " +
	// " DATA CAPTURE NONE " +
	// " CCSID EBCDIC;";
	//
	// // Starts connection with database and adds table
	// startConnection();
	// try {
	// stmt.executeUpdate(tableSQL);
	// } catch (SQLException e) {
	// closeConnection();
	// e.printStackTrace();
	// System.out.println("ERROR: Could not add TH table with username " +
	// cos.getUsername());
	// }
	// closeConnection();
	// }

	/**
	 * Adds given admin to database in ADMINS table. username, full name and
	 * password is stored.
	 * 
	 * @param admin
	 *            - admin to be added.
	 * @throws AlreadyExistsException
	 *             - if a user with given username already exists in database.
	 */
	public void addAdmin(Admin admin) throws AlreadyExistsException {
		// ADMINS columns: USERNAME, FULLNAME, PASSWORD

		if (containsUser(admin))
			throw new AlreadyExistsException("User");
		try {
			startConnection();
			stmt.executeUpdate("INSERT INTO DTUGRP04.ADMINS (USERNAME, FULLNAME, PASSWORD) VALUES('"
					+ admin.getUsername() + "', '" + admin.getFullname() + "', '" + admin.getPassword() + "')");
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
		}
		closeConnection();
	}

	/**
	 * Adds given customer to database in CUSTOMERS table. username, full name
	 * and password is stored.
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
			startConnection();

			// TODO: OLD: USERS -> CUSTOMERS
			stmt.executeUpdate("INSERT INTO DTUGRP04.CUSTOMERS (USERNAME, FULLNAME, PASSWORD, CURRENCY) VALUES('"
					+ customer.getUsername() + "', '" + customer.getFullname() + "', '" + customer.getPassword()
					+ "', '" + customer.getCurrency() + "')");

		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
		}
		closeConnection();
	}

	/**
	 * Adds account to database in ACCOUNTS table. This does not take an Account
	 * as parameter as the AccountID is unknown until the account is generated
	 * in the database. Accounts are generated with values as 0.0, and added to
	 * the costumer given.
	 * 
	 * @param customer
	 * @param main
	 */
	public void addAccount(Customer customer, boolean main) {
		// ACCOUNTS columns: ID, USERNAME, TYPE, BALANCE, CREDIT, INTEREST

		startConnection();
		try {
			stmt.executeUpdate("INSERT INTO DTUGRP04.ACCOUNTS " + "(USERNAME, TYPE, BALANCE, CREDIT, INTEREST)"
					+ "VALUES ('" + customer.getUsername() + "', '" + (main ? "MAIN" : "NORMAL") + "', 0, 0 , 1.05)");
		} catch (SQLException e) {
			closeConnection();
			System.out.println("Could not create account");
			// e.printStackTrace();
		}
		closeConnection();
	}

	/**
	 * Removes given user from database and all accounts associated with the
	 * user. Uses given users username to search through database in tables
	 * CUSTOMERS, ADMINS and ACCOUNTS.
	 * 
	 * @param user
	 *            - user to be removed from database.
	 */
	public void removeUser(User user) {

		startConnection();
		try {
			// Removes TH table
			if (user instanceof Customer) {
				stmt.executeUpdate("DELETE FROM DTUGRP04.ACCOUNTS WHERE USERNAME = '" + user.getUsername() + "'");
				// TODO: OLD: USERS -> CUSTOMERS
				stmt.executeUpdate("DELETE FROM DTUGRP04.CUSTOMERS WHERE USERNAME = '" + user.getUsername() + "'");
				// TODO: OLD: NO LONGER REMOVES TABLE DYNAMICALLY
				// removeTransactionHistoryTable((Customer) user);
			} else
				stmt.executeUpdate("DELETE FROM DTUGRP04.ADMINS WHERE USERNAME = '" + user.getUsername() + "'");

		} catch (SQLException e) {
			closeConnection();
			System.out.println("Could not remove user.");
			e.printStackTrace();
		}
		closeConnection();

	}

	/**
	 * Removes given account from database. Uses given account's ID to search
	 * through the database.
	 * 
	 * @param account
	 *            - account to be removed.
	 */
	public void removeAccount(Account account) {

		startConnection();
		try {
			stmt.executeUpdate("DELETE FROM DTUGRP04.ACCOUNTS WHERE ID = '" + account.getAccountNumber() + "'");
		} catch (SQLException e) {
			closeConnection();
			System.out.println("Could not remove account.");
			// e.printStackTrace();
		}

		closeConnection();

	}

	/**
	 * Fetches all accounts from the database associated with given customer.
	 * Also adds these accounts to local customer instance.
	 * 
	 * @param customer
	 *            - customer whose accounts to fetch.
	 */
	public void addAccountsToLocalCustomer(Customer customer) {
		startConnection();
		try {
			ResultSet rs = stmt
					.executeQuery("SELECT * FROM DTUGRP04.ACCOUNTS WHERE USERNAME = '" + customer.getUsername() + "'");

			while (rs.next()) {
				Account newAccount = new Account(customer, rs.getString("ID"), rs.getDouble("BALANCE"),
						rs.getDouble("CREDIT"), rs.getString("TYPE"), rs.getDouble("INTEREST"));
				if (rs.getString("TYPE").trim().equals("MAIN")) {
					customer.setMainAccount(newAccount);
				}
			}
		} catch (SQLException e) {
			closeConnection();
			System.out.println("Error: DatabaseProtocol::addAccountsToLocalCostumer");
			// e.printStackTrace();
		}

		closeConnection();
	}

	////////////////////
	// Get //
	////////////////////

	/**
	 * Fetches customer from database
	 * 
	 * @param username
	 * @return User Object
	 * @throws SQLException
	 */
	public Customer getCustomer(String username) {
		startConnection();
		try {
			// TODO: OLD: USERS -> CUSTOMERS
			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.CUSTOMERS WHERE USERNAME = '" + username + "'");
			if (rs.next()) {

				Valuta currency;
				switch (rs.getString("CURRENCY")) {
				case "EUR":
					currency = Valuta.EUR;
					break;
				case "USD":
					currency = Valuta.USD;
					break;
				case "GBP":
					currency = Valuta.GBP;
					break;
				case "JPY":
					currency = Valuta.JPY;
					break;
				default:
					currency = Valuta.DKK;
				}

				Customer customer = new Customer(rs.getString("FULLNAME"), rs.getString("USERNAME"),
						rs.getString("PASSWORD"), currency);
				closeConnection();
				return customer;
			} else {
				closeConnection();
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		closeConnection();
		return null;
	}

	/**
	 * Fetches admin from database
	 * 
	 * @param username
	 * @return User Object
	 * @throws SQLException
	 */
	public Admin getAdmin(String username) {
		startConnection();
		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.ADMINS WHERE USERNAME = '" + username + "'");
			if (rs.next()) {
				Admin admin = new Admin(rs.getString("FULLNAME"), rs.getString("USERNAME"), rs.getString("PASSWORD"));
				closeConnection();
				return admin;
			} else {
				closeConnection();
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		closeConnection();
		return null;
	}

	/**
	 * Finds user in the database with the given username. Searches through both
	 * USERS and ADMINS tables.
	 * 
	 * @param username
	 *            - Username of user we want to find
	 */
	public User getUser(String username) {

		// Checks if a customer exists with the given username
		Customer c = getCustomer(username);
		if (!(c == null)) {
			return c;
		}

		// If customer does not exist, returns the admin with the given
		// username.
		// If no admin with given username exists, returns null
		return getAdmin(username);

	}

	/**
	 * Fetches account from table ACCOUNTS in the database with the given
	 * account number.
	 * 
	 * @param accountNumber
	 * @return
	 */
	public Account getAccount(String accountNumber) {
		startConnection();
		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.ACCOUNTS WHERE ID = '" + accountNumber + "'");
			if (rs.next()) {
				Customer customer = getCustomer(rs.getString("USERNAME"));
				Account account = new Account(customer, accountNumber, rs.getDouble("BALANCE"), rs.getDouble("CREDIT"),
						rs.getString("TYPE"), rs.getDouble("INTEREST"));
				closeConnection();
				return account;
			} else {
				closeConnection();
				return null;
			}
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
		}

		closeConnection();
		return null;

	}

	public List<Account> getAllAccounts() {
		startConnection();
		List<Account> allAccounts = new ArrayList<Account>();

		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.ACCOUNTS");

			if (!rs.next()) {
				return null;
			}

			while (rs.next()) {
				Customer customer = getCustomer(rs.getString("USERNAME"));
				Account account = new Account(customer, rs.getString("ID"), rs.getDouble("BALANCE"),
						rs.getDouble("CREDIT"), rs.getString("TYPE"), rs.getDouble("INTEREST"));

				allAccounts.add(account);
			}
			closeConnection();

		} catch (

		SQLException e) {
			closeConnection();
			e.printStackTrace();
		}

		closeConnection();
		return allAccounts;

	}

	public ArrayList<Account> getAccountsByUser(String username) {
		startConnection();

		ArrayList<Account> accounts = new ArrayList<Account>();
		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.ACCOUNTS WHERE USERNAME = '" + username + "'");
			Customer customer = getCustomer(username);
			while (rs.next()) {

				Account account = new Account(customer, rs.getString("ID"), rs.getDouble("BALANCE"),
						rs.getDouble("CREDIT"), rs.getString("TYPE"), rs.getDouble("INTEREST"));
				accounts.add(account);
			}
			closeConnection();
			return accounts;
		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
		}

		closeConnection();
		return null;

	}

	public List<TransactionHistoryElement> getTransactionHistory(Customer customer) {
		startConnection();

		List<TransactionHistoryElement> table = new ArrayList<>();

		try {
//			String query = "SELECT * FROM DTUGRP04.TRANSACTIONHISTORY WHERE ";
//			ArrayList<Account> accounts = customer.getAccounts();
//			for (int i = 0; i < accounts.size(); i++) {
//				query += " FROMACCOUNT = " + accounts.get(i).getAccountNumber() + " OR TOACCOUNT = "
//						+ accounts.get(i).getAccountNumber() + " ";
//				if (i < accounts.size() - 1)
//					query += " OR ";
//			}

			ResultSet th = stmt.executeQuery("SELECT * FROM DTUGRP04.TRANSACTIONHISTORY WHERE FROMUSER = '" + customer.getUsername() 
											+ "' OR TOUSER = '" + customer.getUsername() + "'");
			while (th.next()) {
//				String[] row = { th.getString("DATE"), th.getString("FROMACCOUNT"), th.getString("TOACCOUNT"), 
//						th.getString("FROMUSER"), th.getString("TOUSER"), th.getString("FROMBALANCE"), th.getString("TOBALANCE"), 
//						th.getString("AMOUNT"), th.getString("MESSAGE") };
				TransactionHistoryElement element = new TransactionHistoryElement(th.getString("DATE"), th.getInt("FROMACCOUNT"), 
						th.getInt("TOACCOUNT"), th.getString("FROMUSER"), th.getString("TOUSER"), 
						th.getDouble("FROMBALANCE"), th.getDouble("TOBALANCE"),
						th.getDouble("AMOUNT"), th.getString("MESSAGE"));
				table.add(element);
			}

			return table;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		closeConnection();
		return null;
	}

	////////////
	// Update //
	////////////

	public void setCurrency(Customer customer, Valuta currency) {
		startConnection();
		try {
			stmt.executeUpdate("UPDATE DTUGRP04.CUSTOMERS SET CURRENCY = '" + currency.name() + "' WHERE USERNAME = '"
					+ customer.getUsername() + "'");
		} catch (SQLException e) {

			e.printStackTrace();
		}
		closeConnection();
	}

	public void setNewMainAccount(Account oldMain, Account newMain) {

		oldMain.setType("NORMAL");
		newMain.setType("MAIN");

		startConnection();

		try {
			stmt.executeUpdate("UPDATE DTUGRP04.ACCOUNTS SET TYPE = '" + oldMain.getType() + "' WHERE ID = '"
					+ oldMain.getAccountNumber() + "'");
			stmt.executeUpdate("UPDATE DTUGRP04.ACCOUNTS SET TYPE = '" + newMain.getType() + "' WHERE ID = '"
					+ newMain.getAccountNumber() + "'");
		} catch (SQLException e) {
			closeConnection();
			System.out.println("Error in DatabaseProtocol::selectNewMainAccount");
		}
		closeConnection();

	}

	public void transferFromAccountToAccount(Account source, Account target, double amount) {

		source.changeBalance(-amount);
		target.changeBalance(amount);

		startConnection();

		try {
			stmt.executeUpdate("UPDATE DTUGRP04.ACCOUNTS SET BALANCE = '" + source.getBalance() + "' WHERE ID = '"
					+ source.getAccountNumber() + "'");
			stmt.executeUpdate("UPDATE DTUGRP04.ACCOUNTS SET BALANCE = '" + target.getBalance() + "' WHERE ID = '"
					+ target.getAccountNumber() + "'");

		} catch (SQLException e) {
			closeConnection();

			source.changeBalance(amount);
			target.changeBalance(-amount);

			System.out.println("Error in DatabaseProtocol::transferFromAccountToAccount");
		}
		closeConnection();
	}
	
	public void setAccountBalance(Account account, double newBalance) {

		startConnection();
		account.setBalance(0);
		account.changeBalance(newBalance);
		try {
			stmt.executeUpdate("UPDATE DTUGRP04.ACCOUNTS SET BALANCE = '" + account.getBalance() + "' WHERE ID = '"
					+ account.getAccountNumber() + "'");

		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
		}
		closeConnection();

	}


	////////////////////
	// Connection //
	////////////////////

	private void closeConnection() {
		try {
			if (!con.isClosed())
				con.close();
			if (!stmt.isClosed())
				stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void startConnection() {
		try {
			con = dataSource.getConnection("DTU02", "FAGP2017");
			stmt = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
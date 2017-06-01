package dtu.robboss.app;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.sql.DataSource;

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
		//TODO: OLD:  USERS -> CUSTOMERS
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

	public void addTransactionToTH(String date, Account from, String toID, Double amount, String message) {
		//TABLECOLUMNS:  DATE, FROM, TO, AMOUNT, MESSAGE
		
		try {
			startConnection();
			stmt.executeUpdate("INSERT INTO DTUGRP04.TRANSACTIONHISTORY (DATE, FROM, TO, AMOUNT, MESSAGE) VALUES('"
					+ date + "', '" + from.getAccountNumber() + "', '" + toID + "', '" + amount + "', '" + message + "')");

		} catch (SQLException e) {
			closeConnection();
			e.printStackTrace();
		}
		closeConnection();
		
	}

	//TODO: OLD
//	public void removeTransactionHistoryTable(Customer cos) {
//		String tableSQL = "DROP TABLE DTUGRP04." + cos.getUsername() + "TH ";
//		startConnection();
//		try {
//			stmt.executeUpdate(tableSQL);
//		} catch (SQLException e) {
//			e.printStackTrace();
//			closeConnection();
//		}
//		closeConnection();
//	}

	//TODO: OLD
//	/**
//	 * @throws SQLException
//	 * 
//	 */
//	public void addTransactionHistoryTable(Customer cos) {
//		// Transaction history = TH
//		String tableSQL = "CREATE TABLE DTUGRP04." + cos.getUsername() + "TH " +
//                "( DATE CHAR(16) NOT NULL, "  +
//                " FROM INTEGER NOT NULL, " + 
//                " TO INTEGER NOT NULL, "  + 
//                " AMOUNT DOUBLE NOT NULL, "  + 
//                " MESSAGE VARCHAR(140))" + 
//                " IN DTUGRP04.DTUGRP04" + 
//                " AUDIT NONE " + 
//                " DATA CAPTURE NONE " + 
//				" CCSID EBCDIC;";	
//		
//		// Starts connection with database and adds table
//		startConnection();
//		try {
//			stmt.executeUpdate(tableSQL);
//		} catch (SQLException e) {
//			closeConnection();
//			e.printStackTrace();
//			System.out.println("ERROR: Could not add TH table with username " + cos.getUsername());
//		}
//		closeConnection();
//	}

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

			//TODO: OLD: USERS -> CUSTOMERS
			stmt.executeUpdate(
					"INSERT INTO DTUGRP04.CUSTOMERS (USERNAME, FULLNAME, PASSWORD) VALUES('" + customer.getUsername()
							+ "', '" + customer.getFullname() + "', '" + customer.getPassword() + "')");

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
			stmt.executeUpdate("INSERT INTO DTUGRP04.ACCOUNTS " + "(USERNAME, TYPE, BALANCE, CREDIT, INTEREST)" + "VALUES ('"
					+ customer.getUsername() + "', '" + (main ? "MAIN" : "NORMAL") + "', 0, 0 , 1.0)");
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
				//TODO: OLD: USERS -> CUSTOMERS
				stmt.executeUpdate("DELETE FROM DTUGRP04.CUSTOMERS WHERE USERNAME = '" + user.getUsername() + "'");
				//TODO: OLD: NO LONGER REMOVES TABLE DYNAMICALLY
//				removeTransactionHistoryTable((Customer) user); 
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
				Account newAccount = new Account(customer, rs.getString("ID"), rs.getInt("BALANCE"),rs.getInt("CREDIT"), rs.getString("TYPE"));
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
			//TODO: OLD: USERS -> CUSTOMER
			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.CUSTOMERS WHERE USERNAME = '" + username + "'");
			if (rs.next()) {
				Customer customer = new Customer(rs.getString("FULLNAME"), rs.getString("USERNAME"),
						rs.getString("PASSWORD"));
				closeConnection();
				return customer;
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
			closeConnection();
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
				Account account = new Account(customer, accountNumber, rs.getInt("BALANCE"), rs.getInt("CREDIT"), rs.getString("TYPE"));
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


	public ArrayList<Account> getAccountsByUser(String username) {
		startConnection();

		ArrayList<Account> accounts = new ArrayList<Account>();
		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.ACCOUNTS WHERE USERNAME = '" + username + "'");
			Customer customer = getCustomer(username);
			while (rs.next()) {
				Account account = new Account(customer, rs.getString("ID"), rs.getInt("BALANCE"), rs.getInt("CREDIT"), rs.getString("TYPE"));
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
	
	public ResultSet getTransactionHistory(Customer customer) {
		startConnection();
		
		System.out.println(customer.getUsername());
		try {
			String query = "SELECT * FROM DTUGRP04.TRANSACTIONHISTORY WHERE ";
			for(Account acc : customer.getAccounts())
				query+= " FROM = " + acc.getAccountNumber() + " OR TO = " + acc.getAccountNumber() + " ";
			
			ResultSet th = stmt.executeQuery(query);
			return th;
					
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		closeConnection();
		return null;
	}

	////////////
	// Update //
	////////////
	
	public void setNewMainAccount(Account oldMain, Account newMain){
		
		oldMain.setType("NORMAL");
		newMain.setType("MAIN");
		
		startConnection();
		
		try{
			stmt.executeUpdate("UPDATE DTUGRP04.ACCOUNTS SET TYPE = '" + oldMain.getType() + "' WHERE ID = '"
					+ oldMain.getAccountNumber() + "'");
			stmt.executeUpdate("UPDATE DTUGRP04.ACCOUNTS SET TYPE = '" + newMain.getType() + "' WHERE ID = '"
					+ newMain.getAccountNumber() + "'");
		} catch(SQLException e) {
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
			con = dataSource.getConnection();
			stmt = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	


}
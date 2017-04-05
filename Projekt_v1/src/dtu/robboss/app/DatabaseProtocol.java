package dtu.robboss.app;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.sql.DataSource;

public class DatabaseProtocol {
	@Resource(name = "jdbc/DB2")
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
		ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS USERCOUNT FROM DTUGRP04.USERS");

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

	public boolean containsUser(User user) {
		User userCheck = getUser(user.getUsername());

		return !(userCheck == null);
	}

	public boolean containsAccount(Account account) {
		// TODO: Need account table/tables.

		return false;
	}

	////////////////////
	// ADD AND REMOVE //
	////////////////////
	
	public void addAdmin(Admin admin) throws AlreadyExistsException {
		
		if (containsUser(admin))
			throw new AlreadyExistsException("User");
		
		try {
			startConnection();
			
			stmt.executeUpdate("INSERT INTO DTUGRP04.ADMINS (USERNAME, FULLNAME, PASSWORD) VALUES('"+
			admin.getUsername() + 	"', '" +
			admin.getFullname() +	"', '" +
			admin.getPassword() + 	"')" );
			
			closeConnection();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	public void addCustomer(Customer customer) throws AlreadyExistsException {
		//USERS columns: USERNAME, FULLNAME, PASSWORD, MAINACCOUNT
		
		if (containsUser(customer))
			throw new AlreadyExistsException("User");
		
		try {
			startConnection();
			
			stmt.executeUpdate("INSERT INTO DTUGRP04.USERS (USERNAME, FULLNAME, PASSWORD) VALUES('"+
			customer.getUsername() + 	"', '" +
			customer.getFullname() +	"', '" +
			customer.getPassword() + 	"')" );
			
			closeConnection();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addAccount(User user, boolean main) {
		//ACCOUNTS columns: ID, USER, TYPE, BALANCE, CREDIT (, HISTORY)
		
		startConnection();
		try {
			stmt.executeUpdate("INSERT INTO DTUGRP04.ACCOUNTS " + 
					"(USERNAME, TYPE, BALANCE, CREDIT)" + 
					"VALUES ('" + user.getUsername() +"', '"+ (main? "MAIN" : "NORMAL") +"', 0, 0)"
					);
		} catch (SQLException e) {
			System.out.println("Could not create account");
//			e.printStackTrace();
		}
		closeConnection();
		
	}
	

	public void removeUser(User user) {
		
		startConnection();
		try {
			stmt.executeUpdate("DELETE FROM DTUGRP04.ACCOUNTS WHERE USERNAME = '" + user.getUsername() + "'");
			stmt.executeUpdate("DELETE FROM DTUGRP04.USERS WHERE USERNAME = '" + user.getUsername() + "'");
			stmt.executeUpdate("DELETE FROM DTUGRP04.ADMINS WHERE USERNAME = '" + user.getUsername() + "'");
		} catch (SQLException e) {
			System.out.println("Could not remove user.");
//			 e.printStackTrace();
		}
		closeConnection();

	}

	public void removeAccount(Account account) {
		
		startConnection();
		try {
			stmt.executeUpdate("DELETE FROM DTUGRP04.ACCOUNTS WHERE ID = '" + account.getAccountNumber() + "'");
		} catch (SQLException e) {
			System.out.println("Could not remove account.");
//			 e.printStackTrace();
		}
		
		closeConnection();
		

	}
	
	//TODO: CHECK NAME
	public void addAccountsToUser(Customer customer) {
		startConnection();
		try{
			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.ACCOUNTS WHERE USERNAME = '"+customer.getUsername()+"'");
			
			while(rs.next()){
				Account newAccount = new Account(customer, rs.getString("ID"), rs.getInt("BALANCE"), rs.getInt("CREDIT"));
				if(rs.getString("TYPE").trim().equals("MAIN")){
					customer.setMainAccount(newAccount);
				}
			}
		} catch(SQLException e){
			
		}
		
		
	}

	////////////////////
	//      Get       //
	////////////////////

	
	/**
	 * Fetches customer from database
	 * @param username
	 * @return User Object
	 * @throws SQLException
	 */
	public Customer getCustomer(String username) {
		startConnection();
		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.USERS WHERE USERNAME = '" + username + "'");
			if (rs.next()) {
				Customer customer = new Customer(rs.getString("FULLNAME"), rs.getString("USERNAME"), rs.getString("PASSWORD"));
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
	 * Finds user in the database with the given username. Searches through both USERS and ADMINS tables.
	 * @param username - Username of user we want to find
	 */
	public User getUser(String username) {
	
		// Checks if a customer exists with the given username
		Customer c = getCustomer(username);
		if(! (c == null)){
			return c;
		}

		// If customer does not exist, returns the admin with the given username. 
		// If no admin with given username exists, returns null
		return getAdmin(username);
		
	}
	
	public Account getAccount(String accountNumber) {
		startConnection();
		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.ACCOUNTS WHERE ID = '" + accountNumber + "'");
			if (rs.next()) {
				Customer customer = getCustomer(rs.getString("USER"));
				Account account = new Account(customer, accountNumber, rs.getInt("BALANCE"), rs.getInt("CREDIT"));
				closeConnection();
				return account;
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
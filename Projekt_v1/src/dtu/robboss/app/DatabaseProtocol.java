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

		if (userCheck == null)
			return false;
		else
			return true;
	}

	public boolean containsAccount(Account account) {
		// TODO: Need account table/tables.

		return false;
	}

	////////////////////
	// ADD AND REMOVE //
	////////////////////

	public void addUser(User user) throws AlreadyExistsException {
		//USERS columns: USERNAME, FULLNAME, PASSWORD, MAINACCOUNT
		
		if (containsUser(user))
			throw new AlreadyExistsException("User");
		
		try {
			startConnection();
			
			stmt.executeUpdate("INSERT INTO DTUGRP04.USERS (USERNAME, FULLNAME, PASSWORD) VALUES('"+
			user.getUsername() + 	"', '" +
			user.getFullname() +	"', '" +
			user.getPassword() + 	"')" );
			
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

	////////////////////
	//      Get       //
	////////////////////

	
	/**
	 * Fetches user from database
	 * @param username
	 * @return User Object
	 * @throws SQLException
	 */
	public User getUser(String username) {
		startConnection();
		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.USERS WHERE USERNAME = '" + username + "'");
			if (rs.next()) {
				User user = new User(rs.getString("FULLNAME"), rs.getString("USERNAME"), rs.getString("PASSWORD"));
				closeConnection();
				return user;
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
	
	
	
	public Account getAccount(String accountNumber) {
		startConnection();
		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.ACCOUNTS WHERE ID = '" + accountNumber + "'");
			if (rs.next()) {
				User user = getUser(rs.getString("USER"));
				Account account = new Account(user, accountNumber, rs.getInt("BALANCE"), rs.getInt("CREDIT"));
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

	//TODO: CHECK NAME
	public void addAccountsToUser(User user) {
		startConnection();
		try{
			ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.ACCOUNTS WHERE USERNAME = '"+user.getUsername()+"'");
			
			while(rs.next()){
				Account newAccount = new Account(user, rs.getString("ID"), rs.getInt("BALANCE"), rs.getInt("CREDIT"));
				if(rs.getString("TYPE").trim().equals("MAIN")){
					user.setMainAccount(newAccount);
				}
			}
		} catch(SQLException e){
			
		}
		
		
	}

}
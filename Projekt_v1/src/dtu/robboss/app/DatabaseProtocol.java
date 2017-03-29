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


	// TODO Implement this for bragging.
	// public Object accountCount() {
	// return accountCount;
	// }

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

	public boolean addUser(User user) {

		try {

			stmt.executeUpdate("INSERT INTO DTUGRP04.USERS VALUES(1, '" + user.getUsername() + "', '<Full Name>', '"
					+ user.getPassword() + "', 0, 1)");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void addAccount(Account account) {
		// TODO: Need account table/tables.

		// accounts.add(account);
	}

	public void removeUser(User user) {

		startConnection();
		try {
			stmt.executeUpdate("DELETE FROM DTUGRP04.USERS WHERE USERNAME = '" + user.getUsername() + "'");
		} catch (SQLException e) {
//			System.out.println("Could not remove user.");
			 e.printStackTrace();
		}
		closeConnection();

	}

	public void removeAccount(Account account) {

		// TODO: Need account table/tables and remove an account from table.

		// accounts.remove(account);

	}

	////////////////////
	// Get //
	////////////////////

	
	/**
	 * Fetches user from database
	 * @param username
	 * @return
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

	public Account getAccount(String accountNumber) {
		// TODO: Need account table/tables.

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
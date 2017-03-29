package dtu.robboss.app;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

public class DatabaseProtocol {
	@Resource(name = "jdbc/DB2")
	private DataSource dataSource;
	private Connection con = null;
	private Statement stmt = null;

	int accountCount = 0;

	 List<User> users = new ArrayList<>();
	 List<Account> accounts = new ArrayList<>();

	////////////
	// AMOUNT //
	////////////

	public DatabaseProtocol(DataSource ds1) {
		this.dataSource = ds1;
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

	/**
	 * Fetches user from database
	 * @param username
	 * @return
	 * @throws SQLException
	 */
	public User getUser(String username) throws SQLException {
		Connection con = dataSource.getConnection();
		Statement stmt = con.createStatement();
		// TODO more sanization?
		ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.USERS WHERE USERNAME = '"
				+ username + "'");
		if (rs.next()){
			//Trimming removes trailing and leading whitespace from database string.
			return new User(rs.getString("FULLNAME").trim(),
					 		rs.getString("USERNAME").trim(),
					 		rs.getString("PASSWORD").trim());
		}
		else 
			return null;				
	}

	// TODO Implement this for bragging.
	// public Object accountCount() {
	// return accountCount;
	// }

	///////////////
	// SEARCHING //
	///////////////

	public boolean containsUser(User user) {
		return users.contains(user);
	}

	public boolean containsAccount(Account account) {
		return accounts.contains(account);
	}

	////////////////////
	// ADD AND REMOVE //
	////////////////////

	public void addUser(User user) {

		try {

			stmt.executeUpdate("INSERT INTO DTUGRP04.USERS VALUES(1, '" + user.getUsername() + "', '<Full Name>', '"
					+ user.getPassword() + "', 0, 1)");

		} catch (SQLException e) {
			e.printStackTrace();
		}

		users.add(user);
	}

	public void addAccount(Account account) {
		accountCount++;
		accounts.add(account);
	}

	public void removeUser(User user) {
		users.remove(user);

	}

	public void removeAccount(Account account) {
		accountCount--;
		accounts.remove(account);

	}

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

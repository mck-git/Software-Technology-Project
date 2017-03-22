package dtu.robbos.servlet;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;

/**
 * Servlet implementation class DataBase
 */
@WebServlet("/DataBase")
public class DataBase extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public static int getUserCount(DataSource ds1) {
		try {
			Connection con = ds1.getConnection();
			Statement stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS USERCOUNT FROM DTUGRP04.USERS");

			if (rs.next()) {

				String count = rs.getString("USERCOUNT");
				return Integer.parseInt(count);

			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

}

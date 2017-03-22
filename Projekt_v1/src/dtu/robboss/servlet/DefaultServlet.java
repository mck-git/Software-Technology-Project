package dtu.robboss.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import dtu.robboss.app.*;

/**
 * Servlet implementation class DefaultServlet
 */
@WebServlet(description = "default servlet", urlPatterns={"/DS"})
public class DefaultServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name = "jdbc/DB2")
	private DataSource ds1;
	private BankApplication app;

	public void init(){
		app = new BankApplication(ds1);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().println("from doget");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		String subject = request.getParameter("subject");

		if (subject.equals("UserCount")) {
			out.println("Amount of users: " + app.userCount());
		}

		if (subject.equals("CreateNewUser")) {
			try {
				Connection con = ds1.getConnection();
				Statement stmt = con.createStatement();

				
				stmt.executeUpdate("INSERT INTO DTUGRP04.USERS VALUES(1, 'Magnus', 'Roar Nind Steffensen', '134', 0, 1)");

				subject = "Login";

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (subject.equals("Login")) {

			out.println("<br>Trying to access server: ");
			try {
				Connection con = ds1.getConnection();
				out.println("<br>Created connection");

				Statement stmt = con.createStatement();
				out.println("<br>Created statement");

				ResultSet rs = stmt.executeQuery("SELECT * FROM DTUGRP04.USERS");
				out.print("<br>Data Fetched");
				out.print("<br> Printing data: ");

				while (rs.next()) {
					out.println("<br> Username: " + rs.getString("USERNAME") + " Password: " + rs.getString("PASSWORD"));
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}

package dtu.robboss.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import dtu.robboss.app.*;

/**
 * Servlet implementation class DefaultServlet
 */
@WebServlet(description = "default servlet", urlPatterns = { "/DS" })
public class DefaultServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name = "jdbc/DB2")
	private DataSource dataSource;
	private BankApplication app;

	public void init() {
		app = new BankApplication(dataSource);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.getWriter().println("from doget");

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		String subject = request.getParameter("subject");

		if (subject.equals("UserCount")) {
			out.println("Amount of users: " + app.userCount());
		}

		// TODO CANNOT CREATE PASSWORD AND USERNAME WITH SPACES
		if (subject.equals("CreateNewUser")) {
			try {
				Connection con = dataSource.getConnection();
				Statement stmt = con.createStatement();

				stmt.executeUpdate(
						"INSERT INTO DTUGRP04.USERS VALUES(1, 'Magnus', 'Roar Nind Steffensen', '134', 0, 1)");

				subject = "Login";

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (subject.equals("Login")) {
			try {
				// Get request username and password
				String username = request.getParameter("username");
				String password = request.getParameter("password");

				
				User user = app.database.getUser(username);
				System.out.println(user.toString());
				if (user != null && password.trim().equals(user.getPassword())) {

					HttpSession session = request.getSession();

					session.setAttribute("USER", user);

					RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");

					rd.forward(request, response);
				} else
					out.println("Incorrect username or password.");

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}

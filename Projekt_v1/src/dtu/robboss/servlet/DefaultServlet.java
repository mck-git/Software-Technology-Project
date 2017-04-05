package dtu.robboss.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import dtu.robboss.app.Admin;
import dtu.robboss.app.AdminNotLoggedInException;
import dtu.robboss.app.AlreadyExistsException;
import dtu.robboss.app.BankApplication;
import dtu.robboss.app.Customer;
import dtu.robboss.app.UnknownLoginException;
import dtu.robboss.app.User;

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

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		String subject = request.getParameter("subject");

		if (subject.equals("UserCount")) {
			out.println("Amount of users: " + app.userCount());
		}

		if(subject.equals("CreateNewUser")){
			String fullname = request.getParameter("fullname");
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			String cpr = request.getParameter("cpr");
			
			System.out.println("Full name: " + fullname + ", username: " + username + ", password: " + password + 
					", cpr: " + cpr);
			
			try {
				app.createCustomer(fullname, username, password, cpr);
				subject = "Login";
			} catch (AdminNotLoggedInException e) {
				e.printStackTrace();
			} catch (AlreadyExistsException e) {
				System.out.println("User already exist");
				response.sendRedirect("login.html");
//				e.printStackTrace();
			}
		}

		if (subject.equals("Login")) {

			// Get request username and password
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			
			
			try {
				HttpSession session = request.getSession();

				
				User userLoggedIn = app.login(username, password);

				// Checks if user logged in is a customer
				if(userLoggedIn instanceof Customer) {
					app.refreshAccountsForCustomer((Customer) userLoggedIn);
					session.setAttribute("USER", (Customer) userLoggedIn);
					RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
					rd.forward(request, response);
				}
				
				if(userLoggedIn instanceof Admin) {
					session.setAttribute("USER", (Admin) userLoggedIn);
					RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
					rd.forward(request, response);
				}
				
			} catch (UnknownLoginException e) {
				System.out.println("Failed to login in defaultservlet ");
				response.sendRedirect("login.html");
				// e.printStackTrace();
			}
		}
		


		if (subject.equals("DeleteUser")) {
			User userToDelete = (User) request.getSession().getAttribute("USER");
			try {
				System.out.println("Removing " + userToDelete.getUsername() + ".");
				app.deleteUser(userToDelete);
				request.getSession().removeAttribute("USER");
				RequestDispatcher rd = request.getRequestDispatcher("login.html");
				rd.forward(request, response);

			} catch (AdminNotLoggedInException e) {
				// e.printStackTrace();
				System.out.println("Could not remove user.");
			}
		}

		if (subject.equals("LogOutUser")) {

			request.getSession().removeAttribute("USER");

			RequestDispatcher rd = request.getRequestDispatcher("login.html");
			rd.forward(request, response);

		}
		
		if(subject.equals("NewAccount")){
			Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");
			try {
				app.createAccount(loggedInCustomer, false);
				app.refreshAccountsForCustomer(loggedInCustomer);
				RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
				rd.forward(request, response);
			} catch (AdminNotLoggedInException e) {
				System.out.println("Could not create new account from defaultservlet");
//				e.printStackTrace();
			}
		}
	}

}
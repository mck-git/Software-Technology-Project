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
import dtu.robboss.app.Account;
import dtu.robboss.app.AccountNotfoundException;
import dtu.robboss.app.AdminNotLoggedInException;
import dtu.robboss.app.AlreadyExistsException;
import dtu.robboss.app.BankApplication;
import dtu.robboss.app.Customer;
import dtu.robboss.app.UnknownLoginException;
import dtu.robboss.app.User;
import dtu.robboss.app.TransferException;
import dtu.robboss.app.UserNotLoggedInException;
import dtu.robboss.app.UserNotfoundException;

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

		if (subject.equals("CreateNewUser")) {
			String fullname = request.getParameter("fullname");
			String username = request.getParameter("username");

			try {
				// checks if username is all lower case TODO make this viewable for the user
				for (int i = 0; i < username.length(); i++) {
					if (("" + username.charAt(i)).matches("[^a-z]"))
						throw new InvalidUsernameException();
				}
				
				// Sets password and cpr
				String password = request.getParameter("password");
				String cpr = request.getParameter("cpr");
				
				//Creates customer object and sets subject to login
				app.createCustomer(fullname, username, password, cpr);
				subject = "Login";

			} catch (InvalidUsernameException e) {
				System.out.println(e.getMessage());
			} catch (AlreadyExistsException e) {
				System.out.println("User already exist");
				response.sendRedirect("login.html");
				// e.printStackTrace();
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
				if (userLoggedIn instanceof Customer) {
					Customer customerLoggedIn = (Customer) userLoggedIn;
					app.refreshAccountsForCustomer(customerLoggedIn);
					session.setAttribute("USER", customerLoggedIn);
					RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
					rd.forward(request, response);
				}

				// Checks if user logged in is an admin
				if (userLoggedIn instanceof Admin) {
					Admin adminLoggedIn = (Admin) userLoggedIn;
					session.setAttribute("USER", adminLoggedIn);
					RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
					rd.forward(request, response);
				}

			} catch (UnknownLoginException e) {
				System.out.println("Failed to login in defaultservlet ");
				response.sendRedirect("login.html");
				// e.printStackTrace();
			}
		}
		if (subject.equals("transfermoney")) {

			String beforedecimalseperator = "0" + request.getParameter("beforedecimalseperator");
			String afterdecimalseperator = request.getParameter("afterdecimalseperator") + "00";
			String transferAmount = beforedecimalseperator + "." + afterdecimalseperator.substring(0, 2);

			HttpSession session = request.getSession();
			String recieverType = request.getParameter("receiverType");
			String message = request.getParameter("message");
			Account sourceAccount = ((Customer) session.getAttribute("USER")).getMainAccount();

			try {
				if (recieverType.equals("account")) {
					app.transferFromAccountToAccount(sourceAccount, request.getParameter("receiver"), transferAmount,
							message);
				} else if (recieverType.equals("user")) {
					app.transferFromAccountToCustomer(sourceAccount, request.getParameter("receiver"), transferAmount,
							message);
				}

				RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
				rd.forward(request, response);
			} catch (UserNotLoggedInException | TransferException | AccountNotfoundException
					| UserNotfoundException e) {
				System.out.println("Error in DefaultServlet::doPost -> transfermoney");
				e.printStackTrace();
			}

		}

		if (subject.equals("paybill")) {

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

		if (subject.equals("NewAccount")) {
			Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");
			app.createAccount(loggedInCustomer, false);
			app.refreshAccountsForCustomer(loggedInCustomer);
			RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
			rd.forward(request, response);
		}
	}

}
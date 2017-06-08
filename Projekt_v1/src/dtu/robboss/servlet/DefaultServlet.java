package dtu.robboss.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import dtu.robboss.app.Account;
import dtu.robboss.app.Admin;
import dtu.robboss.app.BankApplication;
import dtu.robboss.app.Customer;
import dtu.robboss.app.TransactionHistoryElement;
import dtu.robboss.app.User;
import dtu.robboss.app.Valuta;
import dtu.robboss.exceptions.AccountNotfoundException;
import dtu.robboss.exceptions.AlreadyExistsException;
import dtu.robboss.exceptions.InvalidUsernameException;
import dtu.robboss.exceptions.TransferException;
import dtu.robboss.exceptions.UnknownLoginException;
import dtu.robboss.exceptions.UserNotLoggedInException;
import dtu.robboss.exceptions.UserNotfoundException;

/**
 * Servlet implementation class DefaultServlet
 */
@WebServlet(description = "default servlet", urlPatterns = { "/DS" })
public class DefaultServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name = "jdbc/exampleDS")
	// local: jdbc/DB2
	// IBM: jdbc/exampleDS
	private DataSource dataSource;
	private BankApplication app;

	@Override
	public void init() {
		app = new BankApplication(dataSource);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		String subject = request.getParameter("subject");

		/////////////////
		// NEWUSER.JSP //
		/////////////////

		if (subject.equals("CreateNewUser")) {
			/*
			 * Creates a new customer with given credentials in the database
			 */
			String fullname = request.getParameter("fullname");
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			Valuta currency = Valuta.currencyStringToEnum(request.getParameter("currency"));

			try {
				// checks if username is all lower case TODO make this viewable
				for (int i = 0; i < username.length(); i++) {
					if (("" + username.charAt(i)).matches("[^a-z]"))
						throw new InvalidUsernameException();
				}

				// Creates customer in database and sets subject to login
				app.startDatabaseConnection();
				app.createCustomer(fullname, username, password, currency);
				app.closeDatabaseConnection();
				subject = "Login";

			} catch (InvalidUsernameException e) {
				System.out.println(e.getMessage());
				app.closeDatabaseConnection();
				response.sendRedirect("login.html");
			} catch (AlreadyExistsException e) {
				System.out.println(e.getMessage());
				app.closeDatabaseConnection();
				response.sendRedirect("login.html");
			}
		}

		//////////////////
		// USERPAGE.JSP //
		//////////////////

		if (subject.equals("Select currency")) {
			/*
			 * Sets the currently logged in user's prefferedCurrency to the
			 * selected one.
			 */
			Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");
			Valuta currency = Valuta.currencyStringToEnum(request.getParameter("currency"));

			app.startDatabaseConnection();

			app.setCurrency(loggedInCustomer, currency);

			app.closeDatabaseConnection();

			RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
			rd.forward(request, response);
		}

		if (subject.equals("transfermoney")) {
			/*
			 * transfers money based on input
			 */
			HttpSession session = request.getSession();
			Customer loggedInCustomer = (Customer) session.getAttribute("USER");

			// Gets required input for transfer
			String recieverType = request.getParameter("receiverType");
			String message = request.getParameter("message");
			String accountIDFrom = request.getParameter("accountToSendFrom");

			app.startDatabaseConnection();

			Account sourceAccount = app.getAccountByID(accountIDFrom);

			// Gets the amount to be transferred.
			// Leading and trailing zero's enables empty input in either field
			// to be legal.
			// Also provides useful double casting sanitation.
			String beforedecimalseperator = "0" + request.getParameter("beforedecimalseperator");
			String afterdecimalseperator = request.getParameter("afterdecimalseperator") + "00";
			String transferAmount = beforedecimalseperator + "." + afterdecimalseperator.substring(0, 2);

			// Tries to transfer money
			try {
				if (loggedInCustomer == null)
					throw new UserNotfoundException();

				// Revert transferAmount to DKK which is the currency the
				// database operates on
				double amount = Valuta.revert(Double.parseDouble(transferAmount), loggedInCustomer);

				// The customer wants to transfer to a specific account
				if (recieverType.equals("account")) {
					Account targetAccount = app.getAccountByID(request.getParameter("receiver"));
					app.transferFromAccountToAccount(sourceAccount, targetAccount, amount, message);
				}
				// The customer wants to transfer to a user, defaulting to the
				// users main account
				else if (recieverType.equals("user")) {
					app.transferFromAccountToCustomer(sourceAccount, request.getParameter("receiver"), amount, message);
				}

				// Updates the transaction history
				List<TransactionHistoryElement> th = app.getTransactionHistory((Customer) session.getAttribute("USER"));
				session.setAttribute("TRANSACTIONHISTORY", th);

			} catch (UserNotLoggedInException | TransferException | AccountNotfoundException | UserNotfoundException
					| NumberFormatException e) {
				System.out.println("Error in DefaultServlet::doPost -> transfermoney\nError message: " + e.getMessage());
			}

			// After the transfer is done, the local account information is
			// updated
			app.refreshAccountsForCustomer((Customer) session.getAttribute("USER"));

			app.closeDatabaseConnection();

			RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
			rd.forward(request, response);
		}

		if (subject.equals("LogOutUser")) {
			/*
			 * Logs user out, removing information from the session scope and
			 * returning to the login page.
			 */
			request.getSession().removeAttribute("USER");
			request.getSession().removeAttribute("CUSTOMERFOUND");
			app.logOut();
			RequestDispatcher rd = request.getRequestDispatcher("login.html");
			rd.forward(request, response);
		}

		if (subject.equals("NewAccount")) {
			/*
			 * Creates a new account for the logged in user. Uses
			 * autoincremented ID in the database.
			 */

			app.startDatabaseConnection();

			Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");
			app.createAccount(loggedInCustomer, false);
			app.refreshAccountsForCustomer(loggedInCustomer);

			app.closeDatabaseConnection();

			RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
			rd.forward(request, response);
		}

		if (subject.equals("Set as main account")) {
			/*
			 * Sets the selected account as the currently logged in users main
			 * account.
			 */
			app.startDatabaseConnection();

			Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");

			// Gets account to be set as main
			String accountID = request.getParameter("accountSelected");
			Account newMain = loggedInCustomer.getAccountByID(accountID);

			app.setNewMainAccount(loggedInCustomer, newMain);

			app.closeDatabaseConnection();

			RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
			rd.forward(request, response);
		}

		if (subject.equals("Delete account")) {
			/*
			 * Deletes the selected account from the database.
			 */
			Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");

			app.startDatabaseConnection();

			// Gets account to be deleted
			String accountID = request.getParameter("accountSelected");
			Account delete = loggedInCustomer.getAccountByID(accountID);

			app.removeAccount(delete);

			app.closeDatabaseConnection();

			RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
			rd.forward(request, response);
		}

		////////////////
		// LOGIN.HTML //
		////////////////

		if (subject.equals("Login")) {
			/*
			 * Logs in as user with given credentials assuming one exists. After
			 * this is done, relevant session attributes are set.
			 */
			// Get request username and password
			String username = request.getParameter("username");
			String password = request.getParameter("password");

			try {
				HttpSession session = request.getSession();

				app.startDatabaseConnection();

				// Gets the user object logged in
				User userLoggedIn = app.login(username, password);

				// Checks if user logged in is a customer
				if (userLoggedIn instanceof Customer) {
					// Casts from user to customer and refreshes customers
					// account information.
					Customer customerLoggedIn = (Customer) userLoggedIn;
					app.refreshAccountsForCustomer(customerLoggedIn);

					// Sets customer as the session attribute
					session.setAttribute("USER", customerLoggedIn);

					// Get transaction history for customer
					List<TransactionHistoryElement> th = app.getTransactionHistory(customerLoggedIn);
					session.setAttribute("TRANSACTIONHISTORY", th);

					app.closeDatabaseConnection();

					RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
					rd.forward(request, response);
				}

				// Checks if user logged in is an admin
				else if (userLoggedIn instanceof Admin) {
					// Cast sfrom user to admin and sets admin as the session
					// attribute
					Admin adminLoggedIn = (Admin) userLoggedIn;
					session.setAttribute("USER", adminLoggedIn);

					// Creates a list to store future admin search results
					session.setAttribute("ACCOUNTSFOUND", new ArrayList<Account>());

					app.closeDatabaseConnection();

					RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
					rd.forward(request, response);
				}

			} catch (UnknownLoginException | UserNotfoundException e) {
				System.out.println("DefaultServlet::doPost -> Login\nError message: " + e.getMessage());

				app.closeDatabaseConnection();

				response.sendRedirect("login.html");
			}
		}

		if (subject.equals("UserCount")) {
			/*
			 * Prints the number of users currently in the database.
			 */

			app.startDatabaseConnection();
			out.println("Number of users: " + app.customerCount());
			app.closeDatabaseConnection();
		}

		///////////////////
		// ADMINPAGE.JSP //
		///////////////////

		if (subject.equals("Search")) {
			/*
			 * 
			 */
			HttpSession session = request.getSession();

			String searchBy = request.getParameter("searchBy");
			String searchToken = request.getParameter("searchToken");

			app.startDatabaseConnection();

			try {
				if (searchBy.equals("account")) {
					// Searching for a specific account.
					// This utilizes the fact that getAccount() creates a
					// customer object with only that one account in it.
					Account accountFound = app.getAccountByID("searchToken");
						if(accountFound != null){
						Customer customerFound = accountFound.getCustomer();
	
						// Sets the attribute in session scope as the search result
						session.setAttribute("CUSTOMERFOUND", customerFound);
					} else {
						session.removeAttribute("CUSTOMERFOUND");
					}

				} else if (searchBy.equals("user")) {
					// Searching for a specific user
					// This finds all information about the user, including all
					// his/hers accounts
					User userFound = app.getUserByUsername(searchToken);
					if(userFound instanceof Customer && userFound != null){
						Customer customerFound = (Customer) userFound;

						// Sets the attribute in session scope as the search result
						session.setAttribute("CUSTOMERFOUND", customerFound);
					} else {
						session.removeAttribute("CUSTOMERFOUND");
					}
				}
			} catch (Exception e) {
				System.out.println("DefaultServlet::doPost -> Search \nError message: " + e.getMessage());
				e.printStackTrace();
			}

			app.closeDatabaseConnection();

			RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
			rd.forward(request, response);
		}

		if (subject.equals("CreateNewUserAdmin")) {
			/*
			 * The admin can create new customer as well as admin users. This
			 * functions in mainly the same way as subject="CreateNewUser",
			 * except this also has a usertype which can either be "customer" or
			 * "admin"
			 */
			
			
			
			if (request.getParameter("userType").equals("customer")) {
				// Creating a customer

				String fullname = request.getParameter("fullname");
				String username = request.getParameter("username");
				String password = request.getParameter("password");
				String currencyString = request.getParameter("currency");
				
				
				
				Valuta currency;
				if(currencyString != null)
					currency = Valuta.currencyStringToEnum(currencyString);
				else 
					currency = Valuta.DKK;
				
				try {
					
					// checks if username is all lower case TODO make this viewable
					for (int i = 0; i < username.length(); i++) {
						if (("" + username.charAt(i)).matches("[^a-z]"))
							throw new InvalidUsernameException();
					}
					
					app.startDatabaseConnection();
					app.createCustomer(fullname, username, password, currency);

				} catch (AlreadyExistsException | InvalidUsernameException e) {

					System.out.println("DefaultServlet::doPost -> CreateNewCustomer\nError message: " + e.getMessage());
				}
				
			} else if (request.getParameter("userType").equals("admin")) {
				// Creating an admin

				String fullname = request.getParameter("fullname");
				String username = request.getParameter("username");
				String password = request.getParameter("password");

				try {
					
					// checks if username is all lower case TODO make this viewable
					for (int i = 0; i < username.length(); i++) {
						if (("" + username.charAt(i)).matches("[^a-z]"))
							throw new InvalidUsernameException();
					}
					
					app.startDatabaseConnection();
					app.createAdmin(fullname, username, password);

				} catch (AlreadyExistsException | InvalidUsernameException e) {
					System.out.println("DefaultServlet::doPost -> CreateNewAdmin\nError message: " + e.getMessage());
				}
			}
			
			app.closeDatabaseConnection();
			RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
			rd.forward(request, response);
		}

		if (subject.equals("DeleteUserByAdmin")) {
			/*
			 * Deletes user matching the input username.
			 */
			try {
				app.startDatabaseConnection();

				User userToDelete = app.getUserByUsername(request.getParameter("username"));

				System.out.println("Removing " + userToDelete.getUsername() + ".");

				app.removeUser(userToDelete);


			} catch (NullPointerException | UserNotfoundException e) {
				System.out.println("DefaultServlet::doPost -> DeleteUserAdmin\nErorr message: Could not remove user.");
			}
			
			app.closeDatabaseConnection();
			// TODO: if admin deletes itself, redirect to login page instead
			RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
			rd.forward(request, response);
		}

		if (subject.equals("Perform Batch")) {
			/*
			 * 
			 */
			try {
				app.startDatabaseConnection();
				app.applyInterestToAllAccounts();
				app.storeOldTransactionsInArchive();

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			app.closeDatabaseConnection();
			RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
			rd.forward(request, response);
		}

		if (subject.equals("Apply Interest")) {

			try {

				app.startDatabaseConnection();
				app.applyInterestToAllAccounts();

				if (request.getSession().getAttribute("CUSTOMERFOUND") != null )
					app.refreshAccountsForCustomer((Customer) request.getSession().getAttribute("CUSTOMERFOUND"));

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			app.closeDatabaseConnection();
			RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
			rd.forward(request, response);
		}
		
		if (subject.equals("Archive Old Transactions")) {

			try {

				app.startDatabaseConnection();
				app.storeOldTransactionsInArchive();


			} catch (Exception e) {
				e.printStackTrace();
			}
			
			app.closeDatabaseConnection();
			RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
			rd.forward(request, response);
		}
		
		if (subject.equals("Set Interest")) {

			try {
				double interest = Double.parseDouble(request.getParameter("interest"));
				String accountID = request.getParameter("accountSelected");

				app.startDatabaseConnection();

				// Sets interest in database
				app.setInterest(accountID, interest);
				app.refreshAccountsForCustomer((Customer) request.getSession().getAttribute("CUSTOMERFOUND"));


			} catch (Exception e) {
				e.printStackTrace();
			}

			app.closeDatabaseConnection();
			RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
			rd.forward(request, response);
		}

		if (subject.equals("Set Credit")) {

			try {
				double credit = Double.parseDouble(request.getParameter("credit"));
				String accountID = request.getParameter("accountSelected");

				app.startDatabaseConnection();

				// Sets credit in database
				app.setCredit(accountID, credit);
				app.refreshAccountsForCustomer((Customer) request.getSession().getAttribute("CUSTOMERFOUND"));


			} catch (Exception e) {
				e.printStackTrace();
			}

			app.closeDatabaseConnection();
			RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
			rd.forward(request, response);
		}

		if (subject.equals("DeleteLoggedInUser")) {
			User userToDelete = (User) request.getSession().getAttribute("USER");
			try {

				app.startDatabaseConnection();

				System.out.println("Removing " + userToDelete.getUsername() + ".");
				app.removeUser(userToDelete);
				request.getSession().removeAttribute("USER");

			} catch (NullPointerException e) {
				// e.printStackTrace();
				System.out.println("DefaultServlet::doPost -> DeleteUser. \nError message: Could not remove user.");
			}
			
			app.closeDatabaseConnection();
			RequestDispatcher rd = request.getRequestDispatcher("login.html");
			rd.forward(request, response);
		}
	}

}
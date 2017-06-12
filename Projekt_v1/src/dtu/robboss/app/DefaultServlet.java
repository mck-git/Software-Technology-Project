package dtu.robboss.app;

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

import dtu.robboss.exceptions.*;

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
	private HttpServletRequest request;
	private HttpServletResponse response;

	@Override
	public void init() {
		app = new BankApplication(dataSource);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.request = request;
		this.response = response;

		response.setContentType("text/html");
		String subject = request.getParameter("subject");

		switch (subject) {
		case "CreateNewCustomer":
			createNewCustomer();
			break;
		case "Select currency":
			selectCurrency();
			break;
		case "transfermoney":
			transferMoney();
			break;
		case "LogOutUser":
			logOut();
			break;
		case "NewAccount":
			newAccount();
			break;
		case "Set as main account":
			selectMainAccount();
			break;
		case "Delete account":
			deleteAccount();
			break;
		case "Login":
			login();
			break;
		case "UserCount":
			userCount();
			break;
		case "Search":
			adminSearch();
			break;
		case "CreateNewUserAdmin":
			newUserByAdmin();
			break;
		case "DeleteUserByAdmin":
			deleteUserByAdmin();
			break;
		case "Perform Batch":
			performBatch();
			break;
		case "Apply Interest":
			applyInterest();
			break;
		case "Archive Old Transactions":
			archiveOldTransactions();
			break;
		case "Set Interest":
			setInterest();
			break;
		case "Set Credit":
			setCredit();
			break;
		case "DeleteLoggedInUser":
			deleteLoggedInUser();
			break;
		default:
			app.logOut();
			response.sendRedirect("login.jsp");
		}

	}

	/**
	 * Deletes the current logged in user locally and from database.
	 */
	private void deleteLoggedInUser() throws ServletException, IOException {
		User userToDelete = (User) request.getSession().getAttribute("USER");
		try {

			app.startDatabaseConnection();

			System.out.println("Removing " + userToDelete.getUsername() + ".");
			app.removeUser(userToDelete, false);
			request.getSession().removeAttribute("USER");

		} catch (NullPointerException | UserException | AccountException | DatabaseException e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
			System.out.println("DefaultServlet::doPost -> DeleteUser. \nError message: Could not remove user.");
		}

		app.closeDatabaseConnection();
		RequestDispatcher rd = request.getRequestDispatcher("login.jsp");
		rd.forward(request, response);
	}

	/**
	 * Sets credit of the account, as specified in response fields
	 */
	private void setCredit() throws ServletException, IOException {
		try {
			double credit = Double.parseDouble(request.getParameter("credit"));
			String accountID = request.getParameter("accountSelected");

			app.startDatabaseConnection();

			// Sets credit in database
			app.setCredit(accountID, credit);
			app.refreshAccountsForCustomer((Customer) request.getSession().getAttribute("CUSTOMERFOUND"));

		} catch (CreditException | DatabaseException e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
			// e.printStackTrace();
		}

		app.closeDatabaseConnection();
		RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
		rd.forward(request, response);
	}

	/**
	 * Sets interest of the account, as specified in response fields
	 */
	private void setInterest() throws ServletException, IOException {
		try {
			double interest = Double.parseDouble(request.getParameter("interest"));
			String accountID = request.getParameter("accountSelected");

			app.startDatabaseConnection();

			// Sets interest in database
			app.setInterest(accountID, interest);
			app.refreshAccountsForCustomer((Customer) request.getSession().getAttribute("CUSTOMERFOUND"));

		} catch (InterestException | DatabaseException e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
			// e.printStackTrace();
		}

		app.closeDatabaseConnection();
		RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
		rd.forward(request, response);
	}

	/**
	 * Moves old transactions to an archive
	 */
	private void archiveOldTransactions() throws ServletException, IOException {
		try {

			app.startDatabaseConnection();
			app.storeOldTransactionsInArchive();

		} catch (Exception e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
		}

		app.closeDatabaseConnection();
		RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
		rd.forward(request, response);
	}

	/**
	 * Applies interest to all accounts
	 */
	private void applyInterest() throws ServletException, IOException {
		try {

			app.startDatabaseConnection();
			app.applyInterestToAllAccounts();

			if (request.getSession().getAttribute("CUSTOMERFOUND") != null)
				app.refreshAccountsForCustomer((Customer) request.getSession().getAttribute("CUSTOMERFOUND"));

		} catch (Exception e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
		}

		app.closeDatabaseConnection();
		RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
		rd.forward(request, response);
	}

	/**
	 * Performs "applyInterest" and "archiveOldTransactions"
	 */
	private void performBatch() throws ServletException, IOException {
		try {
			app.startDatabaseConnection();
			app.applyInterestToAllAccounts();
			app.storeOldTransactionsInArchive();

			if (request.getSession().getAttribute("CUSTOMERFOUND") != null)
				app.refreshAccountsForCustomer((Customer) request.getSession().getAttribute("CUSTOMERFOUND"));

		} catch (Exception e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
		}

		app.closeDatabaseConnection();
		RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
		rd.forward(request, response);
	}

	/**
	 * Deletes user matching the input username.
	 */
	private void deleteUserByAdmin() throws ServletException, IOException {
		User userToDelete = null;

		try {
			app.startDatabaseConnection();

			userToDelete = app.getUserByUsername(request.getParameter("username"));

			System.out.println("Removing " + userToDelete.getUsername() + ".");

			app.removeUser(userToDelete, true);

		} catch (NullPointerException | UserException | AccountException | DatabaseException e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
			System.out.println("DefaultServlet::doPost -> DeleteUserAdmin\nErorr message: Could not remove user.");
		}

		app.closeDatabaseConnection();
		// TODO: if admin deletes itself, redirect to login page instead

		User userLoggedIn = (User) request.getSession().getAttribute("USER");

		if (userLoggedIn.equals(userToDelete)) {
			RequestDispatcher rd = request.getRequestDispatcher("login.jsp");
			rd.forward(request, response);
		} else {
			RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
			rd.forward(request, response);
		}
	}

	/**
	 * The admin can create new customer as well as admin users. This functions
	 * in mainly the same way as subject="CreateNewUser", except this also has a
	 * usertype which can either be "customer" or "admin"
	 */
	private void newUserByAdmin() throws ServletException, IOException {

		String fullname = request.getParameter("fullname");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String passwordhash = "" + password.hashCode();
		
		try {
			if (request.getParameter("userType").equals("customer")) {

				String currencyString = request.getParameter("currency");
				Currency currency;
				
				if (currencyString != null)
					currency = Currency.currencyStringToEnum(currencyString);
				else
					currency = Currency.DKK;


				for (int i = 0; i < username.length(); i++) {
					if (("" + username.charAt(i)).matches("[^a-z]"))
						throw new UserException("invalid username, password or empty full name");
				}

				app.startDatabaseConnection();
				app.createCustomer(fullname, username, passwordhash, currency);

			} else if (request.getParameter("userType").equals("admin")) {

				for (int i = 0; i < username.length(); i++) {
					if (("" + username.charAt(i)).matches("[^a-z]"))
						throw new UserException("invalid username, password or empty full name");
				}

				app.startDatabaseConnection();
				app.createAdmin(fullname, username, passwordhash);

			}

		} catch (AlreadyExistsException | UserException | CurrencyException | DatabaseException e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
		}

		app.closeDatabaseConnection();
		RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
		rd.forward(request, response);
	}

	/**
	 * Searches for accounts or users depending on fields in response object
	 */
	private void adminSearch() throws ServletException, IOException {
		HttpSession session = request.getSession();

		String searchBy = request.getParameter("searchBy");
		String searchToken = request.getParameter("searchToken");

		app.startDatabaseConnection();

		try {
			if (searchBy.equals("account")) {
				// Searching for a specific account.
				// This utilizes the fact that getAccount() creates a
				// customer object with only that one account in it.
				Account accountFound = app.getAccountByID(searchToken);
				if (accountFound != null) {
					Customer customerFound = accountFound.getCustomer();

					// Sets the attribute in session scope as the search result
					session.setAttribute("CUSTOMERFOUND", customerFound);
				} else {
					session.removeAttribute("CUSTOMERFOUND");
					throw new AccountException("account not found");
				}

			} else if (searchBy.equals("user")) {
				// Searching for a specific user
				// This finds all information about the user, including all
				// his/hers accounts

				User userFound = app.getUserByUsername(searchToken);
				if (userFound instanceof Customer) {
					Customer customerFound = (Customer) userFound;

					// Sets the attribute in session scope as the search result
					session.setAttribute("CUSTOMERFOUND", customerFound);
				} else {
					session.removeAttribute("CUSTOMERFOUND");
					throw new UserException("customer not found");
				}
			}
		} catch (UserException | AccountException | DatabaseException e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
			session.removeAttribute("CUSTOMERFOUND");
		}

		app.closeDatabaseConnection();

		RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
		rd.forward(request, response);
	}

	/**
	 * Prints the number of users currently in the database.
	 * 
	 * @throws ServletException
	 */
	private void userCount() throws IOException, ServletException {


		try {
			
			PrintWriter out = response.getWriter();
			app.startDatabaseConnection();
			out.println("Number of users: " + app.customerCount());
			app.closeDatabaseConnection();
			
		} catch (DatabaseException e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
			RequestDispatcher rd = request.getRequestDispatcher("login.jsp");
			rd.forward(request, response);
		}
	}

	/**
	 * Logs in as user with given credentials assuming one exists. After this is
	 * done, relevant session attributes are set.
	 */
	private void login() throws ServletException, IOException {
		// Get request username and password
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String passwordhash = "" + password.hashCode();
		HttpSession session = request.getSession();

		try {

			app.startDatabaseConnection();

			// Gets the user object logged in
			User userLoggedIn = app.login(username, passwordhash);

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

				RequestDispatcher rd = request.getRequestDispatcher("customerpage.jsp");
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

		} catch (UserException | UnknownLoginException | DatabaseException e) {
			System.out.println("DefaultServlet::doPost -> Login\nError message: " + e.getMessage());
			String infomessage = e.getMessage();
			app.closeDatabaseConnection();

			request.setAttribute("INFOMESSAGE", infomessage);
			RequestDispatcher rd = request.getRequestDispatcher("login.jsp");
			rd.forward(request, response);
		}
	}

	/**
	 * Deletes the selected account from the database.
	 */
	private void deleteAccount() throws ServletException, IOException {
		Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");

		app.startDatabaseConnection();
		try {

			// Gets account to be deleted
			String accountID = request.getParameter("accountSelected");
			Account delete = loggedInCustomer.getAccountByID(accountID);

			app.removeAccount(delete);
		} catch (AccountException | DatabaseException e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
			// e.printStackTrace();
		}

		app.closeDatabaseConnection();

		RequestDispatcher rd = request.getRequestDispatcher("customerpage.jsp");
		rd.forward(request, response);
	}

	/**
	 * Sets the selected account as the currently logged in users main account.
	 */
	private void selectMainAccount() throws ServletException, IOException {
		app.startDatabaseConnection();

		Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");

		// Gets account to be set as main
		String accountID = request.getParameter("accountSelected");
		Account newMain = loggedInCustomer.getAccountByID(accountID);

		try {
			app.setNewMainAccount(loggedInCustomer, newMain);
			app.closeDatabaseConnection();
		} catch (DatabaseException e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
			RequestDispatcher rd = request.getRequestDispatcher("login.jsp");
			rd.forward(request, response);
		}

		RequestDispatcher rd = request.getRequestDispatcher("customerpage.jsp");
		rd.forward(request, response);
	}

	/**
	 * Creates a new account for the logged in user. Uses autoincremented ID in
	 * the database.
	 */
	private void newAccount() throws ServletException, IOException {

		app.startDatabaseConnection();

		Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");
		try {
			app.createAccount(loggedInCustomer, "NORMAL");
			app.refreshAccountsForCustomer(loggedInCustomer);
		} catch (DatabaseException e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
		}

		app.closeDatabaseConnection();

		RequestDispatcher rd = request.getRequestDispatcher("customerpage.jsp");
		rd.forward(request, response);
	}

	/**
	 * Logs user out, removing information from the session scope and returning
	 * to the login page.
	 */
	private void logOut() throws ServletException, IOException {
		request.getSession().removeAttribute("USER");
		request.getSession().removeAttribute("CUSTOMERFOUND");
		app.logOut();
		RequestDispatcher rd = request.getRequestDispatcher("login.jsp");
		rd.forward(request, response);
	}

	/**
	 * transfers money based on input
	 */
	private void transferMoney() throws ServletException, IOException {
		HttpSession session = request.getSession();
		Customer loggedInCustomer = (Customer) session.getAttribute("USER");

		// Gets required input for transfer
		String recieverType = request.getParameter("receiverType");
		String message = request.getParameter("message");
		String accountIDFrom = request.getParameter("accountToSendFrom");

		app.startDatabaseConnection();

		// Gets the amount to be transferred.
		// Leading and trailing zero's enables empty input in either field
		// to be legal.
		// Also provides useful double casting sanitation.
		String beforedecimalseperator = "0" + request.getParameter("beforedecimalseperator");
		String afterdecimalseperator = request.getParameter("afterdecimalseperator") + "00";
		String transferAmount = beforedecimalseperator + "." + afterdecimalseperator.substring(0, 2);

		// Tries to transfer money
		try {
			Account sourceAccount = app.getAccountByID(accountIDFrom);

			if (loggedInCustomer == null)
				throw new UserException("no customer logged in");

			// Revert transferAmount to DKK which is the currency the
			// database operates on
			double amount = Currency.revert(Double.parseDouble(transferAmount), loggedInCustomer);

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

			// After the transfer is done, the local account information is
			// updated
			app.refreshAccountsForCustomer((Customer) session.getAttribute("USER"));
		} catch (NumberFormatException | UserException | AccountException | TransferException | DatabaseException e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
		}

		app.closeDatabaseConnection();

		RequestDispatcher rd = request.getRequestDispatcher("customerpage.jsp");
		rd.forward(request, response);
	}

	/**
	 * Sets the currently logged in user's prefferedCurrency to the selected
	 * one.
	 */
	private void selectCurrency() throws ServletException, IOException {
		Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");
		Currency currency = Currency.currencyStringToEnum(request.getParameter("currency"));

		app.startDatabaseConnection();

		try {
			app.setCurrency(loggedInCustomer, currency);
		} catch (DatabaseException e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
		}

		app.closeDatabaseConnection();

		RequestDispatcher rd = request.getRequestDispatcher("customerpage.jsp");
		rd.forward(request, response);
	}

	/**
	 * Creates a new customer with given credentials in the database
	 */
	private void createNewCustomer() throws ServletException, IOException {

		System.out.println("Creating new customer");

		String fullname = request.getParameter("fullname");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String passwordhash = "" + password.hashCode();

		Currency currency = Currency.currencyStringToEnum(request.getParameter("currency"));
		app.startDatabaseConnection();

		System.out.println("Got parameters from response object");

		try {
			for (int i = 0; i < username.length(); i++) {
				if (("" + username.charAt(i)).matches("[^a-z]"))
					throw new UserException("invalid username, password or empty full name");
			}

			System.out.println("Correct username");

			// Creates customer in database and sets subject to login
			app.createCustomer(fullname, username, passwordhash, currency);
			app.closeDatabaseConnection();

			System.out.println("Logging in");

			login();

		} catch (UserException | AlreadyExistsException | CurrencyException | DatabaseException e) {
			String infomessage = e.getMessage();
			request.setAttribute("INFOMESSAGE", infomessage);
			// System.out.println(e.getMessage());
			app.closeDatabaseConnection();
			RequestDispatcher rd = request.getRequestDispatcher("login.jsp");
			rd.forward(request, response);
		}
	}

}
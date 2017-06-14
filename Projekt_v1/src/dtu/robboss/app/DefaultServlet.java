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

	// By default, sendto login page. Overwrite string if other pages are
	// inteded.
	private String destination;

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

		try {
			
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
			case "Create new account":
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
				response.sendRedirect("loginPage.jsp");
			}

		} finally {
			app.closeDatabaseConnection();
		}

	}

	private void forwardToDestination() throws ServletException, IOException {
		RequestDispatcher rd = request.getRequestDispatcher(destination);
		rd.forward(request, response);
	}

	/**
	 * Deletes the current logged in user locally and from database.
	 */
	private void deleteLoggedInUser() throws ServletException, IOException {
		try {

			User userToDelete = (User) request.getSession().getAttribute("USER");
			app.startDatabaseConnection();

			System.out.println("Removing " + userToDelete.getUsername() + ".");
			app.removeUser(userToDelete, false);
			request.getSession().removeAttribute("USER");
			this.destination = "loginPage.jsp";
		} catch (NullPointerException | UserException | AccountException | DatabaseException e) {
			
			this.destination = "customerPage.jsp";
			request.setAttribute("INFOMESSAGE", e.getMessage());
		} finally {

			app.closeDatabaseConnection();
			forwardToDestination();
		}
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

			request.setAttribute("INFOMESSAGE", e.getMessage());
		} finally {

			app.closeDatabaseConnection();
			this.destination = "adminPage.jsp";
			forwardToDestination();
		}
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

			request.setAttribute("INFOMESSAGE", e.getMessage());
		} finally {

			app.closeDatabaseConnection();
			this.destination = "adminPage.jsp";
			forwardToDestination();
		}

	}

	/**
	 * Moves old transactions to an archive
	 */
	private void archiveOldTransactions() throws ServletException, IOException {
		try {

			app.startDatabaseConnection();
			app.storeOldTransactionsInArchive();

		} catch (Exception e) {
			request.setAttribute("INFOMESSAGE", e.getMessage());
		} finally {

			app.closeDatabaseConnection();
			this.destination = "adminPage.jsp";
			forwardToDestination();
		}
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

			request.setAttribute("INFOMESSAGE", e.getMessage());
		} finally {

			app.closeDatabaseConnection();
			this.destination = "adminPage.jsp";
			forwardToDestination();
		}
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

			request.setAttribute("INFOMESSAGE", e.getMessage());
		} finally {

			app.closeDatabaseConnection();
			this.destination = "adminPage.jsp";
			forwardToDestination();
		}
	}

	/**
	 * Deletes user matching the input username.
	 */
	private void deleteUserByAdmin() throws ServletException, IOException {
		User userToDelete = null;

		try {
			app.startDatabaseConnection();

			userToDelete = app.getUserByUsername(request.getParameter("username"));

			// System.out.println("Removing " + userToDelete.getUsername() +
			// ".");

			app.removeUser(userToDelete, true);

		} catch (NullPointerException | UserException | AccountException | DatabaseException e) {

			request.setAttribute("INFOMESSAGE", e.getMessage());
		} finally {

			User userLoggedIn = (User) request.getSession().getAttribute("USER");
			app.closeDatabaseConnection();

			if (userLoggedIn == null || userLoggedIn.equals(userToDelete))
				this.destination = "loginPage.jsp";
			else
				this.destination = "adminPage.jsp";

			forwardToDestination();
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
		String userType = request.getParameter("userType");

		try {

			for (int i = 0; i < username.length(); i++) {
				if (("" + username.charAt(i)).matches("[^a-z]"))
					throw new UserException("invalid username, password or empty full name");
			}

			if (userType.equals("customer")) {

				app.startDatabaseConnection();
				app.createCustomer(fullname, username, passwordhash, Currency.DKK);
			} else if (userType.equals("admin")) {

				app.startDatabaseConnection();
				app.createAdmin(fullname, username, passwordhash);
			}

		} catch (AlreadyExistsException | UserException | CurrencyException | DatabaseException e) {

			request.setAttribute("INFOMESSAGE", e.getMessage());
		} finally {

			app.closeDatabaseConnection();
			this.destination = "adminPage.jsp";
			forwardToDestination();
		}
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

				if (accountFound == null)
					throw new AccountException("account not found");

				// Sets the attribute in session scope as the search result
				Customer customerFound = accountFound.getCustomer();
				session.setAttribute("CUSTOMERFOUND", customerFound);

			} else if (searchBy.equals("user")) {
				// Searching for a specific user
				// This finds all information about the user, including all
				// their accounts

				User userFound = app.getUserByUsername(searchToken);

				if (!(userFound instanceof Customer))
					throw new UserException("customer not found");

				// Sets the attribute in session scope as the search result
				Customer customerFound = (Customer) userFound;
				session.setAttribute("CUSTOMERFOUND", customerFound);
			}

		} catch (UserException | AccountException | DatabaseException e) {

			request.setAttribute("INFOMESSAGE", e.getMessage());
			session.removeAttribute("CUSTOMERFOUND");
		} finally {

			app.closeDatabaseConnection();
			this.destination = "adminPage.jsp";
			forwardToDestination();
		}
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

		} catch (DatabaseException e) {
			app.closeDatabaseConnection();
			request.setAttribute("INFOMESSAGE", e.getMessage());
			this.destination = "loginPage.jsp";
			forwardToDestination();
		} finally {

			app.closeDatabaseConnection();
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

				this.destination = "customerPage.jsp";
			}

			// Checks if user logged in is an admin
			else if (userLoggedIn instanceof Admin) {
				// Cast sfrom user to admin and sets admin as the session
				// attribute
				Admin adminLoggedIn = (Admin) userLoggedIn;
				session.setAttribute("USER", adminLoggedIn);

				// Creates a list to store future admin search results
				session.setAttribute("ACCOUNTSFOUND", new ArrayList<Account>());

				this.destination = "adminPage.jsp";
			}

		} catch (UserException | UnknownLoginException | DatabaseException e) {

			request.setAttribute("INFOMESSAGE", e.getMessage());
			this.destination = "loginPage.jsp";
		} finally {

			app.closeDatabaseConnection();
			forwardToDestination();
		}
	}

	/**
	 * Deletes the selected account from the database.
	 */
	private void deleteAccount() throws ServletException, IOException {
		Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");
		String accountID = request.getParameter("accountSelected");

		app.startDatabaseConnection();
		try {

			Account delete = loggedInCustomer.getAccountByID(accountID);
			app.removeAccount(delete);
		} catch (AccountException | DatabaseException e) {

			request.setAttribute("INFOMESSAGE", e.getMessage());
		} finally {

			app.closeDatabaseConnection();
			this.destination = "customerPage.jsp";
			forwardToDestination();
		}
	}

	/**
	 * Sets the selected account as the currently logged in users main account.
	 */
	private void selectMainAccount() throws ServletException, IOException {

		Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");

		// Gets account to be set as main
		String accountID = request.getParameter("accountSelected");
		Account newMain = loggedInCustomer.getAccountByID(accountID);

		app.startDatabaseConnection();

		try {

			app.setNewMainAccount(loggedInCustomer, newMain);
			this.destination = "customerPage.jsp";
		} catch (DatabaseException e) {

			request.setAttribute("INFOMESSAGE", e.getMessage());
			this.destination = "loginPage.jsp";
		} finally {

			app.closeDatabaseConnection();
			forwardToDestination();
		}
	}

	/**
	 * Creates a new account for the logged in user. Uses autoincremented ID in
	 * the database.
	 */
	private void newAccount() throws ServletException, IOException {

		Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");
		app.startDatabaseConnection();

		try {

			app.createAccount(loggedInCustomer, "NORMAL");
			app.refreshAccountsForCustomer(loggedInCustomer);
		} catch (DatabaseException e) {

			request.setAttribute("INFOMESSAGE", e.getMessage());
		} finally {

			app.closeDatabaseConnection();
			this.destination = "customerPage.jsp";
			forwardToDestination();
		}
	}

	/**
	 * Logs user out, removing information from the session scope and returning
	 * to the login page.
	 */
	private void logOut() throws ServletException, IOException {
		request.getSession().removeAttribute("USER");
		request.getSession().removeAttribute("CUSTOMERFOUND");

		try {
			app.logOut();

			this.destination = "loginPage.jsp";
		} finally {

			app.closeDatabaseConnection();
			forwardToDestination();
		}

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

		// Gets the amount to be transferred.
		// Leading and trailing zero's enables empty input in either field
		// to be legal.
		// Also provides useful double casting sanitation.
		String beforedecimalseperator = "0" + request.getParameter("beforedecimalseperator");
		String afterdecimalseperator = request.getParameter("afterdecimalseperator") + "00";
		String transferAmount = beforedecimalseperator + "." + afterdecimalseperator.substring(0, 2);

		app.startDatabaseConnection();

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

			request.setAttribute("INFOMESSAGE", e.getMessage());
		} finally {

			app.closeDatabaseConnection();
			this.destination = "customerPage.jsp";
			forwardToDestination();
		}
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
			
			request.setAttribute("INFOMESSAGE", e.getMessage());
		} finally {

			app.closeDatabaseConnection();
			this.destination = "customerPage.jsp";
			forwardToDestination();
		}
	}

	/**
	 * Creates a new customer with given credentials in the database
	 */
	private void createNewCustomer() throws ServletException, IOException {


		String fullname = request.getParameter("fullname");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String passwordhash = "" + password.hashCode();

		Currency currency = Currency.currencyStringToEnum(request.getParameter("currency"));


		app.startDatabaseConnection();

		boolean successfullyCreated = false;
		
		try {
			
			for (int i = 0; i < username.length(); i++) {
				if (("" + username.charAt(i)).matches("[^a-z]"))
					throw new UserException("invalid username, password or empty full name");
			}


			// Creates customer in database and sets subject to login
			app.createCustomer(fullname, username, passwordhash, currency);

			successfullyCreated = true;

		} catch (UserException | AlreadyExistsException | CurrencyException | DatabaseException e) {
			
			request.setAttribute("INFOMESSAGE", e.getMessage());
			this.destination = "loginPage.jsp";
			
		} finally {
			
			app.closeDatabaseConnection();
			
			if(successfullyCreated)
				login();
			else
				forwardToDestination();
			
		}
		
		
	}

}
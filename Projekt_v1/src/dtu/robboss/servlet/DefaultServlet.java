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

		if (subject.equals("UserCount")) {
			app.startDatabaseConnection();
			out.println("Amount of users: " + app.userCount());
			app.closeDatabaseConnection();
		}

		if (subject.equals("CreateNewUser")) {
			String fullname = request.getParameter("fullname");
			String username = request.getParameter("username");

			try {
				// checks if username is all lower case TODO make this viewable
				// for the user
				for (int i = 0; i < username.length(); i++) {
					if (("" + username.charAt(i)).matches("[^a-z]"))
						throw new InvalidUsernameException();
				}

				// Sets password and currency
				String password = request.getParameter("password");
				String currencyString = request.getParameter("currency");
				
				Valuta currency;
				switch (currencyString) {
				case "EUR":
					currency = Valuta.EUR;
					break;
				case "USD":
					currency = Valuta.USD;
					break;
				case "GBP":
					currency = Valuta.GBP;
					break;
				case "JPY":
					currency = Valuta.JPY;
					break;
				default:
					currency = Valuta.DKK;
				}
				
				// Creates customer object and sets subject to login
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
				// e.printStackTrace();
			}

		}

		if (subject.equals("Login")) {

			// Get request username and password
			String username = request.getParameter("username");
			String password = request.getParameter("password");

			try {
				HttpSession session = request.getSession();
				app.startDatabaseConnection();
				User userLoggedIn = app.login(username, password);
				
				// Checks if user logged in is a customer
				if (userLoggedIn instanceof Customer) {
					Customer customerLoggedIn = (Customer) userLoggedIn;
					app.refreshAccountsForCustomer(customerLoggedIn);
					session.setAttribute("USER", customerLoggedIn);
					
					// Get transaction history for customer
					List<TransactionHistoryElement> th = app.getTransactionHistory(customerLoggedIn);
					session.setAttribute("TRANSACTIONHISTORY", th);
					app.closeDatabaseConnection();
//					System.out.println(session.getAttribute("TRANSACTIONHISTORY"));
					RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
					rd.forward(request, response);
				}
				
				// Checks if user logged in is an admin
				if (userLoggedIn instanceof Admin) {
					Admin adminLoggedIn = (Admin) userLoggedIn;
					session.setAttribute("USER", adminLoggedIn);
					session.setAttribute("ACCOUNTSFOUND", new ArrayList<Account>());
					app.closeDatabaseConnection();
					RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
					rd.forward(request, response);
				}
			} catch (UnknownLoginException e) {
				System.out.println("DefaultServlet::doPost -> Login\nError message: " + e.getMessage());
				app.closeDatabaseConnection();
				response.sendRedirect("login.html");
				// e.printStackTrace();
			} 
		}
		
		if(subject.equals("Select currency")){
			Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");
			String currencyString = request.getParameter("currency");
			
			Valuta currency;
			switch (currencyString) {
			case "EUR":
				currency = Valuta.EUR;
				break;
			case "USD":
				currency = Valuta.USD;
				break;
			case "GBP":
				currency = Valuta.GBP;
				break;
			case "JPY":
				currency = Valuta.JPY;
				break;
			default:
				currency = Valuta.DKK;
			}
			app.startDatabaseConnection();
			app.setCurrency(loggedInCustomer, currency);
			
			app.closeDatabaseConnection();
//			System.out.println(loggedInCustomer.getCurrency().name());
			RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
			rd.forward(request, response);
		}
		
		if (subject.equals("transfermoney")) {
			
			
			String accountIDFrom = request.getParameter("accountToSendFrom");
//			System.out.println("AccountIDFrom = " + accountIDFrom);
			Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");
			String beforedecimalseperator = "0" + request.getParameter("beforedecimalseperator");
			String afterdecimalseperator = request.getParameter("afterdecimalseperator") + "00";
			String transferAmount = beforedecimalseperator + "." + afterdecimalseperator.substring(0, 2);
			
			HttpSession session = request.getSession();
			String recieverType = request.getParameter("receiverType");
			String message = request.getParameter("message");
			app.startDatabaseConnection();
//			Account sourceAccount = ((Customer) session.getAttribute("USER")).getMainAccount();
			Account sourceAccount = app.getAccount(accountIDFrom);
			
			
//			System.out.println("sourceAccount Customer = " + sourceAccount.getCustomer().getUsername());
			
			try {
				if(loggedInCustomer == null)
					throw new UserNotfoundException();
				
				double amount = Valuta.revert(Double.parseDouble(transferAmount), loggedInCustomer);
				if (recieverType.equals("account")) {
					Account targetAccount = app.getAccount(request.getParameter("receiver"));
					app.transferFromAccountToAccount(sourceAccount, targetAccount, amount,
							message);
				} else if (recieverType.equals("user")) {
					app.transferFromAccountToCustomer(sourceAccount, request.getParameter("receiver"), amount,
							message);
				}

				// Get transaction history for customer
				List<TransactionHistoryElement> th = app.getTransactionHistory((Customer) session.getAttribute("USER"));
				session.setAttribute("TRANSACTIONHISTORY", th);

			} catch (UserNotLoggedInException | TransferException | AccountNotfoundException
					| UserNotfoundException | NumberFormatException e) {
				System.out.println("Error in DefaultServlet::doPost -> transfermoney\nError message: " + e.getMessage());
//				e.printStackTrace();
			}
			
			app.refreshAccountsForCustomer((Customer) session.getAttribute("USER"));
			
			app.closeDatabaseConnection();
			RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
			rd.forward(request, response);

		}

//		if (subject.equals("paybill")) {
//
//		}

		if (subject.equals("DeleteUser")) {
			User userToDelete = (User) request.getSession().getAttribute("USER");
			try {
				app.startDatabaseConnection();
				System.out.println("Removing " + userToDelete.getUsername() + ".");
				app.deleteUser(userToDelete);
				app.closeDatabaseConnection();

				request.getSession().removeAttribute("USER");
				RequestDispatcher rd = request.getRequestDispatcher("login.html");
				rd.forward(request, response);

			} catch (NullPointerException e) {
				// e.printStackTrace();
				System.out.println("DefaultServlet::doPost -> DeleteUser. \nError message: Could not remove user.");
			}
		}

		if (subject.equals("LogOutUser")) {

			request.getSession().removeAttribute("USER");
			app.logOut();
			RequestDispatcher rd = request.getRequestDispatcher("login.html");
			rd.forward(request, response);

		}

		if (subject.equals("NewAccount")) {
			app.startDatabaseConnection();
			Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");
			app.createAccount(loggedInCustomer, false);
			app.refreshAccountsForCustomer(loggedInCustomer);
			app.closeDatabaseConnection();
			RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
			rd.forward(request, response);
		}
		
		if (subject.equals("Set as main account")) {
			app.startDatabaseConnection();
			Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");
			String accountID = request.getParameter("accountSelected");
//			System.out.println("Setting " + accountID + " as MAIN"); 
			Account newMain = loggedInCustomer.getAccountByID(accountID);
			app.setNewMainAccount(loggedInCustomer, newMain);
			app.closeDatabaseConnection();
			RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
			rd.forward(request, response);
			
		}
		
		if (subject.equals("Delete account")){
			// get current user
			Customer loggedInCustomer = (Customer) request.getSession().getAttribute("USER");
			// getting account to be deleted
			String accountID = request.getParameter("accountSelected");
			
			app.startDatabaseConnection();
			Account delete = loggedInCustomer.getAccountByID(accountID);
			app.deleteAccount(delete);
			app.closeDatabaseConnection();
			RequestDispatcher rd = request.getRequestDispatcher("userpage.jsp");
			rd.forward(request, response);
		}

		// ADMIN ONLY
		if (subject.equals("Search")) {

			HttpSession session = request.getSession();
			app.startDatabaseConnection();
			
			String searchBy = request.getParameter("searchBy");
			String searchToken = request.getParameter("searchToken");
			//ArrayList<Account> accounts = new ArrayList<Account>(); TODO OLD CODE
			
			try {
				if (searchBy.equals("account")) {
					// Searching for a specific account 
					// This utilizes the fact that getAccount creates a customer with only
					// that 1 account in it.
					//accounts.add(app.getAccount(searchToken)); TODO OLD CODE
					Customer customerFound = app.getAccount(searchToken).getCustomer();
					session.setAttribute("CUSTOMERFOUND", customerFound);

				} else if (searchBy.equals("user")) {
					// Searcing for a specific user
					//accounts.addAll(app.getAccountsByUser(searchToken));
					Customer customerFound = app.getCustomer(searchToken);
					session.setAttribute("CUSTOMERFOUND", customerFound);

				}
			} catch (Exception e) {
				System.out.println("DefaultServlet::doPost -> Search \nErro message: " + e.getMessage());
				e.printStackTrace();

			}
			
			app.closeDatabaseConnection();
			RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
			rd.forward(request, response);
		}

		if (subject.equals("CreateNewUserAdmin")) {
			// ADMIN CREATES CUSTOMER
			if (request.getParameter("userType").equals("customer")) {
				String fullname = request.getParameter("fullname");
				String username = request.getParameter("username");
				String password = request.getParameter("password");
				String currencyString = request.getParameter("currency");
				
				Valuta currency;
				switch (currencyString) {
				case "EUR":
					currency = Valuta.EUR;
					break;
				case "USD":
					currency = Valuta.USD;
					break;
				case "GBP":
					currency = Valuta.GBP;
					break;
				case "JPY":
					currency = Valuta.JPY;
					break;
				default:
					currency = Valuta.DKK;
				}

				try {
					app.startDatabaseConnection();
					app.createCustomer(fullname, username, password, currency);
					app.closeDatabaseConnection();
				} catch (AlreadyExistsException e) {
					System.out.println("DefaultServlet::doPost -> CreateNewCustomer\nError message: " + e.getMessage());
				}
			}
			else
			// ADMIN CREATES ADMIN
			if (request.getParameter("userType").equals("admin")) {
				String fullname = request.getParameter("fullname");
				String username = request.getParameter("username");
				String password = request.getParameter("password");

				try {
					app.startDatabaseConnection();
					app.createAdmin(fullname, username, password);
					app.closeDatabaseConnection();
				} catch (AlreadyExistsException e) {
					System.out.println("DefaultServlet::doPost -> CreateNewAdmin\nError message: " + e.getMessage());
				}
			}

			RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
			rd.forward(request, response);

		}

		if (subject.equals("DeleteUserByAdmin")) {
			try {
				app.startDatabaseConnection();
				User userToDelete = app.getUser(request.getParameter("username"));
				System.out.println("Removing " + userToDelete.getUsername() + ".");
				app.deleteUser(userToDelete);
				
				app.closeDatabaseConnection();
				RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
				// TODO: if admin deletes itself, redirect to login page instead
				rd.forward(request, response);

			} catch (NullPointerException e) {
				// e.printStackTrace();
				System.out.println("DefaultServlet::doPost -> DeleteUserAdmin\nErorr message: Could not remove user.");
			}
		}
		
		if (subject.equals("Perform Batch")) {
			
			try {
				app.startDatabaseConnection();
				app.applyInterestToAllAccounts();
				app.storeOldTransactionsInArchive();
				app.closeDatabaseConnection();
				RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
				rd.forward(request, response);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (subject.equals("Apply Interest")) {
			
			try {
				app.startDatabaseConnection();
				app.applyInterestToAllAccounts();
				app.refreshAccountsForCustomer((Customer) request.getSession().getAttribute("CUSTOMERFOUND"));
				app.closeDatabaseConnection();
				RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
				rd.forward(request, response);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (subject.equals("Archive Old Transactions")) {
			
			try {
				app.startDatabaseConnection();
				app.storeOldTransactionsInArchive();
				app.closeDatabaseConnection();
				RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
				rd.forward(request, response);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (subject.equals("Set Interest")) {
			
			try {
				double interest = Double.parseDouble(request.getParameter("interest"));
				String accountID = request.getParameter("accountSelected");
				
				// Sets interest in database
				app.startDatabaseConnection();
				app.setInterest(accountID, interest);
				app.refreshAccountsForCustomer((Customer) request.getSession().getAttribute("CUSTOMERFOUND"));
				app.closeDatabaseConnection();
				RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
				rd.forward(request, response);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (subject.equals("Set Credit")) {
			
			try {
				double credit = Double.parseDouble(request.getParameter("credit"));
				String accountID = request.getParameter("accountSelected");
				
				// Sets credit in database
				app.startDatabaseConnection();
				app.setCredit(accountID, credit);
				app.refreshAccountsForCustomer((Customer) request.getSession().getAttribute("CUSTOMERFOUND"));
				app.closeDatabaseConnection();
				RequestDispatcher rd = request.getRequestDispatcher("adminpage.jsp");
				rd.forward(request, response);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
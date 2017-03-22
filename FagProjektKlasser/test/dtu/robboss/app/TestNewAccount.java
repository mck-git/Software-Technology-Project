package dtu.robboss.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestNewAccount extends SampleDataSetup {
	
	Application app = new Application();
	
	User u = new User("name", "u", "pass", "cpr");

	Account account = new Account(u, 10);
	Account account2 = new Account(u, 11);

	@Test
	public void testNewAccount() throws Exception {

		// Admin logs in
		app.login("admin", "admin");

		// Admin creates account
		app.createNewAccount(account);

		// Checks if account was successfully created
		assertEquals(1, app.accountCount());
	}

	@Test
	public void testNewAccountAdminNotLoggedIn() throws Exception {

		try {

			// Admin tries to create account
			app.createNewAccount(account);

			fail("Admin not logged in exception expected");
		} catch (AdminNotLoggedInException e) {

		}

		// Checks if account was successfully not added
		assertEquals(0, app.accountCount());

	}

	@Test
	public void testAccountAlreadyExists() throws Exception {

		// Admin logs in
		app.login("admin", "admin");
		assertTrue(app.adminLoggedIn());

		try {
			// Admin tries to add the same account twice
			app.createNewAccount(account);
			app.createNewAccount(account);

			fail("Already Exists exception expected");
		} catch (AlreadyExistsException e) {

		}

		// Checks if only 1 account was added, as expected
		assertEquals(app.accountCount(), 1);
	}

	@Test
	public void testDeleteAccount() throws Exception {

		// Admin logs in
		app.login("admin", "admin");
		assertTrue(app.adminLoggedIn());

		// Admin creates account
		app.createNewAccount(account);
		assertEquals(1, app.accountCount());

		// Admin deletes account
		app.deleteAccount(account);
		assertEquals(0, app.accountCount());

	}

	
	@Test
	public void testDeleteMainAccount() throws Exception {
		// Admin logs in
		app.login("admin", "admin");

		// Admin creates account
		app.createNewAccount(account);
		app.createNewAccount(account2);

		// Checks if account was successfully created
		assertEquals(2, app.accountCount());

		// Checks if account with ID 1 is the main account for user u
		assertEquals(account, u.getMainAccount());

		// Admin tries to delete main account
		app.deleteAccount(account);

		assertEquals(u.getAccounts().size(), 1);

		// Checks if account2 is new main account for u
		assertEquals(account2, u.getMainAccount());
	}

	@Test
	public void testNewMainAccount() throws Exception {
		// Admin logs in
		app.login("admin", "admin");

		// Admin creates account
		app.createNewAccount(account);
		app.createNewAccount(account2);

		// Checks if account was successfully created
		assertEquals(2, app.accountCount());

		// Checks if account with ID 1 is the main account for user u
		assertEquals(account, u.getMainAccount());

		// Sets account2 as main for u
		app.setUserMainAccount(u, account2);

		assertEquals(account2, u.getMainAccount());

	}
	
	@Test
	public void testDeleteAccountAdminNotLoggedIn() throws Exception {

		// Admin logs in
		app.login("admin", "admin");

		// Admin creates account
		app.createNewAccount(account);
		app.createNewAccount(account2);

		// Checks if account was successfully created
		assertEquals(2, app.accountCount());

		// Checks if account with ID 1 is the main account for user u
		assertEquals(account, u.getMainAccount());

		// admin logs out
		app.logOut();
		assertFalse(app.adminLoggedIn());

		try {
			// Admin tries to delete account while not logged in
			app.deleteAccount(account);

			fail("Admin Not Logged In Exception expected");
		} catch (AdminNotLoggedInException e) {

		}
	}

}

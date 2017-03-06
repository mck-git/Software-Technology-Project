package dtu.robboss.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestNewAccount extends SampleDataSetup {
	User u = new User("name", "pass", "cpr");

	Account account = new Account(u, "1");

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

}

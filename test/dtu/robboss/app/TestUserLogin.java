package dtu.robboss.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestUserLogin {

	Application app = new Application();
	User borge = new User("Borge", "kodeord", "123456789");

	@Test
	public void testLoginUser() throws Exception {

		// Admin logs in
		assertFalse(app.adminLoggedIn());
		app.login("admin", "admin");
		assertTrue(app.adminLoggedIn());

		// Admin creates user
		app.createNewUser(borge);
		assertEquals(1, app.userCount());

		// Admin logs out
		app.logOut();

		// Checks if no user is logged in
		assertFalse(app.userLoggedIn());

		// User logs in
		app.login("Borge", "kodeord");

		// Checks if user successfully logged in
		assertTrue(app.userLoggedIn());

	}

	@Test
	public void testLoginUserFails() throws Exception {
		// Admin logs in
		assertFalse(app.adminLoggedIn());
		app.login("admin", "admin");
		assertTrue(app.adminLoggedIn());

		// Admin creates user
		app.createNewUser(borge);
		assertEquals(1, app.userCount());

		// Admin logs out
		app.logOut();

		// Checks if no user is logged in
		assertFalse(app.userLoggedIn());
		
		// User tries to login
		try{
			app.login("hej123", "jeg kan ikke huske det lol");
			
			fail("Unknown login exception expected");
		} catch(UnknownLoginException e){
			
		}
		
	}
	
	@Test
	public void testUserLogout() throws Exception {
		// Admin logs in
		assertFalse(app.adminLoggedIn());
		app.login("admin", "admin");
		assertTrue(app.adminLoggedIn());

		// Admin creates user
		app.createNewUser(borge);
		assertEquals(1, app.userCount());

		// Admin logs out
		app.logOut();

		// Checks if no user is logged in
		assertFalse(app.userLoggedIn());
		
		// User logs in
		app.login("Borge", "kodeord");

		// Checks if user successfully logged in
		assertTrue(app.userLoggedIn());

		// User logs out
		app.logOut();
		
		// Checks if user successfully logged out
		assertFalse(app.userLoggedIn());
		
	}

}

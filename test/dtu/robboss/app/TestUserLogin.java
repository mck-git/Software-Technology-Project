package dtu.robboss.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestUserLogin extends SampleDataSetup {

	
	@Test
	public void testLoginUser() throws Exception {

		

		// Checks if no user is logged in
		assertFalse(app.userLoggedIn());

		// User logs in
		app.login("borge", "kodeord");

		// Checks if user successfully logged in
		assertTrue(app.userLoggedIn());

	}

	@Test
	public void testLoginUserFails() throws Exception {
		
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
		
		// Checks if no user is logged in
		assertFalse(app.userLoggedIn());
		
		// User logs in
		app.login("borge", "kodeord");

		// Checks if user successfully logged in
		assertTrue(app.userLoggedIn());

		// User logs out
		app.logOut();
		
		// Checks if user successfully logged out
		assertFalse(app.userLoggedIn());
		
	}
	
	@Test
	public void testAdminLogin() throws Exception {
		// Checks if no user is logged in
		assertFalse(app.userLoggedIn());
		
		// Admin logs in
		app.login("admin", "admin");
		
		// Checks if admin logged in successfully
		assertTrue(app.adminLoggedIn());
		
		// Checks if user (not admin) is logged in
		assertFalse(app.userLoggedIn());
		
	}

}

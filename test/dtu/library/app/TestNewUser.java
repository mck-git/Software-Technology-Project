package dtu.library.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestNewUser {

	@Test
	public void testLoginAdmin() {

		Application app = new Application();

		assertFalse(app.adminLoggedIn());

		app.login("admin", "admin");

		assertTrue(app.adminLoggedIn());

	}

	@Test
	public void testNewUser() throws Exception {
		Application app = new Application();

		assertFalse(app.adminLoggedIn());

		app.login("admin", "admin");

		assertTrue(app.adminLoggedIn());

		assertEquals(0, app.userCount());

		User user = new User("user1", "userpass", "123456-1234");

		app.createNewUser(user);

		assertEquals(1, app.userCount());
	}
	
	@Test
	public void testNewUserAldreadyExists() throws Exception {
		Application app = new Application();

		assertFalse(app.adminLoggedIn());

		app.login("admin", "admin");

		assertTrue(app.adminLoggedIn());

		assertEquals(0, app.userCount());

		User user = new User("user1", "userpass", "123456-1234");
		
		try{
		app.createNewUser(user);
		app.createNewUser(user);
		
		fail("User already exist");
		} catch(UserAlreadyExistException e){
			
		}

		assertEquals(1, app.userCount());
	}

	@Test
	public void testNewUserAdminNotLoggedIn() throws Exception {
		Application app = new Application();

		assertFalse(app.adminLoggedIn());

		assertEquals(0, app.userCount());

		User user = new User("user1", "userpass", "123456-1234");

		try {
			app.createNewUser(user);
			fail("Admin not logged in");
		} catch (AdminNotLoggedInException e) {

		}

		assertEquals(0, app.userCount());
	}

	@Test
	public void testDeleteUser() throws Exception {
		Application app = new Application();

		assertFalse(app.adminLoggedIn());

		app.login("admin", "admin");

		assertTrue(app.adminLoggedIn());

		assertEquals(0, app.userCount());

		User user = new User("user1", "userpass", "123456-1234");

		app.createNewUser(user);

		assertEquals(1, app.userCount());

		app.deleteUser(user);

		assertEquals(0, app.userCount());

	}
	
	@Test
	public void testDeleteUserAdminNotLoggedIn() throws Exception {
		Application app = new Application();

		assertFalse(app.adminLoggedIn());

		app.login("admin", "admin");

		assertTrue(app.adminLoggedIn());

		assertEquals(0, app.userCount());

		User user = new User("user1", "userpass", "123456-1234");

		app.createNewUser(user);
		
		app.adminLogOut();
		
		assertEquals(1, app.userCount());
		
		try{
		app.deleteUser(user);
		fail("Admin not logged in");
		} catch (AdminNotLoggedInException e){
			
		}

		assertEquals(1, app.userCount());

	}

}

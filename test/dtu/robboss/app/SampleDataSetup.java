package dtu.robboss.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;

public class SampleDataSetup {

	Application app = new Application();

	@Before
	public void setUp() throws Exception {

		User borge = new User("Borge", "kodeord", "123456789");
		User aage = new User("Aage", "entotrefirefem","987654321");
		User karla = new User("Karla", "dugætterdetaldrig", "56789123");
		
		
		// Admin logs in
		assertFalse(app.adminLoggedIn());
		app.login("admin", "admin");
		assertTrue(app.adminLoggedIn());

		// Admin creates user
		app.createNewUser(borge);
		assertEquals(1, app.userCount());

		app.createNewUser(aage);
		app.createNewUser(karla);
		
		// Admin logs out
		app.logOut();

	}

}

package dtu.robboss.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;

public class SampleDataSetup {

	Application app = new Application();

	@Before
	public void setUp() throws Exception {

		User borge = new User("Borge", "borge", "kodeord", "123456789");
		User aage = new User("Aage", "aage", "entotrefirefem", "987654321");
		User karla = new User("Karla", "karla", "dugætterdetaldrig", "56789123");
		Account accountBorge1 = new Account(borge, 1);
		Account accountAage1 = new Account(aage, 2);
		Account accountKarla1 = new Account(karla, 3);

		Account accountBorge2 = new Account(borge, 4);
		Account accountAage2 = new Account(aage, 5);
		Account accountKarla2 = new Account(karla, 6);

		
		
		// Admin logs in
		assertFalse(app.adminLoggedIn());
		app.login("admin", "admin");
		assertTrue(app.adminLoggedIn());

		// Admin creates user
		app.createNewUser(borge);
		assertEquals(1, app.userCount());

		app.createNewUser(aage);
		app.createNewUser(karla);

		app.createNewAccount(accountBorge1);
		app.createNewAccount(accountAage1);
		app.createNewAccount(accountKarla1);
		

		app.createNewAccount(accountBorge2);
		app.createNewAccount(accountAage2);
		app.createNewAccount(accountKarla2);

		// Admin logs out
		app.logOut();

	}

}

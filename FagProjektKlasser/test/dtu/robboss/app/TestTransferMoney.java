package dtu.robboss.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestTransferMoney extends SampleDataSetup {

	@Test
	public void testAddMoneyToMainAccount() throws Exception {

		User b = app.getUser("borge");

		// Adds money to Borge's account (defaults to main)
		app.changeBalanceUser(b, 1000);

		// Checks if 1000 was successfully added to Borge's account
		assertEquals(b.getMainAccount().getBalance(), 1000);

	}

	@Test
	public void testRemoveMoneyFromMainAccount() throws Exception {

		User b = app.getUser("borge");

		// Removes money from Borge's account (defaults to main)
		app.changeBalanceUser(b, -1000);

		// Checks if 1000 was successfully subtracted from Borge's account
		assertEquals(b.getMainAccount().getBalance(), -1000);
	}

	@Test
	public void testTransferMoneyBetweenMainAccounts() throws Exception {

		User b = app.getUser("borge");
		User a = app.getUser("aage");

		// Borge logs in
		app.login("borge", "kodeord");

		// Checks if user logged in correctly
		assertTrue(app.userLoggedIn());

		// Transfers money from Borge's main to Aage's main
		app.transferMoneyUser(b, a, 1000);

		// Checks that money was successfully deducted from Borge and added to
		// Aage
		assertEquals(b.getMainAccount().getBalance(), -1000);
		assertEquals(a.getMainAccount().getBalance(), 1000);
	}

	@Test
	public void testAddMoneyToAccount() throws Exception {
		
		// Adds money to account with ID 4
		app.changeBalanceAccount(4, 1000);
		
		// Checks if money was added successfully
		assertEquals(app.getAccount(4).getBalance(),1000);	
	}
	
	@Test
	public void testRemoveMoneyFromAccount() throws Exception {
		
		// Removes money from account with ID 4
		app.changeBalanceAccount(4, -1000);
		
		// Checks if money was deducted successfully
		assertEquals(app.getAccount(4).getBalance(),-1000);
		
	}
	
	@Test
	public void testTransferMoneyBetweenAccounts() throws Exception {
		
		
	}

}

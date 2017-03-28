package dtu.robboss.app;

import java.util.ArrayList;
import java.util.HashSet;

public class User {

	private String username;
	private String name;
	private String password;
	private String cpr;
	private ArrayList<Account> accounts;
	private Account mainAccount;
	private HashSet<UserMessage> userMessages; // TODO maybe think more about this name? "user" is redundant?

	public User(String name, String username, String password, String cpr) {
		this.username = username;
		this.name = name;
		this.password = password;
		this.cpr = cpr;
		this.userMessages = new HashSet<>();
		mainAccount = new Account(this, "1"); // TODO id? connect to database
		
		//Creating account list and adding mainAccount.
		this.accounts = new ArrayList<Account>();
		accounts.add(mainAccount);

	}

	////////////////////////
	// ACCOUNT MANAGEMENT //
	////////////////////////

	public void addAccount(Account newAccount) {
		accounts.add(newAccount);
	}

	public void removeAccount(Account account) {
		accounts.remove(account);
	}

	public Account getMainAccount() {
		return this.mainAccount;
	}

	public void setMainAccount(Account newMain) {
		//needs sanitation?
		if(accounts.contains(newMain))
			this.mainAccount = newMain;
		else
			System.out.println("ERROR: account not found in accounts list.");
	}

	/////////////////////////
	// GETTERS AND SETTERS //
	/////////////////////////

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCpr() {
		return cpr;
	}

	public void setCpr(String cpr) {
		this.cpr = cpr;
	}

	public ArrayList<Account> getAccounts() {
		return accounts;
	}
}

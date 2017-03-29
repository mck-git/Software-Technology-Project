package dtu.robboss.app;

import java.util.ArrayList;
import java.util.HashSet;

public class User {

	private String username;
	private String fullName;
	private String password;
	private String cpr; // TODO add CPR to USER database
	private ArrayList<Account> accounts;
	private Account mainAccount;
	private HashSet<UserMessage> userMessages; // TODO maybe think more about this name? "user" is redundant?

	// TODO new constructor? taking a ResultSet as argument?
	public User(String name, String username, String password) {
		this.fullName = name;
		this.username = username;
		this.password = password;
		this.userMessages = new HashSet<>();
		
		//Creating account list and adding mainAccount.
		this.accounts = new ArrayList<Account>();
		
		mainAccount = new Account(this, "1"); // TODO id? connect to database
//		accounts.add(mainAccount);

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

	public String toString(){
		return fullName + ", username: " + username + ", password: " + password;
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

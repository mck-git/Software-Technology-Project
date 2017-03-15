package dtu.robboss.app;

import java.util.ArrayList;

public class User {

	private String username;
	private String name;
	private String password;
	private String cpr;
	private ArrayList<Account> accounts;

	public User(String name, String username, String password, String cpr) {
		this.username = username;
		this.name = name;
		this.password = password;
		this.cpr = cpr;
		this.accounts = new ArrayList<Account>();

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
		return accounts.get(0);
	}

	public void setMainAccount(Account newMain) {
		if (accounts.contains(newMain)) {
			accounts.remove(newMain);
			accounts.add(0, newMain);
		}
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

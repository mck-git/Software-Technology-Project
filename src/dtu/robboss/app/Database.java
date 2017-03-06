package dtu.robboss.app;

import java.util.ArrayList;
import java.util.List;

public class Database {

	int userCount = 0;
	int accountCount = 0;

	List<User> users = new ArrayList<>();
	List<Account> accounts = new ArrayList<>();

	////////////
	// AMOUNT //
	////////////

	public Object userCount() {
		return userCount;
	}

	public Object accountCount() {
		return accountCount;
	}

	///////////////
	// SEARCHING //
	///////////////

	public boolean containsUser(User user) {
		return users.contains(user);
	}

	public boolean containsAccount(Account account) {
		return accounts.contains(account);
	}

	////////////////////
	// ADD AND REMOVE //
	////////////////////

	public void addUser(User user) {
		userCount++;
		users.add(user);
	}

	public void addAccount(Account account) {
		accountCount++;
		accounts.add(account);
	}

	public void removeUser(User user) {
		userCount--;
		users.remove(user);
	}

	public void removeAccount(Account account) {
			
		if (account.getBalance() == 0) {
			accountCount--;
			accounts.remove(account);
		}
		
		
		
	}

}

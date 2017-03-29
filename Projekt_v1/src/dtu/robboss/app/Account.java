package dtu.robboss.app;

//HEJ MAGNUS!

//,,,,,,,,,,

public class Account {

	private User user;
	private String accountNumber;
	private int balance;
	private int credit;

	public Account(User user, String accountNumber) {
		this.user = user;
		this.accountNumber = accountNumber;
		this.balance = 0;
		this.credit = 0;

		user.addAccount(this);
	}
	
	public Account(User user, String accountNumber, int balance, int credit) {
		this.user = user;
		this.accountNumber = accountNumber;
		this.balance = balance;
		this.credit = credit;

		user.addAccount(this);
	}

	
	public boolean isMainAccount() {
		return this.getUser().getMainAccount().equals(this);
	}

	public User getUser() {
		return this.user;
	}

	public String getAccountNumber() {
		return this.accountNumber;
	}

	public int getBalance() {
		return this.balance;
	}

	public int getCredit() {
		return this.credit;
	}

	public void changeBalance(int i) {
		this.balance += i;

	}

}
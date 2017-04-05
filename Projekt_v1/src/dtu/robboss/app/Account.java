package dtu.robboss.app;

//HEJ MAGNUS!

//,,,,,,,,,,

public class Account {

	private User user;
	private String accountNumber;
	private double balance;
	private double credit;

	public Account(User user, String accountNumber) {
		this.user = user;
		this.accountNumber = accountNumber;
		this.balance = 0;
		this.credit = 0;

		user.addAccount(this);
	}
	
	public Account(User user, String accountNumber, double balance, double credit) {
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

	public double getBalance() {
		return this.balance;
	}

	public double getCredit() {
		return this.credit;
	}

	public void changeBalance(double d) {
		this.balance += d;
		this.balance = Math.round(this.balance*100.0)/100.0;
	}

}
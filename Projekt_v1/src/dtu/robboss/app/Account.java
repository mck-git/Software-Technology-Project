package dtu.robboss.app;

//HEJ MAGNUS!

//,,,,,,,,,,

public class Account {

	private Customer customer;
	private String accountNumber;
	private double balance;
	private double credit;
	private String type;
	private double interest;

	public Account(Customer customer, String accountNumber) {
		this.customer = customer;
		this.accountNumber = accountNumber;
		this.balance = 0;
		this.credit = 0;
		this.interest = 1.05;

		customer.addAccount(this);
	}

	// public Account(Customer customer, String accountNumber, double balance,
	// double credit, String type) {
	// this.customer = customer;
	// this.accountNumber = accountNumber;
	// this.balance = balance;
	// this.credit = credit;
	// this.type = type;
	// this.interest = 1.0;
	//
	// customer.addAccount(this);
	// }

	public Account(Customer customer, String accountNumber, double balance, double credit, String type,
			double interest) {
		this.customer = customer;
		this.accountNumber = accountNumber;
		this.balance = balance;
		this.credit = credit;
		this.type = type;
		this.interest = interest;

		customer.addAccount(this);
	}
	
	public void setBalance(double newBalance) {
		this.balance = newBalance;
	}

	public boolean isMainAccount() {
		return this.getCustomer().getMainAccount().equals(this);
	}

	public Customer getCustomer() {
		return this.customer;
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
		this.balance = Math.round(this.balance * 100.0) / 100.0;
	}

	public String getType() {
		return this.type;
	}

	public double getInterest() {
		return this.interest;
	}

	public void setInterest(double interest) {
		this.interest = interest;
	}

	public void setType(String type) {

		if (type.equals("MAIN"))
			this.type = type;

		if (type.equals("NORMAL"))
			this.type = type;

		// TODO: If not?
	}

}
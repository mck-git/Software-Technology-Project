package dtu.robboss.app;

//HEJ MAGNUS!

//,,,,,,,,,,

public class Account {

	private Customer customer;
	private String accountNumber;
	private double balance;
	private double credit;
	private String type;

	public Account(Customer customer, String accountNumber) {
		this.customer = customer;
		this.accountNumber = accountNumber;
		this.balance = 0;
		this.credit = 0;

		customer.addAccount(this);
	}
	
	public Account(Customer customer, String accountNumber, double balance, double credit, String type) {
		this.customer = customer;
		this.accountNumber = accountNumber;
		this.balance = balance;
		this.credit = credit;
		this.type = type;

		customer.addAccount(this);
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
		this.balance = Math.round(this.balance*100.0)/100.0;
	}
	
	public String getType() {
		return this.type;
	}

}
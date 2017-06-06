package dtu.robboss.app;

//HEJ MAGNUS!

//,,,,,,,,,,

public class Account {

	private Customer customer;
	private String id;
	private double balance;
	private double credit;
	private String type;
	private double interest;

	public Account(Customer customer, String id) {
		this.customer = customer;
		this.id = id;
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

	public Account(Customer customer, String id, double balance, double credit, String type,
			double interest) {
		this.customer = customer;
		this.id = id;
		this.balance = Math.round(balance*100.0)/100.0;
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
		return this.id;
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

	public String toString() {
		
		String balance = Valuta.convert(this.balance, this.customer);
		String credit = Valuta.convert(this.credit, this.customer);

		String s = "ID: " + this.id + ", balance: " + balance + ", credit: " + credit
				+ ", interest: " + this.interest + ", type: " + this.type;

		return s;
	}

}
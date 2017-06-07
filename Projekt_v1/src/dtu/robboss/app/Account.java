package dtu.robboss.app;

public class Account {

	private Customer customer;
	private String id;
	private double balance;
	private double credit;
	private String type;
	private double interest;

	/**
	 * Constructor for the Account class. Takes the following parameters:
	 * 
	 * @param customer: every account belongs to a customer (user that is not admin).
	 * @param id: unique ID, auto generated in database
	 * @param balance: how many DKK are in the account (can be shown in other currencies, but stored as DKK)
	 * @param credit: how far below 0 the account balance is allowed to go
	 * @param type: either "normal" or "main". Every customer has at most 1 main account
	 * @param interest: 1.05 corresponds to 5 % interest, applied during batch job
	 */
	public Account(Customer customer, String id, double balance, double credit, String type, double interest) {
		this.customer = customer;
		this.id = id;
		
		// round to 2 decimal places
		this.balance = Math.round(balance * 100.0) / 100.0;
		
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

		String s = "ID: " + this.id + ", balance: " + balance + ", credit: " + credit + ", interest: " + this.interest
				+ ", type: " + this.type;

		return s;
	}

}
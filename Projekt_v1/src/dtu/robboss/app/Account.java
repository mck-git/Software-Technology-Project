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
	 * @param customer
	 *            : every account belongs to a customer (user that is not
	 *            admin).
	 * @param id
	 *            : unique ID, auto generated in database
	 * @param balance
	 *            : how many DKK are in the account (can be shown in other
	 *            currencies, but stored as DKK)
	 * @param credit
	 *            : how far below 0 the account balance is allowed to go
	 * @param type
	 *            : either "normal" or "main". Every customer has at most 1 main
	 *            account
	 * @param interest
	 *            : 1.05 corresponds to 5 % interest, applied during batch job
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

	// ##########################
	// GETTERS, ACCESSORS
	// ##########################

	public boolean isMainAccount() {
		return this.getCustomer().getMainAccount().equals(this);

	}

	public Customer getCustomer() {
		return this.customer;
	}

	public String getAccountID() {
		return this.id;
	}

	public double getBalance() {
		return this.balance;
	}

	public double getCredit() {
		return this.credit;
	}

	public String getType() {
		return this.type;
	}

	public double getInterest() {
		return this.interest;
	}

	public String toString() {

		String balance = Currency.convert(this.balance, this.customer);
		String credit = Currency.convert(this.credit, this.customer);

		String s = "ID: " + this.id + ", balance: " + balance + ", credit: " + credit + ", interest: " + this.interest
				+ ", type: " + this.type;

		return s;
	}

	// ##########################
	// SETTERS, MUTATORS
	// ##########################

	public void setBalance(double newBalance) {
		this.balance = Math.round(newBalance * 100.0) / 100.0;
	}

	/**
	 * Relative change to balance. Used when transferring money: Once for sender
	 * account, once for receiver account. Rounds to 2 decimal places.
	 * 
	 * @param relativeChange
	 *            : how much is subtracted from sender account, and added to
	 *            receiver account.
	 */
	public void changeBalance(double relativeChange) {
		this.balance += relativeChange;

		this.balance = Math.round(this.balance * 100.0) / 100.0;
	}

	public void setInterest(double interest) {
		if (interest >= 0)
			this.interest = interest;
	}

	/**
	 * Accounts have 2 types: MAIN and NORMAL.
	 * 
	 * @param type
	 *            : String representing the account type. Case insensitive.
	 *            Stores type in caps in account object, i.e. "main" -> "MAIN".
	 */
	public void setType(String type) {

		if (type.equalsIgnoreCase("MAIN"))
			this.type = "MAIN";

		if (type.equalsIgnoreCase("NORMAL"))
			this.type = "NORMAL";
	}

}
package dtu.robboss.app;

//HEJ MAGNUS!

//,,,,,,,,,,

public class Account {

	private Customer customer;
	private String accountNumber;
	private int balance;
	private int credit;

	public Account(Customer customer, String accountNumber) {
		this.customer = customer;
		this.accountNumber = accountNumber;
		this.balance = 0;
		this.credit = 0;

		customer.addAccount(this);
	}
	
	public Account(Customer customer, String accountNumber, int balance, int credit) {
		this.customer = customer;
		this.accountNumber = accountNumber;
		this.balance = balance;
		this.credit = credit;

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
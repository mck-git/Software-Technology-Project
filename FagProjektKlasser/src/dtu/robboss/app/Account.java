package dtu.robboss.app;

//HEJ MAGNUS!

//,,,,,,,,,,

public class Account {

	private User user;
	private int id;
	private int balance;
	private int credit;

	public Account(User user, int id) {
		this.user = user;
		this.id = id;
		this.balance = 0;
		this.credit = 0;

		user.addAccount(this);
	}

	public boolean isMainAccount() {
		return this.getUser().getMainAccount().equals(this);
	}

	public User getUser() {
		return this.user;
	}

	public int getID() {
		return this.id;
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

package dtu.robboss.app;

public class TransactionHistoryElement {

	String date;
	
	int fromAccountID, toAccountID;
	double amount, fromBalance, toBalance;
	String message, fromUserName, toUserName;
	//Database format: DATE, FROMACCOUNT, TOACCOUNT, FROMUSER, TOUSER, FROMBALANCE, TOBALANCE, AMOUNT, MESSAGE
	public TransactionHistoryElement(String date, int fromAccountID, int toAccountID, String fromUserName, String toUserName, double fromBalance, double toBalance, double amount, String message){ 
		this.date = date;
		this.fromAccountID = fromAccountID;
		this.toAccountID = toAccountID;
		this.fromUserName = fromUserName;
		this.toUserName = toUserName;
		this.fromBalance = fromBalance;
		this.toBalance = toBalance;
		this.amount = amount;
		this.message = message;
	}
	
	public int getFromAccountID() {
		return fromAccountID;
	}

	public int getToAccountID() {
		return toAccountID;
	}

	public double getFromBalance() {
		return fromBalance;
	}

	public double getToBalance() {
		return toBalance;
	}

	public String getFromUserName() {
		return fromUserName;
	}

	public String getToUserName() {
		return toUserName;
	}

	public String getDate() {
		return this.date;
	}

	public double getAmount() {
		return this.amount;
	}

	public String getMessage() {
		return this.message;
	}
	
}
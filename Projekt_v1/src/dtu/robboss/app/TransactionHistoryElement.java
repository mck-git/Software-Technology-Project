package dtu.robboss.app;

public class TransactionHistoryElement {

	String date;

	String sourceAccountID, targetAccountID;
	double transferAmount, sourceBalance, targetBalance;
	String message, sourceUsername, targetUsername;

	// Database format: DATE, FROMACCOUNT, TOACCOUNT, FROMUSER, TOUSER,
	// FROMBALANCE, TOBALANCE, AMOUNT, MESSAGE
	public TransactionHistoryElement(String date, String sourceAccountID, String targetAccountID, String sourceUsername,
			String targetUsername, double sourceBalance, double targetBalance, double transferAmount, String message) {
		this.date = date;
		this.sourceAccountID = sourceAccountID;
		this.targetAccountID = targetAccountID;
		this.sourceUsername = sourceUsername;
		this.targetUsername = targetUsername;
		this.sourceBalance = sourceBalance;
		this.targetBalance = targetBalance;
		this.transferAmount = transferAmount;
		this.message = message;
	}

	public String getSourceAccountID() {
		return sourceAccountID;
	}

	public String getTargetAccountID() {
		return targetAccountID;
	}

	public double getSourceBalance() {
		return sourceBalance;
	}

	public double getTargetBalance() {
		return targetBalance;
	}

	public String getSourceUsername() {
		return sourceUsername;
	}

	public String getTargetUsername() {
		return targetUsername;
	}

	public String getDate() {
		return this.date;
	}

	public double getTransferAmount() {
		return this.transferAmount;
	}

	public String getMessage() {
		return this.message;
	}

}
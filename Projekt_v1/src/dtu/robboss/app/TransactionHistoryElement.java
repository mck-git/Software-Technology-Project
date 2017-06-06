package dtu.robboss.app;

public class TransactionHistoryElement {

	String date;
	
	int from;
	int to;
	double amount;
	String message;
	
	public TransactionHistoryElement(String date, int from, int to, double amount, String message){ 
		this.date = date;
		this.from = from;
		this.to = to;
		this.amount = amount;
		this.message = message;
	}
	
	public String getDate() {
		return this.date;
	}

	public int getFrom() {
		return this.from;
	}

	public int getTo() {
		return this.to;
	}

	public double getAmount() {
		return this.amount;
	}

	public String getMessage() {
		return this.message;
	}
	
}
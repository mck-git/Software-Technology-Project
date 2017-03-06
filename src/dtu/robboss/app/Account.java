package dtu.robboss.app;

public class Account {

	private User user;
	private String id;
	private int balance;
	private int credit;

	public Account(User user, String id){
		this.user = user;
		this.id = id;
		this.balance = 0;
		this.credit = 0;
	}
	
	
	
	
	
	
	public User getUser(){
		return this.user;
	}
	
	public String getID(){
		return this.id;
	}
	
	public int getBalance(){
		return this.balance;
	}
	
	public int getCredit(){
		return this.credit;
	}
	
}

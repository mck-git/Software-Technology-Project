package dtu.robboss.app;
import java.util.ArrayList;

public class Customer extends User {

	private Valuta currency; 
	private ArrayList<Account> accounts;
	private Account mainAccount;

	// TODO new constructor? taking a ResultSet as argument?
	// or just think about which arguments User should have
	public Customer(String name, String username, String password, Valuta currency) {
		super(name, username, password);
		this.currency = currency;
		//Creating account list and adding mainAccount. 
		//This has to be done before any account is created.
		this.accounts = new ArrayList<Account>();
		
	}

	////////////////////////
	// ACCOUNT MANAGEMENT //
	////////////////////////

	public void addAccount(Account newAccount) {
		accounts.add(newAccount);
	}

	public void removeAccount(Account account) {
		accounts.remove(account);
	}

	public Account getMainAccount() {
		return this.mainAccount;
	}

	public void setMainAccount(Account newMain) {
		//needs sanitation?
		if(accounts.contains(newMain))
			this.mainAccount = newMain;
		else
			System.out.println("Customer::setMainAccount (account not found in accounts list)");
	}

	
	/////////////////////////
	// GETTERS AND SETTERS //
	/////////////////////////

	public Valuta getCurrency() {
		return currency;
	}

	public void setCurrency(Valuta currency) {
		this.currency = currency;
	}

	public ArrayList<Account> getAccounts() {
		return accounts;
	}
	
	public Account getAccountByID(String accountID){
		
		for(Account acc: this.accounts){
			if(acc.getAccountID().equals(accountID))
				return acc;
		}
		
		return null;
		
	}
}

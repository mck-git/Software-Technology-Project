package dtu.robboss.app;

import java.util.Calendar;
import java.util.Date;

public class UserMessage {
	// TODO figure out encapsulation of these fields
	String subject, message;
	User sender, recipient;
	Date timeSent;
	
	public UserMessage(String subject, String message, User sender, User recipient){
		this.subject = subject;
		this.message = message;
		this.sender = sender;
		this.recipient = recipient;
		
		//Sets timeSent to current time
		// TODO is this the time we should use?
		Calendar calendar = Calendar.getInstance();
		timeSent = calendar.getTime();
	}
	
}

package dtu.robboss.app;

import java.util.ArrayList;
import java.util.List;

public class Database {
	
	int userCount = 0;
	
	List<User> users = new ArrayList<>();

	public Object userCount() {
		return userCount;
	}

	public boolean contains(User user) {
		return users.contains(user);
	}

	public void add(User user) {
		userCount++;
		users.add(user);
	}

	public void remove(User user) {
		userCount--;
		users.remove(user);
	}
	
	
}

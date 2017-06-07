package dtu.robboss.app;

public class Admin extends User {

	/**
	 * Admin is a type of user without accounts. If an Admin logs in, they are
	 * directed to the adminpage.jsp.
	 * 
	 * @param username
	 *            : unique identifier for this user. Unique across all users
	 *            (customers and admins).
	 * @param fullName
	 *            : full name of admin. Not unique.
	 * @param password
	 *            : password used when logging in.
	 * 
	 *            Sanitization for the parameters occurs in User class.
	 */
	public Admin(String username, String fullName, String password) {
		super(username, fullName, password);
	}

}

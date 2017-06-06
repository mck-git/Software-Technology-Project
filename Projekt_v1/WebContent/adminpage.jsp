<!DOCTYPE HTML><%@page language="java"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="dtu.robboss.app.Account"%>
<%@ page import="dtu.robboss.app.Admin"%>
<%@ page import="java.util.ArrayList"%>

<html>
<head>
<title>adminpage</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style>
#container {
	width: 100%;
	margin: 0 auto;
	background: #fff;
}

#header {
	width: 100%;
	height: 60px;
	border-bottom: 1px solid #333;
	text-align: center;
	margin-bottom: 10px;
}

.inner {
	width: 100%;
	background: rgba(240, 230, 255, 0.9);
	border-radius: 2px;
}

.outer {
	background: rgba(150, 204, 250, 0.8);
	width: 40%;
	padding: 20px;
	margin: 20px;
	max-height: 200px;
	border-radius: 10px;
	overflow: hidden;
}

#news {
	float: left;
}

#messages {
	float: right;
}

#menu {
	float: left;
}

#accountLookUp {
	float: right;
}

#createUser {
	float: left;
}

#accounts {
	float: right;
}

#modifyUser {
	clear: both;
	float: left;
}

#footer {
	clear: both;
	background: yellow;
	height: 60px;
	text-align: center;
}
</style>

<!-- Get current logged in user -->
<%
	Admin userLoggedIn = (Admin) session.getAttribute("USER");
	ArrayList<Account> accountsFound = (ArrayList<Account>) session.getAttribute("ACCOUNTSFOUND");
%>

</head>
<body>
	<!-- CONTAINER -->
	<div id="container">

		<!-- HEADER -->
		<div id="header">
			<h1>
				Welcome
				<%=userLoggedIn.getUsername()%>, you are so smart and amazing!
			</h1>
		</div>

		<!-- CONTENT AREA -->
		<div id="content_area">


			<div id="news" class="outer">
				<h3 align="center" style="margin-top: 0;">News</h3>

				<div class="inner">
					news <br>

				</div>
			</div>


			<div id="messages" class="outer">
				<h3 align="center" style="margin-top: 0;">Messages</h3>

				<div class="inner">
					message <br>

				</div>
			</div>

			<div id="menu" class="outer">
				<h3 align="center" style="margin-top: 0;">Menu</h3>

				<div class="inner">
					<form method="post" action="DS" align="center">
						<input type="hidden" name="subject" value="DeleteUser" /> <input
							type="submit" value="Delete user"
							onclick="return confirm('Do you wish to delete user?')" />
					</form>


					<form method="post" action="DS" align="center">
						<input type="hidden" name="subject" value="LogOutUser" "/> <input
							type="submit" value="Log out user"
							onclick="return confirm('Do you wish to log out?')" />
					</form>

				</div>
			</div>

			<div id="accountLookUp" class="outer">
				<h3 align="center" style="margin-top: 0;">Account look-up</h3>

				<div class="inner"
					style="display: inline-block; text-align: center; width: 100%;">
					<form method="post" action="DS">
						<input type="hidden" name="subject" value="AccountLookUp" />
						Search by: <input type="radio" name="searchBy" value="account" />
						Account <input type="radio" checked="checked" name="searchBy"
							value="user" /> User <br> Search for: <input type="text"
							name="searchToken" /> <br> <input type="submit"
							value="Search" />
					</form>

				</div>
			</div>

			<div id="createUser" class="outer">
				<h3 align="center" style="margin-top: 0;">Create user</h3>
				<div class="inner">
					<form method="post" action="DS" align="center">
						<input type="hidden" name="subject" value="CreateNewUserAdmin" />
						User type: <input type="radio" name="userType" value="customer" />
						Customer <input type="radio" checked="checked" name="userType"
							value="admin" /> Admin <br> Full name: <input type="text"
							name="fullname" /> <br> User name: <input type="text"
							name="username" /> <br> Password: <input type="text"
							name="password" /> <br> CPR: <input type="text" name="cpr" />
						<br> <input type="submit" value="Create User">
					</form>
				</div>

			</div>


			<div id="accounts" class="outer">
				<h3 align="center" style="margin-top: 0;">Accounts</h3>

				<div class="inner">
					<%
						String type;
					%>
					<%
						for (Account account : accountsFound) {
					%>

					AccountID:
					<%=account.getAccountNumber()%>

					- balance:
					<%=account.getBalance()%>

					- credit:
					<%=account.getCredit()%>

					<%
						if (account.getType().equals("MAIN"))
								type = "MAIN";
							else
								type = "NORMAL";
					%>
					- type:
					<%=type%>
					<br>

					<%
						}
					%>

					<%=(accountsFound.size() == 0 ? "No accounts" : "")%>

				</div>
			</div>


			<div id="modifyUser" class="outer">
				<h3 align="center" style="margin-top: 0;">Modify user</h3>
				<div class="inner">
					<form method="post" action="DS" align="center">
						<input type="hidden" name="subject" value="modifyUserAdmin" />
						User name: <input type="text" name="username" /> <br> <input
							type="submit" value="Modify User">
					</form>
				</div>

			</div>

			<!--  
			<div id="deleteUser" class="outer">
				<h3 align="center" style="margin-top: 0;">Delete user</h3>
				<div class="inner">
					<form method="post" action="DS" align="center">
						<input type="hidden" name="subject" value="DeleteUserAdmin" />
						User name: <input type="text" name="username" /> <br> <input
							type="submit" value="Delete User">
					</form>
				</div>

			</div>
-->


		</div>

		<!-- FOOTER -->
		<div id="footer">
			Footer <br>
			<p>Contact informations here
		</div>

		<!-- END OF CONTAINER -->
	</div>

</body>
</html>

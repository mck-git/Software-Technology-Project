<!DOCTYPE HTML><%@page language="java"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="dtu.robboss.app.User"%>
<%@ page import="dtu.robboss.app.Account"%>
<html>
<head>
<title>userpage</title>
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

#accounts {
	float: left;
}

#currency {
	float: right;
}

#messages {
	float: left;
}

#payment {
	float: right;
}

#news {
	clear: left;
	float: left;
}

#menu {
	float: right;
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
	User userLoggedIn = (User) session.getAttribute("USER");
%>

</head>
<body>
	<!-- CONTAINER -->
	<div id="container">

		<!-- HEADER -->
		<div id="header">
			<h1>
				Welcome
				<%=userLoggedIn.getUsername()%>
			</h1>
		</div>

		<!-- CONTENT AREA -->
		<div id="content_area">

			<div id="accounts" class="outer">
				<h3 align="center" style="margin-top: 0;">Accounts</h3>

				<div class="inner">
					<%
						String type;
					%>
					<%
						for (Account account : userLoggedIn.getAccounts()) {
					%>

					AccountID:
					<%=account.getAccountNumber()%>
					
					- balance:
					<%=account.getBalance()%>
					
					- credit:
					<%=account.getCredit()%>
					 
					<%
						if (account.equals(userLoggedIn.getMainAccount()))
								type = "MAIN";
							else
								type = "NORMAL";
					%>
					 - type: <%=type%>
					<br>

					<%
						}
					%>

					<%=(userLoggedIn.getAccounts().size() == 0 ? "No accounts" : "")%>

				</div>
			</div>

			<div id="currency" class="outer">
				<h3 align="center" style="margin-top: 0;">Currency</h3>

				<div class="inner">
					forms <br>

				</div>
			</div>

			<div id="payment" class="outer">
				<h3 align="center" style="margin-top: 0;">Payment</h3>

				<div class="inner">
					<form method="post" action="DS" align="center" />
					<input type="hidden" name="subject" value="transfermoney" /> Send
					to User: <input type="text" name="targetUsername" /> <br> <br>
					<input type="text" name="beforedecimalseperator"
						style="width: 10em" />. <input type="text"
						name="afterdecimalseperator" style="width: 3em" /> Kr. <input
						type="submit" value="Transfer Money"
						onclick="return confirm('Do you wish to transfer?')" />
					</form>

				</div>
			</div>

			<div id="messages" class="outer">
				<h3 align="center" style="margin-top: 0;">Messages</h3>

				<div class="inner">
					message <br>

				</div>
			</div>

			<div id="news" class="outer">
				<h3 align="center" style="margin-top: 0;">News</h3>

				<div class="inner">
					news <br>

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

					<form method="post" action="DS" align="center">
						<input type="hidden" name="subject" value="NewAccount" "/> <input
							type="submit" value="Add new account"
							onclick="return confirm('Do you wish to create new account?')" />
					</form>

				</div>
			</div>

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

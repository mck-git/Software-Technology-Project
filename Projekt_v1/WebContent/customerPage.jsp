<!DOCTYPE HTML><%@page language="java"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="dtu.robboss.app.Customer"%>
<%@ page import="dtu.robboss.app.Account"%>
<%@ page import="dtu.robboss.app.Currency"%>
<%@ page import="dtu.robboss.app.TransactionHistoryElement"%>
<%@ page import="java.util.ArrayList"%>

<html>
<head>
<title>customerpage</title>
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

table, th, td {
	border: 1px solid black;
}

.inner {
	width: 100%;
	background: rgba(240, 230, 255, 0.9);
	border-radius: 2px;
	max-height: 160px;
	overflow: hidden;
}

.innerScrollable {
	width: 100%;
	background: rgba(240, 230, 255, 0.9);
	border-radius: 2px;
	max-height: 160px;
	overflow: scroll;
}

.outer {
	background: rgba(51, 102, 255, 0.8);
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

#editAccounts {
	float: left;
}

#transfer {
	clear: both;
	float: right;
}

#TH {
	clear: left;
	float: left;
}

#menu {
	clear: right;
	float: right;
}

#footer {
	clear: both;
	background: #ccd9ff;
	height: 65px;
	text-align: center;
}
</style>

<!-- Get current logged in user -->
<%
	//Delete cache from browser
	response.setHeader("Cache-Control", "no-cache");
	response.setHeader("Cache-Control", "no-store");
	response.setHeader("Pragma", "no-cache");
	response.setDateHeader("Expires", 0);

	//If USER is null, redirect to login page.
	if (session.getAttribute("USER") == null)
		response.sendRedirect("login.html");
	String infomessage = (String) request.getAttribute("INFOMESSAGE");
	Customer userLoggedIn = (Customer) session.getAttribute("USER");
	ArrayList<TransactionHistoryElement> th = (ArrayList<TransactionHistoryElement>) session
			.getAttribute("TRANSACTIONHISTORY");
%>

</head>
<body>
	<!-- CONTAINER -->
	<div id="container">

		<!-- HEADER -->
		<div id="header">
			<h1>
				Welcome
				<%=userLoggedIn.getFullName()%>
			</h1>
		</div>

		<%
			if (infomessage != null) {
		%>
		<p style="text-align: center; color: red">
			<%=infomessage%>
		</p>
		<%
			}
		%>

		<!-- CONTENT AREA -->
		<div id="content_area">

			<div id="accounts" class="outer" style="width: 50%;">
				<h3 align="center" style="margin-top: 0">Accounts</h3>

				<div class="innerScrollable">

					<table style="width: 100%">
						<tr>
							<th>ID</th>
							<th>Balance</th>
							<th>Credit</th>
							<th>Type</th>
							<th>Interest</th>
						</tr>

						<%
							if (userLoggedIn != null)
								for (Account account : userLoggedIn.getAccounts()) {
						%>
						<tr>
							<td><%=account.getAccountID()%></td>
							<td><%=Currency.convert(account.getBalance(), userLoggedIn)%></td>
							<td><%=Currency.convert(account.getCredit(), userLoggedIn)%></td>
							<td><%=account.getType()%></td>
							<td><%=account.getInterest()%></td>
						</tr>
						<%
							}
						%>
					</table>
					<%=(userLoggedIn.getAccounts().size() == 0 ? "No accounts" : "")%>


				</div>
			</div>


			<div id="currency" class="outer" style="width: 30%;">
				<h3 align="center" style="margin-top: 0;">Currency</h3>

				<div class="inner" style="text-align: center;">


					<form method="post" action="DS">

						Select preferred currency: <br> <select name="currency">

							<option value="DKK">DKK</option>
							<option value="EUR">EUR</option>
							<option value="USD">USD</option>
							<option value="GBP">GBP</option>
							<option value="JPY">JPY</option>

						</select> <input type="submit" name="subject" value="Select currency" />

					</form>

					<br> Current currency:
					<%=userLoggedIn.getCurrency().name()%>
				</div>
			</div>

			<div id="transfer" class="outer"
				style="height: 300px; max-height: 260px;">
				<h3 align="center" style="margin-top: 0;">Transfer</h3>

				<div class="inner"
					style="display: inline-block; text-align: center; width: 100%; height: 220px; max-height: 220px;">
					<form method="post" action="DS">
						<input type="hidden" name="subject" value="transfermoney" />

						Select account to send from: <select name="accountToSendFrom">
							<%
								for (Account account : userLoggedIn.getAccounts()) {

									String accountID = "" + account.getAccountID();
							%>
							<option value=<%=accountID%>
								<%if (account.equals(userLoggedIn.getMainAccount())) {%>
								selected="selected" <%}%>>AccountID:
								<%=accountID%>



							</option>
							<%
								}
							%>
						</select> <br> Send to: <input type="radio" name="receiverType"
							value="account" /> Account <input type="radio" checked="checked"
							name="receiverType" value="user" /> Customer <br> Receiver:
						<input type="text" name="receiver" maxlength="20" /> <br> <br>
						Message:
						<textarea name="message" style="height: 3em; width: 90%;"
							maxlength="140"></textarea>
						<br> <br> Amount <br> <input type="text"
							name="beforedecimalseperator" style="width: 10em" />. <input
							type="text" name="afterdecimalseperator" style="width: 3em" />
						<%=userLoggedIn.getCurrency().name()%>

						<input type="submit" value="Transfer Money"
							onclick="return confirm('Do you wish to transfer?')" /> <br>

					</form>

				</div>
			</div>

			<div id="editAccounts" class="outer" style="text-align: center;">
				<h3 align="center" style="margin-top: 0;">Edit Accounts</h3>

				<div class="inner">
					<form method="post" action="DS">
						Select account to edit: 
						<select name="accountSelected">
							<%
								for (Account account : userLoggedIn.getAccounts()) {
									String accountID = "" + account.getAccountID();
							%>
							<option value=<%=accountID%>>AccountID:
								<%=accountID%>
							</option>
							<%
								}
							%>
						</select> <br> <br> 
						<input type="submit" name = "subject" value="Create new account"
							onclick="return confirm('Do you wish to create new account?')" />

						<br> <br> <input type="submit" name="subject" value="Set as main account" /> <br> <br> 
						<input type="submit" name="subject" value="Delete account" />
					</form>
				</div>


			</div>

			<div id="TH" class="outer">
				<h3 align="center" style="margin-top: 0;">Transaction History</h3>
				<!--FORMAT: DATE[0], FROMACCOUNT[1], TOACCOUNT[2], FROMUSER[3], TOUSER[4], FROMBALANCE[5], TOBALANCE[6], AMOUNT[7], MESSAGE[8]-->
				<div class="innerScrollable">

					<table style="width: 100%">
						<tr>
							<th>Date</th>
							<th>From (user/account)</th>
							<th>To (user/account)</th>
							<th>Amount</th>
							<th>Balance</th>
							<th>Message</th>
						</tr>

						<%
							for (int i = th.size() - 1; i >= 0; i--) {
						%>
						<tr>
							<td><%=th.get(i).getDate()%></td>
							<td><%=th.get(i).getSourceUsername()%>/<%=th.get(i).getSourceAccountID()%></td>
							<td><%=th.get(i).getTargetUsername()%>/<%=th.get(i).getTargetAccountID()%></td>
							<td><%=Currency.convert(th.get(i).getTransferAmount(), userLoggedIn)%></td>

							<%
								if (userLoggedIn.getUsername().equals(th.get(i).getSourceUsername())) {
							%>
							<td><%=Currency.convert(th.get(i).getSourceBalance(), userLoggedIn)%>
							</td>
							<%
								} else {
							%>
							<td><%=Currency.convert(th.get(i).getTargetBalance(), userLoggedIn)%>
							</td>
							<%
								}
									;
							%>

							<td><%=th.get(i).getMessage()%></td>
						</tr>
						<%
							}
						%>

					</table>

				</div>
			</div>

			<div id="menu" class="outer">
				<h3 align="center" style="margin-top: 0;">Menu</h3>

				<div class="inner"
					style="display: inline-block; text-align: center; width: 100%;">
					<form method="post" action="DS">
						<input type="hidden" name="subject" value="DeleteLoggedInUser" />
						<input type="submit" value="Delete user"
							onclick="return confirm('Do you wish to delete user?')" />
					</form>

					<form method="post" action="DS">
						<input type="hidden" name="subject" value="LogOutUser" /> <input
							type="submit" value="Log out user"
							onclick="return confirm('Do you wish to log out?')" />
					</form>

				</div>
			</div>

		</div>

		<!-- FOOTER -->
		<div id="footer">
			<div style="height: 100%; width: 24%; float: left">
				Username:
				<%=userLoggedIn.getUsername()%><br> Full name:
				<%=userLoggedIn.getFullName()%>
			</div>
			<div style="height: 100%; width: 24%; float: left">
				Number of accounts:
				<%=userLoggedIn.getAccounts().size()%>
			</div>
			<div style="height: 100%; width: 24%; float: right">
				<br> s154666@student.dtu.dk <br> s151952@student.dtu.dk
			</div>
			<div style="height: 100%; width: 24%; float: right">
				Contact information: <br> s144107@student.dtu.dk <br>
				s144063@student.dtu.dk
			</div>
		</div>

		<!-- END OF CONTAINER -->
	</div>

</body>
</html>

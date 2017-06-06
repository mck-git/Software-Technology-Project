<!DOCTYPE HTML><%@page language="java"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="dtu.robboss.app.Customer"%>
<%@ page import="dtu.robboss.app.Account"%>
<%@ page import="dtu.robboss.app.Valuta"%>
<%@ page import="java.util.ArrayList"%>

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

table, th, td {
	border: 1px solid black;
}

.inner {
	width: 100%;
	background: rgba(240, 230, 255, 0.9);
	border-radius: 2px;
	max-height: 160px;
	overflow: scroll;
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

#editAccounts {
	float: left;
}

#payment {
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
	background: #dfa;
	height: 65px;
	text-align: center;
}
</style>

<!-- Get current logged in user -->
<%
	Customer userLoggedIn = (Customer) session.getAttribute("USER");
	ArrayList<String[]> th = (ArrayList<String[]>) session.getAttribute("TRANSACTIONHISTORY");
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

					ID:
					<%=account.getAccountNumber()%>

					- balance:
					<%=Valuta.convert(account.getBalance(), userLoggedIn)%>

					- credit:
					<%=Valuta.convert(account.getCredit(), userLoggedIn)%>

					- interest:
					<%=account.getInterest()%>

					<%
						if (account.equals(userLoggedIn.getMainAccount()))
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

					<%=(userLoggedIn.getAccounts().size() == 0 ? "No accounts" : "")%>
				</div>
			</div>

			<div id="currency" class="outer">
				<h3 align="center" style="margin-top: 0;">Currency</h3>

				<div class="inner" style="text-align: center;">


					<form method="post" action="DS">

						Select preferred currency: <select name="currency">

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

			<div id="payment" class="outer"
				style="height: 300px; max-height: 260px;">
				<h3 align="center" style="margin-top: 0;">Payment</h3>

				<div class="inner"
					style="display: inline-block; text-align: center; width: 100%; height: 220px; max-height: 220px;">
					<form method="post" action="DS">
						<input type="hidden" name="subject" value="transfermoney" />

						Select account to send from: <select name="accountToSendFrom">
							<%
								for (Account account : userLoggedIn.getAccounts()) {

									String accountID = "" + account.getAccountNumber();
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
							name="receiverType" value="user" /> User <br> Receiver: <input
							type="text" name="receiver" /> <br> <br> Message:
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

			<div id="editAccounts" class="outer"  style="text-align: center;">
				<h3 align="center" style="margin-top: 0;">Edit Account</h3>


				<form method="post" action="DS">
					Select account to edit: <select name="accountSelected">
						<%
							for (Account account : userLoggedIn.getAccounts()) {
								String accountID = "" + account.getAccountNumber();
						%>
						<option value=<%=accountID%>>AccountID: <%=accountID%>
						</option>
						<%
							}
						%>
					</select> 
					<br><br>
					<input type="submit" name="subject" value="Set as main account" />
					<br><br> 
					<input type="submit" name="subject" value="Delete account" />
				</form>

			</div>

			<div id="TH" class="outer">
				<h3 align="center" style="margin-top: 0;">Transaction History</h3>
					<!--FORMAT: DATE[0], FROMACCOUNT[1], TOACCOUNT[2], FROMUSER[3], TOUSER[4], FROMBALANCE[5], TOBALANCE[6], AMOUNT[7], MESSAGE[8]-->
				<div class="inner">

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
							<td><%=th.get(i)[0]%></td>
							<td><%=th.get(i)[3]%>/<%=th.get(i)[1]%></td>
							<td><%=th.get(i)[4]%>/<%=th.get(i)[2]%></td>
							<td><%=Valuta.convert(Double.parseDouble(th.get(i)[7]), userLoggedIn)%></td>
							
							<% if(userLoggedIn.getUsername().equals(th.get(i)[3])){ %> 
							<td> <%=Valuta.convert(Double.parseDouble(th.get(i)[5]), userLoggedIn)%> </td> 
							<% }else{ %> 
							<td> <%=Valuta.convert(Double.parseDouble(th.get(i)[6]), userLoggedIn)%> </td> <%}; %>
							
							<td><%=th.get(i)[8]%></td>
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
						<input type="hidden" name="subject" value="DeleteUser" /> <input
							type="submit" value="Delete user"
							onclick="return confirm('Do you wish to delete user?')" />
					</form>

					<form method="post" action="DS">
						<input type="hidden" name="subject" value="LogOutUser" /> <input
							type="submit" value="Log out user"
							onclick="return confirm('Do you wish to log out?')" />
					</form>

					<form method="post" action="DS">
						<input type="hidden" name="subject" value="NewAccount" /> <input
							type="submit" value="Add new account"
							onclick="return confirm('Do you wish to create new account?')" />
					</form>

				</div>
			</div>

		</div>

		<!-- FOOTER -->
		<div id="footer">
			<div style="height: 100%; width: 24%; float: left">
				Username: <%=userLoggedIn.getUsername()%><br>
				Full name: <%=userLoggedIn.getFullName()%>
			</div>
			<div style="height: 100%; width: 24%; float: left">
				Amount of accounts: <%=userLoggedIn.getAccounts().size()%>
			</div>
			<div style="height: 100%; width: 24%; float: right">
				<br>
				s154666@student.dtu.dk
				<br>
				s151952@student.dtu.dk
			</div>
			<div style="height: 100%; width: 24%; float: right">
				Contact information:
				<br>
				s144107@student.dtu.dk
				<br>
				s144063@student.dtu.dk
			</div>
		</div>

		<!-- END OF CONTAINER -->
	</div>

</body>
</html>

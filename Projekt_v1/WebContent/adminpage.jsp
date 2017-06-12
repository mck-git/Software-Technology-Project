<!DOCTYPE HTML><%@page language="java"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="dtu.robboss.app.Currency"%>
<%@ page import="dtu.robboss.app.Customer"%>
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
	max-height: 160px;
}

.innerScrollable {
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

table, th, td {
	border: 1px solid black;
}

#search {
	float: left;
}

#batch {
	float: right;
}

#accounts {
	clear: both;
	float: left;
}

#customer {
	float: right;
}

#modifyUser {
	clear: both;
	float: left;
}

#modifyAccount {
	float: right;
}

#createUser {
	clear: both;
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
	Admin userLoggedIn = (Admin) session.getAttribute("USER");
	//ArrayList<Account> accountsFound = (ArrayList<Account>) session.getAttribute("ACCOUNTSFOUND"); 
	Customer customerFound = (Customer) session.getAttribute("CUSTOMERFOUND");
	String infomessage = (String) request.getAttribute("INFOMESSAGE");
%>

</head>
<body>
	<!-- CONTAINER -->
	<div id="container">

		<!-- HEADER -->
		<div id="header">
			<h1>
				Welcome
				<%=userLoggedIn.getFullName() %>, you are so smart and amazing!
			</h1>
		</div>

		<% if(infomessage != null) { %>
		<p style="text-align: center; color: red;"> <%=infomessage %> </p>
		<% } %>


		<!-- CONTENT AREA -->
		<div id="content_area">

			<div id="search" class="outer">
				<h3 align="center" style="margin-top: 0;">Search</h3>

				<div class="inner"
					style="display: inline-block; text-align: center; width: 100%;">
					<form method="post" action="DS">
						Search by: <input type="radio" name="searchBy" value="account" />
						Account <input type="radio" checked="checked" name="searchBy"
							value="user" /> Customer <br> Search for: <input type="text"
							name="searchToken" /> <br> <input type="submit"
							name="subject" value="Search" />
					</form>

				</div>
			</div>

			<div id="batch" class="outer">
				<h3 align="center" style="margin-top: 0;">Batch jobs</h3>

				<div class="inner">
					Batch Jobs:<br> 1 - Apply interest to all accounts <br> 2
					- Move old transactions to archive table <br>

					<form method="post" action="DS" style="text-align: center;">
						<input type="submit" name="subject" value="Apply Interest" />
					</form>

					<form method="post" action="DS" style="text-align: center;">
						<input type="submit" name="subject"
							value="Archive Old Transactions" />
					</form>

					<form method="post" action="DS" style="text-align: center;">
						<input type="submit" name="subject" value="Perform Batch" />
					</form>

				</div>
			</div>


			<div id="accounts" class="outer" style="width: 60%;">
				<h3 align="center" style="margin-top: 0">Accounts</h3>

				<div class="innerScrollable">

					<table style="width: 100%">
						<tr>
							<th>Customer</th>
							<th>ID</th>
							<th>Balance</th>
							<th>Credit</th>
							<th>Type</th>
							<th>Interest</th>
						</tr>

						<%
							if (customerFound != null)
								for (Account account : customerFound.getAccounts()) {
						%>
						<tr>
							<td><%=customerFound.getUsername()%></td>
							<td><%=account.getAccountID()%></td>
							<td><%=Currency.convert(account.getBalance(), customerFound)%></td>
							<td><%=Currency.convert(account.getCredit(), customerFound)%></td>
							<td><%=account.getType()%></td>
							<td><%=account.getInterest()%></td>
						</tr>
						<%
							}
						%>
						<!-- (customerFound.getAccounts().size() == 0 ? "No accounts" : "")%>   -->
					</table>


				</div>
			</div>


			<div id="customer" class="outer" style="width: 23%">
				<h3 align="center" style="margin-top: 0;">Customer</h3>
				<div class="inner">
					<%
						if (customerFound != null) {
					%>
					Username:
					<%=customerFound.getUsername()%>
					<br /> Full Name:
					<%=customerFound.getFullName()%>
					<br /> Password:
					<%=customerFound.getPassword()%>
					<br /> Preferred Currency:
					<%=customerFound.getCurrency()%>
					<%
						}
					%>

				</div>

			</div>



			<div id="modifyUser" class="outer">
				<h3 align="center" style="margin-top: 0;">Modify user</h3>
				<div class="inner">
					<form method="post" action="DS" style="text-align: center;">
						<input type="hidden" name="subject" value="DeleteUserByAdmin" />
						User name: <input type="text" name="username" /> <br> <input
							type="submit" value="Delete User">
					</form>
				</div>

			</div>


			<div id="modifyAccount" class="outer">
				<h3 align="center" style="margin-top: 0;">Modify Account</h3>
				<div class="innerScrollable">
					<form method="post" action="DS" style="text-align: center;">
						Select Account: <select name="accountSelected" >
						<%
							if (customerFound != null)
								for (Account account : customerFound.getAccounts()) {
									String accountID = "" + account.getAccountID();
						%>
						<option value=<%=accountID%>>AccountID:
							<%=accountID%>
						</option>
						<%
							}
						%>
						</select> <br /> 
						
						Modify Interest: 
						<input type="text" name="interest" placeholder="Etc 5% is written 1.05" /> 
						<input type="submit" name="subject" value="Set Interest" /> <br />
						Modify Credit: 
						<input type="text" name="credit" placeholder="Must be above 0" /> DKK 
						<input type="submit" name="subject" value="Set Credit" />
					</form>
				</div>

			</div>


			<div id="createUser" class="outer">
				<h3 align="center" style="margin-top: 0;">Create user</h3>
				<div class="inner">
					<form method="post" action="DS" style="text-align: center;">
						<input type="hidden" name="subject" value="CreateNewUserAdmin" />
						User type: 
						<input type="radio" name="userType" value="customer" /> Customer 
						<input type="radio" checked="checked" name="userType" value="admin" /> Admin <br> 
						Full name: 
						<input type="text" name="fullname" placeholder="Bob Ross" maxlength="100" /> <br> 
						User name: 
						<input type="text" name="username" placeholder="Valid symbols: a-z" maxlength="20" /> <br> 
						Password: 
						<input type="text" name="password" placeholder="Valid symbols: a-z, A-Z" maxlength="20" /> <br> 
						<input type="submit" value="Create User">
					</form>
				</div>

			</div>


			<div id="menu" class="outer">
				<h3 align="center" style="margin-top: 0;">Menu</h3>

				<div class="inner">
					<form method="post" action="DS" style="text-align: center;">
					<input type="hidden" name="subject" value="DeleteLoggedInUser"/>
						<input type="submit"  value="Delete Admin"
							onclick="return confirm('Do you wish to delete admin?')" />
					</form>


					<form method="post" action="DS" style="text-align: center;">
						<input type="hidden" name="subject" value="LogOutUser"/> <input
							type="submit" value="Log out admin"
							onclick="return confirm('Do you wish to log out?')" />
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
			<div style="height: 100%; width: 24%; float: left">
				Username:
				<%=userLoggedIn.getUsername()%><br> Full name:
				<%=userLoggedIn.getFullName()%>
			</div>
			<div style="height: 100%; width: 24%; float: left">
				
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

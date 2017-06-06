<!DOCTYPE HTML><%@page language="java"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="dtu.robboss.app.Valuta"%>
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


#batch {
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
	clear: both;
	float: center;
	overflow: scroll;
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
	//ArrayList<Account> accountsFound = (ArrayList<Account>) session.getAttribute("ACCOUNTSFOUND"); 
	Customer customerFound = (Customer) session.getAttribute("CUSTOMERFOUND");
	
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

			<div id="batch" class="outer">
				<h3 align="center" style="margin-top: 0;">Batch jobs</h3>

				<div class="inner">
					Batch Jobs:<br>
					1 - Apply interest to all accounts <br>
					2 - Move old transactions to archive table <br>

					<form method="post" action = "DS" align ="center">
							<input type="submit" name="subject" value ="Apply Interest" />
					</form>
					
					<form method="post" action = "DS" align ="center">
							<input type="submit" name="subject" value ="Archive Old Transactions" />
					</form>

					<form method="post" action = "DS" align ="center">
							<input type="submit" name="subject" value ="Perform Batch" />
					</form>

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
							type="submit" value="Log out admin"
							onclick="return confirm('Do you wish to log out?')" />
					</form>

				</div>
			</div>

			<div id="search" class="outer">
				<h3 align="center" style="margin-top: 0;">Search</h3>

				<div class="inner"
					style="display: inline-block; text-align: center; width: 100%;">
					<form method="post" action="DS">
						Search by: <input type="radio" name="searchBy" value="account" />
						Account <input type="radio" checked="checked" name="searchBy" value="user" /> User 
							<br> 
							Search for: <input type="text" name="searchToken" /> 
							<br> 
							<input type="submit" name="subject" value="Search" />
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

			<div id="customer" class="outer">
				<h3 align="center" style="margin-top: 0;">Customer</h3>
				<div class="inner">
					<%if (customerFound != null) { %>
						Username: <%=customerFound.getUsername()%>
						<br/>
						Full Name: <%=customerFound.getFullName()%>
						<br/>
						Password: <%=customerFound.getPassword()%>
						<br/>
						Preferred Currency: <%=customerFound.getCurrency()%>
					<% 
					}
					%>						
					
				</div>

			</div>


			<div id="accounts" class="outer", style="width: 80%;">
				<h3 align="center" style="margin-top: 0;">Accounts</h3>

				<div class="inner">
					
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
							<td><%=account.getAccountNumber()%></td>
							<td><%=Valuta.convert(account.getBalance(), customerFound)%></td>
							<td><%=Valuta.convert(account.getCredit(), customerFound)%></td>
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
			
			<div id="modifyAccount" class="outer">
				<h3 align="center" style="margin-top: 0;">Modify Account</h3>
				<div class="inner">
					<form method="post" action="DS" align="center">
						Select Account: <select name="accountSelected"/>
						<%
							if(customerFound != null)
							for (Account account : customerFound.getAccounts()) {
								String accountID = "" + account.getAccountNumber();
						%>
						<option value=<%=accountID%>>AccountID: <%=accountID%> </option>
						<%
							}
						%>
					</select> 
					<br/>
						Modify Interest: <input type="text" name="interest"/>
										<input type="submit" name="subject" value="Set Interest" />
										<br/>
						Modify Credit: <input type="text" name="credit"/>
										<input type="submit" name="subject" value="Set Credit" />
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
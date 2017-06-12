<!DOCTYPE HTML><%@page language="java"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<title>login</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<style>
.box {
	width: 350px;
	height: 320px;
	background-color: rgba(255, 255, 255, 0.7);
	position: absolute;
	top: 0;
	bottom: 0;
	left: 0;
	right: 0;
	margin: auto;
	
	font-family:    Arial, Helvetica, sans-serif;
	font-size:      15px;
	
}

.page {
	text-align: center;
}
</style>

<style>
 .fit { /* set relative picture size */
    width: 100%;
    height: 100%;
  }
  .center {
    display: block;
    margin: auto;
  }
</style>

<%
	String message = (String) request.getAttribute("INFOMESSAGE");
%>

</head>
<body>

<img class="center fit" src="lillepseudo.png" >    


	<div class="page">
		<div class="box">
			<h1>Login</h1>
			
			<% if(message != null){ %>
			<p style="color:red"><%=message%>
			<%} %>
			
			<form method="post" action="DS">
				<input type="hidden" name = "subject" value = "Login"/> 
				<br />User Name: <br /> <input name="username" maxlength="20" /> <br /> 
				<br />Password: <br /> <input type="password" name="password" maxlength="20" /> 
				<br> <input type="submit" value="Login" />
			</form>
			
			<form action ="NewCustomer.jsp">
				<input type="hidden" name = "subject" value = "CreateNewCustomer"/> 
			<br/><input type="submit" value="Create New Customer" />
			
			</form>
			
			<form method = "post" action ="DS">
				<input type="hidden" name = "subject" value = "UserCount"/> 
			<br/><input type="submit" value="Number Of Users" />
			
			</form>

		</div>
	</div>
</body>

</html>


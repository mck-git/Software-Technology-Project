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


.outer {
	background: rgba(150, 204, 250, 0.8);
	width: 60%;
	padding: 20px;
	margin: 20px auto;
	border-radius: 10px;
	overflow: hidden;
	font-size: 150%;
}
input{
margin: 20px;
}
</style>


</head>
<body>
	<!-- CONTAINER -->
	<div id="container">

		<!-- HEADER -->
		<div id="header">
			<h1>
				Create New User
			</h1>
		</div>

		<!-- CONTENT AREA -->
		<div id="content_area">

	
		<div class="outer">
		
		<form method = "post" action ="DS" align="center">
		<input type="hidden" name = "subject" value = "CreateNewUser"/> 
		Full name: 	<input type="text" name ="fullname"/> 	<br>
		User name: 	<input type="text" name ="username"/> 	<br>
		Password: 	<input type="text" name ="password"/> 	<br>
		CPR: 		<input type="text" name ="cpr"/> 		<br>
		
		<input type="submit" value="Create User" >
		</form>
		
		
		</div>
	
	
		</div>

		<!-- END OF CONTAINER -->
	</div>

</body>
</html>

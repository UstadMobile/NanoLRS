<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
	"http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title> Login - Ustad Mobile Cloud </title>
		<%@include  file="base.html" %>
		
		
		<script type="text/javascript">
			
			$(function() {
				$('#usagereport').puibutton();
				$('#mcqreports').puibutton();
				$('#statementsreports').puibutton();
				$('#stmtsdynadb').puibutton();
				$('#mystmtsdynadb').puibutton();
				$('#chartjstest').puibutton();
				$('#durationreport').puibutton();
				$('#lastactivity').puibutton();
				$('#allclassattendance').puibutton();
				$('#regidtincan').puibutton();
				$('#attendanceexcel').puibutton();
				}
			);
			
			
		</script>
	</head>
	
    <body>
	
		<!-- Header -->
    	<%@include  file="../header.html" %>

    	<p></p>
		
		<!--Menu-->
        <%@include file="menu.html"%>
		
    	<div id="page">
			
			<!-- Main Content -->
			<div id="content">		
				
				<h2>Login</h2>
				<h2>Please enter your username and password.</h2>
				
				<!-- Footer -->
				<%@include  file="footer.html" %>
			</div>
		</div>
		
    </body>
</html>


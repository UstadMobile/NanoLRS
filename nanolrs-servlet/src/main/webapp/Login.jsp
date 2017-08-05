<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
	"http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title> Ustad Mobile Cloud Reports </title>
		<%@include  file="base.html" %>
		
		
		<script type="text/javascript">
			
			$(function() {
				$('#home1').puibutton();
				$('#home2').puibutton();
				$('#submit').puibutton();
				$('#username').puiinputtext();
				$('#password').puiinputtext();
				}
			);
			
			
		</script>
	</head>
	
    <body>
	
		<!-- Header -->
    	<%@include  file="header.html" %>

    	<p></p>
		
		<!--Menu-->
        <%@include file="emptymenu.html"%>
		
    	<div id="page">
			
			<!-- Main Content -->
            <div id="content">

                <h2>Welcome, please login:</h2>

                <div id="toolbarbottom" class="toolbar" >
                </div>
                <form method="post" action="login/">
                    <h3>Username:<input id="username" name="username" type="text" name="email" /></h3>
                    <h3>Password:<input id="password" name="password" type="password" name="pass" /><br/><p/>
                    <input id="submit" name="submit" type="submit" value=" Login " />
                </form>
            </div>
    </div>

		
    </body>
</html>


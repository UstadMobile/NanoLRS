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
				}
			);
			
			
		</script>
	</head>
	
    <body>
	
		<!-- Header -->
    	<%@include  file="header.html" %>

    	<p></p>
		
		<!--Menu-->
        <%@include file="menu.html"%>
		
    	<div id="page">
			
			<!-- Main Content -->
			<div id="content">		
				
				<h2>Welcome, here is what you can do:</h2>
				
				<div id="toolbarbottom" class="toolbar" >
					<ul>
						<a href="reports/"><button id="home1" type="button"style="width:200px;height:100px">Reports</button></a>
						<!-- <a href="management/"><button id="home2" type="button"style="width:200px;height:100px">Management</button></a> -->
					</ul>
				</div>
				
				<!-- Footer -->
				<%@include  file="footer.html" %>
			</div>
		</div>
		
    </body>
</html>


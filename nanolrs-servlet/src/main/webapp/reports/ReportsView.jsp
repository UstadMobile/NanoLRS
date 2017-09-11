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
				$('#report1').puibutton();
				$('#report2').puibutton();
				$('#report3').puibutton();
				$('#report4').puibutton();
				$('#report5').puibutton();

				}
			);
			
		</script>
		
		<style>
		.buttonstyle{
			width:200px;
			height:100px;
			background:none;
			background-color:#2196f3;
			color: white;
			box-shadow: none;
			font-weight: normal;
		}
		</style>
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
				
				<h2>Available Reports</h2>
				
				<div id="toolbarbottom" class="toolbar" >
					<ul>
					    <a href="../reports/enrollment/"><button id="report3" type="button" style="width:200px;height:100px" >ENROLLMENT REPORTS</button></a>
                        <a href="../reports/completion/"><button id="report4" type="button" style="width:200px;height:100px" >COMPLETION REPORTS</button></a>

						<!--
						<a href="DurationReport.jsp"><button id="report1" type="button"style="width:200px;height:100px">Duration Report</button></a>
						<a href="ScoreReport.jsp"><button id="report2" type="button"style="width:200px;height:100px">Score Report</button></a>
						<a href="DiversityReport.jsp"><button id="report5" type="button"style="width:200px;height:100px;">Diversity Report</button></a>
						-->

					</ul>
				</div>
				
				<!-- Footer -->
				<%@include  file="footer.html" %>
			</div>
		</div>
		
    </body>
</html>


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
			
			//UI
			$(function() {
				$('#thisbutton').puibutton();
				$('#default').puitabview();
				$('#ajaxSpinnerImage').puipanel();
				$('#report_submit').puibutton();
				//$('#university').puidropdown();
			});

			function hideAjaxSpinnerImage(){
				$('#ajaxSpinnerImage').hide();
			}

			//On load - load data
			window.onload = function(){
				console.log("On load..");
				$('#ajaxSpinnerImage').show();
				setTimeout(function () {
					console.log("Auto click..");
					$('#report_submit').click();
				}, 500);
			  }

			$(document).on("submit", "#report_form", function(event) {
				console.log("On submit..");
				var $form = $(this);
				event.preventDefault(); // Important! Prevents submitting the form.
			});

			//Show result table:
			function showTable(jsonData) {
				console.log("Show Table..");
				$('#{{report_result}}').dynatable({
				features: {
					paginate: true,
					search: true,
					recordCount: true,
					sorting: true,
					},
				dataset: {records : jsonData}
				})
			}

			//Dynatable change color
            function changeColor() {
                $('#report_result tr td').each(function() {
                    if ($(this).text() == 'false') {
                        //$(this).closest('td').css('background-color', '#f45f42');
                        $(this).closest('td').css('background-image', 'url(cross.svg)');
                        $(this).closest('td').css('background-repeat', 'no-repeat');
						$(this).closest('td').css('background-position', 'center');
                        $(this).closest('td').css('font-size', '0');

                    }
                    if ($(this).text() == 'true'){
                        //$(this).closest('td').css('background-color', '#bbff00');
                        $(this).closest('td').css('background-image', 'url(tick.svg)');
                        $(this).closest('td').css('background-repeat', 'no-repeat');
						$(this).closest('td').css('background-position', 'center');
                        $(this).closest('td').css('font-size', '0');

                    }
                });
            }


			var success;

			//ready (jQuery) called before. In Between HTML documents loaded and bbefore all content(images) have been loaded.
			$(document).ready(function(){
				console.log("On Ready..");
				console.log("On Ready..");
					var table = $('#report_result');
					table.bind('dynatable:afterUpdate', function(e, dynatable){
						console.log("Fired") ;
						changeColor();
					});
				$('#university').multiselect();
				var return_json;

                var university_names = [];
                var university_names = $('#university').val();
                var universitynames = [];
                if(!university_names){
                        university_names=[];
                }

                for (var i = 0; i < university_names.length; i++) {
                        universitynames.push(university_names[i]);
                }


				$('#report_form').on('submit', function(e) {
                    var university_names = [];
                    var university_names = $('#university').val();
                    var universitynames = [];
                    if(!university_names){
                            university_names=[];
                    }

                    for (var i = 0; i < university_names.length; i++) {
                            universitynames.push(university_names[i]);
                    }
					university_names=universitynames;
					console.log("Getting DATA from API..");
					$('#ajaxSpinnerImage').show();
					$.ajax( {
								type: 'post',
								dataType: 'json',
								data: {
									university_names : universitynames
								},
								url:  'completion/',
								complete: function(response){
									if (success == true){
										console.log("OK.");
										//Populate the table here..
										//console.log(response);
										hideAjaxSpinnerImage();

										//Populate table:
										console.log("Show Table..");

										$('#report_result').dynatable({
											features: {
												paginate: true,
												search: true,
												recordCount: true,
												sorting: true,
											},
											dataset: {
												records: return_json
											}
										});
										$('#report_result').data('dynatable').settings.dataset.records = return_json;
										$('#report_result').data('dynatable').dom.update();

										changeColor();

									}
								},
								success: function(response){
									//any calculations ?
									success = true;
									//return_json = response.data;
									return_json = response;
								}
						}); //end of ajax
				}); //end of form submit event
			});//end of jQuery ready

			//If you want to disbale anything, put it here.
			function disableall(){
				//$('input[type=radio]').prop('checked',false);
				//$('input[type=radio]').parents('tr').css('background-color','');
			}


			$('#report_result').change( function(){
				changeColor();
			} );


		</script>

		<!-- Overriding dynatables' row css -->
		<style>
			th a{
				color: black;
				padding: 10px;
				background: none;
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
				<!--Loading spinner-->
				<div id="ajaxSpinnerContainer" class="spinner">
					<div id="ajaxSpinnerImage" title="Crunching report..">
						<img src="../media/spinner.gif" title="Working..">
					</div>
				</div>

				<!--POST request FORM-->
				<form id="report_form" name="report_form" action="completion/" method="POST">

					<center>
						<h2>Completion Reports</h2>
					</center>

					<div style="" id="selection" name="selection">
					 <div style="text-align: center;padding-top:10px;padding-bottom:0px;">

						<select multiple="multiple" name="university" id="university" required>
							<option selected value="ALL" required>All universities</option>
							<option value="Kabul University">Kabul University</option>
							<option value="Kabul Polytechnic University">Kabul Polytechnic University</option>
							<option value="Kabul Education University">Kabul Education University</option>
						</select>

						<button id="report_submit" type="submit"
							name="report_submit" value="submit-value" style="padding: 4.6px 12px;">Filter</button>
						<p></p>

					 </div> <!--Alignment div-->
					</div><!--Selection div-->
				 </form><!--Submit Form-->

				<!-- Report Table goes here -->
				<table id="report_result">
				  <thead>
					<th data-dynatable-column="fullname" >Name</th>
					<th data-dynatable-column="username">Username</th>
					<th data-dynatable-column="university" style="display:none;">Uni id</th>
					<th data-dynatable-column="university_name">University</th>
					<th data-dynatable-column="m1" style="width:15%;" >CV Writing</th>
					<th data-dynatable-column="m2" style="width:15%;" >Cover Letter Writing</th>
					<th data-dynatable-column="m3" style="width:15%;" >Job Search Skills</th>
					<th data-dynatable-column="m4" style="width:15%;" >Job Interview Skills</th>

				  </thead>
				  <tbody></tbody>
				</table>

				<p></p>
				<div id="map_wrapper">
					<div id="map_canvas" class="mapping"></div>
				</div>
				<br></br>

				<!-- Footer -->
				<%@include  file="footer.html" %>
			</div> <!--Content div-->
		</div> <!--Page div-->
		
		
		
    </body>
</html>


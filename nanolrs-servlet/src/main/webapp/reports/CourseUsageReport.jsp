<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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
				$('#export_report_button').puibutton();
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
				$('#report_result').dynatable({
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
                        $(this).closest('td').css('background-image', 'url(../cross.svg)');
                        $(this).closest('td').css('background-repeat', 'no-repeat');
						$(this).closest('td').css('background-position', 'center');
                        $(this).closest('td').css('font-size', '0');

                    }
                    if ($(this).text() == 'true'){
                        //$(this).closest('td').css('background-color', '#bbff00');
                        $(this).closest('td').css('background-image', 'url(../tick.svg)');
                        $(this).closest('td').css('background-repeat', 'no-repeat');
						$(this).closest('td').css('background-position', 'center');
                        $(this).closest('td').css('font-size', '0');

                    }
					//Replace undefined with blank
					if ($(this).text() == 'undefined'){
                        //$(this).closest('td').css('background-color', '#bbff00');
                        $(this).text("-");
                    }

                    //Hide usernames
                    if($(this).text().indexOf("blankspace-UM:")>=0){
                        $(this).closest('td').css('font-size', '0');
                    }
                });


            }


			var success;
			var return_json;

			//Not used anymore.
			function exportReport() {
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


                    var universities_filter = [];
                    var universities_filter = $('#universities_filter').val();
                    var universities_filter_names = [];
                    if(!universities_filter){
                        universities_filter=[];
                    }
                    for(var i=0;i<universities_filter.length; i++){
                        universities_filter_names.push(universities_filter[i]);
                    }
                    universities_filter=universities_filter_names;

                    console.log("Getting Repot Exported..");
					console.log(return_json);
                    $('#ajaxSpinnerImage').show();
                    $.ajax( {
						type: 'post',
						dataType: 'json',
						data: {return_json: JSON.stringify(return_json)},
						url:  '../export/',
						complete: function(response){
							if (success == true){
								hideAjaxSpinnerImage();

								//Populate table:
								console.log("Success true for export..");
							}
						},
						success: function(response){
							//any calculations ?
							success = true;
						}
                        }); //end of ajax
                } //end of onclick submit event

			//ready (jQuery) called before. In Between HTML documents loaded and bbefore all content(images) have been loaded.
			$(document).ready(function(){
				console.log("On Ready..");

				var table = $('#report_result');
				table.bind('dynatable:afterUpdate', function(e, dynatable){
					console.log("Fired") ;
					changeColor();
				});

				//University Filter:
				$('#university').multiselect();
				$('#universities_filter').multiselect();
				//var return_json;

                var university_names = [];
                var university_names = $('#university').val();
                var universitynames = [];
                if(!university_names){
                        university_names=[];
                }
                for (var i = 0; i < university_names.length; i++) {
                        universitynames.push(university_names[i]);
                }

                var universities_filter_names = [];
                var universities_filter = $('#universities_filter').val();
                for(var i=0;i<universities_filter.length; i++){
                    universities_filter_names.push(universities_filter[i]);
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


					var universities_filter = [];
                    var universities_filter = $('#universities_filter').val();
                    var universities_filter_names = [];
                    if(!universities_filter){
                        universities_filter=[];
                    }
                    for(var i=0;i<universities_filter.length; i++){
                        universities_filter_names.push(universities_filter[i]);
                    }
                    universities_filter=universities_filter_names;

					console.log("Getting DATA from API..");
					$('#ajaxSpinnerImage').show();
					$.ajax( {
                        type: 'post',
                        dataType: 'json',
                        data: {
                            university_names : universitynames,
                            universities_filter_names : universities_filter_names
                        },
                        url:  '',
                        complete: function(response){
                            if (success == true){
                                //Populate the table here..
                                hideAjaxSpinnerImage();

                                //Populate table:
                                console.log("Showing Table..");

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

				$('#export_report_form').submit(function() {
                    document.getElementById("return_json").value = JSON.stringify(return_json);
                    document.getElementById("table_headers_html").value = "${table_headers_html}";
					console.log("Changed value pre submit");
                    return true; // return false to cancel form action
                });

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
						<img src="/syncendpoint/media/spinner.gif" title="Working..">
					</div>
				</div>

				<!--POST request FORM-->
				<form id="report_form" name="report_form" action="usagescore/" method="POST">

					<center>
						<h2>Usage & Score Reports</h2>
					</center>

					<div style="" id="selection" name="selection">
					 <div style="text-align: center;padding-top:10px;padding-bottom:0px;">

                        <!-- <input type="checkbox" id="legacy_mode" name="legacy_mode" value="TRUE">Legacy Mode</> -->

						<select multiple="multiple" name="universities_filter" id="universities_filter" required>
						    <option selected value="ALL" required>All universities</option>
						    <c:forEach items="${universities}" var="uniValue">
                                <option value="${uniValue}"> ${uniValue} </option>
                            </c:forEach>
						</select>

						<button id="report_submit" type="submit"
							name="report_submit" value="submit-value" style="padding: 4.6px 12px;">Filter</button>
						<p></p>

					 </div> <!--Alignment div-->
					</div><!--Selection div-->
				 </form><!--Submit Form-->

				 <!-- Export Report data to csv/xls -->
                <form id="export_report_form" name="export_report_form" method="post" action="../export/">
                      <input type="hidden" name="return_json" id="return_json" value="Moo">
                      <input type="hidden" name="table_headers_html" id="table_headers_html" value="Bah">
                      <button id="export_report_button"
                          name="export_report_button" style="padding: 4.6px 12px; float:right;">
                          Export
                      </button>
                 </form>
				 <br></br>
                <!-- Report Table goes here -->
                <!-- style="width:15%;" -->
				<div class="table-outer">
					<div class="table-inner">
						<table id="report_result">
						  <thead>
							<c:forEach items="${table_headers_html}" var="column_map">
									<th data-dynatable-column="${column_map.key}" style="width:15%;">${column_map.value}</th>
							</c:forEach>
						  </thead>
						  <tbody></tbody>
						</table>
					</div>
				</div>
				<p></p>

				<p></p>
				<br></br>
				<br></br>

				<!-- Footer -->
				<%@include  file="footer.html" %>
			</div> <!--Content div-->
		</div> <!--Page div-->



    </body>
</html>

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
        <style>
            td, th{
              width:auto;
            }
        </style>

		<script type="text/javascript">

			//UI
			$(function() {
				$('#thisbutton').puibutton();
				$('#default').puitabview();
				$('#ajaxSpinnerImage').puipanel();
				$('#report_submit').puibutton();
				$('#export_report_button').puibutton();
				$('#since_1').puiinputtext();
                $('#until_1').puiinputtext();


                $('#since_1_disabled').datetimepicker({
                        altField: "#since_1_alt",
                        altFieldTimeOnly: false,
                        altFormat: "yy-mm-dd",
                        altTimeFormat: "h:m",
                        altSeparator: "T",
                        setDate: "7"
                });
                $('#until_1_disabled').datetimepicker({
                        altField: "#until_1_alt",
                        altFieldTimeOnly: false,
                        altFormat: "yy-mm-dd",
                        altTimeFormat: "h:m",
                        altSeparator: "T",
                        maxDate: "2",
                        setDate: "1"

                });

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
					//$('#report_submit').click();
					hideAjaxSpinnerImage();
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
					paginate: false,
					search: true,
					recordCount: true,
					sorting: false,
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
                        $(this).text("");
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
                    //Commenting for now
                    //var universities_filter = $('#universities_filter').val();
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

                //University Filter:
                $('#university').multiselect();
                //$('#universities_filter').multiselect();
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
                var universities_filter = [];
                //Commenting for now
                //var universities_filter = $('#universities_filter').val();
                for(var i=0;i<universities_filter.length; i++){
                    universities_filter_names.push(universities_filter[i]);
                }



				var table = $('#report_result');
				table.bind('dynatable:afterUpdate', function(e, dynatable){
					console.log("Fired") ;
					changeColor();
				});


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

					var days = $('#days').val();

                    var since_1 = $('#since_1').val();
                    var until_1 = $('#until_1').val();

					var universities_filter = [];
					//Commenting for now
                    //var universities_filter = $('#universities_filter').val();
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
					var username = '${param.username}';
					console.log("Username is: " + username);
					$.ajax( {
                        type: 'post',
                        dataType: 'json',
                        data: {
                            university_names : universitynames,
                            universities_filter_names : universities_filter_names,
							username: username,
							days: days,
							from: since_1,
							to: until_1
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
                                        paginate: false,
                                        search: true,
                                        recordCount: true,
                                        sorting: false,
                                    },
                                    dataset: {
                                        records: return_json
                                    }
                                });
                                $('#report_result').data('dynatable').settings.dataset.records = return_json;
                                $('#report_result').data('dynatable').dom.update();

                                changeColor();

                            }else{
                                hideAjaxSpinnerImage();
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


		<!-- Overriding dynatables row css -->
		<style>
			th a{
				color: white;
				padding: 10px;
				background: none;
			}
		</style>
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

				<!--Loading spinner-->
				<div id="ajaxSpinnerContainer" class="spinner">
					<div id="ajaxSpinnerImage" title="Crunching report..">
						<img src="/syncendpoint/media/spinner.gif" title="Working..">
					</div>
				</div>

				<center>
                    <h2>Summary Report</h2>
                </center>

                <!--POST request FORM-->
                <form id="report_form" name="report_form" action="completion/" method="POST">
                    <div style="text-align: center;padding-top:10px;padding-bottom:0px;" id="selection" name="selection">

                        <!-- Start and End dates -->
                        <div style="padding-bottom:10px;">
                            <!--Since:-->
                            <label>Start date</label>
                            <input type="date" name="since_1" id="since_1" style="height: 30px; width: 250px;" placeholder="Since" value="" required/>*
                        </div>
                        <div style="display: none;">
                            <input type="text" name="since_1_alt" id="since_1_alt" value="" style="cursor: pointer;">
                        </div>
                        <div style="padding-bottom:10px;">
                            <label>End date</label>
                            <!--Until:&nbsp;&nbsp;-->
                            <input type="date" name="until_1" id="until_1" style="height: 30px; width: 250px;" placeholder="Until"  value="" required/>*
                        </div>

                        <!--
                        <i id="daystext" name="daystext"> From today through </i>
                        -->
                        <input type="hidden" id="days" name="days" for="report_form" value="${days}"> </input>
                        <p></p>
                        <button id="report_submit" type="submit"
                            name="report_submit" value="submit-value" style="padding: 4.6px 12px;">Filter</button>
                        <p></p>
                    </div>
                 </form>


                <!-- Report Table goes here -->
				<table id="report_result">
                  <thead>
                    <c:forEach items="${table_headers_html}" var="column_map">
                            <th data-dynatable-column="${column_map.key}" >${column_map.value}</th>
                    </c:forEach>
                  </thead>
                  <tbody></tbody>
                </table>
				<p></p>

				<p></p><br></br><br></br>

				<!-- Footer -->
				<%@include  file="footer.html" %>

			</div> <!--Content div-->
		</div> <!--Page div-->



    </body>
</html>

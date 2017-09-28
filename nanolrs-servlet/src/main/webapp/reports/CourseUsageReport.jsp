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
					table: {
						headRowSelector: 'thead tr.actual-header-columns'
					},
					features: {
						paginate: true,
						search: true,
						recordCount: true,
						sorting: false,
						},
					dataset: {records : jsonData}
				});

				$('#report_result2').dynatable({
				features: {
					paginate: false,
					search: false,
					recordCount: false,
					sorting: false,
					},
				dataset: {records : jsonData}
				});
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

				$('#report_result th').each(function() {
					if($(this).hasClass("question")){
						$(this).attr("colspan", "3");
					}
                });

                //stripFirstColumn();
                //stripLastColumn();
                //fixHeights();
                //$("#movingTableDiv").attr('scrollLeft', $("#movingTableDiv").attr('scrollWidth'));
				$('#movingTableDiv').css('width','100%');

				showModules();

            }

			function handleHideQuestions(cb){
				if(cb.checked == true){
					hideQuestions();
				}else{
					//changeColor();
					//$('#report_submit').click();
					showQuestions();
				}
			}

			//Show Questions (all actually)
			function showQuestions(){


				$('#bottom th').each(function(){
					if($(this).attr("data-dynatable-column").startsWith("epub:")){

						$(this).closest('table').find('td').eq($(this).index()).show();
						$(this).show();
					}
					if($(this).attr("data-dynatable-column").startsWith("m")){

						$(this).closest('table').find('td').eq($(this).index()).show();
						$(this).show();
					}

				});

				$('#top th').each(function(){
					if($(this).attr("data-dynatable-column").startsWith("epub:")){
						$(this).show();
					}
					if($(this).attr("data-dynatable-column").startsWith("m")){
						$(this).show();
					}
				});

				$('#top').removeClass("actual-header-columns");
				$('#bottom').addClass("actual-header-columns");

			}



			//Hide Questions
			function hideQuestions(){
				$('#bottom th').each(function(){
					if($(this).attr("data-dynatable-column").startsWith("epub:")){

						$(this).closest('table').find('td').eq($(this).index()).hide();
						$(this).hide();
					}
				});

				$('#top th').each(function(){
					if($(this).attr("data-dynatable-column").startsWith("epub:")){
						$(this).hide();
					}
				});

				$('#top').addClass("actual-header-columns");
				$('#bottom').removeClass("actual-header-columns");
			}

			var all_modules = [
				<c:forEach items="${modules}" var="module" varStatus="status">
					{shortID: '${module.getShortID()}',
					name: '${module.getName()}',
					id: '${module.getIds()[0]}',
					}
					<c:if test="${!status.last}">
					  ,
					</c:if>
				</c:forEach>
				];

			//Show Modules
			function showModules(){
				var modules_filter = $('#modules_filter').val();
				var notSelected = $('#modules_filter option').not(':selected');
				var hide_these = notSelected.map(function () {
					return this.value;
				}).get();

				//First show all
				showQuestions();

				if(modules_filter.indexOf("ALL") > -1){
					console.log("Showing all modules.");
				}else{
					for(var i=0; i < hide_these.length; i++){
						var module = hide_these[i];
						hideModule(module);
					}
				}

				$('#top').addClass("actual-header-columns");
				$('#bottom').removeClass("actual-header-columns");
			}

			//Hide Modules
			function hideModule(module){

				for(var key in all_modules){

					if(all_modules[key].shortID == module){
						var shortID = all_modules[key].shortID;
						var name = all_modules[key].name;
						var id = all_modules[key].id;

						$('#bottom th').each(function(){
							if($(this).attr("data-dynatable-column").startsWith(id)){

								$(this).closest('table').find('td').eq($(this).index()).hide();
								$(this).hide();
							}

							if($(this).attr("data-dynatable-column").startsWith(shortID + "_")){

								$(this).closest('table').find('td').eq($(this).index()).hide();
								$(this).hide();
							}

						});

						$('#top th').each(function(){
							if($(this).attr("data-dynatable-column").startsWith(id)){
								$(this).hide();
							}

							if($(this).attr("data-dynatable-column").startsWith(shortID + "_")){
								$(this).hide();
							}
						});

					}
				}

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
				//$('#modules_filter').multiselect();

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

				var modules_filter_names = [];
                var modules_filter = $('#modules_filter').val();
                for(var i=0;i<modules_filter.length; i++){
                    modules_filter_names.push(modules_filter[i]);
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

					var modules_filter = [];
                    var modules_filter = $('#modules_filter').val();
                    var modules_filter_names = [];
                    if(!modules_filter){
                        modules_filter=[];
                    }
                    for(var i=0;i<modules_filter.length; i++){
                        modules_filter_names.push(modules_filter[i]);
                    }
                    modules_filter=modules_filter_names;

					console.log("Getting DATA from API..");
					$('#ajaxSpinnerImage').show();
					$.ajax( {
                        type: 'post',
                        dataType: 'json',
                        data: {
                            university_names : universitynames,
                            universities_filter_names : universities_filter_names,
							modules_filter_names : modules_filter_names
                        },
                        url:  '',
                        complete: function(response){
                            if (success == true){
                                //Populate the table here..
                                hideAjaxSpinnerImage();

                                //Populate table:
                                console.log("Showing Table..");

                                $('#report_result').dynatable({
									table: {
										headRowSelector: 'thead tr.actual-header-columns'
									},
                                    features: {
                                        paginate: true,
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

			//Get css prop from an style class
            function getCSS(prop, fromClass) {

                var $inspector = $("<div>").css('display', 'none').addClass(fromClass);
                $("body").append($inspector); // add to DOM, in order to read the CSS property
                try {
                    return $inspector.css(prop);
                } finally {
                    $inspector.remove(); // and remove from DOM
                }
            };

            function stripFirstColumn() {
                console.log("sup");
                $('#nameTable').remove();
                // pull out first column:

                //get background color
                var backgroundcolor = getCSS("background-color", "dynatable-head");
                console.log("bc: " + backgroundcolor);

                var nt = $('<table id="nameTable" style="margin-top:36px;"></table>');
                nt.css("background-color", backgroundcolor);
                $('#report_result tr').each(function(i)
                {
                    nt.append('<tr ><td >'+$(this).children('td:first').html()+'</td></tr>');
                });
                nt.appendTo('#stickyTableDiv');


                // remove original first column
                $('#report_result tr').each(function(i)
                {
                    $(this).children('td:first').remove();
                });

            }


            function fixHeights() {
                // change heights:
                var curRow = 1;
                $('#report_result tr').each(function(i){
                    // get heights
                    var c1 = $('#nameTable tr:nth-child('+curRow+')').height();    // column 1
                    var c2 = $(this).height();    // column 2
                    var c3 = $('#totalTable tr:nth-child('+curRow+')').height();    // column 3
                    var maxHeight = Math.max(c1, Math.max(c2, c3));


                    // set heights
                    //$('#nameTable tr:nth-child('+curRow+')').height(maxHeight);
                    $('#nameTable tr:nth-child('+curRow+') td:first').height(maxHeight);
                    //$('#log').append('NameTable: '+$('#nameTable tr:nth-child('+curRow+')').height()+'<br/>');
                    //$(this).height(maxHeight);
                    $(this).children('td:first').height(maxHeight);
                    //$('#log').append('MainTable: '+$(this).height()+'<br/>');
                    //$('#totalTable tr:nth-child('+curRow+')').height(maxHeight);
                    $('#totalTable tr:nth-child('+curRow+') td:first').height(maxHeight);
                    //$('#log').append('TotalTable: '+$('#totalTable tr:nth-child('+curRow+')').height()+'<br/>');

                    curRow++;
                });
            }

            function stripLastColumn() {
                // pull out last column:
                var nt = $('<table id="totalTable" cellpadding="3" cellspacing="0" style="width:70px;"></table>');
                $('#report_result tr').each(function(i)
                {
                    nt.append('<tr><td style="color:'+$(this).children('td:last').css('color')+'">'+$(this).children('td:last').html()+'</td></tr>');
                });
                nt.appendTo('#totalTableSpan');
                // remove original last column
                $('#report_result tr').each(function(i)
                {
                    $(this).children('td:last').remove();
                });
                $('#totalTable td:first').css('background-color','#8DB4B7');
            }

		</script>


		<!-- Overriding dynatables' row css -->
		<style>
			th a{
				color: white;
				padding: 10px;
				background: none;
			}

			topheader a{
				color: white;
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


						<select style="display: none" multiple="multiple" name="modules_filter" id="modules_filter" required>
						    <option selected value="ALL" required>All Modules</option>
						    <c:forEach items="${modules}" var="modValue">
                                <option value="${modValue.getShortID()}"> ${modValue.getName()} </option>
                            </c:forEach>
						</select>


						<button id="report_submit" type="submit"
							name="report_submit" value="submit-value" style="padding: 4.6px 12px;">Filter</button>
						<p></p>
						<label><input type="checkbox" onclick='handleHideQuestions(this);' id="modules_only_mode"></input>Hide Questions</label>
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
				<div id="stickyTableDiv" style="float:left;width:10%"></div>
                <div id="movingTableDiv" style="float:left;width:90%;overflow:auto;">

                    <table id="report_result" class="report_result">
                      <thead>
						<tr id="top">
                        <c:forEach items="${table_headers_html}" var="column_map">
                                <th class="${table_headers_class[column_map.key]} ${table_headers_class2[column_map.key]}" data-dynatable-column="${column_map.key}" >${column_map.value}</th>
                        </c:forEach>
						</tr>
						<tr id="bottom" class="actual-header-columns">
							<c:forEach items="${table_headers_html2}" var="column_map2">
								<th class="${table_headers_class[column_map.key]} ${table_headers_class2[column_map.key]}" data-dynatable-column="${column_map2.key}" >${column_map2.value}</th>
							</c:forEach>
						</tr>
						</thead>
                      <tbody></tbody>
                    </table>
                </div>
                <div id="totalTableSpan" style="float:left;width:70px;border-left:2px solid gray;"></div>
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

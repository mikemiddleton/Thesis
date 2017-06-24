<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!--
<%@ page import="helpers.SmartList" %>
<%@ page import="helpers.Wisard" %>
-->

<html>
<head>
  <meta charset="utf-8">
  <title>SegaWeb Portal (Beta)</title>
  <link href="/segaWeb/css/sega.default.css" rel="stylesheet" type="text/css" />
  <link rel="stylesheet" href="/segaWeb/css/sega.jquery/jquery-ui-1.10.4.custom.css">
  <style>
  select{
    min-width:250px;
    -ms-box-sizing:content-box;
    -moz-box-sizing:content-box;
    -webkit-box-sizing:content-box; 
    box-sizing:content-box;
  }
  </style>


  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/sega.default.css"/>
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/sega.datarequest.css"/>
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/sega.jquery/jquery-ui-1.10.4.custom.css">
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/sega.jquery/DataTables/jquery.dataTables.css">
  <script src="${pageContext.request.contextPath}/media/js/jquery/jquery-1.11.0.min.js"></script>
  <script src="${pageContext.request.contextPath}/media/js/jquery/jquery-ui-1.10.4.custom.min.js"></script>
  <script src="${pageContext.request.contextPath}/media/js/datatables/jquery.dataTables.js"></script>
  <script src="${pageContext.request.contextPath}/media/js/datatables/TableTools.js"></script>
 
  <style>
  select{
    min-width:250px;
    -ms-box-sizing:content-box;
    -moz-box-sizing:content-box;
    -webkit-box-sizing:content-box; 
    box-sizing:content-box;
  }
  </style>

  <!--
  <script src="/segaWeb/media/js/jquery/jquery-1.11.0.min.js"></script>
  <script src="/segaWeb/media/js/jquery/jquery-ui-1.10.4.custom.min.js"></script>
  -->

  <script>
  $(function(){
    $( "#tabs" ).tabs();
  });
  </script>

  <script>
  function showDiv(img){
    document.getElementById("imageViewer").innerHTML="<img src='/segaWeb/images/graphics/"+img+"' class='imageImage'/> ";
    $("#imageViewer").show();
    
  }
  function hideDiv(id){
    document.getElementById("imageViewer").innerHTML="";
    $("#imageViewer").hide();
  }
  </script>


</head>
<body>

<div id="info_dialog" title="General Error"></div>

<!-- Header bottons to jump between tabs-->

<jsp:include page="/includes/header.jsp" /> 
  <div class="progress_content">
    <div class="progress_bar_wrapper" style="z-index:0;">
      <div class="progress_bar_unconfigured" id="source_tab_bar"></div>
      <div class="progress_bar_unconfigured" id="output_tab_bar"></div>
      <div class="progress_bar_unconfigured" id="request_tab_bar"></div>
    </div>
    <div id="progress_wrapper_labels" class="progress_wrapper">
      <div id="server_tab_label"  class="progress_label" style="cursor:pointer;"
            onclick="openTab(0);">Select</div>
      <div id="source_tab_label"  class="progress_label" style="background-image: url(${pageContext.request.contextPath}/images/icons/progress_button_unconfigured.svg);cursor:pointer;"
            onclick="openTab(1);">View</div>
      <div id="output_tab_label" class="progress_label" style="background-image: url(${pageContext.request.contextPath}/images/icons/progress_button_unconfigured.svg);cursor:pointer;"
            onclick="openTab(2);">Command</div>
      <div id="request_tab_label" class="progress_label" style="background-image: url(${pageContext.request.contextPath}/images/icons/progress_button_unconfigured.svg);cursor:pointer;"
            onclick="openTab(3);">Configure</div>
    </div>
  </div>

	<div class="form_content_wrapper">
  <div id="visualization_body" class="ui-accordion form_content">
    <div id="accordion">
      <div id="server_tab" class="accordion_header">Select</div>
      <div id="server_tab_content">
        <div class="post_header" style="color:#666666;"> Select WiSARD Info </div>

          <form id="init_network_config_form" name="init_network_config_form" action="/segaWeb/NetworkManagementServlet" method="post">
            <input type="hidden" name="init" value="true"/>
            <input type="hidden" name="redirect" value="${pageContext.request.contextPath}/index.jsp"/>
          </form>

        <!-- Site Selection -->
          <table style="clear:both">        
            <form id="getGardenSites" name="getGardenSites" action="/segaWeb/NetworkManagementServlet" method="post">

              <tr>
                <td align="right"><span class="data_select_label">Site Select:</span></td>
                <td align="right" valign="middle"><input type="hidden" name="current_tab" value="source_tab" />
                <div class="data_select">  
                <select id="site_selection" class="sega_form_item" name="data_site_selection">
                  <option value="" disabled="true" selected="true">Select a site...</option>
                    <c:forEach var="site" items="${sites}">
                      <c:choose>
                        <c:when test="${!empty selectedGardenSite and site.key eq selectedGardenSite}">
                          <option value="${site.key}" selected="true">${site.value}</option>
                        </c:when>
                        <c:otherwise>
                          <option value="${site.key}">${site.value}</option>
                        </c:otherwise>
                      </c:choose>
                    </c:forEach>
                </select>
                </div>
                <td>
                  <div class="query_icon_wrapper">
                    <img src="${pageContext.request.contextPath}/images/icons/item_icons/info_icon.png" class="query_icon_image" id="site_select_info" onClick="getDescById('dom_description_strings',this.id);" style="cursor:pointer;" title="Info">
                  </div>
                </td>
              </tr>
            
                    <!-- State Selection -->
            <!--
              <tr>
                <td align="right"><span class="data_select_label">State Select:</span></td>
                <td align="right" valign="middle"><input type="hidden" name="current_tab" value="source_tab" /> 
                <div class="data_select">                 
                <select id="state_selection" class="sega_form_item" name="data_state_selection">
                  <option value="" disabled="true" selected="true">Select a state...</option>
                  <c:forEach var="state" items="${states}">
                    <c:choose>
                      <c:when test="${!empty selectedState and state.key eq selectedState}">
                        <option value="${state.key}" selected="true">${state.value}</option>
                      </c:when>
                      <c:otherwise>
                        <option value="${state.key}">${state.value}</option>
                      </c:otherwise>
                    </c:choose>
                  </c:forEach>
                </select>
                </div>
                <td>
                  <div class="query_icon_wrapper">
                    <img src="${pageContext.request.contextPath}/images/icons/item_icons/info_icon.png" class="query_icon_image" id="site_select_info" onClick="getDescById('dom_description_strings',this.id);" style="cursor:pointer;" title="Info">
                  </div>
                </td>
              </tr>
            -->
                
              <!-- Garden Server Select -->

            <!--
              <tr>
                <td align="right"><span class="data_select_label">Garden Server Select:</span></td>
                <td align="right" valign="middle"><input type="hidden" name="current_tab" value="source_tab" />
                  <div class="data_select">
                  <select id="server_selection" class="sega_form_item" name="data_server_selection">
                    <option value="" disabled="true" selected="true">Select a Server...</option>
                    <c:forEach var="server" items="${gardenservers}">
                      <c:choose>
                        <c:when test="${!empty selectedServer and server.key eq selectedServer}">
                          <option value="${server.key}" selected="true">${server.value}</option>
                        </c:when>
                        <c:otherwise>
                          <option value="${server.key}">${server.value}</option>
                        </c:otherwise>
                      </c:choose>
                    </c:forEach>
                  </select>
                  </div>
                <td>
                  <div class="query_icon_wrapper">
                    <img src="${pageContext.request.contextPath}/images/icons/item_icons/info_icon.png" class="query_icon_image" id="site_select_info" onClick="getDescById('dom_description_strings',this.id);" style="cursor:pointer;" title="Info">
                  </div>
                </td>                
              </tr>
              -->

              <!-- CP Role Select -->
              <!--
              <tr>
                <td align="right"><span class="data_select_label">WiSARD Role Select:</span></td>
                <td align="right" valign="middle"><input type="hidden" name="current_tab" value="source_tab" />
                  <div class="data_select">
                  <select id="cprole_selection" class="sega_form_item" name="data_cprole_selection">
                    <option value="" disabled="true" selected="true">Select a CP Role...</option>
                    <c:forEach var="cprole" items="${cproles}">
                      <c:choose>
                        <c:when test="${!empty selectedCPRole and cprole.key eq selectedCPRole}">
                          <option value="${cprole.key}" selected="true">${cprole.value}</option>
                        </c:when>
                        <c:otherwise>
                          <option value="${cprole.key}">${cprole.value}</option>
                        </c:otherwise>
                      </c:choose>
                    </c:forEach>
                  </select>
                  </div>
                <td>
                  <div class="query_icon_wrapper">
                    <img src="${pageContext.request.contextPath}/images/icons/item_icons/info_icon.png" class="query_icon_image" id="site_select_info" onClick="getDescById('dom_description_strings',this.id);" style="cursor:pointer;" title="Info">
                  </div>
                </td>                
              </tr>
              -->

              <!-- SP Type Select -->
              <tr>
                <td align="right"><span class="data_select_label">SP Type Select:</span></td>
                <td align="right" valign="middle"><input type="hidden" name="current_tab" value="source_tab" />
                  <div class="data_select">
                  <select id="sptype_selection" class="sega_form_item" name="data_sptype_selection">
                    <option value="" disabled="true" selected="true">Select an SP Type...</option>
                    <c:forEach var="sptype" items="${sptypes}">
                      <c:choose>
                        <c:when test="${!empty selectedSPTypes and sptype.key eq selectedSPTypes}">
                          <option value="${sptype.key}" selected="true">${sptype.value}</option>
                        </c:when>
                        <c:otherwise>
                          <option value="${sptype.key}">${sptype.value}</option>
                        </c:otherwise>
                      </c:choose>
                    </c:forEach>
                  </select>
                  </div>
                <td>
                  <div class="query_icon_wrapper">
                    <img src="${pageContext.request.contextPath}/images/icons/item_icons/info_icon.png" class="query_icon_image" id="site_select_info" onClick="getDescById('dom_description_strings',this.id);" style="cursor:pointer;" title="Info">
                  </div>
                </td>                
              </tr>  

              <!-- Wis Serial ID entry -->
              <tr>
                <td align="left"><span class="data_select_label">WiSARD Serial Entry:</span></td>
                <td align="left" valign="middle"><input type="text" name="wisard_serial_selection" id="wisard_serial_selection" value="" /></td>
                <td>
                  <div class="query_icon_wrapper">
                    <img src="${pageContext.request.contextPath}/images/icons/item_icons/info_icon.png" class="query_icon_image" id="site_select_info" onClick="getDescById('dom_description_strings',this.id);" style="cursor:pointer;" title="Info">
                  </div>
                </td> 
              </tr>

              <!-- Wis Network ID entry -->
              <tr>
                <td align="left"><span class="data_select_label">WiSARD Net-ID Entry:</span></td>
                <td align="left" valign="middle"><input type="text" name="network_id_selection" id="network_id_selection" value="" /></td>
                <td>
                  <div class="query_icon_wrapper">
                    <img src="${pageContext.request.contextPath}/images/icons/item_icons/info_icon.png" class="query_icon_image" id="site_select_info" onClick="getDescById('dom_description_strings',this.id);" style="cursor:pointer;" title="Info">
                  </div>
                </td> 
              </tr>              

            <tr>
              <td></td>
              <td>
                <input type="hidden" name="current_tab" value="source_tab" />
                <input type="submit" class="button sega_form_item" value="Next"/></td>
            </tr>
          
          </form>
        </table>
      </div>
      
      <!-- ======================================== View Tab =================================== -->

      <div id="source_tab" class="accordian_header">View</div>
      <div id="source_tab_content">
          <div class="post_header" style="color:#666666; margin-bottom:10px;"> View WiSARDs</div>

          
                  <div id="channel_data_table_wrapper" class="dataTables_wrapper">

                      <table id="fetch_devices_table" class="channel_table_class" cellpadding="0" cellspacing="0" border="0">
                          <thead>
                          <tr class="Header">
                              <!--
                              <th>Device ID</th>
                              -->
                              <th>WiSARD Serial ID</th>
                              <th>Relative ID</th>
                              <th>Garden Site</th>
                          </tr>
                          </thead>
                          <tbody>
                          <c:forEach var="wisard" items="${wisardTable}">
                              <tr class="" id="_tr">
                                  <!--
                                  <td>${wisard.device_id}</td>
                                  -->
                                  <td>${wisard.serial_id}</td>
                                  <td>${wisard.network_id}</td>
                                  <td>${wisard.site}</td>
                              </tr>
                          </c:forEach>
                          </tbody>
                      </table>

                  </div>
              <table style="clear:both">


              <tr>
                  <td align="left">
                    <form>
                      <button type="button" class="button sega_form_item" onclick="openTab(0)">Previous</button>
                    </form>
                  </td>
                  <td align="right"><input type="hidden" name="current_tab" value="output_tab">
                    <form action="/segaWeb/NetworkManagementServlet" method="post">
                      <input type="hidden" name="current_tab" value="output_tab" />
                      <input type="submit" class="button sega_form_item" value="Next"/>
                    </form>
                  </td>
              </tr>
          </table>
      </div>

      <!-- ===================================== Command Tab =================================== -->
      
      <div id="output_tab" class="accordian_header">Command</div>
      <div id="output_tab_content">
        <div class="post_header" style="color:#666666; margin-bottom:10px;"> View WiSARDs </div>
          <table style="clear:both">
            <form id="getCMD" name="getCMD" action="/segaWeb/NetworkManagementServlet" method="post">
            <tr>
              <div class="data_select">  
                <select id="cmd_selection" class="sega_form_item" name="data_cmd_selection">
                  <option value="reset">Reset Command</option>
                  <option value="change_interval">Change Intvl</option>
                </select>
              </div>
            </tr>

            <tr>
              <td align="left">
                  <button type="button" class="button sega_form_item" onclick="openTab(1)">Previous</button>
              </td>
              <td align="right"><input type="hidden" name="current_tab" value="request_tab" />        
                  <input type="hidden" name="current_tab" value="request_tab" />
                  <input type="submit" class="button sega_form_item" value="Next"/>
              </td>  
            </tr>
          </form>
        </table>
      </div>

      <!-- =================================== Configure Tab =================================== -->

      <div id="request_tab" class="accordian_header">Configure</div>
      <div id="request_tab_content">
        <div class="post_header" style="color:#666666; margin-bottom:10px;"> View WiSARDs </div>

          <div id="channel_data_table_wrapper" class="dataTables_wrapper">

              <table id="list_operations_table" class="channel_table_class" cellpadding="0" cellspacing="0" border="0">
                  <thead>
                  <tr class="Header">
                      <th>Relative ID</th>
                      <th>Garden Site</th>
                      <th>Command</th>
                  </tr>
                  </thead>
                  <tbody>
                  <c:forEach var="wisard" items="${wisardTable}">
                      <tr class="" id="_tr">
                          <td>${wisard.network_id}</td>
                          <td>${wisard.site}</td>
                          <td>reset</td>
                          <!--
                          <td>${cmd}</td>
                          -->
                      </tr>
                  </c:forEach>
                  </tbody>
              </table>

          </div>

        <table style="clear:both">
          <tr>

            <!--
            <td align="left"><button type="button" class="button sega_form_item" onclick="openTab(2)">Previous</button></td>
            -->
            <td align="middle"><input type="hidden" name="current_tab" value="request_tab" />
              <form action="/segaWeb/NetworkManagementServlet" method="post">
                <input type="hidden" name="redirect" value="/segaWeb/data/form/datarequest.jsp" />
                <input type="submit" class="button sega_form_item" value="Submit"/></td>
              </form>
          </tr>
        </table>
      </div>
  </div>
</div>
</div>

<script>

function loadXMLDoc(dname) {
    if (window.XMLHttpRequest) {
        xhttp = new XMLHttpRequest()
    } else {
        xhttp = new ActiveXObject("Microsoft.XMLHTTP")
    }
    xhttp.open("GET", "${pageContext.request.contextPath}/media/xml/" + dname + ".xml", false);
    try {
        xhttp.responseType = "msxml-document"
    } catch (err) {}
    xhttp.send("");
    return xhttp
}

function getDescById(filename, id) {
    var x = loadXMLDoc(filename);
    var xml = x.responseXML;
    path = "/dom_description_strings/dom_obj[@id='" + id + "']/dom_title | /dom_description_strings/dom_obj[@id='" + id + "']/dom_desc";
    if (window.ActiveXObject || xhttp.responseType == "msxml-document") {
        xml.setProperty("SelectionLanguage", "XPath");
        nodes = xml.selectNodes(path);
        $("#info_dialog").dialog('option', 'title', nodes[0].childNodes[0].nodeValue);
        $("#info_dialog").html('<p>' + nodes[1].childNodes[0].nodeValue + '</p>');
        $("#info_dialog").dialog("open")
    } else if (document.implementation && document.implementation.createDocument) {
        var node = xml.evaluate(path, xml, null, XPathResult.ANY_TYPE, null);
        $("#info_dialog").dialog('option', 'title', node.iterateNext().textContent);
        $("#info_dialog").html('<p>' + node.iterateNext().textContent + '</p>');
        $("#info_dialog").dialog("open")
    }
}

function progressBar(index) {
    $("#accordion").find(".accordion_header").each(function() {
        if ($(this).index(".accordion_header") <= index) {
            document.getElementById($(this).attr('id') + '_label').style.backgroundImage = "url(${pageContext.request.contextPath}/images/icons/progress_button_configured.svg)";
            if (document.getElementById($(this).attr('id') + '_bar') != null) document.getElementById($(this).attr('id') + '_bar').className = "progress_bar_configured"
        } else {
            document.getElementById($(this).attr('id') + '_label').style.backgroundImage = "url(${pageContext.request.contextPath}/images/icons/progress_button_unconfigured.svg)";
            if (document.getElementById($(this).attr('id') + '_bar') != null) document.getElementById($(this).attr('id') + '_bar').className = "progress_bar_unconfigured"
        }
    });
    return false
}

function openTab(index) {
    $('#accordion').accordion('option', 'active', index);
    return false
}

function showDiv(img){
  document.getElementById("imageViewer").innerHTML="<img src='${pageContext.request.contextPath}/images/"+img+"' class='imageImage'/> ";
  $("#imageViewer").show();
  
}
function hideDiv(id){
  document.getElementById("imageViewer").innerHTML="";
  $("#imageViewer").hide();
}

function setMarkers(map){
  for (var i = 0; i < gardensites.length; i++) {
    
    
    var garden = gardensites[i];
    var marker = new google.maps.Marker({
      position: {lat: garden[1], lng: garden[2]},
      map: map,
      title: garden[0]
    });

      attachMarkerInfo(marker,garden);    
    
  }
  
 
}

function attachMarkerInfo(marker,garden){

  var contentString =   
  
  '<div id="threeday" style="background-color:#FFFFFF;"><div class="post_header" id="weather_condition_header">' + 
    garden[0] + 
    '</div>' +
    '<p><img height=auto width=49% src="${pageContext.request.contextPath}/images/DailyPlots/'+
    garden[3] + '_AirTC_3day.jpg" id="DailyPlots/' + 
    garden[3] + '_AirTC_3day.jpg" onclick="showDiv(this.id);return false;" style="cursor:pointer;"/>' + 
        '<img height=auto width=49% src="${pageContext.request.contextPath}/images/DailyPlots/' +
    garden[3] + '_PAR_3day.jpg" id="DailyPlots/' +
    garden[3] + '_PAR_3day.jpg" onclick="showDiv(this.id);return false;" style="cursor:pointer;"/></p>' + 
    '<p><img height=auto width=49% src="${pageContext.request.contextPath}/images/DailyPlots/'+
    garden[3] + '_ST_3day.jpg" id="DailyPlots/' + 
    garden[3] + '_ST_3day.jpg" onclick="showDiv(this.id);return false;" style="cursor:pointer;"/>' + 
        '<img height=auto width=49% src="${pageContext.request.contextPath}/images/DailyPlots/' +
    garden[3] + '_SM_3day.jpg" id="DailyPlots/' +
    garden[3] + '_SM_3day.jpg" onclick="showDiv(this.id);return false;" style="cursor:pointer;"/></p>' +
    '<p><img height=auto width=49% src="${pageContext.request.contextPath}/images/DailyPlots/'+
    garden[3] + '_RH_3day.jpg" id="DailyPlots/' + 
    garden[3] + '_RH_3day.jpg" onclick="showDiv(this.id);return false;" style="cursor:pointer;"/>' + 
        '<img height=auto width=49% src="${pageContext.request.contextPath}/images/DailyPlots/' +
    garden[3] + '_Precip_3day.jpg" id="DailyPlots/' +
    garden[3] + '_Precip_3day.jpg" onclick="showDiv(this.id);return false;" style="cursor:pointer;"/></p></div>';
    /*
    '<div id="thirtyday" style="background-color:#FFFFFF;"><div class="post_header" id="weather_condition_header">' + 
    garden[0] + 
    '</div>' +
    '<p><img height=auto width=49% src="${pageContext.request.contextPath}/images/DailyPlots/'+
    garden[3] + '_AirTC_30day.jpg" id="DailyPlots/' + 
    garden[3] + '_AirTC_30day.jpg" onclick="showDiv(this.id);return false;" style="cursor:pointer;"/>' + 
        '<img height=auto width=49% src="${pageContext.request.contextPath}/images/DailyPlots/' +
    garden[3] + '_PAR_30day.jpg" id="DailyPlots/' +
    garden[3] + '_PAR_30day.jpg" onclick="showDiv(this.id);return false;" style="cursor:pointer;"/></p>' + 
    '<p><img height=auto width=49% src="${pageContext.request.contextPath}/images/DailyPlots/'+
    garden[3] + '_ST_30day.jpg" id="DailyPlots/' + 
    garden[3] + '_ST_30day.jpg" onclick="showDiv(this.id);return false;" style="cursor:pointer;"/>' + 
        '<img height=auto width=49% src="${pageContext.request.contextPath}/images/DailyPlots/' +
    garden[3] + '_SM_30day.jpg" id="DailyPlots/' +
    garden[3] + '_SM_30day.jpg" onclick="showDiv(this.id);return false;" style="cursor:pointer;"/></p>' +
    '<p><img height=auto width=49% src="${pageContext.request.contextPath}/images/DailyPlots/'+
    garden[3] + '_RH_30day.jpg" id="DailyPlots/' + 
    garden[3] + '_RH_30day.jpg" onclick="showDiv(this.id);return false;" style="cursor:pointer;"/>' + 
        '<img height=auto width=49% src="${pageContext.request.contextPath}/images/DailyPlots/' +
    garden[3] + '_Precip_30day.jpg" id="DailyPlots/' +
    garden[3] + '_Precip_30day.jpg" onclick="showDiv(this.id);return false;" style="cursor:pointer;"/></p></div>'; 
    */
  marker.addListener('click', function() {
    if(infowindow){
      infowindow.close();
    }
    infowindow = new google.maps.InfoWindow({
      content:contentString
    });
    
      infowindow.open(marker.get('map'), marker);
    
  });
}

$( document ).ready(function() {

  <c:if test="${empty initialize}">
    $('#init_network_config_form').submit();
  </c:if>

  $("#accordion").accordion({
    heightStyle: "content",

    beforeActivate: function(event, ui) {
      //console.log($("#accordion").find(".accordion_header").index($(".accordion_header#" + ui.newHeader.attr('id'))));
      progressBar($("#accordion").find(".accordion_header").index($(".accordion_header#" + ui.newHeader.attr('id'))));
    }

  });

  <c:if test="${current_tab=='source_tab'}">
    openTab(1);
  </c:if>
  <c:if test="${current_tab=='output_tab'}">
    openTab(2);
  </c:if>
  <c:if test="${current_tab=='request_tab'}">
    openTab(3);
  </c:if>


var oTable = $(".channel_table_class").dataTable();
//oTable.each(function(index, value){
//  value.addClass(index > 0 && index % 2 == 0 ? "even" : "odd"); 
//});

$("#info_dialog").dialog({
    autoOpen: false,
    width: 400,
    buttons: [{
        text: "Ok",
        click: function() {
            $(this).dialog("close")
        }
    }]
});

});
</script>
</body>
</html>
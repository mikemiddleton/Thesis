<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import = "helpers.Person" %>

<div id="header" class="header">		
    	<div class="topLogInStatus">
		<c:choose>
			<c:when test="${!empty sessionScope.remoteUser}"> Logged in as: ${sessionScope.remoteUser.getFirst()} | <a href="/segaWeb/logout.jsp" class="loginLink">Logout</a></c:when>
			<c:otherwise> Not Logged In | <a class="loginLink" href="/segaWeb/login.jsp">Login</a></c:otherwise>
		</c:choose>
		</div>
<div id="header_bg" class="header_bg">
	<div id="header_navigation" class="header_navigation">
    	<div id="header_text" class="header_text" onclick="location.href='/segaWeb/index.jsp';" style="cursor:pointer;">
        	SEGAweb <br />
            portal
        </div>
    	<div id="header_logo" class="header_logo" onclick="location.href='/segaWeb/index.jsp';" style="cursor:pointer;"></div>
        
   	 	<div class="header_navigation_bar">
            <div id="overview_link" class="header_navigation_link" style="border-left-width: thin;border-left-style: dotted;border-left-color: #76838e;">
                <a href="/segaWeb/index.jsp" class="header_nav">home</a>
            </div>
        </div>
        <div class="header_navigation_bar">
            <div id="visualization_link" class="header_navigation_link">
                <a href="/segaWeb/data/form/datarequest.jsp" class="header_nav" title="Request and retrieve data">data</a>
            </div>
        </div>
        <div class="header_navigation_bar">
            <div id="login_link" class="header_navigation_link">
                <c:choose>
					<c:when test="${!empty pageContext.request.remoteUser}"><a href="/segaWeb/login.jsp" class="header_nav" title="Configure user account or experiment settings">settings</a></c:when>
					<c:otherwise> <a href="/segaWeb/login.jsp" class="header_nav" title="Log in to the web portal">login</a></c:otherwise>
				</c:choose>
            </div>
        </div>
        <div class="header_navigation_bar">
            <div id="user_guide_link" class="header_navigation_link">
                <a href="/segaWeb/help/help.jsp" class="header_nav" title="Access the web portal user guide">help</a>
            </div>
        </div>    
        
        
    </div>

</div>
<div id="header_img" class="header_img">
</div>

</div>




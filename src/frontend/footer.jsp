<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="footer_bg">
    <div class="footer_content">
        <a href="/segaWeb/index.jsp" class="footer_nav">Home</a>&nbsp;&nbsp;|&nbsp;&nbsp;
        <a href="http://sega.nau.edu/contact" class="footer_nav">Contact</a>&nbsp;&nbsp;|&nbsp;&nbsp;
		<c:choose>
			<c:when test="${!empty pageContext.request.remoteUser}"><a href="http://wisard-serv1.egr.nau.edu/cas/logout" class="footer_nav">Logout</a></c:when>
			<c:otherwise><a class="footer_nav" href="https://wisard-serv1.egr.nau.edu/cas/login">Login</a></c:otherwise>
		</c:choose>
    </div>
</div>
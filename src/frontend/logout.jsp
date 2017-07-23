<%@ page import ="java.sql.*" %>
<%@ page import ="helpers.SegaDB" %>
<%@ page import = "helpers.Person" %>
<%@ page import = "java.io.PrintWriter" %>
<%@ page import = "javax.servlet.http.*"%>

<%
          session.removeAttribute("remoteUser");
          response.sendRedirect("/segaWeb/index.jsp");  
%>
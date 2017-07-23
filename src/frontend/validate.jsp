<%@ page import ="java.sql.*" %>
<%@ page import ="helpers.SegaDB" %>
<%@ page import = "helpers.Person" %>
<%@ page import = "java.io.PrintWriter" %>
<%@ page import = "javax.servlet.http.*"%>

<%
    try{
        String username = request.getParameter("username");   
        String password = request.getParameter("password");

        /*
        Class.forName("com.mysql.jdbc.Driver");  // MySQL database connection
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/javademo?" + "user=root&password=");    
        PreparedStatement pst = conn.prepareStatement("Select user,pass from login where user=? and pass=?");
        pst.setString(1, username);
        pst.setString(2, password);
        ResultSet rs = pst.executeQuery();                        
        if(rs.next())           
           out.println("Valid login credentials");        
        else
           out.println("Invalid login credentials"); 
        */

        SegaDB sdb = new SegaDB();
        sdb.init();
        ResultSet rs = sdb.getLoginPerson(username, password);
        if(rs.next()){
          Person p = new Person(rs);
          session.setAttribute("testVal", "papabless");
          session.setAttribute("remoteUser", p);
          response.sendRedirect("/segaWeb/index.jsp");    
        }
        else{
          out.println("Incorrect username or password");
        }

        sdb.disconnect();
   }
   catch(Exception e){       
       e.printStackTrace(new PrintWriter(out));   
   }  
%>
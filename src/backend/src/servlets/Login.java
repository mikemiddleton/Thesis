package servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

import helpers.Validate;

public class Login extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// ensure uniform charset for all requests
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		// pull email and pass from request
		String email = request.getParameter("email");
		String pass = request.getParameter("pass");
		
		if(Validate.checkUser(email,pass)){
			RequestDispatcher rs = request.getRequestDispatcher("welcome");
			rs.forward(request,response);
		}
		else{
			out.println("Username or password incorrect");
			RequestDispatcher rs = request.getRequestDispatcher("index.html");
			rs.include(request, response);
		}
	}
}

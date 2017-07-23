package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.nau.rtisnl.SegaWebException;
import helpers.Experiment;
import helpers.Person;
import helpers.SegaDB;
import helpers.SmartList;
import utilities.SegaLogger;

/*
 * Servlet implementation class LoginServlet
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	/* serial version uid */
	private static final long serialVersionUID = 1L;
	/* Log object */
	private static SegaLogger log;
	/* database helper */
	private SegaDB db_helper = null;
	
	public LoginServlet() throws ClassNotFoundException{
		super();
		try{
			log = new SegaLogger("/usr/share/tomcat8/segalogs/LoginServlet.txt");
		}
		catch(IOException e){
			System.out.println("ERROR: was not able to create log file /usr/share/tomcat8/segalogs/LoginServlet.txt");
			e.printStackTrace();
		}
	}
	
	/*
	 * performs the doGet functionality 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}
		
	/*
	 * performs the doPost functionality
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		SegaDB sdb = new SegaDB();
		
		try {
			sdb.getAllPersons();
		} catch (SegaWebException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		log.write("init variable: " + request.getParameter("init"));
		log.write("---");
		
		// get initial values to populate the form results
		if(request.getParameter("init") != null && request.getParameter("init").equalsIgnoreCase("true")){
			
			// get list of garden sites 
			db_helper.init();

		  // query psql db for info to populate forms
		  try {
			  // do the query
			  ResultSet rs = sdb.getAllPersons();
			  SmartList<Person> persons = new SmartList<Person>();
			  while(rs.next()){
				Person p = new Person(rs);
				persons.add(p);
			  } 
			  
			  // add persons to session
			  request.getSession().setAttribute("persons", persons);
			  
		  }  catch (SQLException | SegaWebException e) {
				StringWriter errors = new StringWriter();
	        	e.printStackTrace(new PrintWriter(errors));
	    		log.write(errors);
		  }
		}
		else{
			
		}
	}
}

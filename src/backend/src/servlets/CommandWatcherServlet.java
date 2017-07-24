package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class CommandWatcherServlet
 */
@WebServlet("/CommandWatcherServlet")
public class CommandWatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private volatile String message = "test test";
	private volatile Thread t;
	private volatile boolean checking;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CommandWatcherServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().println(message);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(request.getParameter("start") != null){
			t = new Thread(() -> {
				checking = true;
				while(checking){
					message += ".";
					try {
						Thread.sleep(20000);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
				}
			});
			t.start();
			response.getWriter().println("command watcher started");
			response.getWriter().println(message);
		}
		else if(request.getParameter("stop") != null){
			checking = false;
			response.getWriter().println("command watcher stopped");
			message = "test test";
		}
		else{
			response.getWriter().println(checking ? "running":"stopped");
		}
		
	}

}

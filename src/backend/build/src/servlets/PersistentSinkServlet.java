package servlets;

import helpers.DataFetchHelper;
import helpers.PersistentSinkSessionHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import utilities.SegaLogger;

@ApplicationScoped
@ServerEndpoint(value = "/PersistentSinkServlet")
public class PersistentSinkServlet{

	SegaLogger log = null;


	private PersistentSinkSessionHandler sessionHandler = new PersistentSinkSessionHandler();

	@OnOpen
	public void open(Session session) {
		try {
			log = new SegaLogger("/usr/share/tomcat7/segalogs/PersistentSinkServlet.txt");
		} catch (IOException e) {
			System.out.println("ERROR: Cannot create logger /usr/share/tomcat7/segalogs/SegaDataRequestServlet.txt");
			e.printStackTrace();
		}
		sessionHandler.addSession(session);
		log.write("opened and session added");
	}

	@OnClose
	public void close(Session session) {
		log.write("closed");
		sessionHandler.removeSession(session);
	}

	@OnError
	public void onError(Throwable error) {
		log.write("An error occured.");
		Writer result = new StringWriter();
		PrintWriter printWriter = new PrintWriter(result);
		error.printStackTrace(printWriter);
		log.write(result.toString());
	}


	@OnMessage
	public void onMessage(String message, Session session) {
		String update_data_interval = null;
		String update_server_ip  = null;
		String update_channel_list = null;
		String update_output_style = null;

		JSONParser parser = new JSONParser();

		Object obj = null;
		try {
			obj = parser.parse(message);
		} catch (ParseException e) {
			log.write(e);
		}		JSONObject jsonObject = (JSONObject) obj;

		if ("add".equals((String) jsonObject.get("action"))) {
			if ((update_data_interval = (String) jsonObject.get("update_data_interval")) != null) {
				if ((update_server_ip = (String) jsonObject.get("update_server_ip")) != null) {
					if ((update_channel_list = (String) jsonObject.get("update_channel_list")) != null) {
						if ((update_output_style = (String) jsonObject.get("update_output_style")) != null) {

							DataFetchHelper dataHelper = new DataFetchHelper(update_server_ip);

							sessionHandler.addSink(dataHelper, session);
						}
					}
				}
			}
		}
		else if ("poll".equals((String) jsonObject.get("action"))) {
			if ((update_data_interval = (String) jsonObject.get("update_data_interval")) != null) {
				if ((update_server_ip = (String) jsonObject.get("update_server_ip")) != null) {
					if ((update_channel_list = (String) jsonObject.get("update_channel_list")) != null) {
						if ((update_output_style = (String) jsonObject.get("update_output_style")) != null) {

							sessionHandler.poll(session, update_data_interval, update_server_ip, update_channel_list, update_output_style);
						}
					}
				}
			}

		}

	}
}


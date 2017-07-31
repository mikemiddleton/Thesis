package servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import org.apache.commons.codec.binary.Hex;

import utilities.ArrayUtilities;
import utilities.DataGeneratorCodec;
import utilities.Message;
import utilities.Packet;
import utilities.Packet.Report;
import utilities.SegaLogger;

/**
 * Servlet implementation class CommandWatcherServlet
 */
@WebServlet("/CommandWatcherServlet")
public class CommandWatcherServlet extends HttpServlet implements MqttCallback{
	private static final long serialVersionUID = 1L;
	private volatile String message = "test test";
	private volatile Thread t;
	private volatile boolean checking;


	/** The IP address or DNS host name of the remote garden server (should include RBNB port) */
	private String remote_ip_address = null;
	/** The IP address or DNS host name of the local data server (should include RBNB port) */ 
	private String local_ip_address = null;
	/** The common name of the remote garden server */
	private String common_name = null;

	/** Seconds from 1970 to 2000 - used to encode WiSARD timestamps into system time */
	private static final int wisard_seconds_to_add = 946710000;
	//	/** Number of packets in 3 months at 115200 baud with 64 bytes/packet */
	//	private int archive_size = 1749600000;	

	/** Final reconnect logic value - try to reconnect for 3 days based on the thread sleep time*/
	private long max_reconnect_attempts, reconnect_sleep_time;
	/** Total number of reconnect attempts by mqtt client threads */
	private AtomicInteger mqtt_reconnect_attempts = new AtomicInteger();

	/** Thread object holder */
	private Thread mqtt_thread,packet_thread;	

	/**MessageQueue for incoming packets from the WiSARD hub */
	private volatile Hashtable<Integer, List<Integer>> reset_table;

	/** This variable is set when one of the RBNB threads catches an exception and attempts to reconnect
	 * so that the other thread won't also try reconnecting if it catches the same error before being interrupted*/
	private AtomicBoolean reconnecting_rbnb = new AtomicBoolean(false);

	/** An exception handler for reconnect logic used by each thread that handles RBNB connections */
	private Thread.UncaughtExceptionHandler thread_exception_handler;


	/**The number of milliseconds to sleep between requests */
	private long thread_sleep;
	
	private String subTopic;
	private static int qos = 2;
	private String broker = "tcp://exp249a-12.egr.nau.edu:1883";
	private String subID = "/cmd_watcher";
	private MqttClient subClient;
	private AtomicBoolean reconnecting_mqtt = new AtomicBoolean();
	private AtomicBoolean reconnect_mqtt = new AtomicBoolean(false);
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CommandWatcherServlet() {
        super();
        reset_table = new Hashtable<Integer, List<Integer>>();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		synchronized(reset_table){
			// print out the hash table in json
			response.getWriter().println("{");
			for(int key: reset_table.keySet()){
				response.getWriter().print("\"" + key + "\":");
				response.getWriter().println("[");
				int count = 0;
				for(int value: reset_table.get(key)){
					response.getWriter().print("\"" + value + "\"");
					if(++count < reset_table.get(key).size()){
						response.getWriter().println(",");
					}
					else{
						response.getWriter().println();
					}
				}
				response.getWriter().println("]");
			}
			response.getWriter().println("}");
		}
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		try{
			
			// TODO Auto-generated method stub
			if(request.getParameter("start") != null){
				
				
				// create sub clients and connect to each site's broker
				
				/*
				t = new Thread(() -> {
					checking = true;
					while(checking){
						message += ".";
						try {
							Thread.sleep(20000);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							break;
						}
					}
					try {
						disconnect_mqtt();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}); */
				
				/* JD's MQTT code */
				//Make the persistence directory if it doesn't exist
				//File file = new File(root_directory + "/" + common_name + "/mqtt_config/mqtt_persistence/test_file");
				//file.getParentFile().mkdirs();
				//MqttDefaultFilePersistence subPersistence = new MqttDefaultFilePersistence(root_directory + "/" + common_name + "/mqtt_config/mqtt_persistence/");
				
				common_name = request.getParameter("common_name");
				connect_mqtt(common_name);	
				
				
				/* END JD's MQTT code */
				
		
				//t.start();
				response.getWriter().println("{\"response\":\"started\"}");
			}
			else if(request.getParameter("stop") != null){
				//clear hash table
				reset_table.clear();
				//checking = false;
				disconnect_mqtt();
				response.getWriter().println("{\"response\":\"stopped\"}");
			}
			else{
				response.getWriter().println();
				response.getWriter().println("{\"response\":\"" + (checking ? "running":"stopped") + "\"}");
			}
		} catch(Exception e){
			response.getWriter().println("failed");
			//e.printStackTrace(response.getWriter());
		}
		
	}
	
	private boolean validate_mqtt(){
		if(subClient != null){
			if(subClient.isConnected()){
				return true;
			}
		}
		return false;
	}
	
	private void connect_mqtt(String cn) throws MqttException, InterruptedException{
		subClient = new MqttClient(broker, "RTISNL-EXP249a-12" + subID, new MemoryPersistence());
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(false); //set to false to maintain session in client and broker
		connOpts.setConnectionTimeout(180);//3 minutes to wait for connection to establish
		//writeToLog("Connecting subscriber to broker " + broker + " as client " + subID + "...");
		subClient.setCallback(this);
		if(!subClient.isConnected()){
			subClient.connect(connOpts);
		}
	    //Thread.sleep(100);
		//writeToLog("\tOK - subscriber connected to " + broker);
		//writeToLog("Subscribing to " + subTopic);
		subClient.subscribe(common_name + "/data/wisard");
		//writeToLog("\tOK - " + subID + " subscribed to " + subTopic);				
	}

	private void disconnect_mqtt() throws MqttException{		
		//writeToLog("Disconnecting MQTT Client...");
		if(subClient != null){
			subClient.disconnect();
			reset_table.clear();
			//writeToLog("\tOK - RDF Subscriber");	

		}
	}

	@Override
	public void connectionLost(Throwable arg0) {
		try {
			connect_mqtt(common_name);
		} catch (MqttException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub/

	}

	@Override
	public void messageArrived(String topic, MqttMessage msg){
		
		PrintWriter out = new PrintWriter(System.out, true);
		try{
		// contains "mod_0/stream_30"
		
		//print_to_console(sdf.format(new Date()) + " Rx'd msg from "  + arg0);
			//Packet p = new Packet(msg.getPayload(), true);
			//ArrayList<Report> rpt = p.getReport();
			synchronized(reset_table){
				int net_id_hi = msg.getPayload()[8];
				int net_id_lo = msg.getPayload()[9];
				int net_id = ((net_id_hi << 8) | (net_id_lo & 0xFF));
				int cmd = msg.getPayload()[19];
				if(cmd == 0x1E){
					System.out.println("reset " + net_id);
					if(reset_table.get(net_id) == null){
						reset_table.put(net_id, new ArrayList<Integer>());
					}
					List<Integer> lst = reset_table.get(net_id);
					lst.add(new Integer(cmd));
				}
				//else{
					//System.out.println("command was " + cmd);
				//}
				
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		/*
		if(arg0.contains("/data/wisard")){
			process_wisard_data(arg1);
		}
		else if(arg0.contains("/data/cr1000")){
			process_cr1000_data(arg0,arg1);

		}
		 */
	}	
	
	
}

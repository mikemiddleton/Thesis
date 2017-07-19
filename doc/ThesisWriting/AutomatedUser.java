Runnable runnable = () -> {
    try {
        String name = Thread.currentThread().getName();
        System.out.println("Foo " + name);
        TimeUnit.SECONDS.sleep(1);
        System.out.println("Bar " + name);
    }
    catch (InterruptedException e) {
        e.printStackTrace();
    }
};

Thread thread = new Thread(runnable);
thread.start();

/*
 * Classes who implement this interface must define a method to handle 
 * messages
 */
public interface MessageProcessor{
	public void processMessage(Message msg);
}

/*
 * Classes who implement this interface must define a method to handle
 * exceptions
 */
public interface ErrorHandler{
	public void handleError(Exception e);
}

/*
 * class definition for automated user controllers which create, authenticate,
 * and return automated user objects 
 */
public class AutomatedAgentController{
	public static AutomatedAgent createAutomatedAgent(String username, String password, ArrayList<SubscriptionInfo> subscriptions, MessageProcessor msgProc, ErrorHandler errHandler){
		// logs in user, validates permissions to subscriptions, creates person and automated user
	}

	public static runCommand(NetManagementCommand nmc, AutomatedAgent agent){
		// validates user's permission to run command and performs safety validation
		nmc.runCommand();
	}
}

// An example of a class with a main method - how you would use these classes in a program
public class MyAutomatedAgent{
	public static void main(String[] args){
		// create and fill list of SubscriptionInfo objects
		ArrayList<SubscriptionInfo> subscriptions = new ArrayList<SubscriptionInfo>();

		// an example of how to use the AutomatedUserController to create an AutomatedUser
		AutomatedAgent a = AutomatedAgentController.createAutomatedAgent("username", "password", subscriptions, (Message msg) -> {
			// write logic here to process messsage
		}, (Exception e) -> {
			// handle any exceptions here
			// use instanceof to check for specific exception types
			// thread will exit after this returns
		});
		new Thread(a).start();
	}
}

/*
 * class definition for automated users which encapsulates a person and message processor
 * objects, as well as an array list of subscription objects. This will allow automated 
 * agents to monitor data streams and enact changes upon the WiSARDs they have sufficient
 * permissions for
 */
public abstract class AutomatedAgent implements runnable, MqttCallback{
	protected Person person;
	protected MessageProcessor msgProc;
	protected ErrorHandler errHandler;
	protected ArrayList<SubscriptionInfo> subInfo;
	protected ArrayList<MqttClient> mqttClients;
	protected String autoUserName;
	protected LinkedList<Message> msgQueue;

	public AutomatedAgent(Person p, ArrayList<SubscriptionInfo> s, String autoUserName, MessageProcessor m, ErrorHandler e){
		this.person = p;
		this.msgProc = m;
		this.errHandler = e;
		this.subInfo = s;
		this.autoUserName = autoUserName;
		this.mqttClients = new ArrayList<MqttClient>();
		this.msgQueue = new LinkedList<Message>();
	}

	public void run(){
		try{
			// create connections for all subscription clients and add to an ArrayList
			for(SubscriptionInfo subscription : subInfo){
				MqttConnectOptions connOpts = new MqttConnectOptions();
				connOpts.setCleanSession(false);
				connOpts.setConnectionTimeout(180);
				MqttClient subClient = new MqttClient(subscription.broker, subscription.gsCommonName + "/" + autoUserName);
				subClient.subscribe(subscription.subTopic);
				mqttClients.add(subClient);
			}

			// while thread is running, listen for messages
			while(!Thread.currentThread().isInterrupted()){
				synchronized(msgQueue){	
					Message m = msgQueue.peekFirst();
					if(m != null){
						msgProc.processMessage(m);
						msgQueue.removeFirst();
					}
				}
			}
		} catch(Exception e){
			errHandler.handleError(e);
		}
	}

	@Override
	public void connectionLost(Throwable arg0) {
		// empty
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// empty
	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1){
		synchronized(msgQueue){
			msgQueue.add(new Message(arg0,arg1));
		}
	}
}

/*
 * Objects fo this type will possess the connection information necessary
 * to make a new mqtt subscription client
 */
public class SubscriptionInfo{
	public String subTopic;
	public String broker;
	public String gsCommonName;
	public int qos;
}


subTopic = common_name + "/data/#";
		qos = 2;
		broker = "tcp://" + remote_ip_address + ":1883";
		subID = common_name + "/rdf_subscriber";
		if(!reconnecting_mqtt.get()){
			writeToLog("===== " + new Date(System.currentTimeMillis()) + " =====");
			//Log the configuration parameters
			writeToLog("[Remote IP]: " + remote_ip_address + "\r\n"
					+  "[Local IP]: " + local_ip_address + "\r\n"
					+  "[Common Name]: " + common_name + "\r\n"
					+  "[Root Directory]: " + root_directory + "\r\n"
					+  "[Cache Size]: " + cache_size + "\r\n"
					+  "[Archive Size]: " + archive_size + "\r\n"
					+  "[Debug]: " + debug + "\r\n"
					+  "[Verbose]: " + verbose + "\r\n"
					+  "[Sleep]: " + thread_sleep + "\r\n"
					+  "[Subscriber Topic]: " + subTopic + "\r\n"
					+  "[Quality of Service (QoS)]: " + qos + "\r\n"
					+  "[MQTT Broker]: " + broker + "\r\n"
					+  "[Subscriber ID]: " + subID + "\r\n"
					);
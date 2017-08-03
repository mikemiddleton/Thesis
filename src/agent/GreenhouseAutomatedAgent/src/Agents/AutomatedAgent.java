package Agents;

import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import helpers.Person;
import helpers.SubscriptionInfo;
import interfaces.ErrorHandler;
import interfaces.MessageProcessor;
import utilities.Message;

/*
 * class definition for automated users which encapsulates a person and message processor
 * objects, as well as an array list of subscription objects. This will allow automated 
 * agents to monitor data streams and enact changes upon the WiSARDs they have sufficient
 * permissions for
 */
public class AutomatedAgent implements Runnable, MqttCallback{
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
				System.out.println("Subscribing to subscription topic: " + subscription.subTopic);
				subClient.setCallback(this);
				subClient.connect();
				subClient.subscribe(subscription.subTopic);
				mqttClients.add(subClient);
			}

			// while thread is running, listen for messages
			while(true){
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
	public synchronized void connectionLost(Throwable arg0) {
		// TODO loop through all the clients and reconnect
		// any that aren't connected
		for(int j=0; j<mqttClients.size(); j++){
			MqttClient client = mqttClients.get(j);
			if(!client.isConnected()){
				// try 10 times
				for(int i=0; i<10; i++){	
					
					try {
						System.out.println("Attemping to reconnect client " + client.getClientId());
						client.connect();
						client.subscribe(subInfo.get(j).subTopic);
						break;
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						try {
							Thread.sleep(30000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		}
	}

	@Override
	public synchronized void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		synchronized(msgQueue){
			msgQueue.add(new Message(arg0,arg1));
		}
		
	}

	/*
	private void connect_mqtt(String cn) throws MqttException, InterruptedException{
		subClient = new MqttClient(broker, "RTISNL-EXP249a-12" + subID, new MemoryPersistence());
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(false); //set to false to maintain session in client and broker
		connOpts.setConnectionTimeout(180);//3 minutes to wait for connection to establish
		subClient.setCallback(this);
		if(!subClient.isConnected()){
			subClient.connect(connOpts);
		}
		subClient.subscribe(common_name + "/data/wisard");			
	}
	*/

	/*
	private void disconnect_mqtt() throws MqttException{
		if(subClient != null){
			subClient.disconnect();
		}
	}
	*/
}
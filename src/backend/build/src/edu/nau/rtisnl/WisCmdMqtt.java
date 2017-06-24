package edu.nau.rtisnl;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import utilities.ArrayUtilities;
import utilities.CRC;
import utilities.PacketGenerator;

public class WisCmdMqtt {

	
	private String pubTopic = "exp249a-12/cmd/sicc106-01";
	private static int qos = 2;
	private String broker = "tcp://exp249a-12.egr.nau.edu:1883";
	private String pubID = "siccs106/cmd_publisher";
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		(new WisCmdMqtt()).run();

	}
	
	private void run(){
		//Connect to MQTT
		MqttDefaultFilePersistence pubPersistence = new MqttDefaultFilePersistence("./mqtt_persistence");

		try {
			MqttClient pubClient = new MqttClient(broker, pubID, pubPersistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(false);
			System.out.println("Connecting publisher to broker " + broker + " as client " + pubID + "...");
			pubClient.connect(connOpts); 
			System.out.println("\tOK - " + pubID + " connected to " + broker);
		
			
			byte[] payload = PacketGenerator.Reset_Command_Packet_ExpDur((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x3C, System.currentTimeMillis() + 60*60*1000);
			payload = addCrc(payload);
			//Publish the packet
			MqttMessage message = new MqttMessage(payload);
			message.setQos(qos);
			pubClient.publish(pubTopic,message);
			
			pubClient.disconnect();
			System.out.println("Disconnected... exiting");
			
			
		
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	/**
	 * Adds crc to packet and returns the final packet
	 * @param message
	 * @return
	 */
	public byte[] addCrc(byte[] message){
		//Calculate the value for the CRC
		int crc = CRC.compute_crc(ArrayUtilities.convert_to_int_array(message));
		//Parse out the high byte
		byte crc_hi_byte = (byte) (crc >> 8);
		//Parse out the low byte
		byte crc_low_byte = (byte) (crc & 0xFF);

		//Create new packet with extra 2 bytes for the CRC
		byte[] cmdFinal = new byte[message.length + 2];

		//Populate the new packet with the original packet contents
		for(int i = 0; i < message.length; i++){
			cmdFinal[i] = message[i];
		}

		//Add the CRC to the new packet
		cmdFinal[cmdFinal.length-2] = crc_hi_byte;
		cmdFinal[cmdFinal.length-1] = crc_low_byte;
		
		return cmdFinal;


	}
	
	
	
	

}

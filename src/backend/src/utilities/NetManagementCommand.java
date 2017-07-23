package utilities;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import helpers.ValidationIssue;
import helpers.Site;
import helpers.SmartList;
import helpers.ValidationIssue;
import helpers.Wisard;

public abstract class NetManagementCommand {

	protected String pubTopic = "exp249a-12/cmd/siccs106-01";
	protected int qos = 2;
	protected String broker = "tcp://exp249a-12.egr.nau.edu:1883";
	String pubId = "siccs106/cmd_publisher";
	protected Wisard wisard;
	protected long expiration;
	protected int hub;
	
	public NetManagementCommand(Wisard wisard, int hub){
		this(wisard, hub, 60*60*1000);
	}
	
	public NetManagementCommand(Wisard wisard, int hub, long expiration){
		this.wisard = wisard;
		this.hub = hub;
		this.expiration = expiration;
	}
	
	public abstract byte[] getPayload(byte hub_hi, byte hub_lo, byte dest_hi, byte dest_lo);
	public abstract SmartList<ValidationIssue> validate(Wisard w);
	public abstract SmartList<ValidationIssue> validate(Site s, Wisard w);
	public abstract SmartList<ValidationIssue> validate(Experiment e, Wisard w);
	
	public void runCommand(){
		// 1. acquire broker for the site wisard is deployed
		// 2. make message payload
		// 3. take hex value of relative id and add to command payload
		// 4. make a message from payload
		// 5. connect, publish message, disconnect
	
		//Connect to MQTT
		MqttDefaultFilePersistence pubPersistence = new MqttDefaultFilePersistence("./mqtt_persistence");
		
		try{
			MqttClient pubClient = new MqttClient(broker, pubId, pubPersistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(false);
			pubClient.connect(connOpts);
			
			// get  string hex id from 
			String hex_id_low = Integer.toHexString(wisard.getNetwork_id() & 0xFF);
			String hex_id_hi = Integer.toHexString(wisard.getNetwork_id() >> 8);
			byte dest_lo = Byte.parseByte(hex_id_low,16);
			byte dest_hi = Byte.parseByte(hex_id_hi,16);
			
			// get  string hex id from 
			hex_id_low = Integer.toHexString(hub & 0xFF);
			hex_id_hi = Integer.toHexString(hub >> 8);
			byte hub_lo = Byte.parseByte(hex_id_low,16);
			byte hub_hi = Byte.parseByte(hex_id_hi,16);
			
			// reset command for wisard 60 (0x3C)
			byte[] payload = getPayload(hub_hi, hub_lo, dest_hi, dest_lo);
			payload = addCrc(payload);
			
			// reset command for wisard 61 (0x3D)
			//byte[] payload2 = PacketGenerator.Reset_Command_Packet_ExpDur((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x3D, System.currentTimeMillis() + 60*60*1000);
			//payload2 = addCrc(payload2);
			
			//publish packet 1
			MqttMessage message = new MqttMessage(payload);
			message.setQos(qos);
			pubClient.publish(pubTopic, message);
			
			//publish packet 2
			//MqttMessage message2 = new MqttMessage(payload2);
			//message2.setQos(qos);
			//pubClient.publish(pubTopic, message2);
			
			pubClient.disconnect();
		} catch(MqttException e){
			// to do:
			// - handle exception
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

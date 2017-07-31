package utilities;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Simple container for MqttMessages
 * @author jdk85
 *
 */
public class Message implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1501777499854989487L;
	private String topic;
	private byte[] payload;
	private int qos;
	private int id;
	
	/**
	 * Stores MQTT messages in generic, serializable class
	 * 
	 * @param topic - message topic
	 * @param message - message content
	 */
	public Message(String topic, MqttMessage mqttMsg){
		setTopic(topic);
		setPayload(mqttMsg.getPayload());
		setQos(mqttMsg.getQos());
		setId(mqttMsg.getId());
		
	}

	/**
	 * @return the topic
	 */
	public String getTopic() {
		return topic;
	}
	/**
	 * @param topic the topic to set
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

	/**
	 * 
	 * @return
	 */
	public byte[] getPayload() {
		return payload;
	}

	/**
	 * 
	 * @param payload
	 */
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	/**
	 * 
	 * @return
	 */
	public int getQos() {
		return qos;
	}

	/**
	 * 
	 * @param qos
	 */
	public void setQos(int qos) {
		this.qos = qos;
	}


	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

}
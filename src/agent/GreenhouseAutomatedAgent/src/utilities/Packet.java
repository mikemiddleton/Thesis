package utilities;

import java.util.ArrayList;
import java.util.Arrays;

import com.rbnb.sapi.ChannelMap;

public class Packet
{
	/** Stores the network info for this packet */
	private NetworkPackage network;
	/** Stores the message info for this packet */
	private Message message;
	/** Stores this packet's data elements */
	private DataElement[] data_element;
	/** Stores the reports from each of the data elements */
	private ArrayList<Report> report = new ArrayList<Report>();
	/** Stores the parsed CRC from this packet */
	private int crc = 0;
	/** Keeps count of the data elements stored in this packet */
	private int data_element_count = 1;
	
	/**Stores the raw packet */
	private byte[] raw_packet;
	
	
	/**
	 * The constructor accepts the raw WiSARD packet and parses it into 
	 * reports and data elements
	 * 
	 * @param rec_packet
	 * @throws Exception
	 */
	public Packet(byte[] rec_packet,boolean includeCRC) throws ArrayIndexOutOfBoundsException, NegativeArraySizeException, InstantiationException{
		System.out.println("parsing packet");
		try{
			raw_packet = rec_packet;
			//If there is data and it's not an empty packet - parse it
			if (raw_packet != null && raw_packet.length >= 13) {
				System.out.println("not raw packet");
				//Init the network, message, and data element objects
				network = new NetworkPackage();
				message = new Message();
				data_element = new DataElement[data_element_count];
				
				if(includeCRC){
					//Combine the CRC bytes
					crc = rec_packet[raw_packet.length - 2] & 0xFF;
					crc = (crc << 8) | (raw_packet[rec_packet.length - 1] & 0xFF);	
					if(!CRC.check_crc(raw_packet)){
						System.out.println("failed crc");
						throw new InstantiationException("Packet did not pass CRC");
					}
				}
	
				//Init the payload counter
				int payload_count = 0;
				//Init the message payload
				byte[] message_payload = null;			
				
				int pkt_length;
				if(includeCRC){
					pkt_length = rec_packet.length - 2;
				}
				else{
					pkt_length = rec_packet.length;
				}
				
				System.out.println("I'm about to loop");
				
				//For each byte in the packet (excluding the CRC bytes)
				for (int i = 0; i < pkt_length; i++) {
					//Switch case over the byte position to parse the packet
					switch (i) {
					case 0:
						network.destination = rec_packet[i] & 0xFF;
						break;
					case 1:
						network.destination = (network.destination << 8) | (rec_packet[i] & 0xFF);
						break;
					case 2:
						network.source = rec_packet[i] & 0xFF;
						break;
					case 3:
						network.source = (network.source << 8) | (rec_packet[i] & 0xFF);
						break;
					case 4:
						message.id = rec_packet[i] & 0xFF;
						break;
					case 5:
						message.flags = rec_packet[i] & 0xFF;
						break;
					case 6:
						message.number = rec_packet[i] & 0xFF;
						break;
					case 7:
						message.number = (message.number << 8 ) | (rec_packet[i] & 0xFF);
						break;
					case 8:
						message.address = rec_packet[i] & 0xFF;
						break;
					case 9:
						message.address = (message.address << 8) | (rec_packet[i] & 0xFF);
						break;
					case 10:
						message.length = (rec_packet[i] & 0xFF);
						//If the message length is 7, then it is empty
						if (message.length > 7) {
							// There is at least one data element in the message
							message_payload = new byte[message.length - 7];
							//data_element = new DataElement[++data_element_count];
						}
						break;
					default:
						//If none of the cases above are met, then the current byte is part of the payload
						if (payload_count < message.length){
							message_payload[payload_count++] = rec_packet[i];
						}
						break;
					}
				}
				
				System.out.println("finished a loop");
				///////////////////////////////
				//
				//	Populate data_element array
				//
				///////////////////////////////
				
				//Init the indexing counters 
				//j is the position in the data element array and 
				//k is the byte position in the message payload
				int j = 0, k = 0;
				
				System.out.println("I'm about to loop");
				
				//For each byte in the message payload...
				for (int i = 0; i < message_payload.length; i++) {
					
					switch (k) {
					case 0:
						data_element[j] = new DataElement();
						data_element[j].id = message_payload[i] & 0xFF;
						break;
					case 1:
						data_element[j].length = message_payload[i] & 0xFF;					
						data_element[j].payload = new byte[data_element[j].length - 3];
						
						if (payload_count - data_element[j].length > 0) {
							data_element_count++;
							data_element = extend_data_element_array(data_element, 1);
							payload_count = payload_count - data_element[j].length;
						}
						break;
					case 2:
						data_element[j].version = message_payload[i] & 0xFF;
						break;
					default:
						data_element[j].payload[k - 3] = message_payload[i];
						break;
					}
					
					//If we reached the end of the current data element, reset the counters
					if (k == data_element[j].length - 1) {
						j++;
						k = 0;
					} else {
						k++;
					}
				}
				System.out.println("out of one into another");
				// Fill in data element(s) information
				for (int i = 0; i < data_element_count; i++) {
					//If the data element is a report...
					if (data_element[i].id == 2) {
						//Init the data element counters
						int data_element_index = 0,
								report_length = 0;
						//Init a temporary array used to store and validate a report
						byte[] temp_data;
						
						//Store the processor ID for this data element
						int processor_id = data_element[i].payload[data_element_index++] & 0xFF;
						
						//Calculate and store the timestamp for this data element
						int timestamp = data_element[i].payload[data_element_index++] & 0xFF;
						timestamp = (timestamp << 8) | (data_element[i].payload[data_element_index++] & 0xFF);
						timestamp = (timestamp << 8) | (data_element[i].payload[data_element_index++] & 0xFF);
						timestamp = (timestamp << 8) | (data_element[i].payload[data_element_index++] & 0xFF);
						
						//Generate reports for each data element object
						int tries = 10000;
						System.out.println("about to while");
						while(data_element_index < data_element[i].payload.length && tries-- > 0){
							//Create a new report object
							Report rpt = new Report();
							//Store the reports process id from the data element
							rpt.processor_id = processor_id;					
							//Store the data source ID (sensor id, etc.)
							rpt.datagenerator_id = data_element[i].payload[data_element_index++] & 0xFF;
							//Store the data sample timestamp from the data element
							rpt.timestamp = timestamp;
							//Store the length of this report
							report_length = data_element[i].payload[data_element_index++] & 0xFF;
							
							//The WiSARDs reports contain data points that can be 
							//1-8 bytes because they get truncated if the value has leading 0's
							if(report_length == 0){ //zero length report means that an event occurred instead of a sample
								rpt.data_type = (byte)ChannelMap.TYPE_INT8;
							}else if(report_length == 1){
								rpt.data_type = (byte)ChannelMap.TYPE_INT8;
							}else if(report_length == 2){
								rpt.data_type = (byte)ChannelMap.TYPE_INT16;
							}else if(report_length > 2 && report_length <= 4){
								rpt.data_type = (byte)ChannelMap.TYPE_INT32;
							}else if(report_length > 4 && report_length <= 8 ){
								rpt.data_type = (byte)ChannelMap.TYPE_INT64;
							}else{
								rpt.data_type = (byte)ChannelMap.TYPE_UNKNOWN;
							}
							
							
							
							
							
							if(report_length == 0 && rpt.data_type == ChannelMap.TYPE_INT8){
								//Zero length packet
								rpt.data = (byte)0x01; //If type is INT8 and report length is zero we know it's a zero-length event report
								report.add(rpt);
							}else if((temp_data = Arrays.copyOfRange(data_element[i].payload, data_element_index, data_element_index + report_length)).length > 0){
								//Copy the data element payload into a new array and make sure it has size greater than 0 and equal to the expected size
								//Switch over the data type and cast the MSB into the appropriate type before storing in the report data object
								switch(rpt.data_type){							
								case(ChannelMap.TYPE_INT8):
									rpt.data = (byte)temp_data[0];
									break;
								case(ChannelMap.TYPE_INT16):
									rpt.data = (short)temp_data[0];
									break;
								case(ChannelMap.TYPE_INT32):
									rpt.data = (int)temp_data[0];
									break;	
								case(ChannelMap.TYPE_INT64):							
									rpt.data = (long)temp_data[0];
									break;
								}
								
								//If the data point is greater than a byte,
								//reconstruct the data by shifting all following bytes and OR'ing with the previous data
								for(j = 1; j < temp_data.length; j++){
									switch(rpt.data_type){
									case(ChannelMap.TYPE_INT16):									
										rpt.data = (short)(((short)rpt.data << 8) | (temp_data[j] & 0xFF));
										break;
									case(ChannelMap.TYPE_INT32):
										rpt.data = (int)(((int)rpt.data << 8) | (temp_data[j] & 0xFF));
										break;	
									case(ChannelMap.TYPE_INT64):
										rpt.data = (long)(((long)rpt.data << 8) | (temp_data[j] & 0xFF));
										break;
									}
								}
								report.add(rpt);
								
							}else{
								//We know it's a bad packet here
								rpt.data = raw_packet; //This is a convenience in case we want to fetch the raw packet later
								report.add(rpt);
							}
	
							
							data_element_index += report_length;
							
							
						}
						System.out.println("it's over");
						
					}
				}
	
			}
		}catch(ArrayIndexOutOfBoundsException e){
			throw e;
		}catch(NegativeArraySizeException e){
			throw e;
		}
	}

	/**
	 * Inner class for parsed 
	 * source and destination address of a packet
	 *
	 */
	public class NetworkPackage
	{
		public int destination = 0;
		public int source = 0;
	}
	
	/**
	 * Inner class for parsed message
	 * layer of a packet	  
	 *
	 */
	public class Message
	{
		public int id = 0;
		public int flags = 0;
		public int number = 0;
		public int address = 0;
		public int length = 0;
	}
	
	/**
	 * Inner class for parsed
	 * data elements
	 *
	 */
	public class DataElement
	{
		public int id = 0;
		public int length = 0;
		public int version = 0;
		protected byte[] payload = new byte[0];
	}
	
	/**
	 * Inner class for storing each
	 * report within a data element
	 *
	 */
	public class Report{
		public int processor_id = 0;
		public int datagenerator_id = 0;
		public int timestamp = 0;
		public Object data = null;
		public byte data_type;
	}
	

	/**
	 * Extends a DataElement array by a specified number
	 * and copies the old data into the new array
	 * 
	 * @param array
	 * @param ext
	 * @return
	 */
	private DataElement[] extend_data_element_array(DataElement[] array, int ext)
	{
		DataElement[] temp_array = array;
		array = new DataElement[array.length + ext];
		for (int i = 0; i < temp_array.length; i++)
			array[i] = temp_array[i];
		return array;
	}
	
	/**
	 * Converts a packet into its hex string
	 * representation
	 * 
	 * @param packet
	 * @return
	 */
	public static String get_hex_string(byte[] packet){
		String result = "[";
		for(int i = 0; i < packet.length; i++){
			int onum = packet[i];
			int num = (onum < 0 ? 0xFF+onum+1 : onum);										
			result += ((num<16 ? "0x0" : "0x") + Integer.toHexString(num).toUpperCase());	
			result += " ";
		}
		result += "]";
		
		return result;
	}
	
	/**
	 * Returns a hex string for the raw packet
	 * @return
	 */
	public String getRawPacketHexString(){
		String result = "";
		for(int i = 0; i < raw_packet.length; i++){
			int onum = raw_packet[i];
			int num = (onum < 0 ? 0xFF+onum+1 : onum);										
			result += ((num<16 ? "0x0" : "0x") + Integer.toHexString(num).toUpperCase());	
			result += " ";
		}		
		return result;
	}

	/**
	 * @return the message
	 */
	public Message getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(Message message) {
		this.message = message;
	}

	/**
	 * @return the data_element
	 */
	public DataElement[] getData_element() {
		return data_element;
	}

	/**
	 * @param data_element the data_element to set
	 */
	public void setData_element(DataElement[] data_element) {
		this.data_element = data_element;
	}

	/**
	 * @return the report
	 */
	public ArrayList<Report> getReport() {
		return report;
	}

	/**
	 * @param report the report to set
	 */
	public void setReport(ArrayList<Report> report) {
		this.report = report;
	}
	
	
}

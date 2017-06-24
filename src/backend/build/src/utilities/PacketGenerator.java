package utilities;

import java.nio.ByteBuffer;

public class PacketGenerator {
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @return
	 */
	public static byte[] Reset_Command_Packet(String hub_high, String hub_low, String dest_high, String dest_low){
		return Reset_Command_Packet((byte)Integer.parseInt(hub_high,16),(byte)Integer.parseInt(hub_low,16),(byte)Integer.parseInt(dest_high,16),(byte)Integer.parseInt(dest_low,16));	
	}
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @return
	 */
	public static byte[] Reset_Command_Packet(byte hub_high, byte hub_low, byte dest_high, byte dest_low){
		byte[] cmdMessage = 
	    	{
	    		//=======NET=======
	    		
	    		//DESTINATION ADDRESS [2]
	    		hub_high,//(Destination HI)
	    		hub_low,//(Destination LOW)
	    		//SOURCE ADDRESS [2]
	    		(byte)0xFE,//(Source)
	    		(byte)0xFE,
	    		
	    		//=======TRANSPORT=======
	    		
	    		//MESSAGE ID [1]
	    		(byte)0x03,//(Operational)
	    		//MESSAGE FLAGS [1]
	    		(byte)0x20,//(Single Message)
	    		//MESSAGE NUMBER [2]
	    		(byte)0x00,
	    		(byte)0x01,//(Message 1 of 1)
	    		//MESSAGE ADDRESS [2]
	    		dest_high,
	    		dest_low,//(WiSARD ID)
	    		//MESSAGE PAYLOAD LENGTH [1] Byte Position: 10
	    		(byte)0x00,
	    		
	    		//=======MESSAGE PAYLOAD=======
	    		
	    		//DATA ELEMENT ID [1]
	    		(byte)0x01, //(CMD packet)
	    		//DATA PAYLOAD LENGTH [1]
	    		(byte)0x05,
	    		//DATA ELEMENT VERSION [1]
	    		(byte)0x6E, //??
	    		
	    		//=======DATA ELEMENT PAYLOAD=======
	    		
	    		//COMMAND ID [2]
	    		(byte)0x00, //Always the CP
	    		(byte)0x0B, //Reset Command
	    	};
		//Exclude the Network layer in message length
		cmdMessage[10] = (byte) (cmdMessage.length - 4);
		return cmdMessage;
	}
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @param processor
	 * @param transducer
	 * @param command
	 * @return
	 */
	public static byte[] Valve_Command_Packet(String hub_high, String hub_low, String dest_high, String dest_low, String processor, String transducer, String command){
		return Valve_Command_Packet(
				(byte)Integer.parseInt(hub_high,16),
				(byte)Integer.parseInt(hub_low,16),
				(byte)Integer.parseInt(dest_high,16),
				(byte)Integer.parseInt(dest_low,16),
				(byte)Integer.parseInt(processor,16),
				(byte)Integer.parseInt(transducer,16),
				(byte)Integer.parseInt(command,16)
				);	
	}
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @param processor
	 * @param transducer
	 * @param command
	 * @return
	 */
	public static byte[] Valve_Command_Packet(byte hub_high, byte hub_low, byte dest_high, byte dest_low, byte processor, byte transducer, byte command){
		
	    //Variables for storing fetched data and the command		    		
	    byte[] cmdMessage = 
	    	{
	    		//=======NET=======
	    		
	    		//DESTINATION ADDRESS [2]
	    		hub_high,//(Destination HI)
	    		hub_low,//(Destination LOW)
	    		//SOURCE ADDRESS [2]
	    		(byte)0xFE,//(Source)
	    		(byte)0xFE,
	    		
	    		//=======TRANSPORT=======
	    		
	    		//MESSAGE ID [1]
	    		(byte)0x03,//(Operational)
	    		//MESSAGE FLAGS [1]
	    		(byte)0x20,//(Single Message)
	    		//MESSAGE NUMBER [2]
	    		(byte)0x00,
	    		(byte)0x01,//(Message 1 of 1)
	    		//MESSAGE ADDRESS [2]
	    		dest_high,
	    		dest_low,//(WiSARD ID)
	    		//MESSAGE PAYLOAD LENGTH [1] Byte Position: 10
	    		(byte)0x00,
	    		
	    		//=======MESSAGE PAYLOAD=======
	    		
	    		//DATA ELEMENT ID [1]
	    		(byte)0x01, //(CMD packet)
	    		//DATA PAYLOAD LENGTH [1]
	    		(byte)0x06,
	    		//DATA ELEMENT VERSION [1]
	    		(byte)0x6E, //??
	    		
	    		//=======DATA ELEMENT PAYLOAD=======
	    		
	    		//COMMAND ID [2]
	    		processor, //Processor ID
	    		transducer, //Transducer ID
	    		command //0x5B for on ( index is 16)
	    	};

		//Exclude the Network layer in message length
	    cmdMessage[10] = (byte) (cmdMessage.length - 4);
	    return cmdMessage;
	}
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @param command
	 * @return
	 */
	public static byte[] RSSI_Command_Packet(String hub_high, String hub_low, String dest_high, String dest_low, String command){
		return RSSI_Command_Packet(
				(byte)Integer.parseInt(hub_high,16),
				(byte)Integer.parseInt(hub_low,16),
				(byte)Integer.parseInt(dest_high,16),
				(byte)Integer.parseInt(dest_low,16),
				(byte)Integer.parseInt(command,16)
			);	
	}
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @param command
	 * @return
	 */
	public static byte[] RSSI_Command_Packet(byte hub_high, byte hub_low, byte dest_high, byte dest_low, byte command){
		byte[] cmdMessage = 
	    	{
	    		//=======NET=======
	    		
	    		//DESTINATION ADDRESS [2]
	    		hub_high,//(Destination HI)
	    		hub_low,//(Destination LOW)
	    		//SOURCE ADDRESS [2]
	    		(byte)0xFE,//(Source)
	    		(byte)0xFE,
	    		
	    		//=======TRANSPORT=======
	    		
	    		//MESSAGE ID [1]
	    		(byte)0x03,//(Operational)
	    		//MESSAGE FLAGS [1]
	    		(byte)0x20,//(Single Message)
	    		//MESSAGE NUMBER [2]
	    		(byte)0x00,
	    		(byte)0x01,//(Message 1 of 1)
	    		//MESSAGE ADDRESS [2]
	    		dest_high,
	    		dest_low,//(WiSARD ID)
	    		//MESSAGE PAYLOAD LENGTH [1] Byte Position: 10
	    		(byte)0x00,
	    		
	    		//=======MESSAGE PAYLOAD=======
	    		
	    		//DATA ELEMENT ID [1]
	    		(byte)0x01, //(CMD packet)
	    		//DATA PAYLOAD LENGTH [1]
	    		(byte)0x06,
	    		//DATA ELEMENT VERSION [1]
	    		(byte)0x6E, //??
	    		
	    		//=======DATA ELEMENT PAYLOAD=======
	    		
	    		//COMMAND ID [2]
	    		(byte)0x00, //Always the CP
	    		(byte)0x0A, //RSSI Command
	    		command		//On/Off
	    	};
		//Exclude the Network layer in message length
		cmdMessage[10] = (byte) (cmdMessage.length - 4);
		return cmdMessage;
	}
	
	
	/*
	DE[0] 0x01  (ID)
	DE[1] 0x0A  (Length)
	DE[2] 0x6E  (Version)
	DE[3] 0x01  (TaskID HI)
	DE[4] 0x02  (TaskID LO)
	DE[5] 0x0D  (Field ID for sampling interval)
	DE[6] 0x00  (Value XI)
	DE[7] 0x00  (Value HI)
	DE[8] 0x00  (Value MD)
	DE[9] 0x1E  (Value LO)
	*/
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @param command
	 * @return
	 */
	public static byte[] Interval_Command_Packet(String hub_high, String hub_low, String dest_high, String dest_low, String taskID_high, String taskID_low, String fieldID, String command){
		return Interval_Command_Packet(
				(byte)Integer.parseInt(hub_high,16),
				(byte)Integer.parseInt(hub_low,16),
				(byte)Integer.parseInt(dest_high,16),
				(byte)Integer.parseInt(dest_low,16),
				(byte)Integer.parseInt(taskID_high,16),
				(byte)Integer.parseInt(taskID_low,16),
				(byte)Integer.parseInt(fieldID,16),
				Integer.parseInt(command)
			);	
	}
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @param command
	 * @return
	 */
	public static byte[] Interval_Command_Packet(
			byte hub_high, 
			byte hub_low, 
			byte dest_high, 
			byte dest_low, 
			byte taskID_high,
			byte taskID_low,
			byte fieldID,
			int sampInterval){
		
		byte[] cmdMessage = 
	    	{
	    		//=======NET=======
	    		
	    		//DESTINATION ADDRESS [2]
	    		hub_high,//(Destination HI)
	    		hub_low,//(Destination LOW)
	    		//SOURCE ADDRESS [2]
	    		(byte)0xFE,//(Source)
	    		(byte)0xFE,
	    		
	    		//=======TRANSPORT=======
	    		
	    		//MESSAGE ID [1]
	    		(byte)0x03,//(Operational)
	    		//MESSAGE FLAGS [1]
	    		(byte)0x20,//(Single Message)
	    		//MESSAGE NUMBER [2]
	    		(byte)0x00,
	    		(byte)0x01,//(Message 1 of 1)
	    		//MESSAGE ADDRESS [2]
	    		dest_high,
	    		dest_low,//(WiSARD ID)
	    		//MESSAGE PAYLOAD LENGTH [1] Byte Position: 10
	    		(byte)0x00,
	    		
	    		//=======MESSAGE PAYLOAD=======
	    		
	    		//DATA ELEMENT ID [1]
	    		(byte)0x01, //(CMD packet)
	    		//DATA PAYLOAD LENGTH [1]
	    		(byte)0x00, //Data payload length, byte position 12
	    		//DATA ELEMENT VERSION [1]
	    		(byte)0x6E, //??
	    		//MODIFY TASK BLOCK ID HIGH
	    		(byte)0x00,
	    		//MODIFY TASK BLOCK ID LOW
	    		(byte)0x0F,
	    		//=======DATA ELEMENT PAYLOAD=======
	    		//TASK ID HI
	    		taskID_high,
	    		//TASK ID LO
	    		taskID_low,
	    		//Field ID (for sampling Interval)
	    		fieldID,
	    	};
		//Calculate the necessary byte array for the new sampling interval		
		byte[] interval = ByteBuffer.allocate(4).putInt(sampInterval).array();
		
		//Calculate the number of leading zeros
		int offset = 0;
		for(int i = 0; i < interval.length; i++){
			if(interval[i] == 0x00){
				offset = i+1;
			}
			else{
				break; //Break when we get a non-zero value
			}
		}
		
		//Create the final array
		byte[] intervalFinal;
		if(interval.length - offset != 0){
			intervalFinal = new byte[interval.length-offset];
			System.arraycopy(interval, offset, intervalFinal, 0,interval.length-offset);
		}
		else{
			return null;
		}

		
		byte[] cmdFinal = new byte[cmdMessage.length + intervalFinal.length];
		//Exclude the Network layer in message length
		cmdMessage[10] = (byte) (cmdFinal.length - 4);
		cmdMessage[12] = (byte) (cmdFinal.length - 11);
		System.arraycopy(cmdMessage, 0, cmdFinal, 0, cmdMessage.length);
		System.arraycopy(intervalFinal, 0, cmdFinal, cmdMessage.length, intervalFinal.length);
		
		return cmdFinal;
	}
}

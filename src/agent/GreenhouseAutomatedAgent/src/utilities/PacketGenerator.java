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
	 * @return
	 */
	public static byte[] Reset_Command_Packet_ExpDur(byte hub_high, byte hub_low, byte dest_high, byte dest_low, long expiration){
		
		/** Seconds from 1970 to 2000 - used to encode WiSARD timestamps into system time */
		int wis_expiration = (int)(expiration/1000) - 946710000;
		byte[] cmd_expiration = ByteBuffer.allocate(4).putInt(wis_expiration).array();
		
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
	    		(byte)0x00,//(10)
	    		
	    		//=======MESSAGE PAYLOAD=======
	    		
	    		//DATA ELEMENT ID [1]
	    		(byte)0x01, //(11 CMD packet)
	    		//DATA PAYLOAD LENGTH [1]
	    		(byte)0x09,//(12)
	    		//DATA ELEMENT VERSION [1]
	    		(byte)0x6E, //(13)
	    		
	    		//=======DATA ELEMENT PAYLOAD=======
	    		
	    		//COMMAND ID [2]
	    		(byte)0x00, //(14) Always the CP
	    		
	    		cmd_expiration[0],// (15)
	    		cmd_expiration[1],// (16)
	    		cmd_expiration[2],// (17)
	    		cmd_expiration[3],// (18)
	    		
	    		(byte)0x0B //(19) Reset Command
	    				
	    	};
		//Exclude the Network layer in message length
		cmdMessage[10] = (byte) (cmdMessage.length - 4);
		return cmdMessage;
	}
	
	
	public static byte[] Reset_Command_Packet_ExpDur(String hub_high, String hub_low, String dest_high, String dest_low, long expiration) throws Exception{

		return Reset_Command_Packet_ExpDur(
				(byte)Integer.parseInt(hub_high,16),
				(byte)Integer.parseInt(hub_low,16),
				(byte)Integer.parseInt(dest_high,16),
				(byte)Integer.parseInt(dest_low,16),
				expiration
				);	
		
		
	
	

	
	
}
	
	
	
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @return
	 */
	public static byte[] HID_Request_Packet(String hub_high, String hub_low, String dest_high, String dest_low){
		return HID_Request_Packet((byte)Integer.parseInt(hub_high,16),(byte)Integer.parseInt(hub_low,16),(byte)Integer.parseInt(dest_high,16),(byte)Integer.parseInt(dest_low,16));	
	}
	
	
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @return
	 */
	public static byte[] HID_Request_Packet(byte hub_high, byte hub_low, byte dest_high, byte dest_low){
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
	    		(byte)0x10, //HID Request Command
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
	 * @param processor
	 * @param transducer
	 * @param command
	 * @return
	 */
	public static byte[] Valve_Command_Packet_ExpDur(byte hub_high, byte hub_low, byte dest_high, byte dest_low, byte sp_loc, byte command, long expiration, int duration){
		
		/** Seconds from 1970 to 2000 - used to encode WiSARD timestamps into system time */
		int wis_expiration = (int)(expiration/1000) - 946710000;
		byte[] cmd_expiration = ByteBuffer.allocate(4).putInt(wis_expiration).array();
		byte[] cmd_duration = ByteBuffer.allocate(4).putInt(duration).array();
		byte offset = 2;
		
		
		if(sp_loc == 2){
			offset = 4;
		}
		else if(sp_loc == 3){
			offset = 6;
		}
		else if(sp_loc == 4){
			offset = 8;
		}
		
	    //Variables for storing fetched data and the command		    		
	    byte[] cmdMessage = 
	    	{
	    		//=======NET=======
	    		
	    		//DESTINATION ADDRESS [2]
	    		hub_high,//(Destination HI) (0)
	    		hub_low,//(Destination LOW) (1)
	    		//SOURCE ADDRESS [2]
	    		(byte)0xFE,//(Source) (2)
	    		(byte)0xFE,// (3)
	    		
	    		//=======TRANSPORT=======
	    		
	    		//MESSAGE ID [1]
	    		(byte)0x03,//(Operational) (4)
	    		//MESSAGE FLAGS [1]
	    		(byte)0x20,//(Single Message) (5)
	    		//MESSAGE NUMBER [2]
	    		(byte)0x00,// (6)
	    		(byte)0x01,//(Message 1 of 1) (7)
	    		//MESSAGE ADDRESS [2]
	    		dest_high,// (8)
	    		dest_low,//(WiSARD ID) (9)
	    		//MESSAGE PAYLOAD LENGTH [1] Byte Position: 10
	    		(byte)0x00,// (10)
	    		
	    		//=======MESSAGE PAYLOAD=======
	    		
	    		//DATA ELEMENT ID [1]
	    		(byte)0x01, //(CMD packet) (11)
	    		//DATA PAYLOAD LENGTH [1]
	    		(byte)0x0F, //(12)
	    		//DATA ELEMENT VERSION [1]
	    		(byte)0x01, //(13)
	    		
	    		//=======DATA ELEMENT PAYLOAD=======
	    		
	    		//COMMAND ID [2]
	    		sp_loc, //Processor ID (14)
	    		
	    		cmd_expiration[0],// (15)
	    		cmd_expiration[1],// (16)
	    		cmd_expiration[2],// (17)
	    		cmd_expiration[3],// (18)
	    		
	    		(byte)0x03, //Transducer ID (19)
	    		command, //0x5B or 0x00 (21)
	    		
	    		cmd_duration[0],// (22)
	    		cmd_duration[1],// (23)
	    		cmd_duration[2],// (24)
	    		cmd_duration[3], // (25)
	    		offset //(26)
	    		
	    	};

		//Exclude the Network layer in message length
	    cmdMessage[10] = (byte) (cmdMessage.length - 4);
	    return cmdMessage;
	}
	public static byte[] Valve_Command_Packet(String hub, String destination, String sp_loc, String cmd) throws Exception{

			int iHub = Integer.parseInt(hub);
			int iDest = Integer.parseInt(destination);
			int iSp_loc = -1;
			int iCmd = -1;

			if(cmd.equals("cmd_on")){
				iCmd = 0x5B;
			}
			else if(cmd.equals("cmd_off")){
				iCmd = 0x00;
			}
			else{
				throw new RuntimeException();
			}
			
			switch(sp_loc){
				case("sp_loc_1"):
					iSp_loc = 1;
				break;
				case("sp_loc_2"):
					iSp_loc = 2;
				break;
				case("sp_loc_3"):
					iSp_loc = 3;
				break;
				case("sp_loc_4"):
					iSp_loc = 4;
				break;
				default:
					throw new RuntimeException();
			}
			
			return Valve_Command_Packet(
					(byte)((iHub >> 8) & 0xFF),
					(byte)(iHub & 0xFF),
					(byte)((iDest >> 8) & 0xFF),
					(byte)(iDest & 0xFF),
					(byte)iSp_loc,
					(byte)0x03,
					(byte)iCmd
					);	
			
			
		
		

		
		
	}
	
	public static byte[] Valve_Command_Packet_ExpDur(String hub_high, String hub_low, String dest_high, String dest_low, String sp_loc, String cmd, long expiration, int duration) throws Exception{

		int iSp_loc = -1;
		int iCmd = -1;

		
		if(cmd.equals("cmd_on")){
			iCmd = 0x5B;
		}
		else if(cmd.equals("cmd_off")){
			iCmd = 0x00;
			duration = 0;
		}
		else{
			throw new RuntimeException();
		}
		
		switch(sp_loc){
			case("sp_loc_1"):
				iSp_loc = 1;
			break;
			case("sp_loc_2"):
				iSp_loc = 2;
			break;
			case("sp_loc_3"):
				iSp_loc = 3;
			break;
			case("sp_loc_4"):
				iSp_loc = 4;
			break;
			default:
				throw new RuntimeException();
		}
		//Valve_Command_Packet_ExpDur(byte hub_high, byte hub_low, byte dest_high, byte dest_low, byte sp_loc, byte command, long expiration, int duration)
		return Valve_Command_Packet_ExpDur(
				(byte)Integer.parseInt(hub_high,16),
				(byte)Integer.parseInt(hub_low,16),
				(byte)Integer.parseInt(dest_high,16),
				(byte)Integer.parseInt(dest_low,16),
				(byte)iSp_loc,
				(byte)iCmd,
				expiration,
				duration
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
	
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @param command
	 * @return
	 */
	public static byte[] RSSI_Command_Packet_Exp(byte hub_high, byte hub_low, byte dest_high, byte dest_low, byte command, long expiration){
		/** Seconds from 1970 to 2000 - used to encode WiSARD timestamps into system time */
		int wis_expiration = (int)(expiration/1000) - 946710000;
		byte[] cmd_expiration = ByteBuffer.allocate(4).putInt(wis_expiration).array();
		
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
	    		
	    		cmd_expiration[0],// (15)
	    		cmd_expiration[1],// (16)
	    		cmd_expiration[2],// (17)
	    		cmd_expiration[3],// (18)
	    		
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
	 * @param taskID_high
	 * @param taskID_low
	 * @param fieldID
	 * @param command
	 * @param expiration
	 * @return
	 */
	public static byte[] Interval_Command_Packet_Exp(String hub_high, String hub_low, String dest_high, String dest_low, String taskID_high, String taskID_low, String fieldID, String command, long expiration){
		return Interval_Command_Packet_Exp(
				(byte)Integer.parseInt(hub_high,16),
				(byte)Integer.parseInt(hub_low,16),
				(byte)Integer.parseInt(dest_high,16),
				(byte)Integer.parseInt(dest_low,16),
				(byte)Integer.parseInt(taskID_high,16),
				(byte)Integer.parseInt(taskID_low,16),
				(byte)Integer.parseInt(fieldID,16),
				Integer.parseInt(command),
				expiration
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
	    		fieldID
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
	
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @param taskID_high
	 * @param taskID_low
	 * @param fieldID
	 * @param sampInterval
	 * @param expiration
	 * @return
	 */
	public static byte[] Interval_Command_Packet_Exp(
			byte hub_high, 
			byte hub_low, 
			byte dest_high, 
			byte dest_low, 
			byte taskID_high,
			byte taskID_low,
			byte fieldID,
			int sampInterval,
			long expiration){
		
		
		int wis_expiration = (int)(expiration/1000) - 946710000;
		byte[] cmd_expiration = ByteBuffer.allocate(4).putInt(wis_expiration).array();

		
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
	    		(byte)0x00,//processor id
	    		cmd_expiration[0],// (15)
	    		cmd_expiration[1],// (16)
	    		cmd_expiration[2],// (17)
	    		cmd_expiration[3],// (18)
	    		//MODIFY TASK BLOCK ID LOW
	    		(byte)0x0F,
	    		
	    		//=======DATA ELEMENT PAYLOAD=======
	    		//TASK ID HI
	    		taskID_high,
	    		//TASK ID LO
	    		taskID_low,
	    		//Field ID (for sampling Interval)
	    		fieldID
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

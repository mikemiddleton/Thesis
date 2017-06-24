package experiments;
/*
This experiment monitors a specified soil moisture channel and tries to mirror that value to another. An allowable range is set that determines whether commands need to be sent. This initial version does not check for valve status.
 */

import helpers.ExperimentParameterObject;
import interfaces.ControlInterface;

import java.io.PrintWriter;
import java.io.StringWriter;

import utilities.ControlClass;
import utilities.DataGeneratorCodec;
import utilities.Experiment;
import utilities.PacketGenerator;
import utilities.Rule;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
/**
 * @author jdk85
 *
 */
public class BenchtopMirrorDemo extends ControlClass implements java.io.Serializable  { 


	

	private static final long serialVersionUID = 1194186383934964195L;
	private static String confirmation = "Last Edit: 12/04/2015 - 05:21 PM";
	private int attempts = 0, cmd_timeout = 1000*60*3;
	private long time_sent = 0;
	/**
	 * DemoExp Constructor
	 * The constructor simply calls the parent constructor
	 * @param expID
	 */
	public BenchtopMirrorDemo(String expID, String expDesc, String sinkServerAddress,String gardenServerName,boolean init) {
		super(expID,expDesc,sinkServerAddress,gardenServerName,init);
	}
	/**
	 * 
	 * @see ExperimentParameterObject
	 * @see ControlClass
	 * @see Experiment
	 */
	@Override
	public void initParams(){
		super.initParams();
		ExperimentParameterObject confMessage = new ExperimentParameterObject("confMessage",confirmation,"Confirmation Message","ExperimentParameters","The message parameter is used to verify that the correct version of the Java source code was uploaded",false,false);
		
		
		
		ExperimentParameterObject allowable_sm_threshold = new ExperimentParameterObject(
				"allowable_sm_threshold",
				"10",
				"Allowable +/- Soil Moisture Threhold",
				"Experiment Parameters",
				"This parameter sets the allowable range (+/-) value for the control to generate a command. If the sensor reading on the mirror sensor drops below the sensor reading on the original sensor by the range value, a 'Valve ON' command is generated and sent. Otherwise, if the mirror sensor rises above the original sensor value by the range value, then a 'Valve OFF' command is sent.",
				true,
				false);
		ExperimentParameterObject valve_status_channel = new ExperimentParameterObject(
				"valve_status_channel",
				"exp249a-04_channelized/wisard_18/mod_1/stream_5",
				"Valve Status Channel",
				"Experiment Parameters",
				"The address of the valve status source channel.",
				true,
				true);
		ExperimentParameterObject valveStatus_On = new ExperimentParameterObject(
				"valveStatus_On",
				"0x5B",
				"Valve Status ON Value",
				"Experiment Parameters",
				"The on value of the valve status channel.",
				true,
				true);
		ExperimentParameterObject valveStatus_Off = new ExperimentParameterObject(
				"valveStatus_Off",
				"0x00",
				"Valve Status OFF Value",
				"Experiment Parameters",
				"The off value of the valve status channel.",
				true,
				true);
		
		ExperimentParameterObject commandView_On = new ExperimentParameterObject(
				"commandView_On",
				"1",
				"Command Status ON",
				"Experiment Parameters",
				"When an ON command is generated, this value is sent to the 'Command View' channel. This is mostly used for debugging and verification and has no impact on the actual control.",
				true,
				false);
		
		ExperimentParameterObject commandView_Off = new ExperimentParameterObject(
				"commandView_Off",
				"0",
				"Command Status OFF",
				"Experiment Parameters",
				"When an OFF command is generated, this value is sent to the 'Command View' channel. This is mostly used for debugging and verification and has no impact on the actual control.",
				true,
				false);
		
		ExperimentParameterObject wisard_channel_orig = new ExperimentParameterObject(
				"wisard_channel_orig",
				"exp249a-04_channelized/wisard_3/mod_1/stream_1",
				"WiSARD Channel Original",
				"Experiment Parameters",
				"This is the channel that wisard_18 is trying to mimic.",
				true,
				true);
		

		ExperimentParameterObject wisard_channel_mirror = new ExperimentParameterObject(
				"wisard_channel_mirror",
				"exp249a-04_channelized/wisard_18/mod_1/stream_1",
				"WiSARD Channel Mirror",
				"Experiment Parameters",
				"This is the channel that mimics wisard_3.",
				true,
				true);
				
		ExperimentParameterObject destination_address_high_byte = new ExperimentParameterObject(
				"destination_address_high_byte",
				"0x00",
				"Destination Address (HI Byte)",
				"Experiment Parameters",
				"This is the high byte value for the WiSARD destination ID. This address is used to declare the destination address when creating command messages for the WiSARDNetwork. It should be written as hex and may include the leading '0x'.",
				true,
				true);
		
		ExperimentParameterObject destination_address_low_byte = new ExperimentParameterObject(
				"destination_address_low_byte",
				"0x12",
				"Destination Address (LOW Byte)",
				"Experiment Parameters",
				"This is the low byte value for the WiSARD destination ID. This address is used to declare the destination address when creating command messages for the WiSARDNetwork. It should be written as hex and may include the leading '0x'.",
				true,
				true);
		
		ExperimentParameterObject hub_address_high_byte = new ExperimentParameterObject(
				"hub_address_high_byte",
				"0x00",
				"Hub Address (HI Byte)",
				"Experiment Parameters",
				"This is the high byte value for the WiSARD hub ID. It should be written as hex and may include the leading '0x'.",
				true,
				true);
		
		ExperimentParameterObject hub_address_low_byte = new ExperimentParameterObject(
				"hub_address_low_byte",
				"0x00",
				"Hub Address (LOW Byte)",
				"Experiment Parameters",
				"This is the low byte value for the WiSARD hub ID. It should be written as hex and may include the leading '0x'.",
				true,
				true);
		
		ExperimentParameterObject processor_id = new ExperimentParameterObject(
				"processor_id",
				"0x01",
				"Processor ID",
				"Experiment Parameters",
				"This is the processor ID that is to receive the valve command. It should be written as hex and may include the leading '0x'.",
				true,
				true);
		
		getParameters().add(confMessage);
		
		getParameters().add(allowable_sm_threshold);
		
		getParameters().add(valve_status_channel);
		getParameters().add(valveStatus_On);
		getParameters().add(valveStatus_Off);
		
		getParameters().add(commandView_On);
		getParameters().add(commandView_Off);
		
		getParameters().add(wisard_channel_orig);
		getParameters().add(wisard_channel_mirror);
		
		getParameters().add(hub_address_high_byte);
		getParameters().add(hub_address_low_byte);
		
		getParameters().add(destination_address_high_byte);
		getParameters().add(destination_address_low_byte);
		
		getParameters().add(processor_id);
	}
	/**
	 * @see ControlInterface
	 */
	@Override
	public void initRules() {
		super.initRules();
		getRules().add(new Rule("Debug Logging","If running, experiment will write to the local log file",true,new String[0]));	
		
		
		
	}
	
	/**
	 * getMessage is just used to provide a sanity check when uploading
	 * control programs to segaWeb. The message will be displayed in the edit experiment menu
	 * and can be used to verify that the correct version of a program was uploaded.
	 */
	@Override
	public String getMessage() {
		return confirmation;
		
	}
	/**
	 * This override is pretty important. It overrides the parent createRBNBControl class and returns an inner
	 * class from this instance instead of an inner RBNBControl class from the parent ControlClass
	 */
	@Override
	protected RBNBControl createRBNBControl(){
		return new RBNBControl();
	}

		/**
		 * This class only provides the logic for experiment specific control and 
		 * then relies on the parent class RBNBControl to provide all base level logic
		 * such as RBNB connections, getters and setters for the channel maps needed
		 * by RBNBControl, as well as general command packet structuring.
		 * @author jdk85
		 *
		 */
		public class RBNBControl extends ControlClass.RBNBControl{
			@Override
			public void run(){
	    	try{ 
	    			if(connect()) updateParameter("runningStatus",runningStr); 
		    		else{		    			
		    			updateParameter("runningStatus",notRunningStr); 
		    			log.write("Unable to connect...");
		    		}   	
	    			
		    		//Command Packet parameters
					byte hub_high = getHexParameterAsByte("hub_address_high_byte");
		    		byte hub_low = getHexParameterAsByte("hub_address_low_byte");
		    		
					byte dest_high = getHexParameterAsByte("destination_address_high_byte");
		    		byte dest_low = getHexParameterAsByte("destination_address_low_byte");
		    		
		    		byte processor = getHexParameterAsByte("processor_id");
		    		
		    		//Create the command packet
			        byte[] cmdMessage = PacketGenerator.Valve_Command_Packet(hub_high, hub_low, dest_high, dest_low, processor,(byte)3,(byte)0);
			        
			        //Initialize sensor reading values
			        double STM_SM_Orig_Val = -1, STM_SM_Mirror_Val = -1;
			        double STM_SM_Orig_Epsilon = -1 ,STM_SM_Mirror_Epsilon = -1;
			        int Valve_Status_Val = -1;
			        
			      //Create a channelmap for received data 
			        ChannelMap rMap = new ChannelMap();
			        int timeout = 10000; //10 seconds
					int origSMIndex = -1, mirrorSMIndex = -1, valveStatusIndex = -1;

        			
			        //Get the last known valve status			        
			        ChannelMap valveChannelMap = new ChannelMap();
			        valveChannelMap.Add(getParameterToStringById("valve_status_channel"));
			        ChannelMap valveMap = new ChannelMap();
			        getSink().Request(valveChannelMap, 0, 0, "newest");			        
			        valveMap = getSink().Fetch(timeout);
			        
			        
			        if(!valveMap.GetIfFetchTimedOut() && valveMap.NumberOfChannels() > 0){
			        	if((valveStatusIndex = valveMap.GetIndex(getParameterToStringById("valve_status_channel"))) != -1){							            							            		
		            		Object valve_temp = DataGeneratorCodec.decodeValuePair(valveMap.GetTimes(valveStatusIndex)[0],valveMap.GetDataAsByteArray(valveStatusIndex)[0]).getSample_data();
			      
		            		if(valve_temp instanceof java.lang.Byte){
		            			Valve_Status_Val = (int)(((byte)valve_temp) & 0xFF);
							}
		            	
		            	}
			        }
			        
			        log.write("\tINIT VALVE STATUS: " + Valve_Status_Val); // if it's still -1 we don't know the status
			        
			        //Initialize status channel array
			        byte[] cmdSent = new byte[1];
			        
			        //Add the channels that we need to monitor
			        ChannelMap aMap = new ChannelMap();
			        aMap.Add(getParameterToStringById("wisard_channel_orig"));
			        aMap.Add(getParameterToStringById("wisard_channel_mirror"));
			        aMap.Add(getParameterToStringById("valve_status_channel"));
			        
			        
				        /*
				         * Main Loop - continually checks sink channels for soil moisture status values, sends cmds as needed
				         */			        
				        while(!Thread.currentThread().isInterrupted())
				        {   
						        	//Fetches new data
					        			boolean sendCmd = false;
					        			getSink().Request(aMap, System.currentTimeMillis()/1000.0 - timeout/1000, timeout/1000, "absolute");				
					    				rMap = getSink().Fetch(timeout);
					        			Object og_temp,mirror_temp,valve_temp;
					        			
				
					        			
					        			
										if(!rMap.GetIfFetchTimedOut() && rMap.NumberOfChannels() > 0){
																						
							            	if((origSMIndex = rMap.GetIndex(getParameterToStringById("wisard_channel_orig"))) != -1){
							            		og_temp = DataGeneratorCodec.decodeValuePair(rMap.GetTimes(origSMIndex)[0],rMap.GetDataAsByteArray(origSMIndex)[0]).getSample_data();
							            		if(og_temp instanceof java.lang.Byte){
													STM_SM_Orig_Epsilon = (double)(((byte)og_temp) & 0xFF)/50.;
												}
							            		else if(og_temp instanceof java.lang.Short){
													STM_SM_Orig_Epsilon = (double)(((short)og_temp) & 0xFFFF)/50.;
												}												
												else if(og_temp instanceof java.lang.Integer){
													STM_SM_Orig_Epsilon = (double)(((int)og_temp) & 0xFF)/50.;
												}
												else if(og_temp instanceof java.lang.Long){
													STM_SM_Orig_Epsilon = (double)(((long)og_temp) & 0xFFFFFFFF)/50.;
												}
												else{
													STM_SM_Orig_Epsilon = (double)og_temp/50.;
												}
												
//												STM_SM_Orig_Val = 4.3*Math.pow(10, -6)*Math.pow(STM_SM_Orig_Epsilon,3)
//														- 5.5*Math.pow(10, -4)*Math.pow(STM_SM_Orig_Epsilon,2)
//														+ 2.92*Math.pow(10,-2)*STM_SM_Orig_Epsilon
//														- 5.3*Math.pow(10,-2);		
												STM_SM_Orig_Val = STM_SM_Orig_Epsilon;
							            	}
							            	
							            	if((mirrorSMIndex = rMap.GetIndex(getParameterToStringById("wisard_channel_mirror"))) != -1){							            		
							            		mirror_temp = DataGeneratorCodec.decodeValuePair(rMap.GetTimes(mirrorSMIndex)[0],rMap.GetDataAsByteArray(mirrorSMIndex)[0]).getSample_data();
												
												if(mirror_temp instanceof java.lang.Byte){
													STM_SM_Mirror_Epsilon = (double)(((byte)mirror_temp) & 0xFF)/50.;
												}
												else if(mirror_temp instanceof java.lang.Short){
													STM_SM_Mirror_Epsilon = (double)(((short)mirror_temp) & 0xFFFF)/50.;
												}
												else if(mirror_temp instanceof java.lang.Integer){
													STM_SM_Mirror_Epsilon = (double)(((int)mirror_temp) & 0xFFFF)/50.;
												}
												else if(mirror_temp instanceof java.lang.Long){
													STM_SM_Mirror_Epsilon = (double)(((long)mirror_temp) & 0xFFFFFFFF)/50.;
												}
												else{
													STM_SM_Mirror_Epsilon = (double)mirror_temp/50.;
												}
//												STM_SM_Orig_Val = 4.3*Math.pow(10, -6)*Math.pow(STM_SM_Mirror_Epsilon,3)
//														- 5.5*Math.pow(10, -4)*Math.pow(STM_SM_Mirror_Epsilon,2)
//														+ 2.92*Math.pow(10,-2)*STM_SM_Mirror_Epsilon
//														- 5.3*Math.pow(10,-2);
												
												STM_SM_Mirror_Val = STM_SM_Mirror_Epsilon;
							            	}
							            	
//							            	if((valveStatusIndex = rMap.GetIndex(getParameterToStringById("valve_status_channel"))) != -1){							            							            		
//							            		valve_temp = DataGeneratorCodec.decodeValuePair(rMap.GetTimes(valveStatusIndex)[0],rMap.GetDataAsByteArray(valveStatusIndex)[0]).getSample_data();
//								      
//							            		if(valve_temp instanceof java.lang.Byte){
//							            			Valve_Status_Val = (int)(((byte)valve_temp) & 0xFF);
//												}
//							            	
//							            	}
							            	
							            	//Get the last known valve status
							            	valveChannelMap = new ChannelMap();
									        valveChannelMap.Add(getParameterToStringById("valve_status_channel"));
									        
							            	getSink().Request(valveChannelMap, 0., 0., "newest");			        
									        valveMap = getSink().Fetch(timeout);
									        
									        
									        if(!valveMap.GetIfFetchTimedOut() && valveMap.NumberOfChannels() > 0){
									        	if((valveStatusIndex = valveMap.GetIndex(getParameterToStringById("valve_status_channel"))) != -1){							            							            		
								            		valve_temp = DataGeneratorCodec.decodeValuePair(valveMap.GetTimes(valveStatusIndex)[0],valveMap.GetDataAsByteArray(valveStatusIndex)[0]).getSample_data();
									      
								            		if(valve_temp instanceof java.lang.Byte){
								            			Valve_Status_Val = (int)(((byte)valve_temp) & 0xFF);
													}
								            	
								            	}
									        }
							            	//check if feasible first (81.88 should be max val)
						            	    if((STM_SM_Orig_Epsilon >= 1 && STM_SM_Orig_Epsilon < 82) && (STM_SM_Mirror_Epsilon >= 1 && STM_SM_Mirror_Epsilon < 82)){
						            	    	
							            	    if(STM_SM_Mirror_Val >= (STM_SM_Orig_Val + Integer.parseInt((String)getParameterById("allowable_sm_threshold").getValue()))){
							            	    	if(Valve_Status_Val != (byte)Integer.parseInt(((String)getParameterById("valveStatus_Off").getValue()).replace("0x", ""),16)){
							            	    		cmdMessage[cmdMessage.length - 1] = (byte)0x00; //Valve OFF            		
									            		//if(getRule("Debug Logging").getIsRunning())log.write("Valve OFF Cmd");
									        			cmdSent[0] = Byte.parseByte((String) getParameterById("commandView_Off").getValue());
									            		sendCmd = true;	
									            		if(getRule("Debug Logging").getIsRunning()){
									            			log.write("-----OFF Command Sent-----");
									            			log.write("\tSM Orig Value: " + STM_SM_Orig_Val);
									            			log.write("\tSM Mirror Value: " + STM_SM_Mirror_Val);
										            		log.write("\tValve Status Value: " + Valve_Status_Val);
									            		}
							            	    	}
								            	}
						            	    
							            	
								            	//if soil moisture is less than the lower threshold and the valve is currently off
								            	//turn on the valve
							
							            	    else if(STM_SM_Mirror_Val <= (STM_SM_Orig_Val - Integer.parseInt((String)getParameterById("allowable_sm_threshold").getValue()))){
							            	    	if(Valve_Status_Val != (byte)Integer.parseInt(((String)getParameterById("valveStatus_On").getValue()).replace("0x", ""),16)){
							            	    		cmdMessage[cmdMessage.length - 1] = (byte)0x5B; //Valve ON
									            		//if(getRule("Debug Logging").getIsRunning())log.write("Valve ON Cmd");
									            		cmdSent[0] = Byte.parseByte((String) getParameterById("commandView_On").getValue());
									            		sendCmd = true;
									            		if(getRule("Debug Logging").getIsRunning()){
									            			log.write("-----ON Command Sent-----");		
									            			log.write("\tSM Orig Value: " + STM_SM_Orig_Val);
									            			log.write("\tSM Mirror Value: " + STM_SM_Mirror_Val);
										            		log.write("\tValve Status Value: " + Valve_Status_Val);
									            		}
							            	    	}
								            	}
							            	    //if sm is within range and valve is on
							            	    else if(STM_SM_Mirror_Val <= (STM_SM_Orig_Val + Integer.parseInt((String)getParameterById("allowable_sm_threshold").getValue())) &&
							            	    		STM_SM_Mirror_Val >= (STM_SM_Orig_Val - Integer.parseInt((String)getParameterById("allowable_sm_threshold").getValue()))
							            	    		){
							            	    	if(Valve_Status_Val == (byte)Integer.parseInt(((String)getParameterById("valveStatus_On").getValue()).replace("0x", ""),16)){
							            	    		cmdMessage[cmdMessage.length - 1] = (byte)0x5B; //Valve ON
									            		//if(getRule("Debug Logging").getIsRunning())log.write("Valve ON Cmd");
									            		cmdMessage[cmdMessage.length - 1] = (byte)0x00; //Valve OFF            		
									            		//if(getRule("Debug Logging").getIsRunning())log.write("Valve OFF Cmd");
									        			cmdSent[0] = Byte.parseByte((String) getParameterById("commandView_Off").getValue());
									            		sendCmd = true;	
									            		if(getRule("Debug Logging").getIsRunning()){
									            			log.write("-----OFF Command Sent-----");
									            			log.write("\tSM Orig Value: " + STM_SM_Orig_Val);
									            			log.write("\tSM Mirror Value: " + STM_SM_Mirror_Val);
										            		log.write("\tValve Status Value: " + Valve_Status_Val);
									            		}
							            	    	}
								            	}
							            	    
							            	    
							            	    	
							            	    
						            	    }
						            	    else{
						            	    	if(getRule("Debug Logging").getIsRunning()){
						            	    		log.write("OUT OF RANGE");
						            	    		log.write("\tSTM_SM_Orig_Epsilon: " + STM_SM_Orig_Epsilon);
							            			log.write("\tSTM_SM_Mirror_Epsilon: " + STM_SM_Mirror_Epsilon);
								            		log.write("\tValve Status Value: " + Valve_Status_Val);
								            		
						            	    		log.write("\tSM Orig Value: " + STM_SM_Orig_Val);
							            			log.write("\tSM Mirror Value: " + STM_SM_Mirror_Val);
								            		log.write("\tValve Status Value: " + Valve_Status_Val);
						            	    	}
						            	    }
							            		
								            
							            	
							            	//command is only sent if it needs to be... this allows the loop to iterate through
							            	//multiple rules and append commands to a single packet if the experiment allows for this
							            	if(sendCmd && System.currentTimeMillis() - time_sent > cmd_timeout){						       
										        //Ignore priority for now
												addCrcAndFlushToGarden(getParameterToStringById("gardenServerName"), cmdMessage,System.currentTimeMillis() + 3*60*1000,-1);					        
										        postStatusAsByte(cmdSent);
										        time_sent = System.currentTimeMillis();
							            	}
										}
										Thread.sleep(timeout);
				        
	    		}
	    	}catch(SAPIException e){
				if(validateReconnectAttempt(e.getLocalizedMessage(),attempts++)){
					reconnect();
				}
				else{
					updateParameter("runningStatus",notRunningStr);
					log.write("Reconnect validation failed, experiment operation will not be resumed");
				}
				
			}catch(Exception e){
				updateParameter("runningStatus",notRunningStr);
	    		StringWriter err = new StringWriter();
	        	e.printStackTrace(new PrintWriter(err));
	        	if(getRule("Debug Logging").getIsRunning()){
	        		log.write("Unhandled Exception, experiment operation will not be resumed");
	        		log.write(err);
	        	}
	        	
	        }
			}
		}
}
			


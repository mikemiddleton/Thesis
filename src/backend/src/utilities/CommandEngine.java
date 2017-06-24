package utilities;

import utilities.ArrayUtilities;
import utilities.CRC;
import utilities.DataGeneratorCodec;
import utilities.GardenServer;
import utilities.PacketGenerator;
import utilities.SegaLogger;

import helpers.SampleTimestampPackage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

/*
 * class definition for Command Engine objects
 */
public class CommandEngine {
	// member variables
	
	/*
	 * CommandEngine constructor method
	 */
	public CommandEngine(){
		// constructor code goes here
	}
	
	
	/*
	 * sink runner - creates a sink to pull valve status from RBNB
	 */
	public void sink_runner() throws InterruptedException, SAPIException, IOException{
		long start_time;
		// The size of chunks to fetch in hours
		int chunk_size = 24;
		// The chunk_size in seconds
		long duration = 1000 * 3600 * chunk_size;
		String current_channel_name;
		
		//RBNB timestamp variable used to fetch sequential chunks of data
		long fetch_start;
		long sleepTime = 30000;
		
		//File file = new File("ValveStatus_2h.csv");
		//file.createNewFile();
		
		//Create the filewriter object
		//FileWriter writer = new FileWriter(file);
		//writer.write("WiSARD, Module, Valve Status, Time, Date-Time \n");
		//writer.flush();
		//writer.close();
		
		//Create the channel map
		ChannelMap cMap = new ChannelMap();
		ChannelMap rMap = new ChannelMap();
		cMap.Add("exp249a-12_channelized/wisard_600/mod_1/stream_5");
		cMap.Add("exp249a-12_channelized/wisard_600/mod_2/stream_5");
		cMap.Add("exp249a-12_channelized/wisard_600/mod_3/stream_5");
		cMap.Add("exp249a-12_channelized/wisard_600/mod_4/stream_5");
		
		// Sink
		Sink snk = new Sink();
		snk.OpenRBNBConnection("romer0.cefns.nau.edu:3333", "valveStatusSnk");
		
		// Handle graceful shutdown
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		
		// The beginning of the request
		start_time = System.currentTimeMillis();
		fetch_start = start_time - (3600 * 1000 * 5);
		
		while(true){
			if(first_run){
				fetch_start += 60000;
			}
			else{
				fetch_start = last_fetch_timestamp;
				duration = 2 * (System.currentTimeMillis() - fetch_start);
			}
			snk.Request(cMap), fetch_start/1000., duration/1000., "absolute");
			
			rMap = snk.Fetch(-1);
			
			System.out.println("\tLast Fetch Start Time: " + sdf.format(new Date(fetch_start)));
			
			if(!rMap.GetIfFetchTimedOut() && rMap.NumberOfChannels() > 0){
				System.out.println("Received Data ");
				// Loop through all the channels
				for(int i = 0; i < rMap.NumberOfChannels(); i++){
					current_channel_name = rMap.GetName(i);
					System.out.println(current_channel_name);
				}
				
				process_valve_status(rMap, first_run, file);
				
				first_run = false;
				sleepTime = 30000;
			}
			Thread.sleep(sleepTime);
		}
	}
	
	/*
	 * command runner thread - sends valve commands to gs
	 */
	public void command_runner() throws InterruptedException{
		int duration = 7200;
		short mod;
		
		GardenServer lab_gs = new GardenServer("exp249a-12.egr.nau.edu:3333", "exp249a-12", ".", true, "dontcare");
		lab_gs.execute();
		
		while(!command_thread.isInterrupted()){
			try{
				log = new SegaLogger("./CommandEngine.txt");
			}
			catch(Exception e){
				System.out.println("Can't create log");
				System.exit(-1);
			}
			
			for(mod = 1; mod < 5; mod++){
				byte[] packet = PacketGenerator.Valve_Command_Packet_ExpDur(
						(byte) 0, (byte) 0, (byte) 02, (byte) 88, (byte) mod, (byte) 0x5B, (System.currentTimeMillis() + (20 * 60000)), duration);
				try{
					addCrcAndFlushToGarden(lab_gs, packet);
				}
				catch(SAPIException e){
					System.out.println("Can't flush command");
				}
				Thread.sleep(1000);
			}
			Thread.sleep((duration * 1000) + 600000);
		}
		lab_gs.shutdown();
	}
	
	/*
	 * calculates and adds crc to a command packet and then flushes the command packet to the garden server
	 */
	public boolean addCrcAndFlushToGarden(GardenServer gs, byte[] message) throws SAPIException{
		// Set an expiration date of 3 minutes from now
		long expiration_date = System.currentTimeMillis() + 3 * 60 * 1000;
		// Calculate the value for the CRC
		int crc = CRC.compute_crc(ArrayUtilities.convert_to_int_array(message));
		// Parse out the high byte
		byte crc_hi_byte = (byte) (crc >> 8);
		// Parse out the low byte
		byte crc_low_byte = (byte) (crc & 0xFF);
		
		// Create new packet with extra 2 bytes for the CRC
		byte[] cmdFinal = new byte[message.length + 2];
		
		// Populate the new packet with the original packet contents
		for(int i = 0; i < message.length; i++){
			cmdFinal[i] = message[i];
		}
		
		// Add the CRC to the new packet
		cmdFinal[cmdFinal.length - 2] = crc_hi_byte;
		cmdFinal[cmdFinal.length - 1] = crc_low_byte;
		System.out.println("\t*COMMAND " + ArrayUtilities.get_hex_string(cmdFinal));
		log.write("\t*COMMAND " + ArrayUtilities.get_hex_string(cmdFinal));
		
		if(gs != null){
			// Flush to the appropriate GardenServer object
			gs.insert_command(cmdFinal, expiration_date, 1);
			return true;
		}
		
		return false;
	}
	
	/*
	 * flushes a command to the specified garden server object
	 */
	public boolean flushCommandToGardenServer(GardenServer gs, byte[] cmd, long expiration_date, int priority){
		if(gs != null){
			gs.insert_command(cmd, expiration_date, priority);
			return true;
		}
		return false
	}
	
	/*
	 * \brief Shutdown gracefully
	 */
	public class ShutdownHook extends Thread{
		@Override
		public void run(){
			System.out.println("\r\nShutdown hook  activated...");
		}
	}
	
}

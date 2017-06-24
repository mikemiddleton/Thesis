package servlets;

import helpers.KeyValueObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import utilities.ArrayUtilities;
import utilities.CRC;
import utilities.GardenServer;
import utilities.PacketGenerator;
import utilities.SegaLogger;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;


/**
 * Servlet implementation class WisardBuildServlet
 */
@WebServlet("/CmdGeneratorServlet")
public class CmdGeneratorServlet extends HttpServlet {
	/** Required by java.io.Serializable */
	private static final long serialVersionUID = 5199097670843989396L;
	/** Log object */
	private static SegaLogger log;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CmdGeneratorServlet() {
        super();
        try {
			log = new SegaLogger("/usr/share/tomcat7/segalogs/CmdGeneratorServlet.txt");
		} catch (IOException e) {
			System.out.println("ERROR: Cannot create logger /usr/share/tomcat7/segalogs/CmdGeneratorServlet.txt");
			e.printStackTrace();
		}
    }

	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getSession().removeAttribute("cmd_result");
	
		String redirect,gardenServerName,cmd_type,hub_high,hub_low,dest_high,dest_low,processor,transducer,command,action,taskID_high,taskID_low,fieldID;
		redirect=gardenServerName=hub_high=hub_low=processor=transducer=command=action=taskID_high=taskID_low=fieldID=null;
		dest_high=dest_low="--";
		byte[] cmd_pkt = null;
		//Fetch and set all the params passed to this function
		ArrayList<KeyValueObject> params = new ArrayList<KeyValueObject>();
		Enumeration<String> paramNames = request.getParameterNames();
		String tempName;
		Object paramValue;
		while(paramNames.hasMoreElements()){
			tempName = paramNames.nextElement();
			paramValue = request.getParameter(tempName);
			if(paramValue instanceof String){
				params.add(new KeyValueObject(tempName,paramValue.toString()));
			}
		}
		request.getSession().setAttribute("params", params);
		
		if((action = request.getParameter("action")) != null){
			if(action.equals("getGardenServerList")){				
				request.getSession().setAttribute("gardenServers",SegaExperimentServlet.getGardenServers());
				if(request.getSession().getAttribute("gardenServers") == null){
					request.getSession().removeAttribute("gardenServers");
				}
				request.getSession().setAttribute("refreshGardenServers","false");
			}
		}
		else if((gardenServerName = request.getParameter("gardenServerSelect")) != null){
			//Set the selected garden server name so that when the page reloads it will be selected
			request.getSession().setAttribute("selectedGardenServer", gardenServerName);
			try{
				if( (hub_high = request.getParameter("hub_high")) != null &&
					(hub_low = request.getParameter("hub_low")) != null &&
					(dest_high = request.getParameter("dest_high")) != null &&
					(dest_low = request.getParameter("dest_low")) != null){
					
					if((cmd_type = request.getParameter("cmd_type")) != null){
						request.getSession().setAttribute("selectedCmdType", cmd_type);
						if(cmd_type.equals("reset")){
							cmd_pkt = PacketGenerator.Reset_Command_Packet(hub_high, hub_low, dest_high, dest_low);
						}
						else{
							if((command = request.getParameter("command")) != null){
								if(cmd_type.equals("interval")){
									if((taskID_high = request.getParameter("taskID_high")) != null &&
											(taskID_low = request.getParameter("taskID_low")) != null &&
											(fieldID = request.getParameter("fieldID")) != null){					
										cmd_pkt = PacketGenerator.Interval_Command_Packet(hub_high, hub_low, dest_high, dest_low,taskID_high,taskID_low,fieldID,command);
									}
								}
								else if(cmd_type.equals("rssi")){
									cmd_pkt = PacketGenerator.RSSI_Command_Packet(hub_high, hub_low, dest_high, dest_low, command);
								}
								else if(cmd_type.equals("valve")){
									if((processor = request.getParameter("processor")) != null &&
											(transducer = request.getParameter("transducer")) != null){
										cmd_pkt = PacketGenerator.Valve_Command_Packet(hub_high, hub_low, dest_high, dest_low, processor, transducer, command);
									}
								}
							}
							
						}
					}
				 
				}
				else
					request.getSession().setAttribute("cmd_result","Error creating packet - check user input");
				
				if(cmd_pkt != null){
					try{
						//Set an expiration date of 3 minutes from time now
						long expiration_date = System.currentTimeMillis() + 3*60*1000;
						//Ignore priority for now
						if(addCrcAndFlushToGarden(gardenServerName,cmd_pkt,expiration_date,-1)){					
	//						addCrcAndFlush(gardenServerName,b);
							log.write("Command sent to 0x"+dest_high + dest_low + " @ " + new Date(System.currentTimeMillis()).toString());
							request.getSession().setAttribute("cmd_result","Command sent to 0x"+dest_high + dest_low + 
									"<br>Sent on " + new Date(System.currentTimeMillis()).toString() 
									+ "<br/>Server Name: " + gardenServerName
									);
						}
						else{
							String validServers = "";
							String tab = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
							for(GardenServer gs : SegaExperimentServlet.getGardenServers()){
								validServers = validServers.concat(tab + gs.getGardenServerName() + tab + "IP: " + gs.getGardenServerIP() + "<br/>");
							}
							request.getSession().setAttribute("cmd_result","Command FAILED to send to 0x"+dest_high + dest_low + 
									"<br>Attempt made on " + new Date(System.currentTimeMillis()).toString() 
									+ "<br/>Server '" + gardenServerName + "' not found. Valid servers are:"
									+ "<br/>" + validServers);
						}
						
					}catch(Exception e){
						StringWriter errors = new StringWriter();
			        	e.printStackTrace(new PrintWriter(errors));
						log.write("Command FAILED to send to 0x"+dest_high + dest_low + " @ " + new Date(System.currentTimeMillis()).toString());
						request.getSession().setAttribute("cmd_result","Command FAILED to send to 0x"+dest_high + dest_low + 
								"<br>Attempt made on " + new Date(System.currentTimeMillis()).toString() 
								+ "<br/>Server Name: " + gardenServerName
								+ "<br/>" + errors);
					}
				}
				else{
					log.write("Command creation failed @ " + new Date(System.currentTimeMillis()).toString());
					request.getSession().setAttribute("cmd_result","Command Command creation failed @ " + new Date(System.currentTimeMillis()).toString());
				}
			}catch(Exception e){
				request.getSession().setAttribute("cmd_result", e.getMessage());
				StringWriter errors = new StringWriter();
	        	e.printStackTrace(new PrintWriter(errors));
	    		log.write(errors);
			}
			
		}
		if((redirect=request.getParameter("redirect")) != null){
			response.sendRedirect(redirect);
		}
	}
	
	
	public void addCrcAndFlush(String garden_ip,byte[] message) throws SAPIException{
		int crc = CRC.compute_crc(ArrayUtilities.convert_to_int_array(message));
        byte crc_hi_byte = (byte) (crc >> 8);
        byte crc_low_byte = (byte) (crc & 0xFF);
        
        byte[] cmdFinal = new byte[message.length+2];
        for(int i = 0; i < message.length; i++){
        	cmdFinal[i] = message[i];
        }
        cmdFinal[cmdFinal.length-2] = crc_hi_byte;
        cmdFinal[cmdFinal.length-1] = crc_low_byte;
        
        Source src = new Source(100,"append",1000);
        src.OpenRBNBConnection(garden_ip,"CommandGenerator_ControlSource");
        ChannelMap sMap = new ChannelMap();
        int index = sMap.Add("Commands");
        
        sMap.PutDataAsByteArray(index, cmdFinal); //cmds channel	        	
    	src.Flush(sMap);
    	src.Detach();
    	
	}
	public boolean addCrcAndFlushToGarden(String gardenServerName,byte[] message, long expiration_date, int priority) throws SAPIException{
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
        GardenServer gs = SegaExperimentServlet.getGardenServerByName(gardenServerName);
        if(gs != null){
	        //Flush to the appropriate GardenServer object
	        gs.insert_command(cmdFinal, expiration_date, priority);
	        return true;
        }
        
        return false;        	
        
    	
	}
}

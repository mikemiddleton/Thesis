MessageProcessor msgProc = (Message msg) -> {
	
  int net_id_hi = msg.getPayload()[8];
  int net_id_lo = msg.getPayload()[9];
  int net_id = ((net_id_hi << 8) | (net_id_lo & 0xFF));
  int valve_status = 0;
	
  // if message is from the valve wisard
  if(net_id == 0x0005){
    // if the packet has a stream 5
	if(msg.getPayload()[19] == 0x05){
	  logLine("Messaged received from wisard " + net_id);
			
	  // then this byte is the valve status
	  valve_status = msg.getPayload()[21];
	  logLine("Valve status was changed to: " + valve_status);
	  logLine("-------------------------------------------------------------------");
	}
  }
	
  // is message is from the sensing wisard and a full data packet
  if(net_id == 0x0003 && msg.getPayload().length > 40){
		
  // get sensor reading
  int sp_mod = msg.getPayload()[14];
  int stream = msg.getPayload()[35];
  int sample_hi = msg.getPayload()[37];
  int sample_lo = msg.getPayload()[38];
  int sample = ((sample_hi << 8) | (sample_lo & 0xFF));
		
  // only make decisions when we get a new reading from our sensor
  if(sp_mod == 3 && stream == 5){
    logLine("Messaged received from wisard " + net_id);
	logLine("SP: " + sp_mod + " stream: " + stream + " reads " + sample);
			
	// if we are in upslope where we want to keep watering...
	if(risingState){
		// if soil is still not wet enough, keep watering
		if(sample < upper_limit){
		  if(last_cmd_time==0 || System.currentTimeMillis()-last_cmd_time>
		  	180*1000){
			ActuateValve cmd = new ActuateValve(wisard, 0, (byte)1, 3600,
				true, 300);
			AutomatedAgentController.runCommand(cmd, agent);
			last_cmd_time = System.currentTimeMillis();
			logLine("Sample below upper threshold, sending another on command " + 
				"to keep watering");
		  }
		  else{
		    logLine("Sample below upper threshold, but not enough time since " + 
		    	"last cmd to send a new one");
		  }
		}
		// otherwise, the soil has gotten wet enough - turn off valve
		else{
		  risingState = false;
		  logLine("Sample at or above upper threshold, letting duration expire");
		}
	}
	// otherwise, we are in the downslope where we want it to keep drying
	else{
	  // if soil has dried too much, turn on water and change state
	  if(sample < lower_limit){
		ActuateValve cmd = new ActuateValve(wisard, 0, (byte)1, 3600, true, 300);
		AutomatedAgentController.runCommand(cmd, agent);
		risingState = true;
		last_cmd_time = System.currentTimeMillis();
		logLine("Sample below lower threshold, sending an on command to start watering");
	  }
	  else{
	    if(last_cmd_time == 0 || System.currentTimeMillis() - last_cmd_time > 180*1000){
		  logLine("Sample above lower threshold, letting duration expire");
		}
		else{
		  logLine("Sample still above lower threshold, but not enough time since " + 
		  	"last cmd to send a new one");
		}
	  }
	}
	logLine("-------------------------------------------------------------------");
   }				
  }
};
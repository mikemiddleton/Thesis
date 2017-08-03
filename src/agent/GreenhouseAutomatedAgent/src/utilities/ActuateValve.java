package utilities;

import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import edu.nau.rtisnl.SegaWebException;
import helpers.Site;
import helpers.SmartList;
import helpers.ValidationIssue;
import helpers.Wisard;
import helpers.Experiment;

/*
 * Command class for sending reset commands to a specific WiSARD
 */
public class ActuateValve extends NetManagementCommand {
	protected byte sp;
	protected int duration;
	protected boolean isOn;
	
	// use this constructor when there on/off are both needed
	public ActuateValve(Wisard wisard, int hub, byte sp, boolean isOn, int duration) {
		super(wisard, hub);
		this.sp = sp;
		this.duration = duration;
		this.isOn = isOn;
		overrideSubInfo();
	}
	
	// use this constructor when there on/off are both needed
	public ActuateValve(Wisard wisard, int hub, byte sp, long expiration, boolean isOn, int duration) {
		super(wisard, hub, expiration);
		this.sp = sp;
		this.duration = duration;
		this.isOn = isOn;
		overrideSubInfo();
	}
	
	public void overrideSubInfo(){
		//pubTopic = "exp249a-12/cmd/siccs106-01";
		pubTopic = "bio17/cmd/gh_agent";
		//broker = "tcp://exp249a-12.egr.nau.edu:1883";
		broker = "tcp://bio17.bio.nau.edu:1883";
		//pubId = "siccs106/cmd_publisher";
		pubId = "gh_agent/cmd_publisher";
	}
	
	
	@Override
	public byte[] getPayload(byte hub_hi, byte hub_lo, byte dest_hi, byte dest_lo) {
		if(isOn)
			return PacketGenerator.Valve_Command_Packet_ExpDur(hub_hi, hub_lo, dest_hi, dest_lo, sp, (byte)0x5B, System.currentTimeMillis() + expiration*1000, duration);
		else
			return PacketGenerator.Valve_Command_Packet_ExpDur(hub_hi, hub_lo, dest_hi, dest_lo, sp, (byte)0x00, System.currentTimeMillis() + expiration*1000, duration);
	}

	@Override
	public SmartList<ValidationIssue> validate(Wisard w) {
		SmartList<ValidationIssue> issues = new SmartList<ValidationIssue>();
		
		// To Do: ensure WiSARD exists
		
		
		// check that expiration is reasonable (between 3 minutes and 2 hours
		/*
		if(expiration < 180000 || expiration > 7200000){
			ValidationIssue error = new ValidationIssue("Error: invalid expiration", ValidationIssue.Type.ERROR);
			issues.add(error);
		}
		*/
		
		// if duration is less than 4 minutes, generate warning
		/*
		else if(expiration < 240000){
			ValidationIssue warning = new ValidationIssue("Warning: durations less than 4 minutes may expire before arrival", ValidationIssue.Type.WARNING);
			issues.add(warning);
		}
		*/
		
		return issues;		
	}

	public SmartList<ValidationIssue> validate(Site s, Wisard w){
		SmartList<ValidationIssue> issues = new SmartList<ValidationIssue>();
		return issues;
	}
	
	public SmartList<ValidationIssue> validate(Experiment e, Wisard w){
		SmartList<ValidationIssue> issues = new SmartList<ValidationIssue>();
		return issues;
	}

}


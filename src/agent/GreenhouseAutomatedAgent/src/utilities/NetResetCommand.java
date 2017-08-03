package utilities;

import java.io.IOException;
import java.sql.SQLException;

import edu.nau.rtisnl.SegaWebException;
import helpers.Site;
import helpers.SmartList;
import helpers.ValidationIssue;
import helpers.Wisard;
import helpers.Experiment;

/*
 * Command class for sending reset commands to a specific WiSARD
 */
public class NetResetCommand extends NetManagementCommand {
	public NetResetCommand(Wisard wisard, int hub) {
		super(wisard, hub);
	}
	
	// wisard is the desitnation, hub is the network tree the dest is in, expiration is the time in seconds
	public NetResetCommand(Wisard wisard, int hub, long expiration) {
		super(wisard, hub, expiration);
	}

	@Override
	public byte[] getPayload(byte hub_hi, byte hub_lo, byte dest_hi, byte dest_lo) {
		return PacketGenerator.Reset_Command_Packet_ExpDur(hub_hi, hub_lo, dest_hi, dest_lo, System.currentTimeMillis() + expiration*1000);
	}

	@Override
	public SmartList<ValidationIssue> validate(Wisard w) {
		SmartList<ValidationIssue> issues = new SmartList<ValidationIssue>();
		
		// To Do: ensure WiSARD exists
		
		
		// check that expiration is reasonable (between 3 minutes and 2 hours
		if(expiration < 180000 || expiration > 7200000){
			ValidationIssue error = new ValidationIssue("Error: invalid expiration", ValidationIssue.Type.ERROR);
			issues.add(error);
		}
		
		// if duration is less than 4 minutes, generate warning
		else if(expiration < 240000){
			ValidationIssue warning = new ValidationIssue("Warning: durations less than 4 minutes may expire before arrival", ValidationIssue.Type.WARNING);
			issues.add(warning);
		}
		
		return issues;		
	}

	public SmartList<ValidationIssue> validate(Site s, Wisard w){
		SmartList<ValidationIssue> issues = new SmartList<ValidationIssue>();
		
		if(w.getSite().equals("RTISNL-EXP249a-12") == false){
			ValidationIssue error = new ValidationIssue("Error: commands of this type are not currently allowed at " + w.getSite(), ValidationIssue.Type.ERROR);
			issues.add(error);
		}
		return issues;
	}
	
	public SmartList<ValidationIssue> validate(Experiment e, Wisard w){
		SmartList<ValidationIssue> issues = new SmartList<ValidationIssue>();
		
		// check if wisard is associated with any experiments
		try {
			if(w.getExperiments().where((Experiment exp) -> exp.getExperiment_id() == e.getExperiment_id()).size() > 0){
				ValidationIssue warning = new ValidationIssue("Warning: this WiSARD is associated with one or more experients. Restart at your own discrecion", ValidationIssue.Type.WARNING);
				issues.add(warning);					
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return issues;
	}
}


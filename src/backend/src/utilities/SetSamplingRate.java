package utilities;

import helpers.Site;
import helpers.SmartList;
import helpers.ValidationIssue;
import helpers.Wisard;
import utilities.PacketGenerator;

public class SetSamplingRate extends NetManagementCommand {
	private byte taskID_hi;
	private byte taskID_lo;
	private byte fieldID;
	private int duration;
	
	public SetSamplingRate(Wisard wisard, int hub, byte taskID_hi, byte taskIDlo, byte fieldID, int duration){
		super(wisard, hub);
		this.taskID_hi = taskID_hi;
		this.taskID_lo = taskID_lo;
		this.fieldID = fieldID;
		this.duration = duration;
	}
	
	public SetSamplingRate(Wisard wisard, int hub, long expiration, byte taskID_hi, byte taskIDlo, byte fieldID, int duration){
		super(wisard, hub, expiration);
		this.taskID_hi = taskID_hi;
		this.taskID_lo = taskID_lo;
		this.fieldID = fieldID;
		this.duration = duration;
	}
	
	@Override
	public byte[] getPayload(byte hub_hi, byte hub_lo, byte dest_hi, byte dest_lo) {
		return PacketGenerator.Interval_Command_Packet_Exp(
				hub_hi,
				hub_lo,
				dest_hi,
				dest_lo,
				taskID_hi,
				taskID_lo,
				fieldID,
				duration,
				expiration
			);
	}

	@Override
	public SmartList<ValidationIssue> validate(Wisard w) {
		return new SmartList<ValidationIssue>();
	}

	@Override
	public SmartList<ValidationIssue> validate(Site s, Wisard w) {
		return new SmartList<ValidationIssue>();
	}

	@Override
	public SmartList<ValidationIssue> validate(Experiment e, Wisard w) {
		return new SmartList<ValidationIssue>();
	}

}

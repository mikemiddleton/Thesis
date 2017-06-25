package utilities;

import helpers.SmartList;
import helpers.ValidationIssue;
import helpers.Wisard;

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
		return PacketGenerator.Reset_Command_Packet_ExpDur(hub_hi, hub_lo, dest_hi, dest_lo, System.currentTimeMillis() + expiration);
	}

	@Override
	public SmartList<ValidationIssue> validate(Wisard w) {
		return null;
	}

	// public abstract SmartList<ValidtaionIssue> validate(Site s, Wisard w);
	// public abstract SmartList<ValidationIssue> validate(Experiment e, Wisard w);
	
}


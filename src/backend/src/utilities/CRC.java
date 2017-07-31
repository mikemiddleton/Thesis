package utilities;

import java.util.Arrays;

/**
 * A class for computing and checking CRCs. 
 * 
 * @author jes244
 *
 */
public class CRC
{
	/**
	 * Checks the CRC of a packet from the WiSARDNet.
	 * 
	 * @param packet The packet to be checked.
	 * @return True, if the packet passed the CRC. False, if the packet
	 * failed.
	 */
	static public boolean check_crc(int[] packet)
	{
		// Variables
		int rec_crc, crc;
		int[] new_packet;

		// CRC is assumed to be the last two bytes of the packet
		rec_crc = (packet[packet.length - 2] << 8) + packet[packet.length - 1];
		// Remove the CRC
		new_packet = new int[packet.length - 2];
		for (int i = 0; i < new_packet.length; i++)
			new_packet[i] = packet[i];
		// Compute the CRC 
		crc = compute_crc(new_packet);
		// Does the CRC match rec_packet CRC?
		if (crc == rec_crc)
			return true;
		else
			return false;
	}
	
	/**
	 * Checks the CRC of a packet from the WiSARDNet.
	 * 
	 * @param packet The packet to be checked.
	 * @return True, if the packet passed the CRC. False, if the packet
	 * failed.
	 */
	static public boolean check_crc(byte[] packet)
	{
		// Variables
		int rec_crc, crc;
		int hi_byte_crc = packet[packet.length - 2] & 0xFF;
		int low_byte_crc = packet[packet.length - 1] & 0xFF;
		// CRC is assumed to be the last two bytes of the packet
		rec_crc = (hi_byte_crc << 8) + low_byte_crc;
		
		// Compute the CRC 
		crc = compute_crc(Arrays.copyOfRange(packet, 0, packet.length - 2));
		
		// Does the CRC match rec_packet CRC?
		if (crc == rec_crc)
			return true;
		else
			return false;
	}
	
	/**
	 * Computes the CRC of a packet.
	 * 
	 * @param packet The packet to compute the CRC for.
	 * @return The CRC.
	 */
	static public int compute_crc(int[] packet)
	{
		// Variables
		int crc = 0xFFFF;
		int poly = 0x1021;
		boolean bit, c15;

		for (int n : packet) {
			for (int i = 0; i < 8; i++) {
				bit = ((n >> (7 - i) & 1) == 1);
				c15 = ((crc >> 15 & 1) == 1);
				crc <<= 1;
				if (c15 ^ bit)
					crc ^= poly;
			}
		}
		return crc &= 0xFFFF;
	}
	
	/**
	 * Computes the CRC of a packet.
	 * 
	 * @param packet The packet to compute the CRC for.
	 * @return The CRC.
	 */
	static public int compute_crc(byte[] packet)
	{
		// Variables
		int crc = 0xFFFF;
		int poly = 0x1021;
		boolean bit, c15;

		for (int n : packet) {
			for (int i = 0; i < 8; i++) {
				bit = ((n >> (7 - i) & 1) == 1);
				c15 = ((crc >> 15 & 1) == 1);
				crc <<= 1;
				if (c15 ^ bit)
					crc ^= poly;
			}
		}
		return crc &= 0xFFFF;
	}
}

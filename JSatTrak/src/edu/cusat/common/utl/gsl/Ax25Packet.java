package edu.cusat.common.utl.gsl;


import java.util.Arrays;
import java.util.zip.CRC32;
import jsattrak.utilities.Bytes;
import edu.cusat.gs.rfgateway.Callsign;

public class Ax25Packet {

    /** Flag that surrounds packet, set by standard **/
	private static final byte AX25_FLAG = 0x7E;
        private static final byte AX25_PID = (byte)0xF0;

	/** The packet being created **/
	byte[] packet = new byte[FINAL_FLAG_OFFSET+1];
	
	private static final int INIT_FLAG_OFFSET = 0;
	private static final int DESTINATION_OFFSET = INIT_FLAG_OFFSET + 1;
	private static final int SOURCE_OFFSET = DESTINATION_OFFSET + Callsign.length();
	private static final int CONTROL_OFFSET = SOURCE_OFFSET + Callsign.length();
	private static final int PID_OFFSET = CONTROL_OFFSET + 1;
	private static final int CRC_OFFSET = PID_OFFSET + 1;
	private static final int FINAL_FLAG_OFFSET = CRC_OFFSET + 2;
	
	public Ax25Packet() {
	    // Set flags
	    packet[INIT_FLAG_OFFSET]  = AX25_FLAG;
            packet[FINAL_FLAG_OFFSET] = AX25_FLAG;
            packet[PID_OFFSET] = AX25_PID;
	}
	
	
	public void setSource(String callsign, byte id) {
            System.arraycopy(new Callsign(callsign, id).toArray(true),
                         0, packet, SOURCE_OFFSET, Callsign.length());
	}
	
	public void setDestination(String callsign, byte id) {
	    System.arraycopy(new Callsign(callsign, id).toArray(false), 
                         0, packet, DESTINATION_OFFSET, Callsign.length());
	}
	
	public void setControl(byte control) {
            packet[CONTROL_OFFSET] = control;
	}

        public void setPid(byte pid) {
            packet[PID_OFFSET] = pid;

        }
        public void updateCRC() {
        CRC32 crc = new CRC32();
        // Don't calculate CRC for flags or the CRC itself
        crc.update(packet, DESTINATION_OFFSET, packet.length - 4);
        System.arraycopy(Bytes.toArray((short) crc.getValue(), false), 
                         0, packet, CRC_OFFSET, 2);
        }
	
	public byte[] toArray() {
	    updateCRC();
	    return Arrays.copyOf(packet, packet.length);
	}
	
	
}

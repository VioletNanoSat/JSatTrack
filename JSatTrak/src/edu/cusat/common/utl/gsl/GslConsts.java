package edu.cusat.common.utl.gsl;

import jsattrak.utilities.Bytes;

/**
 * GSL Packet Type definitions
 * 
 * @author Zoe Chiang
 * @author Nate Parsons nsp25
 */
public final class GslConsts {
    public static final int TOP = 1;
    public static final int BOTTOM = 0;
    public static final byte STATE_PACKETS_RECEIVED = (byte) 0x07;
    public static final byte STATE_RESPONSE = (byte) 0x05;
    public static final byte KISS_FEND = (byte) 0xC0;
    public static final byte KISS_FESC = (byte) 0xDB;
    public static final byte KISS_TFEND = (byte) 0xDC;
    public static final byte KISS_TFESC = (byte) 0xDD;

    static final byte[] HEADER_GARBAGE = new byte[] { (byte) 0x03, (byte) 0xF0 };
    static final byte[] GROUND_CALLSIGN = new byte[] { (byte) 'G', (byte) 'R',
            (byte) 'O', (byte) 'U', (byte) 'N', (byte) 'D' };
    static final byte[] TOP_CALLSIGN = new byte[] { (byte) 'T', (byte) 'O',
            (byte) 'P', (byte) 'T', (byte) 'O', (byte) 'P' };
    static final byte[] BOTTOM_CALLSIGN = new byte[] { (byte) 'B', (byte) 'O',
            (byte) 'T', (byte) 'T', (byte) 'O', (byte) 'M' };
    public static final int CALLSIGN_LENGTH = 6;
    public static final int RADIO_HEADER_LENGTH = 18;
    public static final int GSL_DATA_SIZE = 300;
    public static final int RADIO_PACKET_SIZE = RADIO_HEADER_LENGTH
            + GSL_DATA_SIZE + 1;

    public static final int GSL_H_LENGTH = 7;
    public static final int GSLPLD_LENGTH = GSL_DATA_SIZE - GSL_H_LENGTH;

    /** Command Message ID system parameter key. */
    public static final String COMMAND_MESSAGE_ID = "TBL_Command_Message_ID";
    /** Command File Name system parameter key. */
    public static final String COMMAND_FILE_NAME = "TBL_File_Name";

    /**
     * Create a radio packet with a header and footer, and fill the middle with
     * 0s.
     * 
     * @param toSign
     *            - the callsign of the destination satellite
     * @param fromSign
     *            - the callsign of the ground station
     * @return a byte array suitable to send
     */
    public static byte[] radioPacket(byte[] toSign, byte[] fromSign) {
        byte[] ret;
        try {
            ret = new byte[RADIO_PACKET_SIZE];
            int reti = 0;
            ret[reti] = KISS_FEND;
            reti++;
            ret[reti] = (byte) 0x00;
            reti++;
            System.arraycopy(fromSign, 0, ret, reti, CALLSIGN_LENGTH);
            reti += CALLSIGN_LENGTH;
            ret[reti] = (byte) '>';
            reti++;
            System.arraycopy(toSign, 0, ret, reti, CALLSIGN_LENGTH);
            reti += CALLSIGN_LENGTH;
            System.arraycopy(HEADER_GARBAGE, 0, ret, reti,
                    HEADER_GARBAGE.length);
            reti += HEADER_GARBAGE.length;
            java.util.Arrays.fill(ret, reti, ret.length - 2, (byte) 0);
            ret[ret.length - 1] = KISS_FEND;
            return ret;
        } catch (RuntimeException e) {
            // TODO Auto-generated catch block
            System.out.println("IT happened here");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Extracts a short from a byte array, in 'little-endian' order.
     * 
     * @param b
     *            - the byte array to extract a short from
     * @param i
     *            - the start index (aka least significant byte)
     * @return Two bytes of the array, interpreted as a short.
     */
    public static short bytes2short(byte[] b, int i) {
        return (short) ((b[i + 1] << 8) | (b[i] & 0xff));
    }

    public static byte[] HEADER_GARBAGE() {
        return Bytes.arraySlice(HEADER_GARBAGE, 0, HEADER_GARBAGE.length);
    }
}

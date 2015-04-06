package edu.cusat.common.utl.gsl;

/**
 * Immutable container for a GSL Packet
 * 
 * @author Nate Parsons nsp25
 * 
 */
public class GslPacket {
    public static final int LENGTH = 316;

    private byte[] cGslPacket = new byte[LENGTH];

    /**
     * Create a GSL Packet from the given bytes
     * 
     * @param mGslPkt
     *            the source array
     * @throws InvalidLengthException
     *             if the source array is less than GslPacket.LENGTH bytes
     */
    public GslPacket(byte[] mGslPkt) throws InvalidLengthException {
        if (mGslPkt.length > LENGTH)
            throw new InvalidLengthException(LENGTH, mGslPkt.length);
        System.arraycopy(mGslPkt, 0, cGslPacket, 0, LENGTH);
    }

    /**
     * @return a copy of the array backing this GslPacket
     */
    public byte[] array() {
        byte[] ret = new byte[LENGTH];
        System.arraycopy(cGslPacket, 0, ret, 0, LENGTH);
        return ret;
    }
}
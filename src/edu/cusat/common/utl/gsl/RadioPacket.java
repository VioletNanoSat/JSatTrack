package edu.cusat.common.utl.gsl;

import java.io.IOException;
import java.io.InputStream;

import org.omg.CORBA_2_3.portable.OutputStream;

/**
 * A RadoPacket can be sent/received from a TNC in KISS mode.
 * 
 * @author natep
 * 
 */
public class RadioPacket {
    public static final byte FEND = (byte) 0xc0;

    public static final byte FESC = (byte) 0xDB;

    public static final byte TFEND = (byte) 0xDC;

    public static final byte TFESC = (byte) 0xDD;

    private static final byte DATA_FRAME = (byte) 0x00;

    private static final byte[] HEADER = new byte[] { FEND, DATA_FRAME };

    private static final byte[] FOOTER = new byte[] { FEND };

    public static final int LENGTH = HEADER.length + FOOTER.length
            + GslPacket.LENGTH;

    private GslPacket cGslPacket;

    /**
     * TODO take the code from RadioListener
     * 
     * @param in
     * @return
     */
    public static GslPacket parse(InputStream in) {
        byte[] ret = new byte[GslPacket.LENGTH];
        try {
            return new GslPacket(ret);
        } catch (InvalidLengthException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException();
        }
    }

    public RadioPacket(GslPacket packet) {
        cGslPacket = packet;
    }
    
    public boolean write(OutputStream out){
        try {
            out.write(HEADER);
            out.write(cGslPacket.array());
            out.write(FOOTER);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
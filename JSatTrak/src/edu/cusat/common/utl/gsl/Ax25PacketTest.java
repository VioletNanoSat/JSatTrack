    package edu.cusat.common.utl.gsl;

import static org.junit.Assert.*;

import org.junit.Test;

public class Ax25PacketTest {

    private static byte[] expected = new byte[] {
        // Flag 
        (byte) 0x7e,
        // INFO_0
        (byte) 0x92, (byte) 0x9c, (byte) 0x8c, (byte) 0x9e, (byte) 0x40, (byte) 0x40, (byte) 0xe0,
        // Me_0
        (byte) 0x9a, (byte) 0xca, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0xe1,
        // Control = 0
        (byte) 0x0, 
        // PID = 0
        (byte) 0x0, 
        // CRC
        (byte) 0x7f, (byte) 0x39, 
        // Flag
        (byte) 0x7e};

    @Test
    public void testToArray() {
        Ax25Packet pkt = new Ax25Packet();
        pkt.setSource("Me", (byte) 0);
        pkt.setDestination("INFO", (byte) 0);
        pkt.setControl((byte) 0);
        pkt.setPid((byte) 0);
        pkt.updateCRC();
        assertArrayEquals(expected, pkt.toArray());
    }

}

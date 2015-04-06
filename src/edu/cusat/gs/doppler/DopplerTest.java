package edu.cusat.gs.doppler;

import static org.junit.Assert.*;
import static edu.cusat.gs.doppler.Doppler.*;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;

public class DopplerTest {
    private Doppler d;

    private static final Instant T0 = new Instant(0),
                                 T1 = new Instant(1000),
                                 T2 = new Instant(3000); 
    
    private static final double R0 = 16000;
    
    private static final long BASE_FREQ = 437000000;
    private static final double TOLERANCE = 1.;
    
    @Before
    public void setUp() throws Exception {
        d = new Doppler(R0, T0);
    }

    @Test
    public void testGetShift() {

        // First we'll have it move closer to us
        double range1 = 8000;
        double shiftAmt1 = 11661.400768127395231;

        double[] expectedShift = new double[] { BASE_FREQ - shiftAmt1,
                                                BASE_FREQ + shiftAmt1 };
        double[] actualShift = d.getShift(BASE_FREQ, range1, T1);

        // Check direction
        assertTrue("TX shift should be negative",
        		actualShift[TX_OFFSET] < BASE_FREQ);
        assertTrue("RX shift should be positive",
        		actualShift[RX_OFFSET] > BASE_FREQ);
        // Check amount
        assertArrayEquals(expectedShift, actualShift, TOLERANCE);

        // Now farther
        double range2 = 12000;
        double shiftAmt2 = 2915.350192031849;

        expectedShift = new double[] { BASE_FREQ + shiftAmt2,
                                       BASE_FREQ - shiftAmt2 };
        actualShift = d.getShift(BASE_FREQ, range2, T2);

        // Check direction
        assertTrue("TX shift should be positive",
        		actualShift[TX_OFFSET] > BASE_FREQ);
        assertTrue("RX shift should be negative",
        		actualShift[RX_OFFSET] < BASE_FREQ);
        // Check amount
        assertArrayEquals(expectedShift, actualShift, TOLERANCE);
    }

    /**
     * Make sure that when the satellite moves in 0 time, the shift is 
     * unchanged, not infinite
     */
    @Test
    public void testDivZero() { 
        double[] shift1 = d.getShift(BASE_FREQ, 15000, T1);
        double[] shift2 = d.getShift(BASE_FREQ, 14000, T1);
        assertArrayEquals(shift1, shift2, TOLERANCE);
    }
}

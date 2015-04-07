package edu.cusat.gs.rotator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class BasicFilterTest {

    private Filter strFilter;
    private Filter intFilter;
    
    @Before
    public void setUp() throws Exception {
        strFilter = new BasicFilter("W000 000");
        intFilter = new BasicFilter(0,0);
    }

    @Test
    public void testPassesString() {
        assertFalse(strFilter.passes("W000 000")); // Repeat
        assertTrue (strFilter.passes("W010 100")); // New, valid
        assertTrue (strFilter.passes("W011 110")); // New, valid
        assertFalse(strFilter.passes("W011 110")); // Repeat
        assertFalse(strFilter.passes(    ""    )); // Not a command
        assertFalse(strFilter.passes(   null   )); // Not a command
        assertFalse(strFilter.passes("W011 110")); // Still repeat
        assertTrue (strFilter.passes("W123 180")); // New, valid
        assertFalse(strFilter.passes("W500 798")); // Invalid range
        assertFalse(strFilter.passes( "ABCDEF" )); // Not a command
    }

    @Test
    public void testPassesIntInt() {
        assertFalse(intFilter.passes(  0,  0)); // Repeat
        assertTrue (intFilter.passes( 10,100)); // New, valid
        assertTrue (intFilter.passes( 11,110)); // New, valid
        assertFalse(intFilter.passes( 11,110)); // Repeat
        assertFalse(intFilter.passes( -1, -1)); // Invalid range
        assertFalse(intFilter.passes(451,181)); // Invalid range
        assertFalse(intFilter.passes( 11,110)); // Repeat
        assertTrue (intFilter.passes( 15,115)); // New, valid
    }

}

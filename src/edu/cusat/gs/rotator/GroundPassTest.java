package edu.cusat.gs.rotator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.GroundStation;
import jsattrak.objects.SatelliteTleSGP4;
import jsattrak.utilities.TLE;
import name.gano.astro.time.Time;

import org.junit.Before;
import org.junit.Test;

public class GroundPassTest {

    private static final float ONE_SEC = 1F/(60F*60F*24F);

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testFind() {
        try {
            Time startTime = new Time(2009, 4, 29, 14, 00, 0);
            GroundStation gs = new GroundStation("WhiteSands", new double[]{34.5, -105.0, 0.0}, startTime.getJulianDate());
            AbstractSatellite sat = new SatelliteTleSGP4(new TLE("ISS (ZARYA)", "1 25544U 98067A   09091.68602787  .00012306  00000-0  96990-4 0  5909", "2 25544  51.6423 338.9854 0009269 184.1272 286.1737 15.71858385593916"));
            List<GroundPass> passes = GroundPass.find(gs, sat, startTime, 1F/24F, ONE_SEC);
            assertEquals(1, passes.size());
            GroundPass pass = passes.get(0);
            assertEquals("Rise time", 2454951.0902575874, pass.riseTime(), ONE_SEC);
            assertEquals("Rise az",   350.5038139574035,  pass.riseAz(),   ONE_SEC);
            assertEquals("Set time",  2454951.0929849353, pass.setTime(),  ONE_SEC);
            assertEquals("Set az",    73.25762579258941,  pass.setAz(),    ONE_SEC);
            assertTrue("Requires flip", pass.requiresFlip());
        } catch (Exception e) {
            fail("Bad TLE");
        }
    }
    
}

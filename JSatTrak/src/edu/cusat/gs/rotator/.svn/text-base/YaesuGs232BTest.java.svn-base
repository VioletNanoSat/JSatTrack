package edu.cusat.gs.rotator;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cusat.common.Failure;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;

public class YaesuGs232BTest {
	
	private Rotator r;

	@Before
	public void setUp() {
		r = new YaesuGs232B();
		try {
            r.setPort(CommPortIdentifier.getPortIdentifier("COM4"));
            r.connect(9600);
        } catch (NoSuchPortException e1) {
            fail("Choose a different port");
        } catch (Failure e) {
        	fail(e.getMessage());
        }
		if(!r.isConnected())
		    fail("Not connected");
	}
	
	@After
	public void tearDown() throws Exception {
		if(r.isConnected()) r.disconnect();
	}

	@Test
	public void testAzDouble() {
		try {
			double az = r.az();
			System.out.println(az);
			az = r.az(5);
			System.out.println(az);
		} catch (Failure e) {
			fail();
		}
		assertTrue(true);
	}

	@Test
	public void testAzElDoubleDouble() {
		assertTrue(true);
	}

}

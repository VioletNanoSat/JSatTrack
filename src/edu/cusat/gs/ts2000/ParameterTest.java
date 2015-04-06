package edu.cusat.gs.ts2000;

import static org.junit.Assert.*;

import org.junit.Test;

public class ParameterTest {

	@Test
	public void testSetValue() {
		Parameter p1 = new Parameter("p1", 3, 0, 999, 50);
		Parameter p2 = p1.setValue(50);
		assertNotSame(p1, p2);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalValue() {
		new Parameter("p1", 3, 0, 1, 50);
	}

	@Test
	public void testCommandForm() {
		Parameter p1 = new Parameter("p1", 3, 0, 999, 50);
		assertEquals("050", p1.commandForm());
	}

}

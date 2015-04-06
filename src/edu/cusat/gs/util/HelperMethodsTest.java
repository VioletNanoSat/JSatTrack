package edu.cusat.gs.util;

import static edu.cusat.gs.util.HelperMethods.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class HelperMethodsTest {

	@Test
	public void testMegaHertzToHertz() {
		assertEquals(437405000, megaHertzToHertz(437.405000), 0.0000001);
	}

	@Test
	public void testIsBinary() {
		assertTrue(isBinary(1));
		assertTrue(isBinary(0));
		assertFalse(isBinary(-1));
	}

	@Test
	public void testIsDecimal() {
		for(int i=0; i < 10; i++)
			assertTrue(isDecimal(i));
	}
	
	@Test
	public void testIntToString() {
		assertEquals("123",intToString(123,3));
		assertEquals("123",intToString(123,2));
		assertEquals("0123",intToString(123,4));
	}
}

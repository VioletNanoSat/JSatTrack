package edu.cusat.gs.rfgateway;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class CallsignTest {

	@Test
	public void testCallsignString() {
		Callsign call = new Callsign("ABCDEF");
		assertEquals("ABCDEF", call.getCallString());
		assertEquals((byte)0, call.getSsid());
		call = new Callsign("ABCD");
		assertEquals("ABCD  ", call.getCallString());
		assertEquals((byte)0, call.getSsid());
	}

	@Test
	public void testCallsignStringByte() {
		Callsign call = new Callsign("ABCDEF", (byte) 123);
		assertEquals("ABCDEF", call.getCallString());
		assertEquals((byte)123, call.getSsid());
		call = new Callsign("ABCD  ", (byte) 64);
		assertEquals("ABCD  ", call.getCallString());
		assertEquals((byte)64, call.getSsid());
	}

	@Test
	public void testCallsignByteArray() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testFromSimPacket() {
		assertEquals(new Callsign("TOPTOP"), Callsign.fromSimPacket(new byte[]{84, 79, 80, 84, 79, 80, (byte)0xe0}));
	}

	@Test
	public void testToArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testEqualsByteArray() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testEquals() {
		Callsign call1 = new Callsign("ABCDEF",(byte) 0);
		Callsign call2 = new Callsign("ABCDEF",(byte) 0);
		assertTrue(call1 != call2);
		assertEquals(call1,call2);
	}
	
	@Test
	public void testHashCode() {
		Callsign call1 = new Callsign("ABCDEF",(byte) 0);
		Callsign call2 = new Callsign("ABCDEF",(byte) 0);
		assertTrue(call1 != call2);
		assertEquals(call1.hashCode(), call2.hashCode());
	}

	@Test
	public void testEqualsIgnoreSsid() {
		Callsign call1 = new Callsign("ABCDEF");
		Callsign call2 = new Callsign("ABCDEF", (byte) 123);
		assertThat(call1, not(equalTo(call2)));
		assertTrue(call1.equalsIgnoreSsid(call2));
	}

	@Test
	public void testToString() {
		Callsign call = new Callsign("ABCDEF");
		assertEquals("ABCDEF0",call.toString());
	}


}

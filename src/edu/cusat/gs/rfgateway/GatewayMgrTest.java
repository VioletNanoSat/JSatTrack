package edu.cusat.gs.rfgateway;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import edu.cusat.common.GTG;
import edu.cusat.gs.rfgateway.GatewayMgr.ServerStatus;

public class GatewayMgrTest {

	private GatewayMgr mgr;

	@Before
	public void setUp() {
		mgr = new GatewayMgr();
		mgr.registerListener(new GatewayMgrListener() {
			@Override
			public void eventOccurred(String message) {
				System.out.println(message);
			}
		});
	}

	private void assertServerStatus(Callsign call, ServerStatus cmdStat,
			ServerStatus tlmStat) {
		// Wait for the server to fully accept the connection
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}
		assertEquals(cmdStat, mgr.cmdStatus(call));
		assertEquals(tlmStat, mgr.tlmStatus(call));
	}

	@Test
	public void testServer() throws UnknownHostException, IOException {
		final Callsign call = new Callsign("TEST");
		final int cmd = 12345, tlm = 54321;

		// Nothing should be open yet
		assertServerStatus(call, ServerStatus.DEAD, ServerStatus.DEAD);

		// Open a connection
		assertTrue(mgr.openServer(call, cmd, tlm));
		assertServerStatus(new Callsign("TEST"), ServerStatus.WAITING,
				ServerStatus.WAITING);

		// Connect to one of them
		Socket s0 = new Socket("localhost", cmd);
		assertServerStatus(call, ServerStatus.RUNNING, ServerStatus.WAITING);

		// Disconnect and go back to waiting
		s0.getOutputStream().write(GTG.GTG_DISCONNECT);
		s0.close();
		assertServerStatus(call, ServerStatus.WAITING, ServerStatus.WAITING);

		// Just kidding! I want to connect to both!
		s0 = new Socket("localhost", cmd);
		assertServerStatus(call, ServerStatus.RUNNING, ServerStatus.WAITING);
		Socket s1 = new Socket("localhost", tlm);
		assertServerStatus(call, ServerStatus.RUNNING, ServerStatus.RUNNING);

		// OK, it all works. Bye
		s0.getOutputStream().write(GTG.GTG_DISCONNECT);
		s0.close();
		assertServerStatus(call, ServerStatus.WAITING, ServerStatus.RUNNING);
		s1.getOutputStream().write(GTG.GTG_DISCONNECT);
		s1.close();
		assertServerStatus(call, ServerStatus.WAITING, ServerStatus.WAITING);

		mgr.closeServer(call);
		assertServerStatus(call, ServerStatus.DEAD, ServerStatus.DEAD);
	}

	private void assertTncStatus(ServerStatus stat) {
		assertEquals(stat, mgr.tncStatus());
	}

	@Test
	public void testTNC() {
		assertTncStatus(ServerStatus.DEAD);
		mgr.connectToSim(33330, 44440, 33320, 44430);
		assertTncStatus(ServerStatus.RUNNING);
	}

	@Test
	public void testSimTNC() {
		assertTncStatus(ServerStatus.DEAD);
	}

	@Test
	public void testAutoBecaon() {
		fail("Not yet implemented");
	}
}

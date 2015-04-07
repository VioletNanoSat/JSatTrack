package edu.cusat.gs.ts2000;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import edu.cusat.common.utl.FakeSerialPort;
import edu.cusat.gs.ts2000.KenwoodTs2kModel.MainVfo;
import edu.cusat.gs.ts2000.KenwoodTs2kModel.Vfo;

public class KenwoodTs2kControllerTest {
	
	private KenwoodTs2kController controller;
	private FakeSerialPort sport;

	@Before
	public void setUp() throws Exception {
		sport = new FakeSerialPort();
		controller = new KenwoodTs2kController(sport.getFakeId());
		controller.connect();
	}
	
	private void assertNextCmdIs(String cmd){
		assertEquals(cmd, readNextCommand());
	}

	@Test
	public void testSetVFOFrequency_Vfo() {
		controller.setVFOFrequency(432123456, Vfo.A);
		assertNextCmdIs("FA00432123456");
		assertNextCmdIs("FA");
		controller.setVFOFrequency(450000000, Vfo.B);
		assertNextCmdIs("FB00449999999");
		assertNextCmdIs("FB");
	}
	
	@Test
	public void testSetVFOFrequency_MainVfo() {
		controller.setVFOFrequency(432123456, MainVfo.A);
		assertNextCmdIs("FA00432123456");
		assertNextCmdIs("FA");
		controller.setVFOFrequency(450000000, MainVfo.B);
		assertNextCmdIs("FB00449999999");
		assertNextCmdIs("FB");
	}

	@Test
	public void testSetTxVfo_Vfo() {
		controller.setTxVfo(Vfo.A);
		assertNextCmdIs("FT0");
		assertNextCmdIs("FT");
		controller.setTxVfo(Vfo.B);
		assertNextCmdIs("FT1");
		assertNextCmdIs("FT");
	}
	
	@Test
	public void testSetTxVfo_MainVfo() {
		controller.setTxVfo(MainVfo.A);
		assertNextCmdIs("FT0");
		assertNextCmdIs("FT");
		controller.setTxVfo(MainVfo.B);
		assertNextCmdIs("FT1");
		assertNextCmdIs("FT");
	}

	@Test
	public void testSetRxVfo() {
		controller.setRxVfo(Vfo.A);
		assertNextCmdIs("FR0");
		assertNextCmdIs("FR");
		controller.setRxVfo(Vfo.B);
		assertNextCmdIs("FR1");
		assertNextCmdIs("FR");
	}
	
	@Test
	public void testSetRxVfo_MainVfo() {
		controller.setRxVfo(MainVfo.A);
		assertNextCmdIs("FR0");
		assertNextCmdIs("FR");
		controller.setRxVfo(MainVfo.B);
		assertNextCmdIs("FR1");
		assertNextCmdIs("FR");
	}
	
	@Test
	public void testSplit(){
		controller.split(MainVfo.A);
		assertNextCmdIs("FR1");
		assertNextCmdIs("FR");
		assertNextCmdIs("FT0");
		assertNextCmdIs("FT");
		controller.split(MainVfo.B);
		assertNextCmdIs("FR0");
		assertNextCmdIs("FR");
		assertNextCmdIs("FT1");
		assertNextCmdIs("FT");
	}
	
	@Test
	public void testSMeter() {
		controller.sMeter(0);
		assertNextCmdIs("SM0");
		controller.sMeter(1);
		assertNextCmdIs("SM1");
		controller.sMeter(2);
		assertNextCmdIs("SM2");
		controller.sMeter(3);
		assertNextCmdIs("SM3");
	}
	
	@Test
	public void testSquelch() {
		controller.setSquelch(1, 222);
		assertNextCmdIs("SQ1222");
		controller.setSquelch(0, 222);
		assertNextCmdIs("SQ0222");
	}
	
	@Test
	public void testAFGain() {
		controller.setAFGain(0, 150);
		assertNextCmdIs("AG0150");
		assertNextCmdIs("AG");
		controller.setAFGain(1, 100);
		assertNextCmdIs("AG1100");
		assertNextCmdIs("AG");
	}

	/**
	 * Read from the serial port until a semicolon is reached. If an I/O error
	 * occurs, or the end of the stream is reached, <code>connected</code> is
	 * set to <code>false</code>
	 * 
	 * @return the text read
	 */
	private String readNextCommand() {
		StringBuffer sb = new StringBuffer(32);
		try {
			for(;;){
				int i=sport.distream.read();
				if(i==-1){
					fail("Read a -1");
					break;
				}
				char c = (char)i;
				if(c == ';')
					break;
				sb.append(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        return sb.toString();
	}
}

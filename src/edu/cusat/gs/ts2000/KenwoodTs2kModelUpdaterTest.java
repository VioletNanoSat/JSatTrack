package edu.cusat.gs.ts2000;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class KenwoodTs2kModelUpdaterTest {

	KenwoodTs2kModel model;
	KenwoodTs2kModelUpdater updater;
	
	@Before
	public void setUp() throws Exception {
		model = new KenwoodTs2kModel();
		updater = new KenwoodTs2kModelUpdater(model);
	}

	@Test public void testError() {
		assertEquals(0,model.error_count.get().intValue());
		updater.update("?");
	    assertEquals(1,model.error_count.get().intValue());
	    updater.update("E");
	    assertEquals(2,model.error_count.get().intValue());
    }

	@Test public void testUnknown() {
		updater.update("0"); // Not a response the TS-2000 should ever give
		assertEquals(1,model.unknown_count.get().intValue());
	}
	
	@Test public void testVfoAFreq() {
		updater.update("FA00437405000");
		assertEquals(437405000,model.vfo_a_frequency.get().intValue());
	}
	
	@Test public void testVfoBFreq() {
		updater.update("FB00437405000");
		assertEquals(437405000,model.vfo_b_frequency.get().intValue());
	}

	@Test public void testSubFreq() {
		updater.update("FC00437405000");
		assertEquals(437405000,model.vfo_sub_frequency.get().intValue());
	}
	
	@Test public void testID() {
		updater.update("ID019");
		assertEquals(19,model.ID.get().intValue());
	}
	
	@Test public void testMain_Sub_tx_ctrl() {
		KenwoodTs2kModel.Vfo startVfo = model.tx_vfo.get();
		updater.update("DC00");
		assertEquals(KenwoodTs2kModel.Main_Sub.MAIN, model.tx_transceiver.get());
		assertEquals(KenwoodTs2kModel.Main_Sub.MAIN, model.rx_transceiver.get());
		assertEquals(KenwoodTs2kModel.Main_Sub.MAIN, model.ctrl_transceiver.get());
		assertEquals(startVfo, model.tx_vfo.get());
		updater.update("DC01");
		assertEquals(KenwoodTs2kModel.Main_Sub.MAIN, model.tx_transceiver.get());
		assertEquals(KenwoodTs2kModel.Main_Sub.MAIN, model.rx_transceiver.get());
		assertEquals(KenwoodTs2kModel.Main_Sub.SUB,  model.ctrl_transceiver.get());
		assertEquals(startVfo, model.tx_vfo.get());
		updater.update("DC10");
		assertEquals(KenwoodTs2kModel.Main_Sub.SUB,  model.tx_transceiver.get());
		assertEquals(KenwoodTs2kModel.Main_Sub.SUB,  model.rx_transceiver.get());
		assertEquals(KenwoodTs2kModel.Main_Sub.MAIN, model.ctrl_transceiver.get());
		assertEquals(KenwoodTs2kModel.Vfo.SUB, model.tx_vfo.get());
		updater.update("DC11");
		assertEquals(KenwoodTs2kModel.Main_Sub.SUB,  model.tx_transceiver.get());
		assertEquals(KenwoodTs2kModel.Main_Sub.SUB,  model.rx_transceiver.get());
		assertEquals(KenwoodTs2kModel.Main_Sub.SUB,  model.ctrl_transceiver.get());
		assertEquals(KenwoodTs2kModel.Vfo.SUB, model.tx_vfo.get());
	}
	
	@Test public void test_tx_vfo() {
		updater.update("DC11");
		updater.update("FT0");
		assertEquals(KenwoodTs2kModel.Vfo.SUB, model.tx_vfo.get());
		updater.update("DC00");
		updater.update("FT0");
		assertEquals(KenwoodTs2kModel.Vfo.A, model.tx_vfo.get());
		updater.update("FT1");
		assertEquals(KenwoodTs2kModel.Vfo.B, model.tx_vfo.get());
	}
	
	@Test public void test_rx_vfo() {
		updater.update("DC11");
		updater.update("FR0");
		assertEquals(KenwoodTs2kModel.Vfo.SUB, model.rx_vfo.get());
		updater.update("DC00");
		updater.update("FR0");
		assertEquals(KenwoodTs2kModel.Vfo.A, model.rx_vfo.get());
		updater.update("FR1");
		assertEquals(KenwoodTs2kModel.Vfo.B, model.rx_vfo.get());
	}
	
	@Test public void testSMeter() {
		updater.update("SM00000");
		assertEquals(0, model.s_meter.get().intValue());
		updater.update("SM00030");
		assertEquals(30, model.s_meter.get().intValue());
		updater.update("SM00015");
		assertEquals(15, model.s_meter.get().intValue());
	}
	
	@Test public void testSquelch() {
		updater.update("SQ1222");
		assertEquals(222, model.squelch.get().intValue());
		updater.update("SQ1123");
		assertEquals(123, model.squelch.get().intValue());
	}
	
	
	@Test public void testAFGain() {
		updater.update("AG0250");
		assertEquals(250, model.af_gain.get().intValue());
	}
	
	@Test public void testRFGain() {
		updater.update("RG250");
		assertEquals(250, model.rf_gain_value.get().intValue());
	}
	
	@Test public void testPowerStatus() {
		updater.update("PS0");
		assertEquals(KenwoodTs2kModel.STATUS.OFF, model.power_status.get());
		updater.update("PS1");
		assertEquals(KenwoodTs2kModel.STATUS.ON, model.power_status.get());
	}
	
	//Can't test this?
	//@Test  public void testResetTransceiver() {
	//	updater.update("SR1");
	//	assertEquals(KenwoodTs2kModel.RESET.VFOReset, model.transceiver_status.get());
	//}
	
	@Test public void testBusySignalStatus() {
		updater.update("BY01");
		assertEquals(0, model.busy_signal_status_main.get().intValue());
		assertEquals(1, model.busy_signal_status_sub.get().intValue());
	}
	
	@Test public void testOperatingStatus() {
		assertEquals(KenwoodTs2kModel.MODE.RESERVED, model.operating_mode.get());
		updater.update("MD1");
		assertEquals(KenwoodTs2kModel.MODE.LSB, model.operating_mode.get());
	}
	
	@Test public void testTransceiverStatus() {
		updater.update("IF11111111111222233333345677890123445");
		assertEquals("11111111111222233333345677890123445", model.transceiver_status.get());
	}
	
	@Test public void testInternalTNCMode() {
		updater.update("TC 0");
		assertEquals(" 0", model.internal_tnc_mode_actual.get());
		assertEquals(KenwoodTs2kModel.TNCMode.PacketCommunicationMode, model.internal_tnc_mode.get());
	}
	
	
}

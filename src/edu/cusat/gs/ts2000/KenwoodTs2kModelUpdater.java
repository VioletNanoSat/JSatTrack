package edu.cusat.gs.ts2000;

import edu.cusat.gs.ts2000.KenwoodTs2kModel.MODE;
import edu.cusat.gs.ts2000.KenwoodTs2kModel.MainVfo;
import edu.cusat.gs.ts2000.KenwoodTs2kModel.Main_Sub;
import edu.cusat.gs.ts2000.KenwoodTs2kModel.RESET;
import edu.cusat.gs.ts2000.KenwoodTs2kModel.STATUS;
import edu.cusat.gs.ts2000.KenwoodTs2kModel.TNCMode;
import edu.cusat.gs.ts2000.KenwoodTs2kModel.Vfo;

/**
 * Responsible for parsing responses from the TS-2000 and updating the model
 */
public class KenwoodTs2kModelUpdater {

	private KenwoodTs2kModel model;

	/**
	 * Create an updater for model m
	 * @param m
	 */
	public KenwoodTs2kModelUpdater(KenwoodTs2kModel m) {
		model = m;
	}

	/**
	 * Update the model from the given response
	 * 
	 * @param response
	 *            the response from the TS-2000, not including the trailing ';'
	 */
	public void update(String response) {
		if(response.length() < 1)
			return;

		if(response.equals("?") || response.equals("E"))
			error(response);

		else if(response.startsWith(Command.OPERATIONAL_MODE.toString()))
			mode(Command.OPERATIONAL_MODE.getParameters(response));

		else if(response.startsWith(Command.TX_A_B.toString()))
			tx_a_b(Command.TX_A_B.getParameters(response));

		else if(response.startsWith(Command.RX_A_B.toString()))
			rx_a_b(Command.RX_A_B.getParameters(response));

		else if(response.startsWith(Command.TX_DXER_STATUS.toString())) //DC
			tx_dxer(Command.TX_DXER_STATUS.getParameters(response));

		else if(response.startsWith(Command.VFO_A_FREQ.toString()))
			vfo_a_f(Command.VFO_A_FREQ.getParameters(response));

		else if(response.startsWith(Command.VFO_B_FREQ.toString()))
			vfo_b_f(Command.VFO_B_FREQ.getParameters(response));

		else if(response.startsWith(Command.VFO_SUB_FREQ.toString()))
			vfo_sub_f(Command.VFO_SUB_FREQ.getParameters(response));

		else if(response.startsWith(Command.TRANSCEIVER_ID.toString()))
			id(Command.TRANSCEIVER_ID.getParameters(response));

		else if(response.startsWith(Command.S_METER_STATUS.toString()))
			sMeter(Command.S_METER_STATUS.getParameters(response));

		else if(response.startsWith(Command.SQUELCH_LEVEL.toString()))
			squelch(Command.SQUELCH_LEVEL.getParameters(response));

		else if (response.startsWith(Command.AF_GAIN.toString()))
			AFGain(Command.AF_GAIN.getParameters(response));

		else if (response.startsWith(Command.RF_GAIN_STATUS.toString()))
			RFGain(Command.RF_GAIN_STATUS.getParameters(response));

		else if (response.startsWith(Command.TRANSCEIVER_STATUS.toString()))
			TransceiverStatus(Command.TRANSCEIVER_STATUS.getParameters(response));

		else if (response.startsWith(Command.PWR_STATUS.toString()))
			PowerStatus(Command.PWR_STATUS.getParameters(response));

		else if (response.startsWith(Command.RF_GAIN_STATUS.toString()))
			RFGain(Command.RF_GAIN_STATUS.getParameters(response));

		else if (response.startsWith(Command.BUSY_SIGNAL_STATUS.toString()))
			BusySignalStatus(Command.BUSY_SIGNAL_STATUS.getParameters(response));
		
		else if (response.startsWith(Command.RESET_TRANSCEIVER.toString()))
			ResetTransceiver(Command.RESET_TRANSCEIVER.getParameters(response));
		
		else if (response.startsWith(Command.INTERNAL_TNC_MODE.toString()))
			InternalTNCMode(Command.INTERNAL_TNC_MODE.getParameters(response));
		
		
		
		// TODO ADD NEW METHOD CALL HERE! Use this pattern:
		// else if(response.startsWith(Command.xxxxxxxxx.toString()))
		// newMethodName(Command.xxxxxxxxx.getParameters(response));

		else
			unknown(response);
	}

	/*
	 * TODO ADD NEW METHOD HERE! Steps:
	 * 1. Decide on a response to add handling for
	 * 2. Find out what the response is as a function of the total TS-2k state.
	 *     For example, if the response is what the VFO A mode is, what if the
	 *     TS-2000 has the sub receiver selected?
	 * 3. Create a testNewMethodName in KenwoodTs2kModelUpdaterTest 
	 * 4. Create method newMethodName here, with no return type, and taking 
	 *     String[] params as an argument
	 * 5. Add a call to newMethodName in update()
	 * 6. Implement the body of the test method, by calling updater.update() 
	 *     with different possible responses from the Ts2k (you may want to 
	 *     have more than one test method per updater method), and then making
	 *     various assertions about the value of relevant KenwoodTs2kModel 
	 *     Properties
	 * 7. Implement the body of newMethodName so that it passes all tests, and
	 *     the model is kept in the same state as the TS-2000. You may also need
	 *     to edit KenwoodTs2kModel.
	 */

	private void squelch(String[] params) {
		model.squelch.set(Integer.parseInt(params[2]));
	}

	private void sMeter(String[] params) {
		model.s_meter.set(Integer.parseInt(params[2]));
	}

	private void rx_a_b(String[] params) {
		if(Main_Sub.SUB.equals(model.rx_transceiver.get())){
			model.rx_vfo.set(Vfo.SUB);
		} else {
			MainVfo vfo = MainVfo.get(Integer.parseInt(params[1]));
			switch (vfo) {
			case A:
				model.rx_vfo.set(Vfo.A);
				break;
			case B:
				model.rx_vfo.set(Vfo.B);
				break;
			default:
				break;
			}
		}
	}

	private void tx_a_b(String[] params) {
		if(Main_Sub.SUB.equals(model.tx_transceiver.get())){
			model.tx_vfo.set(Vfo.SUB);
		} else {
			MainVfo vfo = MainVfo.get(Integer.parseInt(params[1]));
			switch (vfo) {
			case A:
				model.tx_vfo.set(Vfo.A);
				break;
			case B:
				model.tx_vfo.set(Vfo.B);
				break;
			default:
				break;
			}
		}
	}

	private void tx_dxer(String[] params) {
		Main_Sub tx = Main_Sub.get(Integer.parseInt(params[1]));
		Main_Sub ctrl = Main_Sub.get(Integer.parseInt(params[2]));

		// If we're not on main when we weren't before
		if(Main_Sub.MAIN.equals(tx) && Vfo.SUB.equals(model.tx_vfo.get())){
			model.tx_vfo.set(Vfo.A); // Default to A because we have no idea
		} else if(Main_Sub.SUB.equals(tx)){ // If we're at sub
			model.tx_vfo.set(Vfo.SUB);
		}
		model.tx_transceiver.set(tx);
		model.rx_transceiver.set(tx);
		model.ctrl_transceiver.set(ctrl);
	}

	private void id(String[] params) {
		int id = Integer.parseInt(params[1]);
		if(!model.ID.get().equals(id)){
			System.err.println("NOT A TS-2000! Expected ID: 19, actual: "+ id);
		}
	}

	/** Update VFO A frequency **/
	private void vfo_a_f(String[] params) {
		model.vfo_a_frequency.set(Integer.parseInt(params[1]));
	}

	/** Update VFO B frequency **/
	private void vfo_b_f(String[] params) {
		model.vfo_b_frequency.set(Integer.parseInt(params[1]));
	}

	/** Update VFO sub frequency **/
	private void vfo_sub_f(String[] params) {
		model.vfo_sub_frequency.set(Integer.parseInt(params[1]));
	}

	/** Update mode **/
	private void mode(String[] params) {
		model.operating_mode.set(MODE.get(Integer.parseInt(params[1])));
	}

	/** Update AF Gain*/
	private void AFGain(String[] params) {
		model.af_gain.set(Integer.parseInt(params[2]));
	}

	/**
	 * Update RF Gain
	 */
	private void RFGain(String[] params) {
		model.rf_gain_value.set(Integer.parseInt(params[1]));
	}

	/**
	 * Update Transceiver Status
	 */
	private void TransceiverStatus(String[] params) {
		model.transceiver_status.set(params[1]+params[2]+params[3]+params[4]+params[5]+params[6]+params[7]+params[8]+params[9]+params[10]+params[11]+params[12]+params[13]+params[14]+params[15]);
	}

	/**
	 * Update Busy Signal Status
	 */
	private void BusySignalStatus(String[] params) {
		model.busy_signal_status_main.set(Integer.parseInt(params[1]));
		model.busy_signal_status_sub.set(Integer.parseInt(params[2]));
	}
		
	/**
	 * Update Power Status
	 */
	private void PowerStatus(String[] params) {
		model.power_status.set(STATUS.get(Integer.parseInt(params[1])));
	}
	
	/**
	 * Update Reset Transceiver (SR)
	 */
	private void ResetTransceiver(String[] params) {
		model.reset_transceiver.set(RESET.get(Integer.parseInt(params[1])));
	}

	/**
	 * Update Internal TNC Mode (TC)
	 */
	private void InternalTNCMode(String[] params){
		model.internal_tnc_mode.set(TNCMode.get(Integer.parseInt(params[2])));
		model.internal_tnc_mode_actual.set(params[1]+params[2]);
	}
	
	//private void ExtensionMenu(String[] params) {
	//	String EXMenu = params[1]+params[2]+params[3]+params[4]+params[5];
	//	if () {
	//		model.packet_communication_mode.set(EXMenu);
	//	}
	//	else if () {
	//		model.data_transfer_speed_externaltnc.set(EXMenu);
	//	}
	//}
	
	//public final Property<RESET> reset_transceiver = new Property<RESET>(RESET.VFOReset);
	/**	

	public static void main(String[] args) {
		KenwoodTs2kModelUpdater mu = new KenwoodTs2kModelUpdater(new KenwoodTs2kModel());
		mu.update("IF00437450000     0000000000041001130");
	}
	/**
	 * 
	 * @param params
	 */
	private void transceiverStatus(String[] params) {
		//		System.out.printf("Frequency: %s%n", params[1]);
		//		System.out.printf("Step Size: %s%n", params[2]);
		//		System.out.printf("RIT/XIT  : %s%n", params[3]);
		//		System.out.printf("RIT      : %s%n", params[4]);
		//		System.out.printf("XIT      : %s%n", params[5]);
		//		System.out.printf("Bank 1   : %s%n", params[6]);
		//		System.out.printf("Bank 2   : %s%n", params[7]);
		//		System.out.printf("RX 0 TX 1: %s%n", params[8]);
		//		System.out.printf("Mode     : %s%n", params[9]);
		//		System.out.printf("FR/FT    : %s%n", params[10]);
		//		System.out.printf("Scan     : %s%n", params[11]);
		//		System.out.printf("Split    : %s%n", params[12]);
		//		System.out.printf("Tone     : %s%n", params[13]);
		//		System.out.printf("Tone freq: %s%n", params[14]);
		//		System.out.printf("Shift    : %s%n", params[15]);
	}

	/**
	 * Handle unknown responses
	 * @param response
	 */
	private void unknown(String response) {
		model.last_unknown_response.set(response);
		model.unknown_count.set(1 + model.unknown_count.get());
	}

	/**
	 * Handle error responses
	 */
	private void error(String response) {
		model.last_error_reason.set(
				"Command syntax was incorrect or command was not executed due" +
				" to the current status of the transceiver (even though the " +
		"command syntax was correct)");
		model.error_count.set(1 + model.error_count.get());
	}
}


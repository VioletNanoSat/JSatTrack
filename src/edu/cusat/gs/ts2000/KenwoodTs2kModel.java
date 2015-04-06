package edu.cusat.gs.ts2000;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import edu.cusat.common.mvc.Model;

public class KenwoodTs2kModel extends Model<KenwoodTs2kModelListener> {

	/**
	 * ID number for all TS-2000 radios, does not change
	 * <p> Set by: None
	 * <p> Read by: ID
	 */
	public final Property<Integer> ID = new Property<Integer>(19) {
		public void set(Integer value) {
			throw new UnsupportedOperationException("ID is constant");
		};
	};

	/**
	 * Power status. <code>true</code> = on
	 */
	public final Property<Boolean> powered_on = new Property<Boolean>(false);

	/** 
	 * Frequency of VFO A in Hz 
	 * <p> Set by: FA
	 * <p> Read by: FA, IF (when active?)
	 **/
	public final Property<Integer> vfo_a_frequency = new NonNegativeInt(0);

	/** 
	 * Frequency of VFO B in Hz 
	 * <p> Set by: FB
	 * <p> Read by: FB, IF (when active?)
	 **/
	public final Property<Integer> vfo_b_frequency = new NonNegativeInt(0);

	/** 
	 * Frequency of VFO A in Hz
	 * <p> Set by: FC
	 * <p> Read by: FC, IF (when active?) 
	 **/
	public final Property<Integer> vfo_sub_frequency = new NonNegativeInt(0); 

	/** 
	 * Frequency step size
	 * <p> Set by: ?
	 * <p> Read by: IF
	 **/
	public final Property<Integer> frequency_step_size = new NonNegativeInt(0);

	/**
	 * RIT/XIT frequency (+/- 99999?) in Hz
	 * <p> Set by: ?
	 * <p> Read by: IF
	 */
	public final Property<Integer> r_x_it_frequency = new NonNegativeInt(0);

	/**
	 * RIT status, <code>true</code> = on, <code>false</code> = off
	 * <p> Set by: 
	 * <p> Read by: IF
	 */
	public final Property<Boolean> rit_on = new Property<Boolean>(false);

	/**
	 * XIT status, <code>true</code> = on, <code>false</code> = off
	 * <p> Set by: 
	 * <p> Read by: IF
	 */
	public final Property<Boolean> xit_on = new Property<Boolean>(false);

	/**
	 * Operating mode of the transceiver
	 * <p> Set by: MD
	 * <p> Read by: MD, IF
	 */
	public final Property<MODE> operating_mode = new Property<MODE>(MODE.RESERVED);

	/**
	 * Receiving VFO
	 * <p> Set by: FR
	 * <p> Read by: FR, IF
	 */
	public final Property<Vfo> rx_vfo = new Property<Vfo>(Vfo.A);

	/**
	 * Transmitting VFO
	 * <p> Set by: FT
	 * <p> Read by: FT, IF
	 */
	public final Property<Vfo> tx_vfo = new Property<Vfo>(Vfo.A);

	/**
	 * Scan status 
	 * <p> Set by: SC
	 * <p> Read by: SC, IF
	 */
	public final Property<ScanStatus> scan_status = new Property<ScanStatus>(ScanStatus.OFF);

	/**
	 * Split status
	 * <p> Set by: SP
	 * <p> Read by: SP, IF
	 */
	public final Property<MainVfo> split_status = new Property<MainVfo>(MainVfo.A);

	/**
	 * IF_P13 ???
	 * <p> Set by: 
	 * <p> Read by: IF
	 */
	public final Property<If_P13> if_p13 = new Property<If_P13>(If_P13.OFF);

	/**
	 * Tone frequency
	 * <p> Set by: TN
	 * <p> Read by: TN, IF
	 */
	public final Property<Integer> tone_frequency = new NonNegativeInt(0);

	/**
	 * Shift status
	 * <p> Set by: OS
	 * <p> Read by: OS, IF
	 */
	public final Property<ShiftStatus> shift_status = new Property<ShiftStatus>(ShiftStatus.SIMPLEX);

	/**
	 * Menu channel, <code>true</code> iff Menu A
	 * <p> Set by: MF
	 * <p> Read by: MF
	 */
	public final Property<Boolean> menu_channel = new Property<Boolean>(true);

	/**
	 * Output power (Watts)
	 * Valid range, 5-100 TODO only 5-50 for UHF
	 * <p> Set by: PC
	 * <p> Read by: PC
	 */
	public final Property<Integer> output_power = new BoundedInt(5, 100, 100);

	/**
	 * S-Meter Status
	 * <p> Set by: Transceiver
	 * <p> Read by: SM
	 */
	public final Property<Integer> s_meter = new BoundedInt(0, 30, 0);

	/**
	 * Squelch Level
	 * <p> Set by: SQ
	 * <p> Read by: SQ
	 */
	public final Property<Integer> squelch = new BoundedInt(0, 255, 0);

	/**
	 * AF Gain
	 * <p> Set by: AG
	 * <p> Read by: AG
	 */
	public final Property<Integer> af_gain = new BoundedInt(0, 255, 0);

	/**
	 *  Busy Signal Status Main Transceiver
	 * <p> Set by: Transceiver
	 * <p> Read by: BY
	 * false/0: Not busy
	 * true/1: Busy
	 */
	public final Property<Integer> busy_signal_status_main = new Property<Integer>(0);
	
	/**
	 * Busy Signal Status Sub-Receiver
	 * <p> Set by: Transceiver
	 * <p> Read by: BY
	 * false/0: Not busy
	 * true/1: Busy
	 */
	public final Property<Integer> busy_signal_status_sub = new Property<Integer>(0);

	/**
	 * Power Status
	 * <p> Set by: PS
	 * <p> Read by: PS 
	 */
	public final Property<STATUS> power_status = new Property<STATUS>(STATUS.OFF);

	/**
	 * RF Gain Value
	 * <p> Set by: RG
	 * <p> Read by: RG
	 */
	public final Property<Integer> rf_gain_value = new BoundedInt(0, 255, 0);

	/**
	 * Reset Transceiver
	 * <p> Set by: SR
	 * <p> Read by: ?
	 */
	public final Property<RESET> reset_transceiver = new Property<RESET>(RESET.VFOReset);
	
	/**
	 * Retrieve Transceiver Status
	 * <p> Set by: ?
	 * <p> Read by: IF
	 */

	public final Property<String> transceiver_status = new Property<String>(null);
	
	/**
	 * Extension Menu - Data transfer speed: External TNC (EX)#50F
	 * <p> Set by: EX
	 * <p> Read by: EX
	 */
	public final Property<String> data_transfer_speed_externaltnc = new Property<String>(null);
	
	/**
	 * Extension Menu - Packet communication mode (EX)#55
	 * <p> Set by: EX
	 * <p> Read by: EX
	 */
	public final Property<String> packet_communication_mode = new Property<String>(null);
	
	/**
	 * Internal TNC Mode
	 * <p> Set by: TC
	 * <p> Read by: TC
	 */
	public final Property<TNCMode> internal_tnc_mode = new Property<TNCMode>(TNCMode.PacketCommunicationMode);
	
	public final Property<String> internal_tnc_mode_actual = new Property<String>(null); //With the space
	
	/**
	 * TX transceiver, either main (A or B) or sub
	 * <p> Set by: DC
	 * <p> Read by: DC
	 */
	public final Property<Main_Sub> tx_transceiver = new Property<Main_Sub>(Main_Sub.MAIN);

	/**
	 * Transceiver being controlled, either main (A or B) or sub
	 * <p> Set by: DC
	 * <p> Read by: DC
	 */
	public final Property<Main_Sub> ctrl_transceiver = new Property<Main_Sub>(Main_Sub.MAIN);

	/**
	 * Number of errors, e.g. times that '?' or 'E' has been received
	 */
	public final NonNegativeInt error_count = new NonNegativeInt(0);

	/**
	 * The reason for the last error
	 */
	public final Property<String> last_error_reason = new Property<String>("");

	/**
	 * Number of times an unknown response was received
	 */
	public final NonNegativeInt unknown_count = new NonNegativeInt(0);

	/**
	 * The last unknown response received, without the ';'
	 */
	public final Property<String> last_unknown_response = new Property<String>("");

	public Property<Main_Sub> rx_transceiver = new Property<Main_Sub>(Main_Sub.MAIN); 

	public enum ShiftStatus { 
		SIMPLEX(0), PLUS(1), MINUS(2), ALL(3); 

		public final int code;
		ShiftStatus(int value) { code = value; }

		/** Reverse Lookup Map **/
		private static final Map<Integer, ShiftStatus> lookup = new HashMap<Integer, ShiftStatus>();
		static { for (ShiftStatus s : EnumSet.allOf(ShiftStatus.class)) lookup.put(s.code, s); }
		/** Get an enum by reverse lookup **/
		public static ShiftStatus get(int code) { return lookup.get(code); }
	}

	public enum ScanStatus {
		OFF(0), ON(1), MHZ(2), VISUAL(3), TONE(4), CTCSS(5), DCS(6);

		public final int code;
		ScanStatus(int value) { code = value; }

		/** Reverse Lookup Map **/
		private static final Map<Integer, ScanStatus> lookup = new HashMap<Integer, ScanStatus>();
		static { for (ScanStatus s : EnumSet.allOf(ScanStatus.class)) lookup.put(s.code, s); }
		/** Get an enum by reverse lookup **/
		public static ScanStatus get(int code) { return lookup.get(code); }
	}

	public enum If_P13 { OFF, TONE, CTCSS, DCS; }

	public enum MODE {
		LSB(1), USB(2), CW(3), FM(4), AM(5), 
		FSK(6), CR_R(7), RESERVED(8), FSK_R(9);

		public final int code;
		MODE(int value) { code = value; }

		/** Reverse Lookup Map **/
		private static final Map<Integer, MODE> lookup = new HashMap<Integer, MODE>();
		static { for (MODE s : EnumSet.allOf(MODE.class)) lookup.put(s.code, s); }
		/** Get an enum by reverse lookup **/
		public static MODE get(int code) { return lookup.get(code); }
	}
	
	public enum TNCMode {
		PacketCommunicationMode(0), PCControlCommandMode(1);

		public final int mode;
		TNCMode(int value) { mode = value; }

		/** Reverse Lookup Map **/
		private static final Map<Integer, TNCMode> lookup = new HashMap<Integer, TNCMode>();
		static { for (TNCMode s : EnumSet.allOf(TNCMode.class)) lookup.put(s.mode, s); }
		/** Get an enum by reverse lookup **/
		public static TNCMode get(int mode) { return lookup.get(mode); }
	}

	//For Power Status
	public enum STATUS { OFF(0) , ON(1);
	
	public final int status;
	STATUS(int value) {status = value; }
	/** Reverse Lookup Map **/
	private static final Map<Integer, STATUS> lookup = new HashMap<Integer, STATUS>();
	static { for (STATUS s : EnumSet.allOf(STATUS.class)) lookup.put(s.status, s); }
	/** Get an enum by reverse lookup **/
	public static STATUS get(int status) { return lookup.get(status); }
	}

	//public static Integer get(int parseInt) {
		// TODO Auto-generated method stub
		//return parseInt;
	//} 
	

	public enum RESET { 
		VFOReset(1), MasterReset(2);

		public final int reset;
		RESET(int value) { reset = value; }
		/** Reverse Lookup Map **/
		private static final Map<Integer, RESET> lookup = new HashMap<Integer, RESET>();
		static { for (RESET s : EnumSet.allOf(RESET.class)) lookup.put(s.reset, s); }
		/** Get an enum by reverse lookup **/
		public static RESET get(int reset) { return lookup.get(reset); }

	}

	public enum FR_FT {
		VFO_A(0), VFO_B(1), M_CH(2), CALL(3);

		public final int code;
		FR_FT(int value) { code = value; }

		/** Reverse Lookup Map **/
		private static final Map<Integer, FR_FT> lookup = new HashMap<Integer, FR_FT>();
		static { for (FR_FT s : EnumSet.allOf(FR_FT.class)) lookup.put(s.code, s); }
		/** Get an enum by reverse lookup **/
		public static FR_FT get(int code) { return lookup.get(code); }
	}

	public enum Vfo {
		A(0), B(1), SUB(2);
		public final int code;
		Vfo(int value) { code = value; }

		/** Reverse Lookup Map **/
		private static final Map<Integer, Vfo> lookup = new HashMap<Integer, Vfo>();
		static { for (Vfo s : EnumSet.allOf(Vfo.class)) lookup.put(s.code, s); }
		/** Get an enum by reverse lookup **/
		public static Vfo get(int code) { return lookup.get(code); }
	}

	public enum Main_Sub {
		MAIN(0), SUB(1);
		public final int code;
		Main_Sub(int value) { code = value; }

		/** Reverse Lookup Map **/
		private static final Map<Integer, Main_Sub> lookup = new HashMap<Integer, Main_Sub>();
		static { for (Main_Sub s : EnumSet.allOf(Main_Sub.class)) lookup.put(s.code, s); }
		/** Get an enum by reverse lookup **/
		public static Main_Sub get(int code) { return lookup.get(code); }
	}

	public enum MainVfo { 
		A(0), B(1);
		public final int code;
		MainVfo(int value) { code = value; }

		/** Reverse Lookup Map **/
		private static final Map<Integer, MainVfo> lookup = new HashMap<Integer, MainVfo>();
		static { for (MainVfo s : EnumSet.allOf(MainVfo.class)) lookup.put(s.code, s); }
		/** Get an enum by reverse lookup **/
		public static MainVfo get(int code) {
			return lookup.get(code);
		}
	}
}

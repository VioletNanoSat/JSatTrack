package edu.cusat.gs.ts2000;

import edu.cusat.gs.util.HelperMethods;

/**
 * Various Commands for TS-2000
 */
public class OtherCommandClass {
	
	/**
	 * Constructor
	 * @param communicator
	 */
	
	/**
     * Sets ALT - ON/OFF
     * 
     * @param int p - If '0': OFF, If '1': ON
     * @return Integer: If 0: OFF, 1: ON, -1: Error
     */
	public int setALTPwr(int param) {
		if (HelperMethods.isBinary(param)) {
			return HelperMethods.sendToCommunicatorInt(Command.ALT_STATUS, param + "");    
		}
		System.out.println("Error: param from setALTPwr = " + param);
		return -1;
	}
	
	/**
     * Reads ALT function status
     * 
     * @return Integer: If 0: OFF, 1: ON, -1: Error
     */
	public int getALTPwr() {
		return HelperMethods.sendToCommunicatorInt(Command.ALT_STATUS, null);    
	}
	
	
	/**
     * Sets Auto Mode - ON/OFF
     * 
     * @param int p - If '0': OFF, If '1': ON
     * @return Integer: If 0: OFF, 1: ON, -1: Error
     */
	public int setAutoMode(int param) {
		if (HelperMethods.isBinary(param)) {
			return HelperMethods.sendToCommunicatorInt(Command.AUTO_MODES, param + "");    
		}
		System.out.println("Error: param from setAutoMode = " + param);
		return -1;
	}
	
	/**
     * Sets Auto Mode ON/OFF
     * 
     * @return Integer: If 0: OFF, 1: ON, -1: Error     * 
     */
	public int getAutoMode() {
		return HelperMethods.sendToCommunicatorInt(Command.AUTO_MODES, null);  
	}
	
	
	/**
     * Reads Main Transceiver - Antenna Connection

     * @return int  0: Not Busy, 1: Busy, -1: Error
     */
	public int getMainTranscvStatus() {
		try {
//			String s = comm.send(Command.BUSY_SIGNAL_STATUS, "");
//            int r = Integer.parseInt(s.substring(0, 1));
//            if (HelperMethods.isBinary(r)) {	//check to make sure answer is 0 or 1
//            	return r;
//            }
//            System.out.println("'r' from method getMainTranscvStatus from class OtherCommandClass = " + r);	//debug
            return -1;
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
		
	}
	
	/**
     * Reads  Sub Receiver - Antenna Connection

     * @return int  0: Not Busy, 1: Busy, -1: Error
     */
	public int getSubRecvStatus() {
		try {
//			String s = comm.send(Command.BUSY_SIGNAL_STATUS, "");
//            int r = Integer.parseInt(s.substring(1, 2));
//            if (HelperMethods.isBinary(r)) {	//check to make sure answer is 0 or 1
//            	return r;
//            }
//            System.out.println("'r' from method getSubRecvStatus from class OtherCommandClass = " + r);	//debug
            return -1;
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
		
	}

	
	/**
     * Sets Carrier Gain 
     * @param int: gain in numerical format: 000(min)-100(max)
     * @return int: gain in numerical format: 000(min)-100(max), or -1: Error
     */
	public int setCarrierGain(int param) {
		if (0 <= param && param <= 100) {
			String s = String.format("%03d", param);
			return HelperMethods.sendToCommunicatorInt(Command.CARRIER_GAIN, s);
		}
		System.out.println("Error: param from setCarrierGain = " + param);
		return -1;		//Error
	}
	
	/**
     * Sets Carrier Gain 
     * @return int: gain in numerical format: 000(min)-100(max)
     */
	public int getCarrierGain() {
		return HelperMethods.sendToCommunicatorInt(Command.CARRIER_GAIN, null);
	}
	
	
	/**
     * Sets AF Gain for the Main Transceiver
     * 
     * @param p2 - Integer: 000(min) to 255(max)
     * @return int: 000-255, -1: Error
     */
	public int setAFGainMainTranscv(int param) {
		if (0 <= param && param <= 255) {
			return HelperMethods.sendToCommunicatorInt(Command.AF_GAIN, "0" + String.format("%03d", param));
		}
		System.out.println("Error: param from setAFGainMainTranscv = " + param);
		return -1;
		
	}
	
	/**
     * Sets AF Gain for the Sub Receiver
     * 
     * @param p2 - Integer: 000(min) to 255(max)
     * @return int: 000-255, -1: Error
     */
	public int setAFGainSubRecv(int param) {
		if (0 <= param && param <= 255) {
			return HelperMethods.sendToCommunicatorInt(Command.AF_GAIN, "1" + String.format("%03d", param));
		}
		System.out.println("Error: param from setAFGainSubRecv = " + param);
		return -1;
		
	}
	
	/**
     * Gets AF Gain for the Main Transceiver

     * @return int: 000-255,
     */
	public int getAFGainMainTranscv() {
		return HelperMethods.sendToCommunicatorInt(Command.CARRIER_GAIN, "0");
	}
	
	/**
     * Gets AF Gain for the Sub Receiverr

     * @return int: 000-255,
     */
	public int getAFGainSubRecv() {
		return HelperMethods.sendToCommunicatorInt(Command.CARRIER_GAIN, "1");
	}
	
	
	/**
	 * Sets current frequency to the Channel frequency
	 * EDIT ME ---> NEED TO FIND COMMAND FOR: return double: frequency (MHz)
	 */
	public void setCurrentFrqToChanFrq() {					// EDIT ME!
	    HelperMethods.sendToCommunicatorDbl(Command.SET_FREQ_TO_CALL_FREQ, null);
	}
	
	
	/**
	 * Set Fine Status On/OFF
	 * @param int - 0: OFF, 1: ON
	 * @return int - 0: OFF, 1: ON, -1: Error
	 */
	public int setFinePwr(int param) {
		if (HelperMethods.isBinary(param)) {
			return HelperMethods.sendToCommunicatorInt(Command.FINE_FUNC_STATUS, param + "");
		}
		System.out.println("Error: param from setFinePwr = " + param);
		return -1;
	}
	
	/** Get Fine Status On/OFF
	 *
	 * @return int - 0: OFF, 1: ON
	 */
	public int getFinePwr() {
		return HelperMethods.sendToCommunicatorInt(Command.FINE_FUNC_STATUS, null);
	}
	
	
	/**
     * Sets Channel Numbers in Banks 0,1,2
     * 
     * @param1 int: Bank number (0 (No bank), 1-3),
     * @param2 int: Channel (00-99)
     * @return channel 00-99
     */
	public int setMemoryChannel(int param1, int param2) {
		if (0 <= param2 && param2 <= 99) {
			if (param1 == 0) {
				return HelperMethods.sendToCommunicatorInt(Command.MEM_CHAN, " " + String.format("%02d",param2));
			}
			else if (1 <= param1 && param1 <= 3) {
				return HelperMethods.sendToCommunicatorInt(Command.MEM_CHAN, (param1-1) + String.format("%02d",param2));
			}
			System.out.println("Error: param1 from setMemoryChannel = " + param1);
			return -1;		// error
		}
		System.out.println("Error: param2 from MemoryChannel = " + param2);
		return -1;			// error
	}
	
	/**
     * Gets Channel Numbers from current Bank
     * 
     * @return channel 000-299: (00-99 mean bank 1, 100-199 mean bank 2, 200-299 mean bank 3)
     */
	public int getMemoryChannel() {
		return HelperMethods.sendToCommunicatorInt(Command.MEM_CHAN, null);
	}
	
	
    /**
     * Enables or disables the RIT (Receive Incremental Tuning) function.
     * 
     * RIT provides the ability to change the receive frequency by +/- 20.00 kHz
     * in steps of 10 Hz without changing your transmit frequency. If the Fine
     * Tuning function is ON, the step size is 1 Hz. RIT works equally with all
     * modulation modes and while using VFO or Memory Recall Mode, but only for
     * the main transceiver.
     * 
     * When the current frequency is stored in memory, the RIT offset is also
     * stored.
     * 
     * @param int 0: OFF, 1: ON,
     * @return int 0: OFF, 1: ON, -1: Error
     */
    public int setRitStatus(int param) {
        if (HelperMethods.isBinary(param)) {
        	HelperMethods.sendToCommunicatorInt(Command.RIT_FUNC_STATUS, param + "");
        }
        System.out.println("Error: param from setRITStatus = " + param);
        return -1;
    }

    /**
     * Get the status of the RIT function
     * 
     * @return int - 0: Off, 1: On
	 */
    public int getRitStatus() {
    	return HelperMethods.sendToCommunicatorInt(Command.RIT_FUNC_STATUS, null);
    }
    
	
    /**
     * Sets receiver (VFO A/VFO B/M.CH/COM.) - Applied to current Control Band
     * 
     * @param int - 0: VFO A, 1: VFO B, 2: M.CH, 3: COM.
     * @return int status, -1: Error
     */
	public int selectReceiver(int param) {
		if (0 <= param && param <= 3) {
			HelperMethods.sendToCommunicatorInt(Command.RX_A_B, param + "");
		}
		System.out.println("Error: param from selectReciever = " + param);
		return -1;
	}
	
	/**
     * Gets receiver (VFO A/VFO B/M.CH/COM.) - Applied to current Control Band
     * 
     * @return int - 0: VFO A, 1: VFO B, 2: M.CH, 3: COM., -1: Error
     */
	public int getReceiverSelected() {
		return HelperMethods.sendToCommunicatorInt(Command.RX_A_B, null);
	}
	
	
	/**
     * Reads Transmitter (VFO A/VFO B/M.CH/CALL)
     * 
     * @return int: 0: VFO A, 1: VFO B, 2: M.CH, 3: CALL
     */
	public int getTransmitterSelected() {
		return HelperMethods.sendToCommunicatorInt(Command.TX_A_B, null);
	}
	
	/**
     * Sets Transmitter (VFO A/VFO B/M.CH/CALL) - Applied to current TX band
     * 
     * @param int - 0: VFO A, 1: VFO B, 2: M.CH, 3: CALL
     * @return true if successful, otherwise false
     */
	public int selectTransmitter(int param) {
        if (param >= 0 && param <= 3) {
            return HelperMethods.sendToCommunicatorInt(Command.TX_A_B, param + "");
        } 
        System.out.println("Error: param from selectTransceiver = " + param);
        return -1;
	}
	
	/**
     * Gets Id number to confirm connection to TS-2000
     * 
     * @return true if connected to TS-2000, false
     */
	public boolean transceiverID() {
		int k = HelperMethods.sendToCommunicatorInt(Command.TRANSCEIVER_ID, null);
		if (k == 19) {
			return true;
		}
		System.out.println("Error: Transceiver ID number 'k' = " + k);
		return false;
	}
	
	/**
     * Reads Transceiver Status - NOT FINISHED
     * 
     * @param int 
     * 			  - 1: Frequency (in Hertz), 
     * 				2: Frequency Step Size (in what?), 
     * 				3: RIT / XIT freq. in +/-99999 (in Hz), 
     * 				4: RIT off/on, 
     * 				5: XIT off/on, 
     * 				6 and 7: Specify channel bank number (See Memory Channel Cmd), 
     * 				8: RX / TX, 
     * 				9: Operating Mode (See Operating Mode Status), 
     * 				10: (See SetReceiver and SetTransceiver), 
     * 				11: Scan Function Status, 
     * 				12: Split Operation Command (Don't know what this is?), 
     * 				13: 0: OFF, 1: TONE, 2: CTCSS, 3: DCS, 
     * 				14: Tone frequency (01-39) (refer to page 35 of TS-2000 user manual for more instructions), 
     * 				15: Shift Status (see OffsetFuncStatus)
     * 
     * @return int 
     * 			  - For 1: 11 bits - Frequency (Hz) = 99999999999, 
     * 				For 2: 4 bits - Frequency Step Size (in what?), 
     * 			    For 3: 6 bits - RIT / XIT Freq (Hz) = +/-99999, 
     * 				For 4: 1 bit  - 0:OFF, 1:ON, 
     * 				For 5: 1 bit - 0:OFF, 1:ON, 
     * 				For 6 and 7: 3 bits - Specify channel bank number (See Memory Channel), 
     * 				For 8: 1 bit - 0:RX, 1:TX, 
     * 				For 9: 1 bit - Operating Mode - See Operating Mode Status, 
     * 				For 10: 1 bit - (See SetReceiver and SetTransceiver), 
     * 				For 11: 1 bit - Scan Function Status
     * 						(0: OFF, 1: ON, 2: MHz ON, 3: Visual ON, 4: Tone ON, 5: CTCSS ON, 6: DCS ON), 
     * 				For 12: 1 bit - Split Operation Command (Don't know what this is?), 
     * 				For 13: 1 bit - 0: OFF, 1: TONE, 2: CTCSS, 3: DCS, 
     * 				For 14: 2 bits - Tone frequency (01-39) (refer to page 35 of TS-2000 user manual for more instructions), 
     * 				For 15: 1 bit - Shift Status (see OffsetFuncStatus)
     * @ When operating in Sky Command II+, Parameters 2, 15 become blank, and 2, 3 become 5 bits each.
     * 
     */
	public int transceiverStat(int k) {
//		Controller.sendSerialPort(Identifier + ";");			// Identifier = "IF"
//		String SerialPort = answer();
//		
//		int[] Commands = new int[14];
//		
//		int Hertz = StrToInt(SerialPort.substring(2, 13));						//1
//		int FreqStepSize = StrToInt(SerialPort.substring(13, 17));				//2
//		int RIT_XIT_Freq = StrToInt(SerialPort.substring(17, 23));				//3
//		int RIT_ON_OFF = StrToInt(SerialPort.substring(23, 24));				//4
//		int XIT_ON_OFF = StrToInt(SerialPort.substring(24, 25));				//5
//		int BitSIX = StrToInt(SerialPort.substring(25, 26));					//6	
//		int BitSEVEN = StrToInt(SerialPort.substring(26, 28));					//7
//		int RX_TX = StrToInt(SerialPort.substring(28, 29));						//8
//		int OperatingMode = StrToInt(SerialPort.substring(29, 30));				//9
//		int Receiver_Transceiver = StrToInt(SerialPort.substring(30, 31));		//10
//		int ScanFuncStatus = StrToInt(SerialPort.substring(31, 32));			//11
//		int SplitOperation = StrToInt(SerialPort.substring(32, 33));			//12
//		int P13 = StrToInt(SerialPort.substring(33, 34));						//13
//		int ToneFreq = StrToInt(SerialPort.substring(34, 36));					//14
//		int ShiftStatus = StrToInt(SerialPort.substring(36, 37));				//15
//		
//		Commands[0] = Hertz;
//		Commands[1] = FreqStepSize;
//		Commands[2] = RIT_XIT_Freq;
//		Commands[3] = RIT_ON_OFF;
//		Commands[4] = XIT_ON_OFF;
//		Commands[5] = BitSIX;
//		Commands[6] = BitSEVEN;
//		Commands[7] = RX_TX;
//		Commands[8] = OperatingMode;
//		Commands[9] = Receiver_Transceiver;
//		Commands[10] = ScanFuncStatus;
//		Commands[11] = SplitOperation;
//		Commands[12] = P13;
//		Commands[13] = ToneFreq;
//		Commands[14] = ShiftStatus;
//		
//		return Commands[k-1];
		return 0;
	}
	
	/**
	 * Gets Hertz from Transceiver
	 * 
	 * @return Frequency (in Hz) or -1 if an error occured  
	 */
	public int getHertz() {
		try {
//            String anw = comm.send(Command.TRANSCEIVER_STATUS, "");
//            return Integer.parseInt(anw.substring(0, 12));
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
	}

	/**
	 * NOT STARTED
	 * @return
	 */
	public int getFreqStepSize() {
		try {
//            return Integer.parseInt(comm.send(Command.STEP_FREQ, "").substring(12, 16));
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
		
}

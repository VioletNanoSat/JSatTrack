//package edu.cusat.gs.unused;
//
//
///*
// * Finished
// */
//public class TXBandStatus extends Ts2000Cmd {
//
//	/**
//	 * Constructor method
//	 */
//	public TXBandStatus () {
//		identifier = "DC";
//	}
//	
//	/**
//     * Reads TX Band Status
//     * @param k - If k = 0: TX Band is set to either Main Transceiver or Sub Receiver,
//     * 			     k = 1: Control Band is set to either Main Transceiver or Sub Receiver
//     * @return int 0: Main Transceiver, 1: Sub Receiver
//     * @ When TX Band is set, the same setting is applied to Control Band
//     */
//	public int read(int k) {
//		controller.sendSerialPort(identifier + ";");
//		
//		String serialReturnVal = answer();
//		int[] p = new int[1];
//		p[0] = Integer.parseInt(serialReturnVal.substring(2, 3));
//		p[1] = Integer.parseInt(serialReturnVal.substring(3, 4));
//		
//		return p[k];
//	}
//	
//	/**
//     * Reads TX Band Status
//     * @param int p[0] and int p[1] -> If p[0] = 0 (Move TX Band to Main Transceiver), If p[0] = 1 (Move to Sub Receiver),
//     * 			                       If p[1] = 0 (Control Band to Main Transceiver), If p[1] = 1 (Move to Sub Receiver)
//     * @return true if command is sent, false otherwise
//     *@ When TX Band is set, the same setting is applied to Control Band
//     */
//	public boolean set(int[] p) {
//		int initialValPOne = read(0);
//		
//        if (binary(p[0]) && binary(p[1])) {
//        	if (p[0] != initialValPOne) { 
//        		p[1] = p[0];
//        	}
//            return controller.sendSerialPort(identifier + p[0] + p[1] + ";");
//        } 
//        else {
//            return false;
//        }
//	}
//}

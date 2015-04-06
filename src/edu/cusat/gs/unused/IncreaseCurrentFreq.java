//package edu.cusat.gs.unused;
//
//
///**
// * NOT FINISHED - ??? WHAT IS THE DIFFERENCE BETWEEN THIS AND 'CI' ???
// * @author David Bjanes
// *
// */
//public class IncreaseCurrentFreq extends Ts2000Cmd {
//
//	/**
//	 * Constructor method
//	 */
//	public IncreaseCurrentFreq () {
//		identifier = "CH";
//	}
//	
//	/**
//     * No Read Function for This Method
//     */
//	public int read() {
//		return 99999999;
//	}
//	
//	/**
//     * Sets the current frequency to CALL channel 
//     * @param int p[0] - If '0': Move the MULTI/ CH control 1 step up, If '1': Move the MULTI/ CH control 1 step down
//     * @return true if successful, otherwise false
//     */
//	public boolean set(int[] p) {
//        if (binary(p[0])) {
//            return controller.sendSerialPort(identifier + p[0] + ";");
//        } 
//        else {
//            return false;
//        }
//	}
//}

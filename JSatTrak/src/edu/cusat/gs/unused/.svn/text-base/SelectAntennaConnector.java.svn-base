//package edu.cusat.gs.unused;
//
//import java.io.IOException;
//
//
///**
// * Finished - Used for templet for other cmd classes - Not A Priority
// * @author David Bjanes
// *
// */
//public class SelectAntennaConnector extends Ts2000Cmd  {
//
//	private static final int ANT_1 = 1;
//	private static final int ANT_2 = 2;
//	
//	/**
//	 * Constructor method
//	 */
//	public SelectAntennaConnector() {
//		identifier = "AN";
//	}
//
//	/**
//     * Reads Antenna Connector
//     * 
//     * @return int If '1': ANT1, If '2': ANT2
//     */
//	public int read() throws IOException {
//		controller.sendSerialPort(identifier + ";");
//		
//		String SerialReturnVal = answer();
//		int num = Integer.parseInt(SerialReturnVal.substring(2, 3));
//		if ( (SerialReturnVal.isEmpty() == false) && binary(num-1) ) 
//		{
//			return num;
//		}
//		return 0; 		//default
//	}
//	
//	/**
//     * Selects Antenna Connector ANT1/ANT2
//     * 
//     * @param p1 - If '1': Selects ANT1, If '2': Selects ANT2
//     * @return true if successful, otherwise false
//     */
//	public boolean set(int[] p) {
//		int firstBit = p[0];
//		if (p[firstBit] == ANT_1 || p[firstBit] == ANT_2) {
//            return controller.sendSerialPort("AN" + p[firstBit] + ";");
//        } 
//		else {
//            return false;
//        }
//	}
//}

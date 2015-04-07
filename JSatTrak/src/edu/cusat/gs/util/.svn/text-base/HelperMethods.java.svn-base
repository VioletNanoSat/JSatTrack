package edu.cusat.gs.util;

import edu.cusat.gs.ts2000.Command;

public final class HelperMethods {

	private HelperMethods(){}
	
	/** Helper Method - Changes (double)MHz to (int)Hertz
	 * @param double MHz
	 * @return int Hz
	 */
	public static int megaHertzToHertz(double hz) {
		return (int) (hz * Math.pow(10,6));
	}

	/**
	 * Creates a string from an integer, left padding with zeros if necessary
	 * 
	 * @param num
	 *            the number to convert to string
	 * @param digits
	 *            the minimum number of digits to return
	 * @return the string representation of <code>num</code>, with leading zeros
	 *         so that it has at least <code>digits</code> digits
	 */
	public static String intToString(int num, int digits){
		String format = String.format("%%0%dd", digits);
		return String.format(format, num);
	}
	
	/**
     * Helper Method: Check that an integer is either '0' or '1'
     * @param int: input value
     * @return true or false
     */
    public static boolean isBinary(int num) {
        return num == 0 || num == 1;
    }
    
    /**
     * Helper Method: Checks if integer is between 0-9
     * @param int: input value
     * @return true if the above condition is true, otherwise false
     */
    public static boolean isDecimal(int num) {
    	return (0 <= num && num <= 9);
    }
    
	/**
	 * Helper method: Sends command to Communicator with integer parameters
	 * @param comm TODO
	 * @param cmd: Command from class Command
	 * @param args: parameters in String
	 * @return double: parameter from TS-2000
	 */
    @Deprecated public static int sendToCommunicatorInt(Command cmd, String args) {
		try {
//            return Integer.parseInt(comm.send(cmd, args) );
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
	}
	
	/**
	 * Helper method: Sends command to Communicator with double parameters
	 * @param comm TODO
	 * @param cmd: Command from class Command
	 * @param args: parameters in String
	 * @return double: parameter from TS-2000
	 */
    @Deprecated public static double sendToCommunicatorDbl(Command cmd, String args) {
		try {
//            return Double.parseDouble(comm.send(cmd, args) );
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        return -1;
	}
}

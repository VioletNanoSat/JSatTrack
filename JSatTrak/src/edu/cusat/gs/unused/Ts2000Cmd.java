package edu.cusat.gs.unused;

import java.io.IOException;


public class Ts2000Cmd {

	protected String identifier;
	protected int[] param;
	protected Ts2000 controller;
		
	/**
	 * Reads SerialPort and returns integer parameters after the first two Ascii bits
	 * @return integer parameters after the first two Ascii bits
	 * @throws IOException 
	 */
	public int read() throws IOException {
		controller.sendSerialPort(identifier + ";");
		return Integer.parseInt(answer().substring(2));
	}
		
	/**
	 * Set the method 
	 * @param p
	 * @return
	 */
	public boolean set(int[] p) throws IOException {
		return false;	//default
	}
	
	/**
	 * Returns raw data from Ts-2000
	 * @return String line from Serial Port
	 */
	protected String answer() {
		controller.sendSerialPort(identifier + ";");
		String returnVal = controller.readSerialPort();
		if (returnVal.isEmpty() == false){
			return returnVal;
		}
		else {
			return null;
		}
	}
	
    /**
     * Helper Method: Check that an integer is either '0' or '1'
     * @param int: input value
     * @return true or false
     */
    protected boolean binary(int num) {
        return num == 0 || num == 1;
    }
    
    /**
     * Helper Method: Checks if integer is between 0-9
     * @param int: input value
     * @return true if the above condition is true, otherwise false
     */
    protected boolean isDecimal(int num) {
    	return (0 <= num && num <= 9);
    }
}

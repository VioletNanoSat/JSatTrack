package edu.cusat.gs.unused;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

public class Ts2000 {

    private SerialPort cSerialPort;
    private Writer cOut;
    private InputStreamReader cIn;

    /** Buffer to read from serial port into */
    private char[] cBuffer = new char[2048]; // Just a random, large number

    public static void main(String[] args) {
        try {
            System.out.println(intToString(1, 3));
            System.out.println(intToString(14, 5));
            System.out.println(intToString(920, 2));
            System.out.println((int)(Math.log(10)/Math.log(Math.pow(10,0))));
            System.out.println(Math.log(Math.pow(10,0)));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Ts2000(String port) throws NoSuchPortException, IOException,
            PortInUseException, UnsupportedCommOperationException {
        CommPortIdentifier commPort = CommPortIdentifier
                .getPortIdentifier(port);

        cSerialPort = (SerialPort) commPort.open(getClass().getName(), 10000);
        cSerialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        cSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        cOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                cSerialPort.getOutputStream(), "US-ASCII")));
        cIn = new InputStreamReader(cSerialPort.getInputStream(), "US-ASCII");
    }

    /**
     * Formats an integer into a string of a set number of digits
     * 
     * @param num the number to format
     * @param digits the minimum number of digits
     * @return the formatted number
     */
    private static String intToString(int num, int digits) {
        StringBuffer s = new StringBuffer(digits);
        int zeroes = digits - (int) (Math.log(num) / Math.log(10)) - 1; 
        for (int i = 0; i < zeroes; i++) {
            s.append(0);
        }
        return s.append(num).toString();
    }

    /**
     * Reads from the serial port. If no data is available, this method waits.
     * 
     * @return the data read from the serial port, encoded as an ASCII string,
     *         or <code>null</code> if an IOException occurs
     */
    public String readSerialPort() {
        try {
            int len = cIn.read(cBuffer);
            return String.copyValueOf(cBuffer, 0, len);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sends the given ASCII string over the serial port
     * 
     * @param s
     * @return the success of the operation
     */
    public boolean sendSerialPort(String s) {
        try {
            cOut.write(s + ";");
            
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
}

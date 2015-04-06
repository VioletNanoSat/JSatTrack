package edu.cusat.common;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Gateway-to-gateway message constants. Used by both this and InControl so that both speak the same language.
 *
 * @author Zoe Chiang
 * @author Nate Parsons nsp25
 */
public class GTG
{

    public static final byte CMD_SUCCESS = (byte) 0x00;

    public static final byte CMD_FAILURE = (byte) 0x01;

    public static final byte CMD_DATA = (byte) 0x02;

    public static final byte TLM_DATA = (byte) 0x03;

    public static final byte RADIO_CONNECTED = (byte) 0x04;

    public static final byte RADIO_DISCONNECTED = (byte) 0x05;

    public static final byte RADIO_STATUS = (byte) 0x06;

    public static final byte CONNECTION_STATUS = (byte) 0x07;

    public static final byte CONNECTED = (byte) 0x08;

    public static final byte GTG_DISCONNECT = (byte) 0x09;
    
    public static final byte RADIO_PACKET = (byte) 0x0A;

    /**
     * Byte that InControl must send to JSatTrak (CtrlTcpProvider only for now) 
     * upon connection, and then JSatTrak must echo (CtrlTcpProvider only again)
     */
    public static final byte HELLO_BYTE = (byte) 0x0B;

	/**
	 * Tells JSatTrak to calculate ground passes for the current ground station,
	 * and the specified satellite and duration. Requires __ bytes to follow in
	 * this format:
	 * <p>
	 * <tt>"Name\n([TLE\n]Start\nStartTime\n(EndTime|Duration)\n|"Fake"\n)KTHX"</tt>
	 * <p>
	 * "Fake" will cause JSatTrak to output dummy results
	 * <p>
	 * Where
	 * <p>
	 * Name - the name of the satellite
	 * <p>
	 * TLE - optional only if the satellite is not currently in JSatTrak's
	 * system Otherwise, it specifies the TLE to associate with the satellite.
	 * Will overwrite existing satellite/TLE combination, if it exists.
	 * <p>
	 * StartTime - the time to start checking for ground passes.
	 * <p>
	 * EndTime - the time to stop checking for ground passes
	 * <p>
	 * Duration - how many days after StartTime to check for ground passes
	 * (integer or decimal)
	 * <p>
	 * StartTime and EndTime must be in UTC timezone and in the following
	 * format:
	 * <p>
	 * <tt>yyyy-MM-ddTHH:mm:ss.f</tt>
	 * <p>
	 * where yyyy is the year, MM is month, dd is day, HH is hours (0-24), mm in
	 * minutes, ss is seconds, and f is the decimal part of a second. This 
	 * happens to be the default when you call 
	 * <tt>DateTime.withZone(DateTimeZone.UTC).toString()</tt>
	 */
    public static final byte CTRL_CALC_GROUND_PASSES = 0x0C;
    
    public static String formGroundPassCmd(String name, String tle, DateTime Start, DateTime End, double duration, boolean fake) {
    	if (fake) {
    		return name+ "\n" + "Fake\n" + "KTHX\n";
    	} else {    		
    		return name+"\n"+ (tle==null?"":tle) + "Start\n" + Start.withZone(DateTimeZone.UTC).toString() + "\n" + (End==null?"Duration\n" + duration:"End\n" +End.withZone(DateTimeZone.UTC)) + "\nKTHX\n";
    	}
    }

    public static final int TOP_CMD_PORT = 3333;

    public static final int TOP_TLM_PORT = 4444;
    
    public static final int BOT_CMD_PORT = 3332;
    
    public static final int BOT_TLM_PORT = 4443;

    /** The port that CtrlTcpProvider listens on **/
    public static final int CTRL_PORT = 5555;

    public static final String GSG_ADDR = "localhost";
    
    public static final String RFG_ADDR = "localhost";//"192.168.2.118";

//    offset            = 'Z' | (('+' | '-') HH [':' mm [':' ss [('.' | ',') SSS]]])
    public static void main(String[] args) {
    	DateTime time = ISODateTimeFormat.dateTimeParser().parseDateTime("2010-02-25T21:02:05.25");
    	System.out.println(time.withZone(DateTimeZone.UTC).toString());
    	System.out.println(formGroundPassCmd("MAZZSAT", null, time, new DateTime(), 0, false));
    	System.out.println(formGroundPassCmd("MAZZSAT", null, time, new DateTime(), 0, true));
	}

}

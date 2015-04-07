package edu.cusat.gs.ts2000;

import edu.cusat.common.Failure;
import edu.cusat.gs.ts2000.KenwoodTs2kModel.MainVfo;
import edu.cusat.gs.ts2000.KenwoodTs2kModel.Vfo;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;

public class KenwoodTs2kController implements SerialPortEventListener {

    /** Maximum Frequency, Hz **/
    private static final int FREQ_MAX = 449999999;
    protected CommPortIdentifier portId;
    protected SerialPort port;
    protected boolean connected;
    private KenwoodTs2kModel model = new KenwoodTs2kModel();
    private Timer spoller = new Timer("TS-2000_StatusPoller", true);
    private Reader in;
    private OutputStreamWriter out;
    private KenwoodTs2kModelUpdater modelUpdater = new KenwoodTs2kModelUpdater(model);
    private Vfo myRxVfo = null;
    private Vfo myTxVfo = null;

    public KenwoodTs2kController(CommPortIdentifier portid) {
        setPort(portid);
    }

    public KenwoodTs2kController() {
    }

    public void registerListener(KenwoodTs2kModelListener ml) {
        model.addModelListener(ml);
    }

    public void setPort(CommPortIdentifier portIdentifier) {
        portId = portIdentifier;
    }

    /** Connect to the physical transceiver, if necessary **/
    public void connect() throws Failure {
        if (portId == null) {
            throw new Failure("No port set");
        }

        try {
            port = (SerialPort) portId.open(getClass().getName(), 10000);
        } catch (PortInUseException e) {
            throw new Failure("Port in use by " + e.currentOwner, e);
        }

        try {
            port.setSerialPortParams(9600, SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN
                    | SerialPort.FLOWCONTROL_RTSCTS_OUT);
        } catch (UnsupportedCommOperationException e) {
            throw new Failure(e);
        }

        port.setRTS(true);
        port.notifyOnDataAvailable(true);

        try {
            port.addEventListener(this);
        } catch (TooManyListenersException e) {
            throw new Failure(e);
        }

        try {
            in = new BufferedReader(new InputStreamReader(
                    port.getInputStream(), "US-ASCII"));
            out = new OutputStreamWriter(port.getOutputStream(), "US-ASCII");
        } catch (IOException e) {
            throw new Failure(e);
        }

        connected = true;
    }

    /**
     * Starts asking the TS-2000 for its status
     * @param period how often to check status (in ms)
     */
    public void startPolling(int period) {
        spoller.schedule(new StatusPoller(), 0, period);
    }

    /** Delegate method to send a command without arguments **/
    void send(Command c) {
        send(c.toString());
    }

    /** Delegate method to send a command with arguments **/
    void send(Command c, String args) {
        send(c.toString() + (args != null ? args : ""));
    }

    /** Method to send anything, making sure there is a trailing ';' **/
    public void send(String msg) {
        if (!connected) {
            return;
        }
        try {
            String toSend = msg + (msg.endsWith(";") ? "" : ";");
            System.out.println("Sending to TS-2000: " + toSend);
            out.write(toSend);
            out.flush();
            // If it wasn't a read command, send one unless S-meter command sent
            if (toSend.length() > 3 && !msg.startsWith("SM") && !msg.startsWith("SQ")) {
                send(toSend.substring(0, 2));
            }
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
    }

    /**
     * Disconnect from the physical transceiver, if necessary
     */
    public void disconnect() {
        port.close();
        connected = false;
    }
    
    

    public void powerOn() {
        send(Command.PWR_STATUS, "1");
    }

    public void powerOff() {
        send(Command.PWR_STATUS, "0");
    }

    public boolean isOn() {
        send(Command.PWR_STATUS, "");
        return model.powered_on.get();
    }

    public boolean isConnected() {
        return connected;
    }
    /** Map from Vfo's to the commands for their frequencies **/
    private static final HashMap<Vfo, Command> vfo2freqCmd = new HashMap<Vfo, Command>(3);

    static {
        vfo2freqCmd.put(Vfo.A, Command.VFO_A_FREQ);
        vfo2freqCmd.put(Vfo.B, Command.VFO_B_FREQ);
        vfo2freqCmd.put(Vfo.SUB, Command.VFO_SUB_FREQ);
    }

    /**
     * Sets VFO A/B/Sub-Receiver Frequency
     * @param double the desired frequency in Hz
     * @param vfo the vfo to change the frequency of
     */
    public void setVFOFrequency(double freq, Vfo vfo) {
        // TODO Check to make sure it's within range for each band
        send(vfo2freqCmd.get(vfo), String.format("%011d", (int) Math.min(FREQ_MAX, freq)));
    }

    /**
     * Sets VFO A/B/Sub-Receiver Frequency
     * @param double the desired frequency in Hz
     * @param mainvfo the vfo to change the frequency of, can only be A or B
     */
    public void setVFOFrequency(double freq, MainVfo mainvfo) {
        setVFOFrequency(freq, Vfo.get(mainvfo.code));
    }

    /** Set the desired VFO to transmit **/
    public void setTxVfo(Vfo vfo) {
    	myTxVfo = vfo;
        switch (vfo) {
            case A:
                send(Command.TX_A_B, "0");
                break;
            case B:
                send(Command.TX_A_B, "1");
                break;
            default: // Sub
                // TODO figure out what to do w/ sub
                break;
        }
    }

    /** Set the desired VFO to transmit **/
    public void setTxVfo(MainVfo a) {
        setTxVfo(Vfo.get(a.code));
    }

    /** Set the desired VFO to receive **/
    public void setRxVfo(Vfo vfo) {
    	myRxVfo = vfo;
        switch (vfo) {
            case A:
                send(Command.RX_A_B, "0");
                break;
            case B:
                send(Command.RX_A_B, "1");
                break;
            default: // Sub
                // TODO figure out what to do w/ sub
                break;
        }
    }

    /** Set the desired VFO to receive **/
    public void setRxVfo(MainVfo a) {
        setRxVfo(Vfo.get(a.code));
    }

    /**
     * Sets the TS-2000 into split mode
     * @param txVfo the VFO to transmit on
     */
    public void split(MainVfo txVfo) {
        // Must set the RX VFO first, or else we will RX and TX on the RX VFO
        setRxVfo(txVfo == MainVfo.A ? MainVfo.B : MainVfo.A);
        setTxVfo(txVfo);
    }

    /**
     * Get s-meter reading
     * @return 0000~0030 (Main transceiver)
     * @param reciever main transceiver:0 sub receiver:1
     * main transceiver s-meter:2 sub receiver s-meter: 3*/
    public void sMeter(int reciever) {
        switch (reciever) {
            case 0:
                send(Command.S_METER_STATUS, "0");
                break;
            case 1:
                send(Command.S_METER_STATUS, "1");
                break;
            case 2:
                send(Command.S_METER_STATUS, "2");
                break;
            case 3:
                send(Command.S_METER_STATUS, "3");
                break;
            default:
                send(Command.S_METER_STATUS, "0");
                break;
        }
    }

    public void setSquelch(int receiver, int squelchValue) {
        String squelchValueFormatted = helperToFormat(squelchValue);
        send(Command.SQUELCH_LEVEL, "" + receiver + squelchValueFormatted);
    }

    public void setAFGain(int reciever, int AFGainValue) {
        String AFGainValueFormatted = helperToFormat(AFGainValue);
        send(Command.AF_GAIN, "" + reciever + AFGainValueFormatted);
    }

    public void getBusySignalStatus() {
        send(Command.BUSY_SIGNAL_STATUS);
    }

    /*
     * Values for opMode:
     * 1: LSB
     * 2: USB
     * 3: CW
     * 4: FM
     * 5: AM
     * 6: FSK
     * 7: CR-R
     * 8: Reserved
     * 9: FSK-R
     */
    public void setOperationalMode(int opMode) {
    	if (myRxVfo == null || myTxVfo == null || !myRxVfo.equals(myTxVfo)){
	    	send(Command.RX_A_B, "0");
	    	send(Command.TX_A_B, "1");
	    	send(Command.OPERATIONAL_MODE, "" + opMode);
	    	send(Command.RX_A_B, "1");
	    	send(Command.TX_A_B, "0");
    	}
    	send(Command.OPERATIONAL_MODE, "" + opMode);
    }

    /*
     * Values for powerStatus:
     * 1: On
     * 0: Off
     */
    public void setPowerStatus(int powerStatus) {
        send(Command.PWR_STATUS, "" + powerStatus);
    }

    public void setRFGain(int RFGainValue) {
        String RFGainValueFormatted = helperToFormat(RFGainValue);
        send(Command.RF_GAIN_STATUS, "" + RFGainValueFormatted);
    }

    /**
     * Values for resetValue:
     * 1: VFO reset
     * 2: Master reset
     */
    public void setResetTransceiver(int resetValue) {
        send(Command.RESET_TRANSCEIVER, "" + resetValue);
    }

    /**
     * Values for modeValue:
     * 0: Packet Communication mode
     * 1: PC Control command mode
     */
    public void setInternalTNCMode(int modeValue) {
        send(Command.INTERNAL_TNC_MODE, "" + " " + modeValue);
    }

    /**
     * Gets transceiver information/status
     * * @return int
     * 			    For 1: 11 bits - Frequency (Hz) = 99999999999,
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
     */
    public void transceiverStatus() {
        send(Command.TRANSCEIVER_STATUS);
    }

    public String helperToFormat(int number) {
        if (number < 10) {
            return ("00" + number);
        } else if (number < 100) {
            return ("0" + number);
        }
        return ("" + number);
    }

    /* TODO ADD NEW METHODS HERE! Steps:
     * 1. Pick something you want to be able to control about the TS-2000
     * 2. Identify the commands from Command.java required to do this
     * 3. Send the command to the TS-2000 using HyperTerminal in various forms
     *     and with the TS-2000 in various states, and understand what happens.
     *     It's probably a good idea to take notes (especially on which commands
     *     will tell you what you're interested in about the current state of
     *     the TS-2k.
     * 4. Add a test method to KenwoodTs2kControllerTest, of the form
     *     "test<Method>"
     * 5. Add a method here called <method>, with what you think are the right
     *     parameters, and no return type (communication is asynchronous
     * 6. In the test method, call <method> with a valid set of arguments, and
     *     then use assertNextCmdIs to verify that the desired commands are sent.
     *     Sometimes, you will have to send different sequences of commands in
     *     order to achieve the same result, depending on the TS-2k's current
     *     state
     *
     * Note: Check OtherCommandClass first to see if the method is there. If it
     *  is, copy it over here and build it up from there, but still follow all
     *  the steps
     */
    @Override
    /** Respond to a SerialPortEvent, aka whenever data is available **/
    public void serialEvent(SerialPortEvent ev) {
        if (ev.getEventType() != SerialPortEvent.DATA_AVAILABLE) {
            return; // Don't know what else to do
        }
        String resp = readToSemicolon();
        modelUpdater.update(resp);
    }

    /**
     * Read from the serial port until a semicolon is reached. If an I/O error
     * occurs, or the end of the stream is reached, <code>connected</code> is
     * set to <code>false</code>
     *
     * @return the text read
     */
    private String readToSemicolon() {
        StringBuffer sb = new StringBuffer(32);
        try {
            for (;;) {
                //if(in.read()>=-1 || in.read()<=65535){
                int i = in.read();
                if (i == -1) {
                    disconnect();
                    break;
                }
                char c = (char) i;
                if (c == ';') {
                    break;
                }
                sb.append(c);
                //}
                //else{
                //disconnect();
                //break;
                //}
            }
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
        return sb.toString();
    }

    /**
     * Status Poller continually asks the TS-2000 how it's doing
     * @author nsp25
     */
    private class StatusPoller extends TimerTask {

        @Override
        public void run() {
            if (connected) {
                send(Command.TX_DXER_STATUS);
                send(Command.RX_A_B);
                send(Command.TX_A_B);
                send(Command.VFO_A_FREQ);
                send(Command.VFO_B_FREQ);
                send(Command.VFO_SUB_FREQ);
                send(Command.OPERATIONAL_MODE);
                send(Command.BUSY_SIGNAL_STATUS);
                send(Command.S_METER_STATUS, "0");
                //		send(Command.TRANSCEIVER_STATUS);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader consolein = new BufferedReader(new InputStreamReader(
                System.in));
        KenwoodTs2kController ctrler = new KenwoodTs2kController(
                CommPortIdentifier.getPortIdentifier("COM1"));
        ctrler.connect();
        ctrler.startPolling(3000);

        String in = "";
        while (!"quit".equalsIgnoreCase(in = consolein.readLine())) {
            ctrler.send(in);
        }
    }
}

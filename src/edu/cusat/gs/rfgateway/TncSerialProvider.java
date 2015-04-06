package edu.cusat.gs.rfgateway;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;

import jsattrak.utilities.Bytes;
import edu.cusat.common.utl.gsl.GslConsts;
import edu.cusat.common.utl.gsl.PacketData;
import edu.cusat.gs.rawpacket.RawPacketManager;
import edu.cusat.gs.rfgateway.GatewayMgr.ServerStatus;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class TncSerialProvider extends TncProvider {

    private SerialPort cSerialPort;

    private BufferedOutputStream cOut = null;
    private BufferedInputStream cIn = null;

	/**
	 * The manager of all raw packets received from the TNC, for real-time
	 * display and logging
	 **/
	private RawPacketManager rxMgr;
    
	/**
	 * The manager of all raw packets sent to the TNC, for real-time display and
	 * logging
	 **/
	private RawPacketManager txMgr;
	
	/** Manager for all non-KISS packets **/
	private RawPacketManager nonKissMgr;
	
	/** Timer to keep track of when to resend the TNC's setup string **/
	private Timer setupTimer = new Timer();
	
    public enum Type { THD7A, KAMXL; }
    
    public TncSerialProvider(GatewayMgr mgr, CommPortIdentifier port, Type type) throws 
            UnsupportedCommOperationException, IOException, PortInUseException {
        gm = mgr;
        setupSerial(port, type);
        setupTNC(type);
        setupTimer.cancel();
        if(type.equals(Type.THD7A)) {
        	setupTimer = new Timer();
            setupTimer.scheduleAtFixedRate(new TimerTask() {
                @Override public void run() {
                    try { setupTNC(Type.THD7A); } 
                    catch (IOException e) { e.printStackTrace(); disconnect(); }
                }
            }, 0, 180000); // Re-initialize TH-D7A every three minutes.
        }
        rxMgr = gm.createRawPacketManager("TNC(rx)");
        txMgr = gm.createRawPacketManager("TNC(tx)");
        nonKissMgr = gm.createRawPacketManager("TNC(crap)");
    }
    
    public BufferedOutputStream getCOut(){
    	return cOut;
    }
    
    public void disconnect() {
    	setupTimer.cancel();
        cSerialPort.removeEventListener();
        cSerialPort.close();
        rxMgr.removePacketViewerTabs();
        txMgr.removePacketViewerTabs();
        nonKissMgr.removePacketViewerTabs();
    }
    
    public String get9600Setup(){
    	return KAMXLSETUP9600;
    }
    
    synchronized public void resetTNC(Type type, boolean afsk1200)throws IOException{
    	synchronized(cOut){
    		switch (type) {
        	case THD7A:
        		String RESET = "\rRESTART\r";
        		cOut.write(RESET.getBytes(Bytes.ASCII));
        		cOut.flush();
        		try { Thread.sleep(3000); } 
        		catch (InterruptedException e) { cOut.flush(); }
        		
        		setupTNC(Type.THD7A);
        		
        		break;
        	case KAMXL:
        		try {
        			Thread.sleep(1000);
        			cOut.write(GslConsts.KISS_FEND);
        			cOut.write(-1);
        			cOut.write(GslConsts.KISS_FEND);
        			cOut.flush();
        			Thread.sleep(1000);
        		} catch (InterruptedException e) { cOut.flush(); }
        		
        		try{
        			cOut.write(KICKOUTOFKISS.getBytes(Bytes.ASCII));
        			Thread.sleep(1000);
        			if(afsk1200)
        				cOut.write(KAMXLSETUP1200.getBytes(Bytes.ASCII));
        			else
        				cOut.write(KAMXLSETUP9600.getBytes(Bytes.ASCII));
        			cOut.write(BACKTOKISS.getBytes(Bytes.ASCII));
        		} catch(InterruptedException e){cOut.flush();}
        		
        		cOut.flush();
        		break;
        	}
    	
    	}
    }
    
    synchronized public void resetTNC(Type type)throws IOException{
    	//TODO : synchronize with drop down
    	resetTNC(type, true);
    }

    synchronized public void setupTNC(Type type) throws IOException{
    	//TODO : synchronize with drop down
    	setupTNC(type, true);
    }
    
    synchronized public void setupTNC(Type type, boolean afsk1200) throws IOException {
        synchronized (cOut) {			
        	switch (type) {
        	case THD7A:
        		cOut.write(THD7SETUP1.getBytes(Bytes.ASCII));
        		cOut.flush();
        		try { Thread.sleep(3000); } 
        		catch (InterruptedException e) { cOut.flush(); }
        		cOut.write(THD7SETUP2.getBytes(Bytes.ASCII));
        		cOut.flush();
        		try { Thread.sleep(3000); } 
        		catch (InterruptedException e) { cOut.flush(); }
        		cOut.write("\rBEACON EVERY 1\r".getBytes(Bytes.ASCII));
        		cOut.flush();
        		//added to see if there needs to be time after for no overlap
        		try { Thread.sleep(3000); } 
        		catch (InterruptedException e) { cOut.flush(); }
        		break;
        	case KAMXL:
        		try {
        			Thread.sleep(1000);
        			cOut.write(GslConsts.KISS_FEND);
        			cOut.write(-1);
        			cOut.write(GslConsts.KISS_FEND);
        			cOut.flush();
        			Thread.sleep(1000);
        		} catch (InterruptedException e) { cOut.flush(); }
        		try{
        			cOut.write(KICKOUTOFKISS.getBytes(Bytes.ASCII));
        			Thread.sleep(1000);
        			if(afsk1200)
        				cOut.write(KAMXLSETUP1200.getBytes(Bytes.ASCII));
        			else
        				cOut.write(KAMXLSETUP9600.getBytes(Bytes.ASCII));
        			cOut.write(BACKTOKISS.getBytes(Bytes.ASCII));
        		} catch(InterruptedException e){cOut.flush();}
        		
        		cOut.flush();
        		break;
        	}
		}
    }
    
     /**
     * Setup the serial port
     * @param port
     * @param type TODO
     * @param mgr TODO
     * @throws UnsupportedCommOperationException
     * @throws IOException
     * @throws PortInUseException 
     */
    private void setupSerial(CommPortIdentifier port, Type type) throws 
            UnsupportedCommOperationException, IOException, PortInUseException {
        cSerialPort = (SerialPort) port.open(getClass().getName(), 10000);
        cSerialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        
        if (type.equals(Type.KAMXL)) {
            cSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN
                    | SerialPort.FLOWCONTROL_RTSCTS_OUT);
            cSerialPort.setRTS(true);            
        } else {
            cSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        }

        cOut = new BufferedOutputStream(cSerialPort.getOutputStream());
        cIn = new BufferedInputStream(cSerialPort.getInputStream());
        connected = true;
        try {
            cSerialPort.addEventListener(new TncReader());
        } catch (TooManyListenersException e) {
            cSerialPort.removeEventListener();
            try {
                cSerialPort.addEventListener(new TncReader());
            } catch (TooManyListenersException e1) {
                e1.printStackTrace(); // No way this is happening now, though
            }
        }
        cSerialPort.notifyOnDataAvailable(true);
    }
    
    public RawPacketManager getTxMgr(){
    	return txMgr;
    }
    
    public synchronized boolean send(byte[] arr) {
    	synchronized (cOut) {
    		txMgr.addPacket(arr, true);
        	try {
        		System.out.println(Bytes.toHexString(arr));
        		cOut.write(GslConsts.KISS_FEND);
        		cOut.write(0);
        		for (byte b : arr) {
        			
        			if (b == GslConsts.KISS_FEND) {
        				byte[] x = {GslConsts.KISS_FESC};
        				System.out.println("first if case:" + Bytes.toHexString(x));
        				cOut.write(GslConsts.KISS_FESC);
        				byte[] y = {GslConsts.KISS_TFEND};
        				System.out.println("first if case c0:" + Bytes.toHexString(y));
        				cOut.write(GslConsts.KISS_TFEND);
        			} else if (b == GslConsts.KISS_FESC) {
        				byte[] x = {GslConsts.KISS_FESC};
        				System.out.println("else if case:" + Bytes.toHexString(x));
        				cOut.write(GslConsts.KISS_FESC);
        				byte[] y = {GslConsts.KISS_TFESC};
        				System.out.println("else if case DB:" + Bytes.toHexString(y));
        				cOut.write(GslConsts.KISS_TFESC);
        			} else {
        				byte[] x = {b};
        				System.out.println(Bytes.toHexString(x));
        				cOut.write(b);
        				
        			}
        		}
        		cOut.write(GslConsts.KISS_FEND);
        		cOut.flush();
        	} catch (IOException e) {
        		connected = false;
        		e.printStackTrace();
        		gm.disconnectTNC();
        	}
        	
        	return connected;
		}
    }
    public synchronized boolean receive(byte[] arr) {
    	arr[0] = (byte) 10;
    	rxMgr.addPacket(arr, true);
    	PacketData d = new PacketData(arr);
    	SatTcpProvider test = tcps.get(new Callsign("Violet"));
    	test.sendToInControl(d);
    	// TESTING CODE - Sat Mar 22, 2014
    	// TncReader test = new TncReader;
    	// for(int i = 0; i < arr.length; i++)
    	// 	TncReader.newCharDataState(arr[i]);
    	// END TESTING CO
    	return true;
    }

    private enum RxState { WAIT, DATA, ESCAPE, OTHER; }

    public class TncReader implements SerialPortEventListener {    
    
        /** This class is a state machine, and this is the state variable **/
        private RxState state = RxState.WAIT;
        
        /** An array to hold data as it's being parsed **/
        private byte[] rxBuffer = new byte[1000];
        
        private int rxIndex = 0;
        
        private List<Byte> nonKissBuffer = new ArrayList<Byte>();
        
        @Override
        public void serialEvent(SerialPortEvent ev) {
            if (ev.getEventType() == SerialPortEvent.DATA_AVAILABLE)
                readSerialData();
            else
                gm.notifyListeners("TNC", "Unexpected event type: %d", ev.getEventType());
        }

        private synchronized void readSerialData() {
        	//no matter what
            state = RxState.WAIT;
            int rxInt = 0;
            byte rxChar = 0;
            try {
	            while( -1 != (rxInt = cIn.read())) {
	                rxChar = (byte)rxInt;
	                //TODO remove is causes problems
	                //REMOVE STUFF IN B/T THESE COMMENTS IF THIS MESSES UP
	                if(rxChar == GslConsts.KISS_TFEND || rxChar == GslConsts.KISS_TFESC)
	                	state = RxState.ESCAPE;
	                //REMOVE STUFF IN B/T THESE COMMENTS IF THIS MESSES UP
	                switch (state) {
	                case WAIT:
	                	byte[] x = {rxChar};
	                	System.out.println(Bytes.toHexString(x)+" WAIT ");
	                    newCharWaitState(rxChar);
	                    break;
	                case ESCAPE:
	                	byte[] y = {rxChar};
	                	System.out.println(Bytes.toHexString(y)+" ESCAPE ");
	                	escape(rxChar);
	                	break;
	                case DATA:
	                	byte[] z = {rxChar};
	                	System.out.println(Bytes.toHexString(z)+" DATA ");
	                    newCharDataState(rxChar);
	                    break;
	                default:
	                    gm.notifyListeners("TNC", "Unexpected TNC state: %s", state.toString());
	                }
	            }
            } catch (IOException e) { gm.disconnectTNC(); 
            } catch (Throwable t) {
			    System.out.printf("Error in readSerialData! rxState: %s, rxChar=%x, rxIndex=%d%n", state.toString(), rxChar, rxIndex);
            	Bytes.prettyPrint(rxBuffer);
            	t.printStackTrace(); 
            }
        }
        
        private void toDataState() {
        	if(nonKissBuffer.size() > 0) {
        		Byte[] b1 = nonKissBuffer.toArray(new Byte[nonKissBuffer.size()]);
        		byte[] b2 = new byte[b1.length];
        		for (int i = 0; i < b2.length; i++) b2[i] = b1[i]; 
        		nonKissMgr.addPacket(b2, true);
        		nonKissBuffer.clear();
        	}
            Arrays.fill(rxBuffer, (byte)0);
            rxIndex = 0;
            state = RxState.DATA;
        }

        /**
         * copy everything into the buffer from zero and send it like before but
         * doesn't have to be byte by byte. when we get another 0xc0, create a
         * new packetData, and send it
         */
        public void newCharDataState(byte rxChar) {
        	if (rxChar == GslConsts.KISS_FESC){
        		state = RxState.ESCAPE;
        	} else if (rxChar != GslConsts.KISS_FEND) {
                rxBuffer[rxIndex] = rxChar;
                rxIndex++;
            } else { // got a FEND without a FESC.
            	// If there are multiple c0's in a row
            	if (rxIndex == 0) return;
            	rxMgr.addPacket(Arrays.copyOf(rxBuffer, rxIndex), true);
            	
            	Callsign pktDest = new Callsign(Bytes.arraySlice(rxBuffer, 1, Callsign.length()));
            	Callsign pktSrc = new Callsign(Bytes.arraySlice(rxBuffer, 1+Callsign.length(), Callsign.length()));
            	PacketData pData = new PacketData(Bytes.arraySlice(rxBuffer, 1+2*Callsign.length()+2, rxIndex-2*Callsign.length()));
            	if(gm.simStatus() != ServerStatus.DEAD) {
            		gm.notifyListeners("TNC", "Sending from %s to %s Simulator", pktSrc, pktDest);
            		byte toSendSim[] = new byte[rxIndex - 1];
            		//                        System.arraycopy(pktDest.toArray(false), 0, toSendSim, 0, Callsign.length());
            		//                        System.arraycopy(Callsign.GROUND.toArray(true), 0, toSendSim, Callsign.length(), Callsign.length());
            		//2+2*Callsign.length()
            		System.arraycopy(rxBuffer, 1, toSendSim, 0, toSendSim.length-(2+2*Callsign.length()));
            		gm.sendToSim(pktDest, toSendSim);
            	} else if (tcps.containsKey(pktSrc)) {
            		// tcp is connected so just send there
            		gm.notifyListeners("TNC", "Sending from %s to InControl", pktSrc);
            		tcps.get(pktSrc).sendToInControl(pData);
            	} else {
            		// tcp is not connected so put into list until it is connected
            		gm.notifyListeners("TNC", "Nobody interested in packets from %s, buffering", pktSrc);
            		if (!buffers.containsKey(pktSrc))
            			buffers.put(pktSrc, new ArrayList<PacketData>());
            		buffers.get(pktSrc).add(pData);

            		state = RxState.WAIT; 
            	}
            }
        }

        /**
         * Handle a new character while in the WAIT state. 
         * <p>
         * If it's a C0, we've found the beginning of a packet. Otherwise, it's
         * probably something directly from the TNC
         * @throws IOException 
         */
        private void newCharWaitState(byte rxChar) throws IOException {
            if (rxChar == GslConsts.KISS_FEND) {
            	toDataState();
            } else {
            	nonKissBuffer.add(rxChar);
            	System.out.print(new String(new byte[] { rxChar }, Bytes.ASCII));
            }
        }

        /**
         * Handles escaping KISS metacharacters, if necessary. If rxChar == FESC
         * then it reads another character and returns the character to be be
         * used by the rest of <code>readSerialData()</code>
         * 
         * @param rxChar
         *            the character that may or may not need escaping
         * @return the character after KISS escaping
         * @throws IOException
         *             if an error occurs reading the next character
         */
        private void escape(byte escdChar) throws IOException {
            if(escdChar == GslConsts.KISS_TFEND) {
                System.out.println("Converted DBDC to C0");
                rxBuffer[rxIndex] = GslConsts.KISS_FEND;
                rxIndex++;
	            state = RxState.DATA;
            } if(escdChar == GslConsts.KISS_TFESC) {
                System.out.println("Converted DBDD to DB");
                rxBuffer[rxIndex] = GslConsts.KISS_FESC;
                rxIndex++;
	            state = RxState.DATA;
               // return GslConsts.KISS_FESC;
            }
            //state = RxState.WAIT;
            //gm.notifyListeners("TNC", "Bad escaping in packet, attempting to continue");
            //return escdChar;
        }
    }
}
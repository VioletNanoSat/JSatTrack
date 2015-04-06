package edu.cusat.common.utl;

import gnu.io.CommDriver;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.TooManyListenersException;

public class FakeSerialPort extends SerialPort {

    private static final String FAKE_ID_STRING = "FAKE";

    /** Number of fake serial ports out in the world **/
    private static int numFakes = 0;
    
    /** Which number fake this is **/
    private int fakeNum = numFakes++;
    
	/** Class listening to us for new data **/
    private SerialPortEventListener listener = null;
    
    /* random parameters relevant to serial ports */
    private int baudRate = 0;
    private int dataBits = 0;
    private int flowCtrlMode = 0;
    private int parity = 0;
    private int stopBits = 0;
    private boolean notifyOnDataAvail = false;

    /**
     * Output stream of device being faked. Is hooked up to the istream 
     * returned by getInputStream()
     */
    public NotifyingPipedOutputStream dostream;
    
    /**
     * Input stream to give the user of this serial port
     */
    private PipedInputStream istream;
    
    /**
     * Input stream of device being faked. Is hooked up to the ostream 
     * returned by getOutputStream()
     */
    public PipedInputStream distream;

    /**
     * Input stream to give the user of this serial port
     */
    private PipedOutputStream ostream;
    
    /** 
     *  Set the SerialPort parameters
     *  1.5 stop bits requires 5 databits
     *  @param  b baudrate
     *  @param  d databits
     *  @param  s stopbits
     *  @param  p parity
     *  @throws UnsupportedCommOperationException
     *  @see gnu.io.UnsupportedCommOperationException

     *  If speed is not a predifined speed it is assumed to be
     *  the actual speed desired.
     */
    @Override
    public void setSerialPortParams(int mB, int mD, int mS, int mP)
            throws UnsupportedCommOperationException {
        baudRate = mB;
        dataBits = mD;
        stopBits = mS;
        parity = mP;

    }

	/**
	 * Add this port by name to RXTX's list of CommPortIdentifiers
	 * 
	 * @return the resulting CommPortIdentifier
	 */
    public CommPortIdentifier getFakeId(){
		CommPortIdentifier.addPortName(FAKE_ID_STRING + fakeNum,
				CommPortIdentifier.PORT_SERIAL, new CommDriver() {

					/** Nothing to init **/
					@Override
					public void initialize() {
					}

					/** Give the fake serial port **/
					@Override
					public CommPort getCommPort(String pName, int pType) {
						return FakeSerialPort.this;
					}
				});
    	CommPortIdentifier fakeId = null;
		try {
			fakeId = CommPortIdentifier.getPortIdentifier(FAKE_ID_STRING
					+ fakeNum);
		} catch (NoSuchPortException e) { // We just added it-how could it fail?
    		throw new RuntimeException(e);
    	} 
    	return fakeId;
    }
    
    /**
     * Create a fake serial port hooking everything up right
     * distream <- ostream
     * dostream -> istream
     */
    public FakeSerialPort(){
        ostream = new PipedOutputStream();
        dostream = new NotifyingPipedOutputStream();
        try {
        	distream = new PipedInputStream(ostream);
            istream = new PipedInputStream(dostream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws IOException {
        FakeSerialPort fsp = new FakeSerialPort();
        fsp.dostream.write(new byte[]{1,2,3,4});
        byte[] out = new byte[4];
        fsp.getInputStream().read(out);
        for (byte b : out) {
            System.out.print(b);
        }
        
    }
    
    @Override public int getBaudRate() { return baudRate; }
    
    @Override public int getDataBits() { return dataBits; }

    @Override public int getParity() { return parity ; }

    @Override public int getStopBits() { return stopBits ; }

    @Override
    public void setInputBufferSize(int mSize) {
        try { 
            byte[] data = new byte[istream.available()];
            istream.read(data); 
            istream = new PipedInputStream(dostream, mSize);
            dostream.write(data);
        } catch (IOException e) { }
    }

    @Override public void setOutputBufferSize(int mSize) { }
    
    @Override public int getFlowControlMode() { return flowCtrlMode ; }

    @Override
    public void notifyOnDataAvailable(boolean mEnable) {
        notifyOnDataAvail  = mEnable;
    }

    @Override
    public void addEventListener(SerialPortEventListener mLsnr)
            throws TooManyListenersException {
        if(listener != null) // Only 1 listener allowed
            throw new TooManyListenersException(); 
        listener = mLsnr;
    }
    
    @Override
    public void removeEventListener() { listener = null; }

    @Override
    public void setFlowControlMode(int mFlowcontrol)
            throws UnsupportedCommOperationException {
        // TODO Auto-generated method stub

    }

    @Override public void setRTS(boolean mState) { }

    @Override
    public boolean setUARTType(String mType, boolean mTest)
            throws UnsupportedCommOperationException {
        return false;
    }

    @Override public void disableReceiveFraming() { }

    @Override public void disableReceiveThreshold() { }

    @Override public void disableReceiveTimeout() { }
    
    @Override public void enableReceiveFraming(int mF)
            throws UnsupportedCommOperationException { }

    @Override public void enableReceiveThreshold(int mThresh)
            throws UnsupportedCommOperationException { }

    @Override public void enableReceiveTimeout(int mTime)
            throws UnsupportedCommOperationException { }

    @Override
    public int getInputBufferSize() { return 1024; }

    @Override
    public InputStream getInputStream() throws IOException {
        return istream;
    }

    @Override public int getOutputBufferSize() { return 1024; }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return ostream;
    }

    @Override public int getReceiveFramingByte() { return 0; }

    @Override public int getReceiveThreshold() { return 0; }

    @Override public int getReceiveTimeout() { return 0; }

    @Override public boolean isReceiveFramingEnabled() { return false; }

    @Override public boolean isReceiveThresholdEnabled() { return false; }

    @Override public boolean isReceiveTimeoutEnabled() { return false; }

//***** Methods I'd like to implement
    @Override
    public boolean setEndOfInputChar(byte mB)
            throws UnsupportedCommOperationException {
        return false;
    }

    @Override
    public byte getEndOfInputChar() throws UnsupportedCommOperationException {
        // TODO Figure out what RXTXPort does
        throw new UnsupportedCommOperationException(
                "Don't know what RXTXPort returns");
    }

//***** Flow control methods that don't really need to be implemented
    private boolean dtr = false;
    @Override public void setDTR(boolean mState) { dtr = mState; }
    @Override public boolean isDTR() { return dtr; } 

//***** Is methods I don't know what to do with yet
    @Override public boolean isCD() { return false; } // TODO Figure out
    @Override public boolean isCTS() { return false; } // TODO Figure out
    @Override public boolean isDSR() { return false; } // TODO Figure out
    @Override public boolean isRI() { return false; } // TODO Figure out
    @Override public boolean isRTS() { return false; } // TODO Figure out

//***** Notify methods I don't know what to do with yet
    @Override public void notifyOnBreakInterrupt(boolean mEnable) { }
    @Override public void notifyOnCTS(boolean mEnable) { }
    @Override public void notifyOnCarrierDetect(boolean mEnable) { }
    @Override public void notifyOnDSR(boolean mEnable) { }
    @Override public void notifyOnFramingError(boolean mEnable) { }
    @Override public void notifyOnOutputEmpty(boolean mEnable) { }
    @Override public void notifyOnOverrunError(boolean mEnable) { }
    @Override public void notifyOnParityError(boolean mEnable) { }
    @Override public void notifyOnRingIndicator(boolean mEnable) { }
    
//***** Methods I don't know how to fake yet    
    private static UnsupportedCommOperationException CANT_REPLICATE = 
        new UnsupportedCommOperationException(
                "Not enough documentation to replicate");

    @Override
    public byte getParityErrorChar() throws UnsupportedCommOperationException {
        throw CANT_REPLICATE; // No devices use parity yet
    }
    
    @Override
    public boolean getCallOutHangup() throws UnsupportedCommOperationException {
        throw CANT_REPLICATE;
    }
    @Override
    public int getBaudBase() throws UnsupportedCommOperationException,
            IOException {
        throw CANT_REPLICATE;
    }
    
    @Override
    public boolean setBaudBase(int BaudBase)
            throws UnsupportedCommOperationException, IOException {
        throw CANT_REPLICATE;
    }
    
    @Override
    public boolean setDivisor(int Divisor)
            throws UnsupportedCommOperationException, IOException {
        throw CANT_REPLICATE;
    }
    
    @Override
    public int getDivisor() throws UnsupportedCommOperationException,
            IOException {
        throw CANT_REPLICATE; 
    }
    
    @Override
    public String getUARTType() throws UnsupportedCommOperationException {
        throw CANT_REPLICATE;
    }
    
    /** Take a break from transmitting for mDuratoion msec **/
    @Override public void sendBreak(int mDuration) { 
        if ( mDuration <= 0 ) mDuration = 250;
        try { Thread.sleep(mDuration); } catch (InterruptedException e) { }
    }
    

    @Override
    public boolean setLowLatency() throws UnsupportedCommOperationException {
        return false;
    }

    @Override
    public boolean setParityErrorChar(byte mB)
            throws UnsupportedCommOperationException {
        return false;
    }

// Methods that RXTX hasn't implemented    
    @Override 
    public boolean setCallOutHangup(boolean NoHup)
            throws UnsupportedCommOperationException {
        throw new UnsupportedCommOperationException("Operation not implemented");
    }
    
//***** Methods I don't want to implement
    @Override
    public boolean getLowLatency() throws UnsupportedCommOperationException {
        return false;
    }
    
    /**
     * Special OutputStream to allow SerialPortEventListeners to function properly
     *
     */
    private class NotifyingPipedOutputStream extends PipedOutputStream {
        @Override
        public synchronized void flush() throws IOException {
            super.flush();
            if(notifyOnDataAvail)
                listener.serialEvent(new SerialPortEvent(FakeSerialPort.this,
                        SerialPortEvent.DATA_AVAILABLE, false, false));
        }
    }
}

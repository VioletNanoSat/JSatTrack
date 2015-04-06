package edu.cusat.gs.rfgateway;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cusat.common.utl.gsl.GslConsts;
import edu.cusat.common.utl.gsl.PacketData;
import edu.cusat.gs.rfgateway.TncSerialProvider.Type;

/**
 * The TncProvider provides the means for JSatTrak to communicate with the
 * Kantronics KAM XL using the KISS protocol. For now, it just starts the TNC in
 * KISS mode and waits for incoming KISS packets to send to the correct
 * SatelliteTcpListener/outgoing packets from SatelliteTcpListeners to send to
 * the TNC.
 * 
 * @author Nate Parsons nsp25
 * 
 */
public abstract class TncProvider {

	protected static final String KICKOUTOFKISS = "\r\u0192\u0255\u0192\r";
	protected static final String BACKTOKISS = "\rINTFACE KISS\rRESET\r";
	protected static final String KAMXLSETUP1200 = "\rHB 0/1200\rXMITLVL 40/25";
	protected static final String KAMXLSETUP9600 = "\rHB 0/9600\rXMITLVL 40/63";
	protected final static String THD7SETUP1 = "\rTC 1\rAPO 0\r\rBC 1\r\rDTB 1\r\rPC 1,3\r\rFQ 00437405000,6\r\rSV 0\r\rDL 0\r\rTC 0\r";
	protected final static String THD7SETUP2 = "\rHB 1200\r\rF OFF\r\rXFLOW OFF\r\rPACLEN 0\r\rMAXFRAME 1\r\rPPERSIST OFF\r\rKISS ON\rRESTART\r";

	protected Map<Callsign, SatTcpProvider> tcps = 
	                             new HashMap<Callsign, SatTcpProvider>(2);
	protected Map<Callsign, List<PacketData>> buffers = 
                                 new HashMap<Callsign, List<PacketData>>(2);

    boolean connected;
    protected GatewayMgr gm;
    protected SimServer simServer;
    
    
    public abstract void disconnect();
    public abstract void resetTNC(Type type)throws IOException;
    
    /**
     * Signals to this listener that there is a tcp listener ready to send
     * things for call
     * 
     * @param tcp
     *            the tcplistener that is ready
     */
    public synchronized void registerTlmServer(SatTcpProvider tcp) {
        tcps.put(tcp.getCall(), tcp);
        // Now send everything that's built up in the buffer.
        // From here on out it will be sent as soon as it's received
        if (buffers.containsKey(tcp.getCall()))
            for (PacketData packet : buffers.get(tcp.getCall())) {
                tcps.get(tcp.getCall()).sendToInControl(packet);
            }
    }

    /**
     * The TCP listener died, so we must buffer things
     * 
     * @param call
     */
    public synchronized void unRegisterTlmServer(Callsign call) {
        tcps.remove(call);
    }

    public synchronized boolean send(Callsign call, PacketData d) {
        return send(Callsign.GROUND, call, d);
    }
    
    public synchronized boolean send(Callsign src, Callsign dest, PacketData d){
    	gm.notifyListeners(dest, "Sending Packet");
        byte[] toSend = new byte[2*Callsign.length() + GslConsts.HEADER_GARBAGE().length + d.PACKET_DATA_LENGTH];
        System.arraycopy(dest.toArray(false), 0, toSend, 0, Callsign.length());
        System.arraycopy(src.toArray(true), 0, toSend, Callsign.length(), Callsign.length());
        System.arraycopy(GslConsts.HEADER_GARBAGE(), 0, toSend, 2*Callsign.length(), GslConsts.HEADER_GARBAGE().length);
        System.arraycopy(d.getPacketData(), 0, toSend, 2*Callsign.length()+GslConsts.HEADER_GARBAGE().length, d.PACKET_DATA_LENGTH);
        return send(toSend);
    }
    
    public String get9600Setup(){
    	return KAMXLSETUP9600;
    }

    public abstract boolean send(byte[] arr);
    public abstract boolean receive(byte[] arr);

    /** Checks current radio connection state **/
    public boolean isConnected() { return connected; }
}

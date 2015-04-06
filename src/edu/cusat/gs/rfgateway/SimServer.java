package edu.cusat.gs.rfgateway;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import jsattrak.utilities.Bytes;

import edu.cusat.common.utl.gsl.GslConsts;
import edu.cusat.common.utl.gsl.PacketData;
import edu.cusat.gs.rawpacket.RawPacketManager;
import edu.cusat.gs.rfgateway.GatewayMgr.ServerStatus;
import edu.cusat.gs.rfgateway.TncSerialProvider.Type;
import gnu.io.NoSuchPortException;
import gnu.io.UnsupportedCommOperationException;

public class SimServer extends TncProvider {

	private final Callsign simcall;
	
	private ServerWorker cmdSrv;
	private ServerWorker tlmSrv;
	private boolean shutdown = false;
	
	public SimServer(GatewayMgr mgr, Callsign call, int cmdPort, int tlmPort) throws NoSuchPortException,
	UnsupportedCommOperationException, IOException {
		gm = mgr;
		simcall = call;
        cmdSrv = new ServerWorker(new ServerSocket(cmdPort, 1), gm.createRawPacketManager(call.toString()+"(cmd)"));
        tlmSrv = new ServerWorker(new ServerSocket(tlmPort, 1), gm.createRawPacketManager(call.toString()+"(tlm)"));
	}
	
	// TODO something better
	public ServerStatus status() { return cmdSrv.status; }
	
	@Override
	public boolean send(byte[] arr) { return cmdSrv.send(arr); }
	
	@Override
	public void disconnect() {
		shutdown = true; // To jump out of the main loop
		// TODO Stop accepting/existing connections
	}
	
	/** Start both servers **/ // TODO 2 methods in case 1 server dies?
	public SimServer start() {
		// Do we need a reference to the threads for anything? So far, no
        new Thread(cmdSrv, simcall+"(cmd)").start(); new Thread(tlmSrv, simcall+"(tlm)").start();
        return this;
	}
	 
    /**
     * Inner class to do all the work associated with serving the simulator.
     */
	private class ServerWorker implements Runnable {
		
		/** The server that accepts connections **/
		private ServerSocket server;
		
		/** The stream to write to InControl with **/
		private BufferedOutputStream outstream = null;
		
		/** Our current status **/
	    private ServerStatus status = ServerStatus.DEAD;

		/** The manager of all raw packets, for real-time display and logging **/
		private RawPacketManager rpm;
		
		/**
		 * Create a server that will sit in the DEAD state until
		 * {@link SatTcpProvider#start()}-ed
		 * 
		 * @param servSkt
		 *            the bound ServerSocket to use
		 */
		private ServerWorker(ServerSocket servSkt, RawPacketManager rpm) { 
			server = servSkt; 
			this.rpm = rpm;
		}
			/** Disconnects the server from InControl, and notifies the TNC **/
			// TODO make less rude to InControl
			public void disconnect(){
		    	setStatus(ServerStatus.DEAD);
		        try { if( !server.isClosed() ) server.close(); } 
		        catch( IOException ex ) {
		        	gm.notifyListeners(simcall, "IO exception while disconnecting");
			    }
			} 
	
		    /**
		     * Write to the simulator
		     */
			public boolean send(byte[] arr) {
				rpm.addPacket(arr, true);
				if(status == ServerStatus.RUNNING)
					try {
						outstream.write(GslConsts.KISS_FEND);
						outstream.write(0);
						outstream.write(arr);
						outstream.write(GslConsts.KISS_FEND);
						outstream.flush();
						gm.notifyListeners(simcall, "Sent %d bytes to Sim", 3+arr.length);
					} catch (IOException e) {
						connected = false;
			            e.printStackTrace();
						gm.disconnectTNC();
					}
				return status == ServerStatus.RUNNING;
			}
		
			private void setStatus(ServerStatus newStatus) {
				gm.notifyListeners(simcall, status + "->" + (status = newStatus));
			}
			
			@Override
			public void run() {
				InputStream instream = null;
				int cmd; // First byte is read into here
				byte[] packet = new byte[318]; // The rest into here
				// similar to TncProvider run except doesn't have to go byte by byte
				while (!shutdown) { try {
					// Whether we got here because of a fresh start or from a 
					// disconnect or error, make sure we start out in the closed
					// state
					if( instream != null ) instream.close();
					if( outstream != null ) outstream.close();
					if( server.isClosed() ) server = new ServerSocket(server.getLocalPort(),1);
					// Wait for a connection
					setStatus(ServerStatus.WAITING);
					Socket s = server.accept(); 
					setStatus(ServerStatus.RUNNING);
					connected = true;
					gm.notifyListeners(simcall, "Connected on port %d", server.getLocalPort());
					instream = new BufferedInputStream(s.getInputStream());
					outstream = new BufferedOutputStream(s.getOutputStream());
	
					while ( ServerStatus.RUNNING == status ) {
						if ((cmd = instream.read()) != -1) {
							serve(instream, (byte)cmd, packet);
						}
					}
				} catch (IOException e) { 
					if(!shutdown) // Only notify on unplanned disconnects
						gm.notifyListeners(simcall, e.getMessage()); 
				} } // while(!shutdown)
			disconnect();
		} // run
	
		private void serve(InputStream instream, byte cmd, byte[] packet) throws IOException{
			if(GslConsts.KISS_FEND != cmd) return;
			int bytesRead = instream.read(packet);
			gm.notifyListeners(simcall, "Read %d bytes", bytesRead+1);
			rpm.addPacket(Arrays.copyOf(packet, bytesRead), true);
			// First byte is 0
			// Next 7 are GROUND\x225
			// Next 7 are the satellite callsign\x3e (?)
			Callsign source = Callsign.fromSimPacket(Bytes.arraySlice(packet, 1+Callsign.length(), Callsign.length()));
			gm.notifyListeners(Callsign.GROUND, "Sending message to %s", source.toString());
			
			Callsign destination = Callsign.fromSimPacket(Bytes.arraySlice(packet, 1, Callsign.length()));
			if(Callsign.GROUND.equals(destination))
				gm.notifyListeners(Callsign.GROUND, "Received a message for us");
			else
				gm.notifyListeners(Callsign.GROUND, "Received a message for %s", destination);
			// 3 more are garbage
			PacketData pData = new PacketData(Bytes.arraySlice(packet, 1+2*Callsign.length()+2, packet.length));
            // tcp is connected so just send to the tcp
            if (tcps.containsKey(source)) {
                SatTcpProvider toSendData = tcps.get(source);
                toSendData.sendToInControl(pData);
            }
            else if( gm.tncStatus() == ServerStatus.RUNNING){
            	gm.sendToTnc(source, pData);
            }
            // tcp is not connected so put into list until it is connected
            else {
				if (!buffers.containsKey(source))
					buffers.put(source, new ArrayList<PacketData>());
				buffers.get(source).add(pData);
            }
		}
	} // ServerWorker

	@Override
	public void resetTNC(Type type) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean receive(byte[] arr) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public Callsign getCall() {
		return simcall;
	}
}
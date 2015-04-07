package edu.cusat.gs.rfgateway;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import edu.cusat.common.GTG;
import edu.cusat.common.utl.gsl.PacketData;
import edu.cusat.gs.gui.RfGatewayPanel;
import edu.cusat.gs.rawpacket.RawPacketManager;
import edu.cusat.gs.rfgateway.GatewayMgr.ServerStatus;

/**
 * This class is part of the communication chain between InControl and the
 * satellite. It provides JSatTrak's TCP interface with InControl. Commands from
 * InControl are accepted and forwarded to the TNC (through the TncProvider)
 * as-is, and telemetry from the satellite is accepted from the TncProvider and
 * forwarded to InControl as-is.
 * <p>
 * The tasks related to dealing with the TNC are handled in this class, which
 * has a reference to 2 of its inner classes. One ServerWorker is for
 * InControl's Telemetry gateway, and another is for InControl's Command
 * gateway.
 */
public class SatTcpProvider {

	/** The class that manages everything **/
	private GatewayMgr gm;

	/** The callsign we serve **/
	private Callsign call;

	/** The TNC to interact with **/
	private TncProvider tncp;

	/** The server for InControl's Command gateway to connect to **/
	private ServerWorker cmdSrv;
	private ServerWorker cmdSrvNew; // new one, to kill old one

	/** The server for InControl's Telemetry gateway to connect to **/
	private ServerWorker tlmSrv;
	private ServerWorker tlmSrvNew; // new one, to kill old one
	
	/** The server that accepts connections **/
	private ServerSocket tlmSrvSocket;
	private ServerSocket cmdSrvSocket;
	
	private RawPacketManager tlmSrvRPM;
	private RawPacketManager cmdSrvRPM;
	
	/** This thread's cmdPort, for reference **/
	private int cmdPort = 0;
	
	/** This thread's tlmPort, for reference **/
	private int tlmPort = 0;
	
	/** Flag to indicate we should shut down **/
	private boolean shutdown;
	
	private static int TCP_TIMEOUT = 2000; // 2000 ms = 2 seconds

	/**
	 * Create a SatTcpProvider to start listening for connections from IC
	 * 
	 * @param gwyMgr
	 *            TODO
	 * @param call
	 *            The callsign to use
	 * @param arg_cmdPort
	 *            The port to listen for IC's cmd gateway on
	 * @param arg_tlmPort
	 *            The port to listen for IC's tlm gateway on
	 * @throws IOException
	 *             if a server can't be opened
	 */
	public SatTcpProvider(GatewayMgr gwyMgr, Callsign call, int arg_cmdPort,
			int arg_tlmPort) throws IOException {
		gm = gwyMgr;
		this.call = call;
		cmdPort = arg_cmdPort;
		tlmPort = arg_tlmPort;
		// Create the servers
		tlmSrvSocket = new ServerSocket(tlmPort, 1);
		cmdSrvSocket = new ServerSocket(cmdPort, 1);
		tlmSrvSocket.setSoTimeout(TCP_TIMEOUT);
		cmdSrvSocket.setSoTimeout(TCP_TIMEOUT);
		tlmSrvRPM = gm.createRawPacketManager(call + "(tlm)");
		cmdSrvRPM = gm.createRawPacketManager(call + "(cmd)");
		tlmSrv = new ServerWorker(tlmSrvSocket, tlmSrvRPM);
		cmdSrv = new ServerWorker(cmdSrvSocket, cmdSrvRPM);

	}

	/** Shut down the servers, disconnecting from everything **/
	public void shutdown() {
		shutdown = true; // To jump out of the main loop
		// Stop accepting/existing connections
		cmdSrv.disconnect();
		tlmSrv.disconnect();
		tlmSrvRPM.removePacketViewerTabs();
		cmdSrvRPM.removePacketViewerTabs();
		try {
			tlmSrvSocket.close();
		} catch (IOException e) {
			gm.notifyListeners(call, "Unable to close TlmSrvSocket");
		}
		try {
			cmdSrvSocket.close();
		} catch (IOException e) {
			gm.notifyListeners(call, "Unable to close CmdSrvSocket");
		}
	}

	/**
	 * Used by the GatewayMgr to notify this when the TNC is connected
	 * 
	 * @param tp
	 *            The object actually connected to the TNC
	 */
	protected void notifyOfTncConnection(TncProvider tp) {
		tncp = tp;
		if (tp != null && ServerStatus.RUNNING == tlmStatus())
			tncp.registerTlmServer(this);
	}

	/**
	 * Used by the GatewayMgr to notify this when the TNC is disconnected
	 */
	protected void notifyOfTncDisconnection() {
		tncp = null;
	}

	/**
	 * Used by the TNC to send data to InControl
	 * 
	 * @param packet
	 *            the data to send
	 * @return whether the server is still connected afterwards
	 */
	protected boolean sendToInControl(PacketData packet) {
		byte[] toSend = new byte[packet.PACKET_DATA_LENGTH + 1];
		toSend[0] = GTG.RADIO_PACKET;
		System.arraycopy(packet.getPacketData(), 0, toSend, 1,
				packet.PACKET_DATA_LENGTH);
		return tlmSrv.sendToInControl(toSend);
	}

	/** Start both servers **/
	// TODO 2 methods in case 1 server dies?
	public void start() {
		// Do we need a reference to the threads for anything? So far, no
		new Thread(cmdSrv, call + "(cmd)").start();
		new Thread(tlmSrv, call + "(tlm)").start();
	}

	/**
	 * Inner class to do all the work associated with serving InControl.
	 * 
	 */
	private class ServerWorker implements Runnable {

		/** The stream to write to InControl with **/
		private BufferedOutputStream outstream = null;

		/** Our current status **/
		private ServerStatus status = ServerStatus.DEAD;

		/**
		 * The socket used to communicate with InControl, saved so we can
		 * disconnect while waiting for a read.
		 */
		private Socket sock;

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
			this.rpm = rpm;
		}

		/** Disconnects the server from InControl, and notifies the TNC **/
		// TODO make less rude to InControl
		public void disconnect() {
			setStatus(ServerStatus.DEAD); // thread will auto-disconnect now
			if (tncp != null && this == tlmSrv){
				tncp.unRegisterTlmServer(getCall());
			}
		}

		/**
		 * Helper function to send 2 byte arrays
		 * 
		 * @param cmd
		 *            the first byte
		 * @param data
		 *            the second byte
		 * @return whether or not the server is still connected afterwards
		 **/
		private boolean sendToInControl(byte cmd, byte data) {
			return sendToInControl(new byte[] { cmd, data });
		}

		/**
		 * Send arbitrary byte arrays to InControl
		 * 
		 * @param data
		 *            the data to send
		 * @return whether or not the server is still connected afterwards
		 */
		private boolean sendToInControl(byte[] data) {
			rpm.addPacket(data, true);
			if (ServerStatus.RUNNING == status) {
				try {
					outstream.write(data);
					outstream.flush();
					gm.notifyListeners(call, "Sent %d bytes to IC", data.length);
				} catch (IOException e) {
					gm.notifyListeners(call, "IOException!");
					disconnect();
				}
			} else { // TODO buffer!
				gm.notifyListeners(call, "Not connected to IC!");
			}
			return ServerStatus.RUNNING == status;
		}

		private void setStatus(ServerStatus newStatus) {
			/* If you comment the following out, you must add a new function in 
			 * RfGateway panel to do 
			 * refreshList(waitingList, ServerStatus.WAITING);
    	     * refreshList(runningList, ServerStatus.RUNNING);
    	     * otherwise you won't see your server in the waiting/running list!!!!!!111
    	     */
			gm.notifyListeners(call, status + "->" + (status = newStatus));
		}

		@Override
		public void run() {
			BufferedInputStream instream = null;
			int cmd; // First byte is read into here
			byte[] buffer = new byte[1000]; // The rest into here
			byte[] packet;
			// similar to TncProvider run except doesn't have to go byte by byte
			setStatus(ServerStatus.WAITING);
			while (!shutdown && !status.equals(ServerStatus.DEAD)) {
				try {
					if (this == tlmSrv || this == tlmSrvNew){
						sock = tlmSrvSocket.accept();
					}else if (this == cmdSrv || this == cmdSrvNew){
						sock = cmdSrvSocket.accept();
					}
					
					if (sock.isConnected())
					{
						setStatus(ServerStatus.RUNNING);
						
						/* Connected. Destroy old servers if exist, 
						 * and create a new thread to listen in case we get a new connect request.
						 */
						
						// If we are new, remove old
						if (this == cmdSrvNew && this != cmdSrv)
						{
							if (!cmdStatus().equals(ServerStatus.DEAD)){
								//gm.notifyListeners(callID, "killingA");
								cmdSrv.disconnect();
							}
							cmdSrv = this;
						}else if (this == tlmSrvNew && this != tlmSrv){
							if (!tlmStatus().equals(ServerStatus.DEAD)){
								//gm.notifyListeners(callID, "killingB");
								tlmSrv.disconnect();
							}
							tlmSrv = this;
						}
						
						// Now create "new" waiting sockets
						if (this == tlmSrv){
							tlmSrvNew = new ServerWorker(tlmSrvSocket, tlmSrvRPM);
							new Thread(tlmSrvNew, call + "(tlm)").start();
						}else if (this == cmdSrv){
							cmdSrvNew = new ServerWorker(cmdSrvSocket, cmdSrvRPM);
							new Thread(cmdSrvNew, call + "(cmd)").start();
						}
						
						
						if (this == tlmSrv){
							gm.notifyListeners(call, "Connected on port %d",
								tlmSrvSocket.getLocalPort());}
						else if (this == cmdSrv){
							gm.notifyListeners(call, "Connected on port %d",
								cmdSrvSocket.getLocalPort());}
						instream = new BufferedInputStream(sock.getInputStream());
						outstream = new BufferedOutputStream(sock.getOutputStream());
						if (tncp != null)
							tncp.registerTlmServer(SatTcpProvider.this);
	
						while ((ServerStatus.RUNNING == status) && (sock != null)) {
							
							if ((cmd = instream.read()) != -1) {
								switch (cmd) {
								case GTG.RADIO_PACKET:
									int bytesRead = instream.read(buffer);
									packet = new byte[bytesRead];
									packet = Arrays.copyOf(buffer, bytesRead);
									rpm.addPacket(packet, true);
									// TODO SEND TO RAWPACKETMANAGER HERE
									if (tncp != null) {
										tncp.send(call, new PacketData(packet));
										gm.notifyListeners(call,
												"Sent %d bytes to TNC", bytesRead);
										break;
									} else { // TODO Buffer!
										gm.notifyListeners(
												call,
												"Got %d bytes to send, but no TNC. Discarding.",
												bytesRead);
										break;
									}
								case GTG.CONNECTION_STATUS:
									sendToInControl(
											GTG.CONNECTION_STATUS,
											tncp.isConnected() ? GTG.RADIO_CONNECTED
													: GTG.RADIO_DISCONNECTED);
									break;
								case GTG.GTG_DISCONNECT:
									gm.notifyListeners(call,
											"Received disconnect request");
									disconnect(); // we are disconnected, so push new ones out
									/* Since cmdSrv is the only one that gets a GTG_DISCONNECT
									 * be kind to tlmSrv and disconnect it too.
									 */
									if (this == cmdSrv)
									{
										cmdSrv = cmdSrvNew;
										if (!tlmSrv.status.equals(ServerStatus.DEAD))
											tlmSrv.disconnect();
										tlmSrv = tlmSrvNew;
									}
									break;
								}
							}
						}
					}
				}catch (SocketTimeoutException f){
					// Do nothing!
				}catch (IOException e) {
					if (!shutdown) // Only notify on unplanned disconnects
						gm.notifyListeners(call, e.getMessage());
				}
			} // while loop to get socket connected
			
			if (sock != null && sock.isConnected()){
				try {
					sock.close();
				} catch (IOException e) {
					if (!shutdown) // Only notify on unplanned disconnects
						gm.notifyListeners(call, e.getMessage());
				}
			}
			// Delete everything that our thread has.
			packet = null;
			outstream = null;
			sock = null;
			return;
		} // run
	} // ServerWorker

	// ///////////////////////////////// GETTERS
	public ServerStatus tlmStatus() {
		return tlmSrv.status;
	}

	public ServerStatus cmdStatus() {
		return cmdSrv.status;
	}

	public Callsign getCall() {
		return call;
	}
}
package edu.cusat.gs.rfgateway;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.GroundStation;
import jsattrak.objects.SatelliteTleSGP4;
import jsattrak.utilities.TLE;
import name.gano.astro.time.Time;

import org.joda.time.format.ISODateTimeFormat;

import com.thoughtworks.xstream.XStream;

import edu.cusat.common.GTG;
import edu.cusat.common.GroundPassRequest;
import edu.cusat.common.GroundPassResponse;
import edu.cusat.common.utl.gsl.PacketData;
import edu.cusat.gs.rotator.GroundPass;

/**
 * This class is what allows InControl to control JSatTrak remotely over the
 * internet.
 * 
 * @author Nate Parsons nsp25
 * 
 */
public final class CtrlTcpProvider implements Runnable {
	Socket socket;
	BufferedInputStream instream;
	BufferedOutputStream outstream;
	boolean connected;
	int port;
	ServerSocket server;
	private GroundStation gs;

	public static final String TEST_PASSES = "#,Rise Time,Set Time,1,01 Jan " +
			"2006 01:24:18.648 UTC,01 Jan 2006 01:31:36.225 UTC,2,01 Jan 2006" +
			" 03:04:35.910 UTC,01 Jan 2006 03:15:29.936 UTC,3,01 Jan 2006 " +
			"04:46:32.232 UTC,01 Jan 2006 04:58:02.666 UTC,4,01 Jan 2006 " +
			"06:29:23.128 UTC,01 Jan 2006 06:39:37.333 UTC,5,01 Jan 2006 " +
			"08:14:26.104 UTC,01 Jan 2006 08:18:37.819 UTC,6,02 Jan 2006 " +
			"01:39:55.279 UTC,02 Jan 2006 01:49:27.743 UTC,7,02 Jan 2006 " +
			"03:21:09.427 UTC,02 Jan 2006 03:32:34.688 UTC,8,02 Jan 2006 " +
			"05:03:30.391 UTC,02 Jan 2006 05:14:44.310 UTC";

	/**
	 * Create a CtrlTcpProvider
	 * 
	 * @param p
	 *            Port to listen on
	 * @throws IOException
	 *             if the port can't be opened
	 */
	public CtrlTcpProvider(GroundStation gs, int p) throws IOException {
		this.gs = gs;
		port = p;
		server = new ServerSocket(port);
		new Thread(this).start();
	}

	/**
	 * Open a ServerSocket and wait for a connection. When a connection is made,
	 * wait for commands from InControl.
	 * 
	 * @Override
	 */
	public void run() {
		while (true) {
			try {
				if (instream != null)
					instream.close();
				if (outstream != null)
					outstream.close();

				socket = server.accept();
				System.out.println("CtrlTcpProvider connected on port " + port);
				instream = new BufferedInputStream(socket.getInputStream());
				outstream = new BufferedOutputStream(socket.getOutputStream());
				byte tmp = (byte) instream.read();
				/** By convention, InControl must say hello before we proceed **/
				if (tmp != GTG.HELLO_BYTE) {
					// TODO open dialog or something
					System.out
							.println("Connection attempt made from someone that isn't InControl!");
				} else {
					/** Now we say hello back **/
					System.out.println("Saying hi back");
					connected = true;
					send(GTG.HELLO_BYTE);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}

			// Now that we're connected, just listen
			while (connected) {
				try {
					// The first thing we get is a one-byte command telling us
					// what to do
					int cmd;
					if ((cmd = instream.read()) != -1) {
						switch (cmd) {
						case GTG.CTRL_CALC_GROUND_PASSES:

							// Calculate ground passes
							XStream x = new XStream();
							x.alias("GroundPassRequest", GroundPassRequest.class);
							x.alias("GroundPassResponse", GroundPassResponse.class);
							GroundPassRequest gpreq = (GroundPassRequest) x.fromXML(instream);
							if(gpreq.fake){
								x.toXML(new GroundPassResponse(TEST_PASSES), outstream);
							} else {
								// Read in specifics of request, call GroundPass.find(), return result
								try {
								AbstractSatellite sat = null;
								if(gpreq.tle != null && gpreq.tle != ""){
									String[] tleparts = gpreq.tle.split("\n"); 
										sat = new SatelliteTleSGP4(new TLE(gpreq.satName, tleparts[0], tleparts[1]));
								} else {
									// Find the satellite somewhere
								}
								Time t = new Time(ISODateTimeFormat.dateTimeParser().parseDateTime(gpreq.start));
								GroundPassResponse resp = formGpResp(GroundPass.find(gs, sat, 
										t, 
										gpreq.duration, gpreq.timestep));
								x.toXML(resp,outstream);
								} catch (Exception e) {
									e.printStackTrace();
									x.toXML(new GroundPassResponse("Invalid TLE"));
								}
							}
							
							break;
						case GTG.GTG_DISCONNECT:
							System.out.println("Received disconnect");
							disconnect();
							break;
						}
					}
				} catch (IOException e) {
					connected = false;
				}
			}
		}
	}

	private GroundPassResponse formGpResp(List<GroundPass> gps) {
		StringBuffer buf = new StringBuffer("#,Rise Time,Set Time,Duration [Min]\n");
		for(int i=0; i < gps.size(); i++) {
			buf.append(String.format("%d,%s,%s,%f\n", i+1, 
					Time.dateTimeOfJd(gps.get(i).riseTime()), 
					Time.dateTimeOfJd(gps.get(i).setTime()),
					24*60*(gps.get(i).setTime() - gps.get(i).riseTime())));
		}
		return new GroundPassResponse(buf.toString());
	}

	public void disconnect() {
		connected = false;
		try {
			if (!server.isClosed())
				server.close();
		} catch (IOException ex) {
			System.out.println("TCP Exception");
		}
	}

	/**
	 * Send a single byte
	 * 
	 * @param cmd
	 * @return
	 */
	public synchronized boolean send(byte cmd) {
		return send(new byte[] { cmd });
	}

	public synchronized boolean send(byte cmd, byte data) {
		return send(new byte[] { cmd, data });
	}

	public synchronized boolean send(PacketData packet) {
		byte[] toSend = new byte[packet.PACKET_DATA_LENGTH + 1];
		toSend[0] = GTG.RADIO_PACKET;
		System.arraycopy(packet.getPacketData(), 0, toSend, 1,
				packet.PACKET_DATA_LENGTH);
		send(toSend);
		return connected;
	}

	public synchronized boolean send(byte[] data) {
		if (connected) {
			try {
				if (outstream == null)
					outstream = new BufferedOutputStream(socket
							.getOutputStream());
				outstream.write(data);
				outstream.flush();
			} catch (IOException e) {
				connected = false;
			}
		}
		return connected;
	}

	public int getPort() {
		return port;
	}
	
	public void setCurrentGS( GroundStation gs ){
		this.gs = gs;
	}
}
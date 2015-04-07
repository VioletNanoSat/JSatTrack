package edu.cusat.gs.rfgateway;

import edu.cusat.common.utl.gsl.Ax25Packet;
import edu.cusat.common.utl.gsl.PacketData;
import edu.cusat.gs.gui.RfGatewayPanel;

/**
 * TODO - implement Morse Code sender
 */
public class OperatorCallsignSender {
	private GatewayMgr gm;
	private String operatorCallSign;
	private Byte operatorCallSignID;
	private boolean senderEnabled;
	private boolean morseCodeOptionEnabled;
	private boolean ax25OptionEnabled;
	private Thread autoSender;

	public OperatorCallsignSender(RfGatewayPanel parent, GatewayMgr g)
	{
		autoSender = null;
		senderEnabled = false;
		morseCodeOptionEnabled = false;
		ax25OptionEnabled = false;
		gm = g;
	}
	public void setGatewayMgr(GatewayMgr g)
	{
		gm = g;
	}
	public void setOperatorCallSign(String operatorCallSign,Byte operatorCallSignID)
	{
		this.operatorCallSign = operatorCallSign;
		this.operatorCallSignID = operatorCallSignID;
	}
	public void setEnabled(boolean option)
	{
		senderEnabled = option;
	}
	public void enableAx25Option()
	{
		ax25OptionEnabled = true;
	}
	public void enableMorseCodeOption()
	{
		morseCodeOptionEnabled = true;
	}
	public void notifySender()
	{
		if(autoSender == null && senderEnabled)
		{
			autoSender = new AutoSender();
			autoSender.start();
		}
	}
	private void sendAx25()
	{
		//Ax25Packet operatorCallSignPacket = new Ax25Packet();
		//operatorCallSignPacket.setSource(operatorCallSign, operatorCallSignID);
		//operatorCallSignPacket.setDestination("INFO",(byte)0);
		//operatorCallSignPacket.setControl((byte)0);
		String packet = "Station Operator Callsign:/" + operatorCallSign + "/SSID:" + operatorCallSignID.toString();
		//TODO: is this necessary? Seems like arguments are overly complex
		if(operatorCallSign != null && !operatorCallSign.equals("") && operatorCallSign != null){
		gm.send(new Callsign("INFO", operatorCallSignID),
						//new PacketData(operatorCallSignPacket.toArray()));
						new PacketData(packet.getBytes()));
		}
	}
	
	
	public class AutoSender extends Thread
	{
		@Override
		public void run()
		{
			if(ax25OptionEnabled)
				sendAx25();
			else if(morseCodeOptionEnabled){
				
			}
		}
	}
}
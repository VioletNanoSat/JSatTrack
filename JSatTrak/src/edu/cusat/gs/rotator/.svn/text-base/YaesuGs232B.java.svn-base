package edu.cusat.gs.rotator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jsattrak.utilities.Bytes;

import edu.cusat.common.Failure;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class YaesuGs232B extends Rotator {

	// private static final String CW_ROTATION = "R\r";
	// private static final String CCW_ROTATION = "L\r";
	// private static final String AZ_STOP = "A\r";
	// private static final String MEASURE_AZ = "C\r";
	private static final String SET_AZ = "M%03d\r";
	// private static final String ALL_STOP = "S\r";
	// private static final String AZ_OFFSET_CAL = "O\r";
	// private static final String AZ_FULL_CAL = "F\r";
	// private static final String AZ_SPEED_LOW = "X1\r";
	// private static final String AZ_SPEED_MID1 = "X2\r";
	// private static final String AZ_SPEED_MID2 = "X3\r";
	// private static final String AZ_SPEED_HIGH = "X4\r";
	// private static final String UP_ROTATION = "U\r";
	// private static final String DOWN_ROTATION = "D\r";
	// private static final String EL_STOP = "E\r";
	private static final String MEASURE_AZEL = "C2\r";
	private static final String SET_AZEL = "W%03d %03d\r";
	// private static final String EL_OFFSET_CAL = "O2\r";
	// private static final String EL_FULL_CAL = "F2\r";
	// private static final String MEASURE_EL = "B\r";

	private BufferedReader in;
	private OutputStreamWriter out;
	private Timer statusPoller;
	private StateHolder stateHolder = new StateHolder();

	public YaesuGs232B() {
		super(450F, 180F);
	}

	@Override
	public double az(double az) throws UnsupportedOperationException, Failure {
		try { // Set the az
			sendCommand(String.format(SET_AZ, (int) az));
			// Ask for what it is now
			return az();
		} catch (IOException e) {
			System.out.println(e.getStackTrace());
			disconnect();
			throw new Failure(e);
		}
	}

	@Override
	public double az() throws Failure {
		stateHolder.checkFails();
		return state.az.get();
	}

	@Override
	public double[] azEl(double az, double el) throws Failure {
		try { // set az and el
			sendCommand(String.format(SET_AZEL, (int) az, (int) el));
			return azEl();
		} catch (IOException e) {
			System.out.println(e.getStackTrace());
			disconnect();
			throw new Failure(e);
		}
	}

	@Override
	public double[] azEl() throws Failure {
		stateHolder.checkFails();
		return new double[]{state.az.get(), state.el.get()};
	}

	@Override
	public double el(double el) throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"Cannot set elevation independantly");
	}

	@Override
	public double el() throws Failure { 
		stateHolder.checkFails(); 
		return state.az.get();
	}

	private void sendCommand(String command) throws IOException {
		if(!state.connected.get()) return;
		// Commands must be terminated with '\r' to be interpreted
		if (!command.endsWith("\r"))
			command += "\r";
		// Send the command
		out.write(command);
		out.flush();
	}

	@Override
	public void connect(int baudRate) throws Failure {
		super.connect(baudRate);
		try {
			in = new BufferedReader(new InputStreamReader(
					port.getInputStream(), Bytes.ASCII));
			out = new OutputStreamWriter(port.getOutputStream(), Bytes.ASCII);
			if (statusPoller == null)
				statusPoller = new Timer("GS232B-StatusPoller", true);
			StatusPoller poller = new StatusPoller();
			statusPoller.schedule(poller, 0, 2500); // How often to request AzEl in MS
			port.addEventListener(poller);
			port.notifyOnDataAvailable(true);
		} catch (IOException e) { throw new Failure(e); } 
		catch (TooManyListenersException e) { throw new Failure(e); }
		state.connected.set(true);
	}
	
	@Override
	public void disconnect() { super.disconnect(); statusPoller.cancel(); }

	@Override public boolean supportsAzSeperately() { return true; }

	@Override public boolean supportsElSeperately() { return false; }

	
	
	@Override public Rotator load(FileInputStream fos) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean save(FileOutputStream fos) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Small class to hold state information for rotator
	 */
	private static class StateHolder{
		
		public List<Failure> fails = Collections
				.synchronizedList(new ArrayList<Failure>());

		/**
		 * Checks if there are any failures in the queue
		 * 
		 * @throws Failure
		 *             the least recent failure
		 */
		public void checkFails() throws Failure {
			if (fails.size() > 0) throw fails.remove(0);
			return;
		}
	}

	private class StatusPoller extends TimerTask implements
			SerialPortEventListener {

		private final Pattern AZEL = Pattern.compile("AZ=-?(\\d+)  EL=-?(\\d+)");

		@Override
		public void serialEvent(SerialPortEvent spe) {
			if (spe.getEventType() != SerialPortEvent.DATA_AVAILABLE)
				return; // We don't know what to do with anything else
			try {
				if(!in.ready()) return;
				String response = in.readLine();
				Matcher azEl = AZEL.matcher(response);
				if (azEl.matches()) {
					state.az.set(Double.valueOf(azEl.group(1)));
					state.el.set(Double.valueOf(azEl.group(2)));
				} //else if ("?>".equals(response)){
					//System.out.println("Got ?>");
				//} 
				else stateHolder.fails.add(new Failure(
						"Unexpected response from rotator: " + response));
			} catch (IOException e) {
				stateHolder.fails.add(new Failure(e));
			}
		}

		@Override
		public void run() {
			try { sendCommand(MEASURE_AZEL); } 
		    catch (IOException e) {
				stateHolder.fails.add(new Failure(e));
				state.connected.set(false);
				statusPoller.cancel(); // No more
			}
		}

	}
}
package edu.cusat.gs.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import jsattrak.utilities.Console;
import jsattrak.utilities.ConsoleListener;

/**
 * Creates a log file in the directory data/logs everyday an action is performed. Name of the file is the
 * date. The log file contains the events that take place along with the dates and
 * times.
 * 
 * @author Saummya Kaushal
 * @author Nate Parsons
 * 
 */
public class LogFileWriter implements ConsoleListener {

	private String LOG_DIR = "data/logs";
	
	/** Used to write text to a character-output stream. **/
	private BufferedWriter out = null;

	/** Date and time of the creation of the log file; used as the name of the log file. **/
	private static final DateTimeFormatter format = ISODateTimeFormat.dateTime();
	
	/**
	 * <code>true</code> if a newline was encountered the last time
	 * {@link LogFileWriter#logMessage(String)} was called.
	 **/
	private boolean newlineFound = true;

	/** Creates the log file containing all the events of the day whenever an event takes place in that day. **/
	public LogFileWriter() {
		Console.registerOutputListener(this);
		String date = DateTimeFormat.forPattern("YYYY-MM-dd HH.mm.ss").print(
				new DateTime());
		try {
			out = new BufferedWriter(new FileWriter(
					new File(LOG_DIR, date + ".log"), true));
		} catch (IOException e) { handleIOException(e); }
	}

	/**
	 * Write message, date and time an event takes place into the file created
	 * that day.
	 * 
	 * @param message
	 *            String to be written into the file
	 */
	@Override
	public void logMessage(String message) {
		try {
			if (message != null) {
				// Pre-pend the date, but not if it's a newline
				if (newlineFound)
					out.write(format.print(new DateTime()) + ": ");
				newlineFound = message.endsWith(System.getProperty("line.separator"));
				out.write(message);
			}
			out.flush();
		} catch (IOException e) { handleIOException(e); }

	}

	/**
	 * Called in case an IO exception occurs while creating the file to handle the situation.
	 * @param e: The detailed error message.
	 */
	private void handleIOException(IOException e) {
		// Remove ourselves first so we don't cause a stack overflow
		Console.removeOutputListener(this);
		// Then let any remaining listeners know 
		e.printStackTrace();
	}
}

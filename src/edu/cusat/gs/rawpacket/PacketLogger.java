package edu.cusat.gs.rawpacket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Formatter;

import jsattrak.utilities.Bytes;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class PacketLogger {
    
	private File outputFile;
	private File directory;
	
    private BufferedWriter out;
   
    /**
     * Constructor
     * @param app
     * @param directory
     */
    public PacketLogger(File directory) {
    	this.directory = directory;
    }
    
    /**
     * Creates New Log file if one doesn't exist 
     */
    private void createLogFile() {
		this.outputFile = new File(directory + "/" + getDate() + ".txt");
		boolean created = (new File(directory + "")).mkdir();
    	   	
    	if (created && !outputFile.exists()) {
    		try {
				outputFile.createNewFile();
			} catch (IOException e1) {
				System.out.println("LOG FILE UNABLE TO BE CREATED.");
			}
    	}
        
        try { out = new BufferedWriter(new FileWriter(outputFile, true)); }		
        catch (FileNotFoundException e) { 
        	System.out.println("LOG FILE NOT FOUND"); } 
        catch (IOException e) {
        	System.out.println("COULD NOT OPEN LOG FILE");  }
    }
    
    /**
     * Formats Current Date
     * @return
     */
    private String getDate() {
    	Formatter fmt = new Formatter();
    	Calendar cal = Calendar.getInstance();
    	fmt.format("%tY%tm%td", cal, cal, cal);
		return(fmt  + ""); 
    }
    
    /**
     * Closes Output File Stream
     */
    public void close() {
    	try { out.close(); } 
    	catch (IOException e) { System.out.println("CANNOT CLOSE OUTPUTFILE STREAM."); }
    }
        
    /**
     * Log Packet, Write to Log File
     * @param packet p
     * @return true if successful, false otherwise
     */
   	public boolean logPacket(Packet p) {
		byte[] pckt = p.getByteArray();

		if ( outputFile == null || !(getDate() + ".txt").equals(outputFile.getName()) )
			createLogFile();
			
		try { 		
    		// print out header with date and time in UTC
			out.append(p.getSystemTime() + " :: " + p.getSimTime() + " :: ");
        	
        	// print bytes in Hex  	
        	out.append(Bytes.toHexString(pckt));
        	
        	out.append("\r\n");
				
        	out.flush();
        	
		} catch (IOException e) { return false; }	

    	return true;
    }
	
   	/**
   	 * Retrieves Packets from File
   	 * @param file
   	 * @return
   	 */
	public ArrayList<Packet> retrieveFromFile(File file) {
		BufferedReader reader;
		ArrayList<Packet> newPackets = new ArrayList<Packet>();
		
		if (file.exists()) { try { 
			reader = new BufferedReader(new FileReader(file));
	
	    	String line = reader.readLine();
	        while (line != null)
	        {
	        	int colonIndex1 = line.indexOf(" :: ");
	        	int colonIndex2 = (line.substring(colonIndex1 + 1)).indexOf(" :: ") + colonIndex1+1;
	        	if (!line.equals("") && colonIndex1 != -1 && colonIndex2 != -1){
	        		LocalDateTime systemTime = getJODAFromString(line.substring(0, colonIndex1), true);
	        		LocalDateTime simTime = getJODAFromString(line.substring(colonIndex1 + 4, colonIndex2), true);
	
	        		String array = line.substring(colonIndex2+4);
	
	        		byte[] byteArray = array.getBytes(Bytes.ASCII);
	
	        		line = reader.readLine();
	            
	        		newPackets.add(new Packet(byteArray, systemTime, simTime));	    
	        	}
	        	else
	        		line = reader.readLine();
	        }
	        reader.close();
	        
  		} catch (IOException e) { System.out.println("LOG FILE NOT FOUND"); }
		
		} else
			createLogFile();
  		
		return newPackets;
	}
	
	/**
	 * Extracts a LocalDateTime Object from a String
	 * @param subsetline
	 * @param timeZoneShift
	 * @return
	 * @throws IOException
	 */
	private LocalDateTime getJODAFromString(String subsetline, boolean timeZoneShift) throws IOException {
    	DateTimeFormatter parser = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss.SSS");
        return parser.parseDateTime(subsetline.substring(0, 23)).toLocalDateTime();
    }

	/**
	 * Retrieves all possible log file names in log directory
	 * @return
	 */
	public ArrayList<String> getLogFiles() {
		File[] list = directory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".txt");
			}
		});
		
		ArrayList<String> listOfDates = new ArrayList<String>();
		
		if (list != null)
			for(File f : list) {
				//format
				String date = f.getName().substring((f.getName().length()-12), (f.getName().length()-4));
				date = date.substring(0,4) + "-" + date.substring(4,6) + "-" + date.substring(6);
				
				//add to list
				listOfDates.add(date);
			}
		
		Collections.reverse(listOfDates);
		return listOfDates;
	}
}

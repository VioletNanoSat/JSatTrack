package edu.cusat.gs.rawpacket;

import org.joda.time.LocalDateTime;

/**
 *
 * @author David Bjanes
 */
public class Packet{

    private byte[] array;
    private LocalDateTime systemTime;
    private LocalDateTime simulatorTime;
    public boolean printSystemTime = true;

    public Packet() {
    	array = new byte[0];
    	systemTime = new LocalDateTime();
    	simulatorTime = new LocalDateTime();    	
    }
    
    public Packet(byte[] ary, LocalDateTime locDate, LocalDateTime simDate) {
        this.array = ary;
        this.systemTime = locDate;
        this.simulatorTime = simDate;
    }
    
    public byte[] getByteArray() {
        return array;
    }
    
    public LocalDateTime getSystemTime() {
    	return systemTime;
    }
    
    public String toString() {
		if (printSystemTime)
			return systemTime.toString();
		else
			return simulatorTime.toString();
    }
    
    public LocalDateTime getSimTime() {
    	return simulatorTime;
    }
}

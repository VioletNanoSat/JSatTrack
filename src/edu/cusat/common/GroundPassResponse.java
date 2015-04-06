package edu.cusat.common;

public class GroundPassResponse {

	/**
	 * Upcoming passes, in csv format (pass #, start time, end time)
	 */
	public final String upcomingPasses;
	
	public GroundPassResponse(String upPasses){
		upcomingPasses = upPasses;
	}
	
}

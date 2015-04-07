package edu.cusat.gs.rotator;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import edu.cusat.common.Failure;

public class RotatorController {
    private Rotator rotator;

    // TimeSpans
    private static final Duration ONE_SEC = Duration.standardSeconds(1);

    // Filter
    private Filter filter;
    
    //Offsets
    private int azOffset=0;
    private int elOffset=0;

    public RotatorController(Rotator rotator) {
        this.rotator = rotator;

        // Set up the listener for the port
        filter = new BasicFilter("");
    }

    public void rotate(int az, int el, DateTime goTime) throws Failure {
        while (goTime != null && goTime.plus(Duration.standardSeconds(1)).isAfterNow()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }

        // Don't send unless it's a recent direction and not redundant.
        if (new Duration(goTime, null).isLongerThan(ONE_SEC)) {
            // TODO Log
        } else if (filter.passes(az, el)) {
            rotator.azEl(az, el);
        } else {
            // TODO Log bad command
        }
    }

    public double[] rotate(int az, int el) throws Failure {
        if(filter.passes(az + azOffset, el+ elOffset))
			return rotator.azEl(az + azOffset, el + elOffset);
        return rotator.azEl();
    }
    
    public void addModelListener(RotatorModelListener listener){
    	rotator.addModelListener(listener);
    }
    
    // getter and setter methods for offsets
    public int getElOffset(){
    	return this.elOffset;
    }
    
    public int getAzOffset(){
    	return this.azOffset;
    }
    
    public void setElOffset(int elevOffset){
    	this.elOffset= elevOffset;
    }
    
    public void setAzOffset(int azimOffset){
    	this.azOffset= azimOffset;
    }  
}
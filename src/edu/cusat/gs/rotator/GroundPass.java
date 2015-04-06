package edu.cusat.gs.rotator;

import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.List;

import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.GroundStation;
import name.gano.astro.AER;
import name.gano.astro.time.Time;

/**
 *  
 *  TODO FIGURE OUT TIMES!!! JD OR MJD??!?!? (Hint:solve with DateTime everywhere)
 * @author Nate Parsons
 */
public class GroundPass {

    private final double cRiseTime;
    private final double cSetTime;
    private final GroundStation cGs;
    private final AbstractSatellite cSat;
    // We'll be lazy about these
    private double riseAz = Double.NEGATIVE_INFINITY; 
    private double setAz = Double.NEGATIVE_INFINITY; 

    /**
     * Private constructor to ensure that no invalid ground passes are created
     */
    private GroundPass(GroundStation mGs, AbstractSatellite mSat,
            double mRiseTime, double mSetTime) {
        cGs = mGs;
        cSat = mSat;
        cRiseTime = mRiseTime;
        cSetTime = mSetTime;
    }
    /**
     *
     * @return returns the satellite of the ground pass
     */
    public AbstractSatellite getCurrentSatellite()
    {
        return cSat;
    }
    /**
     * @return the time at which the satellite passes above the ground station's
     *         horizon (JD)
     */
    public double riseTime() { return cRiseTime; }
    
    /**
     * @return the azimuth (in degrees) where the satellite will rise above the
     *         ground station's horizon (JD)
     */
    public double riseAz(){
        if(riseAz == Double.NEGATIVE_INFINITY)
            riseAz = AER.calculate(cGs.getLla_deg_m(), cSat
                .calculateTemePositionFromUT(cRiseTime), cRiseTime)[0];
        return riseAz;
    }

    /**
     * @return the time at which the satellite passes below the ground station's
     *         horizon
     */
    public double setTime() { return cSetTime; }
    
    /**
     * @return the azimuth (in degrees) where the satellite will set below the
     *         ground station's horizon
     */
    public double setAz(){
        if(setAz == Double.NEGATIVE_INFINITY)
            setAz = AER.calculate(cGs.getLla_deg_m(), cSat
                .calculateTemePositionFromUT(cSetTime), cSetTime)[0];
        return setAz;
    }
    /**
     * @author Adam Mendrela
     * @return time how long the satellite will be above the ground station's
     *         horizon
     */
    public double passDuration() { return cRiseTime - cSetTime; }

    /**
     * Determines if a 'flip' of directions given to a rotator tracking this
     * ground pass is required.
     * <p>
     * This method is far from being able to solve the general case.
     * Specifically, it makes the following assumptions:
     * <ul>
     * <li>Ground station is on the northern hemisphere
     * <li>Satellite orbits west->east
     * <li>Rotator is calibrated so 0 degrees is true north
     * <li>Rotator has a mechanical stop that prevents it from rotating past 450
     * degrees
     * <li>Stored angles are within [0,360)
     * </ul>
     * 
     * A more general version of this function would accept parameters instead
     * of the above assumptions, and check to see if the satellite's path will
     * take it between quadrants separated by the stop.
     */
    public boolean requiresFlip() {
        return crossesNorth() && crossesEast();
    }
   
	/**
	 * Determines if the satellite will cross over due north (azimuth=0/360
	 * degrees)
	 * <p>
	 * This method is far from being able to solve the general case.
	 * Specifically, it makes the following assumptions:
	 * <ul>
	 * <li>Satellite orbits west->east
	 * <li>Stored angles are within [0,360)
	 * </ul>
	 * 
	 * A more general version of this function would accept parameters instead
	 * of the above assumptions, and check to see if the satellite's path will
	 * take it between quadrants separated by the stop.
	 */
    public boolean crossesNorth() { // Functions used to ensure lazy evaluation
		return (180 < riseAz() && riseAz <= 360)
				&& (0 < setAz() && setAz < (riseAz + 180) % 360);
    	
    }

	/**
	 * Determines if the satellite will cross over due east (azimuth=90/450
	 * degrees)
	 * <p>
	 * This method is far from being able to solve the general case.
	 * Specifically, it makes the following assumptions:
	 * <ul>
	 * <li>Satellite orbits west->east
	 * <li>Stored angles are within [0,360)
	 * </ul>
	 * 
	 * A more general version of this function would accept parameters instead
	 * of the above assumptions, and check to see if the satellite's path will
	 * take it between quadrants separated by the stop.
	 */
    public boolean crossesEast() {
        return ((270 < riseAz() && riseAz < 360) || (0 <= riseAz && riseAz < 90))
                && (90 < setAz() && setAz < (riseAz + 180) % 360);
    }
    
    /**
     * @param gs Ground Station
     * @param sat Satellite
     * @param start start Julian Date
     * @param duration how long to search for ground passes (days)
     * @param timeStep the granularity of each time step (days)
     * @return each ground pass during the specified period, in sequential order
     */
    public static List<GroundPass> find(GroundStation gs,
			AbstractSatellite sat, Time start, double duration, double timeStep) {
        List<GroundPass> passes = new ArrayList<GroundPass>();
        // linear search
        double time0, el0;
        double riseTime = -1;
        double cTime = start.getJulianDate();
        double time1 = cTime;
        
        double el1 = AER.calculate(gs.getLla_deg_m(), sat
                .calculateTemePositionFromUT(time1), time1)[1]
                - gs.getElevationConst();
         
        for (double jd = cTime; jd <= cTime
                + duration; jd += timeStep) {
            time0 = time1;
            time1 = jd + timeStep / (60.0 * 60.0 * 24.0);

            // calculate elevations at each time step (if needed)
            el0 = el1;
            // calculate the elevation at this newly visited point
            el1 = AER.calculate(gs.getLla_deg_m(), sat
                    .calculateTemePositionFromUT(time1), time1)[1]
                    - gs.getElevationConst();

            // rise
            if (el0 <= 0 && el1 > 0) {
                riseTime = findSatRiseSetRoot(sat, gs, time0, time1, el0,
                        el1);
            }

            // set
            if (el1 <= 0 && el0 > 0) {
                double setTime = findSatRiseSetRoot(sat, gs, time0, time1, el0,
                        el1);

                // add pass
                passes.add(new GroundPass(gs, sat, (riseTime > 0 ? riseTime
                        : start.getMJD()), setTime));
            } // set
        }// linear search
        
        return passes;
    }

    /**
     * Calculate upcoming ground passes using the ModPosLead data of the
     * satellite
     * 
     * @param gs
     *            Ground Station
     * @param sat
     *            Satellite
     * @return each ground pass during the specified period, in sequential order
     */
    public static List<GroundPass> find(GroundStation gs, AbstractSatellite sat){
        List<GroundPass> passes = new ArrayList<GroundPass>();
        
        double[][] modPos = sat.getTemePosLead();
        double[] times = sat.getTimeLead();

        
        // linear search
        double time0, time1, el0, el1;
        double riseTime = -1;

        for (int i = 0; i < times.length - 1; i++) {
            time0 = times[i];
            time1 = times[i+1];

            // calculate elevations at each time step (if needed)
            el0 = AER.calculate(gs.getLla_deg_m(), modPos[i], times[i])[1]
                    - gs.getElevationConst();
            el1 = AER.calculate(gs.getLla_deg_m(), modPos[i + 1], times[i + 1])[1]
                    - gs.getElevationConst();
           
            // rise
            if (el0 <= 0 && el1 > 0) {
                riseTime = findSatRiseSetRoot(sat, gs, time0, time1, el0,
                        el1);
            }

            // set
            if (el1 <= 0 && el0 > 0) {
                double setTime = findSatRiseSetRoot(sat, gs, time0, time1, el0,
                        el1);

                // add pass
                passes.add(new GroundPass(gs, sat, (riseTime > 0 ? riseTime
                        : times[0]), setTime));
            } // set
        }// linear search
         
        return passes;
    }
    
    /**
     * bisection method, crossing time should be bracketed by time0 and time1
     *
     * @param sat satellite
     * @param gs ground station
     * @param time0 first time
     * @param time1 second time
     * @param el0 elevation corresponding to first time
     * @param el1 elevation corresponding to second time
     * @return
     */
    public static double findSatRiseSetRoot(AbstractSatellite sat, GroundStation gs,
            double time0, double time1, double el0, double el1) {
        final double tol = (1.157407E-5) / 4; // 1/4 a sec (in units of a day)

        int iterCount = 0;

        while (abs(time1 - time0) > 2 * tol) {
            // Calculate midpoint of domain
            double timeMid = (time1 + time0) / 2.0;
            double fmid = AER.calculate(gs.getLla_deg_m(), sat
                    .calculateTemePositionFromUT(timeMid), timeMid)[1]
                    - gs.getElevationConst(); 
                        

            if (el0 * fmid > 0) {// same sign
                // replace f0 with fmid
                el0 = fmid;
                time0 = timeMid;

            } else { // else replace f1 with fmid
                el1 = fmid;
                time1 = timeMid;
            }

            iterCount++;
        } // while not in tolerance

        // return best guess using linear interpolation between last two points
        double a = (el1 - el0) / (time1 - time0);
        double b = el1 - a * time1;
        double time = -b / a;

        // System.out.println("Bisection Iters: " + iterCount);

        return time; // return best guess -typically: (time0 + time1)/2.0;
    } // findSatRiseSetRoot
}
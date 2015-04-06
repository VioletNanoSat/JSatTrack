package edu.cusat.gs.doppler;

import org.joda.time.Instant;

/**
 * TODO
 * @author Mike S
 */
public class Doppler {

    /** Current distance to satellite **/
    private double currRange;

    /** Current time **/
    private Instant currTime;

    /** Current doppler shift **/
    private double[] currShift = new double[2];

    /** Speed of light **/
    public static final double C = 299792458;
    
    /** Offset of TX in shift array **/
    public static final int TX_OFFSET = 0;
    
    /** Offset of RX in shift array **/
    public static final int RX_OFFSET = 1;

    /**
     * Constructs Doppler with initial readings
     * 
     * @param currRange
     *            distance from satellite to ground station in meters
     * @param currTime
     *            milliseconds relative to some epoch
     */
    public Doppler(double currRange, Instant currTime) {
        this.currRange = currRange;
        this.currTime = currTime;
    }

    /**
     * Updates Doppler
     * 
     * @param freq
     *            frequency in Hertz
     * @param range
     *            distance to satellite in meters
     * @param time
     *            milliseconds relative to the same epoch used in the
     *            constructor (or you will get inconsistent results)
     * @return {TX Frequency, RX Frequency}
     */
    public double[] getShift(double freq, double range, Instant time) {
        long elapsed = time.getMillis() - currTime.getMillis();
        if (elapsed != 0) {
            double distance = currRange - range;
            double vel = (1000 * distance) / elapsed;
            currShift[0] = freq * Math.sqrt((1 - vel / C)/(1 + vel / C));
            currShift[1] = freq / Math.sqrt((1 - vel / C)/(1 + vel / C));
            currTime = time;
        }
        // In case the range updated, so we don't have huge spikes in v
        currRange = range; 
        return new double[] { currShift[0], currShift[1] };
    }
}

package jsattrak.utilities;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

public class Times {
    
    static private final double MJD_JD_OFFSET = 2400000.5;
    
    static private final LocalDateTime MJD_EPOCH = new LocalDateTime(1858, 11, 17, 0,0,0,0);
    
    
    /** Prevent instances of this class from being created **/
    private Times(){}

    /**
     * Converts a time in Julian Date to DateTime
     */
    public static LocalDateTime dateTimeOfJd(double jd) {
		return MJD_EPOCH.plus(
				new Duration((long) (mjdOfJd(jd) * 24 * 60 * 60000)));
    }
    
    /**
     * Converts a time in Modified Julian Date to a DateTime object
     *
     * @param mjd
     *            - days since midnight, Nov 17 1858
     * @return the same instant in time, in the UTC timezone
     */
    public static LocalDateTime dateTimeOfMjd( double mjd ) {
        return MJD_EPOCH.plus( new Duration((long) (mjd*24*60*60000)) );
    }
    
    /**
     * @param jd
     *            An instant in time expressed as days since 12:00:00 January 1,
     *            4713 BC Julian proleptic calendar
     * @return the same instant in time expressed as days since 00:00:00
     *         November 17, 1858
     */
    public static double mjdOfJd( double jd ) { return jd - MJD_JD_OFFSET; }

    /**
     * @param mjd
     *            An instant in time expressed as days since 00:00:00 November
     *            17, 1858
     * @return the same instant in time expressed as days since 12:00:00 January
     *         1, 4713 BC Julian proleptic calendar
     */
    public static double jdOfMjd(double mjd) { return mjd + MJD_JD_OFFSET; }
}

/*
 * Azimuth, Elevation, Range Calculations
 * =====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 * 
 * This file is part of JSatTrak.
 * 
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 */

package name.gano.astro;

import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.toDegrees;
import jsattrak.objects.GroundStation;

/**
 * AER is both an immutable representation of the vector between a ground
 * station and satellite, and a collection of utility methods for calculating
 * the same.
 * 
 * @author sgano
 * @author Nate Parsons nsp25
 */
public final class AER {
	/** Azimuth - horizontal angle [deg] **/
	private final double azimuth;

	/** @return the azimuth [deg] **/
	public double az() { return azimuth; }

	/** Elevation - vertical angle [deg] **/
	private final double elevation;

	/** @return the elevation [deg] **/
	public double el() { return elevation; }

	/** Range - distance [m] **/
	private final double range;

	/** @return the range [m] **/
	public double range() { return range; }

	/** @return the azimuth [deg], elevation [deg], and range [m] **/
	public double[] aer() { return new double[] { az(), el(), range() }; }

	/**
	 * @param az
	 *            horizontal angle [deg]
	 * @param el
	 *            vertical angle [deg]
	 * @param range
	 *            distance [m]
	 */
	public AER(final double az, final double el, final double range) {
		this.azimuth = az;
		this.elevation = el;
		this.range = range;
	}

	/**
	 * @param aer
	 *            3-element array containing the azimuth [deg], elevation [deg],
	 *            and range [m]
	 */
	public AER(final double[] aer) { this(aer[0], aer[1], aer[2]); }

	/**
	 * Create an immutable representation of the vector between a ground station
	 * and given ECI position as an azimuth, elevation, and range.
	 * 
	 * @param gs
	 *            Ground Station
	 * @param eci_pos
	 *            position in Earth Centered Inertial reference frame [m]
	 * @param time
	 *            Julian date associated with eci_pos
	 */
	public AER(final GroundStation gs, final double[] eci_pos, 
		       final double time) {
        this(calculate(gs.getLla_deg_m(), eci_pos, time));
    }

    /** Copy constructor **/
    public AER(AER orig) {
        azimuth = orig.az();
        elevation = orig.el();
        range = orig.range();
    }

    /**
     * 
     */
    public static AER[] calc_AERs(final GroundStation gs,
            final double[][] eci_pos, final double[] times) {
        double[][] aers = calculate(gs.getLla_deg_m(), eci_pos, times);
        AER[] ret = new AER[aers.length];
        for (int i = 0; i < ret.length; i++)
            ret[i] = new AER(aers[i]);
        return ret;
    }

    /**
     * Calculates the Azumuth, Elevation, and Range from Ground Station to set
     * of positions
     * 
     * @param lla_deg_m_GS
     *            Lat/Lon/Alt in degrees and meters of Ground Station
     * @param eci_pos
     *            ECI positions [i][3] for different times - Mean of Date
     *            Position!! (since SGP4 is MOD)
     * @param times
     *            times associated with eci_pos (Julian Date)
     * @return Azumuth [deg], Elevation [deg], and Range vector [m], [i][3]
     */
    public static double[][] calculate(final double[] lla_deg_m_GS,
                                       final double[][] eci_pos, 
                                       final double[] times) {
        double[][] aer = new double[times.length][3];
        for (int i = 0; i < times.length; i++)
            aer[i] = calculate(lla_deg_m_GS, eci_pos[i], times[i]);
        return aer;
    }

    /**
     * Overloaded function - Calculates the Azumuth, Elevation, and Range from
     * Ground Station to one position
     * 
     * @param lla_deg_m_GS
     *            Lat/Lon/Alt in degrees and meters of Ground Station
     * @param eci_pos
     *            ECI position [3] for times - Mean of Date Position!! (since
     *            SGP4 is MOD)
     * @param time
     *            time associated with eci_pos (Julian Date)
     * @return Azumuth [deg], Elevation [deg], and Range vector [m], [i][3]
     */
    public static double[] calculate(final double[] lla_deg_m_GS,
            						 final double[] eci_pos, 
            						 final double time) {

        double az, el, range;

        // 0th step get local mean Sidereal time
        // first get mean sidereal time for this station
        double thetaDeg = Sidereal.Mean_Sidereal_Deg(time
                                      - AstroConst.JDminusMJD, lla_deg_m_GS[1]);

        // first calculate ECI position of Station
        double[] eciGS = GeoFunctions.calculateECIpositionGivenSidereal(
                lla_deg_m_GS, thetaDeg);

        // find the vector between pos and GS
        double[] rECI = MathUtils.sub(eci_pos, eciGS);

        // calculate range
        range = MathUtils.norm(rECI);

        // now transform ECI to topocentric-horizon system (SEZ) (use Geodetic
        // Lat, not geocentric)
        // ECI vec, sidereal in Deg, latitude in deg
        double[] rSEZ = GeoFunctions.eci2sez(rECI, thetaDeg, lla_deg_m_GS[0]);

        // compute azimuth [radians] -> Deg
        az = toDegrees(atan2(-rSEZ[0], rSEZ[1]));

        //System.out.format("az=%f, rSEZ[-0,1]=%f,%f%n", az, -rSEZ[0], rSEZ[1]);

        // do conversions so N=0, S=180, W=270
        if (az <= 0) {
            az = abs(az) + 90;
        } else if (az <= 90) { // (between 0 and 90)
            az = -1.0 * az + 90.0;
        } else { // between 90 and 180
            az = -1.0 * az + 450.0;
        }

        // compute elevation [radians] > Deg
        el = toDegrees(asin(rSEZ[2] / range));

        //System.out.printf("SEZ: %f, %f, %f%n", rSEZ[0], rSEZ[1], rSEZ[2]);
        return new double[] { az, el, range };
    }
}

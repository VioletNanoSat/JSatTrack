/*
 * Class used to model the sun (in Earth Equatorial Coordinates)
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

package name.gano.astro.bodies;

import static java.util.Arrays.copyOf;

import name.gano.astro.AstroConst;
import name.gano.astro.GeoFunctions;
import name.gano.astro.MathUtils;
import name.gano.astro.coordinates.CoordinateConversion;

/**
 * 
 * @author Shawn E. Gano
 */
public class Sun {

    
    private double[] currentPosition; // current J2000 position of the Sun
    private double currentMJD; // current Modified Julian Date - UT
    //private double[] lla; // lat, lon, alt (in radians,radians,meters)
    private double[] currentPositionTEME; // TEME of date position
    //private double[] lla; // lat and long
    private double[] darkCenterLLA; // center Lat/Long of darkness
    private double[] sunCenterLLA;

    /**
     * Creates a new instance of Sun
     * 
     * @param iniMJD
     *            initial Modified Julian Date
     */
    public Sun(double iniMJD) {
        currentMJD = iniMJD;
        setJ2KPosition(sunPositionLowUT(currentMJD));
    }

    /**
     * sets J2K position as well as calculates MOD position
     * 
     * @param newJ2kPos
     *            j2K position
     */
    private void setJ2KPosition(double[] newJ2kPos) {
        currentPosition = newJ2kPos;
        // convert to MOD, J2000-> MOD
        //double[][] prec =  CoordinateConversion.PrecMatrix_Equ(0.0, (currentMJD-AstroConst.MJD_J2000)/36525)
        //currentPositionMOD = MathUtils.mult(prec,currentPosition); // currentPosition
        // ERROR above in conversion - convert to TEME (not MOD)
        currentPositionTEME = CoordinateConversion.J2000toTEME(currentMJD, currentPosition);
        
        
        // LLA of sun center and darkness center
        sunCenterLLA = GeoFunctions.geodeticLLA_mjd( currentPositionTEME , currentMJD );
        darkCenterLLA = GeoFunctions.geodeticLLA_mjd( getOpositeSunPositionTEME() , currentMJD );
        
        //System.out.println("Sun LLA:" + sunCenterLLA[0]*180/Math.PI + ", " + sunCenterLLA[1]*180/Math.PI);
    }
     
    
    /**
     * Computes the Sun's geocentric position using a low precision analytical series
     *
     * @param mjd modified Julian date (UT)
     * @return Solar position vector [m] with respect to the mean equator and equinox of J2000 (EME2000, ICRF)
     */
    public double[] sunPositionLowUT(double mjd)
    {
        double mjdTT = mjd + name.gano.astro.time.Time.deltaT(mjd); // corrected time to TT from UT
        
        return calculateSunPositionLowTT(mjdTT);
    }

    /**
     * Computes the Sun's geocentric position using a low precision analytical
     * series
     * 
     * @param Mjd_TT
     *            Terrestrial Time (Modified Julian Date)
     * @return Solar position vector [m] with respect to the mean equator and
     *         equinox of J2000 (EME2000, ICRF)
     */
    public static double[] calculateSunPositionLowTT(double Mjd_TT) {
        // Constants

        // Obliquity of J2000 ecliptic
        final double eps = Math.toRadians(23.43929111);
        // Julian cent. since J2000
        final double T = (Mjd_TT - AstroConst.MJD_J2000) / 36525.0; 

        // Variables
        double L, M, r;

        // Mean anomaly, ecliptic longitude and radius

	  M = 2.0*Math.PI * MathUtils.frac( 0.9931267 + 99.9973583*T);             // [rad]
	  L = 2.0*Math.PI * MathUtils.frac ( 0.7859444 + M/(2.0*Math.PI) + 
	                    (6892.0*Math.sin(M)+72.0*Math.sin(2.0*M)) / 1296.0e3); // [rad]
	  r = 149.619e9 - 2.499e9*Math.cos(M) - 0.021e9*Math.cos(2*M);             // [m]
	  
	  // Equatorial position vector
	  double[] temp = {r*Math.cos(L),r*Math.sin(L),0.0};
	  double[] r_Sun = MathUtils.mult(MathUtils.R_x(-eps) , temp);
	  
	  return r_Sun;
	} // SunPositionLow

    /**
     * get current j2000.0 position of the sun
     * 
     * @return current j2000.0 position
     */
    public double[] getCurrentPositionJ2K() {
        return copyOf(currentPosition, currentPosition.length);
    }

    /**
     * get current modified julian date
     * 
     * @return MJD
     */
    public double getCurrentMJD() {
        return currentMJD;
    }

    /**
     * Set current time - a new position is calculated
     * 
     * @param currentMJD
     *            modified julian date
     */
    public void setCurrentMJD(double currentMJD) {
        this.currentMJD = currentMJD;
        // update position
        setJ2KPosition(sunPositionLowUT(currentMJD));
    }

    /**
     * returns oposite position from sun (dark side origin)
     * 
     * @return J2k position [m]
     */
    public double[] getOpositeSunPositionJ2K() {
        return new double[] { -currentPosition[0], -currentPosition[1],
                -currentPosition[2] };
    }

    /**
     * returns opposite position from the sun TEME
     * @return TEME of date position [m]
     */
    public double[] getOpositeSunPositionTEME()
    {
        return new double[] {-currentPositionTEME[0],-currentPositionTEME[1],-currentPositionTEME[2]};
    }

    /**
     *  Return current sun position in Earth Equatorial coordinates with mean of date Equinox
     *  (ECI) 
     * @return TEME of date position [m]
     */
    public double[] getCurrentPositionTEME()
    {
        return currentPositionTEME;
    }
    
    /**
     * Latitude/Longitude/Altitude of position opposite sun  
     *
     * @return Latitude/Longitude/Altitude [rad]
     */
    public double[] getCurrentDarkLLA() {
        return copyOf(darkCenterLLA, darkCenterLLA.length);
    }

    /**
     * Latitude/Longitude/Altitude of Sun
     * 
     * @return Latitude/Longitude/Altitude [rad]
     */
    public double[] getCurrentLLA() {
        return copyOf(sunCenterLLA, sunCenterLLA.length);
    }

    /**
     * Computes the acceleration due to solar radiation pressure assuming the
     * spacecraft surface normal to the Sun direction Note: r, r_sun, Area,
     * mass, P0 and AU must be given in consistent units, e.g. m, m^2, kg and
     * N/m^2.
     * 
     * @param r
     *            Spacecraft position vector
     * @param r_Sun
     *            Sun position vector
     * @param Area
     *            Cross-section
     * @param mass
     *            Spacecraft mass
     * @param CR
     *            Solar radiation pressure coefficient
     * @param P0
     *            Solar radiation pressure at 1 AU
     * @param AU
     *            Length of one Astronomical Unit
     * @return Acceleration (a=d^2r/dt^2)
     */
    public static double[] AccelSolrad(final double[] r, final double[] r_Sun,
            double Area, double mass, double CR, double P0, double AU) {
        // Relative position vector of spacecraft w.r.t. Sun
        double[] d = MathUtils.sub(r, r_Sun);

        // Acceleration
        return MathUtils.scale(d, CR * (Area / mass) * P0 * (AU * AU)
                / Math.pow(MathUtils.norm(d), 3));
    } // AccelSolrad

    /**
     * Computes the fractional illumination of a spacecraft in the vicinity of
     * the Earth assuming a cylindrical shadow model
     * 
     * @param r
     *            Spacecraft position vector [m]
     * @param r_Sun
     *            Sun position vector [m]
     * @return Illumination factor: nu=0 Spacecraft in Earth shadow, nu=1
     *         Spacecraft fully illuminated by the Sun
     */
    public static double illumination(final double[] r, final double[] r_Sun) {

        double[] e_Sun = MathUtils.scale(r_Sun, 1.0 / MathUtils.norm(r_Sun)); // Sun
                                                                              // direction
                                                                              // unit
                                                                              // vector
        double s = MathUtils.dot(r, e_Sun); // Projection of s/c position

        double illuminated = 0.0;

        if (s > 0
                || MathUtils.norm(MathUtils.sub(r, MathUtils.scale(e_Sun, s))) > AstroConst.R_Earth) {
            illuminated = 1.0;
        } else {
            illuminated = 0.0;
        }

        return illuminated;
    } // illumination

} // Sun

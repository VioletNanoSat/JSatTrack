/**
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
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.PI;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

/**
 * Earth computation functions
 */
public class GeoFunctions
{
    
    // same as below except the function takes Julian Date
    /**
     * Compute Geodetic Latatude/Longitude/Altitude from Mean of Date position vector and Date 
     *
     * @param modPos Mean of date position vector
     * @param mjd modified julian date  (is this UTC or TT?) guessing UTC
     * @return vector of geodetic [latitude,longitude,altitude]
     */
    public static double[] geodeticLLA_mjd(double[] modPos, double mjd)
    {
        // calculate days since Y2k
        // MJD = JD - 2400000.5
        // passed in MJD - 51544.5
        //double daysSinceY2k = (julDate - 2400000.5)-51544.5;
        double daysSinceY2k = mjd - 51544.5;
        
        return geodeticLLA_y2k(modPos, daysSinceY2k);
    } // GeodeticLLA
    
    
    /** 
     * LLA = corrected for time (geographical coordinates) for handling geodetic coordinates
     * @param r TEME positions
     * @param d days since Y2K
     * @return Lat, Long, Alt
     */
    private static double[] geodeticLLA_y2k(double[] r, double d)
    {
        final double R_equ    = AstroConst.R_Earth; // Equator radius [m]
        final double f        = AstroConst.f_Earth; // Flattening
        final double eps_mach = 2.22E-16;           // machine precision
        
        // Check validity of input data
        // TODO Shouldn't this throw an exception?
        if (MathUtils.norm(r) < eps_mach) { 
        	System.out.println("Invalid input in geodeticLLA_y2k");
        	return new double[]{0, 0, -R_equ};
        }

        
        final double  eps     = 1.0e3*eps_mach;   // Convergence criterion
        final double  epsRequ = eps*R_equ;
        final double  e2      = f*(2.0-f);        // Square of eccentricity
        
        final double  X = r[0];                   // Cartesian coordinates
        final double  Y = r[1];
        final double  Z = r[2];
        final double  rho2 = X*X + Y*Y;           // Square of distance from z-axis
        
        // original class vars
        // double lon;
        // double lat;
        //double h;
        double[] LLA = new double[3];
        
        // Iteration
        double  dZ, dZ_new, SinPhi;
        double  ZdZ, Nh, N;
        
        dZ = e2*Z;
        for(;;) {
            ZdZ    =  Z + dZ;
            Nh     =  sqrt( rho2 + ZdZ*ZdZ );
            SinPhi =  ZdZ / Nh;                    // Sine of geodetic latitude
            N      =  R_equ / sqrt(1.0-e2*SinPhi*SinPhi);
            dZ_new =  N*e2*SinPhi;
            if ( abs(dZ-dZ_new) < epsRequ ) break;
            dZ = dZ_new;
        }
        
        // Longitude, latitude, altitude
        //double[] LLA = new double[3];
        LLA[1] = atan2( Y, X );  // longitude,  lon
        LLA[0] = atan2( ZdZ, sqrt(rho2) ); // latitude, lat
        LLA[2] = Nh - N; // altitute, h
        
        //System.out.println("LLA[1]: "+ LLA[1]);
        //LLA[1] = LLA[1] -(280.4606 +360.9856473*d)*PI/180.0; // shift based on time
        // add fidelity to the line above
        LLA[1] = LLA[1] - earthRotationDeg(d)*PI/180.; // shift based on time
        double div = Math.floor(LLA[1]/(2*PI));
        LLA[1] = LLA[1] - div*2.*PI;
        if(LLA[1] > PI)
        {
            LLA[1] = LLA[1]- 2.*PI;
        }
        
        //System.out.println("LLA[1]a: "+ LLA[1]);
        
        return LLA; //h
        
    } // calculateGeodeticLLA

    // SEG 10 June 2009 - help standardize earth Rotations
    private static double earthRotationDeg(double d) // days since y2K
    {
        // LLA[1] = LLA[1] -(280.4606 +360.9856473*d)*Math.PI/180.0; // shift based on time

        // calculate T
        double T = (d)/36525.0;

        // do calculation
        return ( (280.46061837 + 360.98564736629*(d)) + 0.000387933*T*T - T*T*T/38710000.0) % 360.0;

    } // earthRotationRad

    /** 
     * Site: Calculates the geocentric position of a site on the Earth's surface
     * 
     * @param lambda Geographical longitude (east positive) in [rad]
     * @param phi Geographical latitude [rad]
     * @param alt altitude in meters (this is 0 if on the Earth's surface)
     * @return Geocentric position [m]
     */
    public static double[] lla2ecef(double lambda, double phi, double alt)
    {
        //
        // Constants
        //
        //double f = 1.0 / 298.257;   // Flattening
        double e_sqr = AstroConst.f_Earth * (2.0 - AstroConst.f_Earth);     // Square of eccentricity
        double cos_phi = cos(phi);    // (Co)sine of geographical latitude
        double sin_phi = sin(phi);
        
        //
        // Variables
        //
        double N = AstroConst.R_Earth_major / sqrt(1.0 - e_sqr * (sin_phi * sin_phi));


        // Cartesian position vector [km]
        return new double[] {(N+alt) * cos_phi * cos(lambda),
                             (N+alt) * cos_phi * sin(lambda),
                             ((1.0 - e_sqr) * N + alt) * sin_phi};
        // also see (for adding in alt - http://www.posc.org/Epicentre.2_2/DataModel/ExamplesofUsage/eu_cs35.html
    }

    /**
     * function to convert earth-centered earth-fixed (ECEF) cartesian
     * coordinates to Lat, Long, Alt
     * <p>
     * DOES NOT INCLUDE UPDATES FOR time
     * <p>
     * SEG 31 Match 2009 -- slightly less accurate (but faster) version of:
     * GeoFunctions.calculateGeodeticLLA without time shift of latitude
     * <p>
     * source:
     * http://www.mathworks.com/matlabcentral/fx_files/7941/1/ecef2lla.m,
     * http://www.mathworks.com/matlabcentral/fileexchange/7941
     * 
     * @see GeoFunctions#lla2ecef(double, double, double) Reverse,
     *      http://www.mathworks.com/matlabcentral/fileexchange/7942
     */
    public static double[] ecef2lla_Fast(double[] pos) // d is current MDT time
    {
        double[] lla = new double[3];

        // WGS84 ellipsoid constants:
        double a = 6378137;
        double e = 8.1819190842622e-2; // 0;%8.1819190842622e-2/a;%8.1819190842622e-2;  % 0.003352810664747

        double b = sqrt(pow(a,2.0)*(1-pow(e,2)));
        double ep = sqrt( (pow(a,2.0)-pow(b,2.0))/pow(b,2.0));
        double p   = sqrt(pow(pos[0],2.0)+pow(pos[1],2.0));
        double th  = atan2(a*pos[2],b*p);
        lla[1] = atan2(pos[1],pos[0]);
        lla[0] = atan2((pos[2]+pow(ep,2.0)*b*pow(sin(th),3.0)),(p-pow(e,2.0)*a*pow(cos(th),3.0)));
        double N   = a/sqrt(1-pow(e,2.0)*pow(sin(lla[0]),2.0));
        lla[2] = p/cos(lla[0])-N;


        if(lla[1] < 0) {
            lla[1] = 2.0*PI + lla[1];
        }

        // return lon in range [0,2*pi)
        lla[1] = lla[1] % (2.0*PI); // modulus

        // correct for numerical instability in altitude near exact poles:
        // (after this correction, error is about 2 millimeters, which is about
        // the same as the numerical precision of the overall function)

        if(abs(pos[0])<1.0 && abs(pos[1])<1.0)
        {
            lla[2] = abs(pos[2])-b;
        }

        // now scale longitude from [0,360] -> [-180,180]
        if(lla[1] > PI) // > 180
        {
            lla[1] = lla[1] - 2.0*PI;
        }
/*
                // now correct for time shift
                // account for earth rotations
                lla[1] = lla[1]-(280.4606 +360.9856473*d)*PI/180.0;

                // correction ??
                //lla[1] = lla[1]-PI/2.0;

                // now ensure [-180,180] range
                double div = floor(lla[1]/(2*PI));
                lla[1] = lla[1] - div*2*PI;
                if(lla[1] > PI)
                {
                        lla[1] = lla[1]- 2.0*PI;
                }
 */

        return lla;

    } // ecef2lla
    
        /**
     * Calculate the ECI position from Lat/Long/Alt and a Julian Date (uses
     * Earth Flattening; WGS-84). This calculates the sidereal for you.
     * @param lla_deg_m
     *            lat [deg], long [deg], altitude [m]
     * @param currentJulianDate
     *            Julian Date
     * 
     * @return ECI position [x,y,z]
     */
    public static double[] calculateECIposition(double[] lla_deg_m,
            double currentJulianDate) {
        // first get mean sidereal time for this station
        double theta = Sidereal.Mean_Sidereal_Deg(currentJulianDate
                - AstroConst.JDminusMJD, lla_deg_m[1]);
        
        return GeoFunctions.calculateECIpositionGivenSidereal(lla_deg_m, theta);
        
    } // calculateECIposition


    /**
     * ECI position in meters of a position (tpyically ground site) - Uses Earth
     * Flattening; WGS-84 theta is pass in as Degrees!! (it is the local mean
     * sidereal time)
     * @param lla_deg_m
     * @param theta
     * 
     * @return
     */
    public static double[] calculateECIpositionGivenSidereal(double[] lla_deg_m,
            double theta) {
        // calc local mean sidereal time
        // double theta =
        // Sidereal.Mean_Sidereal_Deg(currentJulianDate-AstroConst.JDminusMJD,
        // lla_deg_m[1]);
    
        // calculate the ECI j2k position vector of the ground station at the
        // current time
        double[] eciVec = new double[3];
    
/*      // calculate geocentric latitude - using non spherical earth (in radians)
        // http://celestrak.com/columns/v02n03/
        double geocentricLat = atan( pow(1.0-AstroConst.f_Earth, 2.0) * tan( lla_deg_m[0]*PI/180.0 ) ); // (1-f)^2 tan(?).
               
        double r = AstroConst.R_Earth * cos( geocentricLat );
        eciVec[0] = r * cos(toRadians(theta));
        eciVec[1] = r * sin(toRadians(theta));
        eciVec[2] = AstroConst.R_Earth * sin( geocentricLat );
*/    
        // alternate way to calcuate ECI position - using earth flattening
        // http://celestrak.com/columns/v02n03/
        double C = 1.0 / sqrt(1.0 + AstroConst.f_Earth
                * (AstroConst.f_Earth - 2.0)
                * pow(sin(toRadians(lla_deg_m[0])), 2.0));
        double S = pow(1.0 - AstroConst.f_Earth, 2.0) * C;
    
        eciVec[0] = AstroConst.R_Earth * C * cos(toRadians(lla_deg_m[0]))
                * cos(toRadians(theta));
        eciVec[1] = AstroConst.R_Earth * C * cos(toRadians(lla_deg_m[0]))
                * sin(toRadians(theta));
        eciVec[2] = AstroConst.R_Earth * S * sin(toRadians(lla_deg_m[0]));
    
        return eciVec;
    
    } // calculateECIposition

    /**
     * Transform ECI to topocentric-horizon system (SEZ) (south-East-Zenith)
     * 
     * @param rECI
     * @param thetaDeg
     * @param latDeg
     * @return
     */
    public static double[] eci2sez(double[] rECI, double thetaDeg, double latDeg) {
        double[] rSEZ = new double[3]; // new postion in SEZ coorinates

        // ? (the local sidereal time) -> (thetaDeg*PI)
        // ? (the observer's latitude) - > (latDeg*PI)
        rSEZ[0] = sin(toRadians(latDeg)) * cos(toRadians(thetaDeg)) * rECI[0]
                + sin(toRadians(latDeg)) * sin(toRadians(thetaDeg)) * rECI[1]
                - cos(toRadians(latDeg)) * rECI[2];
        rSEZ[1] = -sin(toRadians(thetaDeg)) * rECI[0]
                + cos(toRadians(thetaDeg)) * rECI[1];
        rSEZ[2] = cos(toRadians(latDeg)) * cos(toRadians(thetaDeg)) * rECI[0]
                + cos(toRadians(latDeg)) * sin(toRadians(thetaDeg)) * rECI[1]
                + sin(toRadians(latDeg)) * rECI[2];

        return rSEZ;
    }
    
}

/*
 * Static methods dealing with the Atmosphere
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

import name.gano.astro.bodies.Sun;

/**
 *
 * @author sgano
 */
public class Atmosphere 
{
    
    /**
     * Computes the acceleration due to the atmospheric drag. 
     * (uses modified Harris-Priester model)
     * 
     * @param Mjd_TT Terrestrial Time (Modified Julian Date)
     * @param r Satellite position vector in the inertial system [m]
     * @param v Satellite velocity vector in the inertial system [m/s]
     * @param T Transformation matrix to true-of-date inertial system
     * @param Area Cross-section [m^2]
     * @param mass Spacecraft mass [kg]
     * @param CD Drag coefficient
     * @return Acceleration (a=d^2r/dt^2) [m/s^2]
     */
    public static double[] accelDrag(double Mjd_TT, final double[] r, final double[] v,
            final double[][] T, double Area, double mass, double CD)
    {

        // Constants

        // Earth angular velocity vector [rad/s]
        final double[] omega = {0.0, 0.0, 7.29212e-5};

        // Variables
        double v_abs, dens;
        double[] r_tod, v_tod, v_rel, a_tod ;
        double[][] T_trp;


        // Transformation matrix to ICRF/EME2000 system

        T_trp = MathUtils.transpose(T);


        // Position and velocity in true-of-date system

        r_tod = MathUtils.mult(T, r);
        v_tod = MathUtils.mult(T, v);


        // Velocity relative to the Earth's atmosphere

        v_rel = MathUtils.sub(v_tod, MathUtils.cross(omega, r_tod));
        v_abs = MathUtils.norm(v_rel);


        // Atmospheric density due to modified Harris-Priester model

        dens = density_HP(Mjd_TT, r_tod);

        // Acceleration 
        a_tod = MathUtils.scale(v_rel, -0.5 * CD * (Area / mass) * dens * v_abs);

        return MathUtils.mult(T_trp, a_tod);

    } // accellDrag
    

    /**
     * Computes the atmospheric density for the modified Harris-Priester model.
     * 
     * @param Mjd_TT Terrestrial Time (Modified Julian Date)
     * @param r_tod Satellite position vector in the inertial system [m]
     * @return Density [kg/m^3]
     */
    public static double density_HP(double Mjd_TT, final double[] r_tod)
    {
        // Constants

        final double upper_limit = 1000.0;           // Upper height limit [km]
        final double lower_limit = 100.0;           // Lower height limit [km]
        final double ra_lag = 0.523599;           // Right ascension lag [rad]
        final int n_prm = 3;           // Harris-Priester parameter 
        // 2(6) low(high) inclination

        // Harris-Priester atmospheric density model parameters 
        // Height [km], minimum density, maximum density [gm/km^3]

        final int N_Coef = 50;
        final double[] h = {
            100.0, 120.0, 130.0, 140.0, 150.0, 160.0, 170.0, 180.0, 190.0, 200.0,
            210.0, 220.0, 230.0, 240.0, 250.0, 260.0, 270.0, 280.0, 290.0, 300.0,
            320.0, 340.0, 360.0, 380.0, 400.0, 420.0, 440.0, 460.0, 480.0, 500.0,
            520.0, 540.0, 560.0, 580.0, 600.0, 620.0, 640.0, 660.0, 680.0, 700.0,
            720.0, 740.0, 760.0, 780.0, 800.0, 840.0, 880.0, 920.0, 960.0, 1000.0
        };
        final double[] c_min = {
            4.974e+05, 2.490e+04, 8.377e+03, 3.899e+03, 2.122e+03, 1.263e+03,
            8.008e+02, 5.283e+02, 3.617e+02, 2.557e+02, 1.839e+02, 1.341e+02,
            9.949e+01, 7.488e+01, 5.709e+01, 4.403e+01, 3.430e+01, 2.697e+01,
            2.139e+01, 1.708e+01, 1.099e+01, 7.214e+00, 4.824e+00, 3.274e+00,
            2.249e+00, 1.558e+00, 1.091e+00, 7.701e-01, 5.474e-01, 3.916e-01,
            2.819e-01, 2.042e-01, 1.488e-01, 1.092e-01, 8.070e-02, 6.012e-02,
            4.519e-02, 3.430e-02, 2.632e-02, 2.043e-02, 1.607e-02, 1.281e-02,
            1.036e-02, 8.496e-03, 7.069e-03, 4.680e-03, 3.200e-03, 2.210e-03,
            1.560e-03, 1.150e-03
        };
        final double[] c_max = {
            4.974e+05, 2.490e+04, 8.710e+03, 4.059e+03, 2.215e+03, 1.344e+03,
            8.758e+02, 6.010e+02, 4.297e+02, 3.162e+02, 2.396e+02, 1.853e+02,
            1.455e+02, 1.157e+02, 9.308e+01, 7.555e+01, 6.182e+01, 5.095e+01,
            4.226e+01, 3.526e+01, 2.511e+01, 1.819e+01, 1.337e+01, 9.955e+00,
            7.492e+00, 5.684e+00, 4.355e+00, 3.362e+00, 2.612e+00, 2.042e+00,
            1.605e+00, 1.267e+00, 1.005e+00, 7.997e-01, 6.390e-01, 5.123e-01,
            4.121e-01, 3.325e-01, 2.691e-01, 2.185e-01, 1.779e-01, 1.452e-01,
            1.190e-01, 9.776e-02, 8.059e-02, 5.741e-02, 4.210e-02, 3.130e-02,
            2.360e-02, 1.810e-02
        };


        // Variables

        int i, ih;                                 // Height section variables        
        double height;                             // Earth flattening
        double dec_Sun, ra_Sun, c_dec;             // Sun declination, right asc.
        double c_psi2;                             // Harris-Priester modification
        double density, h_min, h_max, d_min, d_max;// Height, density parameters
        double[] r_Sun;                            // Sun position
        double[] u = new double[3];                // Apex of diurnal bulge


        // Satellite height (in km)
        height = GeoFunctions.geodeticLLA_mjd(r_tod, Mjd_TT)[2] / 1000.0; //Geodetic(r_tod) / 1000.0; //  [km]


        // Exit with zero density outside height model limits
        if (height >= upper_limit || height <= lower_limit) {
            return 0.0;
        }


        // Sun right ascension, declination
        r_Sun = Sun.calculateSunPositionLowTT(Mjd_TT);
        ra_Sun = Math.atan2(r_Sun[1], r_Sun[0]);
        dec_Sun = Math.atan2(r_Sun[2], Math.sqrt(Math.pow(r_Sun[0], 2) + Math.pow(r_Sun[1], 2)));


        // Unit vector u towards the apex of the diurnal bulge
        // in inertial geocentric coordinates
        c_dec = Math.cos(dec_Sun);
        u[0] = c_dec * Math.cos(ra_Sun + ra_lag);
        u[1] = c_dec * Math.sin(ra_Sun + ra_lag);
        u[2] = Math.sin(dec_Sun);


        // Cosine of half angle between satellite position vector and
        // apex of diurnal bulge
        c_psi2 = 0.5 + 0.5 * MathUtils.dot(r_tod, u) / MathUtils.norm(r_tod);


        // Height index search and exponential density interpolation
        ih = 0;                           // section index reset
        for (i = 0; i < N_Coef - 1; i++)       // loop over N_Coef height regimes
        {
            if (height >= h[i] && height < h[i + 1])
            {
                ih = i;                       // ih identifies height section
                break;
            }
        }

        h_min = (h[ih] - h[ih + 1]) / Math.log(c_min[ih + 1] / c_min[ih]);
        h_max = (h[ih] - h[ih + 1]) / Math.log(c_max[ih + 1] / c_max[ih]);

        d_min = c_min[ih] * Math.exp((h[ih] - height) / h_min);
        d_max = c_max[ih] * Math.exp((h[ih] - height) / h_max);

        // Density computation

        density = d_min + (d_max - d_min) * Math.pow(c_psi2, n_prm);


        return density * 1.0e-12;       // [kg/m^3]

    } // Density_HP
    
}
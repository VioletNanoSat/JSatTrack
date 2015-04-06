/*
 * Class used to model the Moon
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

import static java.lang.Math.cos;
import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import name.gano.astro.AstroConst;
import name.gano.astro.MathUtils;

/**
 *
 * @author sgano
 */
public class Moon 
{
    
    /**
     * Computes the Moon's geocentric position using a low precision analytical series
     * @param Mjd_TT Terrestrial Time (Modified Julian Date)
     * @return Lunar position vector [m] with respect to the mean equator and equinox of J2000 (EME2000, ICRF)
     */
    public static double[] MoonPosition(double Mjd_TT)
    {
        // Constants

        final double eps = toRadians(23.43929111);  // Obliquity of J2000 ecliptic
        final double T = (Mjd_TT - AstroConst.MJD_J2000) / 36525.0;  // Julian cent. since J2000

        // Variables

        double L_0, l, lp, F, D, dL, S, h, N;
        double L, B, R, cosB;
        double[] r_Moon;

        // Mean elements of lunar orbit
        L_0 = MathUtils.frac(0.606433 + 1336.851344 * T);     // Mean longitude [rev]
        // w.r.t. J2000 equinox
        l = 2.0 * PI * MathUtils.frac(0.374897 + 1325.552410 * T);     // Moon's mean anomaly [rad]
        lp = 2.0 * PI * MathUtils.frac(0.993133 + 99.997361 * T);     // Sun's mean anomaly [rad]
        D = 2.0 * PI * MathUtils.frac(0.827361 + 1236.853086 * T);     // Diff. long. Moon-Sun [rad]
        F = 2.0 * PI * MathUtils.frac(0.259086 + 1342.227825 * T);     // Argument of latitude 


        // Ecliptic longitude (w.r.t. equinox of J2000)

        dL = +22640 * sin(l) - 4586 * sin(l - 2 * D) + 2370 * sin(2 * D) + 769 * sin(2 * l) - 668 * sin(lp) - 412 * sin(2 * F) - 212 * sin(2 * l - 2 * D) - 206 * sin(l + lp - 2 * D) + 192 * sin(l + 2 * D) - 165 * sin(lp - 2 * D) - 125 * sin(D) - 110 * sin(l + lp) + 148 * sin(l - lp) - 55 * sin(2 * F - 2 * D);

        L = 2.0 * PI * MathUtils.frac(L_0 + dL / 1296.0e3);  // [rad]

        // Ecliptic latitude

        S = F + (dL + 412 * sin(2 * F) + 541 * sin(lp)) / AstroConst.Arcs;
        h = F - 2 * D;
        N = -526 * sin(h) + 44 * sin(l + h) - 31 * sin(-l + h) - 23 * sin(lp + h) + 11 * sin(-lp + h) - 25 * sin(-2 * l + F) + 21 * sin(-l + F);

        B = (18520.0 * sin(S) + N) / AstroConst.Arcs;   // [rad]

        cosB = cos(B);

        // Distance [m]

        R = 385000e3 - 20905e3 * cos(l) - 3699e3 * cos(2 * D - l) - 2956e3 * cos(2 * D) - 570e3 * cos(2 * l) + 246e3 * cos(2 * l - 2 * D) - 205e3 * cos(lp - 2 * D) - 171e3 * cos(l + 2 * D) - 152e3 * cos(l + lp - 2 * D);

        // Equatorial coordinates
        double[] temp = {R * cos(L) * cosB, R * sin(L) * cosB, R * sin(B)};
        r_Moon = MathUtils.mult(MathUtils.R_x(-eps), temp);

        return r_Moon;

    } // MoonPosition
    
    
}

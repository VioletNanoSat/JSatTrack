/*
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

package jsattrak.objects;

import java.awt.Color;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

import name.gano.astro.AER;
import name.gano.astro.AstroConst;
import name.gano.astro.GeoFunctions;
import name.gano.astro.Sidereal;

/**
 *
 * @author sgano
 */
public class GroundStation implements Serializable
{
	private static final long serialVersionUID = -2927620100032405983L;
	// basic atributes
    private String stationName;
    private double[] lla_deg_m; // lat long and alt of station in deg/deg/meters (Geodetic)
    
    /** 360 deg averaged elevation constraint (in future might want to make this an array around site) **/
    private double elevationConst = 0; 
    
    // current time - julian date
    private double currentJulianDate = -1;
    
    // display settings
    private Color stationColor = Color.RED; //
    private boolean show2D = true;
    private boolean show2DName = true;
    private int groundStation2DPixelSize = 6;
    
    private boolean show3D = true;
    private boolean show3DName = true;
    
    // constructor
    public GroundStation(String name, double[] lla_deg_m, double currentJulianDate)
    {
        this.stationName = name;
        this.lla_deg_m = Arrays.copyOf(lla_deg_m, lla_deg_m.length);
        this.currentJulianDate = currentJulianDate;
        
        // pick random color
        Random generator = new Random();
        switch( generator.nextInt(6) )
        {
            case 0: stationColor = Color.red; break;
            case 1: stationColor = Color.blue; break;
            case 2: stationColor = Color.green; break;
            case 3: stationColor = Color.white; break;
            case 4: stationColor = Color.yellow; break;
            case 5: stationColor = Color.orange; break;
            default: stationColor = Color.red; break;
        } // random color switch
    } // constructor
    
    // ECI in meters - Uses Earth Flattening; WGS-84
    // theta is pass in as Degrees!! (it is the local mean sidereal time)
    private double[] calculateECIposition(double theta) {
        return GeoFunctions.calculateECIpositionGivenSidereal(getLla_deg_m(), theta);
    } //calculateECIposition
    
    /**
     * Calculate the ECI j2k position vector of the ground station at the current time
     * overloaded with no inputs -- calculates sidereal time for you
     */
    private double[] calculateECIposition()
    {
        return calculateECIposition(Sidereal.Mean_Sidereal_Deg(currentJulianDate-AstroConst.JDminusMJD, lla_deg_m[1]));
        
    } //calculateECIposition
    
    // transform ECI to topocentric-horizon system (SEZ) (south-East-Zenith)
    
    /**
     * Calculates the Azumuth, Elevation, and Range from Ground Station to another position
     * @param eci_pos ECI position of object in meters
     * @return Azumuth [deg], Elevation [deg], and Range vector [m]
     */
    public double[] calculate_AER(double[] eci_pos) {
        return AER.calculate(getLla_deg_m(), eci_pos, currentJulianDate );
    }
    
    /// get set methods ========================================

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public double[] getLla_deg_m() {
        return Arrays.copyOf(lla_deg_m, lla_deg_m.length);
    }

    public void setLla_deg_m(double[] lla_deg_m) {
        this.lla_deg_m = Arrays.copyOf(lla_deg_m, lla_deg_m.length);
    }

    public double getLatitude() {
        return lla_deg_m[0];
    }

    public double getLongitude() {
        return lla_deg_m[1];
    }

    public double getAltitude() {
        return lla_deg_m[2];
    }

    public double getElevationConst() {
        return elevationConst;
    }

    public void setElevationConst(double elevationConst) {
        this.elevationConst = elevationConst;
    }

    public Color getStationColor() {
        return stationColor;
    }

    public void setStationColor(Color satColor) {
        this.stationColor = satColor;
    }

    public boolean isShow2D() {
        return show2D;
    }

    public void setShow2D(boolean show2D) {
        this.show2D = show2D;
    }

    public boolean isShow2DName() {
        return show2DName;
    }

    public void setShow2DName(boolean show2DName) {
        this.show2DName = show2DName;
    }

    public int getGroundStation2DPixelSize() {
        return groundStation2DPixelSize;
    }

    public void setGroundStation2DPixelSize(int groundStation2DPixelSize) {
        this.groundStation2DPixelSize = groundStation2DPixelSize;
    }

    public boolean isShow3D() {
        return show3D;
    }

    public void setShow3D(boolean show3D) {
        this.show3D = show3D;
    }

    public boolean isShow3DName() {
        return show3DName;
    }

    public void setShow3DName(boolean show3DName) {
        this.show3DName = show3DName;
    }

    /** current time - julian date **/
    public double getCurrentJulianDate() {
        return currentJulianDate;
    }

    public void setCurrentJulianDate(double currentJulianDate) {
        this.currentJulianDate = currentJulianDate;
    }

    @Override
    public String toString() {
        return this.stationName;
    }
    
}

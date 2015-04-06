/*
 * Abstract Class for Satellite Types
 * =====================================================================
 * Copyright (C) 2008-9 Shawn E. Gano
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

import static java.util.Arrays.copyOf;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

import java.awt.Color;
import java.io.Serializable;
import java.util.Random;

import jsattrak.utilities.Arrays2d;
import jsattrak.utilities.TLE;
import name.gano.astro.AstroConst;
import name.gano.astro.Kepler;
import name.gano.worldwind.modelloader.WWModel3D_new;
import net.java.joglutils.model.ModelFactory;
import net.java.joglutils.model.ModelLoadException;

/**
 *
 * @author sgano
 */
public abstract class AbstractSatellite implements Serializable {
    
/************************************ Misc ************************************/
    /** The name of the satellite, returned by toString **/
    private final String name;

    public String getName() { return name; }
    
    @Override public String toString() { return getName().trim(); }
    
    protected AbstractSatellite(String name){ this.name = name; }
    
    private final double frequency = 437405000;
    
    public double getFrequency() { return frequency; }
    
/************************************ Time ************************************/
    
    /** current time - Julian date **/
    private double currentJulianDate = -1;
    public double currentJulDate(){ return currentJulianDate; }
    protected void setJulDate(double date){ currentJulianDate = date; }
        
    /** TLE epoch -- used to calculate how old the TLE is - Julian Date **/
    protected double tleEpochJD = -1; // no age
    
    public double getTleAgeDays() { return currentJulianDate - tleEpochJD; }
    
    /** 
     * TT or UTC?
     * @return
     */
    public abstract double getSatTleEpochJulDate();
    
    /** 
     * @return satellite's current period based on current pos/vel in Minutes
     */
    public double getPeriod() {
        if (j2kPos != null) { return Kepler.CalculatePeriod(
                AstroConst.GM_Earth, j2kPos, j2kVel) / (60F); }
        return 0;
    }
    
/********************************** Position ***********************************/

    /** J2000 position vector **/ 
    protected double[] j2kPos = new double[3];
    /** @return J2000 position vector **/ 
    public double[] getJ2000Position() { return copyOf(j2kPos, j2kPos.length); }

    /** J2000 velocity vector **/ 
    protected double[] j2kVel = new double[3];
    /** @return J2000 position vector **/ 
    public double[] getJ2000Velocity() { return copyOf(j2kVel, j2kVel.length); }

    /** MOD Mean of Date (or actually mean of Epoch Date) position **/
    protected double[] posTEME = new double[3];  // For LLA calcs
    /** @return MOD Mean of Date (or actually mean of Epoch Date) position **/
    public double[] getTEMEPos() { return copyOf(posTEME, posTEME.length); }
    
    /** MOD Mean of Date (or actually mean of Epoch Date) velocity **/
    protected double[] velTEME = new double[3];
    /** @return MOD Mean of Date (or actually mean of Epoch Date) velocity **/
    public double[] getTEMEVelocity() {
        return copyOf(velTEME, velTEME.length);
    }
    
    /** lat,long,alt  [radians, radians, km/m ?] **/
    protected double[] lla = new double[3];
    /** @return lat,long,alt  [radians, radians, km/m ?] **/
    public double[] getLLA(){ return copyOf(lla, lla.length); }
    
    public double getLatitude() {
        if (lla != null) return lla[0];
        return 180; // not possible latitude
    }

    public double getLongitude() {
        if (lla != null) return lla[1];
        return 270; // not possible long
    }

    public double getAltitude(){
        if (lla != null) return lla[2];
        return 0;
    }
    
    public double[] getKeplarianElements() {
        return Kepler.SingularOsculatingElements(AstroConst.GM_Earth, j2kPos,
                j2kVel);
    }

    public abstract void updateTleData(TLE newTLE);

    public abstract void propogate2JulDate(double julDate);

    /**
     * Calculate J2K position of this sat at a given JulDateTime (doesn't save
     * the time) - can be useful for event searches or optimization
     * 
     * @param julDate
     *            - Julian date
     * @return j2k position of satellite in meters
     */
    public abstract double[] calculateJ2KPositionFromUT(double julDate);

    /**
     * Calculate TEME of date position of this sat at a given JulDateTime (doesn't save
     * the time) - can be useful for event searches or optimization
     * 
     * @param julDate
     *            - julian date
     * @return j2k position of satellite in meters
     */
    public abstract double[] calculateTemePositionFromUT(double julDate);

    
/******************************** Ground Track ********************************/
    
     // ground track options  -- grounds tracks draw to ascending nodes, 
    // re-calculated at ascending nodes
    protected boolean showGroundTrack = true;
    protected int grnTrkPointsPerPeriod = 81; // equally space in time >=2 // used to be 121
    /** how far forward to draw ground track - in terms of periods **/
    protected double groundTrackLeadPeriodMultiplier = 2.0;  
    protected double groundTrackLagPeriodMultiplier = 1.0;  // how far behind to draw ground track - in terms of periods
    
    protected boolean groundTrackIni = false; // if ground track has been initialized
    
    /** Whether or not the ground track is shown by default **/
    public boolean isGroundTrackShown() {return showGroundTrack; }

    /** Sets the ground track to display by default **/
    public void showGroundTrack(){ initializeGroundTrack(); }
    
    /** Sets the ground track to not display by default **/
    public void hideGroundTrack(){
        groundTrackIni = false;
        latLongLead = null;
        latLongLag = null;
        temePosLag = null;
        temePosLead = null;
        timeLead = null;
        timeLag = null;
    }
    
    /**
     * initalize the ground track from any starting point, as long as Juldate
     * !=-1
     **/
    protected abstract void initializeGroundTrack();

    public void setGrnTrkPointsPerPeriod(final int grnTrkPointsPerPeriod) {
        this.grnTrkPointsPerPeriod = grnTrkPointsPerPeriod;
    }

    /** forces repaint of ground track next update **/
    public void repaintGroundTrack(){ groundTrackIni = false; }

    public int getGrnTrkPointsPerPeriod() { return grnTrkPointsPerPeriod; }

    public boolean getGroundTrackIni() { return groundTrackIni; } 

/************************************ Lead ************************************/
    /**  leading lat/long coordinates for ground track **/
    protected double[][] latLongLead; 
    /** leading Mean of date position coordinates for ground track **/
    protected double[][] temePosLead; 
    /** array for holding times associated with lead coordinates (Jul Date) **/
    protected double[]   timeLead; 
    
    /** Times associated with lead coordinates (Jul Date) **/
    public double[] getTimeLead() { return copyOf(timeLead, timeLead.length); }

    /** Leading Mean of date position coordinates for ground track **/
    public double[][] getTemePosLead() {
        return Arrays2d.copyOf(temePosLead, temePosLead.length);
    }
    
    public void setGroundTrackLeadPeriodMultiplier(
            final double groundTrackLeadPeriodMultiplier) {
        this.groundTrackLeadPeriodMultiplier = groundTrackLeadPeriodMultiplier;
    }
    
    public double getGroundTrackLeadPeriodMultiplier() {
        return groundTrackLeadPeriodMultiplier;
    }

    public double[] getGroundTrackLlaLeadPt(final int index) {
        return copyOf(latLongLead[index],latLongLead[index].length);
    }

    public double[] getGroundTrackXyzLeadPt(final int index) {
        return copyOf(getTemePosLead()[index],getTemePosLead()[index].length);
    }

    public int getNumGroundTrackLeadPts(){
        if (latLongLead != null) return latLongLead.length;
        return 0;
    }
    
/************************************ Lag *************************************/    
    
    /** lagging lat/long coordinates for ground track **/
    protected double[][] latLongLag;
    /** lagging Mean of date position coordinates for ground track **/
    protected double[][] temePosLag; 
    /** array - times associated with lag coordinates (Jul Date) **/
    protected double[]   timeLag; 

    /** array for holding times associated with lag coordinates (Jul Date) **/    
    public double[] getTimeLag() { return copyOf(timeLag, timeLag.length); }
    
    /** Lagging Mean of date position coordinates for ground track **/
    public double[][] getTemePosLag(){
        return Arrays2d.copyOf(temePosLag, temePosLag.length);
    }
    
    public void setGroundTrackLagPeriodMultiplier(
            final double groundTrackLagPeriodMultiplier) {
        this.groundTrackLagPeriodMultiplier = groundTrackLagPeriodMultiplier;
    }
    
    public double getGroundTrackLagPeriodMultiplier() {
        return groundTrackLagPeriodMultiplier;
    }
    
    public double[] getGroundTrackLlaLagPt(final int index){
        return copyOf(latLongLag[index],latLongLag[index].length); 
    }

    public double[] getGroundTrackXyzLagPt(final int index) {
        return copyOf(getTemePosLag()[index], getTemePosLag()[index].length);
    }
    
    public int getNumGroundTrackLagPts(){
        if (latLongLag != null) return latLongLag.length;
        return 0;
    }

/*********************************** Color ************************************/

    private Color satColor; {   // randomly pick color for satellite
        Random generator = new Random();
        switch( generator.nextInt(6) )
        {
            case 0: satColor = Color.red; break;
            case 1: satColor = Color.blue; break;
            case 2: satColor = Color.green; break;
            case 3: satColor = Color.white; break;
            case 4: satColor = Color.yellow; break;
            case 5: satColor = Color.orange; break;
            default: satColor = Color.red; break;
        } // random color switch
    }
    public Color getSatColor() { return satColor; } 
    public void setSatColor(final Color satColor) { this.satColor = satColor; }
    
/************************************* 2D *************************************/
    
    /** Whether or not to plot 2d **/
    private boolean plot2d = true;
    public boolean isPlot2D() { return plot2d; }
    public void setPlot2d(final boolean plot2d) { this.plot2d = plot2d; }

    /** show name in 2D plots **/
    protected boolean showName2D = true;
    public boolean isShowName2D(){ return showName2D; }
    public void setShowName2D(final boolean showName2D) {
        this.showName2D = showName2D;
    }
    
/********************************* Foot Print *********************************/
    
    /** Whether or not to plot foot print on 2d map **/
    private boolean plot2DFootPrint = true;
    public boolean isPlot2DFootPrint(){ return plot2DFootPrint; }
    public void setPlot2DFootPrint(final boolean plot2DFootPrint) {
        this.plot2DFootPrint = plot2DFootPrint;
    }
    
    private boolean fillFootPrint = true;
    public boolean isFillFootPrint() { return fillFootPrint; }
    public void setFillFootPrint(final boolean fill){ fillFootPrint = fill; }

    /** number of points in footprint, used to be 101 **/
    private int numPtsFootPrint = 41; 
    public int getNumPtsFootPrint(){ return numPtsFootPrint; }
    public void setNumPtsFootPrint(final int numPts){ numPtsFootPrint = numPts;}
    
/************************************* 3D *************************************/

    // 3D Options
    protected boolean show3DOrbitTrace = true;
    protected boolean show3DFootprint = true;
    protected boolean show3DName = true; // not implemented to change yet
    // not implemented to change yet, or to modify showing of sat
    protected boolean show3D = true; 
    protected boolean showGroundTrack3d = false;
    /** show orbit in ECI mode otherwise , ECEF **/
    protected boolean show3DOrbitTraceECI = true; 
    
    // 3D model parameters
    /** use custom 3D model (or default sphere) **/
    protected boolean use3dModel = false; 
    /** path to the custom model, default= globalstar/Globalstar.3ds ? **/
    protected String threeDModelPath = "globalstar/Globalstar.3ds"; 
    /** DO NOT STORE when saving -- need to reload this -- TOO MUCH DATA! **/
    protected transient WWModel3D_new threeDModel; 
    protected double threeDModelSizeFactor = 300000;
    
    public boolean isShow3D() { return show3D; }
    public void setShow3D(boolean show3D) { this.show3D = show3D; }

    public double getThreeDModelSizeFactor() { return threeDModelSizeFactor; }
    public void setThreeDModelSizeFactor(double modelSizeFactor) {
        threeDModelSizeFactor = modelSizeFactor;
    }

    public boolean isShow3DOrbitTraceECI() { return show3DOrbitTraceECI; }
    public void setShow3DOrbitTraceECI(boolean show3DOrbitTraceECI) {
        this.show3DOrbitTraceECI = show3DOrbitTraceECI;
    }
    
    public String get3DModelPath() { return threeDModelPath; }
    /** Relative path to the model -- relative from "user.dir"/data/models/ **/
    public void set3DModelPath(String path) {
        if (use3dModel && !(path.equalsIgnoreCase(threeDModelPath))) {
            // need to load the model
            loadNewModel(path);// "test/data/globalstar/Globalstar.3ds");
        }
        
        threeDModelPath = path; // save path no matter
    }

    public boolean use3dModel() { return use3dModel; }
    public void setUse3dModel(boolean use3dModel) {
        this.use3dModel = use3dModel;

        if (use3dModel && threeDModelPath.length() > 0) {
            // check that file exsists? - auto done in loader

            // String path = "data/models/globalstar/Globalstar.3ds";
            // String path = "data/models/isscomplete/iss_complete.3ds";

            loadNewModel(threeDModelPath);
        }
    }

    private void loadNewModel(String path) {
        String localPath = "data/models/"; // path to models root from user.dir

        try {
            net.java.joglutils.model.geometry.Model model3DS = ModelFactory
                    .createModel(localPath + path);
            // model3DS.setUseLighting(false); // turn off lighting!

            threeDModel = new WWModel3D_new(model3DS, new Position(Angle
                    .fromRadians(this.getLatitude()), Angle.fromRadians(this
                    .getLongitude()), this.getAltitude()));

            threeDModel.setMaitainConstantSize(true);
            threeDModel.setSize(threeDModelSizeFactor); // this needs to be a
                                                        // property!

            threeDModel.updateAttitude(this); // fixes attitude intitially

        } catch (ModelLoadException e) {
            System.out.println("ERROR LOADING 3D MODEL");
        }
    }
    
    public WWModel3D_new getThreeDModel() { return threeDModel; }

    public boolean isShow3DFootprint() { return show3DFootprint; }
    public void setShow3DFootprint(final boolean show) {
        this.show3DFootprint = show;
    }

    public boolean isShow3DName() { return show3DName; }
    public void setShow3DName(final boolean show) { this.show3DName = show; }

    public boolean isShow3DOrbitTrace() { return show3DOrbitTrace; }
    public void setShow3DOrbitTrace(final boolean show) {
        this.show3DOrbitTrace = show;
    }

    public boolean isShowGroundTrack3d() { return showGroundTrack3d; }
    public void setShowGroundTrack3D(final boolean show) { 
        showGroundTrack3d = show; 
    }
}

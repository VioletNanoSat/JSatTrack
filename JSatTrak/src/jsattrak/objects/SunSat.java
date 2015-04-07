package jsattrak.objects;

import java.awt.Color;

import jsattrak.utilities.TLE;
import name.gano.astro.AstroConst;
import name.gano.astro.bodies.Sun;
import name.gano.astro.time.Time;

public class SunSat extends AbstractSatellite {

/************************************ Misc ************************************/
    private Sun sun = new Sun(new Time().getMJDE());
    
    public SunSat() { 
        super("Sun"); 
        showGroundTrack = false;
        setSatColor(Color.yellow);
        timeLead = new double[0];
        timeLag = new double[0];
        temePosLead = new double[0][0];
        temePosLag = new double[0][0];
    }

/************************************ Time ************************************/
    
    @Override
    public double currentJulDate() {
        return sun.getCurrentMJD() + AstroConst.JDminusMJD;
    }
    
    @Override
    protected void setJulDate(double date) {
        sun.setCurrentMJD(date - AstroConst.JDminusMJD);
    }
    
    @Override public double getPeriod() { return 24F*60F; }

/********************************** Position ***********************************/
    
    @Override 
    public double[] getJ2000Position() { return sun.getCurrentPositionJ2K(); }
    
    @Override
    public double[] getJ2000Velocity() {
        throw new UnsupportedOperationException("J2000 Velocity");
    }
    
    @Override
    public double[] getTEMEPos() { return sun.getCurrentPositionTEME(); }
    
    @Override
    public double[] getTEMEVelocity() {
        throw new UnsupportedOperationException("TEME Velocity");
    }
    
    @Override
    public double[] getLLA(){ return sun.getCurrentLLA(); }
    
    @Override public double getLatitude() { return getLLA()[0]; }

    @Override public double getLongitude() { return getLLA()[1]; }

    @Override public double getAltitude() { return getLLA()[1]; }

    @Override public double[] getKeplarianElements() { return null; }
    
    @Override
    public void updateTleData(TLE mNewTLE) { }
    
    @Override
    public void propogate2JulDate(double mJulDate) {
        sun.setCurrentMJD(mJulDate - AstroConst.JDminusMJD);
    }

    @Override
    public double[] calculateJ2KPositionFromUT(double mJulDate) {
        return sun.sunPositionLowUT(mJulDate-AstroConst.JDminusMJD);
    }

    @Override
    public double[] calculateTemePositionFromUT(double mJulDate) {
        return new Sun(mJulDate - AstroConst.JDminusMJD)
                .getCurrentPositionTEME();
    }
    
/******************************** Ground Track ********************************/

    @Override
    public double getSatTleEpochJulDate() { return 0; }

    @Override
    protected void initializeGroundTrack() {
        // 2 days is fine
    }
    
}

package edu.cusat.gs.rotator;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import edu.cusat.common.Failure;

/**
 * A MockRotator behaves exactly as it is commanded, and is meant to be used to
 * test the controller
 */
public class MockRotator extends Rotator {

    /**
     * This Mock object instantly rotates to any angle
     */
    
    private boolean cAzIndep, cElIndep;

    /**
     * Create a mock object with the desired attributes.
     * 
     * @param mMaxAz
     *            maximum azimuth angle
     * @param mMaxEl
     *            maximum elevation angle
     * @param mAzIndep
     *            <code>true</code> if az can be set indep. of el
     * @param mElIndep
     *            <code>true</code> if el can be set indep. of az
     */
    public MockRotator(double mMaxAz, double mMaxEl, boolean mAzIndep,
            boolean mElIndep) {
        super(mMaxAz, mMaxEl);
        cAzIndep = mAzIndep;
        cElIndep = mElIndep;
    }

    @Override
    public double az(double az) throws UnsupportedOperationException {
        if (!cAzIndep) throw new UnsupportedOperationException();
        if (az < 0F || MAX_AZ < az)
            throw new IllegalArgumentException(String.format(
                    "Azimuth of %f is outside the allowed range"
                            + " of 0-%f degrees", az, MAX_AZ));
        state.az.set(az);
        return state.az.get();
    }

    @Override
    public double az() {
        return state.az.get();
    }

    @Override
    public double[] azEl(double az, double el) {
    	System.out.println("Set Az, El: " + Double.toString(az) + ", " + Double.toString(el));
        if (az < 0F || MAX_AZ < az)
            throw new IllegalArgumentException(String.format(
                    "Azimuth of %f is outside the allowed range"
                            + " of 0-%f degrees", az, MAX_AZ));
        if (el < 0F || MAX_EL < el)
            throw new IllegalArgumentException(String.format(
                    "Elevation of %f is outside the allowed range"
                            + " of 0-%f degrees", el, MAX_EL));
        state.az.set(az);
        state.el.set(el);
        return new double[] { state.az.get(), state.el.get() };
    }

    @Override
    public double[] azEl() {
        return new double[] { state.az.get(), state.el.get() };
    }

    @Override
    public double el(double el) throws UnsupportedOperationException {
        if (!cElIndep) throw new UnsupportedOperationException();
        if (el < 0F || MAX_EL < el)
            throw new IllegalArgumentException(String.format(
                    "Elevation of %f is outside the allowed range"
                            + " of 0-%f degrees", el, MAX_EL));
        state.el.set(el);
        return state.el.get();
    }

    @Override
    public double el() {
        return state.el.get();
    }

    @Override
    public boolean supportsAzSeperately() {
        return true;
    }

    @Override
    public boolean supportsElSeperately() {
        return true;
    }

    @Override
    public void connect(int baudRate) throws Failure {
        state.connected.set(true);
    }
    
    @Override
    public boolean isConnected() {
        return state.connected.get();
    }

    @Override
    public Rotator load(FileInputStream fos) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean save(FileOutputStream fos) {
        // TODO Auto-generated method stub
        return false;
    }

	
	
	
}
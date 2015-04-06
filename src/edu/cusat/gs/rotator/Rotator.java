package edu.cusat.gs.rotator;

import edu.cusat.common.Failure;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * A rotator that controls and physically changes the azimuth and elevation of
 * the antenna
 */
public abstract class Rotator {

    /**
     * Maximum azimuth achievable by the azimuth rotator. This is hardware
     * dependent.
     */
    public final double MAX_AZ;
    /**
     * Maximum elevation achievable by the azimuth rotator. This is hardware
     * dependent.
     */
    public final double MAX_EL;
    protected RotatorModel state;

    /**
     * Create a rotator
     */
    protected Rotator(double mMaxAz, double mMaxEl) {
        MAX_AZ = mMaxAz;
        MAX_EL = mMaxEl;
        state = new RotatorModel();

    }
    protected CommPortIdentifier portId;
    protected SerialPort port;

    /**
     * Connect to the physical rotator, if necessary using a specified baudrate
     */
    public void connect(int baudRate) throws Failure {
        if (portId == null) {
            throw new Failure("No port set");
        }
        try {
            port = (SerialPort) portId.open(getClass().getName(), 10000);
            port.setSerialPortParams(baudRate, SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        } catch (PortInUseException e) {
            throw new Failure(e);
        } catch (UnsupportedCommOperationException e) {
            throw new Failure(e);
        }
        if (!(this instanceof YaesuGs232B)) {
            state.connected.set(true);
        }
    }

    public void connect() throws Failure {
        connect(9600);
    }

    /**
     * Disconnect from the physical rotator, if necessary
     */
    public void disconnect() {
        if (port != null) {
            port.close();
        }
        state.connected.set(false);
    }

    /**
     * Test the connection with the rotator
     *
     * @return <code>true</code> if connected, <code>false</code> otherwise
     */
    public boolean isConnected() {
        return state.connected.get();
    }
    /**
     * Azimuth angle of true north in degrees
     */
    protected double north = 0F;

    /**
     * Returns the azimuth angle of true north (in degrees) of this rotator.
     * This will very by ground station.
     */
    public double north() {
        return north;
    }

    /**
     * Tells whether or not this Rotator supports control of azimuth
     * independently of elevation.
     *
     * @return <code>true</code> if, and only if, this rotator allows the
     *         azimuthal angle to be set without changing elevation angle.
     */
    public abstract boolean supportsAzSeperately();

    /**
     * Tells whether or not this Rotator supports control of elevation
     * independently of azimuth.
     *
     * @return <code>true</code> if, and only if, this rotator allows the
     *         elevation angle to be set without changing azimuthal angle.
     */
    public abstract boolean supportsElSeperately();

    /**
     * Command the azimuth rotator to achieve the given angle at the currently
     * set rate. If the rotator is currently moving towards some other commanded
     * angle, it will immediately move towards the new angle.
     * <p>
     * Azimuth is measured in degrees clockwise from the 0-point of the rotator
     *
     * @param mAz
     *            azimuth in degrees. <b>Requires:</b> 0 <= mAz <= MAX_AZ
     * @return measured azimuth angle immediately after the command is sent
     * @throws UnsupportedOperationException
     *             if the azimuth cannot be set without changing elevation
     */
    public abstract double az(double mAz) throws Failure,
            UnsupportedOperationException;

    /**
     * Measure the current azimuth angle of the rotator.
     * <p>
     * Azimuth is measured in degrees clockwise from the 0-point of the rotator
     *
     * @return measured azimuth angle
     */
    public abstract double az() throws Failure;

    /**
     * Command the elevation rotator to achieve the given angle at the currently
     * set rate. If the rotator is currently moving towards some other commanded
     * angle, it will immediately move towards the new angle.
     *
     * @param mEl
     *            elevation in degrees. <b>Requires:</b> 0 <= mEl <= MAX_EL
     * @return measured elevation angle immediately after the command is sent
     * @throws UnsupportedOperationException
     *             if the elevation cannot be set without changing azimuth
     */
    public abstract double el(double mEl) throws Failure,
            UnsupportedOperationException;

    /**
     * Measure the current elevation angle of the rotator.
     *
     * @return measured elevation angle
     */
    public abstract double el() throws Failure;

    /**
     * Command the azimuth and elevation rotors to acheive the given angles at
     * the currently set rate. If the rotator is currently moving towards some
     * other commanded angle, it will immediately move towards the new angle.
     *
     * @param mAz
     *            azimuth in degrees. <b>Requires:</b> 0 <= mAz <= MAX_AZ
     * @param mEl
     *            elevation in degrees. <b>Requires:</b> 0 <= mEl <= MAX_EL
     * @return measured azimuth and elevation angles immediately after the
     *         cmomand is sent. The array is <code>{az, el}</code>
     */
    public abstract double[] azEl(double mAz, double mEl) throws Failure;

    /**
     * Measure the current azimuth and elevation angles of the rotator.
     *
     * @return measured azimuth and elevation angles
     * @throws Failure
     */
    public abstract double[] azEl() throws Failure;

    public void addModelListener(RotatorModelListener l) {
        state.addModelListener(l);
    }

    /**
     * TODO
     *
     * @param fos
     * @return
     */
    public abstract boolean save(FileOutputStream fos);

    /**
     * TODO
     *
     * @param fos
     * @return
     */
    public abstract Rotator load(FileInputStream fos);

    public void setPort(CommPortIdentifier portIdentifier) {
        portId = portIdentifier;
    }
}

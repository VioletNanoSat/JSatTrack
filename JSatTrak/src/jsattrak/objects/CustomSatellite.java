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

import static java.util.Arrays.copyOf;
import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.ImageIcon;

import jsattrak.customsat.InitialConditionsNode;
import jsattrak.customsat.PropogatorNode;
import jsattrak.customsat.StopNode;
import jsattrak.utilities.StateVector;
import jsattrak.utilities.TLE;
import name.gano.astro.AstroConst;
import name.gano.astro.GeoFunctions;
import name.gano.astro.Kepler;
import name.gano.astro.coordinates.J2kCoordinateConversion;
import name.gano.astro.time.Time;
import name.gano.math.interpolation.LagrangeInterp;
import name.gano.swingx.treetable.CustomTreeTableNode;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

/**
 * NOTE !!!!!!!!  -- internal time for epehemeris is TT time all input times UTC
 * @author sgano
 */
public class CustomSatellite  extends AbstractSatellite {
	private static final long serialVersionUID = 5680926575050290993L;
	//====================================
	/** number of rows added at a time to improve speed of memory allocation **/
    private int ephemerisIncrement = 30; 
    /** internal ephemeris (Time store in TT) **/
    private Vector<StateVector> ephemeris = new Vector<StateVector>(ephemerisIncrement, ephemerisIncrement); // array to store ephemeris
    //====================================
    
    
    // table model for the custom config panel and holds all the mission Nodes 
    private transient DefaultTreeTableModel missionTableModel = new DefaultTreeTableModel(); // any TreeTableModel
    
    private boolean showConsoleOnPropogate = true;
    
    public CustomSatellite(String name, Time scenarioEpochDate)
    {
        super(name);
        iniMissionTableModel(scenarioEpochDate);
    }
    
    // Initializes the mission Table Model
    private void iniMissionTableModel(Time scenarioEpochDate)
    {
        // set names of columns
        Vector<String> tableHeaders = new Vector<String>();
        tableHeaders.add("Mission Objects");
//        tableHeaders.add("Time Start?");
//        tableHeaders.add("Time Stop?");
        
        missionTableModel.setColumnIdentifiers(tableHeaders);
        
        // Add root Node
        String[] str = new String[3];
        str[0] = getName();
        
        //DefaultMutableTreeTableNode ttn = new DefaultMutableTreeTableNode(str);
        CustomTreeTableNode rootNode = new CustomTreeTableNode(str);
        rootNode.setIcon( new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/custom/sat_icon.png")) ) );
        missionTableModel.setRoot(rootNode);
        
        // must add Initial conditions
        new InitialConditionsNode(rootNode, scenarioEpochDate);
        
        // by default also add a propogator node
        new PropogatorNode(rootNode);
       
        // ADD SOME NODES (example) -----
//        CustomTreeTableNode ttn2 = new PropogatorNode(rootNode);
//        ttn2.setValueAt("3 Jan 2008", 1); // example at setting a columns value
//        
//        ttn2 = new SolverNode(rootNode, true); // parent / add default children
//                        
//        ttn2 = new StopNode(rootNode);
//                
        // ------------------------------
        
        //must add stop node
        new StopNode(rootNode);
        
    }
    
    // ================================================================
    // functions that have to be fixed yet =========================
    // =================================================================
    
    // this function is basically given time update all current info and update lead/lag data if needed
    @Override public void propogate2JulDate(double julDate) {
        // save date
        setJulDate(julDate); // UTC

        double tempTime, maxTime, minTime;

        double currentMJDtime = julDate - AstroConst.JDminusMJD;

        // CAREFUL ON TIMES... TIME IN EPHMERIS IN TT NOT UTC!!

        double deltaTT2UTC = Time.deltaT(currentMJDtime); // = TT - UTC

        // find the nodes closest to the current time
        if (ephemeris.size() > 0) {
            // double epochkMJD = tleEpochJD - AstroConst.JDminusMJD;

            // in UTC
            minTime = ephemeris.get(0).state[0] - deltaTT2UTC;
            maxTime = ephemeris.get(ephemeris.size() - 1).state[0]
                    - deltaTT2UTC;

            // see if the current time in inside of the ephemeris range
            if (julDate <= maxTime && julDate >= minTime) {

                // / very simple search (slow!)
                StateVector tempState = ephemeris.elementAt(1); // first item
                tempTime = ephemeris.get(1).state[0] - deltaTT2UTC;

                int i = 1;
                // find where in the ephemeris to interpolat around

                while (tempTime < julDate) { // not <= causes out of bounds errors
                    i++;
                    tempState = ephemeris.get(i); // first item
                    tempTime = tempState.state[0] - deltaTT2UTC;
                }

                int i1, i2, i3; // indexes for interpolation
                if (i == 1) {// start case
                    i1 = 0;
                    i2 = 1;
                    i3 = 2;
                } else if (i == ephemeris.size() - 1) {// other end
                    i3 = ephemeris.size() - 1;
                    i2 = ephemeris.size() - 2;
                    i1 = ephemeris.size() - 3;
                } else {
                    i1 = i - 1;
                    i2 = i;
                    i3 = i + 1;
                }

                // X,Y,Z position
                tempState = ephemeris.get(i1); // first item
                double t1 = tempState.state[0];// /86400+epochkMJD;
                double x1 = tempState.state[1];
                double y1 = tempState.state[2];
                double z1 = tempState.state[3];
                tempState = ephemeris.get(i2);
                double t2 = tempState.state[0];// /86400+epochkMJD;
                double x2 = tempState.state[1];
                double y2 = tempState.state[2];
                double z2 = tempState.state[3];
                tempState = ephemeris.get(i3);
                double t3 = tempState.state[0];// /86400+epochkMJD;
                double x3 = tempState.state[1];
                double y3 = tempState.state[2];
                double z3 = tempState.state[3];

                double timeSecEpoch = julDate + deltaTT2UTC; // in TT

                if (j2kPos == null) {
                    j2kPos = new double[3];
                }

                j2kPos[0] = LagrangeInterp.lagrange3pt(timeSecEpoch, t1, x1,
                        t2, x2, t3, x3);
                j2kPos[1] = LagrangeInterp.lagrange3pt(timeSecEpoch, t1, y1,
                        t2, y2, t3, y3);
                j2kPos[2] = LagrangeInterp.lagrange3pt(timeSecEpoch, t1, z1,
                        t2, z2, t3, z3);

                // current j2K Velocity
                // X,Y,Z velocity
                tempState = ephemeris.get(i1); // first item
                t1 = tempState.state[0];// /86400+epochkMJD;
                x1 = tempState.state[4];
                y1 = tempState.state[5];
                z1 = tempState.state[6];
                tempState = ephemeris.get(i2);
                t2 = tempState.state[0];// /86400+epochkMJD;
                x2 = tempState.state[4];
                y2 = tempState.state[5];
                z2 = tempState.state[6];
                tempState = ephemeris.get(i3);
                t3 = tempState.state[0];// /86400+epochkMJD;
                x3 = tempState.state[4];
                y3 = tempState.state[5];
                z3 = tempState.state[6];

                if (j2kVel == null) {
                    j2kVel = new double[3];
                }

                j2kVel[0] = LagrangeInterp.lagrange3pt(timeSecEpoch, t1, x1,
                        t2, x2, t3, x3);
                j2kVel[1] = LagrangeInterp.lagrange3pt(timeSecEpoch, t1, y1,
                        t2, y2, t3, y3);
                j2kVel[2] = LagrangeInterp.lagrange3pt(timeSecEpoch, t1, z1,
                        t2, z2, t3, z3);

                // convert to LLA -- time in days since J2000
                //posMOD = CoordinateConversion.EquatorialEquinoxFromJ2K( currentMJDtime , j2kPos);
                //velMOD = CoordinateConversion.EquatorialEquinoxFromJ2K( currentMJDtime , j2kVel);
                // See SatelliteTleSGP$.java explination of MOD/TEME for calculating lat/long as TLE coordinate systems
                // revised calculations:
                double mjd = julDate-AstroConst.JDminusMJD;
                double ttt = (mjd-AstroConst.MJD_J2000) /36525.0;
                double[][] A = J2kCoordinateConversion.teme_j2k(J2kCoordinateConversion.Direction.from,ttt, 24, 2, 'a');
                // rotate position and velocity
                posTEME = J2kCoordinateConversion.matvecmult( A, j2kPos);
                velTEME = J2kCoordinateConversion.matvecmult( A, j2kVel);

                //System.out.println("Date: " + julDate +", MOD/TEME Pos: " + posMOD[0] + ", " + posMOD[1] + ", " + posMOD[2]);

                
                
                // save old lat/long for ascending node check
                double[] oldLLA = new double[3];
                if (lla != null) {
                    oldLLA = lla.clone(); // copy old LLA
                }

                // current LLA
                lla = GeoFunctions.geodeticLLA_mjd(posTEME, currentMJDtime);

                // Check to see if the ascending node has been passed
                if (showGroundTrack) { // update ground track needed 
                    if (!groundTrackIni || oldLLA == null) {
                        initializeGroundTrack();
                    } else if (oldLLA[0] < 0 && lla[0] >= 0) {
                        // check for ascending node pass
                        // System.out.println("Ascending NODE passed: " +
                        // tle.getSatName() );
                        initializeGroundTrack(); // for new ini each time

                    } // ascending node passed
                    // ELSE if current time is not in the lead/lag interval
                    // reinintialize it
                    else if (timeLead[timeLead.length - 1] < julDate
                            || timeLag[0] > julDate) {
                        initializeGroundTrack();
                    }

                } // if show ground track is true

                // isInTime = true;
                // System.out.println("true");

            } else { // not in the timeFrame
                // only set to null if they aren't already
                if (j2kPos != null) {
                    // set current arrays to null;
                    j2kPos = null;
                    posTEME = null;
                    lla = null;

                    // clear ground track
                    groundTrackIni = false;
                    latLongLead = null; // save some space
                    latLongLag = null; // sace some space
                    temePosLag = null;
                    temePosLead = null;
                    timeLead = null;
                    timeLag = null;
                }

                // isInTime = false;
                // System.out.println("false1");
            }
        } else {// no ephermeris
            // isInTime = false;
            // System.out.println("false2");
        }
    } // propogate2JulDate
    
    
     /**
     * Calculate MOD position of this sat at a given JulDateTime (doesn't save the time) - can be useful for event searches or optimization
     * @param julDate - julian date
     * @return j2k position of satellite in meters
     */
    @Override
    public double[] calculateTemePositionFromUT(double julDate)
    {      
        double[] j2kPosTemp = calculateJ2KPositionFromUT(julDate);
        double[] ptPos = new double[3];

        if(j2kPosTemp != null)
        {
                // convert to LLA -- time in days since J2000
                //ptPos = CoordinateConversion.EquatorialEquinoxFromJ2K( julDate - AstroConst.JDminusMJD , j2kPosTemp);
                // See SatelliteTleSGP$.java explination of MOD/TEME for calculating lat/long as TLE coordinate systems
                // revised calculations:
                double mjd = julDate-AstroConst.JDminusMJD;
                double ttt = (mjd-AstroConst.MJD_J2000) /36525.0;
                double[][] A = J2kCoordinateConversion.teme_j2k(J2kCoordinateConversion.Direction.from,ttt, 24, 2, 'a');
                // rotate position and velocity
                ptPos = J2kCoordinateConversion.matvecmult( A, j2kPosTemp);

        } // if in time and ephemeris is generated
        
        return ptPos;
        
    } // calculatePositionFromUT
    
    /**
     * Calculate J2K position of this sat at a given JulDateTime (doesn't save the time) - can be useful for event searches or optimization
     * @param julDate - julian date
     * @return j2k position of satellite in meters
     */
    @Override
    public double[] calculateJ2KPositionFromUT(double julDate)
    {
        double[] ptPos = new double[3];

        double tempTime, maxTime, minTime;

        // CAREFUL ON TIMES... TIME IN EPHMERIS IN TT NOT UTC!!  
        double deltaTT2UTC = Time.deltaT(julDate - AstroConst.JDminusMJD); // = TT - UTC


        // find the nodes closest to the current time
        if (ephemeris.size() > 0) //
        {
            //double epochkMJD = tleEpochJD? - AstroConst.JDminusMJD;

            // in UTC
            minTime = ephemeris.get(0).state[0] - deltaTT2UTC;
            maxTime = ephemeris.get(ephemeris.size() - 1).state[0] - deltaTT2UTC;

            // see if the current time in inside of the ephemeris range
            if (julDate <= maxTime && julDate >= minTime)
            {

                /// very simple search (slow!)
                StateVector tempState = ephemeris.elementAt(1); // first item
                tempTime = ephemeris.get(1).state[0] - deltaTT2UTC;

                int i = 1;
                // find where in the ephemeris to interpolat around

                while (tempTime < julDate) // not <= causes out of bounds errors
                {
                    i++;
                    tempState = ephemeris.get(i); // first item
                    tempTime = tempState.state[0] - deltaTT2UTC;
                }

                int i1, i2, i3; // indexes for interpolation
                if (i == 1) // start case
                {
                    i1 = 0;
                    i2 = 1;
                    i3 = 2;
                }
                else if (i == ephemeris.size() - 1) // other end
                {
                    i3 = ephemeris.size() - 1;
                    i2 = ephemeris.size() - 2;
                    i1 = ephemeris.size() - 3;
                }
                else
                {
                    i1 = i - 1;
                    i2 = i;
                    i3 = i + 1;
                }

                // X,Y,Z position
                tempState = ephemeris.get(i1); // first item
                double t1 = tempState.state[0];///86400+epochkMJD;
                double x1 = tempState.state[1];
                double y1 = tempState.state[2];
                double z1 = tempState.state[3];
                tempState = ephemeris.get(i2);
                double t2 = tempState.state[0];///86400+epochkMJD;
                double x2 = tempState.state[1];
                double y2 = tempState.state[2];
                double z2 = tempState.state[3];
                tempState = ephemeris.get(i3);
                double t3 = tempState.state[0];///86400+epochkMJD;
                double x3 = tempState.state[1];
                double y3 = tempState.state[2];
                double z3 = tempState.state[3];

                double timeSecEpoch = julDate + deltaTT2UTC; // in TT 

                // interpolate J2K position
                ptPos[0] = LagrangeInterp.lagrange3pt(timeSecEpoch, t1, x1, t2, x2, t3, x3);
                ptPos[1] = LagrangeInterp.lagrange3pt(timeSecEpoch, t1, y1, t2, y2, t3, y3);
                ptPos[2] = LagrangeInterp.lagrange3pt(timeSecEpoch, t1, z1, t2, z2, t3, z3);

            }
            else
            {
            // not in time
            }
        } // if epeheris contains anything


        return ptPos;

    } // calculatePositionFromUT
    
    protected void initializeGroundTrack()
    {
        //System.out.println("Ground Track Ini");
        
        if(currentJulDate() == -1)
        {
            // nothing to do yet, we haven't been given an initial time
            return;
        }
        
        // initial guess -- the current time  (UTC)        
        double lastAscendingNodeTime = currentJulDate(); // time of last ascending Node Time
        
        // calculate period - in minutes
        double periodMin = Kepler.CalculatePeriod(AstroConst.GM_Earth,j2kPos,j2kVel)/(60.0);
        //System.out.println("period [min] = "+periodMin);
        
//        // time step divisions (in fractions of a day)
//        double fracOfPeriod = 15.0;
//        double timeStep = (periodMin/(60.0*24.0)) / fracOfPeriod;
//        
//        // first next guess
//        double newGuess1 = lastAscendingNodeTime - timeStep;
//        
//        // latitude variables
//        double lat0 =  lla[0]; //  current latitude
//        double lat1 = (calculateLatLongAltXyz(newGuess1))[0]; // calculate latitude values       
//        
//        // bracket the crossing using timeStep step sizes
//        // make sure we are within time limits of the satellite!!
//        while( !( lat0>=0 && lat1<0 ) )
//        {
//            // move back a step
//            lastAscendingNodeTime = newGuess1;
//            lat0 = lat1;
//            
//            // next guess
//            newGuess1 = lastAscendingNodeTime - timeStep;
//            
//            // calculate latitudes of the new value
//            lat1 = (calculateLatLongAltXyz(newGuess1))[0];
//        } // while searching for ascending node
//        
//              
//        // secand method -- determine within a second!
//        // ONLY USE THIS IF a 0 is BRACKETED!! else just go to max
//        double outJul = secantMethod(lastAscendingNodeTime-timeStep, lastAscendingNodeTime, 1.0/(60.0*60.0*24.0), 20);
        
        //System.out.println("Guess 1:" + (lastAscendingNodeTime-timeStep) );
        //System.out.println("Guess 2:" + (lastAscendingNodeTime));
        //System.out.println("Answer: " + outJul);
        
        // update times: Trust Period Calculations for how far in the future and past to calculate out to
        // WARNING: period calculation is based on osculating elements may not be 100% accurate
        //          as this is just for graphical updates should be okay (no mid-course corrections assumed)
        
        //lastAscendingNodeTime = outJul;
        // assume last ascending node is now
        lastAscendingNodeTime = currentJulDate();
        
        double leadEndTime = lastAscendingNodeTime + groundTrackLeadPeriodMultiplier*periodMin/(60.0*24); // Julian Date for last lead point (furthest in future)
        double lagEndTime = lastAscendingNodeTime - groundTrackLagPeriodMultiplier*periodMin/(60.0*24); // Julian Date for the last lag point (furthest in past)
        
        // fill in lead/lag arrays
        fillGroundTrack(lastAscendingNodeTime,leadEndTime,lagEndTime);
        
        groundTrackIni = true;
        return;
        
    } // initializeGroundTrack
    
    
    // END -- functions to be fixed ===================
    
    // fill in the Ground Track given Jul Dates for 
    // 
    private void fillGroundTrack(double lastAscendingNodeTime, double leadEndTime, double lagEndTime)
    {
        // points in the lead direction
        int ptsLead = (int)Math.ceil(grnTrkPointsPerPeriod*groundTrackLeadPeriodMultiplier);
        latLongLead = new double[ptsLead][3];        
        temePosLead =  new double[ptsLead][3];
        timeLead = new double[ptsLead];
                
        for(int i=0;i<ptsLead;i++)
        {
            double ptTime = lastAscendingNodeTime + i*(leadEndTime-lastAscendingNodeTime)/(ptsLead-1);
            
            // make sure the time is in ephemeris -- TIMES IN ARE UTC epeheris time is TT
            // make that time correction
            double deltaTT2UTC = Time.deltaT(ptTime - AstroConst.JDminusMJD); // = TT - UTC
            if (ptTime >= ephemeris.firstElement().state[0] - deltaTT2UTC && ptTime <= ephemeris.lastElement().state[0] - deltaTT2UTC)
            {
                // PUT HERE calculate lat lon
                double[] ptLlaXyz = calculateLatLongAltXyz(ptTime);

                latLongLead[i][0] = ptLlaXyz[0]; // save lat
                latLongLead[i][1] = ptLlaXyz[1]; // save long
                latLongLead[i][2] = ptLlaXyz[2]; // save altitude

                temePosLead[i][0] = ptLlaXyz[3]; // x
                temePosLead[i][1] = ptLlaXyz[4]; // y
                temePosLead[i][2] = ptLlaXyz[5]; // z
            }
            else // give value of NaN - so it can be detected and not used
            {
                latLongLead[i][0] = Double.NaN; // save lat
                latLongLead[i][1] = Double.NaN; // save long
                latLongLead[i][2] = Double.NaN; // save altitude

                temePosLead[i][0] = Double.NaN; // x
                temePosLead[i][1] = Double.NaN; // y
                temePosLead[i][2] = Double.NaN; // z
            }
            
            timeLead[i] = ptTime; // save time
            
        } // for each lead point
        
        // points in the lag direction
        int ptsLag = (int)Math.ceil(grnTrkPointsPerPeriod*groundTrackLagPeriodMultiplier);
        latLongLag = new double[ptsLag][3];
        temePosLag = new double[ptsLag][3];
        timeLag = new double[ptsLag];
        
        for(int i=0;i<ptsLag;i++)
        {
            double ptTime = lastAscendingNodeTime + i * (lagEndTime - lastAscendingNodeTime) / (ptsLag - 1);

            // make sure the time is in ephemeris -- TIMES IN ARE UTC epeheris time is TT
            // make that time correction
            double deltaTT2UTC = Time.deltaT(ptTime - AstroConst.JDminusMJD); // = TT - UTC
            if (ptTime >= ephemeris.firstElement().state[0] - deltaTT2UTC && ptTime <= ephemeris.lastElement().state[0] - deltaTT2UTC)
            {

                double[] ptLlaXyz = calculateLatLongAltXyz(ptTime);

                latLongLag[i][0] = ptLlaXyz[0]; // save lat
                latLongLag[i][1] = ptLlaXyz[1]; // save long
                latLongLag[i][2] = ptLlaXyz[2]; // save alt

                temePosLag[i][0] = ptLlaXyz[3]; // x
                temePosLag[i][1] = ptLlaXyz[4]; // y
                temePosLag[i][2] = ptLlaXyz[5]; // z
            }
            else // give value of NaN - so it can be detected and not used
            {
                latLongLag[i][0] = Double.NaN; // save lat
                latLongLag[i][1] = Double.NaN; // save long
                latLongLag[i][2] = Double.NaN; // save alt

                temePosLag[i][0] = Double.NaN; // x
                temePosLag[i][1] = Double.NaN; // y
                temePosLag[i][2] = Double.NaN; // z
            }
            
            timeLag[i] = ptTime;
            
        } // for each lag point
    } // fillGroundTrack
    
    // takes in JulDate
    private double[] calculateLatLongAltXyz(double julDate) {
                
        double[] ptPos = calculateTemePositionFromUT(julDate);
  
        // get lat and long
        double[] ptLla = GeoFunctions.geodeticLLA_mjd(ptPos,julDate-AstroConst.JDminusMJD);
        
        double[] ptLlaXyz = new double[] {ptLla[0],ptLla[1],ptLla[2],ptPos[0],ptPos[1],ptPos[2]};
        
        return ptLlaXyz;
    } // calculateLatLongAlt
    
    // ==== empty functions to fulfill AbstractSatellite req ===========
    @Override public void updateTleData(TLE newTLE){}
    
    // other functions ================
    
    /**
     * SECANT Routines to find Crossings of the Equator (hopefully Ascending Nodes)
     * 
     * @param xn_1 date guess 1
     * @param xn date guess 2
     * @param tol convergence tolerance
     * @param maxIter maximum iterations allowed
     * @return julian date of crossing
     */
    private double secantMethod(double xn_1, double xn, double tol, int maxIter) {

		double d;

		// calculate functional values at guesses
		double fn_1 = this.calculateLatLongAltXyz(xn_1)[0];
		double fn = this.calculateLatLongAltXyz(xn)[0];

		for (int n = 1; n <= maxIter; n++) {
			d = (xn - xn_1) / (fn - fn_1) * fn;
			if (Math.abs(d) < tol) // convergence check
			{
				// System.out.println("Iters:"+n);
				return xn;
			}

			// save past point
			xn_1 = xn;
			fn_1 = fn;

			// new point
			xn = xn - d;
			fn = this.calculateLatLongAltXyz(xn)[0];
		}
        
        System.out.println("Warning: Secant Method - Max Iteration limit reached finding Asending Node.");
        
        return xn;
    } // secantMethod
    
    /**
     * array to store ephemeris
     * <p>
     * table model for the custom config panel and holds all the mission Nodes
     * 
     * @return
     */
    public DefaultTreeTableModel getMissionTableModel() {
        return missionTableModel;
    }

    public Vector<StateVector> getEphemeris() {
        return ephemeris;
    }
    
    /**
     * set ephemeris
     * @param e
     */
    public void setEphemeris(Vector<StateVector> e)
    {
        this.ephemeris = e;
        
//        // fill out all needed arrays (such as lead or lag etc) in MOD coordinates as needed
//        // latLongLead // lla
//        // modPosLead  // x/y/z
//        // timeLead; // array for holding times associated with lead coordinates (Jul Date) - UTC?
//        
//        int ephemerisSize = ephemeris.size();
//        
//        // create Lead (only for now -- all of ephemeris)
//        latLongLead = new double[ephemerisSize][3];
//        modPosLead = new double[ephemerisSize][3];
//        timeLead = new double[ephemerisSize];
//        
//        double[] currentJ2kPos = new double[3];
//        
//        for(int i=0;i<ephemerisSize;i++)
//        {
//            StateVector sv = ephemeris.elementAt(i);
//            
//            // get current j2k pos
//            currentJ2kPos[0] = sv.state[1];
//            currentJ2kPos[1] = sv.state[2];
//            currentJ2kPos[2] = sv.state[3];
//            
//            // save time
//            timeLead[i] = sv.state[0];
//            // mod pos
//            modPosLead[i] = CoordinateConversion.EquatorialEquinoxFromJ2K(sv.state[0] - AstroConst.JDminusMJD, currentJ2kPos);
//            // lla  (submit time in UTC)
//            double deltaTT2UTC = Time.deltaT(sv.state[0] - AstroConst.JDminusMJD); // = TT - UTC
//            latLongLead[i] = GeoFunctions.GeodeticLLA(modPosLead[i], sv.state[0] - AstroConst.JDminusMJD - deltaTT2UTC); // tt-UTC = deltaTT2UTC
//            
//        }
//        
//        groundTrackIni = true; // okay ground track has been ini01  
        
        
        
    } // set ephemeris

    public boolean isShowConsoleOnPropogate()
    {
        return showConsoleOnPropogate;
    }

    public void setShowConsoleOnPropogate(boolean showConsoleOnPropogate)
    {
        this.showConsoleOnPropogate = showConsoleOnPropogate;
    }
    
    // GET SET methods =================
    /**
	 * @returns TT time
	 */
    @Override public double getSatTleEpochJulDate() {
        if(ephemeris.size() > 0) {
            return ephemeris.firstElement().state[0]; 
        }
        return 0; 
    }
}

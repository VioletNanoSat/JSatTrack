/*
 * SatelliteProps.java
 *=====================================================================
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
 * Created on July 25, 2007, 3:14 PM
 *
 * Holds TLE and all satellite properties
 */

package jsattrak.objects;

import java.io.IOException;

import javax.swing.JOptionPane;

import jsattrak.utilities.TLE;
import name.gano.astro.AstroConst;
import name.gano.astro.GeoFunctions;
import name.gano.astro.Kepler;
import jsattrak.utilities.TLE;
import name.gano.astro.coordinates.J2kCoordinateConversion;
import name.gano.astro.propogators.sgp4_cssi.SGP4SatData;
import name.gano.astro.propogators.sgp4_cssi.SGP4unit;
import name.gano.astro.propogators.sgp4_cssi.SGP4utils;
import name.gano.worldwind.modelloader.WWModel3D_new;
import net.java.joglutils.model.ModelFactory;

/**
 * 
 *
 * @author ganos
 */
public class SatelliteTleSGP4 extends AbstractSatellite {

	private TLE tle;
    private SGP4SatData sgp4SatData; // sgp4 propogator data

    /**
     * Creates a new Satellite - properties taken from TLE
     * 
     * @param tle
     *            two line element
     * @throws Exception
     *             if TLE is bad
     */
    public SatelliteTleSGP4(TLE tle) throws Exception{
        this(tle.getSatName(), tle.getLine1(), tle.getLine2());
    }
    
    /**
     * Creates a new instance of SatelliteProps - default properties with given
     * name and TLE lines
     * 
     * @param name
     *            name of satellite
     * @param tleLine1
     *            first line of two line element
     * @param tleLine2
     *            second line of two line element
     * @throws Exception if TLE lines are bad
     */
    public SatelliteTleSGP4(String name, String tleLine1, String tleLine2) throws Exception {
        super(name);
        // create internal TLE object
        tle = new TLE(name,tleLine1,tleLine2);
        
        // initialize sgp4 propogator data for the satellite
        sgp4SatData = new SGP4SatData();
        
        // try to load TLE into propogator

        // options - hard coded
        char opsmode = SGP4utils.OPSMODE_IMPROVED; // OPSMODE_IMPROVED
        SGP4unit.Gravconsttype gravconsttype = SGP4unit.Gravconsttype.wgs72;

        // load TLE data as strings and INI all SGP4 data
        boolean loadSuccess = SGP4utils.readTLEandIniSGP4(name, tleLine1, tleLine2, opsmode, gravconsttype, sgp4SatData);

        // if there is an error loading send an exception
        if (!loadSuccess)
        {
            throw new Exception("Error loading TLE error code:" + sgp4SatData.error);
        }

        // calculate TLE age
        tleEpochJD = sgp4SatData.jdsatepoch;
    }
    
    @Override
    public void updateTleData(TLE newTLE)
    {
        this.tle = newTLE; // save new TLE
        
        // new spg4 object
        sgp4SatData = new SGP4SatData();
        
        // read TLE
        // options - hard coded
        char opsmode = SGP4utils.OPSMODE_IMPROVED; // OPSMODE_IMPROVED
        SGP4unit.Gravconsttype gravconsttype = SGP4unit.Gravconsttype.wgs72;

        // load TLE data as strings and INI all SGP4 data
        boolean loadSuccess = SGP4utils.readTLEandIniSGP4(tle.getSatName(), tle.getLine1(), tle.getLine2(), opsmode, gravconsttype, sgp4SatData);

        // if there is an error loading send an exception
        if (!loadSuccess)
        {
            JOptionPane.showMessageDialog(null,"Error reading updated TLE, error code:" + sgp4SatData.error + "\n Satellite: "+ tle.getSatName());
        }

        // calculate TLE age
        tleEpochJD = sgp4SatData.jdsatepoch;
               
        // ground track needs to be redone with new data
        groundTrackIni = false;
        
        //System.out.println("Updated " + tle.getSatName() );
    }
    
    @Override
    public void propogate2JulDate(double julDate)
    {
        // save date
        setJulDate(julDate);

        // using JulDate because function uses time diff between jultDate of ephemeris, SGP4 uses UTC
        // propogate satellite to given date - saves result in TEME to posTEME and velTEME in km, km/s
        boolean propSuccess = SGP4unit.sgp4Prop2JD(sgp4SatData, julDate, posTEME, velTEME);
        if(!propSuccess)
        {
            System.out.println("Error SGP4 Propagation failed for sat: " + sgp4SatData.name + ", JD: " + sgp4SatData.jdsatepoch + ", error code: "+ sgp4SatData.error);
        }

        // scale output to meters
        for(int i=0;i<3;i++)
        {
            // TEME
             posTEME[i] = posTEME[i]*1000.0;
             velTEME[i] = velTEME[i]*1000.0;
        }
        
        //print differene TT-UT
        //System.out.println("TT-UT [days]= " + SDP4TimeUtilities.DeltaT(julDate-2450000)*24.0*60*60);
        
        
        // SEG - 11 June 2009 -- new information (to me) on SGP4 propogator coordinate system:
        // SGP4 output is in true equator and mean equinox (TEME) of Date *** note some think of epoch, but STK beleives it is of date from tests **
        // It depends also on the source for the TLs if from the Nasa MCC might be MEME but most US Gov - TEME
        // Also the Lat/Lon/Alt calculations are based on TEME (of Date) so that is correct as it was used before!
        // References:
        // http://www.stk.com/pdf/STKandSGP4/STKandSGP4.pdf  (STK's stance on SGP4)
        // http://www.agi.com/resources/faqSystem/files/2144.pdf  (newer version of above)
        // http://www.satobs.org/seesat/Aug-2004/0111.html
        // http://celestrak.com/columns/v02n01/ "Orbital Coordinate Systems, Part I" by Dr. T.S. Kelso
        // http://en.wikipedia.org/wiki/Earth_Centered_Inertial
        // http://ccar.colorado.edu/asen5050/projects/projects_2004/aphanuphong/p1.html  (bad coefficients? conversion between TEME and J2000 (though slightly off?))
        //  http://www.centerforspace.com/downloads/files/pubs/AIAA-2000-4025.pdf
        // http://celestrak.com/software/vallado-sw.asp  (good software)

        double mjd = julDate-AstroConst.JDminusMJD;

        // get position information back out - convert to J2000 (does TT time need to be used? - no)
        //j2kPos = CoordinateConversion.EquatorialEquinoxToJ2K(mjd, sdp4Prop.itsR); //julDate-2400000.5
        //j2kVel = CoordinateConversion.EquatorialEquinoxToJ2K(mjd, sdp4Prop.itsV);
        // based on new info about coordinate system, to get the J2K other conversions are needed!
        // precession from rk5 -> mod
        double ttt = (mjd-AstroConst.MJD_J2000) /36525.0;
        double[][] A = J2kCoordinateConversion.teme_j2k(J2kCoordinateConversion.Direction.to,ttt, 24, 2, 'a');
        // rotate position and velocity
        j2kPos = J2kCoordinateConversion.matvecmult( A, posTEME);
        j2kVel = J2kCoordinateConversion.matvecmult( A, velTEME);

        //System.out.println("Date: " + julDate +", Pos: " + sdp4Prop.itsR[0] + ", " + sdp4Prop.itsR[1] + ", " + sdp4Prop.itsR[2]);

        // save old lat/long for ascending node check
        double[] oldLLA = lla.clone(); // copy old LLA
        
        // calculate Lat,Long,Alt - must use Mean of Date (MOD) Position
        lla = GeoFunctions.geodeticLLA_mjd(posTEME,julDate-AstroConst.JDminusMJD); // j2kPos
        
        // Check to see if the ascending node has been passed
        if(showGroundTrack)
        {
            if( !groundTrackIni ) // update ground track needed
            {
                initializeGroundTrack();
            }
            else if( oldLLA[0] < 0 && lla[0] >=0) // check for ascending node pass
            {
                //System.out.println("Ascending NODE passed: " + tle.getSatName() );
                initializeGroundTrack(); // for new ini each time
                
            } // ascending node passed
            
        } // if show ground track is true
        
        // if 3D model - update its properties -- NOT DONE HERE - done in OrbitModelRenderable (so it can be done for any sat)
               
    } // propogate2JulDate
    
    @Override protected void initializeGroundTrack()
    {
    	// nothing to do yet, we haven't been given an initial time
        if(currentJulDate() == -1) { return; }
        
        // find time of last acending node crossing
        
        // initial guess -- the current time        
        double lastAscendingNodeTime = currentJulDate(); // time of last ascending Node Time
        
        // calculate period - in minutes
        double periodMin = Kepler.CalculatePeriod(AstroConst.GM_Earth,j2kPos,j2kVel)/(60.0);
        //System.out.println("period [min] = "+periodMin);
        
        // time step divisions (in fractions of a day)
        double fracOfPeriod = 15.0;
        double timeStep = (periodMin/(60.0*24.0)) / fracOfPeriod;
        
        // first next guess
        double newGuess1 = lastAscendingNodeTime - timeStep;
        
        // latitude variables
        double lat0 =  lla[0]; //  current latitude
        double lat1 = (calculateLatLongAltXyz(newGuess1))[0]; // calculate latitude values       
        
        // bracket the crossing using timeStep step sizes
        while( !( lat0>=0 && lat1<0 ) )
        {
            // move back a step
            lastAscendingNodeTime = newGuess1;
            lat0 = lat1;
            
            // next guess
            newGuess1 = lastAscendingNodeTime - timeStep;
            
            // calculate latitudes of the new value
            lat1 = (calculateLatLongAltXyz(newGuess1))[0];
        } // while searching for ascending node
        
              
        // secand method -- determine within a second!
        double outJul = secantMethod(lastAscendingNodeTime-timeStep, lastAscendingNodeTime, 1.0/(60.0*60.0*24.0), 20);
        //System.out.println("Guess 1:" + (lastAscendingNodeTime-timeStep) );
        //System.out.println("Guess 2:" + (lastAscendingNodeTime));
        //System.out.println("Answer: " + outJul);
        
        // update times: Trust Period Calculations for how far in the future and past to calculate out to
        // WARNING: period calculation is based on osculating elements may not be 100% accurate
        //          as this is just for graphical updates should be okay (no mid-course corrections assumed)
        lastAscendingNodeTime = outJul;
        double leadEndTime = lastAscendingNodeTime + groundTrackLeadPeriodMultiplier*periodMin/(60.0*24); // Julian Date for last lead point (furthest in future)
        double lagEndTime = lastAscendingNodeTime - groundTrackLagPeriodMultiplier*periodMin/(60.0*24); // Julian Date for the last lag point (furthest in past)
        
        // fill in lead/lag arrays
        fillGroundTrack(lastAscendingNodeTime,leadEndTime,lagEndTime);
        
        groundTrackIni = true;
        return;
        
    } // initializeGroundTrack
    
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
            
           // PUT HERE calculate lat lon
            double[] ptLlaXyz = calculateLatLongAltXyz(ptTime);
            
            latLongLead[i][0] = ptLlaXyz[0]; // save lat
            latLongLead[i][1] = ptLlaXyz[1]; // save long
            latLongLead[i][2] = ptLlaXyz[2]; // save altitude
            
            temePosLead[i][0] = ptLlaXyz[3]; // x
            temePosLead[i][1] = ptLlaXyz[4]; // y
            temePosLead[i][2] = ptLlaXyz[5]; // z
            
            timeLead[i] = ptTime; // save time
            
        } // for each lead point
        
        // points in the lag direction
        int ptsLag = (int)Math.ceil(grnTrkPointsPerPeriod*groundTrackLagPeriodMultiplier);
        latLongLag = new double[ptsLag][3];
        temePosLag = new double[ptsLag][3];
        timeLag = new double[ptsLag];
        
        for(int i=0;i<ptsLag;i++)
        {
            double ptTime = lastAscendingNodeTime + i*(lagEndTime-lastAscendingNodeTime)/(ptsLag-1);
            
            double[] ptLlaXyz = calculateLatLongAltXyz(ptTime);
             
            latLongLag[i][0] = ptLlaXyz[0]; // save lat
            latLongLag[i][1] = ptLlaXyz[1]; // save long
            latLongLag[i][2] = ptLlaXyz[2]; // save alt
            
            temePosLag[i][0] = ptLlaXyz[3]; // x
            temePosLag[i][1] = ptLlaXyz[4]; // y
            temePosLag[i][2] = ptLlaXyz[5]; // z
            
            timeLag[i] = ptTime;
            
        } // for each lag point
    } // fillGroundTrack
    
    // takes in JulDate, returns lla and teme position
    private double[] calculateLatLongAltXyz(double ptTime)
    {
        double[] ptPos = calculateTemePositionFromUT(ptTime);
        
        // get lat and long
        double[] ptLla = GeoFunctions.geodeticLLA_mjd(ptPos,ptTime-AstroConst.JDminusMJD);
        
        double[] ptLlaXyz = new double[] {ptLla[0],ptLla[1],ptLla[2],ptPos[0],ptPos[1],ptPos[2]};
        
        return ptLlaXyz;
    } // calculateLatLongAlt
    
    // 
    
    /**
     * Calculate J2K position of this sat at a given JulDateTime (doesn't save the time) - can be useful for event searches or optimization
     * @param julDate - julian date
     * @return j2k position of satellite in meters
     */
    @Override public double[] calculateJ2KPositionFromUT(double julDate) {
        double[] ptPos = calculateTemePositionFromUT(julDate);

        double mjd = julDate-AstroConst.JDminusMJD;

        // get position information back out - convert to J2000
        // precession from rk5 -> mod
        double ttt = (mjd-AstroConst.MJD_J2000) /36525.0;
        double[][] A = J2kCoordinateConversion.teme_j2k(J2kCoordinateConversion.Direction.to,ttt, 24, 2, 'a');
        // rotate position
        double[] j2kPosI = J2kCoordinateConversion.matvecmult( A, ptPos);
        
        return j2kPosI;
        
    } // calculatePositionFromUT
    
    /**
     * Calculate true-equator, mean equinox (TEME) of date position of this sat at a given JulDateTime (doesn't save the time) - can be useful for event searches or optimization
     * @param julDate - julian date
     * @return j2k position of satellite in meters
     */
    @Override
    public double[] calculateTemePositionFromUT(double julDate)
    {
        double[] ptPos = new double[3];
        double[] ptVel = new double[3];

        // using JulDate because function uses time diff between jultDate of ephemeris, SGP4 uses UTC
        // propogate satellite to given date - saves result in TEME to posTEME and velTEME in km, km/s
        boolean propSuccess = SGP4unit.sgp4Prop2JD(sgp4SatData, julDate, ptPos, ptVel);
        if(!propSuccess)
        {
            System.out.println("Error (2) SGP4 Propagation failed for sat: " + sgp4SatData.name + ", JD: " + sgp4SatData.jdsatepoch + ", error code: "+ sgp4SatData.error);
        }

        // scale output to meters
        for(int i=0;i<3;i++)
        {
            // TEME
             ptPos[i] = ptPos[i]*1000.0;
        }
        
        return ptPos;
        
    } // calculatePositionFromUT
    
    
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
		double fn_1 = latitudeGivenJulianDate(xn_1);
		double fn = latitudeGivenJulianDate(xn);

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
			fn = latitudeGivenJulianDate(xn);
		}

		System.out.println("Warning: Secant Method - Max Iteration limit " +
				"reached finding Asending Node.");

		return xn;
	} // secantMethod
    
    private double latitudeGivenJulianDate(double julDate)
    {
        // computer latitude of the spacecraft at a given date
        double[] ptPos = calculateTemePositionFromUT(julDate);
        
        // get lat and long
        double[] ptLla = GeoFunctions.geodeticLLA_mjd(ptPos,julDate-AstroConst.JDminusMJD);
        
        return ptLla[0]; // pass back latitude
        
    } // latitudeGivenJulianDate

    // GET SET methods =================
    // TT or UTC? = UTC
    @Override public double getSatTleEpochJulDate() {
        // this is how the dates are defined in SDP4
        return sgp4SatData.jdsatepoch;
    }
    
}

/*
 * JTrackingPanel.java
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
 *
 * Created on December 16, 2007, 6:38 PM
 */

package jsattrak.gui;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.toDegrees;
import static name.gano.astro.MathUtils.cross;
import static name.gano.astro.MathUtils.dot;
import static name.gano.astro.MathUtils.norm;

import java.awt.event.ItemEvent;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.JTable.PrintMode;
import javax.swing.table.DefaultTableModel;

import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.GroundStation;
import jsattrak.objects.SunSat;
import jsattrak.utilities.CustomFileFilter;
import jsattrak.utilities.GsListener;
import jsattrak.utilities.SatListener;
import name.gano.astro.AER;
import name.gano.astro.AstroConst;
import name.gano.astro.GeoFunctions;
import name.gano.astro.bodies.Sun;
import name.gano.astro.coordinates.CoordinateConversion;
import name.gano.astro.time.Time;
import name.gano.file.FileUtilities;

import org.joda.time.DateTime;

import edu.cusat.common.Failure;
import edu.cusat.gs.doppler.Doppler;
import edu.cusat.gs.rotator.GroundPass;
import edu.cusat.gs.rotator.RotatorController;

/**
 * TODO MAKE GSHASH FIRE OFF A PROPERTYCHANGED
 * @author Shawn
 */
public class JTrackingPanel extends javax.swing.JPanel implements GsListener, SatListener {

    private static final String CURRENT_TRACKING_INFORMATION = "Current Tracking Information: ";
    private static final String ROT_EL_LBL_TXT = "Elevation (Rotator) [deg]: ";
    private static final String ROT_AZ_LBL_TXT = "Azimuth (Rotator) [deg]: ";
    private static final String RANGE_LBL_TXT = "Range [m]: ";
    private static final String EL_LBL_TXT = "Elevation (Satellite) [deg]: ";
    private static final String AZ_LBL_TXT = "Azimuth (Satellite) [deg]: ";
    private static final String TIME_LBL_TXT = "Current Time: ";
    private static final String NEXT_PASS_LBL_TXT = "Next Pass at: ";
    // data to tell if Lead/Lag data should be updated
    private double oldLeadX = -1;
    private double oldLagX = -1;

    private String timeAsString;

    // current time object - passed for use in pass predictions
    private DateTime currentTime;

    /** table **/
    private DefaultTableModel passTableModel;

    /** app **/
    private JSatTrak app;

    /**
     * hash table to store pass num and midpoint times -- used to setting time
     * in app
     */
    private Hashtable<Integer, Double> passHash = new Hashtable<Integer, Double>();

    RotatorController rController;
    private GroundStation currentGs;
    private AbstractSatellite currentSat;

    private JSatTrak parent;

    /** Upcoming ground passes **/ 
    private List<GroundPass> futurePasses = new ArrayList<GroundPass>(0);
    
    /** <code>null</code> if there is no satellite overhead **/
    private GroundPass currentPass;
    
    private Doppler doppler;

    /**
     * Creates new form JTrackingPanel
     * @param app
     * @param timeAsStringIn
     * @param currentDate
     */
    public JTrackingPanel(JSatTrak app,String timeAsStringIn,
            Time currentDate, JSatTrak parent) {

        this.parent = parent;
        
        this.currentTime = currentDate.toDateTime();

        this.app = app;
        
        initComponents();

        // fill out choice boxes
        refreshComboBoxes();
        
        this.app.registerGsListener(this);
        this.app.registerSatListener(this);

        // do this after components INI
        this.timeAsString = timeAsStringIn;
        updateTime(timeAsString);

        // update pass table
        passTableModel = new DefaultTableModel();
        passTable.setModel(passTableModel);

        passTableModel.addColumn("#"); // pass number
        passTableModel.addColumn("Rise Time"); //
        passTableModel.addColumn("Rise Az."); // new
        passTableModel.addColumn("Set Time"); //
        passTableModel.addColumn("Set Az."); // new
        passTableModel.addColumn("Duration [Sec]"); //
        passTableModel.addColumn("Visibility"); //

        // passTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        passTable
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // passTable.setAutoCreateRowSorter(true);

        // add row highliters to passTable --oops not a swingx table
        // passTable.addHighlighter(new
        // ColorHighlighter(HighlightPredicate.EVEN, Color.WHITE, Color.black));
        // // even, background, foregrond
        // passTable.addHighlighter(new ColorHighlighter(HighlightPredicate.ODD,
        // new Color(229, 229, 229), Color.black)); // odd, background,
        // foregrond
    }

    public void refreshComboBoxes() {
        gsComboBox.removeAllItems();
        for (GroundStation gs : app.groundStations())
            gsComboBox.addItem(gs.getStationName());

        satComboBox.removeAllItems();
        for (AbstractSatellite sat : app.satellites())
            satComboBox.addItem(sat.getName());
    } // refreshComboBoxes

    /**
     * update time called .. update info in box in future might not want to
     * search hash each iteration for gs and sat objects
     *
     * @param timeAsString
     */
    public void updateTime(String timeAsString) {
        this.timeAsString = timeAsString;
        Time parse = app.currentJulianDate.parse(timeAsString);
        currentTime = parse.toDateTime();
        double mjd2 = Time.calcMjd(currentTime.toGregorianCalendar());
        updateTime();
    }
        
    /**
     * Requires that currentTime be set
     */
   private void updateTime() {
        // if something's not set, erase all data
        if (gsComboBox.getSelectedIndex() < 0
                || satComboBox.getSelectedIndex() < 0) {
            clearCurrTrackInfo();
            return;
        }
        
        if (currentSat.getTEMEPos() != null
                && !Double.isNaN(currentSat.getTEMEPos()[0])) {
            double[] aer = currentGs.calculate_AER(currentSat.getTEMEPos()); // TEME
            
            // add text AER and string
             timeLbl.setText( TIME_LBL_TXT + timeAsString);
               azLbl.setText(String.format("%s %.3f", AZ_LBL_TXT, aer[0]));
               elLbl.setText(String.format("%s %.3f", EL_LBL_TXT, aer[1]));
            rangeLbl.setText(String.format("%s %.3f", RANGE_LBL_TXT, aer[2]));

            // update polar plot
            polarPlotLbl.update(timeAsString, aer, currentGs
                    .getElevationConst(), currentGs.getStationName(),
                    currentSat.getName());

            // check to see if we need to update Lead/Lag data
            // no good J2000 positions not saved through time right now in
            // satprops
            // see if we even need to bother - lead data option selected in both
            // 2D and polar plot
            if (polarPlotLbl.isShowLeadLagData()
                    && currentSat.isGroundTrackShown()) {
                boolean updateLeadData = false;
                boolean updateLagData = false;

                // need to update lead data?
                if (polarPlotLbl.getAerLead() == null
                        || polarPlotLbl.getAerLead().length < 1) {
                    updateLeadData = true;
                    oldLeadX = currentSat.getTemePosLead()[0][0];
                } else if (oldLeadX != currentSat.getTemePosLead()[0][0]) {
                    updateLeadData = true;
                    oldLeadX = currentSat.getTemePosLead()[0][0];
                }

                // need to update lag data?
                if (polarPlotLbl.getAerLag() == null
                        || polarPlotLbl.getAerLag().length < 1) {
                    updateLagData = true;
                    oldLagX = currentSat.getTemePosLag()[0][0];
                } else if (oldLagX != currentSat.getTemePosLag()[0][0]) {
                        updateLagData = true;
                        oldLagX = currentSat.getTemePosLag()[0][0];
                }

                // update Lead data if needed
                if (updateLeadData) {
                    double[][] leadData = AER.calculate(currentGs
                            .getLla_deg_m(), currentSat.getTemePosLead(),
                            currentSat.getTimeLead());
                    polarPlotLbl.setAerLead(leadData);
                    futurePasses = GroundPass.find(currentGs, currentSat);
                    // System.out.println("Lead updated");
                }

                // update lag data if needed
                if (updateLagData) {
                    polarPlotLbl.setAerLag(AER.calculate(currentGs
                            .getLla_deg_m(), currentSat
                            .getTemePosLag(), currentSat.getTimeLag()));
                    // System.out.println("Lag updated");
                }
                
             // Figure out when the next pass is
                if (currentPass != null) // There's a pass, but it ended
                    if (Time.dateTimeOfJd(currentPass.setTime()).isBefore(currentTime)) {
                        currentPass = null;
                        nextPassLbl.setText("");
                    }
                // There's no pass now, but we can predict the next one
                if (futurePasses.size() > 0 && currentPass == null) {
                    DateTime nextRise = Time.dateTimeOfJd(futurePasses.get(0).riseTime());
                    if(nextRise.isAfter(currentTime)){
                        nextPassLbl.setText(NEXT_PASS_LBL_TXT
                                + nextRise.toString("MMM dd, YYYY -- HH:mm:ss"));
                    } else {// It just started
                        currentPass = futurePasses.remove(0);
                        nextPassLbl.setText("Pass ends at: "
                                + new DateTime(Time.dateTimeOfJd(currentPass
                                        .setTime()))
                                        .toString("MMM dd, YYYY -- HH:mm:ss"));
                    }
                } 
            } // lead / lag data shown

            // Track a satellite
            if (trackButton.isSelected()) {
                try {
                    trackSatellite((int) Math.round(aer[0]), (int) Math
                            .round(aer[1]), aer[2]);
                } catch (Failure f) { // TODO Display failure
                    String stackTrace = "";
                    for (StackTraceElement ste : f.getStackTrace())
                        stackTrace += ste.toString();
                    JOptionPane.showMessageDialog(this, "Error: "
                            + f.getMessage() + "\n" + stackTrace, "Error",
                            JOptionPane.ERROR_MESSAGE);
                    trackButton.setSelected(false);
                }
            }
            
            // TODO Adjust for Doppler here
            // put in new range and time
            // adjust TS2000 settings accordingly
            
        } else { // NAN check
            currInfoLbl.setText(CURRENT_TRACKING_INFORMATION + 
                    "Satellite Ephemeris Not Available");
            clearCurrTrackInfo();
            
            polarPlotLbl.setTimeString(timeAsString);
            polarPlotLbl.setGs2SatNameString(String.format("%s to %s",
                    currentGs.getStationName().trim(),
                    currentSat.getName().trim()));

            polarPlotLbl.clearLeadLagData();         // clear lead/lag data
            polarPlotLbl.resetCurrentPosition();        // clear current point?
        }

        polarPlotLbl.repaint();

    } // updateTime

    /**
     * @param az azimuth
     * @param el elevation
     * @param range
     * @throws Failure
     */
    private void trackSatellite(int az, int el, double range) throws Failure {
        if(currentPass != null){                   // have a ground pass?
            if(currentPass.requiresFlip()){        // invert all directions?
                az = (az < 180) ? az + 180 : az - 180;
                el = 180 - el;
            } else if (currentPass.crossesNorth() && az < 180) { 
                az += 360; // have to go past 360 to ensure continuous coverage
            }
        } else if (currentSat instanceof SunSat) { 
            // Lead/lag data not implemented in SunSat yet, so GroundPass
            // doesn't work either
        } else { // No pass, prepare for the next one?
            el = 0; // Can't point below horizon
            if(futurePasses.size() > 0){ // Get ready
                if (futurePasses.get(0).requiresFlip()){
                    az -= 180;
                    el = 180 - el;
                } 
            } 
        }
        double[] azEl = rController.rotate(az, el);
        rotAzLbl.setText(ROT_AZ_LBL_TXT + azEl[0]);
        rotElLbl.setText(ROT_EL_LBL_TXT + azEl[1]);
    }

    /**
     * 
     */
    private void clearCurrTrackInfo() {
        timeLbl.setText(TIME_LBL_TXT);
        azLbl.setText(AZ_LBL_TXT);
        elLbl.setText(EL_LBL_TXT);
        rangeLbl.setText(RANGE_LBL_TXT);
    }
   
    private void gsComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_gsComboBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED)
            currentGs = app.getGs(gsComboBox.getSelectedItem().toString());
        if (timeAsString != null)
            updateTime(timeAsString);
        objectChangeReset();
    }//GEN-LAST:event_gsComboBoxItemStateChanged

    private void satComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_satComboBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED)
            currentSat = app.getSatellite(satComboBox.getSelectedItem().toString());
        if (timeAsString != null)
            updateTime(timeAsString);
        objectChangeReset();
        
    }//GEN-LAST:event_satComboBoxItemStateChanged

    private void refreshComboBoxesButtonActionPerformed(
            java.awt.event.ActionEvent evt)// GEN-FIRST:event_refreshComboBoxesButtonActionPerformed
    {// GEN-HEADEREND:event_refreshComboBoxesButtonActionPerformed
        // save selections (if there are any)
        String oldSat = "null";
        if (satComboBox.getSelectedIndex() >= 0) {
            oldSat = satComboBox.getSelectedItem().toString();
        }
        String oldGS = "null";
        if (gsComboBox.getSelectedIndex() >= 0) {
            oldGS = gsComboBox.getSelectedItem().toString();
        }

        refreshComboBoxes();

        // select old selections if there are any
        if (!oldSat.equalsIgnoreCase("null")) {
            satComboBox.setSelectedItem(oldSat);
        }

        if (!oldGS.equalsIgnoreCase("null")) {
            gsComboBox.setSelectedItem(oldGS);
        }

    }// GEN-LAST:event_refreshComboBoxesButtonActionPerformed
 /*clearCurrTrackInfo();
        if (timeAsString != null)
            updateTime(timeAsString);

        objectChangeReset();*/
    private void gsComboBoxActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_gsComboBoxActionPerformed
    {// GEN-HEADEREND:event_gsComboBoxActionPerformed
        clearCurrTrackInfo();
        if (timeAsString != null)
            updateTime(timeAsString);

        objectChangeReset();

    }// GEN-LAST:event_gsComboBoxActionPerformed

    private void satComboBoxActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_satComboBoxActionPerformed
    {// GEN-HEADEREND:event_satComboBoxActionPerformed
        clearCurrTrackInfo();
        if (timeAsString != null)
            updateTime(timeAsString);

        objectChangeReset();
    }// GEN-LAST:event_satComboBoxActionPerformed

    /**
     * functions for use for scripting purposes, sets Ground Station
     */
    public void setGroundStation(String gsName) {
        //gsComboBox.setSelectedItem(gsName);
        setGroundStation(app.getGs(gsName));
    }

    /**
     * functions for use for scripting purposes, sets Ground Station
     */
    public void setGroundStation(GroundStation gs) {
        currentGs = gs;
        if (timeAsString != null)
            updateTime(timeAsString);
        objectChangeReset();

        //setGroundStation(gs.getStationName());
        //currentGs = gs;
    }

    /**
     * functions for use for scripting purposes, sets Satellite
     */
    public void setSatellite(String satName) {
        //satComboBox.setSelectedItem(satName);
        setSatellite(app.getSatellite(satName));
    }

    /**
     * functions for use for scripting purposes, sets Satellite
     */
    public void setSatellite(AbstractSatellite sat) {
        //setSatellite(sat.getName());
        currentSat = sat;
        if (timeAsString != null)
            updateTime(timeAsString);
        objectChangeReset();
    }

    /**
     * GUI changes needed when objects are changed
     */
    private void objectChangeReset() {

        // clear pass table
        if (passTableModel != null) // ini of object
        {
            while (passTableModel.getRowCount() > 0) {
                passTableModel.removeRow(0);
            }

            // refresh lead/lag in polar plot if needed
            if (gsComboBox.getSelectedIndex() < 0
                    || satComboBox.getSelectedIndex() < 0) {
                return; // something not selected skip lead/lag update
            }

            if (polarPlotLbl.isShowLeadLagData() && currentSat.isGroundTrackShown()) {
                polarPlotLbl.setAerLead(AER.calculate(
                        new double[] { currentGs.getLatitude(), currentGs.getLongitude(),
                                currentGs.getAltitude() }, currentSat.getTemePosLead(), currentSat
                                .getTimeLead()));
                polarPlotLbl.setAerLag(AER.calculate(
                        new double[] { currentGs.getLatitude(), currentGs.getLongitude(),
                                currentGs.getAltitude() }, currentSat.getTemePosLag(), currentSat
                                .getTimeLag()));
            }
        }

    }

    private void leadLagCheckBoxActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_leadLagCheckBoxActionPerformed
    {// GEN-HEADEREND:event_leadLagCheckBoxActionPerformed
        polarPlotLbl.setShowLeadLagData(leadLagCheckBox.isSelected());
        polarPlotLbl.repaint();
    }// GEN-LAST:event_leadLagCheckBoxActionPerformed

    private void namesChkBxActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jCheckBox2ActionPerformed
    {// GEN-HEADEREND:event_jCheckBox2ActionPerformed
        polarPlotLbl.setDisplayNames(namesChkBx.isSelected());
        polarPlotLbl.repaint();
    }// GEN-LAST:event_jCheckBox2ActionPerformed

    private void runPassPredictionButtonActionPerformed(
            java.awt.event.ActionEvent evt)// GEN-FIRST:event_runPassPredictionButtonActionPerformed
    {// GEN-HEADEREND:event_runPassPredictionButtonActionPerformed
        runPassPrediction();
    }// GEN-LAST:event_runPassPredictionButtonActionPerformed

    public void runPassPrediction() {
        // get info:
        double timeSpanDays = Double.parseDouble(timeSpanTextField.getText());
        double timeStepSec = Double.parseDouble(timeStepTextField.getText());
        // boolean onlyVisible = visibleOnlyCheckBox.isSelected();

        // get sat and GS
        GroundStation gs = app.getGs(gsComboBox.getSelectedItem().toString());
        AbstractSatellite sat = app.getSatellite(satComboBox.getSelectedItem()
                .toString());

        // start time Jul Date
        double jdStart = new Time(currentTime).getJulianDate();

        // clear hash
        passHash.clear();

        // clear the table
        while (passTableModel.getRowCount() > 0) {
            passTableModel.removeRow(0);
        }

        //quadInterp(timeSpanDays, timeStepSec, gs, sat, jdStart);

        // new sun for internal calculations of Visibility
        Sun internalSun = new Sun(jdStart - AstroConst.JDminusMJD);

        // linear search
        double time0, h0;
        double time1 = jdStart;
        double h1 = AER.calculate(gs.getLla_deg_m(), sat
                .calculateTemePositionFromUT(time1), time1)[1]
                - gs.getElevationConst();

        int passCount = 0;

        if (h1 > 0) {
            passCount++;
            passTableModel.addRow(new Object[] { passCount, "--", "", "", "" });
        }

        double lastRise = 0;

        for (double jd = jdStart; jd <= jdStart + timeSpanDays; jd += timeStepSec
                / (60.0 * 60.0 * 24.0)) {
            time0 = time1;
            time1 = jd + timeStepSec / (60.0 * 60.0 * 24.0);

            // calculate elevations at each time step (if needed)
            h0 = h1;
            // calculate the elevation at this newly visited point
            h1 = AER.calculate(gs.getLla_deg_m(), sat
                    .calculateTemePositionFromUT(time1), time1)[1]
                    - gs.getElevationConst();

            // rise
            if (h0 <= 0 && h1 > 0) {
                double riseTime = findSatRiseSetRoot(sat, gs, time0, time1, h0,
                        h1);
                // System.out.println("Rise at " + riseTime + " (" + time0 + ","
                // + time1 + ")");

                lastRise = riseTime; // save

                // add to table
                passCount++;
                // use Time object to convert Julian date to string using
                // program settings (i.e. time zone)
                String crossTimeStr = app.currentJulianDate
                        .convertJD2String(riseTime);

                passTableModel.addRow(new Object[] { passCount, crossTimeStr,
                        "", "", "", "", "" });

                // calculate using the rise time - the Azimuth
                double az = AER.calculate(gs.getLla_deg_m(), sat
                        .calculateTemePositionFromUT(riseTime), riseTime)[0];
                if (azComboBox.getSelectedIndex() == 0) {
                    passTableModel.setValueAt("" + String.format("%.1f", az),
                            passTableModel.getRowCount() - 1, 2);
                } else {
                    // is AZ in degrees or radians?
                    passTableModel.setValueAt(CoordinateConversion
                            .degrees2CompassPoints(az), passTableModel
                            .getRowCount() - 1, 2);
                }
            }

            // set
            if (h1 <= 0 && h0 > 0) {
                double setTime = findSatRiseSetRoot(sat, gs, time0, time1, h0,
                        h1);
                // System.out.println("Set at " + setTime + " (" + time0 + "," +
                // time1 + ")");

                // add to table
                String crossTimeStr = app.currentJulianDate
                        .convertJD2String(setTime);
                passTableModel.setValueAt(crossTimeStr, passTableModel
                        .getRowCount() - 1, 3); // last row, 3rd column (2)

                // calculate using the set time - the Azimuth
                double az = AER.calculate(gs.getLla_deg_m(), sat
                        .calculateTemePositionFromUT(setTime), setTime)[0];
                if (azComboBox.getSelectedIndex() == 0) {
                    passTableModel.setValueAt("" + String.format("%.1f", az),
                            passTableModel.getRowCount() - 1, 4);
                } else {
                    passTableModel.setValueAt(CoordinateConversion
                            .degrees2CompassPoints(az), passTableModel
                            .getRowCount() - 1, 4);
                } // azimuth

                // add duration
                if (lastRise > 0) {
                    DecimalFormat fmt2Dig = new DecimalFormat("00.000");

                    double duration = (setTime - lastRise) * 24.0 * 60.0 * 60.0; // seconds
                    String durStr = fmt2Dig.format(duration);
                    passTableModel.setValueAt(durStr, passTableModel
                            .getRowCount() - 1, 5); // last row, 4rd column (3)
                }

                if (lastRise > 0) {
                    determineVisibility(gs, sat, internalSun, passCount,
                            lastRise, setTime);
                } // visibility (with last rise)
            } // set
        }// linear search

        // if visible only checked remove other items from the list
        if (visibleOnlyCheckBox.isSelected()) {

            int vizColumn = 6; // vis text column
            for (int i = passTableModel.getRowCount() - 1; i >= 0; i--) {

                if (!passTableModel.getValueAt(i, vizColumn).toString()
                        .equalsIgnoreCase("Visible")) {
                    passTableModel.removeRow(i);
                }
            }
        } // remove non-visible

        // set first col to be small
        passTable.getColumnModel().getColumn(0).setPreferredWidth(10);
    } // runPassPrediction
/*
    /**
     * @param timeSpanDays
     * @param timeStepSec
     * @param gs
     * @param sat
     * @param jdStart
     *//*
    private void quadInterp(double timeSpanDays, double timeStepSec,
            GroundStation gs, AbstractSatellite sat, double jdStart) {
        // elevation data used in search
        double h0=0,h1=0,h2=0;
        
        // initially calculate elevations at 0/1 time points
        AER.calculate(gs.getLla_deg_m(), sat.getTemePosLead(), sat.getTimeLead());
        double time0 = jdStart - timeStepSec/(60.0*60.0*24.0);
        h0 = AER.calculate(gs.getLla_deg_m(),
        sat.calculateTEMEPositionFromUT(time0) , time0)[1];
        double time1 = jdStart;
        h1 = AER.calculate(gs.getLla_deg_m(),
        sat.calculateTEMEPositionFromUT(time1) , time1)[1];
        double time2 = jdStart + timeStepSec/(60.0*60.0*24.0);; // declare var
        
        System.out.println(time0 + "," + h0 );
        System.out.println(time1 + "," + h1 );
        // use quadratic fit to search for zeros
        for(double jd = jdStart; jd <= jdStart + timeSpanDays; jd += timeStepSec/(60.0*60.0*24.0)) {
            time0 = time1;
            time1 = time2;
            time2 = jd + timeStepSec/(60.0*60.0*24.0);
            
            // calculate elevations at each time step (if needed)
            h0 = h1;
            h1 = h2;
            // calculate the elevation at this newly visited point
            h2 = AER.calculate(gs.getLla_deg_m(),
            sat.calculateTEMEPositionFromUT(time2) , time2)[1];
            System.out.println(time2 + "," + h2 );
            
            // create a quadratic interpolator
            QuadraticInterpolatorSimp qis = new QuadraticInterpolatorSimp(time0,h0, time1, h1, time2, h2);
            
             // if(qis.getRootCountInDomain() == 1)
             // {
             // System.out.println("Event(1): " + qis.getLowerRoot() );
             // }else if(qis.getRootCountInDomain() == 2)
             // {
             // System.out.println("Event(2a): " + qis.getLowerRoot() );
             // System.out.println("Event(2b): " + qis.getUpperRoot() );
             // }
            
        } // for loop seaching for rise/set events
    }
*/
    private void determineVisibility(GroundStation gs, AbstractSatellite sat,
            Sun internalSun, int passCount, double lastRise, double setTime) {
        // Visiable, Radar Night, Radar Night

        // use the time 1/2 between rise and set for viz
        // calculations
        // DOES NOT CHECK FOR VIS NEAR END POINTS SO COULD MISS SOME
        // PARTIAL PASS VISIBILITY

        double julDateVizCalc = (setTime - lastRise) / 2.0 + lastRise;

        // SAVE to hash - for use later
        passHash.put(Integer.valueOf(passCount), new Double(julDateVizCalc));

        // twilight offset
        // 7 seems good
        // 6 is used by heavens-above.com
        double twilightOffset = 6; // degrees extra required for darkness

        // set the suns time
        internalSun.setCurrentMJD(julDateVizCalc - AstroConst.JDminusMJD);

        // TEME - sun dot site positions to determine if station is
        // in sunlight
        double[] gsECI = GeoFunctions.calculateECIposition(gs
                .getLla_deg_m(), julDateVizCalc);
        double sunDotSite = dot(internalSun.getCurrentPositionTEME(), gsECI);

        // TEST - find angle between sun -> center of Earth ->
        // Ground Station
        double sinFinalSigmaGS = norm(cross(
                internalSun.getCurrentPositionTEME(), gsECI))
                / (norm(internalSun.getCurrentPositionTEME()) * norm(gsECI));
        double finalSigmaGS = toDegrees(asin(sinFinalSigmaGS));

        if (sunDotSite > 0 || (90.0 - finalSigmaGS) < twilightOffset) {
            // last row, 5rd column (4)
            passTableModel.setValueAt("Radar Sun",
                    passTableModel.getRowCount() - 1, 6);
        } // sun light
        else {
            // now we know the site is in darkness - need to figure
            // out if the satelite is in light
            // use predict algorithm from Vallado 2nd ed.
            double[] satTEME = sat.calculateTemePositionFromUT(julDateVizCalc);
            double sinFinalSigma = norm(cross(internalSun
                    .getCurrentPositionTEME(), satTEME))
                    / (norm(internalSun.getCurrentPositionTEME()) * norm(satTEME));
            double finalSigma = asin(sinFinalSigma);
            double dist = norm(satTEME) * cos(finalSigma - PI / 2.0);

            // changed to mean 30/March/2009 SEG
            if (dist > AstroConst.R_Earth_mean) {
                // sat is in sunlight!
                // last row, 5th column (4)
                passTableModel.setValueAt("Visible", passTableModel
                        .getRowCount() - 1, 6);
            } else { // Radar Night (both in darkness)
                // last row, 5th column (4)
                passTableModel.setValueAt("Radar Night", passTableModel
                        .getRowCount() - 1, 6);
            }

        } // site in dark
    }

    private void go2passButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_go2passButtonActionPerformed
    {// GEN-HEADEREND:event_go2passButtonActionPerformed
        // get selected row in table
        int tableRow = passTable.getSelectedRow();
        go2pass(tableRow);
    }// GEN-LAST:event_go2passButtonActionPerformed

    /**
     * Prints the pass prediction table, using a print dialog
     *
     * @see http://java.sun.com/docs/books/tutorial/uiswing/misc/printtable.html
     */
    private void printTableButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_printTableButtonActionPerformed
    {// GEN-HEADEREND:event_printTableButtonActionPerformed
        try {
            MessageFormat header = new MessageFormat(String.format("%s to %s",
                    gsComboBox.getSelectedItem().toString(), satComboBox
                            .getSelectedItem().toString()));

            boolean complete = passTable.print(PrintMode.FIT_WIDTH, header,
                    new MessageFormat("JSatTrak Pass Predictions  - {0} -"),
                    true, null, false, null);
            if (!complete)
                System.err.println("Printing cancelled");
        } catch (PrinterException e) {
            System.err.println("ERROR Printing Pass Table: " + e.toString());
        }
    }// GEN-LAST:event_printTableButtonActionPerformed

    /**
     * Save the pass prediction table to a CSV
     */
    private void saveTableButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_saveTableButtonActionPerformed
    {// GEN-HEADEREND:event_saveTableButtonActionPerformed

        // first ask for a file name:
        final JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new CustomFileFilter("csv", "*.csv"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            // append the extension if necessary
            if (FileUtilities.getExtension(file) == null) {
                file = new File(file.getAbsolutePath() + ".csv");
            }

            // save data to file : fileSaveAs ------------------
            try {
                BufferedWriter buffOut = new BufferedWriter(
                        new FileWriter(file));

                // write the name of each column heading
                for (int i = 0; i < passTableModel.getColumnCount(); i++) {
                    String val = passTableModel.getColumnName(i);
                    if (i != passTableModel.getColumnCount() - 1) {
                        val += ","; // add comma to all but last element
                    }
                    buffOut.write(val);
                }
                buffOut.write("\n"); // add new line before data

                // loop through all the data
                for (int i = 0; i < passTableModel.getRowCount(); i++) {
                    for (int j = 0; j < passTableModel.getColumnCount(); j++) {
                        String val = passTableModel.getValueAt(i, j).toString();

                        if (j != passTableModel.getColumnCount() - 1) {
                            val += ","; // add comma to all but last element
                        }
                        buffOut.write(val);
                    } // for each column
                    buffOut.write("\n");
                } // for each row
                buffOut.close(); // close file
            } catch (IOException e) {
                System.err.println("Unable to open/close file");
            }
        } // if approve
    }// GEN-LAST:event_saveTableButtonActionPerformed

    private void timeChkBxActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jCheckBox1ActionPerformed
    {// GEN-HEADEREND:event_jCheckBox1ActionPerformed
        polarPlotLbl.setDisplayTime(timeChkBx.isSelected());
        polarPlotLbl.repaint();
    }// GEN-LAST:event_jCheckBox1ActionPerformed

    private void invChkBxActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jCheckBox3ActionPerformed
    {// GEN-HEADEREND:event_jCheckBox3ActionPerformed
        polarPlotLbl.setDarkColors(!invChkBx.isSelected());
        // jPolarPlotLabel.repaint();
    }// GEN-LAST:event_jCheckBox3ActionPerformed

    private void horizLimChkBxActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jCheckBox4ActionPerformed
    {// GEN-HEADEREND:event_jCheckBox4ActionPerformed
        polarPlotLbl.setLimit2Horizon(horizLimChkBx.isSelected());
    }// GEN-LAST:event_jCheckBox4ActionPerformed

    private void printPolarPlotBtnActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton1ActionPerformed
    {// GEN-HEADEREND:event_jButton1ActionPerformed
        polarPlotLbl.print();
    }// GEN-LAST:event_jButton1ActionPerformed

    private void compassChkBxActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jCheckBox5ActionPerformed
    {// GEN-HEADEREND:event_jCheckBox5ActionPerformed
        polarPlotLbl.setUseCompassPoints(compassChkBx.isSelected());
    }// GEN-LAST:event_jCheckBox5ActionPerformed

    /**
     * Open a window to set rotator setitngs Obtain the rotator, and use the
     * RotatorFactory to get the result
     */
    private void rotatorSetBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_rotatorSetBtnActionPerformed
        loadRotator(null);
    }// GEN-LAST:event_rotatorSetBtnActionPerformed

    /**
     * 
     * @param rc
     */
    private void loadRotator(RotatorController rc) {
        if(rc == null){
            RotatorSettingsPanel rsPanel = new RotatorSettingsPanel(this); // non-modal
            parent.addInternalFrame("Rotator Settings", rsPanel, null,true);
        } else rController = rc;
        
    }

    public javax.swing.JPanel loadRotatorSettingsPanel(RotatorController rc,boolean visibility) {
        RotatorSettingsPanel rsPanel = null;
        if(rc == null){
            rsPanel = new RotatorSettingsPanel(this);
            parent.addInternalFrame("Rotator Settings", rsPanel, null,visibility);
        } else rController = rc;
        return (javax.swing.JPanel)rsPanel;
    }

    /**
     * Jumps time to the mid point of a pass
     *
     * @param passNumber
     *            pass number from last solution, range: 0 to N-1
     */
    public void go2pass(int passNumber) {

        if (passNumber >= 0) {
            // get # in that row
            int passNum = Integer.valueOf(passTableModel.getValueAt(passNumber,
                    0).toString());

            if (passHash.containsKey(passNum)) {
                double jdMidPoint = passHash.get(passNum);

                // get current app time
                double daysDiff = abs(new Time(currentTime).getJulianDate()
                        - jdMidPoint);

                // check to see if lead/lag data needs updating
                app.checkTimeDiffResetGroundTracks(daysDiff);

                // set app to new time
                app.setTime(Time.convertJD2Calendar(jdMidPoint)
                        .getTimeInMillis());
            }
        }
    } // go2pass

    /**
     * bisection method, crossing time should be bracketed by time0 and time1
     *
     * @param sat
     * @param gs
     * @param time0
     * @param time1
     * @param f0
     * @param f1
     * @return
     */
    private double findSatRiseSetRoot(AbstractSatellite sat, GroundStation gs,
            double time0, double time1, double f0, double f1) {
        double tol = (1.157407E-5) / 4; // 1/4 a sec (in units of a day)

        int iterCount = 0;

        while (abs(time1 - time0) > 2 * tol) {
            // Calculate midpoint of domain
            double timeMid = (time1 + time0) / 2.0;
            double fmid = AER.calculate(gs.getLla_deg_m(), sat
                    .calculateTemePositionFromUT(timeMid), timeMid)[1]
                    - gs.getElevationConst();

            if (f0 * fmid > 0) // same sign
            {
                // replace f0 with fmid
                f0 = fmid;
                time0 = timeMid;

            } else // else replace f1 with fmid
            {
                f1 = fmid;
                time1 = timeMid;
            }

            iterCount++;
        } // while not in tolerance

        // return best gues using linear interpolation between last two points
        double a = (f1 - f0) / (time1 - time0);
        double b = f1 - a * time1;
        double riseTime = -b / a;

        // System.out.println("Bisection Iters: " + iterCount);

        return riseTime; // return best guess -typically: (time0 + time1)/2.0;
        // return (time0 + time1)/2.0;

    } // findSatRiseSetRoot

    public void setTimeSpanDays(double days) {
        timeSpanTextField.setText("" + days);
    }

    public void setTimeStepSec(double sec) {
        timeStepTextField.setText("" + sec);
    }

    public void setVisibleOnlyFilter(boolean visibleOnly) {
        visibleOnlyCheckBox.setSelected(visibleOnly);
    }

    public Hashtable<Integer, Double> getPassHash() {
        return passHash;
    }

    public String[][] getPredictionTableData() {
        String[][] data = new String[passTableModel.getRowCount()][passTableModel
                .getColumnCount()];

        for (int r = 0; r < passTableModel.getRowCount(); r++) {
            for (int c = 0; c < passTableModel.getColumnCount(); c++) {
                data[r][c] = passTableModel.getValueAt(r, c).toString();
            }
        }

        return data;
    } // getPredictionTableData

    public String[] getTrackingInformation()
    {
        String[] ar = {azLbl.getText(),elLbl.getText(),rangeLbl.getText(),rotAzLbl.getText(),rotElLbl.getText()};
        return ar;
    }

    /**
     * can be used in a script to have the lable rendered off screen or to set
     * its display settings
     *
     * @return
     */
    public JPolarPlotLabel getPolarPlotLabel() {
        return polarPlotLbl;
    }
    
    @Override
    public void gsAdded(GroundStation gs) {
        gsComboBox.addItem(gs.getStationName());
    }

    @Override
    public void gsRemoved(GroundStation gs) {
        gsComboBox.removeItem(gs);
    }
    
    @Override
    public void satAdded(AbstractSatellite sat) {
        satComboBox.addItem(sat);
    }

    @Override
    public void satRemoved(AbstractSatellite sat) {
        satComboBox.removeItem(sat);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox azComboBox;
    private javax.swing.JLabel azLbl;
    private javax.swing.JPanel basicPanel;
    private javax.swing.JCheckBox compassChkBx;
    private javax.swing.JLabel currInfoLbl;
    private javax.swing.JLabel elLbl;
    private javax.swing.JButton go2passButton;
    private javax.swing.JComboBox<String> gsComboBox;
    private javax.swing.JLabel gsLbl;
    private javax.swing.JCheckBox horizLimChkBx;
    private javax.swing.JCheckBox invChkBx;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JCheckBox leadLagCheckBox;
    private javax.swing.JCheckBox namesChkBx;
    private javax.swing.JLabel nextPassLbl;
    private javax.swing.JLabel passPredictLbl;
    private javax.swing.JPanel passPredictPanel;
    private javax.swing.JScrollPane passScrollPane;
    private javax.swing.JTable passTable;
    private jsattrak.gui.JPolarPlotLabel polarPlotLbl;
    private javax.swing.JPanel polarPlotPanel;
    private javax.swing.JToolBar polarPlotToolBar;
    private javax.swing.JButton printPolarPlotBtn;
    private javax.swing.JButton printTableButton;
    private javax.swing.JLabel rangeLbl;
    private javax.swing.JButton refreshComboBoxesButton;
    private javax.swing.JLabel riseSetAzLbl;
    private javax.swing.JLabel rotAzLbl;
    private javax.swing.JLabel rotElLbl;
    private javax.swing.JButton rotatorSetBtn;
    private javax.swing.JButton runPassPredictionButton;
    private javax.swing.JComboBox<Serializable> satComboBox;
    private javax.swing.JLabel satLbl;
    private javax.swing.JButton saveTableButton;
    private javax.swing.JCheckBox timeChkBx;
    private javax.swing.JLabel timeLbl;
    private javax.swing.JLabel timeSpanLbl;
    private javax.swing.JTextField timeSpanTextField;
    private javax.swing.JLabel timeStepLbl;
    private javax.swing.JTextField timeStepTextField;
    private javax.swing.JToggleButton trackButton;
    private javax.swing.JCheckBox visibleOnlyCheckBox;
    // End of variables declaration//GEN-END:variables

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings({ "unchecked", "serial" })
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        polarPlotPanel = new javax.swing.JPanel();
        polarPlotLbl = new jsattrak.gui.JPolarPlotLabel();
        polarPlotToolBar = new javax.swing.JToolBar();
        printPolarPlotBtn = new javax.swing.JButton();
        leadLagCheckBox = new javax.swing.JCheckBox();
        timeChkBx = new javax.swing.JCheckBox();
        namesChkBx = new javax.swing.JCheckBox();
        invChkBx = new javax.swing.JCheckBox();
        horizLimChkBx = new javax.swing.JCheckBox();
        compassChkBx = new javax.swing.JCheckBox();
        passPredictPanel = new javax.swing.JPanel();
        passPredictLbl = new javax.swing.JLabel();
        passScrollPane = new javax.swing.JScrollPane();
        passTable = new javax.swing.JTable();
        timeSpanLbl = new javax.swing.JLabel();
        timeSpanTextField = new javax.swing.JTextField();
        timeStepLbl = new javax.swing.JLabel();
        timeStepTextField = new javax.swing.JTextField();
        runPassPredictionButton = new javax.swing.JButton();
        visibleOnlyCheckBox = new javax.swing.JCheckBox();
        go2passButton = new javax.swing.JButton();
        printTableButton = new javax.swing.JButton();
        saveTableButton = new javax.swing.JButton();
        riseSetAzLbl = new javax.swing.JLabel();
        azComboBox = new javax.swing.JComboBox();
        basicPanel = new javax.swing.JPanel();
        gsLbl = new javax.swing.JLabel();
        gsComboBox = new javax.swing.JComboBox<String>();
        satLbl = new javax.swing.JLabel();
        satComboBox = new javax.swing.JComboBox<Serializable>();
        currInfoLbl = new javax.swing.JLabel();
        refreshComboBoxesButton = new javax.swing.JButton();
        trackButton = new javax.swing.JToggleButton();
        rotatorSetBtn = new javax.swing.JButton();
        timeLbl = new javax.swing.JLabel();
        azLbl = new javax.swing.JLabel();
        rotAzLbl = new javax.swing.JLabel();
        elLbl = new javax.swing.JLabel();
        rotElLbl = new javax.swing.JLabel();
        rangeLbl = new javax.swing.JLabel();
        nextPassLbl = new javax.swing.JLabel();

        jTabbedPane1.setMinimumSize(new java.awt.Dimension(53, 53));

        polarPlotPanel.setLayout(new java.awt.BorderLayout());

        polarPlotLbl.setBackground(new java.awt.Color(0, 0, 0));
        polarPlotPanel.add(polarPlotLbl, java.awt.BorderLayout.CENTER);

        polarPlotToolBar.setRollover(true);

        printPolarPlotBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/document-print.png"))); // NOI18N
        printPolarPlotBtn.setToolTipText("Print Polar Plot - automatically prints with inverted colors, names, and time");
        printPolarPlotBtn.setFocusable(false);
        printPolarPlotBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        printPolarPlotBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        printPolarPlotBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printPolarPlotBtnActionPerformed(evt);
            }
        });
        polarPlotToolBar.add(printPolarPlotBtn);

        leadLagCheckBox.setSelected(true);
        leadLagCheckBox.setText("Lead/Lag Data"); // NOI18N
        leadLagCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leadLagCheckBoxActionPerformed(evt);
            }
        });
        polarPlotToolBar.add(leadLagCheckBox);

        timeChkBx.setSelected(true);
        timeChkBx.setText("Time"); // NOI18N
        timeChkBx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeChkBxActionPerformed(evt);
            }
        });
        polarPlotToolBar.add(timeChkBx);

        namesChkBx.setText("Names"); // NOI18N
        namesChkBx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                namesChkBxActionPerformed(evt);
            }
        });
        polarPlotToolBar.add(namesChkBx);

        invChkBx.setText("Invert");
        invChkBx.setToolTipText("Invert line and background colors");
        invChkBx.setFocusable(false);
        invChkBx.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        invChkBx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invChkBxActionPerformed(evt);
            }
        });
        polarPlotToolBar.add(invChkBx);

        horizLimChkBx.setText("Limit to Horizon");
        horizLimChkBx.setFocusable(false);
        horizLimChkBx.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        horizLimChkBx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                horizLimChkBxActionPerformed(evt);
            }
        });
        polarPlotToolBar.add(horizLimChkBx);

        compassChkBx.setSelected(true);
        compassChkBx.setText("Compass");
        compassChkBx.setFocusable(false);
        compassChkBx.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        compassChkBx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compassChkBxActionPerformed(evt);
            }
        });
        polarPlotToolBar.add(compassChkBx);

        polarPlotPanel.add(polarPlotToolBar, java.awt.BorderLayout.SOUTH);

        jTabbedPane1.addTab("Polar Plot", polarPlotPanel);

        passPredictLbl.setFont(new java.awt.Font("Tahoma", 1, 12));
        passPredictLbl.setText("Pass Predictions:"); // NOI18N

        passTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "#", "Rise Time", "Set Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true
            };

            public Class<?> getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        passScrollPane.setViewportView(passTable);
        passTable.getColumnModel().getColumn(0).setHeaderValue("#");
        passTable.getColumnModel().getColumn(1).setHeaderValue("Rise Time");
        passTable.getColumnModel().getColumn(2).setHeaderValue("Set Time");

        timeSpanLbl.setText("Time Span [Days]:"); // NOI18N

        timeSpanTextField.setText("10"); // NOI18N

        timeStepLbl.setText("Time Step [sec]:"); // NOI18N

        timeStepTextField.setText("60.0"); // NOI18N

        runPassPredictionButton.setText("Calculate"); // NOI18N
        runPassPredictionButton.setToolTipText("Calculate Pass Predictions");
        runPassPredictionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runPassPredictionButtonActionPerformed(evt);
            }
        });

        visibleOnlyCheckBox.setText("Visible Only"); // NOI18N

        go2passButton.setText("Go to Pass");
        go2passButton.setToolTipText("Set the time to the middle of the pass so it can be analyzed (e.g. polar plot)");
        go2passButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                go2passButtonActionPerformed(evt);
            }
        });

        printTableButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/document-print.png"))); // NOI18N
        printTableButton.setToolTipText("Print Table");
        printTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printTableButtonActionPerformed(evt);
            }
        });

        saveTableButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/document-save-as.png"))); // NOI18N
        saveTableButton.setToolTipText("Save Table to File");
        saveTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveTableButtonActionPerformed(evt);
            }
        });

        riseSetAzLbl.setText("Rise/Set Azimuth:");

        azComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Degrees", "Compass Points" }));
        azComboBox.setSelectedIndex(1);

        javax.swing.GroupLayout passPredictPanelLayout = new javax.swing.GroupLayout(passPredictPanel);
        passPredictPanel.setLayout(passPredictPanelLayout);
        passPredictPanelLayout.setHorizontalGroup(
            passPredictPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(passPredictPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(passPredictPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(passPredictPanelLayout.createSequentialGroup()
                        .addComponent(passScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                        .addContainerGap())
                    .addComponent(passPredictLbl)
                    .addGroup(passPredictPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(timeSpanLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timeSpanTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(timeStepLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timeStepTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(54, 54, 54))
                    .addGroup(passPredictPanelLayout.createSequentialGroup()
                        .addComponent(visibleOnlyCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(riseSetAzLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(azComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 111, Short.MAX_VALUE)
                        .addComponent(runPassPredictionButton)
                        .addContainerGap())
                    .addGroup(passPredictPanelLayout.createSequentialGroup()
                        .addComponent(go2passButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 272, Short.MAX_VALUE)
                        .addComponent(saveTableButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(printTableButton)
                        .addContainerGap())))
        );
        passPredictPanelLayout.setVerticalGroup(
            passPredictPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(passPredictPanelLayout.createSequentialGroup()
                .addComponent(passPredictLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(passPredictPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeSpanLbl)
                    .addComponent(timeSpanTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeStepLbl)
                    .addComponent(timeStepTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(passPredictPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(visibleOnlyCheckBox)
                    .addComponent(runPassPredictionButton)
                    .addComponent(riseSetAzLbl)
                    .addComponent(azComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(passScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(passPredictPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(go2passButton)
                    .addComponent(printTableButton)
                    .addComponent(saveTableButton)))
        );

        jTabbedPane1.addTab("Pass Predictions", passPredictPanel);

        gsLbl.setText("Ground Station:"); // NOI18N

        gsComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gsComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                gsComboBoxItemStateChanged(evt);
            }
        });
        gsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gsComboBoxActionPerformed(evt);
            }
        });

        satLbl.setText("Satellite:"); // NOI18N

        satComboBox.setModel(new javax.swing.DefaultComboBoxModel<Serializable>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        satComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                satComboBoxItemStateChanged(evt);
            }
        });
        satComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                satComboBoxActionPerformed(evt);
            }
        });

        currInfoLbl.setText("Current Tracking Information:"); // NOI18N

        refreshComboBoxesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/view-refresh.png"))); // NOI18N
        refreshComboBoxesButton.setToolTipText("refresh sat and ground stations"); // NOI18N
        refreshComboBoxesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshComboBoxesButtonActionPerformed(evt);
            }
        });

        trackButton.setText("Track!");
        trackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackButtonActionPerformed(evt);
            }
        });

        rotatorSetBtn.setText("Rotator Settings");
        rotatorSetBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotatorSetBtnActionPerformed(evt);
            }
        });

        timeLbl.setText("Current Time: "); // NOI18N

        azLbl.setText("Azimuth (Satellite) [deg]: "); // NOI18N

        rotAzLbl.setText("Azimuth (Rotator) [deg]: "); // NOI18N

        elLbl.setText("Elevation (Satellite) [deg]: "); // NOI18N

        rotElLbl.setText("Elevation (Rotator) [deg]: "); // NOI18N

        rangeLbl.setText("Range [m]: "); // NOI18N

        nextPassLbl.setText("Next Pass at: ");

        javax.swing.GroupLayout basicPanelLayout = new javax.swing.GroupLayout(basicPanel);
        basicPanel.setLayout(basicPanelLayout);
        basicPanelLayout.setHorizontalGroup(
            basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(basicPanelLayout.createSequentialGroup()
                        .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(currInfoLbl)
                            .addGroup(basicPanelLayout.createSequentialGroup()
                                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(basicPanelLayout.createSequentialGroup()
                                        .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(gsLbl)
                                            .addComponent(satLbl))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(satComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(gsComboBox, 0, 169, Short.MAX_VALUE)))
                                    .addComponent(azLbl)
                                    .addComponent(elLbl))
                                .addGap(8, 8, 8)
                                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(basicPanelLayout.createSequentialGroup()
                                        .addComponent(refreshComboBoxesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(rotatorSetBtn)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(trackButton)
                                        .addGap(12, 12, 12))
                                    .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(rotElLbl)
                                        .addComponent(rotAzLbl)))))
                        .addGap(0, 0, 0))
                    .addComponent(timeLbl)
                    .addGroup(basicPanelLayout.createSequentialGroup()
                        .addComponent(rangeLbl)
                        .addContainerGap(418, Short.MAX_VALUE))
                    .addGroup(basicPanelLayout.createSequentialGroup()
                        .addComponent(nextPassLbl)
                        .addContainerGap(407, Short.MAX_VALUE))))
        );
        basicPanelLayout.setVerticalGroup(
            basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicPanelLayout.createSequentialGroup()
                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(basicPanelLayout.createSequentialGroup()
                        .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(basicPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(gsLbl)
                                    .addComponent(gsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(satLbl)
                                    .addComponent(satComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(currInfoLbl))
                            .addGroup(basicPanelLayout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(refreshComboBoxesButton)
                                    .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(rotatorSetBtn)
                                        .addComponent(trackButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timeLbl)
                        .addGap(26, 26, 26))
                    .addGroup(basicPanelLayout.createSequentialGroup()
                        .addGap(107, 107, 107)
                        .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(azLbl)
                            .addComponent(rotAzLbl))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotElLbl)
                    .addComponent(elLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rangeLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(nextPassLbl)
                .addGap(99, 99, 99))
        );

        jTabbedPane1.addTab("Basic", basicPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void trackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackButtonActionPerformed
        clearCurrTrackInfo();
        if(timeAsString != null)
            updateTime(timeAsString);
        objectChangeReset();
        /* if (gsComboBox.getSelectedItem() != null)
            currentGs = app.getGs(gsComboBox.getSelectedItem().toString());
        if (satComboBox.getSelectedItem() != null)
            currentSat = app.getSatellite(satComboBox.getSelectedItem().toString());
        if (timeAsString != null)
            updateTime(timeAsString);
        objectChangeReset();
        */
    }//GEN-LAST:event_trackButtonActionPerformed

    public void clickTrackButton()
    {
        trackButton.doClick();
    }
}

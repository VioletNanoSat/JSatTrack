package edu.cusat.gs.gui;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jsattrak.gui.CommPortComboBox;
import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.GroundStation;
import jsattrak.objects.SunSat;
import edu.cusat.common.Failure;
import edu.cusat.gs.rotator.GroundPass;
import edu.cusat.gs.rotator.MockRotator;
import edu.cusat.gs.rotator.Rotator;
import edu.cusat.gs.rotator.RotatorController;
import edu.cusat.gs.rotator.RotatorFactory;
import edu.cusat.gs.rotator.RotatorModel;
import edu.cusat.gs.rotator.RotatorModelListener;
import gnu.io.CommPortIdentifier;

public class RotatorControlPanel extends javax.swing.JPanel implements RotatorModelListener{

	private static final long serialVersionUID = -4935513147278669259L;

	/** The rotator being controlled at the moment **/
    private Rotator rotator = null;
    
    /** The object that handles the specifics of how to control the rotator **/
    private RotatorController rotatorController= null;

    /** Satellite being currently being tracked **/
    private AbstractSatellite curSat;
    
    /** Currently set Ground Station **/
    private GroundStation curGs;   
    
    /** Creates new form RotatorControlPanel */
    public RotatorControlPanel(AbstractSatellite sat,GroundStation gs) {
        curSat = sat;
        curGs = gs;

        initComponents(); 
        
        // To initialize the buttons to green
        disconnectFromRotator();
        setAutoCtrl(true);
    }

    /** @param r The RotatorModel that changed **/
    @Override public void modelChanged(final RotatorModel r) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                rotAzTF.setText(r.az.get().toString());
                rotElTF.setText(r.el.get().toString());
            }
        });
    }
    
//************************** Update time and helpers *************************//
    
    public void updateTime() {
		if (curSat != null && curGs != null) {
			if (curSat.getTEMEPos() != null
					&& !Double.isNaN(curSat.getTEMEPos()[0])) {
				double[] aer = curGs.calculate_AER(curSat.getTEMEPos()); // TEME

				// add text AER and string
				// satellite.setText( TIME_LBL_TXT + timeAsString);
				satAzTF.setText(String.format("%.3f", aer[0]));
				satElTF.setText(String.format("%.3f", aer[1]));
				rangeTF.setText(String.format("%.3f", aer[2]));
			}
		} else {
			satAzTF.setText("N/A");
			satElTF.setText("N/A");
			rangeTF.setText("N/A");
		}

        // Figure out when the next pass is
//        if (currentPass != null) // There's a pass, but it ended
//            if (Time.jd2DateTime(currentPass.setTime()).isBefore(currentTime)) {
//                currentPass = null;
//                nextPassLbl.setText("");
//            }
//        // There's no pass now, but we can predict the next one
//        if (futurePasses.size() > 0 && currentPass == null) {
//            DateTime nextRise = Time.jd2DateTime(futurePasses.get(0).riseTime());
//            if(nextRise.isAfter(currentTime)){
//                nextPassLbl.setText(NEXT_PASS_LBL_TXT
//                        + nextRise.toString("MMM dd, YYYY -- HH:mm:ss"));
//            } else {// It just started
//                currentPass = futurePasses.remove(0);
//                nextPassLbl.setText("Pass ends at: "
//                        + new DateTime(Time.jd2DateTime(currentPass
//                                .setTime()))
//                                .toString("MMM dd, YYYY -- HH:mm:ss"));
//            }
//        } 

		if (!manualCtrlBtn.isSelected() && rotator != null && rotator.isConnected()) {
			if (curGs != null && curSat != null)
				trackSatellite();
		}
    }
	
    private void trackSatellite() {

        // FIXME SunSat doesn't have any future passes
        List<GroundPass> futurePasses = GroundPass.find(curGs,curSat);
        GroundPass currentPass = null;
        if (futurePasses.size() > 0) {
        	currentPass = futurePasses.remove(0);
        }
        // FIXME futurePasses.remove() doesn't return null, it throws IndexOutOfBoundsException
        if(currentPass == null && !(curSat instanceof SunSat)) 
            return;
        double[] aer = curGs.calculate_AER(curSat.getTEMEPos());

        int az = (int)Math.round(aer[0]);
        int el = (int)Math.round(aer[1]);
    	if (aer[1] < 0 && aer[1] >= -5) // minimum for rotating Azimuth to pre-track sat
    		el = 0;
    	if (aer[1] > 180 && aer[1] <= 185) // maximum in case we are at opposite horizon
    		el = 180;
    	// these values correspond to about 1 minute of pre-track for ISS
    	// This is because g5500 rotator takes about 60 seconds to rotate 360 deg azi
       
        // FIXME not how you tell if there's a ground pass, check if we're
        // between riseTime and setTime
        if(currentPass != null){                   // have a ground pass?
            if(currentPass.requiresFlip()){        // invert all directions?
                az = (az < 180) ? az + 180 : az - 180;
                el = 180 - el;
            } else if (currentPass.crossesNorth() && az < 180) {
                az += 360; // have to go past 360 to ensure continuous coverage
            }
        // TODO Fix logic so you always try to point to sun, unless it's below horizon
        } 
        //TODO Fix this dead code
        /*else if (curSat instanceof SunSat) { 
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
        }*/
		try {
			rotatorController.rotate(az, el);
		} catch (Failure f) {
			System.out.println(f.getMessage());
		}
    }

//**************************** Rotator Connection ****************************//
    
    private void rotatorConnectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rotatorConnectBtnActionPerformed
        if (rotatorConnectBtn.isSelected()) {
        	if(rotatorComboBox.getSelectedItem() instanceof RotatorFactory){     
                rotator = ((RotatorFactory) rotatorComboBox
                    .getSelectedItem()).createRotator();
                connectToRotator();
                elOffsetSpin.setEnabled(true);
                azOffsetSpin.setEnabled(true);
        	} else {
    			JOptionPane.showMessageDialog(this, "Please Select Rotator",
    					"Error", JOptionPane.ERROR_MESSAGE);
    			rotatorComboBox.setSelectedIndex(0);

    		}
        }
        else disconnectFromRotator();
    }//GEN-LAST:event_rotatorConnectBtnActionPerformed

    private void connectToRotator() {
		// We need a serial port for all real rotators
		if (((CommPortComboBox)commPortComboBox).getSelectedPort() == null && 
				!(rotator instanceof MockRotator)) {
			JOptionPane.showMessageDialog(this,
					"Please Select COM port", "Error",
					JOptionPane.ERROR_MESSAGE);
			commPortComboBox.setSelectedIndex(0);
			return;
		}
		try {
		    rotator.addModelListener(this);
		    rotator.setPort(((CommPortComboBox)commPortComboBox).getSelectedPort());
		    rotator.connect(Integer.parseInt(baudRate.getSelectedItem().toString()));
		    rotatorController = new RotatorController(rotator);
		    rotAzTF.setText(rotator.az() + rotatorController.getAzOffset() + "");
		    rotElTF.setText(rotator.el() + rotatorController.getElOffset() + "");
		    elOffsetSpin.setEnabled(true);
		    azOffsetSpin.setEnabled(true);

		    rotatorConnectBtn.setBackground(java.awt.Color.RED);
            rotatorConnectBtn.setText("Disconnect Rotator");
		    
		} catch (Failure e) {
		    e.printStackTrace();
		    rotatorComboBox.setSelectedIndex(0);
		    disconnectFromRotator();
			JOptionPane.showMessageDialog(this,
					"Rotator Connection Failure.", "Error",
					JOptionPane.ERROR_MESSAGE);
			
		}
	}
    
	private void disconnectFromRotator() {
		if (rotator != null) {
		    rotator.disconnect();
		    rotator = null;
		    rotatorController = null;
		}
		rotatorComboBox.setSelectedIndex(0);

		rotAzTF.setText("N/A");
		rotElTF.setText("N/A");
		rotatorConnectBtn.setBackground(java.awt.Color.GREEN);
		rotatorConnectBtn.setText("Connect Rotator");
		elOffsetSpin.setEnabled(false);
		azOffsetSpin.setEnabled(false);
	}
    
//*************************** Auto/Manual Control ****************************//

	private void manualCtrlBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualCtrlBtnActionPerformed
    	setAutoCtrl(!manualCtrlBtn.isSelected());
    }//GEN-LAST:event_manualCtrlBtnActionPerformed
    
    private void setAutoCtrl(boolean enabled) {
    	if(enabled){
    		manualCtrlBtn.setBackground(java.awt.Color.GREEN);
    		manualCtrlBtn.setText("Manual Control");
    		azSpinner.setEnabled(false);
    		elSpinner.setEnabled(false);
    		
    	} else {
    		manualCtrlBtn.setBackground(java.awt.Color.RED);
    		manualCtrlBtn.setText("Auto Control");
    		azSpinner.setEnabled(true);
    		elSpinner.setEnabled(true);
    	}
    }

    private void azSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_azSpinnerStateChanged
		if (manualCtrlBtn.isSelected() && rotatorConnectBtn.isSelected())
			try {
				rotatorController.rotate((Integer) azSpinner.getValue(),
						(Integer) elSpinner.getValue());
			} catch (Failure f) {
				JOptionPane.showMessageDialog(this,
						"Can't set the azimuth.", "Error",
						JOptionPane.ERROR_MESSAGE);
				System.out.println(f.getMessage());
			}

    }//GEN-LAST:event_azSpinnerStateChanged

    private void elSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_elSpinnerStateChanged
    	if (manualCtrlBtn.isSelected() && rotatorConnectBtn.isSelected())
			try {
				rotatorController.rotate((Integer) azSpinner.getValue(),
						(Integer) elSpinner.getValue());
			} catch (Failure f) {
				JOptionPane.showMessageDialog(this,
						"Can't set the elevation.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
    }//GEN-LAST:event_elSpinnerStateChanged

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        settingsPnl = new javax.swing.JPanel();
        rotatorLbl = new javax.swing.JLabel();
        rotatorComboBox = new javax.swing.JComboBox();
        commPortLbl = new javax.swing.JLabel();
        commPortComboBox = new CommPortComboBox(CommPortIdentifier.PORT_SERIAL);
        rotatorConnectBtn = new javax.swing.JToggleButton();
        rotatorLbl1 = new javax.swing.JLabel();
        baudRate = new javax.swing.JComboBox();
        elOffset = new javax.swing.JLabel();
        elOffsetSpin = new javax.swing.JSpinner();
        azOffset = new javax.swing.JLabel();
        azOffsetSpin = new javax.swing.JSpinner();
        manCtrlPnl = new javax.swing.JPanel();
        azLbl = new javax.swing.JLabel();
        azSpinner = new javax.swing.JSpinner();
        elLbl = new javax.swing.JLabel();
        elSpinner = new javax.swing.JSpinner();
        manualCtrlBtn = new javax.swing.JToggleButton();
        satStatPnl = new javax.swing.JPanel();
        satStatLbl = new javax.swing.JLabel();
        satAzLbl = new javax.swing.JLabel();
        satAzTF = new javax.swing.JTextField();
        satElLbl = new javax.swing.JLabel();
        satElTF = new javax.swing.JTextField();
        rangeLbl = new javax.swing.JLabel();
        rangeTF = new javax.swing.JTextField();
        rotStatPnl = new javax.swing.JPanel();
        rotStatLbl = new javax.swing.JLabel();
        rotAzLbl = new javax.swing.JLabel();
        rotAzTF = new javax.swing.JTextField();
        rotElLbl = new javax.swing.JLabel();
        rotElTF = new javax.swing.JTextField();

        settingsPnl.setBackground(new java.awt.Color(0, 102, 51));

        rotatorLbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        rotatorLbl.setForeground(new java.awt.Color(255, 255, 255));
        rotatorLbl.setText("Rotator");

        for (RotatorFactory rf : RotatorFactory.values())
        rotatorComboBox.addItem(rf);

        commPortLbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        commPortLbl.setForeground(new java.awt.Color(255, 255, 255));
        commPortLbl.setText("COM Port");

        rotatorConnectBtn.setFont(rotatorConnectBtn.getFont().deriveFont(rotatorConnectBtn.getFont().getSize()+1f));
        rotatorConnectBtn.setText("Connect to Rotator");
        rotatorConnectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rotatorConnectBtnActionPerformed(evt);
            }
        });

        rotatorLbl1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        rotatorLbl1.setForeground(new java.awt.Color(255, 255, 255));
        rotatorLbl1.setText("Baud Rate");

        baudRate.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "9600", "1200", "4800" }));

        elOffset.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        elOffset.setForeground(new java.awt.Color(255, 255, 255));
        elOffset.setText("ElOffset");

        elOffsetSpin.setModel(new javax.swing.SpinnerNumberModel(0, -180, 180, 1));
        elOffsetSpin.setEnabled(false);
        elOffsetSpin.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
            	if (rotator != null && rotator.isConnected()) {
            		rotatorController.setElOffset((Integer) elOffsetSpin.getValue());
            		try {
            			if(manualCtrlBtn.isSelected()){
        				rotatorController.rotate((Integer) azSpinner.getValue(),
        						(Integer) elSpinner.getValue());
            			}
            			else trackSatellite();
            		} catch (Failure e) { 
            			System.out.println("Could not set Elevation Offset. Error");
            		}
            	}
            }
        	});

        azOffset.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        azOffset.setForeground(new java.awt.Color(255, 255, 255));
        azOffset.setText("AzOffset");
        azOffset.setRequestFocusEnabled(false);
        azOffset.setVerifyInputWhenFocusTarget(false);

        azOffsetSpin.setModel(new javax.swing.SpinnerNumberModel(0, -180, 180, 1));
        azOffsetSpin.setEnabled(false);
        azOffsetSpin.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
            	if (rotator != null && rotator.isConnected()) {
            		rotatorController.setAzOffset((Integer) azOffsetSpin.getValue());
            		try {
            			if(manualCtrlBtn.isSelected()){
        				rotatorController.rotate((Integer) azSpinner.getValue(),
        						(Integer) elSpinner.getValue());
            			}
            			else trackSatellite(); // this is temporary. It might work. 
        			} catch (Failure e) {
					System.out.println("Could not set Azimuth Offset. Error");
				}
            }
            }
        });

        javax.swing.GroupLayout settingsPnlLayout = new javax.swing.GroupLayout(settingsPnl);
        settingsPnl.setLayout(settingsPnlLayout);
        settingsPnlLayout.setHorizontalGroup(
            settingsPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingsPnlLayout.createSequentialGroup()
                        .addGroup(settingsPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(settingsPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(settingsPnlLayout.createSequentialGroup()
                                    .addGroup(settingsPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(rotatorLbl1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(commPortLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(settingsPnlLayout.createSequentialGroup()
                                            .addComponent(rotatorLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(0, 0, Short.MAX_VALUE)))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                .addGroup(settingsPnlLayout.createSequentialGroup()
                                    .addComponent(azOffset)
                                    .addGap(27, 27, 27)))
                            .addGroup(settingsPnlLayout.createSequentialGroup()
                                .addComponent(elOffset, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(32, 32, 32)))
                        .addGroup(settingsPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(elOffsetSpin)
                            .addComponent(azOffsetSpin)
                            .addComponent(rotatorComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(baudRate, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(commPortComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(rotatorConnectBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        settingsPnlLayout.setVerticalGroup(
            settingsPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(rotatorComboBox)
                    .addComponent(rotatorLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotatorLbl1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(baudRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(commPortComboBox)
                    .addComponent(commPortLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(azOffset)
                    .addComponent(azOffsetSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(elOffset)
                    .addComponent(elOffsetSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rotatorConnectBtn)
                .addContainerGap())
        );

        manCtrlPnl.setBackground(new java.awt.Color(0, 102, 51));

        azLbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        azLbl.setForeground(new java.awt.Color(255, 255, 255));
        azLbl.setText("Azimuth");
        azLbl.setRequestFocusEnabled(false);
        azLbl.setVerifyInputWhenFocusTarget(false);

        azSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 450, 1));
        azSpinner.setEnabled(false);
        azSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                azSpinnerStateChanged(evt);
            }
        });

        elLbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        elLbl.setForeground(new java.awt.Color(255, 255, 255));
        elLbl.setText("Elevation");

        elSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 180, 1));
        elSpinner.setEnabled(false);
        elSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                elSpinnerStateChanged(evt);
            }
        });

        manualCtrlBtn.setFont(manualCtrlBtn.getFont().deriveFont(manualCtrlBtn.getFont().getSize()+1f));
        manualCtrlBtn.setText("Manual Control");
        manualCtrlBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualCtrlBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout manCtrlPnlLayout = new javax.swing.GroupLayout(manCtrlPnl);
        manCtrlPnl.setLayout(manCtrlPnlLayout);
        manCtrlPnlLayout.setHorizontalGroup(
            manCtrlPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manCtrlPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(manCtrlPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(manualCtrlBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(manCtrlPnlLayout.createSequentialGroup()
                        .addComponent(azLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(azSpinner))
                    .addGroup(manCtrlPnlLayout.createSequentialGroup()
                        .addComponent(elLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(elSpinner)))
                .addContainerGap())
        );
        manCtrlPnlLayout.setVerticalGroup(
            manCtrlPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(manCtrlPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(manCtrlPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(azSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(azLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(manCtrlPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(elSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(elLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(manualCtrlBtn)
                .addContainerGap())
        );

        satStatPnl.setBackground(new java.awt.Color(51, 153, 0));

        satStatLbl.setFont(satStatLbl.getFont().deriveFont(satStatLbl.getFont().getStyle() | java.awt.Font.BOLD, satStatLbl.getFont().getSize()+3));
        satStatLbl.setForeground(new java.awt.Color(255, 255, 255));
        satStatLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        satStatLbl.setText("Satellite Status");

        satAzLbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        satAzLbl.setForeground(new java.awt.Color(255, 255, 255));
        satAzLbl.setText("Azimuth");

        satAzTF.setEditable(false);
        satAzTF.setText("N/A");

        satElLbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        satElLbl.setForeground(new java.awt.Color(255, 255, 255));
        satElLbl.setText("Elevation");

        satElTF.setEditable(false);
        satElTF.setText("N/A");

        rangeLbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        rangeLbl.setForeground(new java.awt.Color(255, 255, 255));
        rangeLbl.setText("Range");

        rangeTF.setEditable(false);
        rangeTF.setText("N/A");

        javax.swing.GroupLayout satStatPnlLayout = new javax.swing.GroupLayout(satStatPnl);
        satStatPnl.setLayout(satStatPnlLayout);
        satStatPnlLayout.setHorizontalGroup(
            satStatPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(satStatPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(satStatPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(satStatPnlLayout.createSequentialGroup()
                        .addComponent(satAzLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(satAzTF, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(satStatPnlLayout.createSequentialGroup()
                        .addComponent(satElLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(satElTF, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(satStatPnlLayout.createSequentialGroup()
                        .addComponent(rangeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rangeTF, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(satStatLbl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        satStatPnlLayout.setVerticalGroup(
            satStatPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(satStatPnlLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(satStatLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(satStatPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(satAzTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(satAzLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(satStatPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(satElTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(satElLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(satStatPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rangeTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rangeLbl))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        rotStatPnl.setBackground(new java.awt.Color(51, 153, 0));

        rotStatLbl.setFont(rotStatLbl.getFont().deriveFont(rotStatLbl.getFont().getStyle() | java.awt.Font.BOLD, rotStatLbl.getFont().getSize()+3));
        rotStatLbl.setForeground(new java.awt.Color(255, 255, 255));
        rotStatLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rotStatLbl.setText("Rotator Status");

        rotAzLbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        rotAzLbl.setForeground(new java.awt.Color(255, 255, 255));
        rotAzLbl.setText("Azimuth");

        rotAzTF.setEditable(false);
        rotAzTF.setText("N/A");

        rotElLbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        rotElLbl.setForeground(new java.awt.Color(255, 255, 255));
        rotElLbl.setText("Elevation");

        rotElTF.setEditable(false);
        rotElTF.setText("N/A");

        javax.swing.GroupLayout rotStatPnlLayout = new javax.swing.GroupLayout(rotStatPnl);
        rotStatPnl.setLayout(rotStatPnlLayout);
        rotStatPnlLayout.setHorizontalGroup(
            rotStatPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rotStatPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rotStatPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(rotStatPnlLayout.createSequentialGroup()
                        .addComponent(rotAzLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rotAzTF, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
                    .addGroup(rotStatPnlLayout.createSequentialGroup()
                        .addComponent(rotElLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rotElTF, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
                    .addComponent(rotStatLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE))
                .addContainerGap())
        );
        rotStatPnlLayout.setVerticalGroup(
            rotStatPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rotStatPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rotStatLbl)
                .addGap(18, 18, 18)
                .addGroup(rotStatPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotAzLbl)
                    .addComponent(rotAzTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rotStatPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rotElLbl)
                    .addComponent(rotElTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(satStatPnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(settingsPnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(manCtrlPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rotStatPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(settingsPnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(manCtrlPnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rotStatPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(satStatPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void elOffsetSpinStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_elOffsetSpinStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_elOffsetSpinStateChanged

    private void azOffsetSpinStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_azOffsetSpinStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_azOffsetSpinStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel azLbl;
    private javax.swing.JLabel azOffset;
    private javax.swing.JSpinner azOffsetSpin;
    private javax.swing.JSpinner azSpinner;
    private javax.swing.JComboBox baudRate;
    private javax.swing.JComboBox commPortComboBox;
    private javax.swing.JLabel commPortLbl;
    private javax.swing.JLabel elLbl;
    private javax.swing.JLabel elOffset;
    private javax.swing.JSpinner elOffsetSpin;
    private javax.swing.JSpinner elSpinner;
    private javax.swing.JPanel manCtrlPnl;
    private javax.swing.JToggleButton manualCtrlBtn;
    private javax.swing.JLabel rangeLbl;
    private javax.swing.JTextField rangeTF;
    private javax.swing.JLabel rotAzLbl;
    private javax.swing.JTextField rotAzTF;
    private javax.swing.JLabel rotElLbl;
    private javax.swing.JTextField rotElTF;
    private javax.swing.JLabel rotStatLbl;
    private javax.swing.JPanel rotStatPnl;
    private javax.swing.JComboBox rotatorComboBox;
    private javax.swing.JToggleButton rotatorConnectBtn;
    private javax.swing.JLabel rotatorLbl;
    private javax.swing.JLabel rotatorLbl1;
    private javax.swing.JLabel satAzLbl;
    private javax.swing.JTextField satAzTF;
    private javax.swing.JLabel satElLbl;
    private javax.swing.JTextField satElTF;
    private javax.swing.JLabel satStatLbl;
    private javax.swing.JPanel satStatPnl;
    private javax.swing.JPanel settingsPnl;
    private int rotatorElOffset;
    private int rotatorAzOffset;
    // End of variables declaration//GEN-END:variables

    /** This window's title **/
    public static final String TITLE = "Rotator";
    
    public void setCurGs(GroundStation gs) { curGs = gs; }
    
    public void setCurSat(AbstractSatellite sat) { curSat = sat; }
   
}

/*
 * Ts2000ControlPanel.java
 *
 * Created on Sep 2, 2009, 9:04:29 PM
 */
package edu.cusat.gs.gui;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalProgressBarUI;

import jsattrak.gui.CommPortComboBox;
import jsattrak.gui.JSatTrak;
import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.GroundStation;
import edu.cusat.common.Failure;
import edu.cusat.gs.doppler.DopplerAdjuster;
import edu.cusat.gs.ts2000.KenwoodTs2kController;
import edu.cusat.gs.ts2000.KenwoodTs2kModel;
import edu.cusat.gs.ts2000.KenwoodTs2kModelListener;
import edu.cusat.gs.ts2000.KenwoodTs2kModel.MainVfo;
import edu.cusat.gs.ts2000.KenwoodTs2kModel.Vfo;
import gnu.io.CommPortIdentifier;

public class Ts2000ControlPanel extends javax.swing.JPanel implements KenwoodTs2kModelListener {

	private static final long serialVersionUID = 1L;
	private static final int INIT_SLIDERS = 50;
	private JSatTrak app;
    //private GroundSystemsPanel parent;
    /** Automatic doppler shift adjuster **/
    private DopplerAdjuster dopplerAdj;
    private KenwoodTs2kController radioController;
    /** Satellite being currently being tracked **/
    private AbstractSatellite currentSat;
    /** Currently set Ground Station **/
    private GroundStation currentGs;

    /** Creates new form Ts2000ControlPanel */
    public Ts2000ControlPanel(JSatTrak app) {
        this.app = app;
        radioController = new KenwoodTs2kController();
        //this.parent = parent;
        initComponents();

        // add VFOs to combo boxes
        for (KenwoodTs2kModel.MainVfo sB : KenwoodTs2kModel.MainVfo.values()) {
            txVfoComboBox.addItem(sB);
            rxVfoComboBox.addItem(sB);
        }
        rxVfoComboBox.setSelectedItem(MainVfo.A);
        txVfoComboBox.setSelectedItem(MainVfo.B);
        radioController.registerListener(this);
        MetalProgressBarUI progUI = new MetalProgressBarUI();
        jProgressBarSMeter.setUI(progUI);
    }
    
    public KenwoodTs2kController getTS2000(){
    	return radioController;
    }

    @Override
    public void modelChanged(final KenwoodTs2kModel model) {
        SwingUtilities.invokeLater(

        new Runnable() {

            private void updateTxVfo() {
            	if (!radioController.isConnected()){
            		 jTextFieldCurTXFreq.setText("N/A");
            		return;
            	}
                Vfo tx_vfo = model.tx_vfo.get();
                txVfoLbl.setText(tx_vfo.toString());

                switch (tx_vfo) {
                    case A:
                        jTextFieldCurTXFreq.setText(model.vfo_a_frequency.get().toString());
                        break;
                    case B:
                        jTextFieldCurTXFreq.setText(model.vfo_b_frequency.get().toString());
                        break;
                    case SUB:
                        jTextFieldCurTXFreq.setText(model.vfo_sub_frequency.get().toString());
                        break;
                    default:
                        jTextFieldCurTXFreq.setText("");
                        break;
                }
            }

            private void updateRxVfo() {
            	if (!radioController.isConnected()){
            		jTextFieldCurRXFreq.setText("N/A");
            		return;
            	}
                Vfo rx_vfo = model.rx_vfo.get();
                rxVfoLbl.setText(rx_vfo.toString());
                switch (rx_vfo) {
                    case A:
                        jTextFieldCurRXFreq.setText(model.vfo_a_frequency.get().toString());
                        break;
                    case B:
                        jTextFieldCurRXFreq.setText(model.vfo_b_frequency.get().toString());
                        break;
                    case SUB:
                        jTextFieldCurRXFreq.setText(model.vfo_sub_frequency.get().toString());
                        break;
                    default:
                        jTextFieldCurRXFreq.setText("");
                        break;
                }
            }

            private void updateSMeter() {
            	if (!radioController.isConnected()){
            		jProgressBarSMeter.setValue(0);
            		jProgressBarSMeter.setString("0");
            		return;
            	}
                jProgressBarSMeter.setValue(model.s_meter.get().intValue());
                jProgressBarSMeter.setString("" + jProgressBarSMeter.getValue());
            }
            private void updatebusySignal() {
            	//if (!radioController.isConnected()) return;
            	
                if (model.busy_signal_status_main.get()== 1)
                    busySignalpanel.setBackground(new java.awt.Color(0, 255, 0));
                else
                    busySignalpanel.setBackground(new java.awt.Color(255, 0, 0));
                                    
            }

            /*private void logTrancieverStatus() {
                radioController.transceiverStatus();
            }*/

            @Override
            public void run() {
                updateTxVfo();
                updateRxVfo();
                updateSMeter();
                updatebusySignal();
                //logTrancieverStatus();
            }
        });
    }

    /** @return the name of the selected port, or <code>null</code> **/
    public CommPortIdentifier getSelectedPort() {
        if (commPortComboBox.getSelectedIndex() > -1) {
            return ((CommPortComboBox) commPortComboBox).getSelectedPort();
        }
        return null;
    }

    public void updateTime(double currentJulianTime) {
        if (dopplerAdj != null) {
            dopplerAdj.updateTime(currentJulianTime);
        }
    }

    public void setCurGs(GroundStation gs) {
        currentGs = gs;
        if (dopplerAdj != null) {
            dopplerAdj.setCurrentGS(gs);
        }
    }

    public void setCurSat(AbstractSatellite sat) {
        currentSat = sat;
        if (dopplerAdj != null) {
            dopplerAdj.setCurrentSatellite(sat);
        }
    }

    public void setBaseFrequency(String freq) {
        if(!freq.contains(".")){
    		freq = new StringBuffer(freq).insert(3, ".").toString();
    		freq = new StringBuffer(freq).insert(7, ".").toString();
    	}
        
    	jTextFieldSetFreq1.setText(freq);
        setFrequencies();
    }

    private void connectBtnActionPerformed(java.awt.event.ActionEvent evt) {                                           
        if (radioController.isConnected()) {
            radioController.disconnect();
            connectBtn.setText("Connect");
        } else {
            if (getSelectedPort() == null) {
                return;
            }
            try {
                radioController.setPort(getSelectedPort());
                radioController.connect();
                radioController.startPolling(10000); //If we wanted to poll
                radioController.setSquelch(0, 50);
                radioController.setAFGain(0, 155);
                radioController.setRFGain(255);
                radioController.setOperationalMode(4);
                SquelchSlider.setValue(50);
                AFGainSlider.setValue(155);
                RFGainSlider.setValue(255);
                connectBtn.setText("Disconnect");
            } catch (Failure f) {
                JOptionPane.showMessageDialog(this, String.format(
                        "Unable to connect (%s)", f.getCause().getMessage()),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }                                           

    private void setFreqBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setFreqBtnActionPerformed
    	setFrequencies();
    }//GEN-LAST:event_setFreqBtnActionPerformed
    
    private void setSchemeBtnActionPerformed(java.awt.event.ActionEvent evt){
    	
    }

    private void setFrequencies() {
        // Get information from the GUI
        if (rxVfoComboBox.getSelectedIndex() < 1
                || txVfoComboBox.getSelectedIndex() < 1) {
            System.out.println("Please select 2 VFO's");
            return;
        }

        MainVfo rxvfo = (MainVfo) rxVfoComboBox.getSelectedItem();
        MainVfo txvfo = (MainVfo) txVfoComboBox.getSelectedItem();
        int freq = Integer.parseInt(jTextFieldSetFreq1.getText().replace(".", ""));

        // Update the doppler adjuster
        if (dopplerAdj != null) {
            dopplerAdj.setReceiveVFO(rxvfo);
            dopplerAdj.setTransmitVFO(txvfo);
            dopplerAdj.setBaseFrequency(freq);
        }

        // Update the actual VFOs
//        if (rxvfo.equals(txvfo)) { // if same, set both as same
            radioController.setRxVfo(rxvfo);
            radioController.setTxVfo(txvfo);
//        } else { // if different, set them as different. silly? probably
//            radioController.split(txvfo);
//        }

        // Set both VFO's to the same frequency for now
        radioController.setVFOFrequency(freq, txvfo);
        if (!rxvfo.equals(txvfo)) // if they are the same VFO, don't set twice.
            radioController.setVFOFrequency(freq, rxvfo);
    }

    private void saveSettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSettingsButtonActionPerformed
        //        tracker.addInternalFrame("Save Settings", new SaveSettingsWindow(parent), null);

        MainVfo txvfo = (MainVfo) txVfoComboBox.getSelectedItem();
        MainVfo rxvfo = (MainVfo) rxVfoComboBox.getSelectedItem();
        if (txvfo != null && rxvfo != null) { // if selected value is a legal argument
            int freq = Integer.parseInt(jTextFieldSetFreq1.getText().replace(".", ""));

            if (dopplerAdj == null) {
                dopplerAdj = new DopplerAdjuster(freq, currentSat, currentGs, radioController, jTextFieldCurTXFreq, jTextFieldCurRXFreq, app.getCurrentJulTime());
            } else {
                dopplerAdj.setCurrentGS(currentGs);
                dopplerAdj.setCurrentSatellite(currentSat);
                dopplerAdj.setBaseFrequency(freq);
            }
            
            dopplerAdj.setTransmitVFO(txvfo);
            dopplerAdj.setReceiveVFO(rxvfo);
        } else {
            JOptionPane.showMessageDialog(this, "Unable to connect/set freq or transmitter not set",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_saveSettingsButtonActionPerformed

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        saveSettingsButton = new javax.swing.JButton();
        bankChannelNumbersPanel = new javax.swing.JPanel();
        SquelchSlider = new javax.swing.JSlider();
        SquelchLabel = new javax.swing.JLabel();
        AFGainLabel = new javax.swing.JLabel();
        AFGainSlider = new javax.swing.JSlider();
        RFGainLabel = new javax.swing.JLabel();
        RFGainSlider = new javax.swing.JSlider();
        AFGainTransceiver = new javax.swing.JComboBox();
        transmitterAndReceiverPanel = new javax.swing.JPanel();
        transmitterLabel1 = new javax.swing.JLabel();
        receiverLabel1 = new javax.swing.JLabel();
        txVfoComboBox = new javax.swing.JComboBox();
        rxVfoComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldSetFreq1 = new javax.swing.JTextField();
        setFreqBtn = new javax.swing.JButton();
        txVfoLbl = new javax.swing.JLabel();
        rxVfoLbl = new javax.swing.JLabel();
//        modComboBox = new javax.swing.JComboBox();
//        modSchemeLbl = new javax.swing.JLabel();
//        setSchemeBtn = new javax.swing.JButton();
        statusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        connectBtn = new javax.swing.JButton();
        commPortComboBox = new CommPortComboBox(CommPortIdentifier.PORT_SERIAL);
        basicPanel = new javax.swing.JPanel();
        OpModeLabel = new javax.swing.JLabel();
        OpModeChoice = new javax.swing.JComboBox();
        PowerStatusLabel = new javax.swing.JLabel();
        PowerStatusChoice = new javax.swing.JComboBox();
        ResetTransceiverLabel = new javax.swing.JLabel();
        ResetTransceiverChoice = new javax.swing.JComboBox();
        InternalTNCModeLabel = new javax.swing.JLabel();
        InternalTNCModeChoice = new javax.swing.JComboBox();
        PowerStatusCheckbox = new javax.swing.JCheckBox();
        ResetTransceiverCheckbox = new javax.swing.JCheckBox();
        currentFrequenciesPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldCurTXFreq = new javax.swing.JTextField();
        jTextFieldCurRXFreq = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        SMeter = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jProgressBarSMeter = new javax.swing.JProgressBar();
        busySignalbox = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        busySignalpanel = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(520, 300));
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(500, 400));

        saveSettingsButton.setText("Apply Settings");
        saveSettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSettingsButtonActionPerformed(evt);
            }
        });

        bankChannelNumbersPanel.setBackground(new java.awt.Color(153, 0, 0));

        SquelchSlider.setMaximum(255);
        SquelchSlider.setValue(125);
        RFGainSlider.setMaximum(255);
        RFGainSlider.setValue(255);
        AFGainSlider.setMaximum(255);
        AFGainSlider.setValue(125);
        SquelchSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                SquelchSliderStateChanged(evt);
            }
        });

        SquelchLabel.setText("Squelch");

        AFGainLabel.setText("AF Gain");

        AFGainSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                AFGainSliderStateChanged(evt);
            }
        });

        RFGainLabel.setText("RF Gain");

        RFGainSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                RFGainSliderStateChanged(evt);
            }
        });

        AFGainTransceiver.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0: Main Transceiver", "1: Sub-Transceiver" }));
        AFGainTransceiver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AFGainTransceiverActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bankChannelNumbersPanelLayout = new javax.swing.GroupLayout(bankChannelNumbersPanel);
        bankChannelNumbersPanel.setLayout(bankChannelNumbersPanelLayout);
        bankChannelNumbersPanelLayout.setHorizontalGroup(
            bankChannelNumbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bankChannelNumbersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bankChannelNumbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(bankChannelNumbersPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(RFGainLabel))
                    .addComponent(SquelchLabel)
                    .addComponent(SquelchSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(RFGainSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(bankChannelNumbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, bankChannelNumbersPanelLayout.createSequentialGroup()
                            .addComponent(AFGainLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(AFGainTransceiver, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(AFGainSlider, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        bankChannelNumbersPanelLayout.setVerticalGroup(
            bankChannelNumbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bankChannelNumbersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SquelchLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SquelchSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bankChannelNumbersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AFGainLabel)
                    .addComponent(AFGainTransceiver, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AFGainSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(RFGainLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(RFGainSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        transmitterAndReceiverPanel.setBackground(new java.awt.Color(149, 30, 30));

        transmitterLabel1.setText(" Transmitter:");

        receiverLabel1.setText("Receiver:");

        txVfoComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Select>" }));

        rxVfoComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Select>" }));

        jLabel1.setText("Base Frequency: ");

        jTextFieldSetFreq1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldSetFreq1.setText("437.405.000");

        setFreqBtn.setText("Set Frequency");
        setFreqBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setFreqBtnActionPerformed(evt);
            }
        });

        txVfoLbl.setText("SUB");

        rxVfoLbl.setText("SUB");

//        modComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<Select>", "1200 AFSK", "9600 GMSK"}));
//
//        modSchemeLbl.setText("Modulation Scheme");
//
//        setSchemeBtn.setText("Set Scheme");
//        setSchemeBtn.addActionListener(new java.awt.event.ActionListener(){
//        	public void actionPerformed(java.awt.event.ActionEvent evt){
//        		setSchemeBtnActionPerformed(evt);
//        	}
//        });

        javax.swing.GroupLayout transmitterAndReceiverPanelLayout = new javax.swing.GroupLayout(transmitterAndReceiverPanel);
        transmitterAndReceiverPanel.setLayout(transmitterAndReceiverPanelLayout);
        transmitterAndReceiverPanelLayout.setHorizontalGroup(
            transmitterAndReceiverPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(transmitterAndReceiverPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(transmitterAndReceiverPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(setFreqBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    //.addComponent(modComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(transmitterAndReceiverPanelLayout.createSequentialGroup()
                        .addGroup(transmitterAndReceiverPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(transmitterAndReceiverPanelLayout.createSequentialGroup()
                                .addComponent(transmitterLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txVfoLbl))
                            .addComponent(txVfoComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(transmitterAndReceiverPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jTextFieldSetFreq1)
                                .addComponent(jLabel1))
                            .addGroup(transmitterAndReceiverPanelLayout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(receiverLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rxVfoLbl))
                            .addComponent(rxVfoComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            //.addComponent(modSchemeLbl))
                        .addGap(0, 0, Short.MAX_VALUE))
                    //.addComponent(setSchemeBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        )));
        transmitterAndReceiverPanelLayout.setVerticalGroup(
            transmitterAndReceiverPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(transmitterAndReceiverPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(transmitterAndReceiverPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(transmitterLabel1)
                    .addComponent(txVfoLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txVfoComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(transmitterAndReceiverPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(receiverLabel1)
                    .addComponent(rxVfoLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rxVfoComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldSetFreq1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(setFreqBtn)
                .addGap(18, 18, 18)
                //.addComponent(modSchemeLbl)
                .addGap(5, 5, 5)
                //.addComponent(modComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                //.addComponent(setSchemeBtn)
                .addContainerGap(54, Short.MAX_VALUE))
        );

        statusPanel.setBackground(new java.awt.Color(255, 0, 0));

        statusLabel.setText("PRIMARY STATUS:");

        connectBtn.setText("Connect");
        connectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                    .addComponent(connectBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                    .addComponent(commPortComboBox, 0, 92, Short.MAX_VALUE))
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(commPortComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                .addComponent(connectBtn)
                .addGap(20, 20, 20))
        );

        basicPanel.setBackground(new java.awt.Color(190, 3, 3));

        OpModeLabel.setText("Op Mode");

        OpModeChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1:LSB", "2:USB", "3:CW", "4:FM", "5:AM", "6:FSK", "7:CR-R", "8:Reserved", "9:FSK-R" }));
        OpModeChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpModeChoiceActionPerformed(evt);
            }
        });

        PowerStatusLabel.setText("Power Status");

        PowerStatusChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0: Off", "1: On" }));
        PowerStatusChoice.setSelectedIndex(1);
        PowerStatusChoice.setToolTipText("");
        PowerStatusChoice.setEnabled(false);
        PowerStatusChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PowerStatusChoiceActionPerformed(evt);
            }
        });

        ResetTransceiverLabel.setText("Reset Transceiver");

        ResetTransceiverChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1: VFO Reset", "2: Master Reset" }));
        ResetTransceiverChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ResetTransceiverChoiceActionPerformed(evt);
            }
        });

        InternalTNCModeLabel.setText("Internal TNC Mode");

        InternalTNCModeChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0: Packet Communication Mode", "1: PC Control Command Mode" }));
        InternalTNCModeChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InternalTNCModeChoiceActionPerformed(evt);
            }
        });

        PowerStatusCheckbox.setEnabled(false);

        javax.swing.GroupLayout basicPanelLayout = new javax.swing.GroupLayout(basicPanel);
        basicPanel.setLayout(basicPanelLayout);
        basicPanelLayout.setHorizontalGroup(
            basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(basicPanelLayout.createSequentialGroup()
                        .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(PowerStatusLabel)
                            .addComponent(OpModeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                        .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(OpModeChoice, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(basicPanelLayout.createSequentialGroup()
                                .addComponent(PowerStatusChoice, 0, 176, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(PowerStatusCheckbox))))
                    .addGroup(basicPanelLayout.createSequentialGroup()
                        .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ResetTransceiverLabel)
                            .addComponent(InternalTNCModeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(basicPanelLayout.createSequentialGroup()
                                .addComponent(ResetTransceiverChoice, 0, 175, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ResetTransceiverCheckbox))
                            .addComponent(InternalTNCModeChoice, 0, 198, Short.MAX_VALUE))))
                .addContainerGap())
        );
        basicPanelLayout.setVerticalGroup(
            basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(OpModeLabel)
                    .addComponent(OpModeChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PowerStatusLabel)
                    .addComponent(PowerStatusChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PowerStatusCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(ResetTransceiverLabel)
                        .addComponent(ResetTransceiverChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(ResetTransceiverCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(InternalTNCModeChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(InternalTNCModeLabel))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        currentFrequenciesPanel.setBackground(new java.awt.Color(204, 0, 0));
        currentFrequenciesPanel.setForeground(new java.awt.Color(255, 255, 255));

        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Current TX Frequency :");

        jTextFieldCurTXFreq.setEditable(false);
        jTextFieldCurTXFreq.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jTextFieldCurTXFreq.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldCurTXFreq.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        jTextFieldCurTXFreq.setSelectionColor(new java.awt.Color(255, 255, 255));

        jTextFieldCurRXFreq.setBackground(new java.awt.Color(212, 208, 200));
        jTextFieldCurRXFreq.setEditable(false);
        jTextFieldCurRXFreq.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jTextFieldCurRXFreq.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Current RX Frequency:");

        javax.swing.GroupLayout currentFrequenciesPanelLayout = new javax.swing.GroupLayout(currentFrequenciesPanel);
        currentFrequenciesPanel.setLayout(currentFrequenciesPanelLayout);
        currentFrequenciesPanelLayout.setHorizontalGroup(
            currentFrequenciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(currentFrequenciesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(currentFrequenciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jTextFieldCurTXFreq, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(currentFrequenciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jTextFieldCurRXFreq, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        currentFrequenciesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jTextFieldCurRXFreq, jTextFieldCurTXFreq});

        currentFrequenciesPanelLayout.setVerticalGroup(
            currentFrequenciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(currentFrequenciesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(currentFrequenciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(currentFrequenciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldCurTXFreq, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextFieldCurRXFreq, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        SMeter.setBackground(new java.awt.Color(102, 0, 0));

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("S-meter");

        jProgressBarSMeter.setForeground(new java.awt.Color(255, 107, 0));
        jProgressBarSMeter.setMaximum(30);
        jProgressBarSMeter.setValue(15);
        jProgressBarSMeter.setString(""+jProgressBarSMeter.getValue());
        jProgressBarSMeter.setStringPainted(true);

        javax.swing.GroupLayout SMeterLayout = new javax.swing.GroupLayout(SMeter);
        SMeter.setLayout(SMeterLayout);
        SMeterLayout.setHorizontalGroup(
            SMeterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SMeterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SMeterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel2)
                    .addComponent(jProgressBarSMeter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        SMeterLayout.setVerticalGroup(
            SMeterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SMeterLayout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jProgressBarSMeter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        busySignalbox.setBackground(new java.awt.Color(149, 30, 30));

        jLabel7.setText("Busy Status");

        busySignalpanel.setBackground(new java.awt.Color(0, 255, 0));

        javax.swing.GroupLayout busySignalpanelLayout = new javax.swing.GroupLayout(busySignalpanel);
        busySignalpanel.setLayout(busySignalpanelLayout);
        busySignalpanelLayout.setHorizontalGroup(
            busySignalpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 18, Short.MAX_VALUE)
        );
        busySignalpanelLayout.setVerticalGroup(
            busySignalpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout busySignalboxLayout = new javax.swing.GroupLayout(busySignalbox);
        busySignalbox.setLayout(busySignalboxLayout);
        busySignalboxLayout.setHorizontalGroup(
            busySignalboxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(busySignalboxLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addComponent(busySignalpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );
        busySignalboxLayout.setVerticalGroup(
            busySignalboxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(busySignalboxLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(busySignalboxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(busySignalpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bankChannelNumbersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(saveSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(busySignalbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(statusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(basicPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(currentFrequenciesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(SMeter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(transmitterAndReceiverPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(currentFrequenciesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(SMeter, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(basicPanel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(statusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(10, 10, 10)
                        .addComponent(bankChannelNumbersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(transmitterAndReceiverPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(saveSettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(busySignalbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void AFGainTransceiverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AFGainTransceiverActionPerformed
        // do nothing
    }//GEN-LAST:event_AFGainTransceiverActionPerformed

    private void OpModeChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpModeChoiceActionPerformed
        updateOPMode();
    }//GEN-LAST:event_OpModeChoiceActionPerformed

    private void PowerStatusChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PowerStatusChoiceActionPerformed
        updatePowerStatus();
    }//GEN-LAST:event_PowerStatusChoiceActionPerformed

    private void ResetTransceiverChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ResetTransceiverChoiceActionPerformed
        updateResetTransceiver();
    }//GEN-LAST:event_ResetTransceiverChoiceActionPerformed

    private void InternalTNCModeChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InternalTNCModeChoiceActionPerformed
        updateInternalTNCMode();
    }//GEN-LAST:event_InternalTNCModeChoiceActionPerformed

    private void SquelchSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_SquelchSliderStateChanged
    	radioController.setSquelch(0, SquelchSlider.getValue());
        SquelchLabel.setText("Squelch: " + SquelchSlider.getValue());
    }//GEN-LAST:event_SquelchSliderStateChanged

    private void AFGainSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_AFGainSliderStateChanged
    	radioController.setAFGain(AFGainTransceiver.getSelectedIndex(), AFGainSlider.getValue());
        AFGainLabel.setText("AF Gain: " + AFGainSlider.getValue());
    }//GEN-LAST:event_AFGainSliderStateChanged

    private void RFGainSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_RFGainSliderStateChanged
    	radioController.setRFGain(RFGainSlider.getValue());
        RFGainLabel.setText("RF Gain: " + RFGainSlider.getValue());
    }//GEN-LAST:event_RFGainSliderStateChanged

    private void updateOPMode() {
        radioController.setOperationalMode(OpModeChoice.getSelectedIndex() + 1);
    	radioController.setRxVfo((MainVfo) rxVfoComboBox.getSelectedItem());
    	radioController.setTxVfo((MainVfo) txVfoComboBox.getSelectedItem());
        //+1 because index starts at 0 and the Op modes start from 1.
    }
        
    private void updatePowerStatus() {
        if (PowerStatusCheckbox.isSelected())
                radioController.setPowerStatus(PowerStatusChoice.getSelectedIndex());
    }

    private void updateResetTransceiver() {
        if (ResetTransceiverCheckbox.isSelected())
            radioController.setResetTransceiver(ResetTransceiverChoice.getSelectedIndex() + 1);
        //+1 because index starts at 0 and the options for reset transceiver start from 1.
    }

    private void updateInternalTNCMode() {
        radioController.setInternalTNCMode(InternalTNCModeChoice.getSelectedIndex());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel AFGainLabel;
    private javax.swing.JSlider AFGainSlider;
    private javax.swing.JComboBox AFGainTransceiver;
    private javax.swing.JComboBox InternalTNCModeChoice;
    private javax.swing.JLabel InternalTNCModeLabel;
    private javax.swing.JComboBox OpModeChoice;
    private javax.swing.JLabel OpModeLabel;
    private javax.swing.JCheckBox PowerStatusCheckbox;
    private javax.swing.JComboBox PowerStatusChoice;
    private javax.swing.JLabel PowerStatusLabel;
    private javax.swing.JLabel RFGainLabel;
    private javax.swing.JSlider RFGainSlider;
    private javax.swing.JCheckBox ResetTransceiverCheckbox;
    private javax.swing.JComboBox ResetTransceiverChoice;
    private javax.swing.JLabel ResetTransceiverLabel;
    private javax.swing.JPanel SMeter;
    private javax.swing.JLabel SquelchLabel;
    private javax.swing.JSlider SquelchSlider;
    private javax.swing.JPanel bankChannelNumbersPanel;
    private javax.swing.JPanel basicPanel;
    private javax.swing.JPanel busySignalbox;
    private javax.swing.JPanel busySignalpanel;
    private javax.swing.JComboBox commPortComboBox;
    private javax.swing.JButton connectBtn;
    private javax.swing.JPanel currentFrequenciesPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JProgressBar jProgressBarSMeter;
    private javax.swing.JTextField jTextFieldCurRXFreq;
    private javax.swing.JTextField jTextFieldCurTXFreq;
    private javax.swing.JTextField jTextFieldSetFreq1;
//    private javax.swing.JComboBox modComboBox;
//    private javax.swing.JLabel modSchemeLbl;
    private javax.swing.JLabel receiverLabel1;
    private javax.swing.JComboBox rxVfoComboBox;
    private javax.swing.JLabel rxVfoLbl;
    private javax.swing.JButton saveSettingsButton;
    private javax.swing.JButton setFreqBtn;
//    private javax.swing.JButton setSchemeBtn;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JPanel transmitterAndReceiverPanel;
    private javax.swing.JLabel transmitterLabel1;
    private javax.swing.JComboBox txVfoComboBox;
    private javax.swing.JLabel txVfoLbl;
    // End of variables declaration//GEN-END:variables
    /** This window's title **/
    public static final String TITLE = "TS2000";
}
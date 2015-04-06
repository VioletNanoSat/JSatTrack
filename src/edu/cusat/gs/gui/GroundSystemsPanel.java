package edu.cusat.gs.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTabbedPane;

import jsattrak.gui.JSatTrak;
import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.GroundStation;
import jsattrak.utilities.GsListener;
import jsattrak.utilities.SatListener;
import edu.cusat.gs.util.AutoTrack;

/**
 * The root of all Triton GUI Panels
 */
public class GroundSystemsPanel extends javax.swing.JPanel implements
		GsListener, SatListener {

    private static final long serialVersionUID = -3085353172907920050L;

	// HACK! Putting in the main frequencies of satellites we care about
    private static final Map<String, String> satFrequencies = new HashMap<String, String>();
    static {
    	satFrequencies.put("FAST1",						"437345000");
    	satFrequencies.put("FAST2",						"145825000");
    	satFrequencies.put("OOREOS",					"437305000");
    	satFrequencies.put("RAX", 						"437505000");
    	satFrequencies.put("RAX-2", 					"437345000");
    	satFrequencies.put("ITUpSAT1",					"437325000");
        satFrequencies.put("AO-27",                    	"436795000");
        satFrequencies.put("BEESAT",                   	"436000000");
        satFrequencies.put("CO-66",                    	"437485000");
        satFrequencies.put("CO-55",                    	"437400000");
        satFrequencies.put("CO-56",                    	"437385000");
        satFrequencies.put("CO-56",                    	"437385000");
        satFrequencies.put("CO-57",                    	"437490000");
        satFrequencies.put("CO-58",                    	"437345000");
        satFrequencies.put("CO-65",                    	"437475000");
        satFrequencies.put("GO-32",                    	"435225000");
        satFrequencies.put("ISS (ZARYA)",			   	"437800000");
        satFrequencies.put("ITUPSAT1",                 	"437325000");
        satFrequencies.put("RS-30",                    	"435215000");
        satFrequencies.put("SO-50",                    	"436795000");
        satFrequencies.put("SUMBDILA",                 	"435350000");
        satFrequencies.put("SwissCube",                	"437505000");
        satFrequencies.put("UWE-2",                    	"437385000");
        satFrequencies.put("CUSAT",        		        "437405000");
        satFrequencies.put("CUSAT-TOP",                 "437405000");
        satFrequencies.put("CUSAT-BOT",                 "437405000");
    }

    /**
     * The main application. A reference is needed to access certain other
     * components, but those should really be passed in if possible so that this
     * reference isn't required.
     */
    private final JSatTrak app;

    /** Whether or not we're automatically selecting the satellite to track **/
    private boolean autoTrack;

    /** Currently set Ground Station **/
    private GroundStation curGs; 

    /** Satellite being currently being tracked **/
    private AbstractSatellite curSat; 

    /** Creates new GroundSystemsPanel **/
    public GroundSystemsPanel(final JSatTrak app) {
        this.app = app;

        initComponents();
        initCustomComponents(app);
        
        setVisible(true);
    }

    /**
     * Notifies various fields in GroundStationPanel of updated time in JSatTrak
     */
	public void updateTime(double curJulTime) {
        if (autoTrack)
            setCurSat(AutoTrack.findOverheadSat(curGs, curJulTime,
                    app.satellites()));
        ts2000ControlPanel.updateTime(curJulTime);
        rotatorControlPanel.updateTime();
    }
    
    /**
     * Sets the satellite being tracked by this and all of the panels it
     * contains
     * 
     * @param sat
     *            Satellite to track
     */
	public void setCurSat(AbstractSatellite sat) {
        curSat = sat;

        if (sat != null) {
            curSatTxtFld.setText(sat.getName());

            // HACK! Setting the base frequency
            if (satFrequencies.containsKey(sat.getName().trim())) {
                ts2000ControlPanel.setBaseFrequency(
                        satFrequencies.get(sat.getName().trim()));
            }
        }
        else {
            curSatTxtFld.setText("");
        }

        ts2000ControlPanel.setCurSat(sat);
        rotatorControlPanel.setCurSat(sat);
    }

//***************************** Action Listeners ****************************//
	
    /** Sets the currently tracked satellite **/
    private void setSatBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setSatBtnActionPerformed
        String satName = (String)satChooser.getSelectedItem();
        
        if(autoTrack = "Auto".equals(satName)) {
            setCurSat(AutoTrack.findOverheadSat(curGs, app.getCurrentJulTime(), app.satellites()));
        }
        setCurSat(app.getSatellite(satName));
    }//GEN-LAST:event_setSatBtnActionPerformed
    
    /** Assigns a new ground station based on the combo box **/
    private void setGsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setGsBtnActionPerformed
        curGs = app.getGs((String)gsChooser.getSelectedItem());
        
        curGsTxtFld.setText(curGs.getStationName());
        
//        rfGatewayPanel.setCurGs(curGs);
        ts2000ControlPanel.setCurGs(curGs);
        rotatorControlPanel.setCurGs(curGs);
        //Adjust the tracking GUI
        curGsTxtFld.setText((String) gsChooser.getSelectedItem());
    }//GEN-LAST:event_setGsBtnActionPerformed

//********************************** Getters *********************************// 
    
    /** @return the main JSatTrak app **/
    public JSatTrak getJSatTrak() { return app; }
    
    /** @return the satellite being tracked at the moment **/
    public AbstractSatellite getCurSat() { return curSat; }

    /** @return the ground station we are at the moment **/
    public GroundStation getCurGs() { return curGs; }
    
//********************** GS and Sat Listener Functions ***********************//
    
    @Override
    public void gsAdded(GroundStation gs) {
        gsChooser.addItem(gs.getStationName());
    }

    @Override
    public void gsRemoved(GroundStation gs) {
        gsChooser.removeItem(gs.getStationName());
    }
    
    @Override
    public void satAdded(AbstractSatellite sat) {
        satChooser.addItem(sat.getName());
    }
    
    @Override
    public void satRemoved(AbstractSatellite sat) {
        satChooser.removeItem(sat.getName());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabPane = new javax.swing.JTabbedPane();
        satTrackingPnl = new javax.swing.JPanel();
        setSatBtn = new javax.swing.JButton();
        satChooser = new javax.swing.JComboBox<String>();
        curSatLbl = new javax.swing.JLabel();
        curSatTxtFld = new javax.swing.JTextField();
        gsTrackingPnl = new javax.swing.JPanel();
        setGsBtn = new javax.swing.JButton();
        gsChooser = new javax.swing.JComboBox<String>();
        curGsLbl = new javax.swing.JLabel();
        curGsTxtFld = new javax.swing.JTextField();

        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setPreferredSize(new java.awt.Dimension(645, 650));

        tabPane.setPreferredSize(new java.awt.Dimension(600, 550));

        satTrackingPnl.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        satTrackingPnl.setPreferredSize(new java.awt.Dimension(250, 76));

        setSatBtn.setText("Set Satellite");
        setSatBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setSatBtnActionPerformed(evt);
            }
        });

        ArrayList<AbstractSatellite> satellites = new ArrayList<AbstractSatellite>(app.satellites());
        String [] satNames = new String[satellites.size()+1];
        satNames[0] = "Auto";
        for(int i = 1;i < satNames.length;i++) {
            satNames[i] = satellites.get(i-1).getName();
        }
        satChooser.setModel(new javax.swing.DefaultComboBoxModel<String>(satNames));

        curSatLbl.setBackground(new java.awt.Color(0, 0, 0));
        curSatLbl.setFont(new java.awt.Font("Tahoma", 0, 12));
        curSatLbl.setText("Currently Tracking:");

        curSatTxtFld.setEditable(false);

        javax.swing.GroupLayout satTrackingPnlLayout = new javax.swing.GroupLayout(satTrackingPnl);
        satTrackingPnl.setLayout(satTrackingPnlLayout);
        satTrackingPnlLayout.setHorizontalGroup(
            satTrackingPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(satTrackingPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(satTrackingPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(satTrackingPnlLayout.createSequentialGroup()
                        .addComponent(satChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setSatBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                    .addGroup(satTrackingPnlLayout.createSequentialGroup()
                        .addComponent(curSatLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(curSatTxtFld, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)))
                .addContainerGap())
        );
        satTrackingPnlLayout.setVerticalGroup(
            satTrackingPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(satTrackingPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(satTrackingPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(satChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(setSatBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(satTrackingPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(curSatLbl)
                    .addComponent(curSatTxtFld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gsTrackingPnl.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gsTrackingPnl.setPreferredSize(new java.awt.Dimension(250, 76));

        setGsBtn.setText("Set Ground Station");
        setGsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setGsBtnActionPerformed(evt);
            }
        });

        ArrayList<GroundStation> gss = new ArrayList<GroundStation>(app.groundStations());
        String [] gsNames = new String[gss.size()];
        for(int i = 0;i < gsNames.length;i++)
        {
            gsNames[i] = gss.get(i).getStationName();
        }
        gsChooser.setModel(new javax.swing.DefaultComboBoxModel<String>(gsNames));

        curGsLbl.setFont(new java.awt.Font("Tahoma", 0, 12));
        curGsLbl.setText("Current Station:");

        curGsTxtFld.setEditable(false);

        javax.swing.GroupLayout gsTrackingPnlLayout = new javax.swing.GroupLayout(gsTrackingPnl);
        gsTrackingPnl.setLayout(gsTrackingPnlLayout);
        gsTrackingPnlLayout.setHorizontalGroup(
            gsTrackingPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gsTrackingPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gsTrackingPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(gsTrackingPnlLayout.createSequentialGroup()
                        .addComponent(gsChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setGsBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE))
                    .addGroup(gsTrackingPnlLayout.createSequentialGroup()
                        .addComponent(curGsLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(curGsTxtFld, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)))
                .addContainerGap())
        );
        gsTrackingPnlLayout.setVerticalGroup(
            gsTrackingPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gsTrackingPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gsTrackingPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gsChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(setGsBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(gsTrackingPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(curGsTxtFld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(curGsLbl))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(gsTrackingPnl, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(satTrackingPnl, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(tabPane, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(tabPane, javax.swing.GroupLayout.PREFERRED_SIZE, 550, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(gsTrackingPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(satTrackingPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel curGsLbl;
    private javax.swing.JTextField curGsTxtFld;
    private javax.swing.JLabel curSatLbl;
    private javax.swing.JTextField curSatTxtFld;
    private javax.swing.JComboBox<String> gsChooser;
    private javax.swing.JPanel gsTrackingPnl;
    private javax.swing.JComboBox<String> satChooser;
    private javax.swing.JPanel satTrackingPnl;
    private javax.swing.JButton setGsBtn;
    private javax.swing.JButton setSatBtn;
    private javax.swing.JTabbedPane tabPane;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Addition of other GUI panels has to happen separately because NetBeans
     * won't cooperate
     * @param app TODO
     */
    private void initCustomComponents(JSatTrak app) {
        ts2000ControlPanel = new Ts2000ControlPanel(app);
        tabPane.addTab(Ts2000ControlPanel.TITLE, ts2000ControlPanel);

        rotatorControlPanel = new RotatorControlPanel(curSat, curGs);
        tabPane.addTab(RotatorControlPanel.TITLE, rotatorControlPanel);

        rfGatewayPanel = new RfGatewayPanel(this);
        tabPane.addTab(RfGatewayPanel.TITLE, rfGatewayPanel);

    }
    
    public Ts2000ControlPanel getTS2000Panel(){
    	return ts2000ControlPanel;
    }
    
	private RfGatewayPanel rfGatewayPanel;
    private Ts2000ControlPanel ts2000ControlPanel;
    private RotatorControlPanel rotatorControlPanel;

    /** This window's title **/
    public static final String TITLE = "Triton";

	public JTabbedPane getTabbedPane() { return tabPane; }
}

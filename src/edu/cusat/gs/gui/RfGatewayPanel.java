package edu.cusat.gs.gui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import jsattrak.gui.CommPortComboBox;
import jsattrak.utilities.Bytes;
import jsattrak.utilities.LafChanger;

import org.jdesktop.swingx.combobox.EnumComboBoxModel;

import edu.cusat.gs.rfgateway.Callsign;
import edu.cusat.gs.rfgateway.GatewayMgr;
import edu.cusat.gs.rfgateway.GatewayMgrListener;
import edu.cusat.gs.rfgateway.GatewayMgr.ServerStatus;
import edu.cusat.gs.rfgateway.OperatorCallsignSender;
import edu.cusat.gs.rfgateway.TncSerialProvider;
import edu.cusat.gs.rfgateway.TncSerialProvider.Type;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

public class RfGatewayPanel extends javax.swing.JPanel implements GatewayMgrListener {

	private static final long serialVersionUID = 1L;

	public static final String DIRECTORY = "data/packets";

    private GatewayMgr gwyMgr = new GatewayMgr();

    /** Creates new form RfGatewayPanel */
    @SuppressWarnings("unchecked")
	public RfGatewayPanel() {
        initComponents();
        hardwareTypeSelect.setModel(new EnumComboBoxModel<Type>(Type.class));
        gwyMgr.registerListener(this);
    }

    public RfGatewayPanel(GroundSystemsPanel gsp) {
    	this();
    	gwyMgr.setJSatTrak(gsp.getJSatTrak());
    	gwyMgr.setTabbedPane(gsp.getTabbedPane());
    }

    public static void main(String[] args) {
        // TODO Set look and feel and default size.
        JFrame frame = new JFrame("RF Gateway Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(650, 650);
        /**Look and Feel*/
        String laf = "org.jvnet.substance.skin.Substance" + "Raven"
                + "LookAndFeel";
        LafChanger.changeLaf(frame, laf);

        JTabbedPane tpane = new JTabbedPane();
        frame.add(tpane);
        RfGatewayPanel rfgp = new RfGatewayPanel();
        rfgp.gwyMgr.setTabbedPane(tpane);
        tpane.add("RF Gateway", rfgp);
//		rfgp.simConnectBtn.doClick();
//		rfgp.openBtn.doClick();
//		rfgp.callTxt.setText("BOTTOM");
//		rfgp.cmdPortTxt.setText("3332");
//		rfgp.tlmPortTxt.setText("4443");
//		rfgp.openBtn.doClick();
        frame.setVisible(true);

    }

    @Override
    public void eventOccurred(String message) {
    	gatewayEventsBox.append(message + "\n");

    	refreshList(waitingList, ServerStatus.WAITING);
    	refreshList(runningList, ServerStatus.RUNNING);
    }

    private void refreshList(JList<String> list, ServerStatus stat) {
    	DefaultListModel<String> m = new DefaultListModel<String>();
    	String[] gwys = gwyMgr.allServersThatAre(stat).toArray(new String[0]);
    	for (int i = 0; i < gwys.length; i++) m.add(i, gwys[i]);
    	list.setModel(m);
    }

    private void tncConnectBtnActionPerformed(java.awt.event.ActionEvent evt) {
        // Connecting or disconnecting?
    	if(gwyMgr.tncStatus() == ServerStatus.DEAD) {
    		// Figure out what kind of TNC we should connect to
    		cpi = ((CommPortComboBox)hardwarePortSelect).getSelectedPort();
    		if(cpi == null){
    			JOptionPane.showMessageDialog(this, "Please select a COM port", "Error", ERROR);
    		}
    		else if(gwyMgr.connectToTNC(cpi, (Type)hardwareTypeSelect.getSelectedItem())) {
    			tncConnectionStat.setText("Status: Connected to hardware");
    			tncConnectBtn.setText("Disconnect from TNC");
    			tncConnectBtn.setBackground(Color.red);
    		}
    	} else {
    		gwyMgr.disconnectTNC();
    		tncConnectionStat.setText("Status: Not connected");
    		tncConnectBtn.setText("Connect to TNC");
    		tncConnectBtn.setBackground(Color.gray);
    	}
    }

    private void simConnectBtnActionPerformed(java.awt.event.ActionEvent evt) {
    	if(gwyMgr.simStatus() == ServerStatus.DEAD) {
			if (gwyMgr.connectToSim(
					Integer.parseInt(simTopCmdPort.getText()),
					Integer.parseInt(simTopTlmPort.getText()),
					Integer.parseInt(simBotCmdPort.getText()),
					Integer.parseInt(simBotTlmPort.getText()))) {
				simConnectionStat.setText("Status: Connected to simulator");
				simConnectBtn.setText("Disconnect from Simulator");
				simConnectBtn.setBackground(Color.red);
			} else {
				JOptionPane.showMessageDialog(this, "Error Connecting. See log for details",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
    		gwyMgr.disconnectSim();
    		simConnectionStat.setText("Status: Not connected");
    		simConnectBtn.setText("Connect to Simulator");
    		simConnectBtn.setBackground(Color.gray);
    	}
    }

//    private void setRBtnsEnabled(boolean enabled) {
//    	hardwareRBtn.setEnabled(enabled);
//    	simRBtn.setEnabled(enabled);
//    	if(!enabled) {
//    		setHwOptionsEnabled(false);
//    		setSimOptionsEnabled(false);
//    	} else {
//    		setHwOptionsEnabled(hardwareRBtn.isSelected());
//        	setSimOptionsEnabled(simRBtn.isSelected());
//    	}
//    }
//
//    private void setHwOptionsEnabled(boolean enabled) {
//    	hardwarePortSelect.setEnabled(enabled);
//    	hardwareTypeSelect.setEnabled(enabled);
//    }
//
//    private void setSimOptionsEnabled(boolean enabled) {
//    	simTopCmdPort.setEnabled(enabled);
//        simTopTlmPort.setEnabled(enabled);
//        simBotCmdPort.setEnabled(enabled);
//        simBotTlmPort.setEnabled(enabled);
//        crxPort.setEnabled(enabled);
//	}

    private void openBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openBtnActionPerformed
        try{
        	gwyMgr.openServer(new Callsign(callTxt.getText(),
                Byte.parseByte(ssidTxt.getText())),
                Integer.parseInt(cmdPortTxt.getText()),
                Integer.parseInt(tlmPortTxt.getText()));
        } catch(NumberFormatException e) {
        	JOptionPane.showMessageDialog(this, "SSID and ports must all be numbers", "Error", ERROR);
        }
    }//GEN-LAST:event_openBtnActionPerformed

    @SuppressWarnings("deprecation")
	private void closeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeBtnActionPerformed
    	Pattern gwyPattern = Pattern.compile("(\\w+)(\\d+).*");
        for (Object waiter : waitingList.getSelectedValues()) {
			Matcher m = gwyPattern.matcher(waiter.toString());
			if(m.matches())
				gwyMgr.closeServer(new Callsign(m.group(1), Byte.parseByte(m.group(2))));
		}
        for (Object runner : runningList.getSelectedValues()) {
			Matcher m = gwyPattern.matcher((String)runner);
			if(m.matches())
				gwyMgr.closeServer(new Callsign(m.group(1), Byte.parseByte(m.group(2))));
		}
    }//GEN-LAST:event_closeBtnActionPerformed

	@SuppressWarnings("deprecation")
	private void callSignBroadcastCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_callSignBroadcastCheckBoxActionPerformed
		System.out.println(evt.getActionCommand());
		if(callSignBroadcastCheckBox.isSelected()){
			callsignCheckBoxThread = new CallsignBroadcastCheckBoxImplementation(evt);
			callsignCheckBoxThread.start();
		} else{
			callsignCheckBoxThread.stop();
		}
	}//GEN-LAST:event_callSignBroadcastCheckBoxActionPerformed

	private void ax25PacketRButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ax25PacketRButtonActionPerformed
	}//GEN-LAST:event_ax25PacketRButtonActionPerformed

	private void setOperatorCallsignBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setOperatorCallsignBtnActionPerformed
		OperatorCallsignSender sender = new OperatorCallsignSender(this, gwyMgr);
		sender.setOperatorCallSign(callsignPacketTextField.getText(), Byte.parseByte(callsignPacketIdTextField.getText()));
		sender.setEnabled(true);
		sender.enableAx25Option();
		sender.notifySender();
	}//GEN-LAST:event_setOperatorCallsignBtnActionPerformed
	
	private void setModSchemeBtnActionPerformed(java.awt.event.ActionEvent evt) {
		//fill in this method appropriately
	}

	
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        callSignSenderTypeBtnGrp = new javax.swing.ButtonGroup();
        hardwareSimGroup = new javax.swing.ButtonGroup();
        tncManager = new javax.swing.JPanel();
        javax.swing.JLabel tncPortLbl = new javax.swing.JLabel();
        hardwarePortSelect = new CommPortComboBox(CommPortIdentifier.PORT_SERIAL);
        javax.swing.JLabel tncTypeLbl = new javax.swing.JLabel();
        hardwareTypeSelect = new javax.swing.JComboBox();
        javax.swing.JLabel simCmdPortLbl = new javax.swing.JLabel();
        simTopCmdPort = new javax.swing.JTextField();
        javax.swing.JLabel simTlmPortLbl = new javax.swing.JLabel();
        simTopTlmPort = new javax.swing.JTextField();
        tncConnectBtn = new javax.swing.JButton();
        tncConnectionStat = new javax.swing.JLabel();
        simBotCmdPort = new javax.swing.JTextField();
        simBotTlmPort = new javax.swing.JTextField();
        simConnectBtn = new javax.swing.JButton();
        simConnectionStat = new javax.swing.JLabel();
        testTxBtn = new javax.swing.JButton();
        resetTNC = new javax.swing.JButton();
        testRxBtn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        tcpListenerManager = new javax.swing.JPanel();
        eventsScrollPane = new javax.swing.JScrollPane();
        gatewayEventsBox = new javax.swing.JTextArea();
        satTcpPanel = new javax.swing.JPanel();
        setupPnl = new javax.swing.JPanel();
        callLbl = new javax.swing.JLabel();
        callTxt = new javax.swing.JTextField();
        ssidLbl = new javax.swing.JLabel();
        ssidTxt = new javax.swing.JTextField();
        cmdPort = new javax.swing.JLabel();
        cmdPortTxt = new javax.swing.JTextField();
        tlmPort = new javax.swing.JLabel();
        tlmPortTxt = new javax.swing.JTextField();
        openBtn = new javax.swing.JButton();
        closeBtn = new javax.swing.JButton();
        runningScrollPane = new javax.swing.JScrollPane();
        runningList = new javax.swing.JList();
        waitingScrollPane = new javax.swing.JScrollPane();
        waitingList = new javax.swing.JList();
        waitingLabel = new javax.swing.JLabel();
        runningLabel = new javax.swing.JLabel();
        statusPnl = new javax.swing.JPanel();
        broadcastPanel = new javax.swing.JPanel();
        callSignBroadcastCheckBox = new javax.swing.JCheckBox();
        callsignPacketTextField = new javax.swing.JTextField(6);
        javax.swing.JLabel operatorCallsignLabel = new javax.swing.JLabel();
        callsignPacketIdTextField = new javax.swing.JTextField("0");
        ax25PacketRButton = new javax.swing.JRadioButton();
        morseCodeRButton = new javax.swing.JRadioButton();
        setOperatorCallsignBtn = new javax.swing.JButton();
        
        modComboBox = new javax.swing.JComboBox();
        modSchemeLbl = new javax.swing.JLabel();
        setSchemeBtn = new javax.swing.JButton();
        

        setPreferredSize(new java.awt.Dimension(600, 550));

        tncManager.setBackground(new java.awt.Color(180, 100, 250));

        tncPortLbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        tncPortLbl.setText("Port");

        tncTypeLbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        tncTypeLbl.setText("Type");

        simCmdPortLbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        simCmdPortLbl.setText("Cmd Port");

        simTopCmdPort.setText("3333");
        simTopCmdPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simTopCmdPortActionPerformed(evt);
            }
        });

        simTlmPortLbl.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        simTlmPortLbl.setText("Tlm Port");

        simTopTlmPort.setText("4444");

        tncConnectBtn.setText("Connect to TNC");
        tncConnectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tncConnectBtnActionPerformed(evt);
            }
        });

        tncConnectionStat.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        tncConnectionStat.setText("Status: Not Connected");

        simBotCmdPort.setText("3332");

        simBotTlmPort.setText("4443");

        simConnectBtn.setText("Connect to Simulator");
        simConnectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simConnectBtnActionPerformed(evt);
            }
        });

        simConnectionStat.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        simConnectionStat.setText("Status: Not Connected");

        testTxBtn.setText("TX Something");
        testTxBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testTxBtnActionPerformed(evt);
            }
        });

        resetTNC.setText("Reset TNC");
        resetTNC.setMaximumSize(new java.awt.Dimension(97, 23));
        resetTNC.setMinimumSize(new java.awt.Dimension(97, 23));
        resetTNC.setPreferredSize(new java.awt.Dimension(99, 23));
        resetTNC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetTNCActionPerformed(evt);
            }
        });

        testRxBtn.setText("RX Something");
        testRxBtn.setMaximumSize(new java.awt.Dimension(97, 23));
        testRxBtn.setMinimumSize(new java.awt.Dimension(97, 23));
        testRxBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testRxBtnActionPerformed(evt);
            }
        });
       
    
        jLabel1.setText("Simulator Connection");

        javax.swing.GroupLayout tncManagerLayout = new javax.swing.GroupLayout(tncManager);
        tncManager.setLayout(tncManagerLayout);
        tncManagerLayout.setHorizontalGroup(
            tncManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tncManagerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tncManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tncManagerLayout.createSequentialGroup()
                        .addGroup(tncManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(tncManagerLayout.createSequentialGroup()
                                .addGroup(tncManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tncTypeLbl)
                                    .addComponent(tncPortLbl))
                                .addGap(27, 27, 27)
                                .addGroup(tncManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(hardwareTypeSelect, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(hardwarePortSelect, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(tncConnectBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(10, 10, 10)
                        .addGroup(tncManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(resetTNC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(testRxBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(testTxBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(tncConnectionStat, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(56, 56, 56)
                .addGroup(tncManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(tncManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(simConnectionStat, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(tncManagerLayout.createSequentialGroup()
                            .addComponent(simCmdPortLbl)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(simTopCmdPort, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(simBotCmdPort, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(simConnectBtn))
                    .addGroup(tncManagerLayout.createSequentialGroup()
                        .addComponent(simTlmPortLbl)
                        .addGap(8, 8, 8)
                        .addComponent(simTopTlmPort, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(simBotTlmPort, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tncManagerLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {hardwarePortSelect, hardwareTypeSelect, simTopTlmPort});

        tncManagerLayout.setVerticalGroup(
            tncManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tncManagerLayout.createSequentialGroup()
                .addGap(0, 11, Short.MAX_VALUE)
                .addGroup(tncManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(tncManagerLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tncManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(simTopTlmPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(simTlmPortLbl)
                            .addComponent(simBotTlmPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tncManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(simTopCmdPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(simCmdPortLbl)
                            .addComponent(simBotCmdPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(simConnectBtn)
                        .addGap(2, 2, 2)
                        .addComponent(simConnectionStat))
                    .addGroup(tncManagerLayout.createSequentialGroup()
                        .addGroup(tncManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tncPortLbl)
                            .addComponent(hardwarePortSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(testTxBtn))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tncManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tncTypeLbl)
                            .addComponent(hardwareTypeSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(testRxBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(tncManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tncConnectBtn)
                            .addComponent(resetTNC, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tncConnectionStat)))
                .addGap(7, 7, 7))
        );

        tcpListenerManager.setBackground(new java.awt.Color(204, 0, 204));

        gatewayEventsBox.setColumns(20);
        gatewayEventsBox.setEditable(false);
        gatewayEventsBox.setRows(5);
        eventsScrollPane.setViewportView(gatewayEventsBox);

        callLbl.setText("Callsign");

        callTxt.setText("TOPTOP");

        ssidLbl.setText("SSID");

        ssidTxt.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        ssidTxt.setText("0");

        cmdPort.setText("Cmd Port");

        cmdPortTxt.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        cmdPortTxt.setText("3333");

        tlmPort.setText("Tlm Port");

        tlmPortTxt.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        tlmPortTxt.setText("4444");

        openBtn.setText("Open");
        openBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openBtnActionPerformed(evt);
            }
        });

        closeBtn.setText("Close");
        closeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeBtnActionPerformed(evt);
            }
        });

        runningScrollPane.setViewportView(runningList);

        waitingScrollPane.setViewportView(waitingList);

        waitingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        waitingLabel.setText("Waiting");

        runningLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        runningLabel.setText("Running");

        javax.swing.GroupLayout setupPnlLayout = new javax.swing.GroupLayout(setupPnl);
        setupPnl.setLayout(setupPnlLayout);
        setupPnlLayout.setHorizontalGroup(
            setupPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setupPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(setupPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(setupPnlLayout.createSequentialGroup()
                        .addGroup(setupPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(setupPnlLayout.createSequentialGroup()
                                .addComponent(callLbl)
                                .addGap(12, 12, 12))
                            .addGroup(setupPnlLayout.createSequentialGroup()
                                .addComponent(cmdPort, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(setupPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(callTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, setupPnlLayout.createSequentialGroup()
                                .addComponent(cmdPortTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tlmPort)))
                        .addGroup(setupPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(setupPnlLayout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(ssidLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ssidTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(setupPnlLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tlmPortTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, setupPnlLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(openBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(closeBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(setupPnlLayout.createSequentialGroup()
                        .addGroup(setupPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(waitingScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                            .addComponent(waitingLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(setupPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(runningScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                            .addComponent(runningLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))))
                .addContainerGap())
        );
        setupPnlLayout.setVerticalGroup(
            setupPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setupPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(setupPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(callLbl)
                    .addComponent(callTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ssidTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ssidLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(setupPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cmdPort)
                    .addComponent(tlmPort)
                    .addComponent(cmdPortTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tlmPortTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(setupPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openBtn)
                    .addComponent(closeBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(setupPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(waitingLabel)
                    .addComponent(runningLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(setupPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(waitingScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addComponent(runningScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        setupPnlLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {callTxt, ssidTxt});

        javax.swing.GroupLayout statusPnlLayout = new javax.swing.GroupLayout(statusPnl);
        statusPnl.setLayout(statusPnlLayout);
        statusPnlLayout.setHorizontalGroup(
            statusPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 249, Short.MAX_VALUE)
        );
        statusPnlLayout.setVerticalGroup(
            statusPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 199, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout satTcpPanelLayout = new javax.swing.GroupLayout(satTcpPanel);
        satTcpPanel.setLayout(satTcpPanelLayout);
        satTcpPanelLayout.setHorizontalGroup(
            satTcpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(satTcpPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(satTcpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(statusPnl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(setupPnl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        satTcpPanelLayout.setVerticalGroup(
            satTcpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(satTcpPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(setupPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(statusPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout tcpListenerManagerLayout = new javax.swing.GroupLayout(tcpListenerManager);
        tcpListenerManager.setLayout(tcpListenerManagerLayout);
        tcpListenerManagerLayout.setHorizontalGroup(
            tcpListenerManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tcpListenerManagerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(eventsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(satTcpPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(11, Short.MAX_VALUE))
        );
        tcpListenerManagerLayout.setVerticalGroup(
            tcpListenerManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tcpListenerManagerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tcpListenerManagerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(satTcpPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventsScrollPane))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        broadcastPanel.setBackground(new java.awt.Color(102, 102, 255));
        broadcastPanel.setEnabled(false);

        callSignBroadcastCheckBox.setBackground(new java.awt.Color(102, 102, 255));
        callSignBroadcastCheckBox.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        callSignBroadcastCheckBox.setText("Enable 10 Minute Callsign Broadcast ");
        callSignBroadcastCheckBox.setEnabled(true);
        callSignBroadcastCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                callSignBroadcastCheckBoxActionPerformed(evt);
            }
        });

        callsignPacketTextField.setEnabled(true);

        operatorCallsignLabel.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        operatorCallsignLabel.setText("Operator Callsign:");

        callsignPacketIdTextField.setEnabled(true);

        ax25PacketRButton.setBackground(new java.awt.Color(102, 102, 255));
        callSignSenderTypeBtnGrp.add(ax25PacketRButton);
        ax25PacketRButton.setSelected(true);
        ax25PacketRButton.setText("AX25 Packet");
        ax25PacketRButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ax25PacketRButtonActionPerformed(evt);
            }
        });

        morseCodeRButton.setBackground(new java.awt.Color(102, 102, 255));
        callSignSenderTypeBtnGrp.add(morseCodeRButton);
        morseCodeRButton.setText("Morse Code");
        morseCodeRButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ax25PacketRButtonActionPerformed(evt);
            }
        });

        setOperatorCallsignBtn.setText("Send");
        setOperatorCallsignBtn.setEnabled(true);
        setOperatorCallsignBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setOperatorCallsignBtnActionPerformed(evt);
            }
        });
        
        //This is where we will set up our modulation scheme stuff
        modComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "<Select>", "1200 AFSK", "9600 GMSK"}));

        modSchemeLbl.setText("Modulation Scheme");

        setSchemeBtn.setText("Set Scheme");
        setSchemeBtn.addActionListener(new java.awt.event.ActionListener(){
        	public void actionPerformed(java.awt.event.ActionEvent evt){
        		setSchemeBtnActionPerformed(evt);
        	}
        });
        
        javax.swing.GroupLayout broadcastPanelLayout = new javax.swing.GroupLayout(broadcastPanel);
        broadcastPanel.setLayout(broadcastPanelLayout);
        broadcastPanelLayout.setHorizontalGroup(
            broadcastPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(broadcastPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(broadcastPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(callSignBroadcastCheckBox)
                    .addGroup(broadcastPanelLayout.createSequentialGroup()
                        .addComponent(operatorCallsignLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(callsignPacketTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(callsignPacketIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(setOperatorCallsignBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(broadcastPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(morseCodeRButton)
                    .addComponent(ax25PacketRButton))    
                .addGap(10, 10, 10)
                .addGroup(broadcastPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                		.addComponent(modSchemeLbl)
                		.addComponent(modComboBox))
                .addGap(10,10,10)		
                .addGroup(broadcastPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                		.addComponent(setSchemeBtn))		
            	
        ));
        broadcastPanelLayout.setVerticalGroup(
            broadcastPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(broadcastPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(broadcastPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(callSignBroadcastCheckBox)
                    .addComponent(ax25PacketRButton)
                    .addComponent(modSchemeLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(broadcastPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(operatorCallsignLabel)
                    .addComponent(callsignPacketTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(callsignPacketIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(morseCodeRButton)
                    .addComponent(setOperatorCallsignBtn)
                    .addComponent(modComboBox)
                    .addComponent(setSchemeBtn))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addComponent(setSchemeBtn)
                )
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(broadcastPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tcpListenerManager, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tncManager, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap()
                )
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tncManager, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tcpListenerManager, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(broadcastPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE)
                )
        );
    }// </editor-fold>//GEN-END:initComponents

    private void testTxBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testTxBtnActionPerformed
        gwyMgr.tncTxTest();
    }//GEN-LAST:event_testTxBtnActionPerformed

    private void resetTNCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetTNCActionPerformed
        if (gwyMgr.tncStatus() !=(ServerStatus.DEAD)){
            gwyMgr.resetTNC((Type)hardwareTypeSelect.getSelectedItem());
        }
    }//GEN-LAST:event_resetTNCActionPerformed

    private void testRxBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testRxBtnActionPerformed
        gwyMgr.tncRxTest();
    }//GEN-LAST:event_testRxBtnActionPerformed

    private void simTopCmdPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simTopCmdPortActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_simTopCmdPortActionPerformed
    
    private void setSchemeBtnActionPerformed(java.awt.event.ActionEvent evt){
    	String option = (String)modComboBox.getSelectedItem();
    	TncSerialProvider kamxl = (TncSerialProvider)gwyMgr.getTNC();
    	try{
    		GroundSystemsPanel g = (GroundSystemsPanel)(this.getParent().getParent());
    		if(option.equals("1200 AFSK")){
    			g.getTS2000Panel().getTS2000().send("EX05006000;");
    			kamxl.resetTNC(Type.KAMXL,true);
    		} else if(option.equals("9600 GMSK")){
    			g.getTS2000Panel().getTS2000().send("EX05006001;");
        		kamxl.resetTNC(Type.KAMXL, false);
    		} else
    			throw new IllegalArgumentException("Not a possible choice");
    	} catch(IOException ioe){
    		ioe.printStackTrace();
    	}
    }
    
    /**
     * Thread used to send the operator callsign and id supplied in the text fields every 10 minutes
     */
    private class CallsignBroadcastCheckBoxImplementation extends Thread {
    	private java.awt.event.ActionEvent evt;
    	
    	public CallsignBroadcastCheckBoxImplementation(java.awt.event.ActionEvent evt){
    		this.evt = evt;
    	}
    	
    	@Override
    	public void run(){
    		Thread thisThread = Thread.currentThread();
    		while(true){
	    		RfGatewayPanel.this.setOperatorCallsignBtnActionPerformed(evt);
	    		//10 min delay -> 10 min = 6e5 ms
	    		try {
					sleep(600000);
				} catch (InterruptedException e) {
					JOptionPane.showMessageDialog(RfGatewayPanel.this, "InterruptionException: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
    		}
    	}
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton ax25PacketRButton;
    private javax.swing.JPanel broadcastPanel;
    private javax.swing.JLabel callLbl;
    private javax.swing.JCheckBox callSignBroadcastCheckBox;
    private Thread callsignCheckBoxThread;
    private javax.swing.ButtonGroup callSignSenderTypeBtnGrp;
    private javax.swing.JTextField callTxt;
    private javax.swing.JTextField callsignPacketIdTextField;
    private javax.swing.JTextField callsignPacketTextField; 
    private javax.swing.JButton closeBtn;
    private javax.swing.JLabel cmdPort;
    private javax.swing.JTextField cmdPortTxt;
    private CommPortIdentifier cpi;
    private javax.swing.JScrollPane eventsScrollPane;
    private javax.swing.JTextArea gatewayEventsBox;
    private javax.swing.JComboBox hardwarePortSelect;
    private javax.swing.ButtonGroup hardwareSimGroup;
    private javax.swing.JComboBox hardwareTypeSelect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JRadioButton morseCodeRButton;
    private javax.swing.JButton openBtn;
    private javax.swing.JButton resetTNC;
    private javax.swing.JLabel runningLabel;
    private javax.swing.JList runningList;
    private javax.swing.JScrollPane runningScrollPane;
    private javax.swing.JPanel satTcpPanel;
    private javax.swing.JButton setOperatorCallsignBtn;
    private javax.swing.JPanel setupPnl;
    private javax.swing.JTextField simBotCmdPort;
    private javax.swing.JTextField simBotTlmPort;
    private javax.swing.JButton simConnectBtn;
    private javax.swing.JLabel simConnectionStat;
    private javax.swing.JTextField simTopCmdPort;
    private javax.swing.JTextField simTopTlmPort;
    private javax.swing.JLabel ssidLbl;
    private javax.swing.JTextField ssidTxt;
    private javax.swing.JPanel statusPnl;
    private javax.swing.JPanel tcpListenerManager;
    private javax.swing.JButton testRxBtn;
    private javax.swing.JButton testTxBtn;
    private javax.swing.JLabel tlmPort;
    private javax.swing.JTextField tlmPortTxt;
    private javax.swing.JButton tncConnectBtn;
    private javax.swing.JLabel tncConnectionStat;
    private javax.swing.JPanel tncManager;
    private javax.swing.JLabel waitingLabel;
    private javax.swing.JList waitingList;
    private javax.swing.JScrollPane waitingScrollPane;
    
    //all of the modulation scheme stuff
    private javax.swing.JComboBox<String> modComboBox;
    private javax.swing.JLabel modSchemeLbl;
    private javax.swing.JButton setSchemeBtn;
    
    // End of variables declaration//GEN-END:variables

    /** This window's title **/
    public static final String TITLE = "RF Gateway";
}
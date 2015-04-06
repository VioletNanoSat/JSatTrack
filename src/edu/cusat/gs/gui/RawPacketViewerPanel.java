package edu.cusat.gs.gui;

import java.awt.Color;
import jsattrak.utilities.Bytes;
import org.joda.time.LocalDateTime;
import edu.cusat.gs.rawpacket.RawPacketManager;

public class RawPacketViewerPanel extends javax.swing.JPanel {


	private static final long serialVersionUID = 1L;

	private RawPacketManager manager;
    
    private int tabIndex;
    
    private final boolean DEBUG = true;

    /** Creates new form RawPacketViewerPanel 
     * @param packetLogViewerPanel1 */
    public RawPacketViewerPanel(RawPacketManager manager, int tabIndex) {
    	this.manager = manager;
    	this.tabIndex = tabIndex;
    	
    	initComponents();  
    }
    
    public int getTabIndex() {
    	return tabIndex;
    }
    
    /**
     * Name of Module
     */
    public String toString() {
    	return "Raw Packet Viewer";
    }
      
    /**
     * Opens a new tab in GroundStation displaying the packet log
     * @param evt
     */
    private void packetLogBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packetLogBtnActionPerformed
		manager.createPacketLogViewerTab();
    }//GEN-LAST:event_packetLogBtnActionPerformed

    /**
     * Toggles pause or unpausing the incoming stream of new packets
     * @param evt
     */
    private void pauseBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseBtnActionPerformed
        if (!manager.getDisplayMostCurPacket()) {
            pauseBtn.setBackground(Color.green);
            pauseBtn.setText("Pause Incoming Packets");
            manager.setDisplayMostCurPacket(true);
        } else {
            pauseBtn.setBackground(Color.red);
            pauseBtn.setText("Resume Incoming Packet Stream");
            manager.setDisplayMostCurPacket(false);
        }
    }//GEN-LAST:event_pauseBtnActionPerformed
    
    /**
     * TODO: Sends created packet to the TNC and RfGateway
     * @param evt
     */
    private void sendPacketBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendPacketBtnActionPerformed

    	//SEND PACKET BACK TO TNC
    	
    }//GEN-LAST:event_sendPacketBtnActionPerformed

    /**
     * Simulates incoming packet stream
     */
    private boolean backandForth = false;
    private void jButtonTestPcktActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTestPcktActionPerformed
        byte[] c = null;

        if (backandForth) {
            c = "KC2Tsdfwsdfwefe3ewf234wef23423feweqVI".getBytes(Bytes.ASCII); 

            backandForth = false;
        } else {
            c = "KC2TDJ>KC2SVI".getBytes(Bytes.ASCII); //[75, 67, 50, 84, 68, 74, 62, 75, 67, 50, 83, 86, 73]

            backandForth = true;
        }

        if (DEBUG)
        	manager.addPacket(c, new LocalDateTime(), new LocalDateTime().minusHours(4), true);
        
    }//GEN-LAST:event_jButtonTestPcktActionPerformed
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
//    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sendPacketBtn = new javax.swing.JButton();
        pauseBtn = new javax.swing.JButton();
        packetLogBtn = new javax.swing.JButton();
        jButtonTestPckt = new javax.swing.JButton();

        setBackground(new java.awt.Color(102, 102, 102));
        setPreferredSize(new java.awt.Dimension(572, 94));

        sendPacketBtn.setText("Send Packet To InControl");
        sendPacketBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendPacketBtnActionPerformed(evt);
            }
        });

        pauseBtn.setBackground(java.awt.Color.green);
        pauseBtn.setForeground(new java.awt.Color(255, 255, 255));
        pauseBtn.setText("Pause Incoming Packets");
        pauseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseBtnActionPerformed(evt);
            }
        });

        packetLogBtn.setText("Create New Packet Log Tab");
        packetLogBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                packetLogBtnActionPerformed(evt);
            }
        });

        jButtonTestPckt.setText("Test Adding Packets");
        jButtonTestPckt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTestPcktActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pauseBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(31, 31, 31)
                        .addComponent(jButtonTestPckt, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sendPacketBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                        .addGap(231, 231, 231)
                        .addComponent(packetLogBtn)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pauseBtn)
                    .addComponent(jButtonTestPckt))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sendPacketBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                    .addComponent(packetLogBtn))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonTestPckt;
    private javax.swing.JButton packetLogBtn;
    private javax.swing.JButton pauseBtn;
    private javax.swing.JButton sendPacketBtn;
    // End of variables declaration//GEN-END:variables

}

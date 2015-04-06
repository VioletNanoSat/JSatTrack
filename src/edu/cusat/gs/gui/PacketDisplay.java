package edu.cusat.gs.gui;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import jsattrak.utilities.Bytes;
import edu.cusat.gs.rawpacket.Packet;
import edu.cusat.gs.rawpacket.RawPacketManager;

public class PacketDisplay extends javax.swing.JPanel {

	private RawPacketManager manager;
	
    private DefaultTableModel dataModel;
    private final int MAX_COL_WIDTH = 18;
    private int MAX_NUM_COL = 30;
        
    //tab index in tabPane of GroundSystemPanel
    private int tabIndex;
    
    /** Creates new form PacketDisplay */
    public PacketDisplay(RawPacketManager manager, JPanel box, int index) {
        this.dataModel = new DefaultTableModel();
        this.controlBox = box;
        this.manager = manager;
        this.tabIndex = index;
              
        initComponents();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);  
        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);
        if ( controlBox.getClass() != new RawPacketViewerPanel(null, 0).getClass())
        	jButtonExit.setBackground(Color.DARK_GRAY);
        else {
        	jButtonExit.setEnabled(false);
        	jButtonExit.setVisible(false);
        }
        	
    }
    
    /**
     * Updater function for when a Tab is exited
     */
    public void decrementTabIndex() {
		tabIndex = tabIndex - 1;		
	}
    
    /**
     * Update the Log Viewers 
     * @param dates
     */
    public void updateControlBox(ArrayList dates) {
    	((PacketLogViewerPanel)controlBox).refresh(dates);
    }  

    /**
     * Name of TNC listener
     */
    public String toString() {
    	return manager.toString() + " " + controlBox.toString();
    }
    
    /**
     * Returns index of tab on GroundSystemsPanel
     */
    public int getTabIndex() { return tabIndex; }
    
    /**
     * Clears Data in DataModel of table
     */
    public void clearJTableLog() {
    	for (int row = 0; row < dataModel.getRowCount(); row++)
    		for (int col = 0; col < dataModel.getColumnCount(); col++)
    			dataModel.setValueAt("", row, col);
    }
    
    /**
     * Sets Label above table
     * @param text
     */
    public void setLabel(String text) {
    	packetInfoLbl.setText(text);
    }

    /**
     * Prints a packet to the TableModel and displays on table
     * @param p
     */
    public void printPacket(Packet p) {
    	if (p.getByteArray() != null) {
    	byte[] pckt = p.getByteArray();

		//if packet is longer than XX number bytes, reduce the width to allow for scrolling bar
		if (pckt.length > 434)
			MAX_NUM_COL--;

		clearJTableLog();
		packetInfoLbl.setText("System Time: " + p.getSystemTime() + "                 Simulator Time: " + p.getSimTime());

    	// create String array and put each converted byte into array in Hex format
    	String[] s = new String[pckt.length];
    	int counter = -1;
    	for (byte b : pckt) {
    		s[++counter] = Bytes.toHexString(new byte[]{b}, 0, 1);
    	}

    	int currentCol = 0;  //jTableLog.getColumnCount();
    	int currentRow = 1;  //jTableLog.getRowCount();

    	// while bytes are left to be placed in table
    	for (int k = 0; k < s.length; k++) {

    		// if the next column to place a byte is "out of bounds"
    		if (currentCol >= MAX_NUM_COL){

    			int h = table.getRowCount();

    			// check if the next row has been created, if not create it
    			if (currentRow >= h)
    				dataModel.addRow(new Object[]{s[k]});
    			else
    				dataModel.setValueAt(s[k], currentRow, 0);

    			// update current values
				currentCol = 1;
				currentRow++;
    		}

    		// if not, place the next byte in the next column
    		else
    		{
    			String n = "";
    			try { n= table.getColumnName(currentCol); }
    			catch (ArrayIndexOutOfBoundsException e) {}

    			// if next column exists, place byte in table... otherwise create a new column
    			if (n == "")
    				dataModel.addColumn((currentCol + ""), new Object[]{s[k]});
    			else
    				dataModel.setValueAt(s[k], currentRow-1, currentCol);

    			// update values
    			currentCol++;
    		}
    	}

    	// set the preferred size of every column
    	for (int k = 0; k < table.getColumnCount(); k++) {
    		table.getColumnModel().getColumn(k).setPreferredWidth(MAX_COL_WIDTH);
    	}

    	// repaint table
    	table.repaint();
    	}
    }
    
    /**
     * Close Tab
     * @param evt
     */
    private void jButtonExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExitActionPerformed
    	manager.closeLogger(this);
    }//GEN-LAST:event_jButtonExitActionPerformed
    
    /**
     * Mouse over sets "Exit button to red"
     * @param evt
     */
    private void jButtonExitMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonExitMouseEntered
        if (jButtonExit.isEnabled())
        	jButtonExit.setBackground(Color.red);
    }//GEN-LAST:event_jButtonExitMouseEntered

    /**
     * Exiting mouse over sets "Exit" button to gray 
     * @param evt
     */
    private void jButtonExitMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonExitMouseExited
    	if (jButtonExit.isEnabled())
    		jButtonExit.setBackground(Color.GRAY);
    }//GEN-LAST:event_jButtonExitMouseExited


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        packetInfoLbl = new javax.swing.JLabel();
        tblScrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable(dataModel);
        jButtonExit = new javax.swing.JButton();

        packetInfoLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        packetInfoLbl.setText("Test Packet Info");

        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.setMaximumSize(new java.awt.Dimension(1, 1));
        tblScrollPane.setViewportView(table);

        jButtonExit.setText("X");
        jButtonExit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonExitMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonExitMouseExited(evt);
            }
        });
        jButtonExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tblScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
                    .addComponent(controlBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(packetInfoLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, 25, 25)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(packetInfoLbl)
                    .addComponent(jButtonExit, javax.swing.GroupLayout.PREFERRED_SIZE, 20, 20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tblScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(controlBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controlBox;
    private javax.swing.JButton jButtonExit;
    private javax.swing.JLabel packetInfoLbl;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tblScrollPane;
    // End of variables declaration//GEN-END:variables
}

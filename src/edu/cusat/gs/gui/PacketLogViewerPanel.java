package edu.cusat.gs.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

import javax.swing.JComboBox;

import org.jdesktop.swingx.JXDatePicker;

import edu.cusat.gs.rawpacket.Packet;
import edu.cusat.gs.rawpacket.PacketLogger;
import edu.cusat.gs.rawpacket.RawPacketManager;

public class PacketLogViewerPanel extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;
	private RawPacketManager manager;
    private PacketLogger packetLog;
    private ArrayList<Packet> eLog;
    
    private int tabIndex; 
    private int loggerIndex;
    
    private javax.swing.JComboBox[] comboBox = new JComboBox[3];
    
    private final int iPACKET = 0;
    private final int iDATE_START = 1;
    private final int iDATE_END = 2;    
    
    private String startDate = "";
    private String endDate = "";
    
//    private ArrayList<Packet> log = new ArrayList<Packet>();
   
    /** Creates new form RawPacketViewerPanel 
     * @param loggerIndex */
    public PacketLogViewerPanel(RawPacketManager manager, PacketLogger logger, int tabIndex, int loggerIndex) {
    	this.manager = manager;
    	this.tabIndex = tabIndex;
    	this.packetLog = logger;
    	this.loggerIndex = loggerIndex;
    	         	
    	initComponents();  
    	
    	comboBox[iDATE_START] = jListOfDatesStart;
    	comboBox[iDATE_END] = jListOfDatesEnd;
    	comboBox[iPACKET] = rxdPacketsComboBox;
    }
    
    public int getTabIndex() { return tabIndex; }
    
    public int getLoggerIndex() { return loggerIndex; }
    
    /**
     * Name of Module
     */
    public String toString() {
    	return "Raw Packet Logger";
    }
    
    /**
     * Sets comboBoxes 0 - Current list of Packets, 1 - Date Start, 2 - Date End
     * @param index
     * @param list
     */
    private void setComboBox (int index, ArrayList<Packet> list) {        	
    	Object currentSelectedItem = comboBox[index].getSelectedItem();
    	getItems(index);
    	
    	comboBox[index].removeAllItems();
    	
    	//Append items instead of replacing them
    	if (list != null && list.size() > 0) {
    		    		
    		ListIterator<Packet> itr = list.listIterator();
    		while(itr.hasNext()) {
    			comboBox[index].addItem(itr.next());
    		}
    	}    	
    	 	
    	
    	//TODO: Check if new 'list' contains previously selected packet, cannon use contains method, compares Object Id #
    	if (currentSelectedItem != null && list.contains(currentSelectedItem)) 
    		comboBox[index].setSelectedItem(currentSelectedItem);
    	else
    		comboBox[index].setSelectedItem(1);
    	
    	comboBox[index].repaint();
    }
    
    /**
     * Returns Object[] list of items in the Combo Box
     * @return
     */
    private ArrayList<Object> getItems(int index) {    	
    	ArrayList<Object> items = new ArrayList<Object>(comboBox[index].getItemCount());

    	int max = comboBox[index].getItemCount();
    	JComboBox tmp = comboBox[index];
    	
    	for (int k = 0; k < max; k++) {
    		Object itm = tmp.getItemAt(k);
    		items.add(itm);
    	}
    	
    	return items;
    }
    
    /**
     * Refreshes Packet Lists and Dates in the ComboBoxes         
     * @param newDates
     */
    public void refresh(ArrayList<Packet> newDates) {
    	setComboBox(iDATE_START, newDates);
    	setComboBox(iDATE_END, newDates);   
    	    
    	//If no new interval has been selected, leave as is
    	ArrayList<Packet> received = listOfPacketsToDisplay(false);
    	if (received.size() > 0)
    		eLog = received;
		setComboBox(iPACKET, eLog);    	
    }
      
    /**
     * Advances in packet list
     * @param evt
     */
    private void playBackBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playBackBtnActionPerformed
    	int currentIndex = rxdPacketsComboBox.getSelectedIndex();
    	
    	if (currentIndex > 0) {
    		rxdPacketsComboBox.setSelectedIndex(currentIndex-1);
    		playBtn.setEnabled(true);
    	} 	
    	
    	if (currentIndex-1 == 0) 
			playBackBtn.setEnabled(false);
        
    }//GEN-LAST:event_playBackBtnActionPerformed

    /**
     * Retreats in packet list
     * @param evt
     */
    private void playBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playBtnActionPerformed
    	int currentIndex = rxdPacketsComboBox.getSelectedIndex();
    	int max = rxdPacketsComboBox.getItemCount();
    	
    	if (currentIndex < max-1) {
    		rxdPacketsComboBox.setSelectedIndex(currentIndex+1);
    		playBackBtn.setEnabled(true);
    	}
    	
    	if (currentIndex+1 == max-1) 
			playBtn.setEnabled(false);
    	
    }//GEN-LAST:event_playBtnActionPerformed

    /**
     * Selects combo box
     * @param evt
     */
    private void rxdPacketsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rxdPacketsComboBoxActionPerformed
    	if ((Packet)rxdPacketsComboBox.getSelectedItem() != null){
    		manager.printPacketToLog((Packet)rxdPacketsComboBox.getSelectedItem(), loggerIndex);
    	}
        
    	if (rxdPacketsComboBox.getSelectedIndex() == 0) {
        	playBtn.setEnabled(true);
        	playBackBtn.setEnabled(false);
    	}
    	
    	else if (rxdPacketsComboBox.getSelectedIndex()+1 == rxdPacketsComboBox.getItemCount()) {
        	if(javax.swing.SwingUtilities.isEventDispatchThread()){
        		playBtn.setEnabled(false);
        		playBackBtn.setEnabled(true);
        	}
        }
    	    	
        else {
        	playBtn.setEnabled(true);
        	playBackBtn.setEnabled(true);
        }
    }//GEN-LAST:event_rxdPacketsComboBoxActionPerformed

    /**
     * Clear Table for Testing purposes
     * @param evt
     */
    private void clearTableBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearTableBtnActionPerformed
    	manager.clearTable(loggerIndex);
    }//GEN-LAST:event_clearTableBtnActionPerformed
        
    /**
     * Toggle button for sorting packets in combo box by simulation time or system time
     * @param evt
     */
    private boolean showSimTime = false;
    private void jRadioButtonSimTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonSimTimeActionPerformed
		ArrayList<Object> items = getItems(iPACKET);
		
    	if (showSimTime) {
    		showSimTime = false;	    		    	
        	jRadioButtonSimTime.setSelected(false);
        	
        	for (Object p : items)
        		((Packet)p).printSystemTime = !showSimTime;
    	}
    	else if (!showSimTime) {
    		showSimTime = true;	    			
        	jRadioButtonSimTime.setSelected(true);
        	
        	for (Object p : items) 
        		((Packet)p).printSystemTime = !showSimTime;
        	
    	}
    	jRadioButtonSimTime.repaint();
    	rxdPacketsComboBox.repaint();
    	
    }//GEN-LAST:event_jRadioButtonSimTimeActionPerformed

    /**
     * Opens Calendar and asks user to select what period of time to view packets from
     * @param evt
     */
    private void calendarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calendarActionPerformed
        
    	eLog = listOfPacketsToDisplay(true);
    	setComboBox(iPACKET, eLog); 
    	
    	//Update GUI, to and from labels
    	String startDate = comboBox[iDATE_START].getSelectedItem().toString();
    	String endDate = comboBox[iDATE_END].getSelectedItem().toString();
    	startDate = startDate.substring(0,4) + "-" + startDate.substring(4,6) + "-" + startDate.substring(6);
    	endDate = endDate.substring(0,4) + "-" + endDate.substring(4,6) + "-" + endDate.substring(6);
    	jLabelPacketsFrom.setText(startDate);
    	jLabelPacketsTo.setText(endDate);
    	
    }//GEN-LAST:event_calendarActionPerformed
    
    /**
     * Helper Method: Returns ArrayList of all Object in ComboBox
     * @param box
     * @return
     */
    private ArrayList<Object> getItems (JComboBox box) {
    	ArrayList<Object> items = new ArrayList<Object>();
    	int len = box.getItemCount();
    	
    	for (int k = 0; k < len; k++)
    		items.add(box.getItemAt(k));
    	
    	return items;
    		
    }
       
    /**
     * Helper Method: Generates total Packet list to be displayed in packet combo box
     * @param refresh
     * @return
     */
    private ArrayList<Packet> listOfPacketsToDisplay(boolean refresh) {
    	ArrayList<Packet> totalPacketList = new ArrayList<Packet>();	
    	
    	//Set indices to a negative value - indicating combobox is empty
    	int bI = -1; int eI = -1;
    	
    	//If we are reseting new date interval, change
    	if (refresh) {
	    	startDate = (String)jListOfDatesStart.getSelectedItem();
	    	endDate = (String)jListOfDatesEnd.getSelectedItem();
	    	bI = jListOfDatesStart.getSelectedIndex();
	    	eI = jListOfDatesEnd.getSelectedIndex();
    	}
    	
    	//If we are just updating current date interval (arrival of new packet)
    	else {
    		ArrayList<Object> dates = getItems(jListOfDatesStart);
    	
    		for (int k = 0; k < dates.size(); k++) {
    			if (jListOfDatesStart.getItemAt(k).equals(startDate))
    				bI = k;
    			if (jListOfDatesStart.getItemAt(k).equals(endDate))
    				eI = k;
    		}   		
    	}
    	
    	//Order doesn't matter in which date occurs before the other
    	if (bI > eI) {
    		int tmp = bI;
    		bI = eI;
    		eI = tmp;
    	}
    	
    	//Get all packets between these dates, if there is a previous interval selected, otherwise return a [] array
    	if (bI != -1 && eI != -1) {
			for (int k = bI; k <= eI; k++){
				String date = (String)jListOfDatesStart.getItemAt(k);
				
				//Unformat date
				date = date.substring(0,4) + date.substring(5,7) + date.substring(8);
    																	//manager.director
				totalPacketList.addAll(packetLog.retrieveFromFile(new File("data/packets/" + manager.toString() + "/" + date + ".txt")));   
			}    
    	}   
    	
    	Collections.reverse(totalPacketList);
    	
    	return totalPacketList;
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
//    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    @SuppressWarnings("rawtypes")
	private void initComponents() {

        clearTableBtn = new javax.swing.JButton();
        playBackBtn = new javax.swing.JButton();
        playBtn = new javax.swing.JButton();
        pktSelectorPnl = new javax.swing.JPanel();
        jRadioButtonSimTime = new javax.swing.JRadioButton();
        pktsLbl = new javax.swing.JLabel();
        rxdPacketsComboBox = new javax.swing.JComboBox();
        jLabelPacketsFrom = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabelPacketsTo = new javax.swing.JLabel();
        calendar = new javax.swing.JButton();
        jListOfDatesEnd = new javax.swing.JComboBox();
        jListOfDatesStart = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(204, 204, 204));
        setPreferredSize(new java.awt.Dimension(572, 94));

        clearTableBtn.setText("Clear Table");
        clearTableBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearTableBtnActionPerformed(evt);
            }
        });

        playBackBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/media-playback-playbackwards.png"))); // NOI18N
        playBackBtn.setToolTipText("Play Backwards"); // NOI18N
        playBackBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playBackBtnActionPerformed(evt);
            }
        });

        playBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/media-playback-start.png"))); // NOI18N
        playBtn.setToolTipText("Play Forwards"); // NOI18N
        playBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playBtnActionPerformed(evt);
            }
        });

        pktSelectorPnl.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jRadioButtonSimTime.setText("Simulator Time");
        jRadioButtonSimTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonSimTimeActionPerformed(evt);
            }
        });

        pktsLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pktsLbl.setText("Packets from");

        rxdPacketsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rxdPacketsComboBoxActionPerformed(evt);
            }
        });

        jLabelPacketsFrom.setText("X");

        jLabel2.setText("to");

        jLabelPacketsTo.setText("X");

        javax.swing.GroupLayout pktSelectorPnlLayout = new javax.swing.GroupLayout(pktSelectorPnl);
        pktSelectorPnl.setLayout(pktSelectorPnlLayout);
        pktSelectorPnlLayout.setHorizontalGroup(
            pktSelectorPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pktSelectorPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pktSelectorPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pktSelectorPnlLayout.createSequentialGroup()
                        .addComponent(pktsLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelPacketsFrom)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelPacketsTo))
                    .addGroup(pktSelectorPnlLayout.createSequentialGroup()
                        .addComponent(jRadioButtonSimTime)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rxdPacketsComboBox, 0, 130, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pktSelectorPnlLayout.setVerticalGroup(
            pktSelectorPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pktSelectorPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pktSelectorPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelPacketsTo)
                    .addComponent(jLabel2)
                    .addComponent(jLabelPacketsFrom)
                    .addComponent(pktsLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pktSelectorPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonSimTime)
                    .addComponent(rxdPacketsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        calendar.setText("Choose Date Interval");
        calendar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calendarActionPerformed(evt);
            }
        });

        jListOfDatesStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jListOfDatesStartActionPerformed(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("   Show Packets From:");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("   Show Packets To:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(clearTableBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(calendar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(playBackBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(playBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jListOfDatesStart, 0, 150, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jListOfDatesEnd, 0, 143, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pktSelectorPnl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pktSelectorPnl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jListOfDatesEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jListOfDatesStart, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(clearTableBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                                    .addComponent(calendar, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(playBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                                    .addComponent(playBackBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))))))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jListOfDatesStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jListOfDatesStartActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jListOfDatesStartActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton calendar;
    private javax.swing.JButton clearTableBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelPacketsFrom;
    private javax.swing.JLabel jLabelPacketsTo;
    @SuppressWarnings("rawtypes")
	private javax.swing.JComboBox jListOfDatesEnd;
    @SuppressWarnings("rawtypes")
	private javax.swing.JComboBox jListOfDatesStart;
    private javax.swing.JRadioButton jRadioButtonSimTime;
    private javax.swing.JPanel pktSelectorPnl;
    private javax.swing.JLabel pktsLbl;
    private javax.swing.JButton playBackBtn;
    private javax.swing.JButton playBtn;
    @SuppressWarnings("rawtypes")
	private javax.swing.JComboBox rxdPacketsComboBox;
    // End of variables declaration//GEN-END:variables
}

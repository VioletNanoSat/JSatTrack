/*
 * JGroundStationBrowser.java
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
 * Created on December 12, 2007, 2:14 PM
 */

package jsattrak.gui;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import jsattrak.objects.GroundStation;
import jsattrak.utilities.TreeGroundStationTransferHandler;

/**
 *
 * @author  sgano
 */
public class JGroundStationBrowser extends javax.swing.JPanel implements java.io.Serializable
{
	public static final String groundStationDB = "data/groundstations/groundstations_db.csv";
    public static final String groundStationDir = "data/groundstations/";
    public static final String groundStationCustomDB = "data/groundstations/groundstations_custom.csv";
    
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode topTreeNode; // top node
    private DefaultMutableTreeNode customSecondaryNode;
    
    /** stores Ground Station name and LLA info [deg,deg,meters] **/
    private HashMap<String,double []> gsHash = new HashMap<String, double[]>(); 
    
    private Frame parent;
    
    /** Creates new form JGroundStationBrowser */
    public JGroundStationBrowser(Frame parent) {
        this.parent = parent;
        
        initComponents();
        
        // top tree node
        topTreeNode = new DefaultMutableTreeNode("Ground Stations");
        treeModel = new DefaultTreeModel(topTreeNode); // create tree model using root node
        groundStationTree.setModel(treeModel); // set the tree's model
        
        loadGsDatabases();
        
        // Drag and Drop Handler
        // setup transfer handler
        //must also enable drag in IDE gui for tree
        groundStationTree.setTransferHandler(new TreeGroundStationTransferHandler(gsHash));
        
        // allow multiple selections
        groundStationTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        
    } // constructor

    /**
     * @param currentSecondaryNodeName
     * @param currentSecondaryNode
     */
    private void loadGsDatabases() {
        
        DefaultMutableTreeNode currentSecondaryNode = null; //new DefaultMutableTreeNode(tleDownloader.secondCat[i]);
        String currentSecondaryNodeName = null;
                
        // read data files for Ground Stations (standard and custom)
        try {
            BufferedReader gsReader; // initalization of reader 
            
            //see if local file exists, if not stream from web
            
            // read local file
            if (new File(groundStationDB).exists()) {
                File gsFile = new File(groundStationDB);
                FileReader gsFileReader = new FileReader(gsFile);
                gsReader = new BufferedReader(gsFileReader); // from local file
            } else {
                // read from web
                URL url = new URL("http://www.gano.name/shawn/JSatTrak/" + groundStationDB);
                URLConnection c = url.openConnection();
                InputStreamReader isr = new InputStreamReader(c.getInputStream());
                gsReader = new BufferedReader(isr); // from the web
            }
            
            String nextLine = null;
            int gsCount = 0; // count of stations loaded
            while ((nextLine = gsReader.readLine()) != null) {
                // split line into parts
                String[] elements = nextLine.split(",");
                
                if (elements.length == 5) { // if the row is formatted correctly
                    String network = elements[0];
                    String stationName = elements[1];
                    double stationLat = Double.parseDouble(elements[2]);
                    double stationLon = Double.parseDouble(elements[3]);
                    double stationAlt = Double.parseDouble(elements[4]);

                    // save ground station to hash
                    gsHash.put(stationName, new double[]{stationLat, stationLon, stationAlt});

                    // check to see if we are still in same Secondary node if not create new one
                    if (!network.equalsIgnoreCase(currentSecondaryNodeName)) {
                        // create new secondary node and add it to the tree
                        currentSecondaryNode = new DefaultMutableTreeNode(network);
                        topTreeNode.add(currentSecondaryNode);
                        currentSecondaryNodeName = network;
                    }

                    // add new Ground station to the node
                    currentSecondaryNode.add(new DefaultMutableTreeNode(stationName));

                    gsCount++;
                }
            }// while there are more lines to read
            gsReader.close();
            
            // now load custom Ground Stations
            if (new File(groundStationCustomDB).exists()) {
                File gsFile = new File(groundStationCustomDB);
                FileReader gsFileReader = new FileReader(gsFile);
                gsReader = new BufferedReader(gsFileReader); // from local file

                customSecondaryNode = new DefaultMutableTreeNode("Custom");
                topTreeNode.add(customSecondaryNode);
                while ((nextLine = gsReader.readLine()) != null) {
                    // split line into parts
                    String[] elements = nextLine.split(",");

                    if (elements.length == 5) {// if the row is formatted correctly
                        // String network = elements[0]; // ignored for custom
                        String stationName = elements[1];
                        double stationLat = Double.parseDouble(elements[2]);
                        double stationLon = Double.parseDouble(elements[3]);
                        double stationAlt = Double.parseDouble(elements[4]);

                        // save ground station to hash
                        gsHash.put(stationName, new double[]{stationLat, stationLon, stationAlt});

                        // add new Ground station to the node
                        customSecondaryNode.add(new DefaultMutableTreeNode(stationName));

                        gsCount++;
                    }
                }// while there are more lines to read
                gsReader.close();
            } else { // if custom file exists
                // add blank Custom node
                customSecondaryNode = new DefaultMutableTreeNode("Custom");
                topTreeNode.add(customSecondaryNode);
            }
            
            // add text to bottom
            statusTextField.setText("Total Ground Stations loaded: " + gsCount);
            
            // auto expand root node
            groundStationTree.expandRow(0);
        
        } catch (IOException e) {initError(e); 
        } catch (NumberFormatException e) { initError(e);
        }
    }
    
    /**
     * Loads a ground station from the file
     * 
     * @param name
     *            the name of the ground station
     * @param julDate
     *            the current Julian Date
     * @return the GroundStation if it is in one of the files, or
     *         <code>null</code>
     */
    public GroundStation getGs(String name, double julDate) {
        return gsHash.containsKey(name) ?
                new GroundStation(name, gsHash.get(name), julDate) : null;
    }

    /** @param e the error to report **/
    private void initError(Exception e) {
        System.out.println("ERROR IN GROUND STATION READING POSSIBLE FILE" +
        		" FORMAT OR MISSING FILES:");
        e.printStackTrace();
        JOptionPane.showMessageDialog(parent,
                "Error Loading Ground Station Data. Check data.\n"
                        + e.toString(), "Data LOADING ERROR",
                JOptionPane.ERROR_MESSAGE);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        groundStationTree = new javax.swing.JTree();
        statusTextField = new javax.swing.JTextField();
        addGSButton = new javax.swing.JButton();

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel1.setText("Ground Station Browser");

        groundStationTree.setDragEnabled(true);
        groundStationTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener()
        {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt)
            {
                groundStationTreeValueChanged(evt);
            }
        });
        groundStationTree.addPropertyChangeListener(new java.beans.PropertyChangeListener()
        {
            public void propertyChange(java.beans.PropertyChangeEvent evt)
            {
                groundStationTreePropertyChange(evt);
            }
        });
        jScrollPane1.setViewportView(groundStationTree);

        statusTextField.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                statusTextFieldActionPerformed(evt);
            }
        });

        addGSButton.setText("+");
        addGSButton.setToolTipText("Add Custom Ground Station");
        addGSButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addGSButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addComponent(addGSButton))
            .addComponent(statusTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(addGSButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void statusTextFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_statusTextFieldActionPerformed
    {//GEN-HEADEREND:event_statusTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_statusTextFieldActionPerformed

    private void groundStationTreePropertyChange(java.beans.PropertyChangeEvent evt)//GEN-FIRST:event_groundStationTreePropertyChange
    {//GEN-HEADEREND:event_groundStationTreePropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_groundStationTreePropertyChange

    private void groundStationTreeValueChanged(javax.swing.event.TreeSelectionEvent evt)//GEN-FIRST:event_groundStationTreeValueChanged
    {//GEN-HEADEREND:event_groundStationTreeValueChanged
        if (groundStationTree.getSelectionCount() > 0) {
            if (gsHash.containsKey(groundStationTree
                    .getLastSelectedPathComponent().toString())) {
                double[] lla = gsHash.get(groundStationTree
                        .getLastSelectedPathComponent().toString());

                statusTextField.setText("Lat:" + lla[0] + ", Lon:" + lla[1]
                        + ", Alt[m]:" + lla[2]);
            } else { // clear text area
                statusTextField.setText("");
            }
        } // something is selected
    }//GEN-LAST:event_groundStationTreeValueChanged

    private void addGSButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addGSButtonActionPerformed
    {//GEN-HEADEREND:event_addGSButtonActionPerformed
        JAddGroundStationDialog dlg = new JAddGroundStationDialog(parent,true);
        dlg.setVisible(true); // show dialog

        if (dlg.isOkHit()) {
            String network = dlg.getNetwork();
            String siteName = dlg.getSiteName();

            // save ground station to hash
            gsHash.put(siteName, new double[] { dlg.getLatitude(),
                    dlg.getLongitude(), dlg.getAltitude() });

            // add new Ground station to the node
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
                    siteName);
            customSecondaryNode.add(newNode);

            groundStationTree.repaint(); // refreseh tree?

            // expand custom
            groundStationTree.scrollPathToVisible(getPath(newNode));

            // append data to file
            if (dlg.isSaveData()) { saveGs(network, siteName); } 

        } // if okay hit

    }//GEN-LAST:event_addGSButtonActionPerformed

    /**
     * Saves a ground station to the existing file, or creates one if necessary.
     * Assumes that the ground station has already been added to the hash.
     * 
     * @param network
     *            the network of the ground station
     * @param siteName
     *            the name of the ground station
     */
    private void saveGs(String network, String siteName) {
        try {
            File gsFile = new File(groundStationCustomDB);
            boolean append = true;
            
            // if file doesn't exist create a new one
            if (!gsFile.exists()) {
                if (!new File(groundStationDir).mkdirs())
                    throw new IOException("Could not create directory: "
                            + groundStationDir);
                if (!gsFile.createNewFile())
                    throw new IOException("Could not create save file: "
                            + groundStationCustomDB);
                
                append = false;
            } // create new file
            
            // from local file
            BufferedWriter gsWriter = new BufferedWriter(new FileWriter(gsFile,append)); 

            gsWriter.write("\n" + network + "," + siteName + ","
                    + gsHash.get(siteName)[0] + "," + gsHash.get(siteName)[1]
                    + "," + gsHash.get(siteName)[2]);

            gsWriter.close();

        } catch (IOException e) {
            System.out.println("ERROR SAVING GROUND STATION - Check"
                    + " permissions or format:");
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Error Saving Ground"
                    + " Station Data. Check data and permissions. \n"
                    + e.toString(), "Data SAVING ERROR",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    /** @return a TreePath containing the specified node. **/
    public static TreePath getPath(TreeNode node) {
        List<TreeNode> list = new ArrayList<TreeNode>();

        // Add all nodes to list
        while (node != null) {
            list.add(node);
            node = node.getParent();
        }
        Collections.reverse(list);

        // Convert array of nodes to TreePath
        return new TreePath(list.toArray());
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addGSButton;
    private javax.swing.JTree groundStationTree;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField statusTextField;
    // End of variables declaration//GEN-END:variables
    
}

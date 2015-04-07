/*
 * JObjectListInternalFrame.java
 *
 * NOTE: THIS WAS A TEST -- commented lines need to be removed!! -- trying to get Substance LAF to work. :(
 *
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
 * JObjectListInternalFrame.java
 *
 * Created on Mar 21, 2009, 8:16:44 PM
 */

package jsattrak.gui;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.CustomSatellite;
import jsattrak.objects.GroundStation;
import jsattrak.objects.SatelliteTleSGP4;
import jsattrak.utilities.IconTreeNode;
import jsattrak.utilities.IconTreeNodeRenderer;
import jsattrak.utilities.ObjectTreeTransferHandler;

/**
 *
 * @author Shawn
 */
public class JObjectListInternalFrame extends javax.swing.JInternalFrame
{
     // tree nodes
    DefaultTreeModel treeModel;
    IconTreeNode topTreeNode; // top node
    IconTreeNode topSatTreeNode; // top satellite node
    IconTreeNode topGSTreeNode; // top Ground Stations node

    // calling JSatTrak program (used to send actions back)
    JSatTrak app;

    /** Creates new form JObjectListInternalFrame */
    public JObjectListInternalFrame(JSatTrak app)
    {
        this.app = app;

        initComponents();

        // setup tree ====
        // setup nodes
        topTreeNode = new IconTreeNode("Objects");  // root
        topSatTreeNode = new IconTreeNode("Satellites"); // sat root
        topGSTreeNode = new IconTreeNode("Ground Stations"); // GS root

        // add nodes to top node
        topTreeNode.add(topSatTreeNode);
        topTreeNode.add(topGSTreeNode);

        //set 2nd level nodes icons
        topSatTreeNode.setIcon( new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/custom/sat_icon.png")) ) );
        topGSTreeNode.setIcon( new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/custom/groundStation.png")) ) );


        // setup tree model
//        DefaultMutableTreeNode f = new DefaultMutableTreeNode();
//        DefaultMutableTreeNode g = new DefaultMutableTreeNode("test");
//        f.add(g);
//        treeModel = new DefaultTreeModel(f);
        
        treeModel = new DefaultTreeModel(topTreeNode); // create tree model using root node
        objectTree.setModel(treeModel); // set the tree's model

        // set cell renderer to show custom icons
        objectTree.setCellRenderer(new IconTreeNodeRenderer());

        // make root node invisible - so you can just see object types
        objectTree.setRootVisible(false);

        // add handler to the Tree
        objectTree.setTransferHandler(new ObjectTreeTransferHandler(app, topSatTreeNode, topGSTreeNode));

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        removeSatButton = new javax.swing.JButton();
        infoSatButton = new javax.swing.JButton();
        optionsSatButton = new javax.swing.JButton();
        addCustomSatButton = new javax.swing.JButton();
        addSatButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        objectTree = new javax.swing.JTree();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Object List");
        setFrameIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/logo/JSatTrakLogo_16.png"))); // NOI18N
        setPreferredSize(new java.awt.Dimension(269, 331));

        removeSatButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/Delete16.gif"))); // NOI18N
        removeSatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSatButtonActionPerformed(evt);
            }
        });

        infoSatButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/Information16.gif"))); // NOI18N
        infoSatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoSatButtonActionPerformed(evt);
            }
        });

        optionsSatButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/preferences-desktop.png"))); // NOI18N
        optionsSatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionsSatButtonActionPerformed(evt);
            }
        });

        addCustomSatButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/custom/sat_icon_cst.png"))); // NOI18N
        addCustomSatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCustomSatButtonActionPerformed(evt);
            }
        });

        addSatButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/custom/sat_icon.png"))); // NOI18N
        addSatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSatButtonActionPerformed(evt);
            }
        });

        objectTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                objectTreeMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(objectTree);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(removeSatButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoSatButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(optionsSatButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 78, Short.MAX_VALUE)
                .addComponent(addSatButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addCustomSatButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(removeSatButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(infoSatButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(optionsSatButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addCustomSatButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addSatButton, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void removeSatButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeSatButtonActionPerformed
    {//GEN-HEADEREND:event_removeSatButtonActionPerformed
        // remove object selected
        if(objectTree.getSelectionCount() != 0) // still might be a root node
        {
            // loop for all selected
            TreePath[] paths = objectTree.getSelectionPaths();
            for(TreePath tp : paths) // for each selected node:
            {
                // get object
                MutableTreeNode node = (MutableTreeNode )tp.getLastPathComponent();

                // first try to remote it from satHash (if it is in there)
                String nameSelected =  node.toString();

                // remove from satHash if it is in sat hash
                if(app.containsSat(nameSelected))
                {
                    app.removeSatellite(nameSelected); // remove it from hashTable
                    objectTree.removeSelectionPath(tp); //remove item from list
                    ((DefaultTreeModel)objectTree.getModel()).removeNodeFromParent( node );

                    app.setStatusMessage("Deleted Satellite: "+nameSelected);
                }
                else if(app.containsGs(nameSelected))
                {
                    app.removeGs(nameSelected); // remove it from hashTable
                    objectTree.removeSelectionPath(tp); //remove item from list
                    ((DefaultTreeModel)objectTree.getModel()).removeNodeFromParent( node );

                    app.setStatusMessage("Deleted Ground Station: "+nameSelected);
                }


            } // for each selected object

            // force repaint
            app.forceRepainting();
        } // if anything selected
    }//GEN-LAST:event_removeSatButtonActionPerformed

    private void infoSatButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_infoSatButtonActionPerformed
    {//GEN-HEADEREND:event_infoSatButtonActionPerformed
         // first make sure object is selected
        if(objectTree.getSelectionCount() != 1)
        {
            return; // quit if nothing selected
        }

        // get name of selected object
        Object obj = objectTree.getSelectionPath().getLastPathComponent();
        String nameSelected = obj.toString();

        // see if it is a satellite
        if(app.containsSat(nameSelected))
        {
            AbstractSatellite prop = app.getSatellite(nameSelected);

            // create property Panel:
            SatPropertyPanel newPanel = new SatPropertyPanel(prop);

            String windowName = prop.getName().trim(); // set name - trim excess spaces

            // create new internal frame window
            JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);

            iframe.setContentPane( newPanel ); // set contents pane
            iframe.setSize(365,355+10); // set size

            iframe.setVisible(true);
            app.addInternalFrame(iframe);

        } // if sat in hashTable
        // else check for Ground Station selection
        else if(app.containsGs(nameSelected))
        {
            GroundStation prop = app.getGs(nameSelected);

            // create property Panel:
            GroundStationInformationPanel newPanel = new GroundStationInformationPanel(prop);

            String windowName = prop.getStationName().trim(); // set name - trim excess spaces

            // create new internal frame window
            JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);

            iframe.setContentPane( newPanel ); // set contents pane
            iframe.setSize(365,355); // set size

            iframe.setVisible(true);
            app.addInternalFrame(iframe);

        } // if gs in hashTable
    }//GEN-LAST:event_infoSatButtonActionPerformed

    private void optionsSatButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_optionsSatButtonActionPerformed
    {//GEN-HEADEREND:event_optionsSatButtonActionPerformed
        openCurrentOptions();
    }//GEN-LAST:event_optionsSatButtonActionPerformed

    public void openCurrentOptions()
    {
        // by default open options for first selected object

        // first make sure at least one sat is selected
        if(objectTree.getSelectionCount() != 1)
        {
            return; // quit if nothing selected
        }

        // get name of selected satellite
        Object obj = objectTree.getSelectionPath().getLastPathComponent();

        openCurrentOptions(obj);
    }

    public void openCurrentOptions(Object obj)
    {
        // try to see if obj is in a current list

        String nameSelected = obj.toString();

        if(app.containsSat(nameSelected))
        {
            AbstractSatellite prop = app.getSatellite(nameSelected);

            // create create Sat Settings panel
            SatSettingsPanel newPanel = new SatSettingsPanel(prop,app);

            String windowName = prop.getName().trim() + " - Settings"; // set name - trim excess spaces

            // create new internal frame window
            JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);

            // save iframe
            newPanel.setInternalFrame(iframe);

            iframe.setContentPane( newPanel ); // set contents pane
            iframe.setSize(340,380+80); // set size w,h (+80 for Nimbus)

            if(prop instanceof CustomSatellite)
            {
                // it needs a bigger panel!
                iframe.setSize(340+35,380+80); // set size w,h (+80 for Nimbus)
            }

            iframe.setVisible(true);
            app.addInternalFrame(iframe);

        } // if sat in hashTable

        // check if a ground station was selected
        else if(app.containsGs(nameSelected))
        {
            GroundStation prop = app.getGs(nameSelected);

            // create create Sat Settings panel
            GroundStationSettingsPanel newPanel = new GroundStationSettingsPanel(prop,app);

            String windowName = prop.getStationName().trim() + " - Settings"; // set name - trim excess spaces

            // create new internal frame window
            JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);

            // save iframe
            newPanel.setInternalFrame(iframe);

            iframe.setContentPane( newPanel ); // set contents pane
            iframe.setSize(340,380); // set size w,h

            iframe.setVisible(true);
            app.addInternalFrame(iframe);
        } // ground station
    } // open CurrentSatOptions

    private void addSatButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addSatButtonActionPerformed
    {//GEN-HEADEREND:event_addSatButtonActionPerformed
        app.showSatBrowserInternalFrame();
    }//GEN-LAST:event_addSatButtonActionPerformed

    private void addCustomSatButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addCustomSatButtonActionPerformed
    {//GEN-HEADEREND:event_addCustomSatButtonActionPerformed
        app.addCustomSat();
    }//GEN-LAST:event_addCustomSatButtonActionPerformed

    private void objectTreeMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_objectTreeMouseClicked
    {//GEN-HEADEREND:event_objectTreeMouseClicked
        // check for a double click
        if (evt.getClickCount() == 2)
        {
            openCurrentOptions();
        }
    }//GEN-LAST:event_objectTreeMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addCustomSatButton;
    private javax.swing.JButton addSatButton;
    private javax.swing.JButton infoSatButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree objectTree;
    private javax.swing.JButton optionsSatButton;
    private javax.swing.JButton removeSatButton;
    // End of variables declaration//GEN-END:variables

    // requires sat Prop
    public void addSat2List(AbstractSatellite prop )
    {
        // add sat to hash
        app.addSatellite(prop.getName(), prop);

        // propogate satellite to current date
        prop.propogate2JulDate( app.getCurrentJulTime() );

        // add name to list
        IconTreeNode newNode = new IconTreeNode(prop.getName());
        treeModel.insertNodeInto(newNode, topSatTreeNode, topSatTreeNode.getChildCount());

        // if SGP4 sat
        if(prop instanceof SatelliteTleSGP4)
        {
            newNode.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/custom/sat_icon_tle.png"))));
        }
        else if(prop instanceof CustomSatellite)
        {
            newNode.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/custom/sat_icon_cst.png"))));
        }


        //System.out.println("node added: " + name);
        objectTree.scrollPathToVisible(getPath(newNode));


    } // addSat2List

    // requires sat Prop
    public void addGS2List(GroundStation prop )
    {
        // add sat to hash
        app.addGs( prop.getStationName() ,  prop);


        // add name to list
        IconTreeNode newNode = new IconTreeNode(prop.getStationName());
        treeModel.insertNodeInto(newNode, topGSTreeNode, topGSTreeNode.getChildCount());

        newNode.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/custom/groundStation_obj.png"))));

        //System.out.println("node added: " + name);
        objectTree.scrollPathToVisible(getPath(newNode));


    } // addSat2List

    // clear list and refresh  - (used in closing and opening scenario)
    public void refreshObjectList()
    {
        // clear elements from Satellites and GS
        int children = treeModel.getChildCount(topSatTreeNode);
        for(int i=0;i<children;i++)
        {
            MutableTreeNode child = (MutableTreeNode)treeModel.getChild(topSatTreeNode, 0); // always get 0th element
            treeModel.removeNodeFromParent(child );
        }

        // remove all GS children
        children = treeModel.getChildCount(topGSTreeNode);
        for(int i=0;i<children;i++)
        {
            MutableTreeNode child = (MutableTreeNode)treeModel.getChild(topGSTreeNode, 0); // always get 0th element
            treeModel.removeNodeFromParent(child );
        }


        // add satellites to tree
        for( AbstractSatellite sat : app.satellites() )
        {
            addSat2List( sat );
        }

        // add GS to tree
        for( GroundStation gs : app.groundStations() )
        {
            addGS2List( gs );
        }


    } // refresh sat List from satHash


    // Returns a TreePath containing the specified node.
    public TreePath getPath(TreeNode node)
    {
        List<TreeNode> list = new ArrayList<TreeNode>();

        // Add all nodes to list
        while (node != null)
        {
            list.add(node);
            node = node.getParent();
        }
        Collections.reverse(list);

        // Convert array of nodes to TreePath
        return new TreePath(list.toArray());
    }

}

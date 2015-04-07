/*
 * JTerrainProfileDialog.java
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
 * Created on November 2, 2007, 1:45 PM
 */

package jsattrak.gui;

import jsattrak.objects.AbstractSatellite;
import jsattrak.utilities.J3DEarthComponent;
import jsattrak.utilities.SatListener;

/**
 *
 * @author  sgano
 */
public class JTerrainProfileDialog extends javax.swing.JDialog implements SatListener
{
//    private boolean okHit = false; // if okay was hit
    JSatTrak app; // used to force repaints
    
    J3DEarthComponent j3dDialog;
    
    /** Creates new form JTerrainProfileDialog */
    public JTerrainProfileDialog(java.awt.Frame parent, boolean modal, JSatTrak app, J3DEarthComponent j3dDialog)
    {
        super(parent, modal);
        
        //this.terrainProfileLayer = terrainProfileLayer;
        this.app = app;
        this.j3dDialog = j3dDialog;
        
        initComponents();
        
        //set inital settings
        try
        {
            showTPCheckBox.setSelected( j3dDialog.getTerrainProfileEnabled() );
            // combo box
            satComboBox.removeAllItems();
            for (AbstractSatellite sat : app.satellites())
            {
                satComboBox.addItem(sat.getName());
            }
            satComboBox.setSelectedItem(j3dDialog.getTerrainProfileSat());

            longSpanTextField.setText("" + j3dDialog.getTerrainProfileLongSpan());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        app.registerSatListener(this);
    }
    
    @Override
    public void satAdded(AbstractSatellite sat) {
        satComboBox.addItem(sat.getName());
    }
    
    @Override
    public void satRemoved(AbstractSatellite sat) {
        satComboBox.removeItem(sat.getName());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        showTPCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        satComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        longSpanTextField = new javax.swing.JTextField();
        applyButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Terrain Profile Settings");

        showTPCheckBox.setText("Show Terrain Profiler");
        showTPCheckBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                showTPCheckBoxActionPerformed(evt);
            }
        });

        jLabel1.setText("Profiler Satellite:");

        satComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel2.setText("+/- Longitude Span [deg]:");

        longSpanTextField.setText("10.0");

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                applyButtonActionPerformed(evt);
            }
        });

        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(showTPCheckBox)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(longSpanTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(satComboBox, 0, 129, Short.MAX_VALUE))))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(applyButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(showTPCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(satComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(longSpanTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 93, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(applyButton)
                    .addComponent(okButton)
                    .addComponent(cancelButton)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void showTPCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_showTPCheckBoxActionPerformed
    {//GEN-HEADEREND:event_showTPCheckBoxActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_showTPCheckBoxActionPerformed

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_applyButtonActionPerformed
    {//GEN-HEADEREND:event_applyButtonActionPerformed
        // save settings
        boolean updateMaps = saveSettings();
        
        // force repaint
        app.forceRepainting(updateMaps);
}//GEN-LAST:event_applyButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        // save all settings back to satProp
         boolean updateMaps = saveSettings();
        
        
        // force repaint of 2D window
          // maybe do this from JSatTrack -- when internal frame is closed of this type and ok was hit?
//        okHit = true;
        // force repaint
        app.forceRepainting(updateMaps);
        
        // close internal frame
        try
        {
            this.dispose(); // could setClosed(true)
        }
        catch(Exception e){}
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        // close internal frame
        try
        {
            this.dispose(); // could setClosed(true)
        }
        catch(Exception e){}
    }//GEN-LAST:event_cancelButtonActionPerformed
    
  
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField longSpanTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox satComboBox;
    private javax.swing.JCheckBox showTPCheckBox;
    // End of variables declaration//GEN-END:variables
    
    
    private boolean saveSettings()
    {
        boolean updateMapData = false;
        
       
        // combo box
        j3dDialog.setTerrainProfileSat( (String) satComboBox.getSelectedItem() );

        try
        {
            j3dDialog.setTerrainProfileLongSpan( Double.parseDouble( longSpanTextField.getText() ) );
        }
        catch(Exception e) { e.printStackTrace();}
        
        // last set if layer is visiable
        j3dDialog.setTerrainProfileEnabled(showTPCheckBox.isSelected() );

        return updateMapData;
    } // saveSettings
    
}

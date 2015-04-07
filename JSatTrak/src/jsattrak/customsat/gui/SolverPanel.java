/*
 * SolverPanel.java
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
 * Created on January 22, 2008, 9:44 AM
 */

package jsattrak.customsat.gui;

import java.awt.Color;
import java.util.Vector;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jsattrak.customsat.GoalParameter;
import jsattrak.customsat.InputVariable;
import jsattrak.customsat.SolverNode;
import jsattrak.gui.JSatTrak;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 *
 * @author  sgano
 */
public class SolverPanel extends javax.swing.JPanel
{
    SolverNode sNode;
     JSatTrak app;
    
    // used for diaplying settings panel
    private JInternalFrame iframe; // used to know what its parent frame is - to close window
    
    
    // vectors for input variables -- not needed stored in JXTable - retrive them from there when saving
    //Vector<InputVariable> inputVars = new Vector<InputVariable>(1);
    
    
    /** Creates new form SolverPanel */
    public SolverPanel(SolverNode sNode, JInternalFrame iframe, JSatTrak app)
    {
        this.iframe = iframe;
        this.sNode = sNode;
        this.app = app;
        
        initComponents();
        
        
        // create columns in table for input and output varaibles/parameters
        inputVarJXTable.setColumnControlVisible(true); 
        inputVarJXTable.addHighlighter(new ColorHighlighter(HighlightPredicate.EVEN, Color.WHITE, Color.black)); // even, background, foregrond
        inputVarJXTable.addHighlighter(new ColorHighlighter(HighlightPredicate.ODD, new Color(229, 229, 229), Color.black)); // odd, background, foregrond

        //inputVarJXTable.getColumnExt(4).setVisible(false); // hide column containing Object.
        inputVarJXTable.setSortable(false); // can't sort because of the way we remove rows
        
        goalParamJXTable.setColumnControlVisible(true); 
//        goalParamJXTable.addHighlighter(AlternateRowHighlighter.genericGrey);
        goalParamJXTable.addHighlighter(new ColorHighlighter(HighlightPredicate.EVEN, Color.WHITE, Color.black)); // even, background, foregrond
        goalParamJXTable.addHighlighter(new ColorHighlighter(HighlightPredicate.ODD, new Color(229, 229, 229), Color.black)); // odd, background, foregrond

        goalParamJXTable.setSortable(false); // can't sort because of the way we remove rows
        
        
        // set number render options
        DefaultTableRenderer  tcr = (DefaultTableRenderer )inputVarJXTable.getDefaultRenderer( (new Double(0)).getClass());
        
        
        
        // set values in GUI
        solverComboBox.setSelectedIndex(sNode.getSolver());
        activeCheckBox.setSelected(sNode.isSolverActive());
        iterSpinner.setValue( sNode.getMaxIter() );
        tolTextField.setText("" + sNode.getConvergenceTol());
        
        // fill in tables with current parametesr
        for (InputVariable iv : sNode.getInputVarVec())
        {
            addInputVariable(iv);
        }

        for (GoalParameter gp : sNode.getGoalParamVec())
        {
            addGoalParameter(gp);
        }
        
        
        // auto fit talbes
        inputVarJXTable.packAll();
        
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
        solverComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        iterSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        tolTextField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        inputVarJXTable = new org.jdesktop.swingx.JXTable();
        addInputVarButton = new javax.swing.JButton();
        removeInputVarButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        addGoalParamButton = new javax.swing.JButton();
        removeGoalParamButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        goalParamJXTable = new org.jdesktop.swingx.JXTable();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        applyButton = new javax.swing.JButton();
        activeCheckBox = new javax.swing.JCheckBox();
        revertVariablesButton = new javax.swing.JButton();
        refreshTablesButton = new javax.swing.JButton();

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText("Solver:");

        solverComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Modified Newton-Raphson", "Modified Broyden" }));

        jLabel2.setText("Max Iter:");

        jLabel3.setText("Convergence Tolerance:");

        tolTextField.setText("1.0E-8");

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel4.setText("Input Variables:");

        inputVarJXTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "Name/Description", "Node Name", "Current Value", "Perturbation", "Scaling Factor"
            }
        )
        {
            Class[] types = new Class []
            {
                java.lang.Object.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean []
            {
                false, false, false, true, true
            };

            public Class getColumnClass(int columnIndex)
            {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(inputVarJXTable);

        addInputVarButton.setText("+");
        addInputVarButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addInputVarButtonActionPerformed(evt);
            }
        });

        removeInputVarButton.setText("-");
        removeInputVarButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                removeInputVarButtonActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel5.setText("Goals:");

        addGoalParamButton.setText("+");
        addGoalParamButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addGoalParamButtonActionPerformed(evt);
            }
        });

        removeGoalParamButton.setText("-");
        removeGoalParamButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                removeGoalParamButtonActionPerformed(evt);
            }
        });

        goalParamJXTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "Name/Description", "Node Name", "Desired Value", "Current Value", "Scale"
            }
        )
        {
            boolean[] canEdit = new boolean []
            {
                false, false, true, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(goalParamJXTable);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cancelButtonActionPerformed(evt);
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

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                applyButtonActionPerformed(evt);
            }
        });

        activeCheckBox.setSelected(true);
        activeCheckBox.setText("Solver Active");
        activeCheckBox.setToolTipText("If unselected solver will not be used and child nodes will run normally");

        revertVariablesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/gtk-revert-to-saved.png"))); // NOI18N
        revertVariablesButton.setToolTipText("Reset Variables to Previous Values (This solver node only)");
        revertVariablesButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                revertVariablesButtonActionPerformed(evt);
            }
        });

        refreshTablesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/reload.png"))); // NOI18N
        refreshTablesButton.setToolTipText("Refresh Variables/Goals Values");
        refreshTablesButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                refreshTablesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(solverComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(activeCheckBox))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(iterSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel3)
                                        .addGap(4, 4, 4)
                                        .addComponent(tolTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addGoalParamButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeGoalParamButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(okButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(applyButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(addInputVarButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeInputVarButton)
                                .addGap(72, 72, 72)
                                .addComponent(revertVariablesButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(refreshTablesButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(solverComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(activeCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(iterSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(tolTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(addInputVarButton)
                    .addComponent(removeInputVarButton)
                    .addComponent(revertVariablesButton)
                    .addComponent(refreshTablesButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(removeGoalParamButton)
                    .addComponent(addGoalParamButton)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(applyButton)
                    .addComponent(cancelButton)
                    .addComponent(okButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addInputVarButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addInputVarButtonActionPerformed
    {//GEN-HEADEREND:event_addInputVarButtonActionPerformed
        String windowName = "Input Variables";
        JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);
        
        // show  window
        AddInputVar panel = new AddInputVar(sNode,this); // non-modal version

        iframe.setContentPane( panel );
        iframe.setSize(180+40,300); // w,h
        iframe.setLocation(5,5);
              
        app.addInternalFrame(iframe);
    }//GEN-LAST:event_addInputVarButtonActionPerformed

    private void removeInputVarButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeInputVarButtonActionPerformed
    {//GEN-HEADEREND:event_removeInputVarButtonActionPerformed

        // because of the way this works -- only good solution
        // the variables can not be sorted :(
        
        int[] rows = inputVarJXTable.getSelectedRows();
                
        for(int i=rows.length-1; i>=0;i--)
        {
            //inputVarJXTable.remove(rows[i]);
            ((DefaultTableModel)inputVarJXTable.getModel()).removeRow(rows[i]);
            
        }
    }//GEN-LAST:event_removeInputVarButtonActionPerformed

    private void addGoalParamButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addGoalParamButtonActionPerformed
    {//GEN-HEADEREND:event_addGoalParamButtonActionPerformed
        String windowName = "Goal Variables";
        JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);
        
        // show satellite browser window
        AddGoalParam panel = new AddGoalParam(sNode,this); // non-modal version

        iframe.setContentPane( panel );
        iframe.setSize(180+40,300); // w,h
        iframe.setLocation(5,5);
              
        app.addInternalFrame(iframe);
    }//GEN-LAST:event_addGoalParamButtonActionPerformed

    private void removeGoalParamButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeGoalParamButtonActionPerformed
    {//GEN-HEADEREND:event_removeGoalParamButtonActionPerformed
         int[] rows = goalParamJXTable.getSelectedRows();
                
        for(int i=rows.length-1; i>=0;i--)
        {
            //inputVarJXTable.remove(rows[i]);
            ((DefaultTableModel)goalParamJXTable.getModel()).removeRow(rows[i]);
            
        }
    }//GEN-LAST:event_removeGoalParamButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        boolean success = saveSettings();
        
         // close internal frame
        if (success)
        {
            try
            {
                iframe.dispose(); // could setClosed(true)
            }
            catch (Exception e)
            {
            }
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        // close internal frame
        try
        {
            iframe.dispose(); // could setClosed(true)
        }
        catch(Exception e){}
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_applyButtonActionPerformed
    {//GEN-HEADEREND:event_applyButtonActionPerformed
         saveSettings();
    }//GEN-LAST:event_applyButtonActionPerformed

    private void revertVariablesButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_revertVariablesButtonActionPerformed
    {//GEN-HEADEREND:event_revertVariablesButtonActionPerformed
       for(InputVariable iv : sNode.getInputVarVec())
       {
           iv.setPrevioustoCurrentValue();
       }
       
       // now reset table (since values have changed)
       refreshTables();
}//GEN-LAST:event_revertVariablesButtonActionPerformed

    private void refreshTablesButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_refreshTablesButtonActionPerformed
    {//GEN-HEADEREND:event_refreshTablesButtonActionPerformed
        refreshTables();
}//GEN-LAST:event_refreshTablesButtonActionPerformed
    
    
    private boolean saveSettings()
    {
        // save settings back to Node
        boolean saveSuccess = true;
        
        // save values from GUI
        sNode.setSolver( solverComboBox.getSelectedIndex() );
        sNode.setSolverActive(activeCheckBox.isSelected());
        sNode.setMaxIter(  (Integer)iterSpinner.getValue() );
        try
        {
             sNode.setConvergenceTol( Double.parseDouble( tolTextField.getText() ) );
        }
        catch(Exception e) 
        {
            JOptionPane.showMessageDialog(this, "Data format error, check input (Convergence Tolerance).", "Data ERROR", JOptionPane.ERROR_MESSAGE);
            saveSuccess = false;
        }
        
        // Get input parameters and goals from Tables
        try
        {
            Vector<InputVariable> inputVarVec = new Vector<InputVariable>(1);
            for (int i = 0; i < inputVarJXTable.getModel().getRowCount(); i++)
            {
                // get var
                InputVariable var = (InputVariable) inputVarJXTable.getModel().getValueAt(i, 0);
                // save editable settings
                var.setDx( Double.parseDouble(inputVarJXTable.getModel().getValueAt(i, 3).toString()) );
                var.setScale(Double.parseDouble(inputVarJXTable.getModel().getValueAt(i, 4).toString() ));

                inputVarVec.add(var);
            }
            sNode.setInputVarVec(inputVarVec);

            Vector<GoalParameter> goalParamVec = new Vector<GoalParameter>(1);
            for (int i = 0; i < goalParamJXTable.getModel().getRowCount(); i++)
            {
                // get param
                GoalParameter gp = (GoalParameter) goalParamJXTable.getModel().getValueAt(i, 0);
                // save editable settings
                gp.setGoalValue(Double.parseDouble(goalParamJXTable.getModel().getValueAt(i, 2).toString()) );
                gp.setScale(Double.parseDouble(goalParamJXTable.getModel().getValueAt(i, 4).toString()) );

                goalParamVec.add(gp);
            }
            sNode.setGoalParamVec(goalParamVec);

        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Data format error, check input (Table Values).", "Data ERROR", JOptionPane.ERROR_MESSAGE);
            saveSuccess = false;
        }
        
        
        return saveSuccess;
    } // savesettings
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox activeCheckBox;
    private javax.swing.JButton addGoalParamButton;
    private javax.swing.JButton addInputVarButton;
    private javax.swing.JButton applyButton;
    private javax.swing.JButton cancelButton;
    private org.jdesktop.swingx.JXTable goalParamJXTable;
    private org.jdesktop.swingx.JXTable inputVarJXTable;
    private javax.swing.JSpinner iterSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton okButton;
    private javax.swing.JButton refreshTablesButton;
    private javax.swing.JButton removeGoalParamButton;
    private javax.swing.JButton removeInputVarButton;
    private javax.swing.JButton revertVariablesButton;
    private javax.swing.JComboBox solverComboBox;
    private javax.swing.JTextField tolTextField;
    // End of variables declaration//GEN-END:variables
    
    public void setIframe(JInternalFrame iframe)
    {
        this.iframe = iframe;
    }
    
    public void addInputVariable(InputVariable iv)
    {                
        ((DefaultTableModel)inputVarJXTable.getModel()).addRow( new Object[] {iv,iv.getParentNode().getValueAt(0).toString() ,iv.getValue(),iv.getDx(),iv.getScale()});
        
    }
    
    public void addGoalParameter(GoalParameter iv)
    {                
        ((DefaultTableModel)goalParamJXTable.getModel()).addRow( new Object[] {iv,iv.getParentNode().getValueAt(0).toString() ,iv.getGoalValue(),iv.getValue(),iv.getScale()});
        
    }
    
    public void refreshTables()
    {
        // refresh all tables 
        
        // clear tables
        for(int i = 0; i<((DefaultTableModel)goalParamJXTable.getModel()).getRowCount();i++)
        {
            ((DefaultTableModel)goalParamJXTable.getModel()).removeRow(0);
        }
        for(int i = 0; i<((DefaultTableModel)inputVarJXTable.getModel()).getRowCount();i++)
        {
            ((DefaultTableModel)inputVarJXTable.getModel()).removeRow(0);
        }

        
        // fill in tables with current parametesr
        for (InputVariable iv : sNode.getInputVarVec())
        {
            addInputVariable(iv);
        }

        for (GoalParameter gp : sNode.getGoalParamVec())
        {
            addGoalParameter(gp);
        }
        
    }
   
     
}
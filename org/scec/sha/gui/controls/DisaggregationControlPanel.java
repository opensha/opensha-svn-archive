package org.scec.sha.gui.controls;

import java.awt.*;
import javax.swing.*;

import org.scec.param.DoubleParameter;
import org.scec.param.ParameterAPI;
import org.scec.param.ParameterConstraintAPI;
import org.scec.param.editor.DoubleParameterEditor;
import org.scec.param.event.ParameterChangeFailListener;
import org.scec.param.event.ParameterChangeFailEvent;
import java.awt.event.*;


/**
 * <p>Title: DisaggregationControlPanel</p>
 * <p>Description: This is control panel in which user can choose whether
 * to choose disaggregation or not. In addition, prob. can be input by the user</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class DisaggregationControlPanel extends JFrame
    implements ParameterChangeFailListener{
  private JCheckBox diaggregateCheckBox = new JCheckBox();
  private JLabel jLabel1 = new JLabel();
  private JButton okButton = new JButton();
  private JButton cancelButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  //Disaggregation Parameter
  private DoubleParameter disaggregationParam =
      new DoubleParameter("Disaggregation Prob", 0, 1, new Double(.01));
  private DoubleParameterEditor disaggregationEditor=new DoubleParameterEditor();

  // applet which called this control panel
  DisaggregationControlPanelAPI parent;

  public DisaggregationControlPanel(DisaggregationControlPanelAPI parent) {
    try {
      this.parent= parent;
      disaggregationParam.addParameterChangeFailListener(this);
      disaggregationEditor.setParameter(disaggregationParam);
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  // initialize the gui components
  private void jbInit() throws Exception {
    diaggregateCheckBox.setForeground(new Color(80, 80, 133));
    diaggregateCheckBox.setText("Disaggregrate");
    diaggregateCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        diaggregateCheckBox_actionPerformed(e);
      }
    });
    this.getContentPane().setLayout(gridBagLayout1);
    jLabel1.setFont(new java.awt.Font("Dialog", 1, 18));
    jLabel1.setForeground(new Color(80, 80, 133));
    jLabel1.setText("Disaggregation Control Panel");
    okButton.setBackground(new Color(200, 200, 230));
    okButton.setFont(new java.awt.Font("Dialog", 1, 12));
    okButton.setForeground(new Color(80, 80, 133));
    okButton.setText("OK");
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okButton_actionPerformed(e);
      }
    });
    cancelButton.setBackground(new Color(200, 200, 230));
    cancelButton.setFont(new java.awt.Font("Dialog", 1, 12));
    cancelButton.setForeground(new Color(80, 80, 133));
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });

    this.getContentPane().add(jLabel1,  new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 0, 14), 0, 6));
    this.getContentPane().add(diaggregateCheckBox,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(15, 14, 0, 39), 31, 0));
    this.getContentPane().add(this.disaggregationEditor,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
           ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(15, 14, 0, 39), 31, 0));
    this.getContentPane().add(cancelButton,  new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(75, 0, 9, 14), 25, 9));
    this.getContentPane().add(okButton,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(75, 96, 9, 0), 51, 9));

  }


  /**
   *  Shown when a Constraint error is thrown on Disaggregation ParameterEditor
   *
   * @param  e  Description of the Parameter
   */
  public void parameterChangeFailed( ParameterChangeFailEvent e ) {

    StringBuffer b = new StringBuffer();
    ParameterAPI param = ( ParameterAPI ) e.getSource();

    ParameterConstraintAPI constraint = param.getConstraint();
    String oldValueStr = e.getOldValue().toString();
    String badValueStr = e.getBadValue().toString();
    String name = param.getName();


    b.append( "The value ");
    b.append( badValueStr );
    b.append( " is not permitted for '");
    b.append( name );
    b.append( "'.\n" );
    b.append( "Resetting to ");
    b.append( oldValueStr );
    b.append( ". The constraints are: \n");
    b.append( constraint.toString() );

    JOptionPane.showMessageDialog(
        this, b.toString(),
        "Cannot Change Value", JOptionPane.INFORMATION_MESSAGE
        );

  }

  /**
   * Only show disaggregration prob parameter when disaggregration is selected
   *
   * @param e
   */
  void diaggregateCheckBox_actionPerformed(ActionEvent e) {
    if(this.diaggregateCheckBox.isSelected())
      disaggregationEditor.setVisible(true);
    else disaggregationEditor.setVisible(false);
  }

  /**
   * this function is called when Ok button is selected
   * @param e
   */
  void okButton_actionPerformed(ActionEvent e) {
    // set the diaggregation parameters in the parent
    boolean diaggregationSelected = diaggregateCheckBox.isSelected();
    parent.setDisaggregationSelected(diaggregationSelected);
    if(diaggregationSelected)
      parent.setDisaggregationProb(((Double)this.disaggregationParam.getValue()).doubleValue());
    this.dispose();
  }

  /**
   * this function is called when Cancel button is selected
   * @param e
   */
  void cancelButton_actionPerformed(ActionEvent e) {
    this.dispose();
  }

}
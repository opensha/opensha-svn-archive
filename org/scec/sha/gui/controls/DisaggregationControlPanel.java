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

  //Disaggregation Parameter
  private DoubleParameter disaggregationParam =
      new DoubleParameter("Disaggregation Prob", 0, 1, new Double(.01));
  private DoubleParameterEditor disaggregationEditor=new DoubleParameterEditor();

  // applet which called this control panel
  DisaggregationControlPanelAPI parent;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  public DisaggregationControlPanel(DisaggregationControlPanelAPI parent,
                                    Component parentComponent) {
    try {
      jbInit();
      this.parent= parent;
      disaggregationParam.addParameterChangeFailListener(this);
      disaggregationEditor.setParameter(disaggregationParam);
      // show the window at center of the parent component
      this.setLocation(parentComponent.getX()+parentComponent.getWidth()/2,
                     parentComponent.getY()+parentComponent.getHeight()/2);

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

    this.setTitle("Disaggregation Control Panel");
    this.getContentPane().add(diaggregateCheckBox,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(15, 6, 18, 0), 15, 0));
    this.getContentPane().add(this.disaggregationEditor,  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
          ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 1, 0, 0), 15, 0));
    //diaggregateCheckBox.setSelected(false);
    disaggregationEditor.setVisible(false);
    this.setSize(320,70);
  }


  /**
   *  Shown when a Constraint error is thrown on Disaggregation ParameterEditor
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
   * @param e
   */
  void diaggregateCheckBox_actionPerformed(ActionEvent e) {
    parent.setDisaggregationSelected(diaggregateCheckBox.isSelected());
    if(this.diaggregateCheckBox.isSelected()) {
      disaggregationEditor.setVisible(true);
      parent.setDisaggregationProb(((Double)this.disaggregationParam.getValue()).doubleValue());
    }
    else disaggregationEditor.setVisible(false);
  }

}
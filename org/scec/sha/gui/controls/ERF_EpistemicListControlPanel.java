package org.scec.sha.gui.controls;

import java.awt.*;
import javax.swing.*;

import org.scec.param.DoubleParameter;
import org.scec.param.editor.DoubleParameterEditor;
import org.scec.param.event.*;
import org.scec.param.ParameterAPI;
import org.scec.param.ParameterConstraintAPI;
import java.awt.event.*;

/**
 * <p>Title: ERF Epistemic List Control Panel</p>
 * <p>Description: This window will allow the user to select the percentile to be
 * plotted for the ERF list</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ERF_EpistemicListControlPanel extends JFrame
    implements ParameterChangeFailListener{
  private JCheckBox allCurvesCheckBox = new JCheckBox();
  private JComboBox percentileComboBox = new JComboBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  // static Strings to be shown in Percentile pick list
  private final static String NO_PERCENTILE = "No Percentile";
  private final static String FIVE_50_95_PERCENTILE = "5th, 50th and 95th Percentile";
  private final static String CUSTOM_PERCENTILE = "Custom Percentile";

  //percentile Parameter
  private DoubleParameter percentileParam =
      new DoubleParameter("Percentile", 0, 100, new Double(50));
  private DoubleParameterEditor percentileEditor=new DoubleParameterEditor();


  public ERF_EpistemicListControlPanel(Component parentComponent) {
    try {
      jbInit();
      initPercentileCombo();
      // show the window at center of the parent component
      this.setLocation(parentComponent.getX()+parentComponent.getWidth()/2,
                     parentComponent.getY()+parentComponent.getHeight()/2);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    allCurvesCheckBox.setForeground(new Color(80, 80, 133));
    allCurvesCheckBox.setSelected(true);
    allCurvesCheckBox.setText("Plot all curves in one color");
    this.setTitle("Epistemic List Control");
    this.getContentPane().setLayout(gridBagLayout1);
    percentileComboBox.setBackground(new Color(200, 200, 230));
    percentileComboBox.setForeground(new Color(80, 80, 133));
    percentileComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        percentileComboBox_actionPerformed(e);
      }
    });
    this.getContentPane().add(allCurvesCheckBox,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 10, 0, 13), 20, -8));
    this.getContentPane().add(percentileComboBox,  new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 10, 13), 60, -1));

    // set the percentile editor
    percentileParam.addParameterChangeFailListener(this);
    percentileEditor.setParameter(percentileParam);

    // add the percentile editor to the window
    this.getContentPane().add(percentileEditor,  new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 10, 13), 60, -1));
    percentileEditor.setVisible(false);

    // set the size
    this.setSize(220,175);
  }

  /**
   * Initialize the percentile combo box
   */
  private void initPercentileCombo() {
    percentileComboBox.addItem(NO_PERCENTILE);
    percentileComboBox.addItem(FIVE_50_95_PERCENTILE);
    percentileComboBox.addItem(CUSTOM_PERCENTILE);
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
   * this function is called whenever user selects anything in percentile pick list
   * @param e
   */
  void percentileComboBox_actionPerformed(ActionEvent e) {
    String selected = percentileComboBox.getSelectedItem().toString();
    if(selected.equalsIgnoreCase(this.CUSTOM_PERCENTILE))
      this.percentileEditor.setVisible(true);
    else percentileEditor.setVisible(false);

  }
}
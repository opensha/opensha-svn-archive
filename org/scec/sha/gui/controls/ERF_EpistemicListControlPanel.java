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

  // static Strings to be shown in Percentile pick list
  public final static String NO_PERCENTILE = "No Percentile";
  public final static String FIVE_50_95_PERCENTILE = "5th, 50th and 95th Percentile";
  public final static String CUSTOM_PERCENTILE = "Custom Percentile";

  //percentile Parameter
  private DoubleParameter percentileParam =
      new DoubleParameter("Percentile", 0, 100, new Double(50));
  private DoubleParameterEditor percentileEditor=new DoubleParameterEditor();

  // saving the instance of caller class
  ERF_EpistemicListControlPanelAPI api;
  private JCheckBox avgCheckBox = new JCheckBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  /**
   *
   * @param api : the calling class. It should implement the ERF_EpistemicListControlPanelAPI
   * @param parentComponent
   */
  public ERF_EpistemicListControlPanel(ERF_EpistemicListControlPanelAPI api,
                                       Component parentComponent) {
    try {
      jbInit();
      this.api = api;
      initPercentileCombo();
      // show the window at center of the parent component
      this.setLocation(parentComponent.getX()+parentComponent.getWidth()/2,
                     parentComponent.getY()+parentComponent.getHeight()/2);
      // set the initial values in the caller
      api.setPercentileOption(percentileComboBox.getSelectedItem().toString());
      api.setAverageSelected(this.avgCheckBox.isSelected());
      api.setPlotAllCurves(this.allCurvesCheckBox.isSelected());
      api.setPercentileOption(percentileComboBox.getSelectedItem().toString());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    allCurvesCheckBox.setForeground(new Color(80, 80, 133));
    allCurvesCheckBox.setActionCommand("Plot all curves in one color");
    allCurvesCheckBox.setSelected(true);
    allCurvesCheckBox.setText("Plot all curves (in one color)");
    allCurvesCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        allCurvesCheckBox_actionPerformed(e);
      }
    });
    this.setTitle("Epistemic List Control");
    this.getContentPane().setLayout(gridBagLayout1);
    percentileComboBox.setBackground(new Color(200, 200, 230));
    percentileComboBox.setForeground(Color.red);
    percentileComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        percentileComboBox_actionPerformed(e);
      }
    });

    avgCheckBox.setForeground(Color.green);
    avgCheckBox.setText("Average");
    avgCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        avgCheckBox_actionPerformed(e);
      }
    });
    this.getContentPane().add(percentileComboBox,    new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 8, 5, 0), 0, 0));

    // set the percentile editor
   percentileParam.addParameterChangeFailListener(this);
   percentileEditor.setParameter(percentileParam);

   // add the percentile editor to the window
   this.getContentPane().add(percentileEditor,  new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    this.getContentPane().add(allCurvesCheckBox,   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 10, 2, 13), 10, 3));
    this.getContentPane().add(avgCheckBox,        new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 10, 4, 13), 126, 0));

    percentileEditor.setVisible(false);

    // set the size
    this.setSize(220,200);
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
    // update the option in the calling class also
    api.setPercentileOption(selected);

  }

  /**
   * This function returns custom percentile value
   * @return :double value of percentile between 0 and 100
   */
  public double getCustomPercentileValue() {
    return ((Double)percentileParam.getValue()).doubleValue();
  }

  /**
   * This function sets the custom percentile value.
   * @param value : Value of the percentile to be set.
   */
  public void setCustomPercentileValue(double value) {
    percentileComboBox.setSelectedItem(CUSTOM_PERCENTILE);
    percentileParam.setValue(value);
    percentileEditor.refreshParamEditor();
  }

  /**
   * this function is called whenever check box for "plotting all curves" is selected
   * or deselected
   * @param e
   */
  void allCurvesCheckBox_actionPerformed(ActionEvent e) {
     // update the value in calling class as well
     api.setPlotAllCurves(this.allCurvesCheckBox.isSelected());
  }

  /**
   * set the average as selected/deselected in the applet as chosen by the user
   * @param e
   */
  void avgCheckBox_actionPerformed(ActionEvent e) {
    api.setAverageSelected(this.avgCheckBox.isSelected());
  }
}
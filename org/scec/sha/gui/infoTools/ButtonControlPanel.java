package org.scec.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import org.jfree.data.Range;

import org.scec.util.*;
import org.scec.sha.gui.infoTools.ButtonControlPanelAPI;
import org.scec.sha.gui.controls.AxisLimitsControlPanel;
import org.scec.sha.gui.controls.AxisLimitsControlPanelAPI;

/**
 * <p>Title: ButtonControlPanel</p>
 * <p>Description: This class creates a button Panel for the Applications:
 * HazardCurveApplet, HazardCurveServerModeApp and HazardSpectrum Applet</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public class ButtonControlPanel extends JPanel implements AxisLimitsControlPanelAPI{
  private JCheckBox jCheckxlog = new JCheckBox();


  // message string to be dispalayed if user chooses Axis Scale
    // without first clicking on "Add Graph"
   private final static String AXIS_RANGE_NOT_ALLOWED =
      new String("First Choose Add Graph. Then choose Axis Scale option");

  //images for the OpenSHA
  private final static String FRAME_ICON_NAME = "openSHA_Aqua_sm.gif";
  private final static String POWERED_BY_IMAGE = "PoweredBy.gif";

  //stores the instance of the application using this ButtonControlPanel
  ButtonControlPanelAPI application;
  private JCheckBox jCheckylog = new JCheckBox();
  private JButton setAxisButton = new JButton();
  private JButton toggleButton = new JButton();

  //Axis Range control panel object (creates the instance for the AxisLimitsControl)
  private AxisLimitsControlPanel axisControlPanel;

  //boolean to check if axis range is auto or custom
  private boolean customAxis = false;
  private FlowLayout flowLayout1 = new FlowLayout();

  public ButtonControlPanel(ButtonControlPanelAPI api) {
    application = api;
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    jCheckxlog.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jCheckxlog_actionPerformed(e);
      }
    });
    jCheckxlog.setText("X Log");
    jCheckxlog.setFont(new java.awt.Font("Dialog", 1, 11));
    this.setLayout(flowLayout1);
    this.setDoubleBuffered(false);
    this.setMinimumSize(new Dimension(0, 0));
    this.setPreferredSize(new Dimension(400, 36));
    jCheckylog.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jCheckylog_actionPerformed(e);
      }
    });
    jCheckylog.setText("Y Log");
    jCheckylog.setFont(new java.awt.Font("Dialog", 1, 11));
    setAxisButton.setText("Set Axis");
    setAxisButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setAxisButton_actionPerformed(e);
      }
    });
    toggleButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        toggleButton_actionPerformed(e);
      }
    });
    toggleButton.setText("Show Data");
    /*toggleButton.setMaximumSize(new Dimension(1100, 26));
    toggleButton.setMinimumSize(new Dimension(90, 26));
    toggleButton.setPreferredSize(new Dimension(106, 26));
    toggleButton.setToolTipText("");*/

    this.add(toggleButton, 0);
    this.add(setAxisButton, 1);
    this.add(jCheckylog, 2);
    this.add(jCheckxlog, 3);
  }
  void imgLabel_mousePressed(MouseEvent e) {

  }
  void imgLabel_mouseReleased(MouseEvent e) {

  }
  void imgLabel_mouseEntered(MouseEvent e) {

  }
  void imgLabel_mouseExited(MouseEvent e) {

  }

  public JButton getToggleButton(){
    return toggleButton;
  }


  /**
   * Action method when yLog is selected or deselected
   * @param e : ActionEvent
   */
  void jCheckylog_actionPerformed(ActionEvent e) {
    application.setY_Log(jCheckylog.isSelected());
  }

  /**
   * Action method when xLog is selected or deselected
   * @param e : ActionEvent
   */
  void jCheckxlog_actionPerformed(ActionEvent e) {
    application.setX_Log(jCheckxlog.isSelected());
  }

  //Action method when the "Toggle Plot" is pressed
  void toggleButton_actionPerformed(ActionEvent e) {
    application.togglePlot();
  }

  //Action method when the "Set Axis Range" button is pressed.
  void setAxisButton_actionPerformed(ActionEvent e) {
    Range xAxisRange = application.getX_AxisRange();
    Range yAxisRange = application.getY_AxisRange();
    if(xAxisRange==null || yAxisRange==null) {
      JOptionPane.showMessageDialog(this,AXIS_RANGE_NOT_ALLOWED);
      return;
    }

    double minX=xAxisRange.getLowerBound();
    double maxX=xAxisRange.getUpperBound();
    double minY=yAxisRange.getLowerBound();
    double maxY=yAxisRange.getUpperBound();
    if(customAxis) { // select the custom scale in the control window
      if(axisControlPanel == null)
        axisControlPanel=new AxisLimitsControlPanel(this, this,
            AxisLimitsControlPanel.CUSTOM_SCALE, minX,maxX,minY,maxY);
      else  axisControlPanel.setParams(AxisLimitsControlPanel.CUSTOM_SCALE,
                                       minX,maxX,minY,maxY);

    }
    else { // select the auto scale in the control window
      if(axisControlPanel == null)
        axisControlPanel=new AxisLimitsControlPanel(this, this,
            AxisLimitsControlPanel.AUTO_SCALE, minX,maxX,minY,maxY);
      else  axisControlPanel.setParams(AxisLimitsControlPanel.AUTO_SCALE,
                                       minX,maxX,minY,maxY);
    }
    axisControlPanel.pack();
    axisControlPanel.show();
  }

  /**
   * sets the range for X and Y axis
   * @param xMin : minimum value for X-axis
   * @param xMax : maximum value for X-axis
   * @param yMin : minimum value for Y-axis
   * @param yMax : maximum value for Y-axis
   *
   */
  public void setAxisRange(double xMin,double xMax, double yMin, double yMax) {
    application.setAxisRange(xMin,xMax,yMin,yMax);
    customAxis=true;
  }

  /**
   * set the auto range for the axis. This function is called
   * from the AxisLimitControlPanel
   */
  public void setAutoRange() {
    application.setAutoRange();
    customAxis = false;
  }

  /**
   * Sets the X-Log CheckBox to be selected or deselected based on the flag
   * @param flag
   */
  public void setXLog(boolean flag){
    jCheckxlog.setSelected(flag);
  }

  /**
   * Sets the Y-Log CheckBox to be selected or deselected based on the flag
   * @param flag
   */
  public void setYLog(boolean flag){
    jCheckylog.setSelected(flag);
  }
}
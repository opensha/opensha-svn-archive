package org.scec.sha.gui.controls;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import org.scec.gui.plot.LogPlotAPI;
/**
 * <p>Title: AxisLimitsControlPanel</p>
 *
 * <p>Description: This Class pop up window when custom scale is selecetd for the combo box that enables the
 * user to customise the X and Y Axis scale</p>

 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class AxisLimitsControlPanel extends JFrame {

  /**
   * @todo variables
   */
  private double minX,maxX;
  private double minY,maxY;

  private JPanel panel1 = new JPanel();
  private JLabel jLabel1 = new JLabel();
  private JTextField jTextMinX = new JTextField();
  private JLabel jLabel2 = new JLabel();
  private JTextField jTextMaxX = new JTextField();
  private JLabel jLabel3 = new JLabel();
  private JTextField jTextMinY = new JTextField();
  private JLabel jLabel4 = new JLabel();
  private JTextField jTextMaxY = new JTextField();
  private JButton ok = new JButton();
  private JButton cancel = new JButton();
  private AxisLimitsControlPanelAPI axisLimitAPI;
  private JComboBox rangeComboBox = new JComboBox();
  private JLabel jLabel5 = new JLabel();
  private JLabel jLabel6 = new JLabel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();

  // Axis scale options
  public final static String AUTO_SCALE = "Auto Scale";
  public final static String CUSTOM_SCALE = "Custom Scale";

  /**
   * Contructor which displays the window so that user can set the X and Y axis
   * range
   * @param axisLimitAPI : AxisLimitsControlPanelAPI needs to be implemented
   * by all the applets which want to use this class
   * @param component The parent component. This is the parent window on which
   * this Axis range window will appear, center aligned
   * @param scale : It can have value "Custom Scale" or "Auto Scale". It specifes
   * what value to be selected initially when this panel comes up
   * @param minX : Current minX value in the parent component
   * @param maxX : Current maxX value in the parent component
   * @param minY : Current minY value in the parent component
   * @param maxY : Current maxY value in the parent component
   */
  public AxisLimitsControlPanel(AxisLimitsControlPanelAPI axisLimitAPI,
                                Component parent, String scale,
                                double minX, double maxX, double minY, double maxY) {
    this.axisLimitAPI= axisLimitAPI;
    this.minX=minX;
    this.minY=minY;
    this.maxX=maxX;
    this.maxY=maxY;
    // show the window at center of the parent component
    this.setLocation(parent.getX()+parent.getWidth()/2,
                     parent.getY()+parent.getHeight()/2);
    this.rangeComboBox.addItem(this.AUTO_SCALE);
    this.rangeComboBox.addItem(this.CUSTOM_SCALE);
    try{
      jbInit();
      this.rangeComboBox.setSelectedItem(scale);
    }catch(Exception e){
      System.out.println("Error Occured while running range combo box: "+e);
    }
  }

  /**
   * This is called whenever this window is shown again
   * So, we need to set the params again
   * @param scale : whether custom scale or auto scale is chosen
   * @param minX : min X value for graph
   * @param maxX : max X value for graph
   * @param minY : min Y value for graph
   * @param maxY : max Y value for graph
   */
  public void setParams(String scale, double minX, double maxX, double minY,
                        double maxY ) {
    // fill in the parameters in the window
    this.minX=minX;
    this.minY=minY;
    this.maxX=maxX;
    this.maxY=maxY;
    this.jTextMinX.setText(""+this.minX);
    this.jTextMaxX.setText(""+this.maxX);
    this.jTextMinY.setText(""+this.minY);
    this.jTextMaxY.setText(""+this.maxY);
    this.rangeComboBox.setSelectedItem(scale);

  }

  void jbInit() throws Exception {
    rangeComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rangeComboBox_actionPerformed(e);
      }
    });
    panel1.setLayout(gridBagLayout1);
    panel1.add(rangeComboBox,  new GridBagConstraints(3, 1, 3, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(26, 0, 0, 0), -4, 0));
    panel1.add(jLabel5,  new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(24, 0, 0, 0), 26, 11));
    panel1.add(jTextMaxY,  new GridBagConstraints(5, 3, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 10), 119, 3));
    panel1.add(jLabel1,  new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(21, 22, 0, 0), 26, 3));
    panel1.add(jTextMinX,  new GridBagConstraints(1, 2, 3, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(22, 0, 0, 0), 112, 3));
    panel1.add(jLabel3,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(13, 22, 0, 0), 14, 0));
    panel1.add(jTextMinY,  new GridBagConstraints(1, 3, 3, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(8, 0, 0, 0), 112, 3));
    panel1.add(jLabel2,  new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(21, 17, 0, 0), 26, 3));
    panel1.add(jTextMaxX,  new GridBagConstraints(5, 2, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(22, 0, 0, 10), 119, 3));
    panel1.add(jLabel4,  new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(14, 17, 0, 11), 15, -2));
    panel1.add(cancel,  new GridBagConstraints(6, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(14, 0, 24, 10), 24, 3));
    panel1.add(ok,  new GridBagConstraints(4, 4, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(14, 17, 24, 18), 35, 3));
    panel1.add(jLabel6,   new GridBagConstraints(0, 0, 7, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 50, 0, 69), 103, 6));
    jLabel1.setForeground(new Color(80, 80, 133));
    jLabel1.setText("Min X:");
    jLabel2.setForeground(new Color(80, 80, 133));
    jLabel2.setText("Max X:");
    jLabel3.setForeground(new Color(80, 80, 133));
    jLabel3.setText("Min Y:");
    jLabel4.setForeground(new Color(80, 80, 133));
    jLabel4.setText("Max Y:");
    ok.setBackground(new Color(200, 200, 230));
    ok.setForeground(new Color(80, 80, 133));
    ok.setText("OK");
    ok.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ok_actionPerformed(e);
      }
    });
    cancel.setBackground(new Color(200, 200, 230));
    cancel.setForeground(new Color(80, 80, 133));
    cancel.setText("Cancel");
    cancel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancel_actionPerformed(e);
      }
    });
    this.getContentPane().setLayout(gridBagLayout2);
    panel1.setBackground(new Color(200, 200, 230));
    panel1.setMaximumSize(new Dimension(348, 143));
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setResizable(false);
    this.jTextMinX.setText(""+this.minX);
    this.jTextMaxX.setText(""+this.maxX);
    this.jTextMinY.setText(""+this.minY);
    this.jTextMaxY.setText(""+this.maxY);

    rangeComboBox.setFont(new java.awt.Font("Dialog", 1, 12));
    rangeComboBox.setForeground(new Color(80, 80, 133));
    jLabel5.setForeground(new Color(80, 80, 133));
    jLabel5.setText("Axis Scale:");
    jLabel6.setFont(new java.awt.Font("Dialog", 1, 18));
    jLabel6.setForeground(new Color(80, 80, 133));
    jLabel6.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel6.setText("Axis Control Panel");
    this.getContentPane().add(panel1,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 0, 0), -48, -7));
  }


  /**
   * This function also calls the setYRange and setXRange functions of the IMRTesterApplet class
   * which sets the range of the axis based on the user input
   *
   * @param e= this event occur when the Ok button is clicked on the custom axis popup window
   */
  void ok_actionPerformed(ActionEvent e) {
    String selectedRange = this.rangeComboBox.getSelectedItem().toString();
    if(selectedRange.equalsIgnoreCase(this.AUTO_SCALE)) { // if auto scale is selected
      axisLimitAPI.setAutoRange();
      this.dispose();
    }
   else { // if custom scale is selected
     try {
       double xMin=Double.parseDouble(this.jTextMinX.getText());
       double xMax=Double.parseDouble(this.jTextMaxX.getText());
       double yMin=Double.parseDouble(this.jTextMinY.getText());
       double yMax=Double.parseDouble(this.jTextMaxY.getText());

       // check whether xMin<=xMax and yMin<=yMax)
       if(xMin>=xMax){
         JOptionPane.showMessageDialog(this,new String("Max X must be greater than Min X"),new String("Check Axis Range"),JOptionPane.ERROR_MESSAGE);
         return;
       }
       if(yMin>=yMax){
         JOptionPane.showMessageDialog(this,new String("Max Y must be greater than Min Y"),new String("Check Axis Range"),JOptionPane.ERROR_MESSAGE);
         return;
       }
       axisLimitAPI.setAxisRange(xMin, xMax, yMin, yMax);
       this.dispose();
     } catch(Exception ex) {
       System.out.println("Exception:"+ex);
       JOptionPane.showMessageDialog(this,new String("Text Entered must be a valid numerical value"),new String("Check Axis Range"),JOptionPane.ERROR_MESSAGE);
     }
   }
  }

  /**
   *
   * @param e= this event occurs to destroy the popup window if the user has selected cancel option
   */
  void cancel_actionPerformed(ActionEvent e) {
    this.dispose();
  }

  /**
   * This is called when user selects "Auto scale" or "Custom scale" option
   * @param e
   */
  void rangeComboBox_actionPerformed(ActionEvent e) {
    String selectedRange = this.rangeComboBox.getSelectedItem().toString();
    if(selectedRange.equalsIgnoreCase(this.AUTO_SCALE)) {
      // if auto scale is selected disable the text boxes
      this.jTextMinX.setEnabled(false);
      this.jTextMaxX.setEnabled(false);
      this.jTextMinY.setEnabled(false);
      this.jTextMaxY.setEnabled(false);
    }
    else {
      // if custom scale is selected enable the text boxes
      this.jTextMinX.setEnabled(true);
      this.jTextMaxX.setEnabled(true);
      this.jTextMinY.setEnabled(true);
      this.jTextMaxY.setEnabled(true);
    }
  }
}

package org.scec.sha.magdist.gui;



import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * <p>Title: MagFreDistAxisScale</p>
 * <p>Description:  This Class pop up window when custom scale is selecetd for the combo box that enables the
 * user to customise the X and Y Axis scale</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta   Date: Aug,21,2002
 * @version 1.0
 */

public class MagFreqDistAxisScale extends JFrame{

  JPanel panel1 = new JPanel();
  JLabel jLabel1 = new JLabel();
  JTextField jTextMinX = new JTextField();
  JLabel jLabel2 = new JLabel();
  JTextField jTextMaxX = new JTextField();
  JLabel jLabel3 = new JLabel();
  JTextField jTextMinY = new JTextField();
  JLabel jLabel4 = new JLabel();
  JTextField jTextMaxY = new JTextField();
  JButton ok = new JButton();
  JButton cancel = new JButton();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  private MagFreqDistTesterApplet magFreqDistTesterApplet;


  public MagFreqDistAxisScale(MagFreqDistTesterApplet magFreqDist) {
    this.magFreqDistTesterApplet= magFreqDist;
    try{
      jbInit();
    }catch(Exception e){
      System.out.println("Error Occured while running range combo box: "+e);
    }
  }
  void jbInit() throws Exception {
    panel1.setLayout(gridBagLayout1);
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
    getContentPane().add(panel1,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), -11, -2));
    panel1.add(jLabel1,  new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(30, 15, 6, 68), 26, 3));
    panel1.add(jLabel3,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(11, 15, 0, 0), 14, 0));
    panel1.add(jTextMinX,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(30, 0, 0, 0), 72, 3));
    panel1.add(jTextMinY,  new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(7, 0, 0, 0), 72, 3));
    panel1.add(jLabel2,  new GridBagConstraints(2, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(30, 17, 6, 78), 26, 3));
    panel1.add(jTextMaxX,  new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(30, 0, 0, 17), 72, 3));
    panel1.add(jTextMaxY,  new GridBagConstraints(3, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 17), 72, 3));
    panel1.add(jLabel4,  new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(11, 17, 0, 0), 15, -2));
    panel1.add(ok,  new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(12, 0, 14, 0), 35, 3));
    panel1.add(cancel,     new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(12, 0, 14, 17), 24, 3));
  }
/**
 * This function also calls the setYRange and setXRange functions of the IMRTesterApplet class
 * which sets the range of the axis based on the user input
 *
 * @param e= this event occur when the Ok button is clicked on the custom axis popup window
 */
  void ok_actionPerformed(ActionEvent e) {
    try {
      float xMin=Float.parseFloat(this.jTextMinX.getText());
      float xMax=Float.parseFloat(this.jTextMaxX.getText());
      if(xMin>=xMax){
        JOptionPane.showMessageDialog(this,new String("Max X must be greater than Min X"),new String("Check Axis Range"),JOptionPane.ERROR_MESSAGE);
        return;
      }
      else
        this.magFreqDistTesterApplet.setXRange(xMin,xMax);
      float yMin=Float.parseFloat(this.jTextMinY.getText());
      float yMax=Float.parseFloat(this.jTextMaxY.getText());
      if(yMin>=yMax){
        JOptionPane.showMessageDialog(this,new String("Max Y must be greater than Min Y"),new String("Check Axis Range"),JOptionPane.ERROR_MESSAGE);
        return;
      }
      else
        this.magFreqDistTesterApplet.setYRange(yMin,yMax);
      this.dispose();
    } catch(Exception ex) {
        System.out.println("Exception:"+ex);
        JOptionPane.showMessageDialog(this,new String("Text Entered must be a valid numerical value"),new String("Check Axis Range"),JOptionPane.ERROR_MESSAGE);
    }
  }
/**
 *
 * @param e= this event occurs to destroy the popup window if the user has selected cancel option
 */
  void cancel_actionPerformed(ActionEvent e) {
    this.dispose();
  }
}
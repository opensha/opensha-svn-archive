package org.scec.sha.fault.demo;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import  java.util.Vector;
import java.util.StringTokenizer;

import org.scec.sha.fault.FaultTrace;
import org.scec.data.Location;

/**
 * <p>Title:  CustomFault.java </p>
 * <p>Description: This window is needed so that users can specify their own
 * faults and see them  </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date Nov 16, 2002
 * @version 1.0
 */

public class CustomFault extends JFrame {
  private JTextArea dipTextArea = new JTextArea();
  private JTextArea depthTextArea = new JTextArea();
  private JTextArea traceTextArea = new JTextArea();
  private JLabel dipLabel = new JLabel();
  private JLabel traceLabel = new JLabel();
  private JLabel depthLabel = new JLabel();
  private JLabel titleLabel = new JLabel();
  private JButton addButton = new JButton();
  private JButton cancelButton = new JButton();
  private Border border1;
  private GriddedFaultApplet applet;
  private JLabel faultNameLabel = new JLabel();
  private JTextField faultNameText = new JTextField();


  /**
   * constructor which accepts the GriddedFaultApplet as a parameter
   * It is needed to pass the values enterd by user back to the applet
   *
   * @param applet
   */
  public CustomFault(GriddedFaultApplet applet) {
    try {
      this.applet = applet;
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    border1 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(178, 178, 178));
    dipTextArea.setBounds(new Rectangle(6, 94, 156, 166));
    this.getContentPane().setLayout(null);
    depthTextArea.setBorder(border1);
    depthTextArea.setBounds(new Rectangle(204, 93, 176, 170));
    traceTextArea.setBounds(new Rectangle(9, 291, 370, 171));
    dipLabel.setText("Dips (degrees):");
    dipLabel.setBounds(new Rectangle(9, 72, 98, 21));
    traceLabel.setText("Fault Trace:");
    traceLabel.setBounds(new Rectangle(8, 270, 79, 19));
    depthLabel.setText("Depths:");
    depthLabel.setBounds(new Rectangle(203, 70, 92, 24));
    titleLabel.setFont(new java.awt.Font("Lucida Grande", 1, 20));
    titleLabel.setText("Custom Fault");
    titleLabel.setBounds(new Rectangle(114, 0, 154, 31));
    addButton.setBounds(new Rectangle(169, 475, 104, 24));
    addButton.setText("Add Fault");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });
    cancelButton.setBounds(new Rectangle(286, 473, 91, 25));
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });
    faultNameLabel.setText("Fault Name:");
    faultNameLabel.setBounds(new Rectangle(8, 39, 75, 24));
    faultNameText.setBounds(new Rectangle(85, 41, 129, 23));
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.getContentPane().add(titleLabel, null);
    this.getContentPane().add(dipLabel, null);
    this.getContentPane().add(depthTextArea, null);
    this.getContentPane().add(dipTextArea, null);
    this.getContentPane().add(traceLabel, null);
    this.getContentPane().add(traceTextArea, null);
    this.getContentPane().add(addButton, null);
    this.getContentPane().add(cancelButton, null);
    this.getContentPane().add(faultNameLabel, null);
    this.getContentPane().add(faultNameText, null);
    this.getContentPane().add(depthLabel, null);
  }


  /**
   * This function is called when a new custom fault is desired to be added
   *
   * @param e
   */
  void addButton_actionPerformed(ActionEvent e) {

     try {
       // check for dips text area
       // vector of dips
       Vector dips = new Vector();
       // first check the dips. Check that there is only one value in 1 row
       // also only numbers are allowed
       String dipText= this.dipTextArea.getText();
       // first read each line and then check that there is only one value in it
       StringTokenizer lineToken = new StringTokenizer(dipText,"\n", false);
       while(lineToken.hasMoreTokens()) {
         String line = lineToken.nextToken();
         StringTokenizer token = new StringTokenizer(line,"\t ");
         if(token.countTokens() > 1)
           throw new RuntimeException("Only 1 value in each row is allowed in dip values");
         // add this dip. If it is not numeric charcter, then exceeption will be thrown and caught
         dips.add(new Double(token.nextToken()));
       }


       // check for depths text area
       // vector of depths
        Vector depths = new Vector();
        // first check the depths. Check that there is only one value in 1 row
        // also only numbers are allowed
        String depthText= this.depthTextArea.getText();
        // first read each line and then check that there is only one value in it
        lineToken = new StringTokenizer(depthText,"\n", false);
        while(lineToken.hasMoreTokens()) {
          String line = lineToken.nextToken();
          StringTokenizer token = new StringTokenizer(line,"\t ");
          if(token.countTokens() > 1)
            throw new RuntimeException("Only 1 value in each row is allowed in depth values");
          // add this depth. If it is not numeric charcter, then exceeption will be thrown and caught
          depths.add(new Double(token.nextToken()));
       }

       //check that number of depths are 1 greater than the number of dips
       if( (dips.size()+1) != depths.size())
         throw new RuntimeException("Number of depths should be 1 greater than number of dips");

       //check for the fault Trace
       String faultName = this.faultNameText.getText().trim();
       if(faultName.equalsIgnoreCase(""))
         throw new RuntimeException("Select the fault Name");
       FaultTrace faultTrace = new FaultTrace(faultName);

        // first check the depths. Check that there are only 3 values in 1 row
        // also only numbers are allowed
        String traceText= this.traceTextArea.getText();
        // first read each line and then check that there are only 3 values in it
        lineToken = new StringTokenizer(traceText,"\n", false);
        double lat, lon, depth;
        while(lineToken.hasMoreTokens()) {
          String line = lineToken.nextToken();
          StringTokenizer token = new StringTokenizer(line,"\t ");
          if(token.countTokens() !=3 )
            throw new RuntimeException("Only 3 values in each row are allowed in fault trace");
          // get latitude, longitude and depth
          lat = (new Double(token.nextToken())).doubleValue();
          lon = (new Double(token.nextToken())).doubleValue();
          depth = (new Double(token.nextToken())).doubleValue();
          faultTrace.addLocation(new Location(lat, lon, depth));
       }
       applet.setCustomFaultParams(dips, depths, faultTrace);
       this.dispose();
     }catch (RuntimeException ex) {
       String message = ex.getMessage();
       // show the message if user entered invalid data
       if(message!=null)
         JOptionPane.showMessageDialog(this, message);
       else
         ex.printStackTrace();
       return;
     }

  }

  /**
   * When cancel button is selected
   *
   * @param e
   */
  void cancelButton_actionPerformed(ActionEvent e) {
    this.dispose();
  }
}
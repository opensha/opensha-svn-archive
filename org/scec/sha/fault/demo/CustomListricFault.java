package org.scec.sha.fault.demo;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import  java.util.Vector;
import java.util.StringTokenizer;

import org.scec.sha.fault.FaultTrace;
import org.scec.data.Location;
import org.scec.util.FaultUtils;

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

public class CustomListricFault extends JFrame {
  private Border border1;
  private GriddedFaultApplet applet;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel1 = new JPanel();
  private JLabel dipLabel = new JLabel();
  private JTextArea depthTextArea = new JTextArea();
  private JLabel depthLabel = new JLabel();
  private JButton cancelButton = new JButton();
  private JTextArea traceTextArea = new JTextArea();
  private JLabel titleLabel = new JLabel();
  private JTextArea dipTextArea = new JTextArea();
  private JLabel faultNameLabel = new JLabel();
  private JTextField faultNameText = new JTextField();
  private JButton addButton = new JButton();
  private JLabel traceLabel = new JLabel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();


  /**
   * constructor which accepts the GriddedFaultApplet as a parameter
   * It is needed to pass the values enterd by user back to the applet
   *
   * @param applet
   */
  public CustomListricFault(GriddedFaultApplet applet) {
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
    this.getContentPane().setLayout(borderLayout1);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    jPanel1.setLayout(gridBagLayout1);
    dipLabel.setText("Dips (degrees):");
    depthTextArea.setBorder(border1);
    depthTextArea.setMaximumSize(new Dimension(2147483647, 85));
    depthTextArea.setLineWrap(true);
    depthTextArea.setRows(5);
    depthTextArea.setWrapStyleWord(true);
    depthLabel.setText("Depths:");
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });
    titleLabel.setFont(new java.awt.Font("Lucida Grande", 1, 20));
    titleLabel.setText("Custom Listric Fault");
    faultNameLabel.setText("Fault Name:");
    addButton.setText("Add Fault");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });
    traceLabel.setText("Fault Trace:");
    traceTextArea.setLineWrap(true);
    dipTextArea.setMaximumSize(new Dimension(2147483647, 85));
    dipTextArea.setLineWrap(true);
    dipTextArea.setRows(5);
    this.getContentPane().add(jPanel1,  BorderLayout.CENTER);
    jPanel1.add(dipLabel,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 6, 0, 33), 21, 4));
    jPanel1.add(titleLabel,  new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 73, 0, 37), 34, 7));
    jPanel1.add(dipTextArea,        new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 6, 0, 10), 0, 31));
    jPanel1.add(faultNameLabel,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 6, 0, 48), 29, 7));
    jPanel1.add(faultNameText,  new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(8, 103, 0, 0), 86, 2));
    jPanel1.add(traceTextArea,  new GridBagConstraints(0, 5, 3, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 6, 0, 12), 0, 123));
    jPanel1.add(traceLabel,  new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(11, 6, 0, 25), 51, 2));
    jPanel1.add(depthTextArea,       new GridBagConstraints(1, 3, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 0, 12), 0, 31));
    jPanel1.add(depthLabel,  new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 13, 0, 83), 32, 7));
    jPanel1.add(addButton,  new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 60, 12, 0), 4, -3));
    jPanel1.add(cancelButton,     new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(2, 1, 10, 54), 16, -2));
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
       double dip;
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
         dip=Double.parseDouble(token.nextToken());
         dips.add(new Double(dip));
         FaultUtils.assertValidDip(dip);
       }


       // check for depths text area
       // vector of depths
        Vector depths = new Vector();
        double upperSeismo = Double.NaN;
        double lowerSeismo;
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
          lowerSeismo = Double.parseDouble(token.nextToken());
          // check that depths are in increasing order
          if(depths.size()!=0)
            FaultUtils.assertValidSeisUpperAndLower(upperSeismo, lowerSeismo);
          // add this depth. If it is not numeric charcter, then exceeption will be thrown and caught
          depths.add(new Double(lowerSeismo));
          upperSeismo = lowerSeismo;
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
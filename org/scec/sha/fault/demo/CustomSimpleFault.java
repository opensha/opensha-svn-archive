package org.scec.sha.fault.demo;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.StringTokenizer;

import org.scec.sha.fault.FaultTrace;
import org.scec.data.Location;
import org.scec.sha.fault.SimpleFaultData;
import org.scec.util.FaultUtils;

/**
 * <p>Title: CustomSimpleFault</p>
 * <p>Description: This is the GUI for Custom Simple Fault </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta and Vipin Gupta
 * @date :Nov 18, 2002
 * @version 1.0
 */

public class CustomSimpleFault extends JFrame {
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel1 = new JPanel();
  private JLabel dipLabel = new JLabel();
  private JLabel upperSeismoLabel = new JLabel();
  private JButton cancelButton = new JButton();
  private JTextArea traceTextArea = new JTextArea();
  private JLabel titleLabel = new JLabel();
  private JTextField faultNameText = new JTextField();
  private JLabel faultNameLabel = new JLabel();
  private JButton addButton = new JButton();
  private JLabel traceLabel = new JLabel();
  private JTextField dipText = new JTextField();
  private JTextField upperSeismoText = new JTextField();
  private JLabel lowerSeismoLabel = new JLabel();
  private JTextField lowerSeismoText = new JTextField();
  private GriddedFaultApplet applet;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  /**
   * constructor which accepts the GriddedFaultApplet as a parameter
   * It is needed to pass the values enterd by user back to the applet
   *
   * @param applet
   */
  public CustomSimpleFault(GriddedFaultApplet applet) {
    try {
      this.applet = applet;
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    jPanel1.setLayout(gridBagLayout1);
    dipLabel.setText("Dips (degrees):");
    upperSeismoLabel.setText("Upper Seismo Depth:");
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });
    titleLabel.setFont(new java.awt.Font("Lucida Grande", 1, 20));
    titleLabel.setText("Custom Simple Fault");
    faultNameLabel.setText("Fault Name:");
    addButton.setText("Add Fault");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });
    traceLabel.setText("Fault Trace:");
    lowerSeismoLabel.setText("Lower Seismo Depth:");
    this.getContentPane().add(jPanel1,  BorderLayout.CENTER);
    jPanel1.add(faultNameLabel,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(7, 10, 0, 25), 29, 7));
    jPanel1.add(faultNameText,  new GridBagConstraints(2, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(7, 10, 0, 9), 76, 0));
    jPanel1.add(dipText,  new GridBagConstraints(2, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 9), 76, 0));
    jPanel1.add(upperSeismoText,  new GridBagConstraints(2, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 10, 0, 9), 76, 0));
    jPanel1.add(lowerSeismoText,  new GridBagConstraints(2, 4, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(8, 10, 0, 9), 76, 0));
    jPanel1.add(dipLabel,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 10), 21, 4));
    jPanel1.add(upperSeismoLabel,  new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 10, 0, 0), 18, 7));
    jPanel1.add(lowerSeismoLabel,  new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 19, 9));
    jPanel1.add(traceTextArea,  new GridBagConstraints(0, 6, 3, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 9), 254, 154));
    jPanel1.add(traceLabel,  new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 10, 0, 0), 51, 2));
    jPanel1.add(cancelButton,  new GridBagConstraints(1, 7, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(7, 17, 14, 9), 19, -2));
    jPanel1.add(titleLabel,  new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(11, 27, 0, 9), 27, 7));
    jPanel1.add(addButton,  new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(7, 45, 14, 0), 4, -3));
  }

  /**
   * When cancel button is selected
   * @param e
   */
  void cancelButton_actionPerformed(ActionEvent e) {
    this.dispose();
  }

  /**
   * When add fault is selected
   *
   * @param e
   */
  void addButton_actionPerformed(ActionEvent e) {
    try {


      double dip= Double.parseDouble(this.dipText.getText());
      double upperSeismoDepth = Double.parseDouble(this.upperSeismoText.getText());
      double lowerSeismoDepth = Double.parseDouble(this.lowerSeismoText.getText()) ;

      // check whther dip and upper and lower seismo depth are valid
      FaultUtils.assertValidDip(dip);
      FaultUtils.assertValidSeisUpperAndLower(upperSeismoDepth, lowerSeismoDepth);

      //check for the fault Trace
      String faultName = this.faultNameText.getText().trim();
      if(faultName.equalsIgnoreCase(""))
        throw new RuntimeException("Select the fault Name");

      FaultTrace faultTrace = new FaultTrace(faultName);

       // first check the depths. Check that there are only 3 values in 1 row
       // also only numbers are allowed
       String traceText= this.traceTextArea.getText();
       // first read each line and then check that there are only 3 values in it
       StringTokenizer lineToken = new StringTokenizer(traceText,"\n", false);
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

      // make simple data object and pass it to fault applet to be displayed
      SimpleFaultData simpleFaultData = new SimpleFaultData(dip,
          lowerSeismoDepth, upperSeismoDepth, faultTrace);
      applet.setCustomSimpleFault(simpleFaultData);
      this.dispose();
    }catch (RuntimeException ex) {
      String message = ex.getMessage();
      // show the message if user entered invalid data
      if(message!=null) JOptionPane.showMessageDialog(this, message);
      else JOptionPane.showMessageDialog(this, e.toString());
      return;
    }

  }
}
package org.scec.sha.earthquake.PEER_TestCases.PEER_TestGuiPlots;

import java.awt.*;
import javax.swing.*;
import java.util.Vector;
import java.awt.event.*;

/**
 * <p>Title: PEER_FileDeleteWindow</p>
 * <p>Description: This class deletes the PEER data file after checking if the
 * user has input the correct password</p>
 * @author : Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class PEER_FileDeleteWindow extends JFrame {
  JComboBox fileComboBox = new JComboBox();
  JLabel jLabel1 = new JLabel();
  JPasswordField filePassword = new JPasswordField();
  JLabel jLabel2 = new JLabel();
  JButton okButton = new JButton();
  JButton cancelButton = new JButton();

  //Instance of the PEER_TestResultsSubmissionApplet
  PEER_TestResultsSubmissionApplet peer=null;

  //Vector to store all the fileNames, gets is value from the PEER_TestResultsSubmissionApplet
  Vector dataFiles=null;
  GridBagLayout gridBagLayout1 = new GridBagLayout();

  public PEER_FileDeleteWindow(PEER_TestResultsSubmissionApplet p,Vector fileNames) {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    peer=p;
    dataFiles=fileNames;
    int size=dataFiles.size();
    for(int i=0;i<size;++i)
       fileComboBox.addItem(dataFiles.get(i));
    fileComboBox.setSelectedIndex(0);
  }

  private void jbInit() throws Exception {
    this.getContentPane().setLayout(gridBagLayout1);
    fileComboBox.setBackground(new Color(200, 200, 230));
    fileComboBox.setForeground(new Color(80, 80, 133));
    jLabel1.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel1.setForeground(new Color(80, 80, 133));
    jLabel1.setText("Select File to Delete:");
    filePassword.setBackground(new Color(200, 200, 230));
    filePassword.setFont(new java.awt.Font("Dialog", 1, 12));
    filePassword.setForeground(new Color(80, 80, 133));
    jLabel2.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel2.setForeground(new Color(80, 80, 133));
    jLabel2.setText("Enter Password:");
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
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.getContentPane().add(jLabel1,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(34, 20, 0, 0), 3, 11));
    this.getContentPane().add(fileComboBox,  new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(34, 12, 0, 63), 28, 10));
    this.getContentPane().add(jLabel2,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(31, 20, 0, 17), 15, 10));
    this.getContentPane().add(filePassword,  new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(26, 12, 0, 63), 154, 16));
    this.getContentPane().add(cancelButton,  new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(27, 7, 29, 41), 20, 13));
    this.getContentPane().add(okButton,  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(27, 0, 29, 0), 46, 13));
  }

  /**
   * Makes the connection to the servlet if user enters the correct password &
   * confirms that he indeed wants to delete the file
   * @param e
   */
  void okButton_actionPerformed(ActionEvent e) {
    if(!new String(filePassword.getPassword()).equals(new String("PEER"))){
      filePassword.setText("");
      JOptionPane.showMessageDialog(this,new String("Incorrect Password"),"Check Password",
                                    JOptionPane.OK_OPTION);
    }
    else {
      //calls the PEER_TestResultsSubmissionApplet method to open the connection to
      //delete the file selected.
      int flag=JOptionPane.showConfirmDialog(this,new String("Are you sure you want to delete the file"),
                                    "Confirmation Message",JOptionPane.OK_CANCEL_OPTION);

      if(flag == JOptionPane.OK_OPTION)
        peer.openDeleteConnection(fileComboBox.getSelectedItem().toString());
    }

  }

  /**
   *
   * @param e =this event occurs to destroy the popup window if the user has selected cancel option
   */
  void cancelButton_actionPerformed(ActionEvent e) {
    this.dispose();
  }
}
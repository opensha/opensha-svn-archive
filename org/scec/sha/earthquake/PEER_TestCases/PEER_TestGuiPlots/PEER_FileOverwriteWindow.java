package org.scec.sha.earthquake.PEER_TestCases.PEER_TestGuiPlots;

import java.awt.*;
import javax.swing.*;
import java.util.Vector;
import java.awt.event.*;

/**
 * <p>Title: PEER_FileOverwriteWindow</p>
 * <p>Description: This class deletes the PEER data file after checking if the
 * user has input the correct password</p>
 * @author : Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class PEER_FileOverwriteWindow extends JFrame {
  JPasswordField filePassword = new JPasswordField();
  JLabel jLabel2 = new JLabel();
  JButton continueButton = new JButton();
  JButton cancelButton = new JButton();

  //Instance of the PEER_TestResultsSubmissionApplet
  PEER_TestResultsSubmissionApplet peer=null;

  //Contains the name of the file to be overwritten
  String fileName;
  private JLabel jLabel5 = new JLabel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  public PEER_FileOverwriteWindow(PEER_TestResultsSubmissionApplet p,String file) {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    peer=p;
    this.fileName =file ;
  }

  private void jbInit() throws Exception {
    this.getContentPane().setLayout(gridBagLayout1);
    filePassword.setBackground(new Color(200, 200, 230));
    filePassword.setFont(new java.awt.Font("Dialog", 1, 12));
    filePassword.setForeground(new Color(80, 80, 133));
    jLabel2.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel2.setForeground(new Color(80, 80, 133));
    jLabel2.setText("Enter Password:");
    continueButton.setBackground(new Color(200, 200, 230));
    continueButton.setFont(new java.awt.Font("Dialog", 1, 12));
    continueButton.setForeground(new Color(80, 80, 133));
    continueButton.setText("Continue");
    continueButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        continueButton_actionPerformed(e);
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
    jLabel5.setFont(new java.awt.Font("Dialog", 1, 16));
    jLabel5.setForeground(new Color(80, 80, 133));
    jLabel5.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel5.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel5.setText("OverWrite Existing File");
    this.getContentPane().add(filePassword,  new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(63, 10, 0, 62), 143, 13));
    this.getContentPane().add(continueButton,  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 0, 23, 0), 4, 13));
    this.getContentPane().add(cancelButton,  new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20, 23, 23, 17), 16, 13));
    this.getContentPane().add(jLabel2,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(66, 24, 0, 0), 33, 10));
    this.getContentPane().add(jLabel5,  new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 33, 0, 44), 114, 16));
    this.setTitle("Deletion Window");
    this.setTitle("PEER Data File Deletion Window");
  }



  /**
   * Makes the connection to the servlet if user enters the correct password &
   * confirms that he indeed wants to delete the file
   * @param e
   */
  void continueButton_actionPerformed(ActionEvent e) {

    String password = new String(filePassword.getPassword());

    if(!peer.checkPassword(password))
      JOptionPane.showMessageDialog(this,new String("Incorrect Password"),"Check Password",
                                    JOptionPane.OK_OPTION);

    else {
      //delete the file selected.
      int flag=JOptionPane.showConfirmDialog(this,new String("Are you sure you want to overwrite the file??"),
          "Overwrite Confirmation Message",JOptionPane.OK_CANCEL_OPTION);

      int found=0;
      if(flag == JOptionPane.OK_OPTION)
         found=1;
      if(found==1)
        peer.openOverwriteConnection(fileName);

      this.dispose();
    }
    filePassword.setText("");
  }

  /**
   *
   * @param e =this event occurs to destroy the popup window if the user has selected cancel option
   */
  void cancelButton_actionPerformed(ActionEvent e) {
    this.dispose();
  }
}
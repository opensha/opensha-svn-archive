package org.opensha.refFaultParamDb.gui.login;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Title: ChangePassword.java </p>
 * <p>Description: Change the password for the user</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ChangePassword extends JFrame {
  private JPanel mainPanel = new JPanel();
  private JLabel changePasswordLabel = new JLabel();
  private JLabel userNameLabel = new JLabel();
  private JTextField userNameText = new JTextField();
  private JLabel oldPwdLabel = new JLabel();
  private JLabel newPwdLabel = new JLabel();
  private JLabel confirmNewPwdLabel = new JLabel();
  private JPasswordField oldPwdText = new JPasswordField();
  private JPasswordField oldPwdText1 = new JPasswordField();
  private JPasswordField oldPwdText2 = new JPasswordField();
  private JButton changePasswordButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();

  public ChangePassword() {
    try {
      jbInit();
      this.pack();
      this.show();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  public static void main(String[] args) {
    ChangePassword changePassword = new ChangePassword();
  }
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    mainPanel.setLayout(gridBagLayout1);
    changePasswordLabel.setFont(new java.awt.Font("Dialog", 1, 16));
    changePasswordLabel.setForeground(new Color(80, 80, 133));
    changePasswordLabel.setHorizontalAlignment(SwingConstants.CENTER);
    changePasswordLabel.setText("Change Password");
    userNameLabel.setFont(new java.awt.Font("Dialog", 1, 12));
    userNameLabel.setForeground(new Color(80, 80, 133));
    userNameLabel.setRequestFocusEnabled(true);
    userNameLabel.setText("Username:");
    userNameText.setForeground(new Color(80, 80, 133));
    userNameText.setText("");
    oldPwdLabel.setFont(new java.awt.Font("Dialog", 1, 12));
    oldPwdLabel.setForeground(new Color(80, 80, 133));
    oldPwdLabel.setText("Old Password:");
    newPwdLabel.setFont(new java.awt.Font("Dialog", 1, 12));
    newPwdLabel.setForeground(new Color(80, 80, 133));
    newPwdLabel.setText("New Password:");
    confirmNewPwdLabel.setFont(new java.awt.Font("Dialog", 1, 12));
    confirmNewPwdLabel.setForeground(new Color(80, 80, 133));
    confirmNewPwdLabel.setRequestFocusEnabled(true);
    confirmNewPwdLabel.setText("Confirm New Password:");
    oldPwdText.setFont(new java.awt.Font("Dialog", 1, 12));
    oldPwdText.setForeground(new Color(80, 80, 133));
    oldPwdText.setText("");
    oldPwdText1.setText("");
    oldPwdText1.setFont(new java.awt.Font("Dialog", 1, 12));
    oldPwdText1.setForeground(new Color(80, 80, 133));
    oldPwdText2.setText("");
    oldPwdText2.setFont(new java.awt.Font("Dialog", 1, 12));
    oldPwdText2.setForeground(new Color(80, 80, 133));
    changePasswordButton.setForeground(new Color(80, 80, 133));
    changePasswordButton.setText("Change Password");
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.add(userNameLabel,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(26, 18, 0, 65), 27, 13));
    mainPanel.add(oldPwdLabel,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(18, 18, 0, 53), 20, 13));
    mainPanel.add(newPwdLabel,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(19, 18, 0, 49), 18, 13));
    mainPanel.add(confirmNewPwdLabel,  new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(18, 18, 0, 0), 20, 13));
    mainPanel.add(userNameText,  new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(28, 16, 0, 23), 163, 3));
    mainPanel.add(oldPwdText,  new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(20, 16, 0, 23), 164, 4));
    mainPanel.add(oldPwdText2,  new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(20, 16, 0, 23), 164, 4));
    mainPanel.add(oldPwdText1,  new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(21, 16, 0, 23), 164, 4));
    mainPanel.add(changePasswordButton,  new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(11, 37, 20, 23), 21, 4));
    mainPanel.add(changePasswordLabel,  new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 39, 0, 55), 148, 13));
  }
}
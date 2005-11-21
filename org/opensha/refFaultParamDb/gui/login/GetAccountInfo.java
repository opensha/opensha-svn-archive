package org.opensha.refFaultParamDb.gui.login;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Title: GetAccountInfo.java </p>
 * <p>Description: Retrieve the account info and mail it to user if user forgets
 * username/password </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class GetAccountInfo extends JFrame {
  private JPanel jPanel1 = new JPanel();
  private JTextField emailText = new JTextField();
  private JLabel emailLabel = new JLabel();
  private JButton emailAccountInfoButton = new JButton();
  private JLabel forgotLabel = new JLabel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();

  public GetAccountInfo() {
    try {
      jbInit();
      pack();
      show();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  public static void main(String[] args) {
    GetAccountInfo getAccountInfo = new GetAccountInfo();
  }
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(gridBagLayout2);
    jPanel1.setLayout(gridBagLayout1);
    emailText.setText("");
    emailText.setForeground(new Color(80, 80, 133));
    emailLabel.setFont(new java.awt.Font("Dialog", 1, 12));
    emailLabel.setForeground(new Color(80, 80, 133));
    emailLabel.setText("Email:");
    emailAccountInfoButton.setForeground(new Color(80, 80, 133));
    emailAccountInfoButton.setText("Email Account Info");
    forgotLabel.setFont(new java.awt.Font("Dialog", 1, 16));
    forgotLabel.setForeground(new Color(80, 80, 133));
    forgotLabel.setHorizontalAlignment(SwingConstants.CENTER);
    forgotLabel.setText("Forgot username/password");
    this.getContentPane().add(jPanel1,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 5, 13, 24), -4, 4));
    jPanel1.add(forgotLabel,  new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 7), 78, 13));
    jPanel1.add(emailAccountInfoButton,  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 22, 33), 20, 7));
    jPanel1.add(emailLabel,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(14, 17, 0, 0), 73, 13));
    jPanel1.add(emailText,  new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(15, 75, 0, 31), 184, 3));
  }
}
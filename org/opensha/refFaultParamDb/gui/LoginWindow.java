package org.opensha.refFaultParamDb.gui;
import java.awt.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.net.*;
import java.awt.event.*;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;

/**
 * <p>Title: LoginWindow</p>
 * <p>Description: This class provide controlled access to the users who want to access the
 * California Reference Geologic Fault Parameter (Paleo Site) GUI </p>
 * @author : Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class LoginWindow extends JFrame implements ActionListener {

  private static final boolean D= false;
  private final static String TITLE = "Login";
  private JPanel passwordPanel = new JPanel();
  private JButton loginButton = new JButton();
  private JPasswordField passwordText = new JPasswordField();
  private JLabel jLabel5 = new JLabel();
  private JButton cancelButton = new JButton();
  private JLabel jLabel2 = new JLabel();
  private JTextField usernameText = new JTextField();
  private JLabel jLabel1 = new JLabel();
  private BorderLayout borderLayout1 = new BorderLayout();

  //checks if user did successful login
  private boolean loginSuccess = false;

  private JButton newUserButton = new JButton();
  private JButton forgetPassButton = new JButton();


  public LoginWindow(){
    init();
  }


  public void init() {
    try {
      jbInit();
      setTitle(TITLE);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    usernameText.setForeground(new Color(80, 80, 133));
    usernameText.setBackground(Color.white);
    passwordText.setBackground(Color.white);
    this.getContentPane().setLayout(borderLayout1);
    newUserButton.setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
    newUserButton.setForeground(new Color(80, 80, 133));
    newUserButton.setText("New User");
    forgetPassButton.setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
    forgetPassButton.setForeground(new Color(80, 80, 133));
    forgetPassButton.setToolTipText("Forgot Password");
    forgetPassButton.setText("Forgot Passwd");
    this.getContentPane().add(passwordPanel, java.awt.BorderLayout.CENTER);
    passwordPanel.setLayout(GUI_Utils.gridBagLayout);
    loginButton.setFont(new java.awt.Font("Dialog", 1, 12));
    loginButton.setForeground(new Color(80, 80, 133));
    loginButton.setText("Login");
    passwordText.setBackground(Color.white);
    passwordText.setFont(new java.awt.Font("Dialog", 1, 12));
    passwordText.setForeground(new Color(80, 80, 133));
    jLabel5.setFont(new java.awt.Font("Dialog", 1, 16));
    jLabel5.setForeground(new Color(80, 80, 133));
    jLabel5.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel5.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel5.setText("Authorizing User");
    cancelButton.setFont(new java.awt.Font("Dialog", 1, 12));
    cancelButton.setForeground(new Color(80, 80, 133));
    cancelButton.setText("Cancel");
    jLabel2.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel2.setForeground(new Color(80, 80, 133));
    jLabel2.setText("Enter Password:");
    jLabel1.setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
    jLabel1.setForeground(new Color(80, 80, 133));
    jLabel1.setText("Enter Username:");
    passwordPanel.add(jLabel5, null);
    passwordPanel.add(jLabel5, new GridBagConstraints(0, 0, 5, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(6, 2, 0, 4), 271, 13));
    passwordPanel.add(usernameText, new GridBagConstraints(2, 1, 3, 1, 1.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(24, 0, 0, 83), 186, 7));
    passwordPanel.add(passwordText, new GridBagConstraints(2, 2, 3, 1, 1.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(8, 0, 0, 83), 186, 9));
    passwordPanel.add(jLabel1, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(25, 8, 0, 0), 20, 13));
    passwordPanel.add(jLabel2, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(10, 8, 0, 0), 20, 13));
    passwordPanel.add(loginButton,
                      new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0
                                             , GridBagConstraints.CENTER,
                                             GridBagConstraints.NONE,
                                             new Insets(24, 25, 24, 0), 5, 0));
    loginButton.addActionListener(this);
    /*passwordPanel.add(cancelButton, new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(24, 0, 24, 0), 9, 0));

    passwordPanel.add(newUserButton,
                      new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0
                                             , GridBagConstraints.CENTER,
                                             GridBagConstraints.NONE,
                                             new Insets(24, 0, 24, 0), 0, 0));
    passwordPanel.add(forgetPassButton,
                      new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0
                                             , GridBagConstraints.CENTER,
                                             GridBagConstraints.NONE,
                                             new Insets(24, 0, 24, 45), -29, 0));*/
    pack();
    this.setLocationRelativeTo(null);
    show();
  }

  /**
   * This function is called when any button is clicked on this window
   * @param event
   */
  public void actionPerformed(ActionEvent event) {
    Object source = event.getSource();
    if(source==loginButton) { // if login button is clicked, save the username/passwd
      GUI_Utils.setPassword(this.passwordText.getText());
      GUI_Utils.setUserName(this.usernameText.getText());
      PaleoSiteApp2 paleoSiteApp = new PaleoSiteApp2();
    }
  }

  //static initializer for setting look & feel
  static {
    String osName = System.getProperty("os.name");
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
    }
  }


  public static void main(String args[]) {
    LoginWindow loginWindow = new LoginWindow();
  }

}

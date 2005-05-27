package org.opensha.sha.gui.infoTools;
import java.awt.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.border.*;

/**
 * <p>Title: UserAuthorizationCheckWindow</p>
 * <p>Description: This class provide controlled access to the users who want to generate
 * the datasets for the Hazard Maps using Condor at University of Southern California.</p>
 * @author : Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class UserAuthorizationCheckWindow extends JDialog {

  private static final boolean D= false;

  private JPanel passwordPanel = new JPanel();
  private JButton continueButton = new JButton();
  private JPasswordField passwordText = new JPasswordField();
  private JLabel jLabel5 = new JLabel();
  private JButton cancelButton = new JButton();
  private JLabel jLabel2 = new JLabel();
  JTextField usernameText = new JTextField();
  JLabel jLabel1 = new JLabel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  BorderLayout borderLayout1 = new BorderLayout();

  //checks if user did successful login
  private boolean loginSuccess = false;


  //Servlet address
  private final static String SERVLET_ADDRESS = "https://wave.usc.edu/cmedb/CheckAuthorizationServlet";


  public UserAuthorizationCheckWindow(){
    init();
  }


  public void init() {
    try {
      setModal(true);
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    usernameText.setForeground(new Color(80, 80, 133));
    usernameText.setBackground(Color.white);
    passwordText.setBackground(Color.white);

    this.getContentPane().setLayout(borderLayout1);
    this.getContentPane().add(passwordPanel, java.awt.BorderLayout.CENTER);
    passwordPanel.setLayout(gridBagLayout1);
    continueButton.setFont(new java.awt.Font("Dialog", 1, 12));
    continueButton.setForeground(new Color(80, 80, 133));
    continueButton.setText("Continue");
    continueButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        continueButton_actionPerformed(e);
      }
    });
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
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });
    jLabel2.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel2.setForeground(new Color(80, 80, 133));
    jLabel2.setText("Enter Password:");
    jLabel1.setFont(new java.awt.Font("Dialog", Font.BOLD, 12));
    jLabel1.setForeground(new Color(80, 80, 133));
    jLabel1.setText("Enter Username:");
    passwordPanel.add(jLabel5, null);
    passwordPanel.add(cancelButton, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(30, 25, 44, 38), 16, 13));
    passwordPanel.add(continueButton,
                      new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
                                             , GridBagConstraints.CENTER,
                                             GridBagConstraints.NONE,
                                             new Insets(30, 9, 44, 0), 4, 13));
    passwordPanel.add(passwordText, new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(13, 0, 0, 27), 206, 12));
    passwordPanel.add(jLabel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(19, 31, 0, 0), 24, 10));
    passwordPanel.add(usernameText, new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(40, 0, 0, 27), 206, 10));
    passwordPanel.add(jLabel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(45, 31, 0, 0), 21, 10));
    passwordPanel.add(jLabel5, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(28, 13, 0, 14), 240, 16));


    this.setSize(400,260);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    this.setLocation( (d.width - this.getSize().width) / 2,
                     (d.height - this.getSize().height) / 2);
  }



  /**
   * Makes the connection to the servlet if user enters the correct password &
   * confirms that he indeed wants to delete the file
   * @param e
   */
  void continueButton_actionPerformed(ActionEvent e) {


    String username = new String(usernameText.getText());
    String password = new String(passwordText.getPassword());
    if(username == null || username.trim().equals("") || password == null || username.trim().equals("")){
      JOptionPane.showMessageDialog(this,
                                    new String(
          "Must Enter User Name and Password."),
                                    "Check Login",
                                    JOptionPane.OK_OPTION);
      return;
    }
    if(!isUserAuthorized(username,password)){
      JOptionPane.showMessageDialog(this,
                                    new String("<html><body><b>Incorrect Username or Password.</b>"+
                                               "<br>Not registered or forgot password, go to the URL below: </br>"+
                                               "<br>http://gravity.usc.edu:8080/usermanagement"+
                                               ".</br></boby></html>"),
                                    "Incorrect login information",
                                    JOptionPane.ERROR_MESSAGE);
      passwordText.setText("");
      return;
    }
    else{
      this.dispose();
      loginSuccess = true;
    }
  }


  /**
   * Check if user was able to successfully login.
   * @return boolean to hazard dataset calculation application to see if the login
   * was successful.
   */
  public boolean isLoginSuccess(){
    return loginSuccess;
  }

  /**
   *
   * @param e =this event occurs to destroy the popup window if the user has selected cancel option
   */
  void cancelButton_actionPerformed(ActionEvent e) {
    System.exit(0);
  }



  /**
   * Returns true if the user is authorized to use the applications
   * @param username
   * @param password
   * @return
   */
  private static boolean isUserAuthorized(String username, String password) {
    try {

      System.setProperty("java.protocol.handler.pkgs",
                         "com.sun.net.ssl.internal.www.protocol"); //add https protocol handler
      java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.
                                         Provider()); //dynamic registration of SunJSSE provider

//Create a trust manager that does not validate certificate chains:
      com.sun.net.ssl.TrustManager[] trustAllCerts = new com.sun.net.ssl.
          TrustManager[] {
          new com.sun.net.ssl.X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[]
                                       certs) {
          return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[]
                                       certs) {
          return true;
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[]
                                       certs, String authType) throws javax.
            security.cert.CertificateException {
          return;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[]
                                       certs, String authType) throws javax.
            security.cert.CertificateException {
          return;
        }
      } //X509TrustManager
      } ; //TrustManager[]

//Install the all-trusting trust manager:
      com.sun.net.ssl.SSLContext sc = com.sun.net.ssl.SSLContext.getInstance(
          "SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      com.sun.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.
          getSocketFactory());

      // servlet URL
      //URL servletURL = new URL(SERVLET_ADDRESS+"?"+CheckAuthorizationServlet.USERNAME+"="+username+
      //                         "&"+CheckAuthorizationServlet.PASSWORD+"="+password);
      URL servletURL = new URL(SERVLET_ADDRESS + "?" + "username" + "=" +
                               username +
                               "&" + "password" + "=" + password);
      URLConnection servletConnection = servletURL.openConnection();

      // Receive the "object" from the servlet after it has received all the data
      ObjectInputStream fromServlet = new
          ObjectInputStream(servletConnection.getInputStream());
      Boolean auth = (Boolean) fromServlet.readObject();
      fromServlet.close();
      return auth.booleanValue();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

}

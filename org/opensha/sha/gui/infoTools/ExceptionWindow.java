package org.opensha.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;


import org.opensha.util.MailUtil;


/**
 * <p>Title: ExceptionWindow</p>
 * <p>Description: This application window gets popped up whenever the
 * any application crashes. When application aburptly crashes this window gets popped up
 * with exception that occured. When this window pops up user can also specify how
 * that exception (what was user trying to do) occured. An email will be send out
 * to the people maintaining the system. </p>
 * @author : Nitin Gupta and Vipin Gupta
 * @created
 * @version 1.0
 */

public class ExceptionWindow extends JDialog {
  private JPanel jPanel1 = new JPanel();
  private JLabel exceptionLabel = new JLabel();
  private JScrollPane exceptionScrollPane = new JScrollPane();
  private JTextPane exceptionTextPane = new JTextPane();
  private JScrollPane errorDescriptionPanel = new JScrollPane();
  private JTextPane errorTextPanel = new JTextPane();
  private JLabel jLabel1 = new JLabel();
  private JButton sendButton = new JButton();
  private JButton cancelButton = new JButton();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JLabel emailLabel = new JLabel();
  private JTextField emailText = new JTextField();

  //servlet ( to send the mail)
  private static final String SERVLET_URL = "http://gravity.usc.edu/OpenSHA/servlet/EmailServlet";

  //TITLE of this window
  private static final String TITLE = "Application bug reporting window";
  private static final boolean D = false;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  public ExceptionWindow(Component parent,String exceptionText,String selectedParametersInfo) {

    try {
      //making the exception window be the most active window
      setModal(true);
      //user is forced to use the "button" operations.
      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
      // show the window at center of the parent component
      this.setLocation(parent.getX()+parent.getWidth()/2,
                       parent.getY()+parent.getHeight()/2);
      jbInit();
      String exceptionMessage = exceptionText+"\n\n"+
          "Selected Parameters Info :\n"+
          "--------------------------"+selectedParametersInfo;


      exceptionTextPane.setText(exceptionMessage);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  /*public static void main(String[] args) {
    ExceptionWindow exceptionWindow = new ExceptionWindow("Hello");
    exceptionWindow.show();
    exceptionWindow.pack();
  }*/
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    jPanel1.setLayout(gridBagLayout1);
    exceptionLabel.setFont(new java.awt.Font("Lucida Grande", 1, 17));
    exceptionLabel.setText("Exception Occured:");
    exceptionTextPane.setEditable(false);
    jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 17));
    jLabel1.setText("Brief desc. of how problem occured:");
    sendButton.setActionCommand("cancelButton");
    sendButton.setText("Send");
    sendButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sendButton_actionPerformed(e);
      }
    });
    cancelButton.setActionCommand("cancelButton");
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });
    emailLabel.setText("Enter your email:");
    jPanel1.setMinimumSize(new Dimension(100, 100));
    jPanel1.setPreferredSize(new Dimension(470, 330));
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(exceptionScrollPane,  new GridBagConstraints(0, 1, 3, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 8, 0, 8), 418, 65));
    exceptionScrollPane.getViewport().add(exceptionTextPane, null);
    jPanel1.add(errorDescriptionPanel,  new GridBagConstraints(0, 3, 3, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 8, 0, 8), 418, 61));
    errorDescriptionPanel.getViewport().add(errorTextPanel, null);
    jPanel1.add(jLabel1,  new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 14, 0, 43), 93, 8));
    jPanel1.add(exceptionLabel,  new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 15, 0, 123), 148, 12));
    jPanel1.add(emailLabel,  new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(11, 14, 6, 0), 31, 13));
    jPanel1.add(sendButton,  new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(23, 44, 17, 0), 1, 10));
    jPanel1.add(emailText,  new GridBagConstraints(1, 4, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(11, 0, 0, 114), 170, 16));
    jPanel1.add(cancelButton,  new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(23, 25, 17, 56), 0, 10));
    emailText.setText("");
  }

  void cancelButton_actionPerformed(ActionEvent e) {
    this.dispose();
    System.exit(0);
  }

  void sendButton_actionPerformed(ActionEvent e) {

    String email = emailText.getText();
    if(email.trim().equalsIgnoreCase("")) {
     JOptionPane.showMessageDialog(this, "Please Enter email Address");
     return;
    }
    if(email.indexOf("@") ==-1 || email.indexOf(".") ==-1) {
      JOptionPane.showMessageDialog(this, "Please Enter valid email Address");
      return;
    }
    else{
      String emailMessage = "Application Exception\n"+
                            "----------------------\n"+
                            exceptionTextPane.getText()+"\n\n\n"+
                            "Exception Description (as provided by user)\n"+
                            "----------------------\n"+
                            errorTextPanel.getText();
      //establishing connection with servlet to email exception message to system maintainer
      sendParametersToServlet(email,emailMessage);
      dispose();
      System.exit(0);
    }
  }


  /**
   * sets up the connection with the servlet on the server (gravity.usc.edu), to send the mail
   */
  private void sendParametersToServlet(String email,String emailMessage) {

    try{
      if(D) System.out.println("starting to make connection with servlet");
      URL hazardMapServlet = new URL(SERVLET_URL);


      URLConnection servletConnection = hazardMapServlet.openConnection();
      if(D) System.out.println("connection established");

      // inform the connection that we will send output and accept input
      servletConnection.setDoInput(true);
      servletConnection.setDoOutput(true);

      // Don't use a cached version of URL connection.
      servletConnection.setUseCaches (false);
      servletConnection.setDefaultUseCaches (false);
      // Specify the content type that we will send binary data
      servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

      ObjectOutputStream toServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());


      //sending email address to the servlet
      toServlet.writeObject(email);

      //sending the email message to the servlet
      toServlet.writeObject(emailMessage);


      toServlet.flush();
      toServlet.close();

      // Receiving the "email sent" message from servlet
      ObjectInputStream fromServlet = new ObjectInputStream(servletConnection.getInputStream());
      String sentEmailText=(String)fromServlet.readObject();
      fromServlet.close();

    }catch (Exception e) {
      System.out.println("Exception in connection with servlet:" +e);
      e.printStackTrace();
    }
  }




}

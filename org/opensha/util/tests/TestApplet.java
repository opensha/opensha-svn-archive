package org.opensha.util.tests;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;

/**
 * <p>Title: TestApplet</p>
 * <p>Description: This Applet is a test applet to check the copy and paste problems
 * in the Text-Areas and Text-Fields if we use the Java Version 1.4 on the Macs</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public class TestApplet extends JApplet {
  private boolean isStandalone = false;
  private JPanel jPanel1 = new JPanel();

  private JTextField textField = new JTextField();
  private JTextArea textArea = new JTextArea();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  // height and width of the applet
  private final static int W = 600;
  private final static int H = 500;

  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public TestApplet() {
  }
  //Initialize the applet
  public void init() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  //Component initialization
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    this.getContentPane().setSize(W,H);
    this.jPanel1.setPreferredSize(new Dimension(W,H));
    jPanel1.setLayout(gridBagLayout1);
    textArea.setText("Try to copy and paste this from here using the both Java-1.3 and " +
    "Java1.4 and you can see the difference.");
    textArea.setLineWrap(true);
    jLabel1.setText("Enter Text here:");
    jLabel2.setText("TextArea Text:");
    textField.setText("Try copying text here using Java-1.4");
    jPanel1.add(jLabel1,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 4, 11));
    jPanel1.add(textField,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 13));
    jPanel1.add(textArea,  new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
    jPanel1.add(jLabel2,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 13, 11));
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
  }
  //Get Applet information
  public String getAppletInfo() {
    return "Applet Information";
  }
  //Get parameter info
  public String[][] getParameterInfo() {
    return null;
  }
  //Main method
  public static void main(String[] args) {
    TestApplet applet = new TestApplet();
    applet.isStandalone = true;
    Frame frame;
    frame = new Frame() {
      protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
          System.exit(0);
        }
      }
      public synchronized void setTitle(String title) {
        super.setTitle(title);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      }
    };
    frame.setTitle("Test Applet to check the copy and paste problems with applet in Java-1.4 on Macs");
    frame.add(applet, BorderLayout.CENTER);
    applet.init();
    applet.start();
    frame.setSize(W,H);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
    frame.setVisible(true);
  }
}

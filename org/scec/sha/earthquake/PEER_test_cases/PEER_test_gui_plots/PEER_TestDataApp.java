package org.scec.sha.earthquake.PEER_test_cases.PEER_test_gui_plots;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.util.ListIterator;
import java.util.Vector;
import java.util.StringTokenizer;
import java.lang.RuntimeException;

import org.scec.data.function.*;
import org.scec.gui.*;
import org.scec.gui.plot.LogPlotAPI;
import org.scec.gui.plot.*;
import com.borland.jbcl.layout.*;


/**
 * <p>Title: PEER_TestDataApp </p>
 * <p>Description: This Applet allows the different user to submit their dataFiles
 * for the PEER Tests and these datafiles are then stored  as the Jar files on the
 * server scec.usc.edu .
 * After Submission of their datafiles, the users can see the result of their files
 * as the PEER test plots in the Applet PEER_Test_GuiPlotter</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author: Nitin Gupta & Vipin Gupta
 * @date : Dec 17,2002
 * @version 1.0
 */

public class PEER_TestDataApp extends JApplet {

  //ClassName and Debug property
  private static final String C = "PEER_TestDataApp";
  private static final boolean D = true;

  //Directory from which to search for all the PEER test files
  String DIR = "GroupTestDataFiles/";
  String FILE_EXTENSION=".dat";

  //Reads the selected test case file. returns the function conatining x,y values
  ArbitrarilyDiscretizedFunc function= new ArbitrarilyDiscretizedFunc();

  //Vector to store all the existing Test Case file names
   Vector testFiles= new Vector();

  private boolean isStandalone = false;
  private JPanel dataPanel = new JPanel();
  private JComboBox testComboBox = new JComboBox();
  private Border border1;
  private Border border2;
  private JLabel xLabel = new JLabel();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JButton submitButton = new JButton();
  private JLabel appletLabel = new JLabel();
  private JTextArea messageTextArea = new JTextArea();
  private JLabel jLabel3 = new JLabel();
  private JTextField fileNameText = new JTextField();
  private JLabel jLabel4 = new JLabel();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JTextArea xTextArea = new JTextArea();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private JTextArea yTextArea = new JTextArea();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JButton ResultButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public PEER_TestDataApp() {
  }
  //Initialize the applet
  public void init() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
   searchTestFiles();
   createFunction();
   setXValues();
  }
  //Component initialization
  private void jbInit() throws Exception {
    border1 = new EtchedBorder(EtchedBorder.RAISED,new Color(248, 254, 255),new Color(121, 124, 136));
    border2 = new EtchedBorder(EtchedBorder.RAISED,new Color(248, 254, 255),new Color(121, 124, 136));
    this.setSize(new Dimension(402, 606));
    this.getContentPane().setLayout(borderLayout1);
    dataPanel.setBackground(Color.white);
    dataPanel.setBorder(BorderFactory.createEtchedBorder());
    dataPanel.setLayout(gridBagLayout1);
    xLabel.setForeground(new Color(80, 80, 133));
    xLabel.setText("X:");
    jLabel1.setForeground(new Color(80, 80, 133));
    jLabel1.setText("Select Test Case:");
    jLabel2.setForeground(new Color(80, 80, 133));
    jLabel2.setText("Y:");
    submitButton.setBackground(new Color(200, 200, 230));
    submitButton.setFont(new java.awt.Font("Dialog", 1, 12));
    submitButton.setForeground(new Color(80, 80, 133));
    submitButton.setText("Submit");
    submitButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        submitButton_actionPerformed(e);
      }
    });
    appletLabel.setFont(new java.awt.Font("Dialog", 1, 16));
    appletLabel.setForeground(new Color(80, 80, 133));
    appletLabel.setToolTipText("");
    appletLabel.setHorizontalAlignment(SwingConstants.CENTER);
    appletLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    appletLabel.setText("PEER Test Data ");
    messageTextArea.setBackground(new Color(200, 200, 230));
    messageTextArea.setFont(new java.awt.Font("Dialog", 1, 11));
    messageTextArea.setForeground(new Color(80, 80, 133));
    messageTextArea.setBorder(BorderFactory.createLineBorder(Color.black));
    messageTextArea.setLineWrap(true);
    jLabel3.setForeground(new Color(80, 80, 133));
    jLabel3.setText("Enter File Name:");
    fileNameText.setBackground(new Color(200, 200, 230));
    fileNameText.setFont(new java.awt.Font("Dialog", 1, 11));
    fileNameText.setForeground(new Color(80, 80, 133));
    jLabel4.setForeground(new Color(80, 80, 133));
    jLabel4.setText("Message:");
    jLabel4.setVerticalAlignment(SwingConstants.TOP);
    testComboBox.setBackground(new Color(200, 200, 230));
    testComboBox.setFont(new java.awt.Font("Dialog", 1, 12));
    testComboBox.setForeground(new Color(80, 80, 133));
    yTextArea.setBackground(new Color(200, 200, 230));
    yTextArea.setFont(new java.awt.Font("Dialog", 1, 11));
    yTextArea.setForeground(new Color(80, 80, 133));
    xTextArea.setBackground(new Color(200, 200, 230));
    xTextArea.setFont(new java.awt.Font("Dialog", 1, 11));
    xTextArea.setForeground(new Color(80, 80, 133));
    ResultButton.setBackground(new Color(200, 200, 230));
    ResultButton.setFont(new java.awt.Font("Dialog", 1, 12));
    ResultButton.setForeground(new Color(80, 80, 133));
    ResultButton.setMaximumSize(new Dimension(71, 37));
    ResultButton.setMinimumSize(new Dimension(71, 37));
    ResultButton.setPreferredSize(new Dimension(71, 37));
    ResultButton.setMnemonic('0');
    ResultButton.setText("View Result");
    ResultButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ResultButton_actionPerformed(e);
      }
    });
    this.getContentPane().add(dataPanel, BorderLayout.CENTER);
    dataPanel.add(testComboBox,  new GridBagConstraints(2, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 17, 0, 5), 95, 7));
    dataPanel.add(jLabel1,  new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 12, 0, 15), 10, 14));
    dataPanel.add(fileNameText,  new GridBagConstraints(2, 2, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(17, 17, 0, 5), 221, 8));
    dataPanel.add(jLabel3,  new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(17, 12, 0, 17), 13, 12));
    dataPanel.add(xLabel,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(16, 24, 0, 20), 11, 3));
    dataPanel.add(jLabel2,  new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 27, 0, 23), 8, 7));
    dataPanel.add(jScrollPane1,  new GridBagConstraints(0, 4, 1, 2, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 12, 28, 0), 54, 387));
    dataPanel.add(jScrollPane2,  new GridBagConstraints(1, 4, 1, 2, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 15, 28, 0), 54, 387));
    dataPanel.add(messageTextArea,  new GridBagConstraints(2, 4, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 9, 0, 5), 0, 206));
    dataPanel.add(jLabel4,  new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(15, 19, 0, 0), 44, 5));
    dataPanel.add(appletLabel,  new GridBagConstraints(1, 0, 3, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 18, 0, 112), 48, 16));
    jScrollPane2.getViewport().add(yTextArea, null);
    jScrollPane1.getViewport().add(xTextArea, null);
    dataPanel.add(submitButton,  new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(135, 11, 28, 0), 36, 10));
    dataPanel.add(ResultButton,  new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(135, 0, 28, 17), 37, 9));
  }
  //Start the applet
  public void start() {
  }
  //Stop the applet
  public void stop() {
  }
  //Destroy the applet
  public void destroy() {
  }
  //Get Applet information
  public String getAppletInfo() {
    return C;
  }
  //Get parameter info
  public String[][] getParameterInfo() {
    return null;
  }
  //Main method
  public static void main(String[] args) {
    PEER_TestDataApp applet = new PEER_TestDataApp();
    applet.isStandalone = true;
    JFrame frame = new JFrame();
    //EXIT_ON_CLOSE == 3
    frame.setDefaultCloseOperation(3);
    frame.setTitle("PEER Test Data Submission Applet");
    frame.getContentPane().add(applet, BorderLayout.CENTER);
    applet.init();
    applet.start();
    frame.setSize(400,610);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
    frame.setVisible(true);
  }

  //static initializer for setting look & feel
  static {
    try {
      //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    }
    catch(Exception e) {
    }
  }



  /**
   * This function looks for all the test cases files within the directory
   * and stores their name in Vector
   */
  private  void searchTestFiles(){

    try{
      // files.log contains all the files uploaded so far
      InputStream input = PEER_TestGuiPlotter.class.getResourceAsStream("/"+DIR+"files.log");
      DataInputStream dataStream = new DataInputStream(input);
      String line;
      while((line=dataStream.readLine())!=null) {
        if(line.endsWith(FILE_EXTENSION)) testFiles.add(line);
        else continue;
        int index=line.indexOf("_");
        String testCases = line.substring(0,index);
        int count = testComboBox.getItemCount();
        boolean flag = false;
        // check whether this set has already been added to combo box
        for(int i=0; i<count; ++i) {
          if(testComboBox.getItemAt(i).toString().equalsIgnoreCase(testCases)) {
            flag = true;
            break;
          }
        }
        if(!flag) testComboBox.addItem(testCases);
      }
    }catch(Exception e) {
      e.printStackTrace();
    }

  }






  /**
   * this method shows the X Values in the TextArea
   */
  private void setXValues(){
    ListIterator it=function.getXValuesIterator();
    StringBuffer st = new StringBuffer();
    while(it.hasNext()){
       st.append(it.next().toString());
       st.append('\n');
    }
    xTextArea.setText(st.toString());
  }


  /**
   * This method is called when the submit button is clicked
   * Provides Error checking to se that user has entered all the valid values in
   * the parameters
   */
  private boolean submitButton() throws RuntimeException{

      //creating the new file name in which function data has to be stored.
      StringBuffer fileName =new StringBuffer(testComboBox.getSelectedItem().toString());
      boolean flag = false;
      fileName.append("_");
      fileName.append(fileNameText.getText());
      fileName.append(".dat");
      int size= testFiles.size();
      //checking if the fileName already exists, if so then ask user to input another fileName
      for(int i=0;i<size;++i)
        if(fileName.equals(testFiles.get(i).toString())){
          flag=true;
          break;
        }
      if(!flag)
        function.setName(fileName.toString());
      else{
        fileNameText.setText("");
        throw new RuntimeException("FileName already exists, Please enter new File Name");
      }
      flag = getYValues();
      return flag;
  }


  /**
   * This method gets the Y values entered by the user and updates the ArbDiscretizedFunc
   * with these Y values.
   */
  private boolean getYValues(){

    String yValues= new String(yTextArea.getText());
    //checking if the TextArea where values are to be entered is empty.
    if(yValues.equals("")){
      JOptionPane.showMessageDialog(this,new String("Must Enter Y Values"),"Input Error",
                                    JOptionPane.ERROR_MESSAGE);
      return false;
    }
    //if the user has entered the Y Values in the TextArea.
    else{
      Vector vt = new Vector();
      //getting each Y value and adding to the Vector.
      StringTokenizer st = new StringTokenizer(yValues.trim(),"\n");
      while(st.hasMoreTokens())
        vt.add(st.nextToken());

      //checking if the vector size is 20, which are number of X values we have
      int size = vt.size();
      if(size!= function.getNum()){
        JOptionPane.showMessageDialog(this,new String("Incorrect number of Y Values"),"Input Error",
                                      JOptionPane.ERROR_MESSAGE);
        return false;
      }
      else{
        for(int i=0;i<size;++i)
          //updating the function with the Y-values entered by the user.
          function.set(i,Double.parseDouble(vt.get(i).toString().trim()));
      }
    }
    return true;
  }


  /**
   * initialises the function with the x and y values
   * the y values are modified with the values entered by the user
   */
  private void createFunction(){
    function.set(.001,1);
    function.set(.01,1);
    function.set(.05,1);
    function.set(.15,1);
    function.set(.1,1);
    function.set(.2,1);
    function.set(.25,1);
    function.set(.3,1);
    function.set(.4,1);
    function.set(.5,1);
    function.set(.6,1);
    function.set(.7,1);
    function.set(.8,1);
    function.set(.9,1);
    function.set(1.0,1);
    function.set(1.1,1);
    function.set(1.2,1);
    function.set(1.3,1);
    function.set(1.4,1);
    function.set(1.5,1);
  }


  /**
   * This method is called if the submit button is clicked.
   * It checks if the information is filled correctly in all the parameters
   * and if everthing has been correctly entered this applet sets-up connection
   * with the servlet.
   * @param e
   */
  void submitButton_actionPerformed(ActionEvent e) {
    //checks if user has entered the filename
    boolean flag = true;
    if(fileNameText.getText().trim().equals(""))
      JOptionPane.showMessageDialog(this,new String("Must enter File name"),"Input Error",
                                    JOptionPane.ERROR_MESSAGE);
    else{
      try{
      flag = submitButton();
      }catch(RuntimeException ee){
        JOptionPane.showMessageDialog(this,new String(ee.getMessage()),
                                      "Duplicate file Name",JOptionPane.INFORMATION_MESSAGE);
      }

      if(flag){
        int confirm=JOptionPane.showConfirmDialog(this,new String("Want to continue with the file Submission"),
                    "Confirmation",JOptionPane.OK_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE);

      //makes the connection to the servlet if the user has selected the OK option in the DialogBox.
      if(confirm == JOptionPane.OK_OPTION)
        openConnection();
      }
    }
  }

  /**
   * sets up the connection with the servlet on the server (scec.usc.edu)
   */
  void openConnection() {


    Vector vt =new Vector();
    String fileName = function.getName();
    int size = function.getNum();
    for(int i=0;i<size;i++){
      String temp= new String(function.getX(i) +" "+function.getY(i));
      vt.add(temp);
    }

    try{
      if(D)
      System.out.println("starting to make connection with servlet");
      URL PEER_TestServlet = new
                            URL("http://scec.usc.edu:9999/examples/servlet/PEER_InputFilesServlet");


      URLConnection servletConnection = PEER_TestServlet.openConnection();
      if(D)
      System.out.println("connection established");

      // inform the connection that we will send output and accept input
      servletConnection.setDoInput(true);
      servletConnection.setDoOutput(true);

      // Don't use a cached version of URL connection.
      servletConnection.setUseCaches (false);
      servletConnection.setDefaultUseCaches (false);
      // Specify the content type that we will send binary data
      servletConnection.setRequestProperty ("Content-Type","application/octet-stream");

      // send the serialized ArbDesc. "function" object to the servlet using serialization
      ObjectOutputStream outputToServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());

      if(D){
        System.out.println("Function is:"+function.toString());
        System.out.println("Function Name:"+function.getName());
        System.out.println("Function Number:"+function.getNum());
      }
      outputToServlet.writeObject(fileName);
      outputToServlet.writeObject(vt);
      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "destroy" from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
      ObjectInputStream(servletConnection.getInputStream());

     String temp=inputToServlet.readObject().toString();

     if(D)
       System.out.println("Receiving the Input from the Servlet:"+temp);
     inputToServlet.close();
    }catch (Exception e) {
      System.out.println("Exception in connection with servlet:" +e);
      e.printStackTrace();
    }
  }

  void ResultButton_actionPerformed(ActionEvent e) {
    int confirm = JOptionPane.showConfirmDialog(this,new String("View Results for the PEER Test Files"),
                                  "View PEER Results",JOptionPane.OK_CANCEL_OPTION);

     //opens the new webpage to show the plots for the added PEER Testfiles
      if(confirm == JOptionPane.OK_OPTION){
        try{
        this.getAppletContext().showDocument(new URL("http://scec.usc.edu/OpenSHA/applications/PEER_TestGuiPlotter.html"));
        }catch(Exception ee){
          JOptionPane.showMessageDialog(this,new String(ee.getMessage()),
                                        "Unable to make Connection",JOptionPane.OK_OPTION);
        }
      }

  }
}
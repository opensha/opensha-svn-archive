package org.scec.sha.earthquake.PEER_TestCases.PEER_TestGuiPlots;

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
import org.scec.gui.plot.*;



/**
 * <p>Title: PEER_TestResultsSubmissionApplet </p>
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

public class PEER_TestResultsSubmissionApplet extends JApplet {

  //ClassName and Debug property
  private static final String C = "PEER_TestResultsSubmissionApplet";
  private static final boolean D = true;

  //Directory from which to search for all the PEER test files
  String DIR = "GroupTestDataFiles/";
  String FILE_EXTENSION=".dat";

  //Reads the selected test case file. returns the function conatining x,y values
  ArbitrarilyDiscretizedFunc function= new ArbitrarilyDiscretizedFunc();

  //Vector to store all the existing Test Case file names
   Vector testFiles= new Vector();

  private boolean isStandalone = false;
  private Border border1;
  private Border border2;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JSplitPane mainSplitPane = new JSplitPane();
  private JTextArea yTextArea = new JTextArea();
  private JPanel dataPanel = new JPanel();
  private JButton submitButton = new JButton();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private JLabel xLabel = new JLabel();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JTextField fileNameText = new JTextField();
  private JComboBox testComboBox = new JComboBox();
  private JLabel appletLabel = new JLabel();
  private JButton deleteFileButton = new JButton();
  private JLabel jLabel4 = new JLabel();
  private JTextArea xTextArea = new JTextArea();
  private JLabel jLabel3 = new JLabel();
  private JTextArea messageTextArea = new JTextArea();
  private JLabel jLabel2 = new JLabel();
  private JLabel jLabel1 = new JLabel();
  private JPanel deletePanel = new JPanel();
  private JLabel jLabel5 = new JLabel();
  private JTextArea deletionMessageText = new JTextArea();
  private Border border3;
  private Border border4;
  private Border border5;
  private Border border6;
  private JLabel jLabel6 = new JLabel();
  private JPasswordField filePassword = new JPasswordField();
  private JComboBox fileComboBox = new JComboBox();
  private JLabel jLabel7 = new JLabel();
  private JLabel jLabel8 = new JLabel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public PEER_TestResultsSubmissionApplet() {
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

   int size = testFiles.size();
   for(int i=0;i<size;++i)
     fileComboBox.addItem(testFiles.get(i));
  }


  //Component initialization
  private void jbInit() throws Exception {
    border1 = new EtchedBorder(EtchedBorder.RAISED,new Color(248, 254, 255),new Color(121, 124, 136));
    border2 = new EtchedBorder(EtchedBorder.RAISED,new Color(248, 254, 255),new Color(121, 124, 136));
    border3 = BorderFactory.createLineBorder(Color.white,2);
    border4 = BorderFactory.createLineBorder(Color.white,2);
    border5 = BorderFactory.createLineBorder(Color.black,2);
    border6 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    this.setSize(new Dimension(765, 624));
    this.getContentPane().setLayout(borderLayout1);

    String messageText ="Text Goes Here ...................";
    yTextArea.setBackground(new Color(200, 200, 230));
    yTextArea.setFont(new java.awt.Font("Dialog", 1, 11));
    yTextArea.setForeground(new Color(80, 80, 133));
    dataPanel.setBackground(Color.white);
    dataPanel.setBorder(BorderFactory.createEtchedBorder());
    dataPanel.setLayout(gridBagLayout1);
    submitButton.setBackground(new Color(200, 200, 230));
    submitButton.setFont(new java.awt.Font("Dialog", 1, 12));
    submitButton.setForeground(new Color(80, 80, 133));
    submitButton.setText("Submit");
    submitButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        submitButton_actionPerformed(e);
      }
    });
    xLabel.setFont(new java.awt.Font("Dialog", 1, 12));
    xLabel.setForeground(new Color(80, 80, 133));
    xLabel.setText("X:");
    fileNameText.setBackground(new Color(200, 200, 230));
    fileNameText.setFont(new java.awt.Font("Dialog", 1, 11));
    fileNameText.setForeground(new Color(80, 80, 133));
    testComboBox.setBackground(new Color(200, 200, 230));
    testComboBox.setFont(new java.awt.Font("Dialog", 1, 12));
    testComboBox.setForeground(new Color(80, 80, 133));
    appletLabel.setFont(new java.awt.Font("Dialog", 1, 16));
    appletLabel.setForeground(new Color(80, 80, 133));
    appletLabel.setToolTipText("");
    appletLabel.setHorizontalAlignment(SwingConstants.CENTER);
    appletLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    appletLabel.setText("PEER Test Data Submission");
    deleteFileButton.setBackground(new Color(200, 200, 230));
    deleteFileButton.setFont(new java.awt.Font("Dialog", 1, 12));
    deleteFileButton.setForeground(new Color(80, 80, 133));
    deleteFileButton.setMaximumSize(new Dimension(71, 45));
    deleteFileButton.setMinimumSize(new Dimension(71, 45));
    deleteFileButton.setPreferredSize(new Dimension(71, 45));
    deleteFileButton.setText("Delete ");
    deleteFileButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteFileButton_actionPerformed(e);
      }
    });
    jLabel4.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel4.setForeground(new Color(80, 80, 133));
    jLabel4.setText("Instructions:");
    jLabel4.setVerticalAlignment(SwingConstants.TOP);
    xTextArea.setBackground(new Color(200, 200, 230));
    xTextArea.setFont(new java.awt.Font("Dialog", 1, 11));
    xTextArea.setForeground(new Color(80, 80, 133));
    jLabel3.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel3.setForeground(new Color(80, 80, 133));
    jLabel3.setText("Enter Your Identifier:");
    messageTextArea.setBackground(new Color(200, 200, 230));
    messageTextArea.setFont(new java.awt.Font("Dialog", 1, 11));
    messageTextArea.setForeground(new Color(80, 80, 133));
    messageTextArea.setBorder(BorderFactory.createLineBorder(Color.black));
    messageTextArea.setMinimumSize(new Dimension(359, 120));
    messageTextArea.setPreferredSize(new Dimension(361, 120));
    messageTextArea.setEditable(false);
    messageTextArea.setLineWrap(true);
    messageTextArea.setText(messageText);
    messageTextArea.setEditable(false);
    jLabel2.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel2.setForeground(new Color(80, 80, 133));
    jLabel2.setText("Y:");
    jLabel1.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel1.setForeground(new Color(80, 80, 133));
    jLabel1.setText("Select Test Case:");
    deletePanel.setLayout(gridBagLayout2);
    deletePanel.setBackground(Color.white);
    jLabel5.setFont(new java.awt.Font("Dialog", 1, 16));
    jLabel5.setForeground(new Color(80, 80, 133));
    jLabel5.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel5.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel5.setText("PEER Test Data Deletion");
    deletionMessageText.setBackground(new Color(200, 200, 230));
    deletionMessageText.setFont(new java.awt.Font("Dialog", 1, 11));
    deletionMessageText.setForeground(new Color(80, 80, 133));
    deletionMessageText.setBorder(border6);
    deletionMessageText.setMinimumSize(new Dimension(142, 50));
    deletionMessageText.setPreferredSize(new Dimension(349, 100));
    deletionMessageText.setEditable(false);
    deletionMessageText.setText("Deletion Text goes here.....");
    jLabel6.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel6.setForeground(new Color(80, 80, 133));
    jLabel6.setText("Instructions:");
    filePassword.setForeground(new Color(80, 80, 133));
    filePassword.setFont(new java.awt.Font("Dialog", 1, 12));
    filePassword.setBackground(new Color(200, 200, 230));
    fileComboBox.setForeground(new Color(80, 80, 133));
    fileComboBox.setBackground(new Color(200, 200, 230));
    jLabel7.setText("Enter Password:");
    jLabel7.setForeground(new Color(80, 80, 133));
    jLabel7.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel8.setText("Select File to Delete:");
    jLabel8.setForeground(new Color(80, 80, 133));
    jLabel8.setFont(new java.awt.Font("Dialog", 1, 12));
    mainSplitPane.setDividerSize(5);
    this.getContentPane().add(mainSplitPane, BorderLayout.CENTER);
    mainSplitPane.setDividerLocation(370);
    dataPanel.add(appletLabel,   new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 38, 0, 18), 64, 16));
    mainSplitPane.add(deletePanel, JSplitPane.RIGHT);
    deletePanel.add(jLabel5,       new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 28, 0, 79), 64, 16));
    mainSplitPane.add(dataPanel, JSplitPane.LEFT);
    dataPanel.add(jLabel4,  new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 24, 0, 67), 55, 3));
    dataPanel.add(messageTextArea,  new GridBagConstraints(0, 2, 3, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 24, 0, 18), 0, 0));
    dataPanel.add(jScrollPane2,  new GridBagConstraints(1, 6, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 19, 8, 0), 92, 312));
    dataPanel.add(jScrollPane1,  new GridBagConstraints(0, 6, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 24, 8, 0), 92, 312));
    dataPanel.add(jLabel3,  new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(9, 24, 0, 42), 25, 12));
    dataPanel.add(xLabel,  new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 24, 0, 49), 31, 3));
    dataPanel.add(jLabel2,  new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 56), 23, 7));
    dataPanel.add(jLabel1,  new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 24, 0, 35), 54, 14));
    dataPanel.add(testComboBox,  new GridBagConstraints(2, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(11, 0, 6, 18), -20, -1));
    dataPanel.add(fileNameText,  new GridBagConstraints(2, 4, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 0, 0, 18), 105, 4));
    dataPanel.add(submitButton,        new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(279, 26, 22, 18), 19, 41));
    jScrollPane1.getViewport().add(xTextArea, null);
    jScrollPane2.getViewport().add(yTextArea, null);
    deletePanel.add(deleteFileButton,          new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 103, 309, 15), 24, 0));
    deletePanel.add(jLabel6,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 14, 0, 18), 49, 4));
    deletePanel.add(jLabel8,    new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(7, 14, 0, 0), 16, 11));
    deletePanel.add(jLabel7,    new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(16, 14, 0, 45), 0, 10));
    deletePanel.add(fileComboBox,    new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 17, 0, 25), 39, -1));
    deletePanel.add(filePassword,    new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(19, 16, 0, 25), 166, 2));
    deletePanel.add(deletionMessageText,         new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
            ,GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(-1, 15, 1, 24), 0, 40));
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
    PEER_TestResultsSubmissionApplet applet = new PEER_TestResultsSubmissionApplet();
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
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
      InputStream input = PEER_TestResultsSubmissionApplet.class.getResourceAsStream("/"+DIR+"files.log");
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
    System.out.println("X Values  are:"+st.toString());
    xTextArea.setText(st.toString());
    xTextArea.setEditable(false);
  }


  /**
   * This method is called when the submit button is clicked
   * Provides Error checking to se that user has entered all the valid values in
   * the parameters
   */
  private boolean submitButton() throws RuntimeException{

      //creating the new file name in which function data has to be stored.
      String fileName =new String(testComboBox.getSelectedItem().toString());
      boolean flag = true;
      fileName=fileName.concat("_");
      fileName=fileName.concat(fileNameText.getText());
      fileName=fileName.concat(".dat");
      int size= testFiles.size();
      //checking if the fileName already exists, if so then ask user to input another fileName
      for(int i=0;i<size;++i)
        if(fileName.equals(testFiles.get(i).toString())){
          flag=false;
          break;
        }
      if(flag)
        function.setName(fileName.toString());
      else{
        fileNameText.setText("");
        throw new RuntimeException("Identifier Name already exists, Please enter new Identifier Name");
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

    try{
      if(fileNameText.getText().trim().equals("")){
        flag = false;
        JOptionPane.showMessageDialog(this,new String("Must enter Identifier name"),"Input Error",
                                      JOptionPane.ERROR_MESSAGE);
      }
      else{
        int index = fileNameText.getText().indexOf(" ");
        if(index !=-1){
          fileNameText.setText("");
          throw new RuntimeException("Indentifier name cannot have spaces");
        }
        flag = submitButton();
      }
    }catch(RuntimeException ee){
      flag=false;
      JOptionPane.showMessageDialog(this,new String(ee.getMessage()),
                                    "Input Error",JOptionPane.ERROR_MESSAGE);
    }

    if(flag){
      int confirm=JOptionPane.showConfirmDialog(this,new String("Want to continue with the file Submission??"),
          "Confirmation",JOptionPane.OK_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE);

      //makes the connection to the servlet if the user has selected the OK option in the DialogBox.
      if(confirm == JOptionPane.OK_OPTION)
        openConnection();
    }
  }

  /**
   * sets up the connection with the servlet on the server (scec.usc.edu)
   */
  void openConnection() {


    JFrame frame = new JFrame();
    frame.setTitle("Addition Operation Performed");
    frame.setBounds(this.getAppletXAxisCenterCoor()-60,
                    this.getAppletYAxisCenterCoor()-50,
                    180,150);
    frame.getContentPane().add(new Label("Adding new file, Please be patient......" ));
    frame.pack();
    frame.show();

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

      ObjectOutputStream outputToServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());

      if(D){
        System.out.println("Function is:"+function.toString());
        System.out.println("Function Name:"+function.getName());
        System.out.println("Function Number:"+function.getNum());
      }
      //sending the "Add" string to servlet to tell it to create a new data file
      outputToServlet.writeObject(new String("Add"));

      // sending the name of the new file name to be created by the servlet
      outputToServlet.writeObject(fileName);

      //sending the vector of values to be input in the file, to the servlet
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
     //displaying the user the addition  has ended
     JOptionPane.showMessageDialog(this,new String("File Added Successfully....."),
                                   "Add Confirmation",JOptionPane.OK_OPTION);

     //adding the file to the vector as well as the combo box that
     //displays the files that can be deleted
     fileComboBox.addItem(new String(fileName));
     testFiles.add(new String(fileName));
     frame.dispose();
    }catch (Exception e) {
      System.out.println("Exception in connection with servlet:" +e);
      e.printStackTrace();
    }
  }

  /**
   * Opens the connection with the servlet
   * @param fileName = file to be deleted is sent to the servlet
   */
  void openDeleteConnection(String fileName) {


    JFrame frame = new JFrame();
    frame.setTitle("Delete Operation Performed");
    frame.setBounds(this.getAppletXAxisCenterCoor()-60,
                    this.getAppletYAxisCenterCoor()-50,
                    180,150);
    frame.getContentPane().add(new Label("Deletion being performed, Please be patient......" ));
    frame.pack();
    frame.show();

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

      ObjectOutputStream outputToServlet = new
          ObjectOutputStream(servletConnection.getOutputStream());

      //sending the "Delete" string to servlet to tell it to create a new data file
      outputToServlet.writeObject(new String("Delete"));

      // sending the name of the new file name to be created by the servlet
      outputToServlet.writeObject(fileName);

      outputToServlet.flush();
      outputToServlet.close();

      // Receive the "destroy" from the servlet after it has received all the data
      ObjectInputStream inputToServlet = new
          ObjectInputStream(servletConnection.getInputStream());

      String temp=inputToServlet.readObject().toString();

      if(D)
        System.out.println("Receiving the Input from the Servlet:"+temp);
      inputToServlet.close();
      //displaying the user the deletion  has ended
      JOptionPane.showMessageDialog(this,new String("File Deleted Successfully....."),
                                   "Delete Confirmation",JOptionPane.OK_OPTION);


      //removing the deleted file from the vector as well as the combo box that
      //displays the files that can be deleted
      int size=testFiles.size();
      for(int i=0;i<size;++i)
        if(testFiles.get(i).toString().equals(fileName))
          testFiles.remove(i);
      fileComboBox.removeAllItems();
      size=testFiles.size();
      for(int i=0;i<size;++i)
          fileComboBox.addItem(testFiles.get(i));
      frame.dispose();
    }catch (Exception e) {
      System.out.println("Exception in connection with servlet:" +e);
      e.printStackTrace();
    }
  }


  /**
   * When the user wants to delete the PEER data file from the server
   * @param e
   */
  void deleteFileButton_actionPerformed(ActionEvent e) {
    if(!new String(filePassword.getPassword()).equals(new String("PEER"))){
      filePassword.setText("");
      JOptionPane.showMessageDialog(this,new String("Incorrect Password"),"Check Password",
                                    JOptionPane.OK_OPTION);
    }
    else {
      //delete the file selected.
      int flag=JOptionPane.showConfirmDialog(this,new String("Are you sure you want to delete the file??"),
          "Confirmation Message",JOptionPane.OK_CANCEL_OPTION);

      if(flag == JOptionPane.OK_OPTION)
        openDeleteConnection(fileComboBox.getSelectedItem().toString());
    }

  }

  /**
   * gets the Applets X-axis center coordinates
   * @return
   */
  private int getAppletXAxisCenterCoor() {
    return (this.getX()+this.getWidth())/2;
  }

  /**
   * gets the Applets Y-axis center coordinates
   * @return
   */
  private int getAppletYAxisCenterCoor() {
    return (this.getY() + this.getHeight())/2;
  }


}
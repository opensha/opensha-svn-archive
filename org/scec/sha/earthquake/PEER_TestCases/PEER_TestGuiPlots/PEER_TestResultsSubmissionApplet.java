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

  //Instance for the PEER Delete window class
  PEER_FileDeleteWindow peerDelete;

  private boolean isStandalone = false;
  private Border border1;
  private Border border2;
  private Border border3;
  private Border border4;
  private Border border5;
  private Border border6;
  private JPanel mainPanel = new JPanel();
  private Border border7;
  private JPanel titlePanel = new JPanel();
  private Border border8;
  private JPanel dataPanel = new JPanel();
  private JButton submitButton = new JButton();
  private JTextField fileNameText = new JTextField();
  private JComboBox testComboBox = new JComboBox();
  private JLabel dataSubmLabel = new JLabel();
  private JLabel jLabel4 = new JLabel();
  private JLabel jLabel3 = new JLabel();
  private JTextArea messageTextArea = new JTextArea();
  private JLabel jLabel1 = new JLabel();
  private JTextArea yTextArea = new JTextArea();
  private JLabel xLabel = new JLabel();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JTextArea xTextArea = new JTextArea();
  private JLabel jLabel2 = new JLabel();
  private JPanel deletePanel = new JPanel();
  private Border border9;
  private JLabel jLabel5 = new JLabel();
  private JLabel jLabel6 = new JLabel();
  private JButton deleteFileButton = new JButton();
  private JLabel jLabel7 = new JLabel();
  private JLabel jLabel8 = new JLabel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();
  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
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


  }


  //Component initialization
  private void jbInit() throws Exception {
    border1 = new EtchedBorder(EtchedBorder.RAISED,new Color(248, 254, 255),new Color(121, 124, 136));
    border2 = new EtchedBorder(EtchedBorder.RAISED,new Color(248, 254, 255),new Color(121, 124, 136));
    border3 = BorderFactory.createLineBorder(Color.white,2);
    border4 = BorderFactory.createLineBorder(Color.white,2);
    border5 = BorderFactory.createLineBorder(Color.black,2);
    border6 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border7 = BorderFactory.createEtchedBorder(new Color(248, 254, 255),new Color(121, 124, 136));
    border8 = new EtchedBorder(EtchedBorder.RAISED,new Color(248, 254, 255),new Color(121, 124, 136));
    border9 = new EtchedBorder(EtchedBorder.RAISED,new Color(248, 254, 255),new Color(121, 124, 136));
    this.setSize(new Dimension(663, 551));
    this.getContentPane().setLayout(borderLayout1);

    String messageText ="1) Select the test case you would like to submit data for.\n\n"+
                        "2) Enter your identifier (this is used to label your "+
                        "result in the comparison plot).\n"+
                        "NOTE: your identifier cannot have any spaces, dots(.)"+
                        " or a underscore (_) in it.\n\n"+
                        "3) Paste your y-axis data in the right-hand box below "+
                        "according to the x-values shown in the left-hand box.\n\n"+
                        "4) Hit the submit button.";

    mainPanel.setLayout(gridBagLayout4);
    mainPanel.setBorder(border7);
    titlePanel.setBackground(Color.white);
    titlePanel.setLayout(gridBagLayout3);
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
    fileNameText.setBackground(new Color(200, 200, 230));
    fileNameText.setFont(new java.awt.Font("Dialog", 1, 11));
    fileNameText.setForeground(new Color(80, 80, 133));
    testComboBox.setBackground(new Color(200, 200, 230));
    testComboBox.setFont(new java.awt.Font("Dialog", 1, 12));
    testComboBox.setForeground(new Color(80, 80, 133));
    dataSubmLabel.setFont(new java.awt.Font("Dialog", 1, 16));
    dataSubmLabel.setForeground(new Color(80, 80, 133));
    dataSubmLabel.setToolTipText("");
    dataSubmLabel.setHorizontalAlignment(SwingConstants.CENTER);
    dataSubmLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    dataSubmLabel.setText("Data Submission");
    jLabel4.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel4.setForeground(new Color(80, 80, 133));
    jLabel4.setText("Instructions:");
    jLabel4.setVerticalAlignment(SwingConstants.TOP);
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
    messageTextArea.setWrapStyleWord(true);
    messageTextArea.setText(messageText);
    messageTextArea.setEditable(false);
    jLabel1.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel1.setForeground(new Color(80, 80, 133));
    jLabel1.setText("Select Test Case:");
    jLabel1.setBounds(new Rectangle(19, 319, 172, 31));
    yTextArea.setBackground(new Color(200, 200, 230));
    yTextArea.setFont(new java.awt.Font("Dialog", 1, 11));
    yTextArea.setForeground(new Color(80, 80, 133));
    xLabel.setFont(new java.awt.Font("Dialog", 1, 12));
    xLabel.setForeground(new Color(80, 80, 133));
    xLabel.setText("X:");
    xTextArea.setBackground(new Color(200, 200, 230));
    xTextArea.setFont(new java.awt.Font("Dialog", 1, 11));
    xTextArea.setForeground(new Color(80, 80, 133));
    jLabel2.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel2.setForeground(new Color(80, 80, 133));
    jLabel2.setText("Y:");
    deletePanel.setBackground(Color.white);
    deletePanel.setBorder(border9);
    deletePanel.setLayout(gridBagLayout2);
    jLabel5.setFont(new java.awt.Font("Dialog", 1, 16));
    jLabel5.setForeground(new Color(80, 80, 133));
    jLabel5.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel5.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel5.setText("PEER PSHA-Test Results Submission/Deletion Form");
    jLabel6.setText("Data Deletion");
    jLabel6.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel6.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel6.setForeground(new Color(80, 80, 133));
    jLabel6.setFont(new java.awt.Font("Dialog", 1, 16));
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
    jLabel7.setForeground(new Color(80, 80, 133));
    jLabel7.setText("(Password Protected)");
    jLabel8.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel8.setForeground(new Color(80, 80, 133));
    jLabel8.setText("Select Test Case:");
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.add(dataPanel,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, -4, 0, 1), 0, -3));
    dataPanel.add(dataSubmLabel,  new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 33, 0, 23), 146, 16));
    dataPanel.add(messageTextArea,  new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 18, 0, 0), -32, 107));
    dataPanel.add(jLabel4,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 18, 0, 28), 55, 3));
    dataPanel.add(testComboBox,  new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(28, 11, 0, 0), 20, -1));
    mainPanel.add(deletePanel,  new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, -4, 1, 1), 0, 0));
    deletePanel.add(jLabel6,   new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 235, 0, 147), 158, 16));
    dataPanel.add(submitButton,  new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(21, 7, 10, 19), 12, 8));
    deletePanel.add(jLabel7,      new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 0, 8, 70), 57, 6));
    deletePanel.add(deleteFileButton,             new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(29, 250, 0, 4), 17, 0));
    mainPanel.add(titlePanel,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, -4, 0, 1), 0, 0));
    titlePanel.add(jLabel5,   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 40, 5, 88), 83, 16));
    dataPanel.add(jLabel8,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(22, 18, 0, 0), 50, 13));
    dataPanel.add(jLabel3,  new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(16, 18, 31, 0), 25, 12));
    dataPanel.add(fileNameText,  new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(20, 12, 31, 0), 145, 4));
    dataPanel.add(jScrollPane2,  new GridBagConstraints(3, 2, 1, 3, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 38, 20, 0), 61, 317));
    dataPanel.add(jLabel2,  new GridBagConstraints(3, 0, 1, 2, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(34, 39, 0, 25), 23, 7));
    dataPanel.add(xLabel,  new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 32, 0, 20), 31, 3));
    dataPanel.add(jScrollPane1,  new GridBagConstraints(2, 2, 1, 3, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 32, 20, 0), 63, 317));
    jScrollPane1.getViewport().add(xTextArea, null);
    jScrollPane2.getViewport().add(yTextArea, null);
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
        //throw new RuntimeException("Identifier Name already exists, Please enter new Identifier Name");
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
        int indexofSpace = fileNameText.getText().indexOf(" ");
        int indexofDot = fileNameText.getText().indexOf(".");
        int indexofUnderScore = fileNameText.getText().indexOf("_");
        if(indexofSpace !=-1){
          fileNameText.setText("");
          throw new RuntimeException("Indentifier name cannot have spaces");
        }
        if(indexofUnderScore !=-1){
          fileNameText.setText("");
          throw new RuntimeException("Indentifier name cannot have UnderScore('_')");
        }
        if(indexofDot !=-1){
          fileNameText.setText("");
          throw new RuntimeException("Indentifier name cannot have Dot('.')");
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

      int found=0;
      //makes the connection to the servlet if the user has selected the OK option in the DialogBox.
      if(confirm == JOptionPane.OK_OPTION)
         found=1;
      if(found==1)
         openConnection();
    }
  }

  /**
   * sets up the connection with the servlet on the server (scec.usc.edu)
   */
  void openConnection() {



    //Frame added to show the user that data processing going on
    JFrame frame = new JFrame();
    frame.setTitle("Information");
    JLabel label =new JLabel("Adding new file, Please be patient......");
    frame.getContentPane().setLayout(new GridBagLayout());
    frame.getContentPane().add(label, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 2, 1, 2), 230,50));
    frame.setLocation(getAppletXAxisCenterCoor()-60,getAppletYAxisCenterCoor()-50);
    frame.show();
    label.paintImmediately(label.getBounds());

    //vector which contains all the X and Y values from the function  to be send to the
    //servlet.
    Vector vt =new Vector();

    int size = function.getNum();
    for(int i=0;i<size;i++){
      String temp= new String(function.getX(i) +" "+function.getY(i));
      vt.add(temp);
    }

    //Name of the PEER file to be added
    String fileName = function.getName();

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


    //Frame added to show the user that data processing going on
    JFrame frame = new JFrame();
    frame.setTitle("Information");

    JLabel label =new JLabel("Deletion being performed, Please be patient......");
    frame.getContentPane().setLayout(new GridBagLayout());
    frame.getContentPane().add(label, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 2, 1, 2), 230,50));
    frame.setLocation(getAppletXAxisCenterCoor()-60,getAppletYAxisCenterCoor()-50);
    frame.show();
    label.paintImmediately(label.getBounds());


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

      //sending the "Delete" string to servlet to tell it to delete data file
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
      peerDelete.updateFileNames(testFiles);
      frame.dispose();
    }catch (Exception e) {
      System.out.println("Exception in connection with servlet:" +e);
      e.printStackTrace();
    }
  }


  /**
   * Opens the connection with the servlet to check password
   * @param password= to see if the user has entered the correct password
   * for deletion of file
   */
    public boolean checkPassword(String password) {

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

        if(D)
          System.out.println("Password::"+password);

        //sending the "Delete" string to servlet to tell it to check the password
        outputToServlet.writeObject(new String("Password"));

        // sending the password entered by the user to the servlet to check for its
        // authentication.
        outputToServlet.writeObject(password);

        outputToServlet.flush();
        outputToServlet.close();

        // Receive the "destroy" from the servlet after it has received all the data
        ObjectInputStream inputToServlet = new
            ObjectInputStream(servletConnection.getInputStream());

        String temp=inputToServlet.readObject().toString();

        if(D)
          System.out.println("Receiving the Input from the Servlet:"+temp);
        inputToServlet.close();
        if(temp.equalsIgnoreCase("success"))return true;
        else return false;
      }catch (Exception e) {
        System.out.println("Exception in connection with servlet:" +e);
        e.printStackTrace();
      }
      return false;
    }



  /**
   * When the user wants to delete the PEER data file from the server
   * @param e
   */
  void deleteFileButton_actionPerformed(ActionEvent e) {

     peerDelete = new PEER_FileDeleteWindow(this,testFiles);
     peerDelete.setLocation(getAppletXAxisCenterCoor()-60,getAppletYAxisCenterCoor()-50);
     peerDelete.pack();
     peerDelete.show();
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
package org.scec.sha.earthquake.PEER_TestCases;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Vector;
import java.net.*;



import com.jrefinery.chart.*;
import com.jrefinery.chart.tooltips.*;
import com.jrefinery.data.*;


import org.scec.data.function.*;
import org.scec.gui.*;
import org.scec.gui.plot.LogPlotAPI;
import org.scec.gui.plot.jfreechart.*;
import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.util.*;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta and Vipin Gupta
 * Date : Sept 23 , 2002
 * @version 1.0
 */

public class PEER_TestsApplet extends JApplet implements LogPlotAPI {

  /**
   * Name of the class
   */
  protected final static String C = "PEER_TestApplet";
  // for debug purpose
  protected final static boolean D = false;

  // mesage needed in case of show data if plot is not available
  final static String NO_PLOT_MSG = "No Plot Data Available";

  private Insets plotInsets = new Insets( 4, 10, 4, 4 );

  private boolean isStandalone = false;
  private Border border1;


  //Supported PEER Test Cases
  public final static String PEER_TESTS_SET_ONE = "Set1";
  public final static String PEER_TESTS_SET_TWO = "Set2";

  //Test Cases and the Site Lists
  public final static String TEST_CASE_ONE ="Case1";
  public final static String TEST_CASE_TWO ="Case2";
  public final static String TEST_CASE_THREE ="Case3";
  public final static String TEST_CASE_FOUR ="Case4";
  public final static String TEST_CASE_FIVE ="Case5";
  public final static String TEST_CASE_SIX ="Case6";
  public final static String TEST_CASE_SEVEN ="Case7";
  public final static String TEST_CASE_EIGHT_ONE ="Case8-0sig";
  public final static String TEST_CASE_EIGHT_TWO ="Case8-1sig";
  public final static String TEST_CASE_EIGHT_THREE ="Case8-2sig";
  public final static String TEST_CASE_NINE_ONE ="Case9-Sa97";
  public final static String TEST_CASE_NINE_TWO ="Case9-SA97";
  public final static String TEST_CASE_NINE_THREE ="Case9-Ca97";
  public final static String TEST_CASE_TEN ="Case10";
  public final static String TEST_CASE_ELEVEN ="Case11";


  //Sites Supported
  public final static String SITE_ONE = "a";
  public final static String SITE_TWO = "b";
  public final static String SITE_THREE = "c";
  public final static String SITE_FOUR = "d";
  public final static String SITE_FIVE = "e";
  public final static String SITE_SIX = "f";
  public final static String SITE_SEVEN = "g";




  //log flags declaration
  boolean xLog =false;
  boolean yLog =false;

  // default insets
  Insets defaultInsets = new Insets( 4, 4, 4, 4 );

  // height and width of the applet
  protected final static int W = 970;
  protected final static int H = 750;

  /**
   * FunctionList declared
   */
  DiscretizedFuncList totalProbFuncs = new DiscretizedFuncList();

  DiscretizedFunctionXYDataSet data = new DiscretizedFunctionXYDataSet();

  //Disaggregation Parameter
  DoubleParameter disaggregationParam = new DoubleParameter("Disaggregation Prob",
                                                             0,1,new Double(.01));

  DoubleParameterEditor disaggregationEditor=new DoubleParameterEditor();
  // Create the x-axis and y-axis - either normal or log
  com.jrefinery.chart.NumberAxis xAxis = null;
  com.jrefinery.chart.NumberAxis yAxis = null;


  /**
   * these four values save the custom axis scale specified by user
   */
   protected double minXValue;
   protected double maxXValue;
   protected double minYValue;
   protected double maxYValue;
   protected boolean customAxis = false;


  // make the GroupTestGUIBean instance
  PEER_TestsGuiBean peerTestGuiBean;
  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  private GridBagLayout gridBagLayout6 = new GridBagLayout();
  private GridBagLayout gridBagLayout7 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();

  /**
   * adding scroll pane for showing data
   */
  JScrollPane dataScrollPane = new JScrollPane();

  // text area to show the data values
  JTextArea pointsTextArea = new JTextArea();

  /**
   * chart panel
   */
  ChartPanel chartPanel;

  //flag to check for the disaggregation functionality
  private boolean disaggregationFlag= false;

  // PEER Test Cases
  private String TITLE = new String("PEER Test Cases");

  // light blue color
  Color lightBlue = new Color( 200, 200, 230 );

  /**
   * for Y-log, 0 values will be converted to this small value
   */
  private double Y_MIN_VAL = 1e-16;

  protected boolean graphOn = false;
  private GridBagLayout gridBagLayout11 = new GridBagLayout();
  private JPanel jPanel1 = new JPanel();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private Border border2;
  private final static String AUTO_SCALE = "Auto Scale";
  private final static String CUSTOM_SCALE = "Custom Scale";
  private final static Dimension COMBO_DIM = new Dimension( 180, 30 );
  private final static Dimension BUTTON_DIM = new Dimension( 80, 20 );
  private Border border3;
  private Border border4;
  private GridBagLayout gridBagLayout16 = new GridBagLayout();
  private Border border5;
  private JSplitPane topSplitPane = new JSplitPane();
  private JSplitPane chartSplit = new JSplitPane();
  private JComboBox rangeComboBox = new JComboBox();
  private JButton clearButton = new JButton();
  private JLabel jCustomAxisLabel = new JLabel();
  private JButton toggleButton = new JButton();
  private JCheckBox jCheckylog = new JCheckBox();
  private JPanel buttonPanel = new JPanel();
  private JButton addButton = new JButton();
  private JCheckBox jCheckxlog = new JCheckBox();
  private JPanel panel = new JPanel();
  private GridBagLayout gridBagLayout9 = new GridBagLayout();
  private JSplitPane parameterSplitPane = new JSplitPane();
  private JSplitPane siteSplitPane = new JSplitPane();
  private GridBagLayout gridBagLayout8 = new GridBagLayout();
  private JPanel sitePanel = new JPanel();
  private JPanel imtPanel = new JPanel();
  private JSplitPane controlsSplit = new JSplitPane();
  private GridBagLayout gridBagLayout13 = new GridBagLayout();
  private JPanel sourcePanel = new JPanel();
  private GridBagLayout gridBagLayout5 = new GridBagLayout();
  private Border border6;
  private Border border7;
  private Border border8;
  private JComboBox testCasesCombo = new JComboBox();
  private GridBagLayout gridBagLayout15 = new GridBagLayout();
  private JPanel imrPanel = new JPanel();
  private JLabel jLabel1 = new JLabel();
  private JCheckBox disaggregationCheckbox = new JCheckBox();
  private GridBagLayout gridBagLayout12 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();

  //images for the OpenSHA
  private final static String FRAME_ICON_NAME = "openSHA_Aqua_sm.gif";
  private final static String POWERED_BY_IMAGE = "PoweredBy.gif";

  //static string for the OPENSHA website
  private final static String OPENSHA_WEBSITE="http://www.OpenSHA.org";

  private JLabel imgLabel = new JLabel();
  private GridBagLayout gridBagLayout10 = new GridBagLayout();

  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
        (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public PEER_TestsApplet() {

  data.setFunctions(this.totalProbFuncs);
  // for Y-log, convert 0 values in Y axis to this small value
  data.setConvertZeroToMin(true,Y_MIN_VAL);
  }
  //Initialize the applet
  public void init() {
    try {

      jbInit();
      // make the GroupTestGuiBean
      String testSelected = testCasesCombo.getSelectedItem().toString();
      testCasesCombo.setLightWeightPopupEnabled(false);
      peerTestGuiBean = new PEER_TestsGuiBean(this);
      peerTestGuiBean.setTestCaseAndSite(testSelected);
      this.updateChoosenIMR();
      this.updateChoosenTestCase();
      this.updateChoosenIMT();
      this.updateChoosenEqkSource();

      //initialising the disaggregation parameter and adding to the button Panel
      disaggregationEditor.setParameter(disaggregationParam);
      buttonPanel.add(disaggregationEditor,  new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(18, 7, 42, 110), 16, 0));
      disaggregationEditor.setVisible(false);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  //Component initialization
  private void jbInit() throws Exception {
    border1 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border2 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border3 = BorderFactory.createEmptyBorder();
    border4 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border5 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border6 = BorderFactory.createBevelBorder(BevelBorder.RAISED,Color.white,Color.white,new Color(98, 98, 112),new Color(140, 140, 161));
    border7 = BorderFactory.createBevelBorder(BevelBorder.RAISED,Color.white,Color.white,new Color(98, 98, 112),new Color(140, 140, 161));
    border8 = BorderFactory.createBevelBorder(BevelBorder.RAISED,Color.white,Color.white,new Color(98, 98, 112),new Color(140, 140, 161));
    this.getContentPane().setBackground(Color.white);
    this.setSize(new Dimension(973, 742));
    imgLabel.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        imgLabel_mouseClicked(e);
      }
    });
    this.getContentPane().setLayout(borderLayout1);


    // for showing the data on click of "show data" button
    pointsTextArea.setBorder( BorderFactory.createEtchedBorder() );
    pointsTextArea.setText( NO_PLOT_MSG );
    dataScrollPane.setBorder( BorderFactory.createEtchedBorder() );
    jPanel1.setLayout(gridBagLayout12);



    jPanel1.setBackground(Color.white);
    jPanel1.setBorder(border4);
    jPanel1.setMinimumSize(new Dimension(959, 600));
    jPanel1.setPreferredSize(new Dimension(959, 600));
    topSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    topSplitPane.setBottomComponent(buttonPanel);
    topSplitPane.setDividerSize(5);
    topSplitPane.setTopComponent(chartSplit);
    rangeComboBox.setBackground(new Color(200, 200, 230));
    rangeComboBox.setForeground(new Color(80, 80, 133));
    rangeComboBox.setMaximumSize(new Dimension(115, 19));
    rangeComboBox.setMinimumSize(new Dimension(115, 19));
    rangeComboBox.setPreferredSize(new Dimension(115, 19));
    rangeComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rangeComboBox_actionPerformed(e);
      }
    });
    rangeComboBox.setBackground(new Color(200, 200, 230));
    clearButton.setBackground(new Color(200, 200, 230));
    clearButton.setFont(new java.awt.Font("Dialog", 1, 11));
    clearButton.setForeground(new Color(80, 80, 133));
    clearButton.setBorder(null);
    //clearButton.setBorder(null);
    clearButton.setMaximumSize(new Dimension(97, 31));
    clearButton.setMinimumSize(new Dimension(97, 31));
    clearButton.setPreferredSize(new Dimension(92, 33));
    clearButton.setText("Clear Plot");
    clearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clearButton_actionPerformed(e);
      }
    });
    jCustomAxisLabel.setFont(new java.awt.Font("Dialog", 1, 11));
    jCustomAxisLabel.setForeground(new Color(80, 80, 133));
    jCustomAxisLabel.setText("Set Axis:");
    toggleButton.setBackground(new Color(200, 200, 230));
    toggleButton.setFont(new java.awt.Font("Dialog", 1, 11));
    toggleButton.setForeground(new Color(80, 80, 133));
    toggleButton.setBorder(null);
    //toggleButton.setBorder(null);
    toggleButton.setMaximumSize(new Dimension(97, 31));
    toggleButton.setMinimumSize(new Dimension(97, 31));
    toggleButton.setPreferredSize(new Dimension(92, 33));
    toggleButton.setToolTipText("");
    toggleButton.setText("Show Data");
    toggleButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        toggleButton_actionPerformed(e);
      }
    });
    jCheckylog.setBackground(Color.white);
    jCheckylog.setFont(new java.awt.Font("Dialog", 1, 11));
    jCheckylog.setForeground(new Color(80, 80, 133));
    jCheckylog.setText("Y Log");
    jCheckylog.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jCheckylog_actionPerformed(e);
      }
    });
    buttonPanel.setBackground(Color.white);
    buttonPanel.setBorder(border1);
    buttonPanel.setMaximumSize(new Dimension(2147483647, 40));
    buttonPanel.setMinimumSize(new Dimension(726, 40));
    buttonPanel.setPreferredSize(new Dimension(726, 40));
    buttonPanel.setLayout(gridBagLayout10);
    addButton.setBackground(new Color(200, 200, 230));
    addButton.setFont(new java.awt.Font("Dialog", 1, 11));
    addButton.setForeground(new Color(80, 80, 133));
    addButton.setBorder(null);
    //addButton.setBorder(null);
    addButton.setMaximumSize(new Dimension(97, 31));
    addButton.setMinimumSize(new Dimension(97, 31));
    addButton.setPreferredSize(new Dimension(97, 31));
    addButton.setText("Add Graph");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });
    jCheckxlog.setBackground(Color.white);
    jCheckxlog.setFont(new java.awt.Font("Dialog", 1, 11));
    jCheckxlog.setForeground(new Color(80, 80, 133));
    jCheckxlog.setText("X Log");
    jCheckxlog.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jCheckxlog_actionPerformed(e);
      }
    });
    panel.setLayout(gridBagLayout9);
    panel.setBackground(Color.white);
    panel.setBorder(border5);
    chartSplit.setLeftComponent(panel);
    chartSplit.setRightComponent(parameterSplitPane);
    siteSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    siteSplitPane.setDividerSize(5);
    sitePanel.setLayout(gridBagLayout13);
    sitePanel.setBackground(Color.white);
    imtPanel.setLayout(gridBagLayout8);
    imtPanel.setBackground(Color.white);
    controlsSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    controlsSplit.setDividerSize(5);
    parameterSplitPane.setLeftComponent(controlsSplit);
    parameterSplitPane.setRightComponent(sourcePanel);
    sourcePanel.setLayout(gridBagLayout5);
    sourcePanel.setBackground(Color.white);
    sourcePanel.setBorder(border2);
    sourcePanel.setMaximumSize(new Dimension(2147483647, 10000));
    sourcePanel.setMinimumSize(new Dimension(2, 300));
    sourcePanel.setPreferredSize(new Dimension(2, 300));
    testCasesCombo.setBackground(new Color(200, 200, 230));
    testCasesCombo.setForeground(new Color(80, 80, 133));
    testCasesCombo.setBorder(null);
    imrPanel.setLayout(gridBagLayout15);
    imrPanel.setBackground(Color.white);
    jLabel1.setFont(new java.awt.Font("Dialog", 1, 11));
    jLabel1.setForeground(new Color(80, 80, 133));
    jLabel1.setText("Select Test & Site:");
    disaggregationCheckbox.setBackground(Color.white);
    disaggregationCheckbox.setFont(new java.awt.Font("Dialog", 1, 12));
    disaggregationCheckbox.setForeground(new Color(80, 80, 133));
    disaggregationCheckbox.setText("Disaggregate");
    disaggregationCheckbox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        disaggregationCheckbox_actionPerformed(e);
      }
    });
    //loading the OpenSHA Logo
    imgLabel.setText("");
    imgLabel.setIcon(new ImageIcon(ImageUtils.loadImage(this.POWERED_BY_IMAGE)));
    dataScrollPane.getViewport().add( pointsTextArea, null );
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(topSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 9, 5, 3), 220, 359));
    topSplitPane.add(chartSplit, JSplitPane.TOP);
    chartSplit.add(panel, JSplitPane.LEFT);
    chartSplit.add(parameterSplitPane, JSplitPane.RIGHT);
    parameterSplitPane.add(controlsSplit, JSplitPane.LEFT);
    siteSplitPane.add(sitePanel, JSplitPane.TOP);
    siteSplitPane.add(imtPanel, JSplitPane.BOTTOM);
    controlsSplit.add(imrPanel, JSplitPane.TOP);
    parameterSplitPane.add(sourcePanel, JSplitPane.RIGHT);
    controlsSplit.add(siteSplitPane, JSplitPane.BOTTOM);
    topSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    buttonPanel.add(jCheckylog,   new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(14, 0, 0, 0), 1, 7));
    buttonPanel.add(jCheckxlog,   new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(14, 0, 0, 0), 1, 7));
    buttonPanel.add(toggleButton,   new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(14, 7, 0, 0), 1, 7));
    buttonPanel.add(clearButton,   new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(14, 6, 0, 0), 1, 7));
    buttonPanel.add(addButton,   new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(14, 0, 0, 0), 1, 7));
    buttonPanel.add(testCasesCombo,   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(14, 6, 6, 0), 1, 7));
    buttonPanel.add(jLabel1,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(14, 2, 6, 18), 1, 7));
    buttonPanel.add(rangeComboBox,     new GridBagConstraints(8, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(14, 0, 6, 18), 1, 7));
    buttonPanel.add(disaggregationCheckbox,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(27, 2, 43, 0), 16, 0));
    buttonPanel.add(jCustomAxisLabel,       new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(14, 0, 6, 0), 1, 7));
    buttonPanel.add(imgLabel,      new GridBagConstraints(2, 1, 3, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(44, 68, 12, 21), 30, 26));
    topSplitPane.setDividerLocation(575);
    chartSplit.setDividerLocation(575);
    rangeComboBox.addItem(new String(AUTO_SCALE));
    rangeComboBox.addItem(new String(CUSTOM_SCALE));
    parameterSplitPane.setDividerLocation(175);
    siteSplitPane.setDividerLocation(175);
    controlsSplit.setDividerLocation(175);
    sourcePanel.validate();
    sourcePanel.repaint();

    //initialising the values inside the combobox for the supported test cases and sites
    Vector v = new Vector();

    //test case-1 ,Set-1
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ONE+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ONE+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ONE+"-"+this.SITE_THREE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ONE+"-"+this.SITE_FOUR));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ONE+"-"+this.SITE_FIVE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ONE+"-"+this.SITE_SIX));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ONE+"-"+this.SITE_SEVEN));


    //test case-2,Set-1
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWO+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWO+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWO+"-"+this.SITE_THREE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWO+"-"+this.SITE_FOUR));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWO+"-"+this.SITE_FIVE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWO+"-"+this.SITE_SIX));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TWO+"-"+this.SITE_SEVEN));


    //test case-3
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_THREE+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_THREE+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_THREE+"-"+this.SITE_THREE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_THREE+"-"+this.SITE_FOUR));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_THREE+"-"+this.SITE_FIVE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_THREE+"-"+this.SITE_SIX));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_THREE+"-"+this.SITE_SEVEN));


    //test case-4
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_THREE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_FOUR));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_FIVE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_SIX));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_SEVEN));


    //test case-5
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_THREE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_FOUR));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_FIVE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_SIX));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_FIVE+"-"+this.SITE_SEVEN));

    //test case-6
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SIX+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SIX+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SIX+"-"+this.SITE_THREE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SIX+"-"+this.SITE_FOUR));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SIX+"-"+this.SITE_FIVE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SIX+"-"+this.SITE_SIX));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SIX+"-"+this.SITE_SEVEN));


    //test case-7
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SEVEN+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SEVEN+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SEVEN+"-"+this.SITE_THREE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SEVEN+"-"+this.SITE_FOUR));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SEVEN+"-"+this.SITE_FIVE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SEVEN+"-"+this.SITE_SIX));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_SEVEN+"-"+this.SITE_SEVEN));

    //test case-8_0sig
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_ONE+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_ONE+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_ONE+"-"+this.SITE_THREE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_ONE+"-"+this.SITE_FOUR));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_ONE+"-"+this.SITE_FIVE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_ONE+"-"+this.SITE_SIX));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_ONE+"-"+this.SITE_SEVEN));

    //test case-8_1sig
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_TWO+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_TWO+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_TWO+"-"+this.SITE_THREE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_TWO+"-"+this.SITE_FOUR));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_TWO+"-"+this.SITE_FIVE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_TWO+"-"+this.SITE_SIX));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_TWO+"-"+this.SITE_SEVEN));

    //test case-8_2sig
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_THREE+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_THREE+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_THREE+"-"+this.SITE_THREE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_THREE+"-"+this.SITE_FOUR));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_THREE+"-"+this.SITE_FIVE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_THREE+"-"+this.SITE_SIX));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_EIGHT_THREE+"-"+this.SITE_SEVEN));


    //test case-9_Sa97
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_ONE+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_ONE+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_ONE+"-"+this.SITE_THREE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_ONE+"-"+this.SITE_FOUR));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_ONE+"-"+this.SITE_FIVE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_ONE+"-"+this.SITE_SIX));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_ONE+"-"+this.SITE_SEVEN));

    //test case-9_SA97
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_TWO+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_TWO+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_TWO+"-"+this.SITE_THREE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_TWO+"-"+this.SITE_FOUR));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_TWO+"-"+this.SITE_FIVE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_TWO+"-"+this.SITE_SIX));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_TWO+"-"+this.SITE_SEVEN));

    //test case-9_Ca97
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_THREE+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_THREE+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_THREE+"-"+this.SITE_THREE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_THREE+"-"+this.SITE_FOUR));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_THREE+"-"+this.SITE_FIVE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_THREE+"-"+this.SITE_SIX));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_NINE_THREE+"-"+this.SITE_SEVEN));

    //test case-10
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TEN+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TEN+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TEN+"-"+this.SITE_THREE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_TEN+"-"+this.SITE_FOUR));


    //test case-11
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ELEVEN+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ELEVEN+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ELEVEN+"-"+this.SITE_THREE));
    v.add(new String(this.PEER_TESTS_SET_ONE +"-"+this.TEST_CASE_ELEVEN+"-"+this.SITE_FOUR));


    //test case-1 , Set-2
    v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_ONE+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_ONE+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_ONE+"-"+this.SITE_THREE));

    //test case-2 , Set-2
    v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_TWO+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_TWO+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_TWO+"-"+this.SITE_THREE));

    //test case-3 , Set-2
    v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_THREE+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_THREE+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_THREE+"-"+this.SITE_THREE));


    //test case-4 , Set-2
    v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_FOUR+"-"+this.SITE_THREE));

    //test case-6 , Set-2
    v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_SIX+"-"+this.SITE_ONE));
    v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_SIX+"-"+this.SITE_TWO));
    v.add(new String(this.PEER_TESTS_SET_TWO +"-"+this.TEST_CASE_SIX+"-"+this.SITE_THREE));


    int size = v.size();
    for(int i=0;i<size;++i)
      this.testCasesCombo.addItem(v.get(i));

    testCasesCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        testCasesCombo_actionPerformed(e);
      }
    });
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
    return "Applet Information";
  }

  //Get parameter info
  public String[][] getParameterInfo() {
    return null;
  }

  //Main method
  public static void main(String[] args) {
    PEER_TestsApplet applet = new PEER_TestsApplet();
    applet.isStandalone = true;
    JFrame frame = new JFrame();
    //EXIT_ON_CLOSE == 3
    frame.setDefaultCloseOperation(3);
    frame.setTitle("Peer Group Tests");
    frame.getContentPane().add(applet, BorderLayout.CENTER);
    applet.init();
    applet.start();
    frame.setSize(W,H);
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
   *  update the GUI with the test case choosen
   */
  public void updateChoosenTestCase() {
    if(this.peerTestGuiBean==null)
      return;
  }



  /**
   *  update the GUI with the IMT choosen
   */
  public void updateChoosenIMT() {
    if(this.peerTestGuiBean==null)
      return;
    imtPanel.removeAll();
    imtPanel.setLayout(gridBagLayout8);
    imtPanel.add(peerTestGuiBean.getIMTEditor(),
                 new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                 GridBagConstraints.CENTER,
                 GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    imtPanel.validate();
    imtPanel.repaint();
  }

  /**
   *  update the GUI with the IMR choosen
   *  refresh the sites params as well
   */
  public void updateChoosenIMR() {
    // update the IMR and site panel
    if(this.peerTestGuiBean==null)
      return;
    imrPanel.removeAll();
    sitePanel.removeAll();
    // update the imr editor
    imrPanel.add(peerTestGuiBean.getImrEditor(),
                 new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                 GridBagConstraints.CENTER,
                 GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    // update the site editor
    sitePanel.add(peerTestGuiBean.getSiteEditor(),
                 new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                 GridBagConstraints.CENTER,
                 GridBagConstraints.BOTH, defaultInsets, 0, 0 ));

    imrPanel.validate();
    imrPanel.repaint();
    sitePanel.validate();
    sitePanel.repaint();
  }

  /**
   *  update the GUI with the choosen Eqk source
   */
  public void updateChoosenEqkSource() {
    // update the EqkSource panel
    if(this.peerTestGuiBean==null)
      return;
    this.sourcePanel.removeAll();
    sourcePanel.setLayout(gridBagLayout5);
    sourcePanel.add(peerTestGuiBean.get_erf_Editor(),
                    new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    sourcePanel.validate();
    sourcePanel.repaint();
  }


  /**
   *  Adds a feature to the GraphPanel attribute of the EqkForecastApplet object
   */
  protected void addGraphPanel() {

      // Starting
      String S = C + ": addGraphPanel(): ";

      String newXYAxisName = this.totalProbFuncs.getXYAxesName();


      // create a default chart based on some sample data...

      // Determine which IM to add to the axis labeling
      String xAxisLabel = totalProbFuncs.getXAxisName();
      String yAxisLabel = totalProbFuncs.getYAxisName();


      //create the standard ticks so that smaller values too can plotted on the chart
      TickUnits units = MyTickUnits.createStandardTickUnits();



      /// check if x log is selected or not
      if(xLog) xAxis = new HorizontalLogarithmicAxis(xAxisLabel);
      else xAxis = new HorizontalNumberAxis( xAxisLabel );

      xAxis.setAutoRangeIncludesZero( false );
      xAxis.setCrosshairLockedOnData( false );
      xAxis.setCrosshairVisible(false);
      xAxis.setStandardTickUnits(units);

      /// check if y log is selected or not
      if(yLog) yAxis = new VerticalLogarithmicAxis(yAxisLabel);
      else yAxis = new VerticalNumberAxis( yAxisLabel );

      yAxis.setAutoRangeIncludesZero( false );
      yAxis.setCrosshairLockedOnData( false );
      yAxis.setCrosshairVisible( false);
      yAxis.setStandardTickUnits(units);

      int type = com.jrefinery.chart.StandardXYItemRenderer.LINES;


      LogXYItemRenderer renderer = new LogXYItemRenderer( type, new StandardXYToolTipGenerator() );


      /* to set the range of the axis on the input from the user if the range combo box is selected*/
      if(this.customAxis) {
          xAxis.setRange(this.minXValue,this.maxXValue);
          yAxis.setRange(this.minYValue,this.maxYValue);
        }

      // build the plot
      org.scec.gui.PSHALogXYPlot plot = new org.scec.gui.PSHALogXYPlot(this,data,
                                       xAxis, yAxis, xLog, yLog);


      plot.setBackgroundAlpha( .8f );



      plot.setRenderer( renderer );


      JFreeChart chart = new JFreeChart(TITLE, JFreeChart.DEFAULT_TITLE_FONT, plot, false );

      chart.setBackgroundPaint( lightBlue );

      // chart.setBackgroundImage(image);
      // chart.setBackgroundImageAlpha(.3f);

      // Put into a panel
      chartPanel = new ChartPanel(chart, true, true, true, true, false);
      //panel.setMouseZoomable(true);

      chartPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );
      chartPanel.setMouseZoomable(true);
      chartPanel.setDisplayToolTips(true);
      chartPanel.setHorizontalAxisTrace(false);
      chartPanel.setVerticalAxisTrace(false);


      if(D) System.out.println(this.totalProbFuncs.toString());
      if(D) System.out.println(S + "data:" + data);

      graphOn=false;
      togglePlot();
   }


   /**
    *  Description of the Method
    */
   protected void togglePlot() {

       // Starting
       String S = C + ": togglePlot(): ";
       panel.removeAll();
       if ( graphOn ) {

           this.toggleButton.setText( "Show Plot" );
           graphOn = false;

           panel.add( dataScrollPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                   , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );
       }
       else {
           graphOn = true;
           // dataScrollPane.setVisible(false);
           this.toggleButton.setText( "Show Data" );
                         // panel added here
           if(chartPanel !=null)
               panel.add( chartPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                       , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

           else
               // innerPlotPanel.setBorder(oval);
               panel.add( dataScrollPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                       , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );

       }


       validate();
       repaint();

       if ( D ) System.out.println( S + "Ending" );

    }

    /**
     * this function is called when Add Graph button is clicked
     * @param e
     */
    void addButton_actionPerformed(ActionEvent e) {
      addButton();
    }


    /**
     * this function is called to draw the graph
     */
    private void addButton() {


      // clear the function list
      //this.totalProbFuncs.clear();

      peerTestGuiBean.getChoosenFunction(totalProbFuncs);



      // set the log values
      data.setXLog(xLog);
      data.setYLog(yLog);

      // set the data in the text area
      String xAxisTitle =  totalProbFuncs.getXAxisName();
      String yAxisTitle =  totalProbFuncs.getYAxisName();

      this.pointsTextArea.setText("X Axis:"+ xAxisTitle + "\n" +
                                  "Y Axis:" + yAxisTitle +"\n" +
                                  totalProbFuncs.toString());
      addGraphPanel();

      //displays the disaggregation string in the pop-up window
      String disaggregationString= peerTestGuiBean.getDisaggregationString();
      if(disaggregationString !=null)
        JOptionPane.showMessageDialog(this,new String(disaggregationString),
                                     "Disaggregation Result",JOptionPane.OK_OPTION);
      disaggregationString=null;
    }

    /**
     * if we select or deselect x log
     * @param e
     */
    void jCheckxlog_actionPerformed(ActionEvent e) {
      xLog  = this.jCheckxlog.isSelected();
      data.setXLog(xLog);
      addGraphPanel();
    }

    /**
     * if we select or deselect x log
     * @param e
     */
    void jCheckylog_actionPerformed(ActionEvent e) {
      yLog  = this.jCheckylog.isSelected();
      data.setYLog(yLog);
      addGraphPanel();
  }

  /**
   * This function handles the Zero values in the X and Y data set when exception is thrown,
   * it reverts back to the linear scale displaying a message box to the user.
   */
  public void invalidLogPlot(String message) {

     int xCenter=getAppletXAxisCenterCoor();
     int yCenter=getAppletYAxisCenterCoor();
     if(message.equals("Log Value of the negative values and 0 does not exist for X-Log Plot")) {
       this.jCheckxlog.setSelected(false);
       ShowMessage showMessage=new ShowMessage("      X-Log Plot Error as it contains Zero Values");
       showMessage.setBounds(xCenter-60,yCenter-50,370,145);
       showMessage.pack();
       showMessage.show();
     }
     if(message.equals("Log Value of the negative values and 0 does not exist for Y-Log Plot")) {
       this.jCheckylog.setSelected(false);
       ShowMessage showMessage=new ShowMessage("      Y-Log Plot Error as it contains Zero Values");
       showMessage.setBounds(xCenter-60,yCenter-50,375,148);
       showMessage.pack();
       showMessage.show();
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

  /**
   * when "show data" button is clicked
   *
   * @param e
   */
  void toggleButton_actionPerformed(ActionEvent e) {
    this.togglePlot();
  }

  /**
   * this function is called when "clear plot" is selected
   *
   * @param e
   */
  void clearButton_actionPerformed(ActionEvent e) {
    clearPlot(true);
  }

  /**
   *  Clears the plot screen of all traces
   */
  void clearPlot(boolean clearFunctions) {

    if ( D )
      System.out.println( "Clearing plot area" );

    int loc = this.chartSplit.getDividerLocation();
    int newLoc = loc;

    panel.removeAll();

    pointsTextArea.setText( NO_PLOT_MSG );
    if( clearFunctions) {
      this.totalProbFuncs.clear();
    }

    validate();
    repaint();
    chartSplit.setDividerLocation( newLoc );
  }

  /**
  * whenever selection is made in the combo box
  * @param e
  */
  void rangeComboBox_actionPerformed(ActionEvent e) {

    String str=(String)rangeComboBox.getSelectedItem();
    if(str.equalsIgnoreCase(AUTO_SCALE)){
      customAxis=false;
      addGraphPanel();
    }
    if(str.equalsIgnoreCase(CUSTOM_SCALE))  {
       Range rX = xAxis.getRange();
       Range rY= yAxis.getRange();
       double minX=rX.getLowerBound();
       double maxX=rX.getUpperBound();
       double minY=rY.getLowerBound();
       double maxY=rY.getUpperBound();


       int xCenter=getAppletXAxisCenterCoor();
       int yCenter=getAppletYAxisCenterCoor();
       AxisScale axisScale=new AxisScale(this,minX,maxX,minY,maxY);
       axisScale.setBounds(xCenter-60,yCenter-50,375,148);
       axisScale.pack();
       axisScale.show();
    }
  }

  /**
   * sets the range for X-axis
   * @param xMin : minimum value for X-axis
   * @param xMax : maximum value for X-axis
   */
  public void setXRange(double xMin,double xMax) {
     minXValue=xMin;
     maxXValue=xMax;
     this.customAxis=true;

  }

  /**
   * sets the range for Y-axis
   * @param yMin : minimum value for Y-axis
   * @param yMax : maximum value for Y-axis
   */
  public void setYRange(double yMin,double yMax) {
     minYValue=yMin;
     maxYValue=yMax;
     this.customAxis=true;
     addGraphPanel();
  }

  void testCasesCombo_actionPerformed(ActionEvent e) {
    String testSelected = testCasesCombo.getSelectedItem().toString();
    peerTestGuiBean.setTestCaseAndSite(testSelected);
  }

  void disaggregationCheckbox_actionPerformed(ActionEvent e) {
    if(disaggregationCheckbox.isSelected()){

      disaggregationEditor.setVisible(true);
      disaggregationFlag=true;
    }
    else{
      disaggregationEditor.setVisible(false);
      disaggregationFlag=false;
    }
  }

 boolean getDisaggregationFlag(){
  return disaggregationFlag;
 }

 /**
  * @return the value of the disaggregation parameter
  */
 double getDisaggregationProbablity(){
   return ((Double)(disaggregationParam.getValue())).doubleValue();
 }

 void imgLabel_mouseClicked(MouseEvent e) {
     try{
     this.getAppletContext().showDocument(new URL(OPENSHA_WEBSITE));
     }catch(java.net.MalformedURLException ee){
       JOptionPane.showMessageDialog(this,new String("No Internet Connection Available"),
                                     "Error Connecting to Internet",JOptionPane.OK_OPTION);
     }
  }

}

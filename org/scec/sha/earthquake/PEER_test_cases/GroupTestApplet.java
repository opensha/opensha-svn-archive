package org.scec.sha.earthquake.PEER_test_cases;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;



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



/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin gupta and Vipin Gupta
 * Date : Sept 23 , 2002
 * @version 1.0
 */

public class GroupTestApplet extends JApplet implements LogPlotAPI {

  /**
   * Name of the class
   */
  protected final static String C = "GroupTestApplet";
  // for debug purpose
  protected final static boolean D = true;

  // mesage needed in case of show data if plot is not available
  final static String NO_PLOT_MSG = "No Plot Data Available";

  private Insets plotInsets = new Insets( 4, 10, 4, 4 );

  private boolean isStandalone = false;
  private Border border1;


  //log flags declaration
  boolean xLog =false;
  boolean yLog =false;

  // default insets
  Insets defaultInsets = new Insets( 4, 4, 4, 4 );

  // height and width of the applet
  protected final static int W = 950;
  protected final static int H = 700;

  /**
   * FunctionList declared
   */
  DiscretizedFuncList totalProbFuncs = new DiscretizedFuncList();

  DiscretizedFunctionXYDataSet data = new DiscretizedFunctionXYDataSet();


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
  GroupTestGuiBean groupTestBean;
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

  // PEER Test Cases
  private String TITLE = new String("PEER Test Cases");

  // light blue color
  Color lightBlue = new Color( 200, 200, 230 );

  /**
   * for Y-log, 0 values will be converted to this small value
   */
  private double Y_MIN_VAL = 1e-8;

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
  private GridBagLayout gridBagLayout12 = new GridBagLayout();
  private JPanel panel = new JPanel();
  private GridBagLayout gridBagLayout9 = new GridBagLayout();
  private JSplitPane parameterSplitPane = new JSplitPane();
  private JSplitPane siteSplitPane = new JSplitPane();
  private GridBagLayout gridBagLayout8 = new GridBagLayout();
  private JSplitPane testSplitPane = new JSplitPane();
  private JPanel testCasesPanel = new JPanel();
  private JPanel sitePanel = new JPanel();
  private JPanel imtPanel = new JPanel();
  private JSplitPane controlsSplit = new JSplitPane();
  private GridBagLayout gridBagLayout15 = new GridBagLayout();
  private GridBagLayout gridBagLayout14 = new GridBagLayout();
  private GridBagLayout gridBagLayout13 = new GridBagLayout();
  private JPanel imrPanel = new JPanel();
  private JPanel sourcePanel = new JPanel();
  private GridBagLayout gridBagLayout5 = new GridBagLayout();
  private GridBagLayout gridBagLayout10 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();




  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
        (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public GroupTestApplet() {

  data.setFunctions(this.totalProbFuncs);
  // for Y-log, convert 0 values in Y axis to this small value
  data.setConvertZeroToMin(true,Y_MIN_VAL);
  }
  //Initialize the applet
  public void init() {
    try {

      jbInit();
      // make the GroupTestGuiBean
      groupTestBean = new GroupTestGuiBean(this);
      this.updateChoosenIMR();
      this.updateChoosenTestCase();
      this.updateChoosenIMT();
      this.updateChoosenEqkSource();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  //Component initialization
  private void jbInit() throws Exception {
    border1 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border2 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border3 = BorderFactory.createBevelBorder(BevelBorder.RAISED,Color.white,Color.white,new Color(98, 98, 112),new Color(140, 140, 161));
    border4 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    border5 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    this.getContentPane().setBackground(Color.white);
    this.setSize(new Dimension(959, 644));
    this.getContentPane().setLayout(borderLayout1);


    // for showing the data on click of "show data" button
    pointsTextArea.setBorder( BorderFactory.createEtchedBorder() );
    pointsTextArea.setText( NO_PLOT_MSG );
    dataScrollPane.setBorder( BorderFactory.createEtchedBorder() );
    jPanel1.setLayout(gridBagLayout10);



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
    clearButton.setBorder(border3);
    clearButton.setMaximumSize(new Dimension(80, 21));
    clearButton.setMinimumSize(new Dimension(80, 21));
    clearButton.setPreferredSize(new Dimension(80, 21));
    clearButton.setText("Clear Plot");
    clearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clearButton_actionPerformed(e);
      }
    });
    jCustomAxisLabel.setFont(new java.awt.Font("Dialog", 1, 11));
    jCustomAxisLabel.setForeground(new Color(80, 80, 133));
    jCustomAxisLabel.setText("Custom Axis Label");
    toggleButton.setBackground(new Color(200, 200, 230));
    toggleButton.setFont(new java.awt.Font("Dialog", 1, 11));
    toggleButton.setForeground(new Color(80, 80, 133));
    toggleButton.setBorder(border3);
    toggleButton.setMaximumSize(new Dimension(80, 39));
    toggleButton.setMinimumSize(new Dimension(80, 21));
    toggleButton.setPreferredSize(new Dimension(80, 21));
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
    buttonPanel.setLayout(gridBagLayout12);
    addButton.setBackground(new Color(200, 200, 230));
    addButton.setFont(new java.awt.Font("Dialog", 1, 11));
    addButton.setForeground(new Color(80, 80, 133));
    addButton.setBorder(border3);
    addButton.setMaximumSize(new Dimension(80, 20));
    addButton.setMinimumSize(new Dimension(80, 20));
    addButton.setPreferredSize(new Dimension(80, 20));
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
    testSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    testSplitPane.setDividerSize(5);
    testCasesPanel.setLayout(gridBagLayout14);
    testCasesPanel.setBackground(Color.white);
    sitePanel.setLayout(gridBagLayout13);
    sitePanel.setBackground(Color.white);
    imtPanel.setLayout(gridBagLayout8);
    imtPanel.setBackground(Color.white);
    controlsSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    controlsSplit.setDividerSize(5);
    imrPanel.setLayout(gridBagLayout15);
    imrPanel.setBackground(Color.white);
    parameterSplitPane.setLeftComponent(controlsSplit);
    parameterSplitPane.setRightComponent(sourcePanel);
    sourcePanel.setLayout(gridBagLayout5);
    sourcePanel.setBackground(Color.white);
    sourcePanel.setBorder(border2);
    sourcePanel.setMaximumSize(new Dimension(2147483647, 10000));
    sourcePanel.setMinimumSize(new Dimension(2, 300));
    sourcePanel.setPreferredSize(new Dimension(2, 300));
    dataScrollPane.getViewport().add( pointsTextArea, null );
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(topSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 9, 3, 0), 204, 251));
    topSplitPane.add(chartSplit, JSplitPane.TOP);
    chartSplit.add(panel, JSplitPane.LEFT);
    chartSplit.add(parameterSplitPane, JSplitPane.RIGHT);
    parameterSplitPane.add(controlsSplit, JSplitPane.LEFT);
    siteSplitPane.add(sitePanel, JSplitPane.TOP);
    siteSplitPane.add(imtPanel, JSplitPane.BOTTOM);
    parameterSplitPane.add(sourcePanel, JSplitPane.RIGHT);
    controlsSplit.add(testSplitPane, JSplitPane.TOP);
    controlsSplit.add(siteSplitPane, JSplitPane.BOTTOM);
    testSplitPane.add(testCasesPanel, JSplitPane.TOP);
    testSplitPane.add(imrPanel, JSplitPane.BOTTOM);
    topSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    buttonPanel.add(toggleButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 9, 10, 3), 2, -2));
    buttonPanel.add(clearButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 9, 10, 0), 9, -2));
    buttonPanel.add(jCheckxlog, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 20, 10, 0), 20, 2));
    buttonPanel.add(jCheckylog, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 0, 10, 0), 22, 2));
    buttonPanel.add(jCustomAxisLabel, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 11), 0, 0));
    buttonPanel.add(rangeComboBox, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    buttonPanel.add(addButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 9, 10, 0), 9, -2));
    topSplitPane.setDividerLocation(575);
    chartSplit.setDividerLocation(575);
    rangeComboBox.addItem(new String(AUTO_SCALE));
    rangeComboBox.addItem(new String(CUSTOM_SCALE));
    parameterSplitPane.setDividerLocation(175);
    siteSplitPane.setDividerLocation(125);
    testSplitPane.setDividerLocation(150);
    controlsSplit.setDividerLocation(350);
    sourcePanel.validate();
    sourcePanel.repaint();

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
    GroupTestApplet applet = new GroupTestApplet();
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
    if(this.groupTestBean==null)
      return;
    testCasesPanel.removeAll();
    testCasesPanel.add(groupTestBean.getTestCasesEditor(),
                       new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                       GridBagConstraints.CENTER,
                       GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    testCasesPanel.validate();
    testCasesPanel.repaint();

  }



  /**
   *  update the GUI with the IMT choosen
   */
  public void updateChoosenIMT() {
    if(this.groupTestBean==null)
      return;
    imtPanel.removeAll();
    imtPanel.setLayout(gridBagLayout8);
    imtPanel.add(groupTestBean.getIMTEditor(),
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
    if(this.groupTestBean==null)
      return;
    imrPanel.removeAll();
    sitePanel.removeAll();
    // update the imr editor
    imrPanel.add(groupTestBean.getImrEditor(),
                 new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                 GridBagConstraints.CENTER,
                 GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    // update the site editor
    sitePanel.add(groupTestBean.getSiteEditor(),
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
    if(this.groupTestBean==null)
      return;
    this.sourcePanel.removeAll();
    sourcePanel.setLayout(gridBagLayout5);
    sourcePanel.add(groupTestBean.getEqkSourceEditor(),
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



      plot.setXYItemRenderer( renderer );


      JFreeChart chart = new JFreeChart(TITLE, JFreeChart.DEFAULT_TITLE_FONT, plot, false );

      chart.setBackgroundPaint( lightBlue );

      // chart.setBackgroundImage(image);
      // chart.setBackgroundImageAlpha(.3f);

      // Put into a panel
      chartPanel = new ChartPanel(chart, true, true, true, true, false);
      //panel.setMouseZoomable(true);

      chartPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );
      chartPanel.setMouseZoomable(true);
      chartPanel.setGenerateToolTips(true);
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

      groupTestBean.getChoosenFunction(totalProbFuncs);



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
       PEER_Test_Cases_AxisScale axisScale=new PEER_Test_Cases_AxisScale(this,minX,maxX,minY,maxY);
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

}

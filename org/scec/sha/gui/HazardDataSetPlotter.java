package org.scec.sha.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.jfree.data.Range;
import org.scec.data.Site;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.data.function.DiscretizedFuncAPI;
import org.scec.data.function.DiscretizedFuncList;
import org.scec.gui.plot.jfreechart.DiscretizedFunctionXYDataSet;
import org.scec.param.event.ParameterChangeEvent;
import org.scec.param.event.ParameterChangeListener;
import org.scec.sha.calc.FractileCurveCalculator;
import org.scec.sha.calc.HazardCurveCalculator;
import org.scec.sha.gui.beans.HazardDataSiteSelectionGuiBean;
import org.scec.sha.gui.infoTools.ButtonControlPanel;
import org.scec.sha.gui.infoTools.ButtonControlPanelAPI;
import org.scec.sha.gui.infoTools.CalcProgressBar;
import org.scec.sha.gui.infoTools.GraphPanel;
import org.scec.sha.gui.infoTools.GraphPanelAPI;
import org.scec.sha.gui.infoTools.GraphWindow;
import org.scec.sha.gui.infoTools.GraphWindowAPI;
import org.scec.sha.gui.infoTools.HazardCurveDisaggregationWindow;
import org.scec.util.ImageUtils;
import org.scec.util.SystemPropertiesUtils;
import org.scec.util.FileUtils;

import ch.randelshofer.quaqua.QuaquaManager;
import java.awt.*;

/**
 * <p>Title: HazardDataSetPlotter</p>
 * <p>Description: </p>
 * @author Nitin Gupta and Vipin Gupta
 * Date : Sept 23 , 2002
 * @version 1.0
 */

public class HazardDataSetPlotter extends JApplet
    implements ButtonControlPanelAPI,GraphPanelAPI, GraphWindowAPI{

  /**
   * Name of the class
   */
  private final static String C = "HazardDataSetPlotter";
  // for debug purpose
  private final static boolean D = false;

  //instance for the ButtonControlPanel
  ButtonControlPanel buttonControlPanel;

  //instance of the GraphPanel (window that shows all the plots)
  GraphPanel graphPanel;

  //instance of the GraphWindow to pop up when the user wants to "Peel-Off" curves;
  GraphWindow graphWindow;

  private Insets plotInsets = new Insets( 4, 10, 4, 4 );

  private boolean isStandalone = false;
  private Border border1;

  //log flags declaration
  private boolean xLog =false;
  private boolean yLog =false;

  // default insets
  private Insets defaultInsets = new Insets( 4, 4, 4, 4 );

  // height and width of the applet
  private final static int W = 1100;
  private final static int H = 750;

  /**
   * FunctionList declared
   */
  private DiscretizedFuncList functions = new DiscretizedFuncList();
  private DiscretizedFunctionXYDataSet data = new DiscretizedFunctionXYDataSet();

  //holds the ArbitrarilyDiscretizedFunc
  private ArbitrarilyDiscretizedFunc function;

  private HazardDataSiteSelectionGuiBean siteGuiBean;


  /**
   * these four values save the custom axis scale specified by user
   */
  private double minXValue;
  private double maxXValue;
  private  double minYValue;
  private double maxYValue;
  private boolean customAxis = false;


  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  private GridBagLayout gridBagLayout6 = new GridBagLayout();
  private GridBagLayout gridBagLayout7 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();



  //flags to check which X Values the user wants to work with: default or custom
  boolean useCustomX_Values = false;


  // PEER Test Cases
  private String TITLE = new String("Hazard Curves");

  // light blue color
  private Color lightBlue = new Color( 200, 200, 230 );

  /**
   * for Y-log, 0 values will be converted to this small value
   */
  private double Y_MIN_VAL = 1e-16;

  private boolean graphOn = false;
  private GridBagLayout gridBagLayout11 = new GridBagLayout();
  private JPanel jPanel1 = new JPanel();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private Border border2;
  private final static Dimension COMBO_DIM = new Dimension( 180, 30 );
  private final static Dimension BUTTON_DIM = new Dimension( 80, 20 );
  private Border border3;
  private Border border4;
  private Border border5;
  private Border border6;
  private Border border7;
  private Border border8;



  //images for the OpenSHA
  private final static String FRAME_ICON_NAME = "openSHA_Aqua_sm.gif";
  private final static String POWERED_BY_IMAGE = "PoweredBy.gif";

  //static string for the OPENSHA website
  private final static String OPENSHA_WEBSITE="http://www.OpenSHA.org";

  JSplitPane topSplitPane = new JSplitPane();
  JButton clearButton = new JButton();
  JPanel buttonPanel = new JPanel();
  JButton addButton = new JButton();
  JSplitPane chartSplit = new JSplitPane();
  JPanel panel = new JPanel();
  GridBagLayout gridBagLayout9 = new GridBagLayout();
  JPanel paramsPanel = new JPanel();

  GridBagLayout gridBagLayout15 = new GridBagLayout();
  JPanel imrPanel = new JPanel();
  GridBagLayout gridBagLayout10 = new GridBagLayout();
  BorderLayout borderLayout1 = new BorderLayout();
  private JButton peelOffButton = new JButton();
  private JLabel imgLabel = new JLabel(new ImageIcon(ImageUtils.loadImage(this.POWERED_BY_IMAGE)));
  private FlowLayout flowLayout1 = new FlowLayout();
  private GridBagLayout gridBagLayout14 = new GridBagLayout();


  //Get command-line parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
        (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public HazardDataSetPlotter() {
    data.setFunctions(functions);
    // for Y-log, convert 0 values in Y axis to this small value
    data.setConvertZeroToMin(true,Y_MIN_VAL);
  }
  //Initialize the applet
  public void init() {
    try {



      // initialize the GUI components
      jbInit();

      // initialize the various GUI beans
      initSiteGuiBean();
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

    this.setSize(new Dimension(1060, 670));
    this.getContentPane().setLayout(borderLayout1);


    jPanel1.setLayout(gridBagLayout10);

    //creating the Object the GraphPaenl class
    graphPanel = new GraphPanel(this);

    jPanel1.setBackground(Color.white);
    jPanel1.setBorder(border4);
    jPanel1.setMinimumSize(new Dimension(959, 600));
    jPanel1.setPreferredSize(new Dimension(959, 600));

    //loading the OpenSHA Logo

    topSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

    clearButton.setText("Clear Plot");
    clearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clearButton_actionPerformed(e);
      }
    });

    buttonPanel.setAlignmentX((float) 0.0);
    buttonPanel.setAlignmentY((float) 0.0);
    buttonPanel.setMinimumSize(new Dimension(568, 20));
    buttonPanel.setLayout(flowLayout1);


    addButton.setText("Add Graph");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });


    panel.setLayout(gridBagLayout9);
    panel.setBackground(Color.white);
    panel.setBorder(border5);
    panel.setMinimumSize(new Dimension(0, 0));


    imrPanel.setLayout(gridBagLayout15);
    imrPanel.setBackground(Color.white);
    chartSplit.setLeftComponent(panel);
    chartSplit.setRightComponent(paramsPanel);



    peelOffButton.setText("Peel Off");
    peelOffButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        peelOffButton_actionPerformed(e);
      }
    });


    imgLabel.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        imgLabel_mouseClicked(e);
      }
    });
    paramsPanel.setLayout(gridBagLayout14);
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(topSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 4, 5, 6), 243, 231));

    //object for the ButtonControl Panel
    buttonControlPanel = new ButtonControlPanel(this);

    buttonPanel.add(addButton, 0);
    buttonPanel.add(clearButton, 1);
    buttonPanel.add(peelOffButton, 2);
    buttonPanel.add(buttonControlPanel,3);
    buttonPanel.add(imgLabel, 4);
    topSplitPane.add(chartSplit, JSplitPane.TOP);
    topSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    chartSplit.add(panel, JSplitPane.LEFT);
    chartSplit.add(paramsPanel, JSplitPane.RIGHT);
    topSplitPane.setDividerLocation(600);
    chartSplit.setDividerLocation(600);

  }


  /**
   * Initialize the site gui bean
   */
  private void initSiteGuiBean() {

     // create the Site Gui Bean object
     siteGuiBean = new HazardDataSiteSelectionGuiBean();
     // show the sitebean in JPanel
     paramsPanel.add(siteGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
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
    return "Hazard Data Set Plotter Applet";
  }


  //Main method
  public static void main(String[] args) {
    HazardDataSetPlotter applet = new HazardDataSetPlotter();
    applet.isStandalone = true;
    JFrame frame = new JFrame();
    //EXIT_ON_CLOSE == 3
    frame.setDefaultCloseOperation(3);
    frame.setTitle(applet.getAppletInfo());
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
    String osName = System.getProperty("os.name");
    try {
      if(osName.startsWith("Mac OS"))
        UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
      else
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
    }
  }



  /**
   *  Adds a feature to the GraphPanel attribute of the EqkForecastApplet object
   */
  private void addGraphPanel() {

      // Starting
      String S = C + ": addGraphPanel(): ";
      graphPanel.drawGraphPanel(functions,data,xLog,yLog,customAxis,null,buttonControlPanel);
      togglePlot();
      graphPanel.setDefaultSeriesColor();
   }


   //checks if the user has plot the data window or plot window
   public void togglePlot(){
     panel.removeAll();
     graphPanel.togglePlot(buttonControlPanel);
     panel.add(graphPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
     panel.validate();
     panel.repaint();
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
      String S = C + ": addButton(): ";
      if ( D ) System.out.println( S + "Starting" );


      DiscretizedFuncAPI function =null;
      try{
        //getting the function from the site for the selected site.
        function = siteGuiBean.getChoosenFunction();
      }catch(RuntimeException e){
        JOptionPane.showMessageDialog(this,e.getMessage(),"Incorrect Parameter Input",JOptionPane.ERROR_MESSAGE);
        return;
      }

      if ( D ) System.out.println( S + "New Function info = " + function.getInfo() );
      data.setXLog(xLog);
      data.setYLog(yLog);
      //functions.setYAxisName( attenRel.getGraphIMYAxisLabel() );
      //functions.setXAxisName( attenRel.getGraphXAxisLabel() );
      if( !functions.contains( function ) )
        functions.add(function);
      addGraphPanel();
      if ( D ) System.out.println( S + "Ending" );

    }

    /**
     * to draw the graph
     */
    private void drawGraph() {
      // you can show warning messages now
     // set the log values
     data.setXLog(xLog);
     data.setYLog(yLog);

     addGraphPanel();
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
  private void clearPlot(boolean clearFunctions) {

    if ( D )
      System.out.println( "Clearing plot area" );

    int loc = this.chartSplit.getDividerLocation();
    int newLoc = loc;
    graphPanel.removeChartAndMetadata();
    panel.removeAll();
    if( clearFunctions) {
      functions.clear();
    }
    customAxis = false;
    chartSplit.setDividerLocation( newLoc );
  }

  /**
   * sets the range for X and Y axis
   * @param xMin : minimum value for X-axis
   * @param xMax : maximum value for X-axis
   * @param yMin : minimum value for Y-axis
   * @param yMax : maximum value for Y-axis
   *
   */
  public void setAxisRange(double xMin,double xMax, double yMin, double yMax) {
    minXValue=xMin;
    maxXValue=xMax;
    minYValue=yMin;
    maxYValue=yMax;
    this.customAxis=true;
    drawGraph();

  }

  /**
   * set the auto range for the axis. This function is called
   * from the AxisLimitControlPanel
   */
  public void setAutoRange() {
    this.customAxis=false;
    drawGraph();
  }



  void imgLabel_mouseClicked(MouseEvent e) {
    try{
      this.getAppletContext().showDocument(new URL(OPENSHA_WEBSITE), "new_peer_win");
    }catch(java.net.MalformedURLException ee){
      JOptionPane.showMessageDialog(this,new String("No Internet Connection Available"),
                                    "Error Connecting to Internet",JOptionPane.OK_OPTION);
    }
  }

  /**
   *
   * @returns the Range for the X-Axis
   */
  public Range getX_AxisRange(){
    return graphPanel.getX_AxisRange();
  }

  /**
   *
   * @returns the Range for the Y-Axis
   */
  public Range getY_AxisRange(){
    return graphPanel.getY_AxisRange();
  }




  /**
   * tells the application if the xLog is selected
   * @param xLog : boolean
   */
  public void setX_Log(boolean xLog){
    this.xLog = xLog;
    data.setXLog(xLog);
    drawGraph();
  }

  /**
   * tells the application if the yLog is selected
   * @param yLog : boolean
   */
  public void setY_Log(boolean yLog){
    this.yLog = yLog;
    data.setYLog(yLog);
    drawGraph();
  }


  /**
   *
   * @returns the boolean: Log for X-Axis Selected
   */
  public boolean getXLog(){
    return xLog;
  }

  /**
   *
   * @returns the boolean: Log for Y-Axis Selected
   */
  public boolean getYLog(){
    return yLog;
  }

  /**
   *
   * @returns boolean: Checks if Custom Axis is selected
   */
  public boolean isCustomAxis(){
    return customAxis;
  }

  /**
   *
   * @returns the Min X-Axis Range Value, if custom Axis is choosen
   */
  public double getMinX(){
    return minXValue;
  }

  /**
   *
   * @returns the Max X-Axis Range Value, if custom axis is choosen
   */
  public double getMaxX(){
    return maxXValue;
  }

  /**
   *
   * @returns the Min Y-Axis Range Value, if custom axis is choosen
   */
  public double getMinY(){
    return minYValue;
  }

  /**
   *
   * @returns the Max Y-Axis Range Value, if custom axis is choosen
   */
  public double getMaxY(){
    return maxYValue;
  }

  /**
   *
   * @returns the instance to the JPanel showing the JFreechart adn metadata
   */
  public GraphPanel getGraphPanel(){
    return graphPanel;
  }

  /**
   *
   * @returns the String containing the values selected for different parameters
   */
  /*public String getParametersInfo(){
    String systemSpecificLineSeparator = SystemPropertiesUtils.getSystemLineSeparator();
    return "IMR Param List:" +systemSpecificLineSeparator+
           "---------------"+systemSpecificLineSeparator+
        this.imrGuiBean.getVisibleParametersCloned().getParameterListMetadataString()+systemSpecificLineSeparator+systemSpecificLineSeparator+
        "Site Param List: "+systemSpecificLineSeparator+
        "----------------"+systemSpecificLineSeparator+
        siteGuiBean.getParameterListEditor().getVisibleParametersCloned().getParameterListMetadataString()+systemSpecificLineSeparator+
        systemSpecificLineSeparator+"IMT Param List: "+systemSpecificLineSeparator+
        "---------------"+systemSpecificLineSeparator+
        imtGuiBean.getVisibleParametersCloned().getParameterListMetadataString()+systemSpecificLineSeparator+
        systemSpecificLineSeparator+"Forecast Param List: "+systemSpecificLineSeparator+
        "--------------------"+systemSpecificLineSeparator+
        erfGuiBean.getParameterList().getParameterListMetadataString()+systemSpecificLineSeparator+
        systemSpecificLineSeparator+"TimeSpan Param List: "+systemSpecificLineSeparator+
        "--------------------"+systemSpecificLineSeparator+
        timeSpanGuiBean.getVisibleParametersCloned().getParameterListMetadataString()+systemSpecificLineSeparator;
  }*/

  /**
   *
   * @returns the DiscretizedFuncList for all the data curves
   */
  public DiscretizedFuncList getCurveFunctionList(){
    return functions;
  }


  /**
   *
   * @returns the DiscretizedFunctionXYDataSet to the data
   */
  public DiscretizedFunctionXYDataSet getXY_DataSet(){
    return data;
  }


  /**
   * Actual method implementation of the "Peel-Off"
   * This function peels off the window from the current plot and shows in a new
   * window. The current plot just shows empty window.
   */
  private void peelOffCurves(){
    graphWindow = new GraphWindow(this);
    clearPlot(true);
    graphWindow.show();
  }


  /**
   * Action method to "Peel-Off" the curves graph window in a seperate window.
   * This is called when the user presses the "Peel-Off" window.
   * @param e
   */
  void peelOffButton_actionPerformed(ActionEvent e) {
    peelOffCurves();
  }

}
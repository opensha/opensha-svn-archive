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

public class GroupTestApplet extends Applet {

  /**
   * Name of the class
   */
  protected final static String C = "GroupTestApplet";
  // for debug purpose
  protected final static boolean D = true;

  private Insets plotInsets = new Insets( 4, 10, 4, 4 );

  private boolean isStandalone = false;
  private JSplitPane chartSplit = new JSplitPane();
  private JPanel panel = new JPanel();
  private JPanel buttonPanel = new JPanel();
  private Border border1;
  private JSplitPane controlsSplit = new JSplitPane();
  private JSplitPane magDistSplit = new JSplitPane();


  //log flags declaration
  boolean xLog =false;
  boolean yLog =false;

  // default insets
  Insets defaultInsets = new Insets( 4, 4, 4, 4 );

  // height and width of the applet
  protected final static int W = 915;
  protected final static int H = 625;

  /**
   * FunctionList declared
   */
  DiscretizedFuncList totalProbFuncs = new DiscretizedFuncList();

  DiscretizedFunctionXYDataSet data = new DiscretizedFunctionXYDataSet();


  // make the GroupTestGUIBean instance
  GroupTestGuiBean groupTestBean;
  private JSplitPane imtSplitPane = new JSplitPane();
  private JPanel testCasesPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  private GridBagLayout gridBagLayout6 = new GridBagLayout();
  private GridBagLayout gridBagLayout7 = new GridBagLayout();
  JButton addButton = new JButton();
  JButton clearButton = new JButton();
  JButton toggleButton = new JButton();
  JCheckBox jCheckxlog = new JCheckBox();
  JCheckBox jCheckylog = new JCheckBox();
  private GridBagLayout gridBagLayout12 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();
  private JPanel imrPanel = new JPanel();
  private JPanel imtPanel = new JPanel();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private GridBagLayout gridBagLayout8 = new GridBagLayout();
  private JPanel sourcePanel = new JPanel();
  private JPanel magDistControlPanel = new JPanel();
  private GridBagLayout gridBagLayout10 = new GridBagLayout();
  private GridBagLayout gridBagLayout5 = new GridBagLayout();

  /**
   * adding scroll pane for showing data
   */
  JScrollPane dataScrollPane = new JScrollPane();

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
  private GridBagLayout gridBagLayout9 = new GridBagLayout();


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
      // make the GroupTestGuiBean
      groupTestBean = new GroupTestGuiBean(this);
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  //Component initialization
  private void jbInit() throws Exception {
    border1 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    this.setSize(new Dimension(925, 590));
    this.setLayout(null);
    chartSplit.setBounds(new Rectangle(18, 9, 727, 514));
    buttonPanel.setBorder(border1);
    buttonPanel.setBounds(new Rectangle(19, 536, 882, 44));
    buttonPanel.setLayout(gridBagLayout12);
    controlsSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    magDistSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    magDistSplit.setBounds(new Rectangle(753, 9, 167, 513));
    imtSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    testCasesPanel.setLayout(gridBagLayout1);
    addButton.setText("Add Graph");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });
    clearButton.setText("Clear Plot");
    toggleButton.setMaximumSize(new Dimension(83, 39));
    toggleButton.setToolTipText("");
    toggleButton.setText("Show Data");
    jCheckxlog.setText("X Log");
    jCheckylog.setText("Y Log");
    imrPanel.setLayout(gridBagLayout2);
    magDistControlPanel.setLayout(gridBagLayout10);
    sourcePanel.setLayout(gridBagLayout5);
    panel.setLayout(gridBagLayout9);
    buttonPanel.add(toggleButton,  new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 9, 10, 0), -9, -2));
    buttonPanel.add(clearButton,  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 9, 10, 0), 9, -2));
    this.add(chartSplit, null);
    chartSplit.add(panel, JSplitPane.TOP);
    chartSplit.add(controlsSplit, JSplitPane.BOTTOM);
    controlsSplit.add(imtSplitPane, JSplitPane.TOP);
    this.add(buttonPanel, null);
    buttonPanel.add(addButton,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 25, 10, 0), 3, -2));
    imtSplitPane.add(testCasesPanel, JSplitPane.TOP);
    imtSplitPane.add(imrPanel, JSplitPane.BOTTOM);
    controlsSplit.add(imtPanel, JSplitPane.BOTTOM);
    buttonPanel.add(jCheckxlog,  new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 20, 10, 0), 20, 2));
    buttonPanel.add(jCheckylog,  new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 0, 10, 337), 22, 2));
    this.add(magDistSplit, null);
    magDistSplit.add(sourcePanel, JSplitPane.TOP);
    magDistSplit.add(magDistControlPanel, JSplitPane.BOTTOM);
    chartSplit.setDividerLocation(525);
    controlsSplit.setDividerLocation(350);
    magDistSplit.setDividerLocation(500);

    imtSplitPane.setDividerLocation(90);

    updateChoosenTestCase();
    updateChoosenIMT();
    updateChoosenIMR();
    updateChoosenEqkSource();
    imrPanel.removeAll();
    imrPanel.add(groupTestBean.getImrEditor(), new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                 GridBagConstraints.CENTER,
                 GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    imrPanel.validate();
    imrPanel.repaint();

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
    testCasesPanel.removeAll();
    testCasesPanel.add(groupTestBean.getTestCasesEditor(),
                       new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                       GridBagConstraints.CENTER,
                       GridBagConstraints.BOTH, defaultInsets, 0, 0 ));

  }

  /**
   *  update the GUI with the IMT choosen
   */
  public void updateChoosenIMT() {
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
    // update the IMR panel
    imrPanel.removeAll();
    imrPanel.add(groupTestBean.getImrEditor(),
                 new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                 GridBagConstraints.CENTER,
                 GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    imrPanel.validate();
    imrPanel.repaint();
  }

  /**
   *  update the GUI with the choosen Eqk source
   */
  public void updateChoosenEqkSource() {
    // update the EqkSource panel
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

      HorizontalNumberAxis xAxis = new HorizontalNumberAxis( xAxisLabel );

      xAxis.setAutoRangeIncludesZero( false );
      xAxis.setCrosshairLockedOnData( false );
      xAxis.setCrosshairVisible(false);
      xAxis.setStandardTickUnits(units);


      VerticalNumberAxis yAxis = new VerticalNumberAxis( yAxisLabel );

      yAxis.setAutoRangeIncludesZero( false );
      yAxis.setCrosshairLockedOnData( false );
      yAxis.setCrosshairVisible( false);
      yAxis.setStandardTickUnits(units);

      int type = com.jrefinery.chart.StandardXYItemRenderer.LINES;


      LogXYItemRenderer renderer = new LogXYItemRenderer( type, new StandardXYToolTipGenerator() );
      //StandardXYItemRenderer renderer = new StandardXYItemRenderer( type, new StandardXYToolTipGenerator() );


      // build the plot

      org.scec.gui.PSHALogXYPlot plot = new org.scec.gui.PSHALogXYPlot(data, xAxis, yAxis, renderer);


      plot.setBackgroundAlpha( .8f );



      plot.setXYItemRenderer( renderer );


      JFreeChart chart = new JFreeChart(TITLE, JFreeChart.DEFAULT_TITLE_FONT, plot, true );

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

      //panel.removeAll();
      //panel.add( chartPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
        //                      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );


      if(D) System.out.println(this.totalProbFuncs.toString());
      if(D) System.out.println(S + "data:" + data);

      //validate();
      //repaint();
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

      DiscretizedFuncAPI function = this.groupTestBean.getChoosenFunction();

      // clear the function list
      this.totalProbFuncs.clear();

      // set the log values
      data.setXLog(xLog);
      data.setYLog(yLog);

      //add this function to the function list
      totalProbFuncs.add(function);

      addGraphPanel();

    }

}
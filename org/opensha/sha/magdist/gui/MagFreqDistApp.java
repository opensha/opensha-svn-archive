package org.opensha.sha.magdist.gui;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.ArrayList;


import org.opensha.sha.gui.infoTools.ButtonControlPanel;
import org.opensha.sha.gui.infoTools.GraphPanel;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.util.ImageUtils;
import org.opensha.sha.gui.infoTools.*;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.sha.param.editor.MagFreqDistParameterEditor;
import org.opensha.sha.magdist.SummedMagFreqDist;
import org.jfree.data.Range;

/**
 * <p>Title:MagFreqDistApp </p>
 *
 * <p>Description: Shows the MagFreqDist Editor and plot in a window.</p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MagFreqDistApp
    extends JFrame implements GraphPanelAPI,ButtonControlPanelAPI {

  private JSplitPane mainSplitPane = new JSplitPane();
  private JSplitPane plotSplitPane = new JSplitPane();
  private JTabbedPane plotTabPane = new JTabbedPane();
  private JPanel editorPanel = new JPanel();
  private JPanel buttonPanel = new JPanel();

  /**
   * Defines the panel and layout for the GUI elements
   */
  private JPanel incrRatePlotPanel = new JPanel();
  private JPanel momentRatePlotPanel = new JPanel();
  private JPanel cumRatePlotPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private FlowLayout flowLayout1 = new FlowLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JButton addButton = new JButton();


  private final boolean D = false;

  //instance for the ButtonControlPanel
  private ButtonControlPanel buttonControlPanel;

  //instance of the GraphPanel (window that shows all the plots)
  private GraphPanel incrRateGraphPanel,momentRateGraphPanel,cumRateGraphPanel;

  //instance of the GraphWindow to pop up when the user wants to "Peel-Off" curves;
  private GraphWindow graphWindow;

  //X and Y Axis  when plotting the Curves Name
  private String incrRateXAxisName = "Incremental-Rate", incrRateYAxisName = "Magnitude";
  //X and Y Axis  when plotting the Curves Name
  private String cumRateXAxisName = "Cumulative-Rate", cumRateYAxisName = "Magnitude";
  //X and Y Axis  when plotting the Curves Name
  private String momentRateXAxisName = "Moment-Rate", momentRateYAxisName = "Magnitude";

  private boolean isIncrRatePlot,isMomentRatePlot,isCumRatePlot;

  //log flags declaration
  private boolean xLog;
  private boolean yLog;

  /**
   * these four values save the custom axis scale specified by user
   */
  private double incrRateMinXValue,incrRateMaxXValue,incrRateMinYValue,incrRateMaxYValue;
  private double cumRateMinXValue,cumRateMaxXValue,cumRateMinYValue,cumRateMaxYValue;
  private double momentRateMinXValue,momentRateMaxXValue,momentRateMinYValue,momentRateMaxYValue;

  private boolean incrCustomAxis,momentCustomAxis,cumCustomAxis;

  private JButton peelOffButton = new JButton();
  private JMenuBar menuBar = new JMenuBar();
  private JMenu fileMenu = new JMenu();


  private JMenuItem fileExitMenu = new JMenuItem();
  private JMenuItem fileSaveMenu = new JMenuItem();
  private JMenuItem filePrintMenu = new JCheckBoxMenuItem();
  private JToolBar jToolBar = new JToolBar();

  private JButton closeButton = new JButton();
  private ImageIcon closeFileImage = new ImageIcon(ImageUtils.loadImage("closeFile.png"));

  private JButton printButton = new JButton();
  private ImageIcon printFileImage = new ImageIcon(ImageUtils.loadImage("printFile.jpg"));

  private  JButton saveButton = new JButton();
  ImageIcon saveFileImage = new ImageIcon(ImageUtils.loadImage("saveFile.jpg"));

  private final static String POWERED_BY_IMAGE = "PoweredBy.gif";

  private JLabel imgLabel = new JLabel(new ImageIcon(ImageUtils.loadImage(this.POWERED_BY_IMAGE)));
  private JButton clearButton = new JButton();


  //instance of the MagDist Editor
  private MagFreqDistParameterEditor magDistEditor;

  private JCheckBox jCheckSumDist = new JCheckBox();


  //list for storing all types of Mag Freq. dist. (incremental, cumulative and momentRate).
  private ArrayList incrRateFunctionList,cumRateFunctionList,momentRateFunctionList;
  //summed distribution
  private SummedMagFreqDist summedMagFreqDist;

  private String incrRatePlotTitle="",cumRatePlotTitle="",momentRatePlotTitle="";


  public MagFreqDistApp() {
    try {
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    getContentPane().setLayout(borderLayout1);



    addButton.setText("Add Dist");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });

    //object for the ButtonControl Panel
    buttonControlPanel = new ButtonControlPanel(this);

    peelOffButton.setText("Peel Off");
    peelOffButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        peelOffButton_actionPerformed(e);
      }
    });

    clearButton.setText("Clear Plot");
    clearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clearButton_actionPerformed(e);
      }
    });


    fileMenu.setText("File");
    fileExitMenu.setText("Exit");
    fileSaveMenu.setText("Save");
    filePrintMenu.setText("Print");

    fileExitMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fileExitMenu_actionPerformed(e);
      }
    });

    fileSaveMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fileSaveMenu_actionPerformed(e);
      }
    });

    filePrintMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        filePrintMenu_actionPerformed(e);
      }
    });

    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        closeButton_actionPerformed(actionEvent);
      }
    });
    printButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        printButton_actionPerformed(actionEvent);
      }
    });
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        saveButton_actionPerformed(actionEvent);
      }
    });


    menuBar.add(fileMenu);
    fileMenu.add(fileSaveMenu);
    fileMenu.add(filePrintMenu);
    fileMenu.add(fileExitMenu);

    setJMenuBar(menuBar);
    closeButton.setIcon(closeFileImage);
    closeButton.setToolTipText("Exit Application");
    Dimension d = closeButton.getSize();
    jToolBar.add(closeButton);
    printButton.setIcon(printFileImage);
    printButton.setToolTipText("Print Graph");
    printButton.setSize(d);
    jToolBar.add(printButton);
    saveButton.setIcon(saveFileImage);
    saveButton.setToolTipText("Save Graph as image");
    saveButton.setSize(d);
    jToolBar.add(saveButton);
    jToolBar.setFloatable(false);

    this.getContentPane().add(jToolBar, BorderLayout.NORTH);

    mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    editorPanel.setLayout(gridBagLayout1);
    buttonPanel.setLayout(flowLayout1);
    plotSplitPane.add(plotTabPane, JSplitPane.TOP);
    plotSplitPane.add(editorPanel, JSplitPane.BOTTOM);
    mainSplitPane.add(buttonPanel, JSplitPane.RIGHT);
    mainSplitPane.add(plotSplitPane, JSplitPane.LEFT);
    this.getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);
    plotSplitPane.setDividerLocation(400);
    mainSplitPane.setDividerLocation(530);
    incrRatePlotPanel.setLayout(borderLayout1);
    momentRatePlotPanel.setLayout(borderLayout1);
    cumRatePlotPanel.setLayout(borderLayout1);
    plotTabPane.add("Incremental-Rate", incrRatePlotPanel);
    plotTabPane.add("Cumulative-Rate", cumRatePlotPanel);
    plotTabPane.add("Moment-Rate", momentRatePlotPanel);
    plotTabPane.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        plotTabPane_stateChanged(e);
      }
    });
    jCheckSumDist.setBackground(Color.white);
    jCheckSumDist.setForeground(Color.red);
    jCheckSumDist.setText("Summed Dist");
    jCheckSumDist.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jCheckSumDist_actionPerformed(e);
      }
    });
    buttonPanel.add(jCheckSumDist,0);
    buttonPanel.add(addButton, 1);
    buttonPanel.add(clearButton, 2);
    buttonPanel.add(peelOffButton, 3);
    buttonPanel.add(buttonControlPanel, 4);
    buttonPanel.add(imgLabel, 5);
  }


  /**
   *
   */
  public void setMagDistEditor(MagFreqDistParameterEditor magDistEditor) {
    this.magDistEditor = magDistEditor;
    editorPanel.add(magDistEditor.getMagFreDistParameterEditor(),
                    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                           , GridBagConstraints.NORTH,
                                           GridBagConstraints.BOTH,
                                           new Insets(4, 4, 4, 4), 0, 0));
  }



  /**
   * This function is called when Summed distribution box is clicked
   *
   * @param e
   */
  void jCheckSumDist_actionPerformed(ActionEvent e) {

    int k=0;
    if(jCheckSumDist.isSelected()) {

     // if user wants a summed distribution
      double min = magDistEditor.getMin();
      double max = magDistEditor.getMax();
      int num = magDistEditor.getNum();

      // make the new object of summed distribution
      summedMagFreqDist = new  SummedMagFreqDist(min,max,num);

      // add all the existing distributions to the summed distribution
      int size = incrRateFunctionList.size();

      try {
      for(int i=0; i < size; ++i)
        summedMagFreqDist.addIncrementalMagFreqDist((IncrementalMagFreqDist)incrRateFunctionList.get(i));
      }catch(Exception ex) {
         JOptionPane.showMessageDialog(this,
                                       "min, max, and num must be the same to sum the distributions"
                                       );
         jCheckSumDist.setSelected(false);
         return;
      }

      // now we will do work so that we can put summed distribuiton to top of functionlist
      insertSummedDistribution();

    }
    // if summed distribution needs to be removed
   else {
     // remove the summed distribution and related moment rate and cumulative rate
     incrRateFunctionList.remove(0);
     cumRateFunctionList.remove(0);
     momentRateFunctionList.remove(0);
   }
    addGraphPanel();

  }


  /**
  *  Adds a feature to the GraphPanel attribute of the EqkForecastApplet object
  */
 private void addGraphPanel() {

     // Starting
     String S = ": addGraphPanel(): ";

     incrRateGraphPanel.drawGraphPanel(incrRateXAxisName,incrRateYAxisName,
                                       incrRateFunctionList,xLog,yLog,incrCustomAxis,
                                       incrRatePlotTitle,buttonControlPanel);
     incrRateGraphPanel.drawGraphPanel(cumRateXAxisName,cumRateYAxisName,
                                       cumRateFunctionList,xLog,yLog,cumCustomAxis,
                                       cumRatePlotTitle,buttonControlPanel);
     incrRateGraphPanel.drawGraphPanel(momentRateXAxisName,momentRateYAxisName,
                                       momentRateFunctionList,xLog,yLog,momentCustomAxis,
                                       momentRatePlotTitle,buttonControlPanel);
     togglePlot();
  }

  //checks if the user has plot the data window or plot window
  public void togglePlot(){
    incrRatePlotPanel.removeAll();
    cumRatePlotPanel.removeAll();
    momentRatePlotPanel.removeAll();
    incrRateGraphPanel.togglePlot(buttonControlPanel);
    cumRateGraphPanel.togglePlot(buttonControlPanel);
    momentRateGraphPanel.togglePlot(buttonControlPanel);
    incrRatePlotPanel.add(incrRateGraphPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
           , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
    incrRatePlotPanel.validate();
    incrRatePlotPanel.repaint();

    cumRatePlotPanel.add(cumRateGraphPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
           , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
    cumRatePlotPanel.validate();
    cumRatePlotPanel.repaint();

    momentRatePlotPanel.add(momentRateGraphPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
           , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
    momentRatePlotPanel.validate();
    momentRatePlotPanel.repaint();
  }


  /**
   * private function to insert the summed distribtuion to function list
   * It first makes the clone of the original function list
   * Then clears the original function list and then adds summed distribtuion to
   * the top of the original function list and then adds other distributions
   *
   */
  private void insertSummedDistribution() {

    // add the summed distribution to the list
    incrRateFunctionList.add(summedMagFreqDist);
    cumRateFunctionList.add(summedMagFreqDist.getCumRateDist());
    momentRateFunctionList.add(summedMagFreqDist.getMomentRateDist());

  }

  /**
   *
   * @param e ChangeEvent
   */
  private void plotTabPane_stateChanged(ChangeEvent e){
    JTabbedPane pane = (JTabbedPane)e.getSource();
    int index = pane.getSelectedIndex();
    if(index == 0){
      isCumRatePlot = false;
      isIncrRatePlot = true;
      isMomentRatePlot = false;
    }
    else if(index ==1){
      isCumRatePlot = true;
      isIncrRatePlot = false;
      isMomentRatePlot = false;
    }
    else if(index ==2){
      isCumRatePlot = false;
      isIncrRatePlot = false;
      isMomentRatePlot = true;
    }
  }



  /**
   * this function is called when "Add Dist" button is clicked
   * @param e
   */
  void addButton_actionPerformed(ActionEvent e) {
     addButton();
  }



    /**
     *  This causes the model data to be calculated and a plot trace added to
     *  the current plot
     *
     * @param  e  The feature to be added to the Button_mouseClicked attribute
     */
    protected void addButton(){

        if ( D ) System.out.println("Starting" );

        try{
          this.magDistEditor.setMagDistFromParams();
          IncrementalMagFreqDist function= (IncrementalMagFreqDist)this.magDistEditor.getParameter().getValue();
          if(D) System.out.println(" after getting mag dist from editor");
          EvenlyDiscretizedFunc cumRate;
          EvenlyDiscretizedFunc moRate;

          // get the cumulative rate and moment rate distributions for this function
          cumRate=(EvenlyDiscretizedFunc)function.getCumRateDist();
          moRate=(EvenlyDiscretizedFunc)function.getMomentRateDist();


        if(!this.jCheckSumDist.isSelected()) {
          // add the functions to the functionlist
          incrRateFunctionList.add((EvenlyDiscretizedFunc)function);
          cumRateFunctionList.add(cumRate);
          momentRateFunctionList.add(moRate);
        } else { // if summed distribution is selected, add to summed distribution
             try {
               // add this distribution to summed distribution
               summedMagFreqDist.addIncrementalMagFreqDist(function);

               // previous sum is invalid in the function lists. so remove that
               incrRateFunctionList.remove(incrRateFunctionList.size()-1);
               cumRateFunctionList.remove(cumRateFunctionList.size()-1);
               momentRateFunctionList.remove(momentRateFunctionList.size()-1);

               // add the functions to the functionlist
               incrRateFunctionList.add((EvenlyDiscretizedFunc)function);
               cumRateFunctionList.add(cumRate);
               momentRateFunctionList.add(moRate);
               // this function will insert summed distribution at top of function list
               insertSummedDistribution();
             }catch(Exception ex) {
               JOptionPane.showMessageDialog(this,
                                     "min, max, and num must be the same to sum the distributions."+
                                     "\n To add this distribution first deselect the Summed Dist option"
                                     );
               return;
             }
          }


          // draw the graph
          addGraphPanel();


        // catch the error and display messages in case of input error
        }catch(NumberFormatException e){
          JOptionPane.showMessageDialog(this,new String("Enter a Valid Numerical Value"),"Invalid Data Entered",JOptionPane.ERROR_MESSAGE);
        }catch(NullPointerException e) {
          //JOptionPane.showMessageDialog(this,new String(e.getMessage()),"Data Not Entered",JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        }catch(Exception e) {
          JOptionPane.showMessageDialog(this,new String(e.getMessage()),"Invalid Data Entered",JOptionPane.ERROR_MESSAGE);
        }

       if ( D ) System.out.println("Ending" );

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

    int loc = mainSplitPane.getDividerLocation();
    int newLoc = loc;

    if( clearFunctions){
      incrRateGraphPanel.removeChartAndMetadata();
      cumRateGraphPanel.removeChartAndMetadata();
      momentRateGraphPanel.removeChartAndMetadata();
      //panel.removeAll();
      incrRateFunctionList.clear();
      cumRateFunctionList.clear();
      momentRateFunctionList.clear();
    }
    if(isCumRatePlot)
      cumCustomAxis = false;
    else if(isMomentRatePlot)
      this.momentCustomAxis = false;
    else if(isIncrRatePlot)
      this.incrCustomAxis = false;

    mainSplitPane.setDividerLocation( newLoc );
  }



  /**
   * File | Exit action performed.
   *
   * @param actionEvent ActionEvent
   */
  private void fileSaveMenu_actionPerformed(ActionEvent actionEvent) {
    try {
      save();
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this, e.getMessage(), "Save File Error",
                                    JOptionPane.OK_OPTION);
      return;
    }
  }

  /**
   * File | Exit action performed.
   *
   * @param actionEvent ActionEvent
   */
  private void filePrintMenu_actionPerformed(ActionEvent actionEvent) {
    print();
  }

  /**
   * Opens a file chooser and gives the user an opportunity to save the chart
   * in PNG format.
   *
   * @throws IOException if there is an I/O error.
   */
  public void save() throws IOException {
    if(isIncrRatePlot)
      incrRateGraphPanel.save();
    else if(isCumRatePlot)
      cumRateGraphPanel.save();
    else if(isMomentRatePlot)
      momentRateGraphPanel.save();
  }

  /**
   * Creates a print job for the chart.
   */
  public void print() {
    if(isIncrRatePlot)
      incrRateGraphPanel.print(this);
    else if(isCumRatePlot)
      cumRateGraphPanel.print(this);
    else if(isMomentRatePlot)
      momentRateGraphPanel.print(this);
  }


  /**
   * Actual method implementation of the "Peel-Off"
   * This function peels off the window from the current plot and shows in a new
   * window. The current plot just shows empty window.
   */
  private void peelOffCurves(){
    //graphWindow = new GraphWindow(this);
    graphWindow.setVisible(true);
  }


  /**
   * Action method to "Peel-Off" the curves graph window in a seperate window.
   * This is called when the user presses the "Peel-Off" window.
   * @param e
   */
  void peelOffButton_actionPerformed(ActionEvent e) {
    peelOffCurves();
  }


  /**
   * File | Exit action performed.
   *
   * @param actionEvent ActionEvent
   */
  private void fileExitMenu_actionPerformed(ActionEvent actionEvent) {
    close();
  }

  /**
   *
   */
  private void close() {
    int option = JOptionPane.showConfirmDialog(this,
        "Do you really want to exit the application?\n" +
                                               "You will loose all unsaved data.",
                                               "Exit App",
                                               JOptionPane.OK_CANCEL_OPTION);
    if (option == JOptionPane.OK_OPTION)
      System.exit(0);
  }

  public void closeButton_actionPerformed(ActionEvent actionEvent) {
    close();
  }

  public void printButton_actionPerformed(ActionEvent actionEvent) {
    print();
  }

  public void saveButton_actionPerformed(ActionEvent actionEvent) {
    try {
      save();
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this, e.getMessage(), "Save File Error",
                                    JOptionPane.OK_OPTION);
      return;
    }
  }


  /**
   *
   * @returns the Min X-Axis Range Value, if custom Axis is choosen
   */
  public double getMinX() {
    if(isIncrRatePlot)
      return incrRateMinXValue;
    else if(isCumRatePlot)
      return cumRateMinXValue;
    else
      return momentRateMinXValue;
  }

  /**
   *
   * @returns the Max X-Axis Range Value, if custom axis is choosen
   */
  public double getMaxX() {
    if(isIncrRatePlot)
      return incrRateMaxXValue;
    else if(isCumRatePlot)
      return cumRateMaxXValue;
    else
      return momentRateMaxXValue;
  }

  /**
   *
   * @returns the Min Y-Axis Range Value, if custom axis is choosen
   */
  public double getMinY() {
    if(isIncrRatePlot)
      return incrRateMinYValue;
    else if(isCumRatePlot)
      return cumRateMinYValue;
    else
      return momentRateMinYValue;
  }

  /**
   *
   * @returns the Max X-Axis Range Value, if custom axis is choosen
   */
  public double getMaxY() {
    if(isIncrRatePlot)
      return incrRateMaxYValue;
    else if(isCumRatePlot)
      return cumRateMaxYValue;
    else
      return momentRateMaxYValue;
  }


  public static void main(String[] args) {
    MagFreqDistApp magfreqdistapp = new MagFreqDistApp();
  }



  public void setAxisRange(double xMin, double xMax, double yMin, double yMax) {
    if(isIncrRatePlot){
      incrRateMinXValue = xMin;
      incrRateMaxXValue = xMax;
      incrRateMinYValue = yMin;
      incrRateMaxYValue = yMax;
      this.incrCustomAxis=true;
    }
    else if(isCumRatePlot){
      cumRateMinXValue = xMin;
      cumRateMaxXValue = xMax;
      cumRateMinYValue = yMin;
      cumRateMaxYValue = yMax;
      this.cumCustomAxis = true;
    }
    else{
      momentRateMinXValue = xMin;
      momentRateMaxXValue = xMax;
      momentRateMinYValue = yMin;
      momentRateMaxYValue = yMax;
      this.momentCustomAxis = true;
    }
    addGraphPanel();
  }

  public void setAutoRange() {
    if(isIncrRatePlot)
      incrCustomAxis=false;
    if(isCumRatePlot)
      cumCustomAxis=false;
    if(isMomentRatePlot)
      momentCustomAxis=false;
    addGraphPanel();
  }


  public void setX_Log(boolean xLog) {
    this.xLog = xLog;
    this.addGraphPanel();
  }

  public void setY_Log(boolean yLog) {
    this.yLog = yLog;
    this.addGraphPanel();
  }

  public Range getX_AxisRange() {
    if(isIncrRatePlot)
      return incrRateGraphPanel.getX_AxisRange();
    else if(isCumRatePlot)
      return cumRateGraphPanel.getX_AxisRange();
    else
      return momentRateGraphPanel.getX_AxisRange();
  }

  public Range getY_AxisRange() {
    if(isIncrRatePlot)
      return incrRateGraphPanel.getY_AxisRange();
    else if(isCumRatePlot)
      return cumRateGraphPanel.getY_AxisRange();
    else
      return momentRateGraphPanel.getY_AxisRange();
  }

  public ArrayList getPlottingFeatures() {
    if(isIncrRatePlot)
      return incrRateGraphPanel.getCurvePlottingCharacterstic();
    else if(isCumRatePlot)
      return cumRateGraphPanel.getCurvePlottingCharacterstic();
    else
      return momentRateGraphPanel.getCurvePlottingCharacterstic();
  }

  public void plotGraphUsingPlotPreferences() {
    ArrayList plotPrefs;
    if(isIncrRatePlot){
      plotPrefs = incrRateGraphPanel.getCurvePlottingCharacterstic();
      cumRateGraphPanel.setCurvePlottingCharacterstic(plotPrefs);
      momentRateGraphPanel.setCurvePlottingCharacterstic(plotPrefs);
    }
    else if(isCumRatePlot){
      plotPrefs = cumRateGraphPanel.getCurvePlottingCharacterstic();
      incrRateGraphPanel.setCurvePlottingCharacterstic(plotPrefs);
      momentRateGraphPanel.setCurvePlottingCharacterstic(plotPrefs);
    }
    else{
      plotPrefs = momentRateGraphPanel.getCurvePlottingCharacterstic();
      incrRateGraphPanel.setCurvePlottingCharacterstic(plotPrefs);
      cumRateGraphPanel.setCurvePlottingCharacterstic(plotPrefs);
    }
    addGraphPanel();
  }

  public String getXAxisLabel() {
    if(isIncrRatePlot)
      return incrRateXAxisName;
    else if(isCumRatePlot)
      return cumRateXAxisName;
    else
      return momentRateXAxisName;
  }

  public String getYAxisLabel() {
    if(isIncrRatePlot)
      return incrRateYAxisName;
    else if(isCumRatePlot)
      return cumRateYAxisName;
    else
      return momentRateYAxisName;
  }

  public String getPlotLabel() {
    if(isIncrRatePlot)
      return incrRatePlotTitle;
    else if(isCumRatePlot)
      return cumRatePlotTitle;
    else
      return momentRatePlotTitle;

  }


  public void setXAxisLabel(String xAxisLabel) {
    if(isIncrRatePlot)
      incrRateXAxisName = xAxisLabel;
    else if(isCumRatePlot)
      cumRateXAxisName = xAxisLabel;
    else
      momentRateXAxisName = xAxisLabel;
  }

  public void setYAxisLabel(String yAxisLabel) {
    if (isIncrRatePlot)
      incrRateYAxisName = yAxisLabel;
    else if (isCumRatePlot)
      cumRateYAxisName = yAxisLabel;
    else
      momentRateYAxisName = yAxisLabel;

  }

  public void setPlotLabel(String plotTitle) {
    if(isIncrRatePlot)
      incrRatePlotTitle = plotTitle;
    else if(isCumRatePlot)
      cumRatePlotTitle = plotTitle;
    else
      momentRatePlotTitle = plotTitle;
  }
}

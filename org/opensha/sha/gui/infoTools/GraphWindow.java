package org.opensha.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;

import org.jfree.data.Range;
import org.opensha.sha.gui.infoTools.*;
import org.opensha.util.ImageUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.io.IOException;

/**
 * <p>Title: GraphWindow</p>
 * <p>Description: This window pops up when the user wants to see the plot curves
 * in a separate window ( peel the plot from the original window )</p>
 * @author : Nitin Gupta
 * @version 1.0
 */

public class GraphWindow
    extends JFrame implements ButtonControlPanelAPI, GraphPanelAPI {

  private final static int W = 670;
  private final static int H = 700;
  private JSplitPane chartSplitPane = new JSplitPane();
  private JPanel chartPane = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel buttonPanel = new JPanel();
  private FlowLayout flowLayout1 = new FlowLayout();

  //boolean parameters for the Axis to check for log
  private boolean xLog = false;
  private boolean yLog = false;

  //boolean parameter to check for range of the axis
  private boolean customAxis = false;

  private static int windowNumber = 1;

  private final static String TITLE = "Plot Window - ";

  private String plotTitle = "Hazard Curves";

  private double minXValue, maxXValue, minYValue, maxYValue;

  //instance for the ButtonControlPanel
  private ButtonControlPanel buttonControlPanel;

  //instance of the application implementing the Graph Window class
  private GraphWindowAPI application;

  //instance of the GraphPanel class
  private GraphPanel graphPanel;

  /**
   * List of ArbitrarilyDiscretized functions and Weighted funstions
   */
  private ArrayList functionList;

  //X and Y Axis  when plotting tha Curves Name
  private String xAxisName;
  private String yAxisName;

  /**
   * for Y-log, 0 values will be converted to this small value
   */
  private double Y_MIN_VAL = 1e-16;

  JMenuBar menuBar = new JMenuBar();
  JMenu fileMenu = new JMenu();

  JMenuItem fileExitMenu = new JMenuItem();
  JMenuItem fileSaveMenu = new JMenuItem();
  JMenuItem filePrintMenu = new JCheckBoxMenuItem();
  JToolBar jToolBar = new JToolBar();

  JButton closeButton = new JButton();
  ImageIcon closeFileImage = new ImageIcon(ImageUtils.loadImage("closeFile.png"));

  JButton printButton = new JButton();
  ImageIcon printFileImage = new ImageIcon(ImageUtils.loadImage("printFile.jpg"));

  JButton saveButton = new JButton();
  ImageIcon saveFileImage = new ImageIcon(ImageUtils.loadImage("saveFile.jpg"));

  /**
   *
   * @param api : Instance of this application using this object.
   */
  public GraphWindow(GraphWindowAPI api) {
    application = api;
    graphPanel = new GraphPanel(this);

    //creating the plotting pref array list from the application
    //becuase it needs to be similar to what application has.
    ArrayList plotCharacterstics = new ArrayList();
    ArrayList applicationPlottingPrefList = api.getPlottingFeatures();
    int size = applicationPlottingPrefList.size();
    for (int i = 0; i < size; ++i) {
      PlotCurveCharacterstics curvePlotPref = (PlotCurveCharacterstics)
          applicationPlottingPrefList.get(i);
      plotCharacterstics.add(new PlotCurveCharacterstics(curvePlotPref.
          getCurveName(), curvePlotPref.getCurveType(),
          curvePlotPref.getCurveColor(), curvePlotPref.getCurveWidth(),
          curvePlotPref.getNumContinuousCurvesWithSameCharacterstics()));
    }
    graphPanel.setCurvePlottingCharacterstic(plotCharacterstics);
    //adding the list of Functions to the Peel-Off window
    functionList = new ArrayList();
    ArrayList applicationCurveList = api.getCurveFunctionList();

    size = applicationCurveList.size();
    for (int i = 0; i < size; ++i)
      functionList.add(applicationCurveList.get(i));

    xAxisName = api.getXAxisLabel();
    yAxisName = api.getYAxisLabel();
    try {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    //increasing the window number corresponding to the new window.
    ++windowNumber;
    /**
     * Recreating the chart with all the default settings that existed in the main application.
     */
    xLog = api.getXLog();
    yLog = api.getYLog();
    customAxis = api.isCustomAxis();
    if (customAxis)
      buttonControlPanel.setAxisRange(api.getMinX(), api.getMaxX(), api.getMinY(),
                                      api.getMaxY());
    if (xLog)
      buttonControlPanel.setXLog(xLog);
    if (yLog)
      buttonControlPanel.setYLog(yLog);
    if (!xLog && !yLog)
      drawGraph();
  }

  //function to create the GUI component.
  private void jbInit() throws Exception {
    this.setSize(W, H);
    this.getContentPane().setLayout(borderLayout1);
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
    closeButton.setToolTipText("Close Window");
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

    chartSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    chartPane.setLayout(gridBagLayout1);
    buttonPanel.setLayout(flowLayout1);
    this.getContentPane().add(chartSplitPane, BorderLayout.CENTER);
    chartSplitPane.add(chartPane, JSplitPane.TOP);
    chartSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    chartSplitPane.setDividerLocation(580);
    //object for the ButtonControl Panel
    buttonControlPanel = new ButtonControlPanel(this);
    buttonPanel.add(buttonControlPanel, null);
    togglePlot();
    this.setTitle(TITLE + windowNumber);
  }


  /**
   * File | Exit action performed.
   *
   * @param actionEvent ActionEvent
   */
  private void fileExitMenu_actionPerformed(ActionEvent actionEvent) {
    this.dispose();
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

  public void closeButton_actionPerformed(ActionEvent actionEvent) {
    this.dispose();
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
   * Opens a file chooser and gives the user an opportunity to save the chart
   * in PNG format.
   *
   * @throws IOException if there is an I/O error.
   */
  public void save() throws IOException {
    graphPanel.save();
  }

  /**
   * Creates a print job for the chart.
   */
  public void print() {
    graphPanel.print(this);
  }

  /**
   *
   * @returns the Range for the X-Axis
   */
  public Range getX_AxisRange() {
    return graphPanel.getX_AxisRange();
  }

  /**
   *
   * @returns the Range for the Y-Axis
   */
  public Range getY_AxisRange() {
    return graphPanel.getY_AxisRange();
  }

  /**
   * tells the application if the xLog is selected
   * @param xLog : boolean
   */
  public void setX_Log(boolean xLog) {
    this.xLog = xLog;
    drawGraph();
  }

  /**
   * tells the application if the yLog is selected
   * @param yLog : boolean
   */
  public void setY_Log(boolean yLog) {
    this.yLog = yLog;
    drawGraph();
  }

  /**
   * sets the range for X and Y axis
   * @param xMin : minimum value for X-axis
   * @param xMax : maximum value for X-axis
   * @param yMin : minimum value for Y-axis
   * @param yMax : maximum value for Y-axis
   *
   */
  public void setAxisRange(double xMin, double xMax, double yMin, double yMax) {
    minXValue = xMin;
    maxXValue = xMax;
    minYValue = yMin;
    maxYValue = yMax;
    customAxis = true;
    drawGraph();
  }

  /**
   * set the auto range for the axis. This function is called
   * from the AxisLimitControlPanel
   */
  public void setAutoRange() {
    customAxis = false;
    drawGraph();
  }

  /**
   * to draw the graph
   */
  private void drawGraph() {
    graphPanel.drawGraphPanel(xAxisName, yAxisName, functionList, xLog, yLog,
                              customAxis, plotTitle, buttonControlPanel);
    togglePlot();
  }

  /**
   * plots the curves with defined color,line width and shape.
   * @param plotFeatures
   */
  public void plotGraphUsingPlotPreferences() {
    drawGraph();
  }

  //checks if the user has plot the data window or plot window
  public void togglePlot() {
    chartPane.removeAll();
    graphPanel.togglePlot(buttonControlPanel);
    chartPane.add(graphPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 0), 0, 0));
    chartPane.validate();
    chartPane.repaint();
  }

  /**
   *
   * @returns the Min X-Axis Range Value, if custom Axis is choosen
   */
  public double getMinX() {
    return minXValue;
  }

  /**
   *
   * @returns the Max X-Axis Range Value, if custom axis is choosen
   */
  public double getMaxX() {
    return maxXValue;
  }

  /**
   *
   * @returns the Min Y-Axis Range Value, if custom axis is choosen
   */
  public double getMinY() {
    return minYValue;
  }

  /**
   *
   * @returns the Max Y-Axis Range Value, if custom axis is choosen
   */
  public double getMaxY() {
    return maxYValue;
  }

  /**
   *
   * @returns the plotting feature like width, color and shape type of each
   * curve in list.
   */
  public ArrayList getPlottingFeatures() {
    return graphPanel.getCurvePlottingCharacterstic();
  }

  /**
   *
   * @returns the X Axis Label
   */
  public String getXAxisLabel() {
    return xAxisName;
  }

  /**
   *
   * @returns Y Axis Label
   */
  public String getYAxisLabel() {
    return yAxisName;
  }

  /**
   *
   * @returns plot Title
   */
  public String getPlotLabel() {
    return TITLE;
  }

  /**
   *
   * sets  X Axis Label
   */
  public void setXAxisLabel(String xAxisLabel) {
    xAxisName = xAxisLabel;
  }

  /**
   *
   * sets Y Axis Label
   */
  public void setYAxisLabel(String yAxisLabel) {
    yAxisName = yAxisLabel;
  }

  /**
   *
   * sets plot Title
   */
  public void setPlotLabel(String plotTitle) {
    this.plotTitle = plotTitle;
  }
}

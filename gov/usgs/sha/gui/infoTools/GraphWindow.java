package gov.usgs.sha.gui.infoTools;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.scec.util.ImageUtils;
import java.util.ArrayList;
import org.jfree.data.Range;
import org.scec.sha.gui.infoTools.ButtonControlPanel;
import org.scec.sha.gui.infoTools.GraphPanel;
import org.scec.sha.gui.infoTools.GraphPanelAPI;
import org.scec.sha.gui.infoTools.ButtonControlPanelAPI;
import org.scec.data.function.DiscretizedFuncList;
import java.io.IOException;

/**
 * <p>Title: GraphWindow</p>
 *
 * <p>Description: Thi class allows user to visualise the computed data as graphs.</p>
 * @author Ned Field,Nitin Gupta,E.V.Leyendecker
 * @version 1.0
 */
public class GraphWindow
    extends JFrame implements ButtonControlPanelAPI,GraphPanelAPI{

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


  private final static int W = 650;
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


  private String plotTitle = "Hazard Curves";

  private double minXValue, maxXValue, minYValue, maxYValue;

//instance for the ButtonControlPanel
  private ButtonControlPanel buttonControlPanel;


//instance of the GraphPanel class
  private GraphPanel graphPanel;


//X and Y Axis  when plotting tha Curves Name
  private String xAxisName;
  private String yAxisName;


  private JComboBox graphListCombo = new JComboBox();

  private ArrayList functionList;

  /**
   * Class constructor that shows the list of graphs that user can plot.
   * @param dataList ArrayList List of DiscretizedFunctionList
   */
  public GraphWindow(ArrayList dataList) {

    functionList = dataList;
    //adding list of graphs to the shown to the user.
    int size = dataList.size();
    for(int i=0;i<size;++i)
      graphListCombo.addItem(((DiscretizedFuncList)dataList.get(i)).getName());

    graphListCombo.setSelectedIndex(0);

    try {
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }

    graphPanel = new GraphPanel(this);
    drawGraph();
  }


  /**
   * Component initialization.
   *
   * @throws java.lang.Exception
   */
  private void jbInit() throws Exception {

    setSize(new Dimension(W, H));
    setTitle("Data Plot Window");
    this.getContentPane().setLayout(null);
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
    graphListCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent itemEvent) {
        graphListCombo_itemStateChanged(itemEvent);
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
    jToolBar.add(closeButton);
    printButton.setIcon(printFileImage);
    printButton.setToolTipText("Print Graph");
    jToolBar.add(printButton);
    saveButton.setIcon(saveFileImage);
    saveButton.setToolTipText("Save Graph as image");
    jToolBar.add(saveButton);
    this.getContentPane().add(jToolBar, BorderLayout.NORTH);
    this.setSize(W,H);
    this.getContentPane().setLayout(borderLayout1);
    chartSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    chartPane.setLayout(gridBagLayout1);

    buttonPanel.setLayout(flowLayout1);
    this.getContentPane().add(chartSplitPane, BorderLayout.CENTER);
    chartSplitPane.add(chartPane, JSplitPane.TOP);
    chartSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    chartSplitPane.setDividerLocation(600);
    //object for the ButtonControl Panel
    buttonControlPanel = new ButtonControlPanel(this);
    buttonPanel.add(buttonControlPanel,null);
    chartPane.add(graphListCombo, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(4, 4, 4, 4), 0, 0));
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
    try{
      save();
    }catch(IOException e){
      JOptionPane.showMessageDialog(this,e.getMessage(),"Save File Error",JOptionPane.OK_OPTION);
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
    ArrayList listOfFunctionsToPlot = new ArrayList();
    int totFunctions = functionList.size();

    //getting the list of the curves that we need to plot
    String selectedDataToPlot = (String)graphListCombo.getSelectedItem();
    for(int i=0;i<totFunctions;++i){
      DiscretizedFuncList funcList = (DiscretizedFuncList)functionList.get(i);
      if(selectedDataToPlot.equals(funcList.getName())){
        int numFunc = funcList.size();
        for(int j=0;j<numFunc;++j)
          listOfFunctionsToPlot.add(funcList.get(j));
        break;
      }
    }

    //sending the list of curves to be plotted
    graphPanel.drawGraphPanel(xAxisName, yAxisName, listOfFunctionsToPlot, xLog, yLog,
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

    if(graphPanel !=null)
      chartPane.remove(graphPanel);
    graphPanel.togglePlot(buttonControlPanel);
    chartPane.add(graphPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(2, 2, 2, 2), 0, 0));
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
    return plotTitle;
  }

  /**
   *
   * sets  X Axis Label
   */
  public void setXAxisLabel(String xAxisLabel) {
    xAxisName = xAxisLabel;
  }

  /**
   * Opens a file chooser and gives the user an opportunity to save the chart
   * in PNG format.
   *
   * @throws IOException if there is an I/O error.
   */
  public void save() throws IOException {
    graphPanel.getChartPanel().doSaveAs();
  }


  /**
   * Creates a print job for the chart.
   */
  public void print(){
    graphPanel.getChartPanel().createChartPrintJob();
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

  public void graphListCombo_itemStateChanged(ItemEvent itemEvent) {
    //creating the new instance of the plot
    graphPanel = new GraphPanel(this);
    drawGraph();
  }

  public void closeButton_actionPerformed(ActionEvent actionEvent) {
    this.dispose();
  }

  public void printButton_actionPerformed(ActionEvent actionEvent) {
    print();
  }

  public void saveButton_actionPerformed(ActionEvent actionEvent) {
    try{
      save();
    }catch(IOException e){
      JOptionPane.showMessageDialog(this,e.getMessage(),"Save File Error",JOptionPane.OK_OPTION);
      return;
    }
  }
}

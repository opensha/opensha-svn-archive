package javaDevelopers.vipin;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.BufferedReader;
import org.opensha.sha.surface.GriddedSurface;
import java.util.StringTokenizer;
import org.opensha.data.Location;
import org.opensha.gui.plot.jfreechart.GriddedSurfaceXYDataSet;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import org.opensha.data.LocationList;
import org.opensha.exceptions.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * <p>Title: Show all the ruptures as a animation using JFreechart</p>
 * <p>Description: Read the fault sections and ruptures from files and display
 * them using JFreechart. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class RuptureAnimationGUI extends JFrame implements  ActionListener, Runnable {
  private JPanel displayPanel = new JPanel();
  private JButton showRupsButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private final static String FAULT_SECTION_FILE_NAME = PrepareTreeStructure.FAULT_SECTIONS_OUT_FILENAME;
  private final static String X_AXIS_LABEL = "Longitude (deg.)";
  private final static String Y_AXIS_LABEL = "Latitude (deg.)";
  private final static String TITLE = "Fault Sections & Ruptures";
  // light blue color
  private final static Color lightBlue = new Color( 200, 200, 230 );
  private NumberAxis yAxis = new NumberAxis( Y_AXIS_LABEL );
  private NumberAxis xAxis = new NumberAxis( X_AXIS_LABEL );
  private final static int TIME_DELAY = 800;
   // build the plot
  private  XYPlot plot = new XYPlot(null, xAxis, yAxis, new StandardXYItemRenderer());
  private  int faultSectionCounter=0;
  private  int rupCount=0;
  private FileReader frRups;
  private BufferedReader brRups;
  private ChartPanel chartPanel;

  //static initializer for setting look & feel
  static {
    String osName = System.getProperty("os.name");
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
    }
  }

  public RuptureAnimationGUI() {
    try {
      jbInit();
      loadFaultSections(); // load the fault sections
      addGraphPanel(); // show the fault sections using JFreechart
      this.showRupsButton.addActionListener(this);
      frRups = new FileReader(PrepareTreeStructure.RUP_OUT_FILENAME);
      brRups = new BufferedReader(frRups); // buffered reader
      brRups.readLine(); // skip first line as it just contains number of ruptures
      pack();
      show();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * when user click on button to view the ruptures
   * @param event
   */
  public void actionPerformed(ActionEvent event) {
    Object source = event.getSource();
    if(source == this.showRupsButton) showRuptures();
  }

  /**
   * Show the ruptures animation. It makes a new thread to do the animation
   */
  private void showRuptures() {
    Thread animationThread = new Thread(this);
    animationThread.start();
  }

  /**
   * Thread runs this to create a animation  for ruptures
   */
  public void run() {
    try {
      LocationList locList=null;
      String line = brRups.readLine().trim();
      double lat, lon;
      while(line!=null) {
        line=line.trim();
        if(!line.equalsIgnoreCase("")) { // if line is not a blank line
          if(line.startsWith("#"))  { // this is new rupture name
            if(rupCount>0)  {
              System.out.println("Rupture#"+rupCount+","+locList.toString());
              addLocationListToPlot(locList, faultSectionCounter);
              addRendererForRupture(faultSectionCounter); // add renderer to ruptures
              this.addGraphPanel();
              Thread.sleep(TIME_DELAY);
            }
            locList = new LocationList();
            rupCount++;
          } else { // location on a rupture
            StringTokenizer tokenizer = new StringTokenizer(line,",");
            lat = Double.parseDouble(tokenizer.nextToken());
            lon = Double.parseDouble(tokenizer.nextToken());
            locList.addLocation(new Location(lat,lon,0.0));
          }
        }
        line=brRups.readLine();
      }
    }catch(Exception e) {
      e.printStackTrace();
    }
  }


   private void addRendererForRupture(int index) {
    StandardXYItemRenderer xyItemRenderer = new StandardXYItemRenderer();
    xyItemRenderer.setPaint(Color.black);
    xyItemRenderer.setStroke(new BasicStroke((float)2.0));
    if(index==0) plot.setRenderer(xyItemRenderer);
    else plot.setSecondaryRenderer(index-1, xyItemRenderer);
  }


   private void addGraphPanel() {
    JFreeChart chart = new JFreeChart(TITLE, JFreeChart.DEFAULT_TITLE_FONT, plot, false );
    chart.setBackgroundPaint( lightBlue );
    xAxis.setAutoRangeIncludesZero( false );
    yAxis.setAutoRangeIncludesZero( false );

    if(chartPanel!=null) // if chart panel already exists, just chnage jfreechart instance in it
      chartPanel.setChart(chart);
    else { // add chart panel for first time
      // Put into a panel
      chartPanel = new ChartPanel(chart, true, true, true, true, false);
      chartPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
          LOWERED));
      chartPanel.setMouseZoomable(true);
      chartPanel.setDisplayToolTips(true);
      chartPanel.setHorizontalAxisTrace(false);
      chartPanel.setVerticalAxisTrace(false);
      displayPanel.add(chartPanel,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 3, 0, 3), 0, 0));
    }

  }



  public static void main(String[] args) {
    RuptureAnimationGUI ruptureAnimationGUI = new RuptureAnimationGUI();
  }

  /**
   * Load the fault sections to be displayed in the window
   */
  private void  loadFaultSections() {
   try {
      LocationList locList=null;
      // read from fault sections file
      FileReader fr = new FileReader(FAULT_SECTION_FILE_NAME);
      BufferedReader br = new BufferedReader(fr);
      String line = br.readLine().trim();
      double lat, lon;
      int col=0;
      faultSectionCounter=-1;
      while(line!=null) {
        line=line.trim();
        if(!line.equalsIgnoreCase("")) { // if line is not a blank line
          if(line.startsWith("#"))  { // this is new fault section name
            col=0;
            if(faultSectionCounter>=0)  addLocationListToPlot(locList, faultSectionCounter);
            locList = new LocationList();
            faultSectionCounter++;

          } else { // location on a faulr section
            StringTokenizer tokenizer = new StringTokenizer(line,",");
            lat = Double.parseDouble(tokenizer.nextToken());
            lon = Double.parseDouble(tokenizer.nextToken());
            locList.addLocation(new Location(lat,lon,0.0));
          }
        }
        line=br.readLine();
      }
      // add the last fault section to the plot
      addLocationListToPlot(locList, faultSectionCounter++);
      br.close();
      fr.close();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Add fault section to plot
   *
   * @param locList
   * @param faultSectionCounter
   * @param row
   * @throws InvalidRangeException
   * @throws java.lang.ClassCastException
   * @throws java.lang.ArrayIndexOutOfBoundsException
   */
   private void addLocationListToPlot(LocationList locList,
                                     int index) throws
      InvalidRangeException, ClassCastException, ArrayIndexOutOfBoundsException {
    GriddedSurface griddedSurface = new GriddedSurface(1, locList.size());
    for (int i = 0; i < locList.size(); ++i)
      griddedSurface.set(0, i, locList.getLocationAt(i));
    GriddedSurfaceXYDataSet griddedDataSet = new GriddedSurfaceXYDataSet(griddedSurface);
    if(index==0) plot.setDataset(griddedDataSet);
    else plot.setSecondaryDataset(index-1, griddedDataSet);
  }

  /**
   * intialize the GUI components
   * @throws java.lang.Exception
   */
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(gridBagLayout2);
    displayPanel.setLayout(gridBagLayout1);
    showRupsButton.setText("Show Ruptures");
    this.getContentPane().add(displayPanel,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 3, 0, 3), 453, 308));
    this.getContentPane().add(showRupsButton,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 183, 3, 150), 15, 6));
  }
}

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

/**
 * <p>Title: Show all the ruptures as a animation using JFreechart</p>
 * <p>Description: Read the fault sections and ruptures from files and display
 * them using JFreechart. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class RuptureAnimationGUI extends JFrame {
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
   // build the plot
  private XYPlot plot = new XYPlot(null, xAxis, yAxis, new StandardXYItemRenderer());
  private int faultSectionCounter;


  public RuptureAnimationGUI() {
    try {
      jbInit();
      loadFaultSections(); // load the fault sections
      addGraphPanel(); // show the fault sections using JFreechart
      pack();
      show();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void addGraphPanel() {

    JFreeChart chart = new JFreeChart(TITLE, JFreeChart.DEFAULT_TITLE_FONT, plot, false );
    chart.setBackgroundPaint( lightBlue );
    xAxis.setAutoRangeIncludesZero( false );
    yAxis.setAutoRangeIncludesZero( false );
    // Put into a panel
    ChartPanel chartPanel = new ChartPanel(chart, true, true, true, true, false);
    chartPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );
    chartPanel.setMouseZoomable(true);
    chartPanel.setDisplayToolTips(true);
    chartPanel.setHorizontalAxisTrace(false);
    chartPanel.setVerticalAxisTrace(false);
    displayPanel.add(chartPanel,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 3, 0, 3), 0, 0));

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
      int col=0,
      faultSectionCounter=0;
      while(line!=null) {
        line=line.trim();
        if(!line.equalsIgnoreCase("")) { // if line is not a blank line
          if(line.startsWith("#"))  { // this is new fault section name
            col=0;
            if(faultSectionCounter>0)  addFaultSectionToPlot(locList, faultSectionCounter);
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
      addFaultSectionToPlot(locList, faultSectionCounter);
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
  private void addFaultSectionToPlot(LocationList locList,
                                     int faultSectionCounter) throws
      InvalidRangeException, ClassCastException, ArrayIndexOutOfBoundsException {
    GriddedSurface griddedSurface = new GriddedSurface(1, locList.size());
    for (int i = 0; i < locList.size(); ++i)
      griddedSurface.set(0, i, locList.getLocationAt(i));
    GriddedSurfaceXYDataSet griddedDataSet = new GriddedSurfaceXYDataSet(griddedSurface);
    if(faultSectionCounter==0)
      plot.setDataset(griddedDataSet);
    else plot.setSecondaryDataset(faultSectionCounter-1, griddedDataSet);
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
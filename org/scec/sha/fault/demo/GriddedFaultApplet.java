package org.scec.sha.fault.demo;

import org.scec.sha.fault.*;
import org.scec.sha.fault.*;
import org.scec.sha.surface.*;
import org.scec.util.*;

import com.jrefinery.chart.*;
import com.jrefinery.chart.tooltips.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import org.scec.gui.*;


import org.scec.gui.*;
import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.data.function.*;
import org.scec.gui.plot.jfreechart.*;
import org.scec.data.*;



/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class GriddedFaultApplet
    extends JApplet
    implements ItemListener, ChangeListener
{


    protected final static String C = "GriddedFaultApplet";
    protected final static boolean D = false;

    protected GriddedFaultFactory factory = null;

    boolean isStandalone = false;

    // Plot panel insets

    protected Insets plotInsets = new Insets( 4, 10, 4, 4 );
    protected Insets defaultInsets = new Insets( 4, 4, 4, 4 );
    protected Insets emptyInsets = new Insets( 0, 0, 0, 0 );


    protected final static int W = 880;
    protected final static int H = 670;
    protected final static int A1 = 360;
    protected final static int A2 = 430;
    protected final static Font BUTTON_FONT = new java.awt.Font( "Dialog", 1, 11 );

    protected final static String NA = "N/A";
    protected final static String FAULTS_TITLE = "California Fault System";


    protected final static String SURFACE = "Grid Plot";
    protected final static String SUB_SURFACE = "Grid Sub Plot";
    protected final static String FAULT = "Fault Trace";
    protected final static String BOTH_PLOTS = "Grid & Fault Trace";
    protected final static String ALL_FAULTS = "All Fault Traces";
    protected final static String BOTH_SUB_AND_SURFACE_PLOTS = "Grid & Sub Plot";
    protected final static String THREE_D = "3D Gridded Surface";

    protected final static String FRANKEL = "Frankel";
    protected final static String STIRLING = "Stirling";

    protected final static int SUB_SURFACE_PLOT_TYPE = 4;
    protected final static int SURFACE_PLOT_TYPE = 2;
    protected final static int FAULT_PLOT_TYPE = 1;
    protected final static int BOTH_PLOT_TYPE = 3;
    protected final static int ALL_FAULTS_PLOT_TYPE = 5;
    protected final static int BOTH_SUB_SURFACE_PLOT_TYPE = 6;
    protected final static int THREE_D_PLOT_TYPE = 7;

    ArrayList files;
    static {
        String lf = UIManager.getCrossPlatformLookAndFeelClassName();
        //System.out.println("Setting L&F = " + lf);
        try { UIManager.setLookAndFeel( lf ); }
        catch ( Exception e ) { System.out.println("L&F Error " + e.toString());}
        //System.out.println("Done Setting L&F = " + lf);

    }


    DoubleParameter gridSpacingParam;
    ParameterEditor gridSpacingEditor;
    SubRectanglePanel rect = new SubRectanglePanel(0,100,0,100);

    SimpleFaultDataList simpleFaultDataList;

    protected int currentControlsBar = A1;
    protected int currentMainBar = A2;

    protected final static String NO_PLOT_MSG = "No Plot Data Available";

    protected final static Dimension COMBO_DIM = new Dimension( 180, 20 );
    protected final static Dimension SMALL_COMBO_DIM = new Dimension(70, 20 );

    protected final static Dimension BUTTON_DIM = new Dimension( 80, 20 );

    protected final static GridBagLayout GBL = new GridBagLayout();

    protected final static Color darkBlue = new Color( 80, 80, 133 );
    protected final static Color lightBlue = new Color( 200, 200, 230 );
    protected final static Color background = Color.white;
    protected final static Color peach = new Color(245, 225, 180) ;

    protected final static Font FONT_1_11 = new java.awt.Font("Dialog", 1, 11);

    final static Double GRID_SPACING = new Double("2.0");

    SidesBorder topBorder = new SidesBorder( darkBlue, background, background, background );
    SidesBorder bottomBorder = new SidesBorder( background, darkBlue, background, background );
    OvalBorder oval = new OvalBorder( 7, 4, darkBlue, darkBlue );

    JPanel outerPanel = new JPanel();
    JPanel outerControlPanel = new JPanel();
    JPanel mainPanel = new JPanel();
    JPanel titlePanel = new JPanel();
    JPanel plotPanel = new JPanel();
    JPanel innerPlotPanel = new JPanel();
    JPanel parametersPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    JPanel inputPanel = new JPanel();
    JPanel sheetPanel = new JPanel();
    JPanel controlPanel = new JPanel();

    LabeledBoxPanel gridControlsPanel = new LabeledBoxPanel();
    LabeledBoxPanel gridInfoPanel = new LabeledBoxPanel();

    LabeledBorderPanel plotTypePanel = new LabeledBorderPanel();
    LabeledBorderPanel gridSpacingPanel = new LabeledBorderPanel();
    LabeledBorderPanel griddedSubsetPanel = new LabeledBorderPanel();


    JLabel titleLabel = new JLabel();
    JLabel faultLabel = new JLabel();
    JLabel frankel_StirlingLabel = new JLabel();

    JButton clearButton = new JButton();
    JButton addButton = new JButton();
    JButton toggleButton = new JButton();
    JCheckBox plotColorCheckBox = new JCheckBox();


    JSplitPane parametersSplitPane = new JSplitPane();
    JSplitPane mainSplitPane = new JSplitPane();
    JScrollPane dataScrollPane = new JScrollPane();

    JTextArea pointsTextArea = new JTextArea();

    JComboBox faultComboBox = new JComboBox();
    JComboBox frankel_StirlingComboBox = new JComboBox();
    NameValueLabel gridSpacingLabel = new NameValueLabel();
    NameValueLabel rowsLabel = new NameValueLabel();
    NameValueLabel colsLabel = new NameValueLabel();
    NameValueLabel dipLabel = new NameValueLabel();
    NameValueLabel upperSeismoLabel = new NameValueLabel();
    NameValueLabel lowerSeismoLabel = new NameValueLabel();
    NameValueLabel faultNameLabel = new NameValueLabel();


    ButtonGroup plotButtonGroup = new ButtonGroup();
    JRadioButton surfaceRadioButton = new JRadioButton();
    JRadioButton subSurfaceRadioButton = new JRadioButton();
    JRadioButton faultRadioButton = new JRadioButton();
    JRadioButton allFaultsRadioButton = new JRadioButton();
    JRadioButton bothPlotsRadioButton = new JRadioButton();
    JRadioButton subAndSurfaceRadioButton = new JRadioButton();
    JRadioButton threeDRadioButton = new JRadioButton();


    GriddedFaultPlotter plotter = new GriddedFaultPlotter();
    GriddedSurfaceAPI surface = null;
    double currentGridSpacing = -1;

   // VisadPlotter plotter3D = new VisadPlotter();
    Component comp3D = null;
    boolean threeD = false;

    int titleSize = 0;

    protected ChartPanel panel;
    protected JPanel threeDPanel;
    protected boolean graphOn = false;

    protected String lastXYAxisName = "";
    protected javax.swing.JFrame frame;
    boolean isWhite = true;

    protected int faultTracePlot = 1;
    GridBagLayout gridBagLayout1 = new GridBagLayout();


    /**
     *  Used to determine if shoudl switch to new IMR, and for display purposes
     */
    public String currentGriddedSurfaceName = "Sierra Madre";

    public String currentGriddedFaultType = this.STIRLING;

    /**
     *  Construct the applet
     */
    public GriddedFaultApplet() { }


    /**
     *  Sets the frame attribute of the IMRTesterApplet object
     *
     * @param  newFrame  The new frame value
     */
    public void setFrame( JFrame newFrame ) { frame = newFrame; }


    /**
     *  Get a parameter value
     *
     * @param  key  Description of the Parameter
     * @param  def  Description of the Parameter
     * @return      The parameter value
     */
    public String getParameter( String key, String def ) {
        return isStandalone ? System.getProperty( key, def ) :
                ( getParameter( key ) != null ? getParameter( key ) : def );
    }

    /**
     *  Get parameter info
     *
     * @return    The parameterInfo value
     */
    public String[][] getParameterInfo() { return null; }


    /**
     *  Gets the currentIMRName attribute of the IMRTesterApplet object
     *
     * @return    The currentIMRName value
     */
    public String getCurrentGriddedSurfaceName() { return currentGriddedSurfaceName; }

    /**
     *  Get Applet information
     *
     * @return    The appletInfo value
     */
    public String getAppletInfo() { return "Gridded Surface Applet"; }



    /**
     *  Initialize the applet
     */
    public void init() {
        threeDRadioButton.setVisible(false);
        ArrayList files = new ArrayList();
        files.add("Frankel96_CALA.char");
        files.add("Frankel96_CALB.char");
        simpleFaultDataList = Frankel96_SimpleFaultDataFileReader.getSimpleFaultDataList(files);

        oval.setBottomColor( darkBlue );
        oval.setTopColor( darkBlue );
        oval.setHeight( 10 );
        oval.setWidth( 10 );

        // initialize the current fault
        initFaults();

        try { jbInit(); }
        catch ( Exception e ) { e.printStackTrace(); }

        clearInfo();
        rect.disable();
    }


    /**
     *  THis must be called before the IMR is used. This is what initializes the
     *  IMR
     */
    protected void initFaults() {

        // starting
        String S = C + ": initFaults()(): ";
        if( this.simpleFaultDataList.size() < 1 )
            throw new RuntimeException( S + "No faults specified, unable to continue" );

        boolean first = true;

        frankel_StirlingComboBox.addItem(FRANKEL);
        frankel_StirlingComboBox.addItem(STIRLING);
        currentGriddedFaultType = (String)frankel_StirlingComboBox.getSelectedItem();

        ListIterator it = simpleFaultDataList.listIterator();
        ArrayList list = new ArrayList();
        while( it.hasNext() ){

            Object obj = it.next();
            if(obj != null && obj instanceof SimpleFaultData){
                String name = ((SimpleFaultData)obj).getFaultTrace().getName();
                list.add(name);
            }
        }
        Collections.sort( list );
        ListIterator it2 = list.listIterator();

        currentGriddedSurfaceName = "";
        while ( it2.hasNext() ){

            if( !first ) faultComboBox.addItem( it2.next().toString() );
            else {
                first = false;
                String val = it2.next().toString();
                currentGriddedSurfaceName = val;
                faultComboBox.addItem( val );
            }

        }

        if( !currentGriddedSurfaceName.equals("" )) faultComboBox.setSelectedItem( currentGriddedSurfaceName );

    }



    private GriddedSurfaceAPI getFaultGriddedSurface(String faultName){

        String S = C + " ; getFaultGriddedSurface(): ";
        if( D ) System.out.println(S + "Starting");

        SimpleFaultData faultData = simpleFaultDataList.getSimpleFaultData(faultName);

        if( D ) System.out.println(S + "Creating StirlingGriddedFaultFactory for " + faultName);

        currentGriddedFaultType = (String)frankel_StirlingComboBox.getSelectedItem();
        if(currentGriddedFaultType.equalsIgnoreCase(this.STIRLING))
          factory = new StirlingGriddedFaultFactory(faultData,
                      ((Double)gridSpacingEditor.getValue()).doubleValue());
        else
          factory = new FrankelGriddedFaultFactory(faultData,
                      ((Double)gridSpacingEditor.getValue()).doubleValue());

        GriddedSurfaceAPI surface = factory.getGriddedSurface();
        surface.setName(faultName);

        gridSpacingLabel.setValue( "" + ( (Double)gridSpacingEditor.getValue() ).doubleValue() + " km");
        rowsLabel.setValue("" + surface.getNumRows() );
        colsLabel.setValue("" + surface.getNumCols() );
        dipLabel.setValue( "" + faultData.getAveDip() );
        upperSeismoLabel.setValue( "" + faultData.getUpperSeismogenicDepth() );
        lowerSeismoLabel.setValue( "" + faultData.getLowerSeismogenicDepth() );
        faultNameLabel.setValue( faultName );


        if( D ) System.out.println(S + "Surface = " + surface.toString());
        if( D ) System.out.println(S + "Ending");

        return surface;

    }



    /**
     *  Adds a feature to the GraphPanel attribute of the TesterApplet object
     */
    protected void addGraphPanel() {

        // Starting
        String S = C + ": addGraphPanel(): ";
        if ( D ) System.out.println( S + "Starting: Last xy axis name = " + lastXYAxisName );

        panel = plotter.addGraphPanel(currentGriddedSurfaceName,((Double)gridSpacingParam.getValue()).doubleValue());


        if ( D ) System.out.println( S + "Toggling plot on" );
        graphOn = false;
        togglePlot();
        if ( D ) System.out.println( S + "Done" );
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of the Parameter
     */
    public void itemStateChanged( ItemEvent e ) {

        // Starting
        String S = C + ": itemStateChanged(): ";
        if ( D ) System.out.println( S + "Starting" );

        Object src = e.getSource();

        if ( src.equals( faultComboBox ) ){
            updateChoosenFault();
            rect.disable();
        } else if(src.equals(this.frankel_StirlingComboBox)) {
           addPlot();
        }
        else {

            ItemSelectable selectable = e.getItemSelectable();
            if( selectable instanceof AbstractButton){

                rect.disable();
                rect.invalidate();
                rect.repaint();
                boolean selected = ( e.getStateChange() == ItemEvent.SELECTED );

                if( selected ){

                    plotter.clear();

                    if( selectable.equals( faultRadioButton ) )
                        faultTracePlot = FAULT_PLOT_TYPE;
                    else if ( selectable.equals( surfaceRadioButton ) )
                        faultTracePlot = SURFACE_PLOT_TYPE;
                    else if ( selectable.equals( allFaultsRadioButton ) )
                        faultTracePlot = ALL_FAULTS_PLOT_TYPE;
                    else if ( selectable.equals( bothPlotsRadioButton ) )
                        faultTracePlot = BOTH_PLOT_TYPE;
                    else if ( selectable.equals( subSurfaceRadioButton ) ) {
                        faultTracePlot = SUB_SURFACE_PLOT_TYPE;
                        if (surface != null ) rect.enable();
                    }
                    else if ( selectable.equals( subAndSurfaceRadioButton ) ) {
                        faultTracePlot = BOTH_SUB_SURFACE_PLOT_TYPE;
                        rect.enable();
                    }
                    else if ( selectable.equals( this.threeDRadioButton) ) {
                        faultTracePlot = THREE_D_PLOT_TYPE;
                        if (surface != null ) rect.enable();
                    }
                }
            }

        }

        // Ending
        if ( D ) System.out.println( S + "Ending" );

    }



    /**
     *  Used for synch applet with new Fault choosen. Updates lables and
     *  initializes the Fault if needed.
     */
    protected void updateChoosenFault() {

        // Starting
        String S = C + ": updateChoosenFault(): ";

        String choice = faultComboBox.getSelectedItem().toString();

        if ( choice.equals( currentGriddedSurfaceName ) ) return;
        else currentGriddedSurfaceName = choice;

        if ( D ) System.out.println( S + "Starting: New IMR = " + choice );

        // Clear the current traces
        clearPlot();

        if ( titleLabel != null ) {
            titleLabel.setText( currentGriddedSurfaceName );
            titleLabel.validate();
            titleLabel.repaint();
        }

        if ( frame != null )
            frame.setTitle( this.getAppletInfo() + ": [" + currentGriddedSurfaceName + ']');

        //imr = imrs.setImr( currentIMRName, this );

        sheetPanel.removeAll();
        inputPanel.removeAll();
        validate();
        repaint();

        // Ending
        if ( D ) System.out.println( S + "Ending" );

    }


    protected boolean gridSpacingChanged(){

        double newGridSpacing = ( (Double)gridSpacingEditor.getValue() ).doubleValue();
        if( currentGridSpacing == newGridSpacing ) return false;
        else{
            currentGridSpacing = newGridSpacing;
            return true;
        }
    }

    protected void clearInfo(){
        gridSpacingLabel.setValue( NA );
        rowsLabel.setValue( NA );
        colsLabel.setValue( NA );

        dipLabel.setValue( NA );
        upperSeismoLabel.setValue( NA );
        lowerSeismoLabel.setValue( NA );
        faultNameLabel.setValue( NA );
    }

    /**
     *  This causes the model data to be calculated and a plot trace added to
     *  the current plot
     *
     * @param  e  The feature to be added to the Button_mouseClicked attribute
     */
    public void addButton_mouseClicked( MouseEvent e ) { addPlot( ); }


    /**
     *  This causes the model data to be calculated and a plot trace added to
     *  the current plot
     *
     * @param  e  The feature to be added to the Button_mouseClicked attribute
     */
    protected void addPlot(){

        String S = C + ": addButton(): ";
        if (D) System.out.println( S + "Starting: Plot Type = " + faultTracePlot);

        // Add points data to text area, people can see
        plotter.setLightweight(false);
        this.currentGriddedSurfaceName = faultComboBox.getSelectedItem().toString();
        if ( frame != null )
            frame.setTitle( this.getAppletInfo() + ": " + currentGriddedSurfaceName );


        String selectedFaultType = (String)frankel_StirlingComboBox.getSelectedItem();
        switch (faultTracePlot) {

            case THREE_D_PLOT_TYPE:

                panel = null;
                threeD = true;
                if( surface == null ||
                    !surface.getName().equals(currentGriddedSurfaceName) ||
                    gridSpacingChanged() ||
                    !currentGriddedFaultType.equalsIgnoreCase(selectedFaultType)){

                    gridSpacingChanged();
                    surface = getFaultGriddedSurface(currentGriddedSurfaceName);

                    int rows = surface.getNumRows();
                    int cols = surface.getNumCols();


                    rect.enable();
                    rect.setFullRange(0, rows - 1, 0, cols - 1);
                    rect.disable();
                }

                gridSpacingLabel.setValue( "" + ( (Double)gridSpacingEditor.getValue() ).doubleValue() + " km");
                rowsLabel.setValue("" + surface.getNumRows() );
                colsLabel.setValue("" + surface.getNumCols() );

                // if( factory != null) {
                    // double[][] threeD = factory.getVisadDomainSurface(surface);
                //}

                pointsTextArea.setText( "Gridded Surface Data:\n" + currentGriddedSurfaceName + '\n' + surface.toString() );
                //comp3D = plotter3D.getExamplePlot();
                //comp3D = plotter3D.getPlot(surface);
                graphOn = false;
                togglePlot();

                break;

            case ALL_FAULTS_PLOT_TYPE:

                comp3D = null;
                threeD = false;
                clearInfo();
                rect.disable();
                plotter.clear();
                plotter.setLightweight(true);
                plotter.setPlotType( GriddedFaultPlotter.LINES);

                this.currentGriddedSurfaceName = FAULTS_TITLE;
                if ( frame != null )
                    frame.setTitle( this.getAppletInfo() + ": [" + currentGriddedSurfaceName + ']');

                int max = simpleFaultDataList.size();
                for( int i = 0; i < max; i++){
                    FaultTraceXYDataSet dataSet = new FaultTraceXYDataSet( simpleFaultDataList.getSimpleFaultDataAt(i).getFaultTrace() );
                    plotter.add(dataSet);
                }
                addGraphPanel();
                break;

            case FAULT_PLOT_TYPE:

                SimpleFaultData simpleFaultData = simpleFaultDataList.getSimpleFaultData(currentGriddedSurfaceName);
                comp3D = null;
                threeD = false;
                clearInfo();
                rect.disable();

                dipLabel.setValue( "" + simpleFaultData.getAveDip() );
                upperSeismoLabel.setValue( "" + simpleFaultData.getUpperSeismogenicDepth() );
                lowerSeismoLabel.setValue( "" + simpleFaultData.getLowerSeismogenicDepth() );
                faultNameLabel.setValue( simpleFaultData.getFaultTrace().getName() );

                plotter.clear();
                plotter.setPlotType( GriddedFaultPlotter.SHAPES_AND_LINES);
                plotter.add( new FaultTraceXYDataSet(simpleFaultData.getFaultTrace()) );
                addGraphPanel();
                break;


            case SURFACE_PLOT_TYPE:

                comp3D = null;
                threeD = false;
                if( surface == null ||
                    !surface.getName().equals(currentGriddedSurfaceName) ||
                    gridSpacingChanged() ||
                    !currentGriddedFaultType.equalsIgnoreCase(selectedFaultType)){

                    gridSpacingChanged();
                    surface = getFaultGriddedSurface(currentGriddedSurfaceName);

                    int rows = surface.getNumRows();
                    int cols = surface.getNumCols();


                    rect.enable();
                    rect.setFullRange(0, rows - 1, 0, cols - 1);
                    rect.disable();
                }

                gridSpacingLabel.setValue( "" + ( (Double)gridSpacingEditor.getValue() ).doubleValue() + " km");
                rowsLabel.setValue("" + surface.getNumRows() );
                colsLabel.setValue("" + surface.getNumCols() );


                //ArbDiscrFunction2DWithParamsList functions2 = getGriddedSurfaceFunctions2DWithParams( surface );
                GriddedSurfaceXYDataSet functions2 = new GriddedSurfaceXYDataSet(surface);

                pointsTextArea.setText( "Gridded Surface Data:\n" + currentGriddedSurfaceName + '\n' + surface.toString() );

                plotter.clear();
                plotter.setPlotType( GriddedFaultPlotter.SHAPES);
                plotter.add(functions2);
                addGraphPanel();
                break;

            case SUB_SURFACE_PLOT_TYPE:

                comp3D = null;
                threeD = false;
                boolean newSurfaceNeeded = false;
                if( surface == null) {
                    newSurfaceNeeded = true;
                    if (D) System.out.println("1 newSurfaceNeeded " + newSurfaceNeeded);
                }
                else if( !surface.getName().equals(currentGriddedSurfaceName) ){
                    newSurfaceNeeded = true;
                    if (D) System.out.println("2 newSurfaceNeeded " + newSurfaceNeeded);
                }
                else if( gridSpacingChanged() ) {
                    newSurfaceNeeded = true;
                    if (D) System.out.println("3 newSurfaceNeeded " + newSurfaceNeeded);
                }

                //if( surface == null || !surface.getName().equals(currentGriddedSurfaceName) || gridSpacingChanged() ){

                if( newSurfaceNeeded ||
                    !currentGriddedFaultType.equalsIgnoreCase(selectedFaultType)){
                    surface = getFaultGriddedSurface(currentGriddedSurfaceName);

                    int rows = surface.getNumRows();
                    int cols = surface.getNumCols();

                    rect.enable();
                    rect.setFullRange(0, rows - 1, 0, cols - 1);
                    rect.disable();

                    double newGridSpacing = ( (Double)gridSpacingEditor.getValue() ).doubleValue();
                    currentGridSpacing = newGridSpacing;

                }
                else{

                    SimpleFaultData faultData = simpleFaultDataList.getSimpleFaultData(currentGriddedSurfaceName);
                    gridSpacingLabel.setValue( "" + ( (Double)gridSpacingEditor.getValue() ).doubleValue() + " km" );
                    rowsLabel.setValue("" + surface.getNumRows() );
                    colsLabel.setValue("" + surface.getNumCols() );
                    dipLabel.setValue( "" + faultData.getAveDip() );
                    upperSeismoLabel.setValue( "" + faultData.getUpperSeismogenicDepth() );
                    lowerSeismoLabel.setValue( "" + faultData.getLowerSeismogenicDepth() );
                    faultNameLabel.setValue( faultData.getFaultTrace().getName() );
                }

                rect.enable();
                plotter.clear();
                plotter.setPlotType( GriddedFaultPlotter.SHAPES);

                int rowMax = surface.getNumRows() - 1;
                int colMax = surface.getNumCols() - 1;

                int xMin = rect.getXMin();
                int xMax = rect.getXMax();

                int yMin = rect.getYMin();
                int yMax = rect.getYMax();

                boolean ok = true;
                if( xMin < 0 || xMax < 0 || yMin < 0 || yMax < 0 ) ok = false;
                else if( xMin > xMax || yMin > yMax ) ok = false;
                else if( xMin > rowMax || xMax > rowMax || yMin > colMax || yMax > colMax ) ok = false;

                if(ok){
                    GriddedSubsetSurface subSurface = new GriddedSubsetSurface(
                        xMax - xMin + 1, yMax - yMin + 1, xMin, yMin, surface
                    );

                    GriddedSurfaceXYDataSet functions10 = new GriddedSurfaceXYDataSet(subSurface);
                    //ArbDiscrFunction2DWithParamsList functions10 = getGriddedSurfaceFunctions2DWithParams( subSurface );
                    pointsTextArea.setText( "Gridded Subsurface Data:\n" + currentGriddedSurfaceName + '\n' + subSurface.toString() );
                    plotter.add(functions10);

                }
                else{

                    if(D) System.out.println(S + "Showing Dialog");
                    JOptionPane.showMessageDialog(
                        null, "There is a problem with you subset range values, please correct.",
                        "Cannot Generate", JOptionPane.INFORMATION_MESSAGE
                    );

                }
                addGraphPanel();
                break;


            case BOTH_SUB_SURFACE_PLOT_TYPE:

                comp3D = null;
                threeD = false;
                boolean subReady = rect.isEnabled();
                rect.enable();
                plotter.clear();

                if( subReady ) plotter.setPlotType( GriddedFaultPlotter.SUB_SHAPES);
                else plotter.setPlotType( GriddedFaultPlotter.SHAPES);

                boolean newSurfaceNeeded2 = false;
                if( surface == null) {
                    newSurfaceNeeded2 = true;
                    if (D) System.out.println("1 newSurfaceNeeded " + newSurfaceNeeded2);
                }
                else if( !surface.getName().equals(currentGriddedSurfaceName) ){
                    newSurfaceNeeded2 = true;
                    if (D) System.out.println("2 newSurfaceNeeded " + newSurfaceNeeded2);
                }
                else if( gridSpacingChanged() ) {
                    newSurfaceNeeded2 = true;
                    if (D) System.out.println("3 newSurfaceNeeded " + newSurfaceNeeded2);
                }

                //if( surface == null || !surface.getName().equals(currentGriddedSurfaceName) || gridSpacingChanged() ){

                if( newSurfaceNeeded2 ||
                    !currentGriddedFaultType.equalsIgnoreCase(selectedFaultType)){
                    surface = getFaultGriddedSurface(currentGriddedSurfaceName);

                    int rows = surface.getNumRows();
                    int cols = surface.getNumCols();

                    rect.enable();
                    rect.setFullRange(0, rows - 1, 0, cols - 1);

                    double newGridSpacing = ( (Double)gridSpacingEditor.getValue() ).doubleValue();
                    currentGridSpacing = newGridSpacing;

                }
                else{

                    SimpleFaultData faultData = simpleFaultDataList.getSimpleFaultData(currentGriddedSurfaceName);
                    gridSpacingLabel.setValue( "" + ( (Double)gridSpacingEditor.getValue() ).doubleValue() + " km" );
                    rowsLabel.setValue("" + surface.getNumRows() );
                    colsLabel.setValue("" + surface.getNumCols() );
                    dipLabel.setValue( "" + faultData.getAveDip() );
                    upperSeismoLabel.setValue( "" + faultData.getUpperSeismogenicDepth() );
                    lowerSeismoLabel.setValue( "" + faultData.getLowerSeismogenicDepth() );
                    faultNameLabel.setValue( faultData.getFaultTrace().getName() );
                }

                plotter.add(new GriddedSurfaceXYDataSet(surface));


                if( subReady )  {
                    int rowMax2 = surface.getNumRows() - 1;
                    int colMax2 = surface.getNumCols() - 1;

                    int xMin2 = rect.getXMin();
                    int xMax2 = rect.getXMax();

                    int yMin2 = rect.getYMin();
                    int yMax2 = rect.getYMax();

                    boolean ok2 = true;
                    if( xMin2 < 0 || xMax2 < 0 || yMin2 < 0 || yMax2 < 0 ) ok2 = false;
                    else if( xMin2 > xMax2 || yMin2 > yMax2 ) ok2 = false;
                    else if( xMin2 > rowMax2 || xMax2 > rowMax2 || yMin2 > colMax2 || yMax2 > colMax2 ) ok2 = false;

                    if(ok2){
                        GriddedSubsetSurface subSurface2 = new GriddedSubsetSurface(
                            xMax2 - xMin2 + 1, yMax2 - yMin2 + 1, xMin2, yMin2, surface
                        );

                        GriddedSurfaceXYDataSet functions11 = new GriddedSurfaceXYDataSet(subSurface2);
                        //ArbDiscrFunction2DWithParamsList functions10 = getGriddedSurfaceFunctions2DWithParams( subSurface );
                        pointsTextArea.setText( "Gridded Subsurface Data:\n" + currentGriddedSurfaceName + '\n' + subSurface2.toString() );
                        plotter.add(functions11);

                    }
                    else{

                        if(D) System.out.println(S + "Showing Dialog");
                        JOptionPane.showMessageDialog(
                            null, "There is a problem with you subset range values, please correct.",
                            "Cannot Generate", JOptionPane.INFORMATION_MESSAGE
                        );

                    }
                }
                addGraphPanel();
                break;

            case BOTH_PLOT_TYPE:

                comp3D = null;
                threeD = false;
                if( surface == null ||
                    !surface.getName().equals(currentGriddedSurfaceName) ||
                    gridSpacingChanged()||
                    !currentGriddedFaultType.equalsIgnoreCase(selectedFaultType)){

                    surface = getFaultGriddedSurface(currentGriddedSurfaceName);

                    int rows = surface.getNumRows();
                    int cols = surface.getNumCols();

                    rect.enable();
                    rect.setFullRange(0, rows - 1, 0, cols - 1);
                    rect.disable();

                }

                GriddedSurfaceXYDataSet functions3 = new GriddedSurfaceXYDataSet(surface);
                pointsTextArea.setText( currentGriddedSurfaceName + '\n' + surface.toString() );
                //plotter.add(functions3);


                SimpleFaultData faultData1 = simpleFaultDataList.getSimpleFaultData(currentGriddedSurfaceName);

                gridSpacingLabel.setValue( "" + ( (Double)gridSpacingEditor.getValue() ).doubleValue() + " km");
                rowsLabel.setValue("" + surface.getNumRows() );
                colsLabel.setValue("" + surface.getNumCols() );

                dipLabel.setValue( "" + faultData1.getAveDip() );
                upperSeismoLabel.setValue( "" + faultData1.getUpperSeismogenicDepth() );
                lowerSeismoLabel.setValue( "" + faultData1.getLowerSeismogenicDepth() );
                faultNameLabel.setValue( faultData1.getFaultTrace().getName() );


                plotter.clear();
                plotter.setPlotType( GriddedFaultPlotter.SHAPES_LINES_AND_SHAPES);
                plotter.add( new FaultTraceXYDataSet(faultData1.getFaultTrace()) );
                plotter.add(functions3);
                addGraphPanel();
                break;


            default:
                break;
        }

        //if ( D ) System.out.println( S + "Graphing function:" + function.toString() );



        if ( titleLabel != null ) {
            titleLabel.setText( currentGriddedSurfaceName );
            titleLabel.validate();
            titleLabel.repaint();
        }

        if ( D ) System.out.println( S + "Ending" );

    }

    /**
     *  Clears the plot screen of all traces, then sychs imr to model
     *
     * @param  e  Description of the Parameter
     */
    void clearButton_mouseClicked( MouseEvent e ) { clearButton(); }

    void clearButton(){ clearPlot(); }

    /**
     *  Clears the plot screen of all traces
     */
    void clearPlot() {

        if ( D ) System.out.println( "Clearing plot area" );

        int loc = mainSplitPane.getDividerLocation();
        int newLoc = loc;
        titleSize = titlePanel.getHeight() + 6;

        innerPlotPanel.removeAll();
        panel = null;
        pointsTextArea.setText( NO_PLOT_MSG );
        plotter.clear();


        if ( !titlePanel.isVisible() ) {
            titlePanel.setVisible( true );
        }

        if ( titleLabel != null ) {
            titleLabel.setText( this.currentGriddedSurfaceName );
            titleLabel.validate();
            titleLabel.repaint();
        }


        validate();
        repaint();

        mainSplitPane.setDividerLocation( newLoc );
    }



    /**
     *  Description of the Method
     *
     * @param  e  Description of the Parameter
     */
    protected void toggleButton_mouseClicked( MouseEvent e ) {
        togglePlot();
    }



    /**
     *  Description of the Method
     */
    protected void togglePlot() {

        // Starting
        String S = C + ": togglePlot(): ";

        innerPlotPanel.removeAll();

        int loc = mainSplitPane.getDividerLocation();
        titleSize = titlePanel.getHeight() + 6;

        int newLoc = loc;
        if ( graphOn ) {
            if ( D )
                System.out.println( S + "Showing Data" );
            toggleButton.setText( "Show Plot" );
            graphOn = false;

            if ( !titlePanel.isVisible() ) {
                titlePanel.setVisible( true );
                // newLoc = loc - titleSize;
            }

            // dataScrollPane.setVisible(true);
            // innerPlotPanel.setBorder(oval);
            innerPlotPanel.add( dataScrollPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                    , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );
        }
        else {
            if ( D )
                System.out.println( S + "About to show Plot" );
            graphOn = true;
            // dataScrollPane.setVisible(false);
            toggleButton.setText( "Show Data" );
            if ( panel != null || comp3D != null ) {
                if ( D )
                    System.out.println( S + "Showing Plot" );


                if ( titlePanel.isVisible() ) {
                    titlePanel.setVisible( false );
                    //newLoc = loc + titleSize;
                }

                // innerPlotPanel.setBorder(null);

                // panel added here
                if( !threeD )
                    innerPlotPanel.add( panel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
                else
                    innerPlotPanel.add( comp3D, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

                // innerPlotPanel.add(dataScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                // ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
            }
            else {
                if ( D )
                    System.out.println( S + "No Plot - So Showing Data" );


                if ( !titlePanel.isVisible() ) {
                    titlePanel.setVisible( true );
                    // newLoc = loc - titleSize;
                }

                // innerPlotPanel.setBorder(oval);
                innerPlotPanel.add( dataScrollPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );
            }

        }

        if ( D ) System.out.println( S + "Calling validate and repaint" );
        mainSplitPane.setDividerLocation( newLoc );
        validate();
        repaint();

        if ( D ) System.out.println( S + "Loc = " + loc + '\t' + "New Loc = " + newLoc );
        if ( D ) System.out.println( S + "Ending" );

    }

    public void stateChanged(ChangeEvent e) {

        // Starting
        String S = C + ": stateChanged(): ";
        if ( D ) System.out.println( S + "Starting" );

        if( e.getSource().equals( plotColorCheckBox ) ){

            if( isWhite ) {
                isWhite = false;
                plotter.setPlotColor(Color.black);

                if( panel != null )
                    panel.getChart().getPlot().setBackgroundPaint(Color.black);

            }
            else{
                isWhite = true;
                plotter.setPlotColor(Color.white);

                if( panel != null )
                    panel.getChart().getPlot().setBackgroundPaint(Color.white);

            }

        }


        // Ending
        if ( D ) System.out.println( S + "Ending" );

    }



    /**
     *  Start the applet
     */
    public void start() { }

    /**
     *  Stop the applet
     */
    public void stop() { }

    /**
     *  Destroy the applet
     */
    public void destroy() { }

    /**
     *  Main method
     *
     * @param  args  The command line arguments
     */
    public static void main( String[] args ) {

        String S = C + ": main(): ";
        if( D ) System.out.println(S + "Starting");
        //if( D ) System.out.println(S + "Ending");


        Color c = GriddedFaultApplet.peach;
        Font f = new Font( "Dialog", Font.PLAIN, 11 );

        UIManager.put( "ScrollBar.width", new Integer( 12 ) );
        UIManager.put( "ScrollPane.width", new Integer( 12 ) );

        UIManager.put( "PopupMenu.font", f );
        UIManager.put( "Menu.font", f );
        UIManager.put( "MenuItem.font", f );

        UIManager.put( "ScrollBar.border", BorderFactory.createEtchedBorder( 1 ) );

        UIManager.put( "PopupMenu.background", c );

        //UIManager.put("PopupMenu.selectionBackground", c );
        //UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(Color.red, 1 ) );

        UIManager.put( "Menu.background", c );
        //UIManager.put("Menu.selectionBackground", c );

        UIManager.put( "MenuItem.background", c );
        UIManager.put( "MenuItem.disabledBackground", c );
        //UIManager.put("MenuItem.selectionBackground", c );

        // UIManager.put("MenuItem.borderPainted", new Boolean(false) );
        UIManager.put( "MenuItem.margin", new Insets( 0, 0, 0, 0 ) );

        UIManager.put( "ComboBox.background", c );
        //UIManager.put("ComboBox.selectionBackground", new Color(220, 230, 170));



        GriddedFaultApplet applet = new GriddedFaultApplet();
        applet.isStandalone = true;
        JFrame frame = new JFrame();
        //EXIT_ON_CLOSE == 3
        frame.setDefaultCloseOperation( 3 );
        frame.getContentPane().add( applet, BorderLayout.CENTER );

        applet.init();
        applet.start();
        applet.setFrame( frame );

        frame.setTitle( applet.getAppletInfo() + ":  [" + applet.getCurrentGriddedSurfaceName() + ']' );

        frame.setSize( W, H );
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation( ( d.width - frame.getSize().width ) / 2, ( d.height - frame.getSize().height ) / 2 );
        frame.setVisible( true );
    }


    private JRadioButton initRadioButton(JRadioButton button, String label, int index){

        button.setBackground(Color.white);
        button.setFont(FONT_1_11);
        button.setForeground(SystemColor.activeCaption);
        button.setFocusPainted(false);
        button.setMargin(emptyInsets);
        button.setText(label);
        button.setActionCommand(label);
        button.addItemListener(this);

        plotButtonGroup.add(button);
        plotTypePanel.add(button,  new GridBagConstraints(0, index, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, emptyInsets, 0, 0));

        return button;
    }

    /**
     *  Component initialization
     *
     * @exception  Exception  Description of the Exception
     */
    protected void jbInit() throws Exception {

        String S = C + ": jbInit(): ";


        this.setFont( new java.awt.Font( "Dialog", 0, 10 ) );
        this.getContentPane().setBackground( background );
        this.setSize( new Dimension( 559, 483 ) );
        this.getContentPane().setLayout( GBL );

        gridControlsPanel.setTitle("Gridded Surface Controls");
        gridControlsPanel.setHeaderPanelBackgroundColor( peach  );
        gridControlsPanel.setLayout(GBL);

        gridInfoPanel.setTitle("Gridded Surface Info");
        gridInfoPanel.setHeaderPanelBackgroundColor( peach );
        gridInfoPanel.setLayout(GBL);

        outerPanel.setBackground( background );
        outerPanel.setLayout( GBL );

        mainPanel.setBackground( background );
        mainPanel.setBorder( oval );
        // mainPanel.setBorder(BorderFactory.createLineBorder( darkBlue ) );
        mainPanel.setLayout( GBL );

        titlePanel.setBackground( background );
        titlePanel.setBorder( bottomBorder );
        titlePanel.setLayout( GBL );

        titleLabel.setHorizontalAlignment( SwingConstants.CENTER );
        //titleLabel.setText(this.getAppletInfo());
        titleLabel.setFont( new java.awt.Font( "Dialog", 1, 16 ) );

        plotPanel.setBackground( background );
        // plotPanel.setBorder(oval);
        plotPanel.setLayout( GBL );

        innerPlotPanel.setBackground( background );
        innerPlotPanel.setLayout( GBL );
        innerPlotPanel.setBorder( null );

        controlPanel.setLayout( GBL );
        controlPanel.setBackground( background );
        controlPanel.setBorder(BorderFactory.createEtchedBorder(1));

        outerControlPanel.setLayout( GBL );
        outerControlPanel.setBackground( background );


        clearButton.setBackground( lightBlue );
        clearButton.setForeground( darkBlue );
        clearButton.setFont( BUTTON_FONT );
        clearButton.setBorder( BorderFactory.createRaisedBevelBorder() );
        clearButton.setFocusPainted( false );
        clearButton.setText( "Clear Plot" );

        /*
        clearButton.addFocusListener(
            new java.awt.event.FocusListener() {
                public void focusGained(FocusEvent e){
                    clearButtonFocusGained();
                }
                public void focusLost(FocusEvent e){ }
            }
        );
        */
        clearButton.addMouseListener(
        new java.awt.event.MouseAdapter() {
            public void mouseClicked( MouseEvent e ) {
                clearButton_mouseClicked( e );
            }
        } );


        clearButton.setPreferredSize( BUTTON_DIM );
        clearButton.setMinimumSize( BUTTON_DIM );

        addButton.setBackground( lightBlue );
        addButton.setForeground( darkBlue );
        addButton.setFont( BUTTON_FONT );
        addButton.setBorder( BorderFactory.createRaisedBevelBorder() );
        addButton.setFocusPainted( false );
        addButton.setText( "Add Trace" );

        /*
        addButton.addFocusListener(
            new java.awt.event.FocusListener() {
                public void focusGained(FocusEvent e){
                    addButtonFocusGained();
                }
                public void focusLost(FocusEvent e){ }
            }
        );
        */

        addButton.addMouseListener(
        new java.awt.event.MouseAdapter() {
            public void mouseClicked( MouseEvent e ) {
                addButton_mouseClicked( e );
            }
        } );



        addButton.setPreferredSize( BUTTON_DIM );
        addButton.setMinimumSize( BUTTON_DIM );

        toggleButton.setBackground( lightBlue );
        toggleButton.setForeground( darkBlue );
        toggleButton.setFont( BUTTON_FONT );
        toggleButton.setBorder( BorderFactory.createRaisedBevelBorder() );
        toggleButton.setFocusPainted( false );
        toggleButton.setText( "Show Data" );


        /*
        toggleButton.addFocusListener(
            new java.awt.event.FocusListener() {
                public void focusGained(FocusEvent e){
                    toggleButtonFocusGained();
                }
                public void focusLost(FocusEvent e){ }
            }
        );
        */

        toggleButton.addMouseListener(
            new java.awt.event.MouseAdapter() {
                public void mouseClicked( MouseEvent e ) {
                    toggleButton_mouseClicked( e );
                }
        } );

        toggleButton.setPreferredSize( BUTTON_DIM );
        toggleButton.setMinimumSize( BUTTON_DIM );
        //toggleButton.setVisible(false);


        plotColorCheckBox.setBackground(Color.white);
        plotColorCheckBox.setFont(new java.awt.Font("Dialog", 1, 11));
        plotColorCheckBox.setForeground(new Color(80, 80, 133));
        plotColorCheckBox.setText("Black Background");
        plotColorCheckBox.addChangeListener(this);
        plotColorCheckBox.setSelected(true);


        buttonPanel.setBackground( background );
        buttonPanel.setBorder( topBorder );
        buttonPanel.setLayout( GBL );

        parametersPanel.setLayout( GBL );
        parametersPanel.setBackground( background );
        //parametersPanel.setBorder(BorderFactory.createEtchedBorder());

        //inputPanel.setBorder(oval);
        inputPanel.setLayout( GBL );
        inputPanel.setBackground( background );

        //sheetPanel.setBorder(BorderFactory.createEtchedBorder());
        sheetPanel.setLayout( GBL );
        sheetPanel.setBackground( background );

        //parametersSplitPane.setBorder(oval);
        parametersSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        parametersSplitPane.setBorder( null );
        parametersSplitPane.setDividerSize( 5 );

        mainSplitPane.setOrientation( JSplitPane.HORIZONTAL_SPLIT );
        mainSplitPane.setBorder( null );
        mainSplitPane.setDividerSize( 5 );

        pointsTextArea.setBorder( BorderFactory.createEtchedBorder() );
        pointsTextArea.setText( NO_PLOT_MSG );


        dataScrollPane.setBorder( BorderFactory.createEtchedBorder() );

        titleLabel.setForeground( darkBlue );

        faultLabel.setForeground( darkBlue );
        faultLabel.setText( "Choose Fault: " );
        frankel_StirlingLabel.setForeground(darkBlue);
        frankel_StirlingLabel.setText("Fault Model:");

        faultComboBox.setBackground( lightBlue );
        faultComboBox.setForeground( darkBlue );
        faultComboBox.setFont( new java.awt.Font( "Dialog", 0, 11 ) );
        faultComboBox.setBorder( null );
        faultComboBox.setPreferredSize( COMBO_DIM );

        faultComboBox.addItemListener( this );
        faultComboBox.setMinimumSize( COMBO_DIM );

        frankel_StirlingComboBox.setBackground( lightBlue );
        frankel_StirlingComboBox.setForeground( darkBlue );
        frankel_StirlingComboBox.setFont( new java.awt.Font( "Dialog", 0, 11 ) );
        frankel_StirlingComboBox.setBorder( null );
        frankel_StirlingComboBox.setPreferredSize( SMALL_COMBO_DIM );

        frankel_StirlingComboBox.addItemListener( this );
        frankel_StirlingComboBox.setMinimumSize( SMALL_COMBO_DIM );


        this.getContentPane().add( outerPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, emptyInsets, 0, 0 ) );

        outerPanel.add( mainPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 5, 5, 5, 5 ), 0, 0 ) );

        // mainPanel.add( titlePanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0
                //, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 4, 4, 2, 4 ), 0, 0 ) );

        titlePanel.add( titleLabel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, emptyInsets, 0, 0 ) );

        mainPanel.add( mainSplitPane, new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 2, 4, 4, 4 ), 0, 0 ) );

        // mainPanel.add( plotPanel, new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0
                //, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 2, 4, 4, 4 ), 0, 0 ) );

        mainPanel.add( buttonPanel, new GridBagConstraints( 0, 2, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 1, 1, 1, 1 ), 0, 0 ) );


        controlPanel.add( parametersPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, emptyInsets, 0, 0 ) );


        outerControlPanel.add( controlPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 5, 0, 0 ), 0, 0 ) );


        parametersPanel.add( parametersSplitPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, emptyInsets, 0, 0 ) );


        // controlPanel.add( buttonPanel, new GridBagConstraints( 0, 1, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1.0, 0.0
                //, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 1, 1, 1, 1 ), 0, 0 ) );


        plotPanel.add( titlePanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 4, 4, 2, 4 ), 0, 0 ) );

        plotPanel.add( innerPlotPanel, new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ) );


        dataScrollPane.getViewport().add( pointsTextArea, null );

        buttonPanel.add( addButton, new GridBagConstraints( 4, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 5, 3, 0, 3 ), 0, 0 ) );
        buttonPanel.add( clearButton, new GridBagConstraints( 5, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 5, 3, 0, 3 ), 0, 0 ) );

        buttonPanel.add( toggleButton, new GridBagConstraints( 6, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 5, 3, 0, 3 ), 0, 0 ) );

        buttonPanel.add( plotColorCheckBox, new GridBagConstraints( 7, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 5, 3, 0, 1 ), 0, 0 ) );

        buttonPanel.add( faultComboBox, new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 7, 1, 0, 2 ), 0, 0 ) );
        buttonPanel.add( faultLabel, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

        buttonPanel.add( frankel_StirlingLabel, new GridBagConstraints( 2, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 7, 1, 0, 2 ), 0, 0 ) );
        buttonPanel.add( frankel_StirlingComboBox, new GridBagConstraints( 3, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 7, 1, 0, 2 ), 0, 0 ) );

        parametersSplitPane.setBottomComponent( gridInfoPanel );
        parametersSplitPane.setTopComponent( gridControlsPanel );
        parametersSplitPane.setDividerLocation( 390 );


        parametersSplitPane.setOneTouchExpandable( false );

        mainSplitPane.setBottomComponent( outerControlPanel );
        mainSplitPane.setTopComponent( plotPanel );

        mainSplitPane.setDividerLocation( 590 );
        //mainSplitPane.setDividerLocation( 900 );
        mainSplitPane.setOneTouchExpandable( false );

        // Big function here, sets all the IMR stuff and puts in sheetsPanel and

        clearPlot();

        buildInfoPanel();
        buildControlsPanel();
    }




    private void buildControlsPanel() throws Exception{


        gridSpacingParam = new DoubleParameter("Grid Spacing",.01,20,null, GRID_SPACING );
        gridSpacingEditor = new ConstrainedDoubleParameterEditor(gridSpacingParam);
        // gridSpacingEditor.setValue(GRID_SPACING);
        // gridSpacingEditor.synchToModel();
        gridSpacingEditor.setWidgetBorder(BorderFactory.createLoweredBevelBorder());
        gridSpacingEditor.validate();

        plotTypePanel.setLayout(GBL);
        plotTypePanel.setTitle("Plot Type");


        griddedSubsetPanel.setLayout(GBL);
        griddedSubsetPanel.setTitle("Gridded Subset");

        rect.disable();

        int index = 0;
        faultRadioButton = initRadioButton(faultRadioButton, FAULT, index++);
        allFaultsRadioButton = initRadioButton(allFaultsRadioButton, ALL_FAULTS, index++);
        surfaceRadioButton = initRadioButton(surfaceRadioButton, SURFACE, index++);
        //subSurfaceRadioButton = initRadioButton(subSurfaceRadioButton, SUB_SURFACE, index++);
        bothPlotsRadioButton = initRadioButton(bothPlotsRadioButton, BOTH_PLOTS, index++);
        subAndSurfaceRadioButton = initRadioButton(subAndSurfaceRadioButton, BOTH_SUB_AND_SURFACE_PLOTS, index++);
        threeDRadioButton = initRadioButton(threeDRadioButton, THREE_D, index++);


        plotButtonGroup.setSelected(faultRadioButton.getModel(), true);

        // gridSpacingPanel.add( gridSpacingEditor,  new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
            // ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0) );


        griddedSubsetPanel.add(rect,    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, emptyInsets, 1, 1) );

        gridControlsPanel.add(plotTypePanel,  new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, emptyInsets, 0, 0) );


        gridControlsPanel.add(gridSpacingEditor,  new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, emptyInsets, 0, 0) );
        gridControlsPanel.add(griddedSubsetPanel,  new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, emptyInsets, 0, 0) );


    }


    private void buildInfoPanel(){

        gridSpacingLabel.setKeyName("Grid Spacing ");
               rowsLabel.setKeyName("Num. Rows    ");
               colsLabel.setKeyName("Num. Cols.   ");
                dipLabel.setKeyName("Ave. Dip     ");
        upperSeismoLabel.setKeyName("Upper Seismo.");
        lowerSeismoLabel.setKeyName("Lower Seismo.");
          faultNameLabel.setKeyName("Fault Name   ");

        gridSpacingLabel.setLableForground(SystemColor.activeCaption);
               rowsLabel.setLableForground(SystemColor.activeCaption);
               colsLabel.setLableForground(SystemColor.activeCaption);
                dipLabel.setLableForground(SystemColor.activeCaption);
        upperSeismoLabel.setForeground(SystemColor.activeCaption);
        upperSeismoLabel.setLableForground(SystemColor.activeCaption);
        lowerSeismoLabel.setLableForground(SystemColor.activeCaption);
          faultNameLabel.setLableForground(SystemColor.activeCaption);

        int counter = 0;

        gridInfoPanel.add(faultNameLabel, new GridBagConstraints( 0, counter++, 1, 1, 1.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, emptyInsets, 0, 0 ) );
        gridInfoPanel.add(gridSpacingLabel, new GridBagConstraints( 0, counter++, 1, 1, 1.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, emptyInsets, 0, 0 ) );
        gridInfoPanel.add(rowsLabel, new GridBagConstraints( 0, counter++, 1, 1, 1.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, emptyInsets, 0, 0 ) );
        gridInfoPanel.add(colsLabel, new GridBagConstraints( 0, counter++, 1, 1, 1.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, emptyInsets, 0, 0 ) );
        gridInfoPanel.add(dipLabel, new GridBagConstraints( 0, counter++, 1, 1, 1.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, emptyInsets, 0, 0 ) );
        gridInfoPanel.add(upperSeismoLabel, new GridBagConstraints( 0, counter++, 1, 1, 1.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, emptyInsets, 0, 0 ) );
        gridInfoPanel.add(lowerSeismoLabel, new GridBagConstraints( 0, counter++, 1, 1, 1.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, emptyInsets, 0, 0 ) );
    }

}

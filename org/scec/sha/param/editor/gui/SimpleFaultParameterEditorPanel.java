package org.scec.sha.param.editor.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.scec.param.editor.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.param.*;
import org.scec.sha.param.*;
import org.scec.sha.param.editor.*;
import org.scec.sha.fault.*;
import org.scec.sha.surface.*;
import org.scec.data.Location;


/**
 * <p>Title: SimpleFaultParameterEditorPanel</p>
 * <p>Description: It is a more general parameter than just a Simple Fault Parameter
 * Editor because actually inside it creates an object of the EvenlyGriddedSurface.</p>
 * @author : Edward Field, Nitin Gupta and Vipin Gupta
 * @created : July 31, 2003
 * @version 1.0
 */

public class SimpleFaultParameterEditorPanel extends ParameterEditor
    implements ParameterChangeListener,
    ParameterChangeFailListener,
    ActionListener {


  /** Class name for debugging. */
  protected final static String C = "SimpleFaultParameterEditorPanel";
  /** If true print out debug statements. */
  protected final static boolean D = false;
  private Insets defaultInsets = new Insets( 4, 4, 4, 4 );


  //Final static declaration for the Parameters in the EvenlyGriddedSurface
  public static final String FAULT_NAME = "Fault Name";
  public static final String GRID_SPACING = "Grid Spacing";
  public static final String NUMBER_OF_FAULT_TRACE = "Num. of Fault Trace (Points)";
  public static final String NUM_DIPS = "Num. of Dips";
  public static final String DEPTH_PARAM_NAME = "Depth-";
  public static final String DIP_PARAM_NAME = "Dip-";
  public static final String LON_PARAM_NAME = "Lon-";
  public static final String LAT_PARAM_NAME = "Lat-";

  //Default Values for the param
  public static final int DEFAULT_NUM_FAULT_TRACE =3;
  public static final int DEFAULT_DIPS =1;
  public static final int latlonCols = 2;
  public static final double DEFAULT_GRID_SPACING = 1.0;

  // title of Parameter List Editor
  public static final String SIMPLE_FAULT_EDITOR_TITLE = new String("Simple Fault Editor");
  public static final String LAT_EDITOR_TITLE = "Latitudes";
  public static final String LON_EDITOR_TITLE = "Longitudes";
  public static final String DIP_EDITOR_TITLE = "Dips";
  public static final String DEPTH_EDITOR_TITLE = "Depths";
  public static final String FAULT_TYPE_TITLE = "Choose Fault Type";

  //static string for the Fault type supported
  public static final String FRANKEL ="Frankel's";
  public static final String STIRLING ="Stirling's";




  //boolean for the FaultName to be shown
  boolean showFaultName = false;

  //boolean for the Evenly Gridded Param
  boolean evenlyGriddedParamChange = false;

  /**
   * Declaration of the variables, so as to show the contents in table format
   */
  String latlonsName="Lat-Lon";
  String dipsName ="Dips";
  String depthsName = "Depths";

  /**
   * Some variable declarations
   */
  private double avgDip;
  private FaultTrace fltTrace;
  private double upperSies;
  private double lowerSies;


  //Reference to the EvenlyGriddedSurface Param
  private SimpleFaultParameter surfaceParam;

  /**
   * Paramter List for holding all parameters
   */
  private ParameterList parameterList = new ParameterList();

  /**
   * List to store the Lats
   */
  private ParameterList parameterListForLats ;

  /**
   * List to store the Lons
   */
  private ParameterList parameterListForLons ;
  /**
   * ParameterList for the Dips
   */
  private ParameterList parameterListForDips ;

  /**
   * ParameterList for the Depths
   */
  private ParameterList parameterListForDepths ;

  /**
   * ParameterListEditor for holding parameters
   */
  private ParameterListEditor editor;

  /**
   * ParameterListEditor for holding parameterListForLats
   */
  private ParameterListEditor editorForLats;

  /**
   * ParameterListEditor for holding parameterListForLons
   */
  private ParameterListEditor editorForLons;

  /**
   * ParameterListEditor for holding parameterListForDips
   */
  private ParameterListEditor editorForDips;

  /**
   * ParameterListEditor for holding parameterListForDepths
   */
  private ParameterListEditor editorForDepths;

  /**
   * IntegerParameterEditor for Number of Dips
   */
  private IntegerParameterEditor numDipsEditor;

  /**
   * StringParameter for the Fault type
   */
  private ConstrainedStringParameterEditor faultTypeEditor;

  //vectors to store the previous values for the lats, lons,dips and depths
  private Vector prevLats = new Vector();
  private Vector prevLons = new Vector();
  private Vector prevDepths = new Vector();
  private Vector prevDips = new Vector();

  public SimpleFaultParameterEditorPanel() {

  }

  /**
   * Set the values in the Parameters for the EvenlyGridded Surface
   */
  public void setParameter(ParameterAPI param)  {

    String S = C + ": Constructor(): ";
    if ( D ) System.out.println( S + "Starting:" );
    // remove the previous editor
    removeAll();
    surfaceParam = (SimpleFaultParameter) param;

    // make the params editor
    initParamListAndEditor();

    //by default the showFaultName is false so the fault name parameter is not visible
    if(!showFaultName)
      this.editor.getParameterEditor(this.FAULT_NAME).setVisible(false);


    //Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED,Color.white,Color.white,new Color(98, 98, 112),new Color(140, 140, 161));
    //button.setBorder(border);
    add(editor,new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

    /**
     * showing the Lats and Lons in the tabular format
     */
    add(this.editorForLats,new GridBagConstraints( 0, 1, 0, 1, 1.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.WEST, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

    add(this.editorForLons,new GridBagConstraints( 0, 1, 0, 1, 1.0, 0.0
        , GridBagConstraints.EAST, GridBagConstraints.EAST, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

    add(this.numDipsEditor,new GridBagConstraints( 0, 2, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

    add(this.editorForDips,new GridBagConstraints( 0, 3, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

    add(this.editorForDepths,new GridBagConstraints( 0, 4, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    add(this.faultTypeEditor,new GridBagConstraints( 0, 5, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );


    // All done
    if ( D ) System.out.println( S + "Ending:" );
  }

  /**
   * Called when the parameter has changed independently from
   * the editor, such as with the ParameterWarningListener.
   * This function needs to be called to to update
   * the GUI component ( text field, picklist, etc. ) with
   * the new parameter value.
   */
  public void synchToModel() {
    editor.synchToModel();
    editorForLats.synchToModel();
    editorForLons.synchToModel();
    numDipsEditor.synchToModel();
    editorForDips.synchToModel();
    editorForDepths.synchToModel();
    faultTypeEditor.synchToModel();
  }

  /**
   * creating the GUI parameters elemenst for the EvenlyGriddedSurface Param
   */
  private void initParamListAndEditor(){

    /**
     * creating the parameters for the parameterList that includes:
     * 1)name of the fault
     * 2)Grid Spacing
     * 3)Num of the Flt Trace
     * All these parameters are added to one param List and to one ParamList Editor
     */
    StringParameter faultName= new StringParameter(this.FAULT_NAME);
    faultName.addParameterChangeListener(this);
    DoubleParameter gridSpacing = new DoubleParameter(this.GRID_SPACING,0.0,100,new Double(this.DEFAULT_GRID_SPACING));
    gridSpacing.addParameterChangeListener(this);
    IntegerParameter numFltTrace = new IntegerParameter(this.NUMBER_OF_FAULT_TRACE,1,100,new Integer(this.DEFAULT_NUM_FAULT_TRACE));
    numFltTrace.addParameterChangeListener(this);
    parameterList.addParameter(faultName);
    parameterList.addParameter(gridSpacing);
    parameterList.addParameter(numFltTrace);
    this.editor = new ParameterListEditor(parameterList);
    editor.setTitle(SIMPLE_FAULT_EDITOR_TITLE);
    //creating the table for the Lat and Lon of the FltTrace
    setLatLon();
    //creating the Double parameter for the Dips
    IntegerParameter dip = new IntegerParameter(NUM_DIPS,new Integer(this.DEFAULT_DIPS));
    dip.addParameterChangeListener(this);
    try{
      numDipsEditor = new IntegerParameterEditor(dip);
    }catch(Exception ee){
      ee.printStackTrace();
    }
    //creating the table for the Dips
    setDips();

    //creating the table for the Depths
    setDepths();

    //create the String parameter if the dip is one
    Vector fltType = new Vector();
    fltType.add(this.FRANKEL);
    fltType.add(this.STIRLING);

    StringParameter faultTypeParam = new StringParameter(this.FAULT_TYPE_TITLE,fltType,(String)fltType.get(0));
    faultTypeParam.addParameterChangeListener(this);
    try{
    faultTypeEditor = new ConstrainedStringParameterEditor(faultTypeParam);

    }catch(Exception ee){
      ee.printStackTrace();
    }
  }

  /**
   * Sets the Lat and Lon for the faultTrace
   */
  private void setLatLon(){
    //System.out.println("Set Lat Lon called");
    int numFltTracePoints = ((Integer)parameterList.getParameter(this.NUMBER_OF_FAULT_TRACE).getValue()).intValue();
    DoubleParameter[] lat = new DoubleParameter[numFltTracePoints];
    DoubleParameter[] lon = new DoubleParameter[numFltTracePoints];

    //making the parameterList for the Lat and Lons
    this.parameterListForLats = new ParameterList();
    this.parameterListForLons = new ParameterList();

    //creating the editor for the lons
    for(int i=0;i<numFltTracePoints;++i){
      //System.out.println("i: "+i +"; Lat size: "+prevLats.size());
      //checks if any value exists in the vector for that lats parameter else just fill it up with a blank.
      if(prevLats.size()<(i+1))
        lat[i] = new DoubleParameter(LAT_PARAM_NAME+(i+1),-90.0,90.0,"Degrees");
      else
        lat[i] = new DoubleParameter(LAT_PARAM_NAME+(i+1),-90.0,90.0,"Degrees", (Double)prevLats.get(i));
      this.parameterListForLats.addParameter(lat[i]);
      lat[i].addParameterChangeListener(this);
    }
    editorForLats = new ParameterListEditor(parameterListForLats);
    editorForLats.setTitle(this.LAT_EDITOR_TITLE);

    //creating the editor for the Lons
    for(int i=0;i<numFltTracePoints;++i){
      //checks if any value exists in the vector for that lons parameter else just fill it up with a blank.
      if(prevLons.size() < (i+1))
        lon[i] = new DoubleParameter(this.LON_PARAM_NAME+(i+1),-360.0,360.0,"Degrees");
      else
        lon[i] = new DoubleParameter(this.LON_PARAM_NAME+(i+1),-360.0,360.0,"Degrees",(Double)prevLons.get(i));
      lon[i].addParameterChangeListener(this);
      this.parameterListForLons.addParameter(lon[i]);
    }
    editorForLons = new ParameterListEditor(parameterListForLons);
    editorForLons.setTitle(this.LON_EDITOR_TITLE);
    editorForLats.validate();
    editorForLats.revalidate();
    editorForLats.repaint();
    editorForLons.validate();
    editorForLons.revalidate();
    editorForLons.repaint();
  }

  public void setAll(String name, double gridSpacing, Vector lats, Vector lons,
                     Vector dips, Vector depths, String faultType) {
    parameterList.getParameter(this.FAULT_NAME).setValue(name);
    setAll(gridSpacing, lats, lons, dips, depths, faultType);
  }


  /**
   * This sets all the fault data needed to make a evenly discretized fault
   * @param gridSpacing
   * @param lats
   * @param lons
   * @param dips
   * @param depths
   * @param faultType
   */
  public void setAll(double gridSpacing, Vector lats, Vector lons,
                     Vector dips, Vector depths, String faultType) {
    int numFltPts = lats.size();
    int numDips = dips.size();

    if (lats.size() != lons.size())
      throw new RuntimeException(C+".setAll(): lats and lons Vectors must be the same size");

    if (dips.size() != depths.size()-1)
      throw new RuntimeException(C+".setAll(): size of dips Vector must one less than the depths Vector");

    if (dips.size()>1 && faultType.equals(this.FRANKEL))
      throw new RuntimeException(C+".setAll(): "+FRANKEL+" fault type can't be used if dips.size() > 1");

    parameterList.getParameter(this.GRID_SPACING).setValue(new Double(gridSpacing));
    parameterList.getParameter(this.NUMBER_OF_FAULT_TRACE).setValue(new Integer(numFltPts));
    numDipsEditor.getParameter().setValue(new Integer(numDips));

    for(int i=0;i<numFltPts;++i) {
      parameterListForLats.getParameter(LAT_PARAM_NAME+(i+1)).setValue(lats.get(i));
      parameterListForLons.getParameter(LON_PARAM_NAME+(i+1)).setValue(lons.get(i));
    }

    for(int i=0;i<numDips;++i)
      parameterListForDips.getParameter(DIP_PARAM_NAME+(i+1)).setValue(dips.get(i));

    for(int i=0;i<numDips+1;++i)
      parameterListForDepths.getParameter(DEPTH_PARAM_NAME+(i+1)).setValue(depths.get(i));

    faultTypeEditor.getParameter().setValue(faultType);

    synchToModel();
  }


  /**
   *Sets the Dip
   */
  private void setDips(){
    int numDips = ((Integer)this.numDipsEditor.getParameter().getValue()).intValue();

    DoubleParameter[] dip = new DoubleParameter[numDips];

    //making the parameterList for the Dips
    this.parameterListForDips = new ParameterList();
    for(int i=0;i<numDips;++i){
      //checks if any value exists in the vector for that dips parameter else just fill it up with a blank.
      if(prevDips.size() < (i+1))
        dip[i] = new DoubleParameter(DIP_PARAM_NAME+(i+1),0.0,90.0,"Degrees");
      else
        dip[i] = new DoubleParameter(DIP_PARAM_NAME+(i+1),0.0,90.0,"Degrees",(Double)prevDips.get(i));
      dip[i].addParameterChangeListener(this);
      this.parameterListForDips.addParameter(dip[i]);
    }
    editorForDips = new ParameterListEditor(parameterListForDips);
    editorForDips.setTitle(this.DIP_EDITOR_TITLE);
    editorForDips.validate();
    editorForDips.revalidate();
    editorForDips.repaint();
  }


  /**
   * Sets the Depths
   */
  private void setDepths(){
    int numDepths = ((Integer)this.numDipsEditor.getParameter().getValue()).intValue()+1;
    DoubleParameter[] depth = new DoubleParameter[numDepths];

    //making the parameterList for the Dips
    this.parameterListForDepths = new ParameterList();

    for(int i=0;i<numDepths;++i){
      //checks if any value exists in the vector for that Depth parameter else just fill it up with a blank.
      if(prevDepths.size() < (i+1))
        depth[i] = new DoubleParameter(DEPTH_PARAM_NAME+(i+1),0.0,99999.0,"Kms");
      else
        depth[i] = new DoubleParameter(DEPTH_PARAM_NAME+(i+1),0.0,99999.0,"Kms",(Double)prevDepths.get(i));
      depth[i].addParameterChangeListener(this);
      this.parameterListForDepths.addParameter(depth[i]);
    }
    editorForDepths = new ParameterListEditor(parameterListForDepths);
    editorForDepths.setTitle(this.DEPTH_EDITOR_TITLE);
    editorForDepths.validate();
    editorForDepths.revalidate();
    editorForDepths.repaint();
  }



  /**
   * Main GUI Initialization point. This block of code is updated by JBuilder
   * when using it's GUI Editor.
   */
  protected void jbInit() throws Exception {

    // Main component
    this.setLayout( new GridBagLayout());
  }

  /**
   * Sets the visibility for the fault name param
   * @param flag
   */
  public void setFaultNameVisible(boolean flag){
    showFaultName = flag;
    this.editor.getParameterEditor(FAULT_NAME).setVisible(flag);
  }

  /**
   *  This is the main function of this interface. Any time a control
   *  paramater or independent paramater is changed by the user in a GUI this
   *  function is called, and a paramater change event is passed in. This
   *  function then determines what to do with the information ie. show some
   *  paramaters, set some as invisible, basically control the paramater
   *  lists.
   *
   * @param  event
   */
  public void parameterChange( ParameterChangeEvent event ) {

    String S = C + ": parameterChange(): ";
    if ( D )
      System.out.println( "\n" + S + "starting: " );

    //System.out.println("Parameter Channged: "+event.getParameterName());

    //System.out.println("param change");
    String name1 = event.getParameterName();

    /**
     * If the changed parameter is the number of the fault trace param
     */
    if(name1.equalsIgnoreCase(this.NUMBER_OF_FAULT_TRACE)){
      prevLats.removeAllElements();
      prevLons.removeAllElements();
      //System.out.println("Inside the Fault Trace param change");
      ListIterator it = editorForLats.getParameterList().getParametersIterator();
      //saving the previous lat values in the vector
      while(it.hasNext()){
        ParameterAPI param = (ParameterAPI)it.next();
        if(param.getValue()!=null)
          prevLats.add(param.getValue());
      }
      //saving the previous lon values in the vector
      it = editorForLons.getParameterList().getParametersIterator();
      while(it.hasNext()){
        ParameterAPI param = (ParameterAPI)it.next();
        if(param.getValue()!=null)
          prevLons.add(param.getValue());
      }

      //removing the lats and Lons editor from the Applet
      remove(editorForLats);
      remove(editorForLons);
      //System.out.println("Calling the set LAt lon from Parameter change");
      //if the user has changed the values for the Number of the fault trace
      this.setLatLon();

      /**
       * showing the Lats and Lons in the tabular format
       * Adding the lats and lons editor to the Parameter editor
       */
      add(this.editorForLats,new GridBagConstraints( 0, 1, 0, 1, 1.0, 0.0
          , GridBagConstraints.WEST, GridBagConstraints.WEST, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

      add(this.editorForLons,new GridBagConstraints( 0, 1, 0, 1, 1.0, 0.0
          , GridBagConstraints.EAST, GridBagConstraints.EAST, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    }

    /**
     * If the changed parameter is the number of the Dips
     */
    if(name1.equalsIgnoreCase(this.NUM_DIPS)) {
      //System.out.println("Inside the Num dips param change");
      prevDips.removeAllElements();
      prevDepths.removeAllElements();
      ListIterator it = editorForDips.getParameterList().getParametersIterator();
      //saving the previous Dip values in the vector
      while(it.hasNext()){
        ParameterAPI param = (ParameterAPI)it.next();
        if(param.getValue()!=null)
          prevDips.add(param.getValue());
      }

      //saving the previous Depths values in the vector
      it = editorForDepths.getParameterList().getParametersIterator();
      while(it.hasNext()){
        ParameterAPI param = (ParameterAPI)it.next();
        if(param.getValue()!=null)
          prevDepths.add(param.getValue());
      }

      //removing the dips and depth editor from the applet
      remove(editorForDips);
      remove(editorForDepths);
      setDips();
      setDepths();

      //Adding the dips and depth editor to the parameter editor
      add(this.editorForDips,new GridBagConstraints( 0, 3, 1, 1, 1.0, 0.0
          , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

      add(this.editorForDepths,new GridBagConstraints( 0, 4, 1, 1, 1.0, 0.0
          , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

      if(((Integer)numDipsEditor.getParameter().getValue()).intValue() !=1)
        this.faultTypeEditor.setVisible(false);

      if(((Integer)numDipsEditor.getParameter().getValue()).intValue() ==1)
        this.faultTypeEditor.setVisible(true);
    }
    this.validate();
    this.revalidate();
    this.repaint();
    evenlyGriddedParamChange = true;
  }

  /**
   *  Shown when a Constraint error is thrown on a ParameterEditor
   *
   * @param  e  Description of the Parameter
   */
  public void parameterChangeFailed( ParameterChangeFailEvent e ) {

    String S = C + " : parameterChangeWarning(): ";
    if(D) System.out.println(S + "Starting");


    StringBuffer b = new StringBuffer();

    ParameterAPI param = ( ParameterAPI ) e.getSource();
    ParameterConstraintAPI constraint = param.getConstraint();
    String oldValueStr = e.getOldValue().toString();
    String badValueStr = e.getBadValue().toString();
    String name = param.getName();


    b.append( "The value ");
    b.append( badValueStr );
    b.append( " is not permitted for '");
    b.append( name );
    b.append( "'.\n" );

    b.append( "Resetting to ");
    b.append( oldValueStr );
    b.append( ". The constraints are: \n");
    b.append( constraint.toString() );
    b.append( "\n" );

    JOptionPane.showMessageDialog(
        this, b.toString(),
        "Cannot Change Value", JOptionPane.INFORMATION_MESSAGE
        );

    if(D) System.out.println(S + "Ending");

  }

  /**
   * This function is called when Update button is pressed
   *
   * @param ae
   */
  public void actionPerformed(ActionEvent ae ) {
    try{
      setEvenlyGriddedSurfaceFromParams();
    }catch(RuntimeException e){
      JOptionPane.showMessageDialog(this,e.getMessage(),"Incorrect Values",JOptionPane.ERROR_MESSAGE);
    }
  }


  public void setEvenlyGriddedSurfaceFromParams()throws RuntimeException{

    //checks if any parameter has been only then updates the Griddedsurface
    if(this.evenlyGriddedParamChange){
      // EvenlyGriddedSurface
      GriddedFaultFactory fltFactory = null;
      //gets the faultName
      String fltName = (String)parameterList.getParameter(this.FAULT_NAME).getValue();
      //creates the fault trace data
      FaultTrace fltTrace = new FaultTrace(fltName);

      //initialising the vectors for the lats, lons, depths and dips
      Vector lats = new Vector();
      Vector lons = new Vector();
      Vector depths = new Vector();
      Vector dips = new Vector();
      //getting the number of  fault trace
      int fltTracePoints = ((Integer)this.parameterList.getParameter(this.NUMBER_OF_FAULT_TRACE).getValue()).intValue();
      //getting the number of dips
      int numDips = ((Integer)this.numDipsEditor.getParameter().getValue()).intValue();

      //adding the latitudes to the Vector
      for(int i=0;i<fltTracePoints;++i){
        Double latLocation =(Double)this.editorForLats.getParameterList().getParameter(this.LAT_PARAM_NAME+(i+1)).getValue();
        lats.add(latLocation);
      }

      //adding the longitudes to the Vector
      for(int i=0;i<fltTracePoints;++i){
        Double lonLocation =(Double)this.editorForLons.getParameterList().getParameter(this.LON_PARAM_NAME+(i+1)).getValue();
        lons.add(lonLocation);
      }

      //variable added to store the previous Depth (to make sure they're in ascending order)
      double prevDepth=((Double)this.editorForDepths.getParameterList().getParameter(this.DEPTH_PARAM_NAME+("1")).getValue()).doubleValue();

      //adding the depths(equal to numDips +1) to the Vector
      for(int i=0;i<=numDips;++i){
        Double depthLocation = (Double)this.editorForDepths.getParameterList().getParameter(this.DEPTH_PARAM_NAME+(i+1)).getValue();
        depths.add(depthLocation);
        //compares the depths, becuase depths should be entered in the increasing order
        if(depthLocation.doubleValue() < prevDepth)
          throw new RuntimeException("Depths should be entered in increasing order");
        prevDepth = depthLocation.doubleValue();
      }

      //adding the dips to the vector
      for(int i=0;i<numDips;++i){
        Double dipLocation = (Double)this.editorForDips.getParameterList().getParameter(this.DIP_PARAM_NAME+(i+1)).getValue();
        dips.add(dipLocation);
      }

      //adding the locations to the FaultTrace
      for(int i=0;i<fltTracePoints;++i){
        double lat = ((Double)lats.get(i)).doubleValue();
        double lon = ((Double)lons.get(i)).doubleValue();
        double depth = ((Double)depths.get(0)).doubleValue();
        Location loc = new Location(lat,lon,depth);
        fltTrace.addLocation(loc);
      }
      this.fltTrace = fltTrace;

      if(D)
        System.out.println("Fault-trace length (km) = "+fltTrace.getTraceLength());

      //getting the gridSpacing
      double gridSpacing = ((Double)this.parameterList.getParameter(this.GRID_SPACING).getValue()).doubleValue();

      /**
       * Checking for the number of Dips.
       * If the number of dip is equal to 1 then give the option to the user
       * to make the FaultType (Frankel or Stirling) parameter visible to the
       * user. Else no choice is given to the user and make the object of the
       * SimpleListricGriddedFaultFactory.
       */
      if(numDips ==1){
        //gets the dip as the only value in the vector of dips
        double dip = ((Double)dips.get(0)).doubleValue();
        this.avgDip =dip;
        //gets the fault type
        String fltType = (String)this.faultTypeEditor.getParameter().getValue();
        //System.out.println("Fault-type: "+fltType);
        //gets the upperSiesDepth and LowerSiesDepth
        double upperSiesDepth =((Double)depths.get(0)).doubleValue();
        double lowerSiesDepth =((Double)depths.get(1)).doubleValue();
        upperSies = upperSiesDepth;
        lowerSies = lowerSiesDepth;
        //make the object of the FrankelGriddedFaultFactory
        if(fltType.equalsIgnoreCase(this.FRANKEL)){
          fltFactory = new FrankelGriddedFaultFactory(fltTrace,dip,upperSiesDepth,lowerSiesDepth,gridSpacing);
        }
        //make the object for the Stirling gridded fault
        if(fltType.equalsIgnoreCase(this.STIRLING)){
          fltFactory = new StirlingGriddedFaultFactory(fltTrace,dip,upperSiesDepth,lowerSiesDepth,gridSpacing);
        }
      }
      else{
        //make the object for the simple Listric fault
        fltFactory = new SimpleListricGriddedFaultFactory(fltTrace,dips,depths,gridSpacing);
      }
      //gets the griddedsurface from the faultFactory and sets the Value for the
      //SimpleFaultParameter
      surfaceParam.setValue((EvenlyGriddedSurface)fltFactory.getGriddedSurface());

      if(D) {
        EvenlyGriddedSurface surf = (EvenlyGriddedSurface)fltFactory.getGriddedSurface();
        for(int i=0;i<surf.getNumCols();i++)
          for(int k=0;k<surf.getNumRows();k++)
            System.out.println(surf.getLocation(k,i).toString());
      }

      //make the change flag to be false
      this.evenlyGriddedParamChange = false;
    }
  }





  public FaultTrace getFaultTrace(){
    return fltTrace;
  }

  public double getUpperSiesmogenicDepth(){
    return upperSies;
  }

  public double getLowerSiesmogenicDepth(){
    return lowerSies;
  }

  public String getFaultName(){
    return (String)parameterList.getParameter(this.FAULT_NAME).getValue();
  }


/* NED REPLACED THESE WITH THE setAll(*) method

  public void setLatitudes(Vector lats){
    int numFltTracePoints = ((Integer)parameterList.getParameter(this.NUMBER_OF_FAULT_TRACE).getValue()).intValue();
    for(int i=0;i<numFltTracePoints;++i)
      this.parameterListForLats.getParameter(LAT_PARAM_NAME+(i+1)).setValue(lats.get(i));

  }

  public void setLongitudes(Vector lons){
    int numFltTracePoints = ((Integer)parameterList.getParameter(this.NUMBER_OF_FAULT_TRACE).getValue()).intValue();
    for(int i=0;i<numFltTracePoints;++i)
      this.parameterListForLons.getParameter(LON_PARAM_NAME+(i+1)).setValue(lons.get(i));

  }

  public void setDips(Vector dips){
    int numDips = ((Integer)this.numDipsEditor.getParameter().getValue()).intValue();
    for(int i=0;i<numDips;++i)
      this.parameterListForDips.getParameter(DIP_PARAM_NAME+(i+1)).setValue(dips.get(i));
  }


  public void setDepths(Vector depths){
    int numDepths = ((Integer)this.numDipsEditor.getParameter().getValue()).intValue()+1;
    for(int i=0;i<numDepths;++i)
      this.parameterListForDepths.getParameter(DEPTH_PARAM_NAME+(i+1)).setValue(depths.get(i));
  }



  public void setFaultName(String name){
    this.parameterList.getParameter(this.FAULT_NAME).setValue(name);
  }

  public void setGridSpacing(double value){
    this.parameterList.getParameter(this.GRID_SPACING).setValue(new Double(value));
  }

  public void setNumFaultTracePoints(int value){
    this.parameterList.getParameter(this.NUMBER_OF_FAULT_TRACE).setValue(new Integer(value));
  }

  public void setNumDips(int value){
    this.numDipsEditor.getParameter().setValue(new Integer(value));
  }
*/

}




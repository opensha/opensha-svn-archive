package org.scec.sha.param;


import java.util.*;

import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.exceptions.ParameterException;
import org.scec.sha.fault.*;
import org.scec.data.Location;


/**
 * <p>Title: SimpleFaultParameterCalculator</p>
 * <p>Description: This class acts as the intermediatory between SimpleFaultParameter
 * and its editor.It extends the Dependent Parameter class so as to save the list of
 * visible parameters. Most of the editor functionality has been embedded into this
 * class because we want to make all the functionality available to the user if
 * he does not want to use the GUI components.
 * This class is extended by SimpleFaultParameter.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class SimpleFaultParameterCalculator extends DependentParameter implements java.io.Serializable{

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


 //static string declaration for the Lat, Lon , Dip and Depth Paramater (ParameterListParameter) names title
  public static final String LAT_TITLE = "Latitudes";
  public static final String LON_TITLE = "Longitudes";
  public static final String DIP_TITLE = "Dips";
  public static final String DEPTH_TITLE = "Depths";

  //Fault Type Param Name
  public static final String FAULT_TYPE_TITLE = "Choose Fault Type";

  //static string for the Fault type supported
  public static final String FRANKEL ="Frankel's";
  public static final String STIRLING ="Stirling's";


  /**
   * Some variable declarations
   */
  private double avgDip;
  private FaultTrace fltTrace;
  private double upperSies;
  private double lowerSies;


  /**
   * Paramter List for holding all parameters
   */
  private ParameterList parameterList = new ParameterList();

  /**
   * List to store the Lats
   */
  private ParameterListParameter parameterListParameterForLats ;

  /**
   * List to store the Lons
   */
  private ParameterListParameter parameterListParameterForLons ;
  /**
   * ParameterList for the Dips
   */
  private ParameterListParameter parameterListParameterForDips ;

  /**
   * ParameterList for the Depths
   */
  private ParameterListParameter parameterListParameterForDepths ;

  /**
   * DoubleParameter for Ave. Dip Direction, if the person has selected
   * Stirling Fault Model
   */
  public static final String DIP_DIRECTION_PARAM_NAME = "Ave. Dip Direction";
  //used only when stirling fault model is selected
  private static final Double DEFAULT_DIP_DIRECTION = null;
  private static final String DIP_DIRECTION_PARAM_UNITS = "degrees";
  private DoubleParameter dipDirectionParam = new DoubleParameter(DIP_DIRECTION_PARAM_NAME,
      new Double(0),new Double(360),DIP_DIRECTION_PARAM_UNITS,DEFAULT_DIP_DIRECTION);

  //creating the Double parameter for the Dips
  private IntegerParameter numDipParam = new IntegerParameter(NUM_DIPS,new Integer(this.DEFAULT_DIPS));

  //creating the StringParameter for the FaultType
  StringParameter faultTypeParam;

  //vectors to store the previous values for the lats, lons,dips and depths
  private Vector prevLats = new Vector();
  private Vector prevLons = new Vector();
  private Vector prevDepths = new Vector();
  private Vector prevDips = new Vector();

  /**
   *  No constraints specified for this parameter. Sets the name of this
   *  parameter.
   *
   * @param  name  Name of the parameter
   */
  public SimpleFaultParameterCalculator(String name) {
    super(name,null,null,null);
  }

  /**
   * No constraints specified, all values allowed. Sets the name and value.
   *
   * @param  name   Name of the parameter
   * @param  surface  EvenlyGriddedSurface  object
   */
  public SimpleFaultParameterCalculator(String name, EvenlyGriddedSurface surface){
    super(name,null,null,surface);
  }

  /**
   *  Compares the values to if this is less than, equal to, or greater than
   *  the comparing objects.
   *
   * @param  obj                     The object to compare this to
   * @return                         -1 if this value < obj value, 0 if equal,
   *      +1 if this value > obj value
   * @exception  ClassCastException  Is thrown if the comparing object is not
   *      a DoubleParameter, or DoubleDiscreteParameter.
   */
  public int compareTo( Object obj ) throws UnsupportedOperationException {
    throw new java.lang.UnsupportedOperationException("This method not implemented yet");
  }


  /**
   *  Set's the parameter's value.
   *
   * @param  value                 The new value for this Parameter
   * @throws  ParameterException   Thrown if the object is currenlty not
   *      editable
   * @throws  ConstraintException  Thrown if the object value is not allowed
   */
  public void setValue( EvenlyGriddedSurface value ) throws ParameterException {
    setValue( (Object) value );
  }

  /**
   *  Compares value to see if equal.
   *
   * @param  obj                     The object to compare this to
   * @return                         True if the values are identical
   * @exception  ClassCastException  Is thrown if the comparing object is not
   *      a DoubleParameter, or DoubleDiscreteParameter.
   */
  public boolean equals( Object obj ) throws UnsupportedOperationException {
    throw new java.lang.UnsupportedOperationException("This method not implemented yet");

  }


  /**
   *  Returns a copy so you can't edit or damage the origial.
   *
   * @return    Exact copy of this object's state
   */
  public Object clone() throws UnsupportedOperationException {
    throw new java.lang.UnsupportedOperationException("This method not implemented yet");
  }


  /**
   *
   * creating the parameters for the parameterList that includes:
   * 1)name of the fault
   * 2)Grid Spacing
   * 3)Num of the Flt Trace
   * All the above parameters are added to one param List and to one ParamList Editor
   * This is only few parameters that compose the SimpleFaultParameter, which is a complex
   * parameter comprising of other parameters too.
   */
  public void initParamList(){

    StringParameter faultName= new StringParameter(this.FAULT_NAME);
    DoubleParameter gridSpacing = new DoubleParameter(this.GRID_SPACING,0.0,100,new Double(this.DEFAULT_GRID_SPACING));
    IntegerParameter numFltTrace = new IntegerParameter(this.NUMBER_OF_FAULT_TRACE,1,100,new Integer(this.DEFAULT_NUM_FAULT_TRACE));
    parameterList.addParameter(faultName);
    parameterList.addParameter(gridSpacing);
    parameterList.addParameter(numFltTrace);
    //create the String parameter if the dip is one
    Vector fltType = new Vector();
    fltType.add(this.FRANKEL);
    fltType.add(this.STIRLING);
    faultTypeParam = new StringParameter(this.FAULT_TYPE_TITLE,fltType,(String)fltType.get(0));
  }


  /**
   *
   * @returns the ParameterList comprising of following parameters:
   * 1)name of the fault
   * 2)Grid Spacing
   * 3)Num of the Flt Trace
   */
  public ParameterList getFaultTraceParamList(){
    return parameterList;
  }


  /**
   * returns ParameterListParameter
   * @returns the Parameter comprising of all the latitudes
   */
  public ParameterAPI getLatParam(){
    return parameterListParameterForLats;
  }

  /**
   * returns ParameterListParameter
   * @returns the Parameter comprising of all the longitudes
   */
  public ParameterAPI getLonParam(){
    return parameterListParameterForLons;
  }

  /**
   * returns ParameterListParameter
   * @returns the Parameter comprising of all the depths
   */
  public ParameterAPI getDepthParam(){
    return parameterListParameterForDepths;
  }

  /**
   * returns ParameterListParameter
   * @returns the Parameter comprising of all the dips
   */
  public ParameterAPI getDipParam(){
    return parameterListParameterForDips;
  }

  /**
   *
   * @returns the parameter for the number of Dips
   */
  public ParameterAPI getNumDipParam(){
    return numDipParam;
  }

  /**
   *
   * @returns the parameter for selected fault type
   */
  public ParameterAPI getFaultTypeParam(){
    return faultTypeParam;
  }

  /**
   *
   * @returns the parameter for Dip direction
   */
  public ParameterAPI getDipDirectionParam(){
    return dipDirectionParam;
  }


  /**
   * Creates Latitude and Longitude parameters based on the number of the faultTrace.
   * If the user has already specified the values for these parameters once ,it saves
   * those values for future reference. So that when the number of fault-trace changes
   * user does not always have to fill in all the values.
   */
  public void initLatLonParamList(){

    int numFltTracePoints = ((Integer)parameterList.getParameter(this.NUMBER_OF_FAULT_TRACE).getValue()).intValue();
    DoubleParameter[] lat = new DoubleParameter[numFltTracePoints];
    DoubleParameter[] lon = new DoubleParameter[numFltTracePoints];

    //making the parameterList for the Lat and Lons
    ParameterList parameterListForLats = new ParameterList();
    ParameterList parameterListForLons = new ParameterList();

    //creating the editor for the lons
    for(int i=0;i<numFltTracePoints;++i){
      //checks if any value exists in the vector for that lats parameter else just fill it up with a blank.
      if(prevLats.size()<(i+1))
        lat[i] = new DoubleParameter(LAT_PARAM_NAME+(i+1),-90.0,90.0,"Degrees");
      else
        lat[i] = new DoubleParameter(LAT_PARAM_NAME+(i+1),-90.0,90.0,"Degrees", (Double)prevLats.get(i));
      parameterListForLats.addParameter(lat[i]);

    }
    parameterListParameterForLats = new ParameterListParameter(LAT_TITLE,parameterListForLats);

    //creating the editor for the Lons
    for(int i=0;i<numFltTracePoints;++i){
      //checks if any value exists in the vector for that lons parameter else just fill it up with a blank.
      if(prevLons.size() < (i+1))
        lon[i] = new DoubleParameter(this.LON_PARAM_NAME+(i+1),-360.0,360.0,"Degrees");
      else
        lon[i] = new DoubleParameter(this.LON_PARAM_NAME+(i+1),-360.0,360.0,"Degrees",(Double)prevLons.get(i));

      parameterListForLons.addParameter(lon[i]);
    }
    parameterListParameterForLons = new ParameterListParameter(LON_TITLE,parameterListForLons);
  }


  /**
   * Creates Latitude and Longitude parameters based on the number of the Dips.
   * If the user has already specified the values for these parameters once ,it saves
   * those values for future reference. So that when the number of dips changes
   * user does not always have to fill in all the values.
   */
  public void initDipParamList(){
    int numDips = ((Integer)numDipParam.getValue()).intValue();

    DoubleParameter[] dip = new DoubleParameter[numDips];

    //making the parameterList for the Dips
    ParameterList parameterListForDips = new ParameterList();
    for(int i=0;i<numDips;++i){
      //checks if any value exists in the vector for that dips parameter else just fill it up with a blank.
      if(prevDips.size() < (i+1))
        dip[i] = new DoubleParameter(DIP_PARAM_NAME+(i+1),0.0,90.0,"Degrees");
      else
        dip[i] = new DoubleParameter(DIP_PARAM_NAME+(i+1),0.0,90.0,"Degrees",(Double)prevDips.get(i));
      parameterListForDips.addParameter(dip[i]);
    }
    parameterListParameterForDips = new ParameterListParameter(DIP_TITLE,parameterListForDips);
  }


  /**
   * Creates Latitude and Longitude parameters based on the number of the Dips.
   * If the user has already specified the values for these parameters once ,it saves
   * those values for future reference. So that when the number of dips changes
   * user does not always have to fill in all the values.
   * Number of Depths are always one more than the number of dips
   */
  public void initDepthParamList(){
    int numDepths = ((Integer)numDipParam.getValue()).intValue()+1;
    DoubleParameter[] depth = new DoubleParameter[numDepths];

    //making the parameterList for the Dips
    ParameterList parameterListForDepths = new ParameterList();

    for(int i=0;i<numDepths;++i){
      //checks if any value exists in the vector for that Depth parameter else just fill it up with a blank.
      if(prevDepths.size() < (i+1))
        depth[i] = new DoubleParameter(DEPTH_PARAM_NAME+(i+1),0.0,99999.0,"Kms");
      else
        depth[i] = new DoubleParameter(DEPTH_PARAM_NAME+(i+1),0.0,99999.0,"Kms",(Double)prevDepths.get(i));
      parameterListForDepths.addParameter(depth[i]);
    }
    parameterListParameterForDepths = new ParameterListParameter(DEPTH_TITLE,parameterListForDepths);
  }


  /**
   * creates the evenly gridded surface from the fault parameter.
   * This function has to be called explicitly in order to Create/Update
   * the  gridded surface , if user is not using the GUI.
   * @throws RuntimeException
   */
  public void setEvenlyGriddedSurfaceFromParams()throws RuntimeException{

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
    int numDips = ((Integer)numDipParam.getValue()).intValue();

    //adding the latitudes to the Vector
    for(int i=0;i<fltTracePoints;++i){
      Double latLocation =(Double)parameterListParameterForLats.getParameter().getParameter(this.LAT_PARAM_NAME+(i+1)).getValue();
      lats.add(latLocation);
    }

    //adding the longitudes to the Vector
    for(int i=0;i<fltTracePoints;++i){
      Double lonLocation =(Double)parameterListParameterForLons.getParameter().getParameter(this.LON_PARAM_NAME+(i+1)).getValue();
      lons.add(lonLocation);
    }

    //variable added to store the previous Depth (to make sure they're in ascending order)
    double prevDepth=((Double)parameterListParameterForDepths.getParameter().getParameter(this.DEPTH_PARAM_NAME+("1")).getValue()).doubleValue();

    //adding the depths(equal to numDips +1) to the Vector
    for(int i=0;i<=numDips;++i){
      Double depthLocation = (Double)parameterListParameterForDepths.getParameter().getParameter(this.DEPTH_PARAM_NAME+(i+1)).getValue();
      depths.add(depthLocation);
      //compares the depths, becuase depths should be entered in the increasing order
      if(depthLocation.doubleValue() < prevDepth)
        throw new RuntimeException("Depths should be entered in increasing order");
      prevDepth = depthLocation.doubleValue();
    }

    //adding the dips to the vector
    for(int i=0;i<numDips;++i){
      Double dipLocation = (Double)parameterListParameterForDips.getParameter().getParameter(this.DIP_PARAM_NAME+(i+1)).getValue();
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
      String fltType = (String)this.faultTypeParam.getValue();
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
        //checking to see if the Dip Direction Param value is null then assign default Double.NaN
        //else assign the dip direction value.
        Double aveDipDir = (Double)dipDirectionParam.getValue();
        if(aveDipDir == null)
          ((StirlingGriddedFaultFactory)fltFactory).setAveDipDir(Double.NaN);
        else
          ((StirlingGriddedFaultFactory)fltFactory).setAveDipDir(aveDipDir.doubleValue());
      }
    }
    else{
      //make the object for the simple Listric fault
      fltFactory = new SimpleListricGriddedFaultFactory(fltTrace,dips,depths,gridSpacing);
    }
    //gets the griddedsurface from the faultFactory and sets the Value for the
    //SimpleFaultParameter
    setValue((EvenlyGriddedSurface)fltFactory.getGriddedSurface());

    if(D) {
      EvenlyGriddedSurface surf = (EvenlyGriddedSurface)fltFactory.getGriddedSurface();
      for(int i=0;i<surf.getNumCols();i++)
        for(int k=0;k<surf.getNumRows();k++)
          System.out.println(surf.getLocation(k,i).toString());
    }
  }

  /**
   *
   * @returns the fault trace
   */
  public FaultTrace getFaultTrace(){
    return fltTrace;
  }

  /**
   *
   * @returns the Upper Siesmogenic depth
   */
  public double getUpperSiesmogenicDepth(){
    return upperSies;
  }

  /**
   *
   * @returns the Lower Siesmogenic depth
   */
  public double getLowerSiesmogenicDepth(){
    return lowerSies;
  }

  /**
   *
   * @returns the name of the fault
   */
  public String getFaultName(){
    return (String)parameterList.getParameter(this.FAULT_NAME).getValue();
  }

  /**
   * Sets the Average Dip Direction for the evenly discritized fault.
   * By Default its value is NaN and its value can only be set if one has
   * selected the Fault type to be Stirling
   */
  public void setDipDirection(double value){
    if(((String)faultTypeParam.getValue()).equals(STIRLING))
      dipDirectionParam.setValue(new Double(value));
  }



  /**
   * This sets all the fault data needed to make a evenly discretized fault
   * @param name : Name of the fault
   * @param gridSpacing
   * @param lats : Vector of Latitudes for the discretized fault
   * @param lons : Vector of Longitudes for the discretized fault
   * @param dips : Vector of Dips
   * @param depths : Vector of Depths, which are one more then the number of dips
   * @param faultType : STIRLING or FRANKEL fault
   */
  public void setAll(String name, double gridSpacing, Vector lats, Vector lons,
                     Vector dips, Vector depths, String faultType) {
    parameterList.getParameter(SimpleFaultParameterCalculator.FAULT_NAME).setValue(name);
    setAll(gridSpacing, lats, lons, dips, depths, faultType);
  }


  /**
   * This sets all the fault data needed to make a evenly discretized fault
   * @param gridSpacing
   * @param lats : Vector of Latitudes for the discretized fault
   * @param lons : Vector of Longitudes for the discretized fault
   * @param dips : Vector of Dips
   * @param depths : Vector of Depths, which are one more then the number of dips
   * @param faultType : STIRLING or FRANKEL fault
   */
  public void setAll(double gridSpacing, Vector lats, Vector lons,
                     Vector dips, Vector depths, String faultType) {
    int numFltPts = lats.size();
    int numDips = dips.size();

    if (lats.size() != lons.size())
      throw new RuntimeException(C+".setAll(): lats and lons Vectors must be the same size");

    if (dips.size() != depths.size()-1)
      throw new RuntimeException(C+".setAll(): size of dips Vector must one less than the depths Vector");

    if (dips.size()>1 && faultType.equals(SimpleFaultParameterCalculator.FRANKEL))
      throw new RuntimeException(C+".setAll(): "+SimpleFaultParameterCalculator.FRANKEL+" fault type can't be used if dips.size() > 1");

    numDipParam.setValue(new Integer(numDips));

    parameterList.getParameter(SimpleFaultParameterCalculator.GRID_SPACING).setValue(new Double(gridSpacing));
    parameterList.getParameter(SimpleFaultParameterCalculator.NUMBER_OF_FAULT_TRACE).setValue(new Integer(numFltPts));
    numDipParam.setValue(new Integer(numDips));

    for(int i=0;i<numFltPts;++i) {
      parameterListParameterForLats.getParameter().getParameter(SimpleFaultParameterCalculator.LAT_PARAM_NAME+(i+1)).setValue(lats.get(i));
      parameterListParameterForLons.getParameter().getParameter(SimpleFaultParameterCalculator.LON_PARAM_NAME+(i+1)).setValue(lons.get(i));
    }

    for(int i=0;i<numDips;++i)
      parameterListParameterForDips.getParameter().getParameter(SimpleFaultParameterCalculator.DIP_PARAM_NAME+(i+1)).setValue(dips.get(i));

    for(int i=0;i<numDips+1;++i)
      parameterListParameterForDepths.getParameter().getParameter(SimpleFaultParameterCalculator.DEPTH_PARAM_NAME+(i+1)).setValue(depths.get(i));

    faultTypeParam.setValue(faultType);
  }

  /**
   *
   * @returns the Vector containing the values for all the specified Latitudes
   */
  public Vector getLatParamVals(){
   return prevLats;
  }

  /**
   *
   * @returns the Vector containing the values for all the specified Longitudes
   */
  public Vector getLonParamVals(){
   return prevLons;
  }

  /**
   *
   * @returns the Vector containing the values for all the specified Dips
   */
  public Vector getDipParamVals(){
   return prevDips;
  }

  /**
   *
   * @returns the Vector containing the values for all the specified Depths
   */
  public Vector getDepthParamVals(){
   return prevDepths;
  }
}
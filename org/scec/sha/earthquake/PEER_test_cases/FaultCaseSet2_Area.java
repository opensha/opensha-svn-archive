package org.scec.sha.earthquake.PEER_test_cases;


import java.util.Vector;
import java.util.Iterator;


import org.scec.data.TimeSpan;
import org.scec.data.Location;

import org.scec.param.*;
import org.scec.sha.fault.*;
import org.scec.sha.surface.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_CharEqkSource;
import org.scec.sha.magdist.*;
import org.scec.param.event.*;


/**
 * <p>Title: FaultCaseSet1_Fault</p>
 * <p>Description: Fault 1 Equake rupture forecast. The Peer Group Test cases </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * Date : Oct 24, 2002
 * @version 1.0
 */

public class FaultCaseSet2_Area extends EqkRupForecast
    implements ParameterChangeListener{

  /**
   * @todo variables
   */
  //for Debug purposes
  private static String  C = new String("FaultCaseTest1_Fault");
  private boolean D = false;

  /**
   * timespan field in yrs for now(but have to ultimately make it a TimeSpan class variable
   */
  private double timeSpan;
  private TimeSpan time;

  /**
   * definition of the vectors for storing the sources
   */
  private Vector area_EqkSources = new Vector();


  //Param Name
  private final static String GRID_PARAM_NAME =  "Area Grid Spacing (km)";
  private final static String DEPTH_LOWER_PARAM_NAME =  "Depth Lower(km)";
  private final static String DEPTH_UPPER_PARAM_NAME =  "Depth Upper(km)";
  private final static String MAG_DIST_PARAM_NAME = "Area Mag Dist";

  //timespan Variable
  private final static String TIMESPAN_PARAM_NAME = "Timespan(#yrs)";

  // default grid spacing is 1km
  private Double DEFAULT_GRID_VAL = new Double(1);
  // Number of locations in this area
  private int NUM_LOCATIONS = 90;

  // values for Seismo depths
  private double UPPER_SEISMO_DEPTH = 0.0;
  private double LOWER_SEISMO_DEPTH = 25.0;

  // area name
  private String FAULT1_NAME = new String("Area");

  //top loaction
  private Location []area_Location = new Location[90];


  // add the grid spacing field
  DoubleParameter gridParam=new DoubleParameter(this.GRID_PARAM_NAME,this.DEFAULT_GRID_VAL);

  //add Depth Lower
  DoubleParameter depthLowerParam = new DoubleParameter(DEPTH_LOWER_PARAM_NAME);

  //add depth Upper
  DoubleParameter depthUpperParam = new DoubleParameter(DEPTH_UPPER_PARAM_NAME);

  //add the timespan parameter
  DoubleParameter timespanParam = new DoubleParameter(this.TIMESPAN_PARAM_NAME);

  //adding the supported MagDists
  Vector supportedMagDists=new Vector();

  //Mag Freq Dist Parameter
  MagFreqDistParameter magDistParam ;

  // Fault trace
  FaultTrace faultTrace;

  // private declaration of the flag to check if any parameter has been changed from its original value.
  private boolean  parameterChangeFlag = true;


  /**
   * This constructor constructs the source
   *
   * No argument constructor
   */
  public FaultCaseSet2_Area() {

    /* Now make the source in Fault 1 */
    adjustableParams.addParameter(gridParam);
    adjustableParams.addParameter(depthLowerParam);
    adjustableParams.addParameter(depthUpperParam);
    adjustableParams.addParameter(timespanParam);

    // adding the supported MagDistclasses
    supportedMagDists.add(GutenbergRichterMagFreqDist.NAME);
    magDistParam = new MagFreqDistParameter(MAG_DIST_PARAM_NAME, supportedMagDists);
    //add the magdist parameter
    adjustableParams.addParameter(this.magDistParam);


    // listen for change in the parameters
    gridParam.addParameterChangeListener(this);
    depthLowerParam.addParameterChangeListener(this);
    depthUpperParam.addParameterChangeListener(this);
    timespanParam.addParameterChangeListener(this);
    magDistParam.addParameterChangeListener(this);

    // set the fault locations
    this.setLocations();
  }




  /**
   *  This is the main function of this interface. Any time a control
   *  paramater or independent paramater is changed by the user in a GUI this
   *  function is called, and a paramater change event is passed in.
   *
   *  This sets the flag to indicate that the sources need to be updated
   *
   * @param  event
   */
  public void parameterChange( ParameterChangeEvent event ) {

    parameterChangeFlag=true;
  }


  /**
   * update the sources based on the user paramters, only when user has changed any parameter
   */
  public void updateGUI(){
    if(parameterChangeFlag) {


      // first build the fault trace, then add
      // add the location to the trace

      faultTrace = new FaultTrace(FAULT1_NAME);
      for(int i=0;i<NUM_LOCATIONS;++i)
        faultTrace.addLocation((Location)this.area_Location[i].clone());


      /*** FIX  FIX FIX FIX TO BE DONE **********/

      // value of gridspacing has been set to 1 km
     /*  SimpleFaultData faultData= new SimpleFaultData(dipValue,LOWER_SEISMO_DEPTH,UPPER_SEISMO_DEPTH,faultTrace);

       FrankelGriddedFaultFactory factory =
           new FrankelGriddedFaultFactory(faultData,((Double)gridParam.getValue()).doubleValue());

           // get the gridded surface
       GriddedSurfaceAPI surface = factory.getGriddedSurface();
       source = new  Set2_Area_Source((IncrementalMagFreqDist)magDistParam.getValue(),((Double)rakeParam.getValue()).doubleValue() ,
                                       ((Double)offsetParam.getValue()).doubleValue(),(EvenlyGriddedSurface)surface);
                                       */
    }
    parameterChangeFlag = false;
  }




  /**
   * sets the timeSpan field
   * @param yrs : have to be modfied from the double varible to the timeSpan field variable
   */
  public void setTimeSpan(double yrs){
    timeSpan =yrs;
    int size = this.area_EqkSources.size();
    for( int i =0; i<size; ++i)
      ((Set2_Area_Source)area_EqkSources.get(i)).setTimeSpan(yrs);

  }




  /**
   * This method sets the time-span field
   * @param time
   */
  public void setTimeSpan(TimeSpan timeSpan){
    time = new TimeSpan();
    time= timeSpan;
  }



  /**
   * Get number of ruptures for source at index iSource
   * This method iterates through the list of 3 vectors for charA , charB and grB
   * to find the the element in the vector to which the source corresponds
   * @param iSource index of source whose ruptures need to be found
   */
  public int getNumRuptures(int iSource){
    return getSource(iSource).getNumRuptures();
  }

  /**
   * Get the ith rupture of the source. this method DOES NOT return reference
   * to the object. So, when you call this method again, result from previous
   * method call is valid. This behavior is in contrast with
   * getRupture(int source, int i) method
   *
   * @param source
   * @param i
   * @return
   */
  public EqkRupture getRuptureClone(int iSource, int nRupture) {
    return getSource(iSource).getRuptureClone(nRupture);
  }

  /**
   * Get the ith rupture of the source. this method DOES NOT return reference
   * to the object. So, when you call this method again, result from previous
   * method call is valid. This behavior is in contrast with
   * getRupture(int source, int i) method
   *
   * @param source
   * @param i
   * @return
   */
  public EqkRupture getRupture(int iSource, int nRupture) {
    return getSource(iSource).getRupture(nRupture);
  }

  /**
   * Return the earhthquake source at index i. This methos returns the reference to
   * the class variable. So, when you call this method again, result from previous
   * method call is no longer valid.
   * this is secret, fast but dangerous method
   *
   * @param iSource : index of the source needed
   *
   * @return Returns the ProbEqkSource at index i
   *
   */
  public ProbEqkSource getSource(int iSource) {
    return (ProbEqkSource) area_EqkSources.get(iSource);
  }

  /**
   * Get the number of earthquake sources
   *
   * @return integer value specifying the number of earthquake sources
   */
  public int getNumSources(){
    return area_EqkSources.size();
  }

  /**
   * Return the earthquake source at index i. This methos DOES NOT return the
   * reference to the class variable. So, when you call this method again,
   * result from previous method call is still valid. This behavior is in contrast
   * with the behavior of method getSource(int i)
   *
   * @param iSource : index of the source needed
   *
   * @return Returns the ProbEqkSource at index i
   *
   * FIX:FIX :: This function has not been implemented yet. Have to give a thought on that
   *
   */
  public ProbEqkSource getSourceClone(int iSource) {
    return null;
      /*ProbEqkSource probEqkSource =getSource(iSource);
      if(probEqkSource instanceof WardGridTestCharEqkSource){
          WardGridTestCharEqkSource probEqkSource1 = (WardGridTestCharEqkSource)probEqkSource;
          ProbEqkRupture r = probEqkSource1.getRupture(0);
          r.
          WardGridTestCharEqkSource frankel96_Char = new WardGridTestCharEqkSource(;

      }*/

  }


  /**
   * Return  iterator over all the earthquake sources
   *
   * @return Iterator over all earhtquake sources
   */
  public Iterator getSourcesIterator() {

    return area_EqkSources.iterator();
  }

  /**
   * Get the list of all earthquake sources. Clone is returned.
   * All the 3 different Vector source List are combined into the one Vector list
   * So, list can be save in Vector and this object subsequently destroyed
   *
   * @return Vector of Prob Earthquake sources
   */
  public Vector  getSourceList(){

    return area_EqkSources;
  }



  /**
   * Return the name for this class
   *
   * @return : return the name for this class
   */
  public String getName(){
    return C;
  }

  /**
   * set the fault locations
   */
  private void setLocations() {

    int count=0;

    //Top location
    area_Location[count++]= new Location(38.901,-122);
    area_Location[count++]= new Location(38.899, -121.920);
    area_Location[count++] = new Location(38.892 ,-121.840);
    area_Location[count++] = new Location(38.881,-121.760);
    area_Location[count++] = new Location(38.866,-121.682);
    area_Location[count++] = new Location(38.846,-121.606);
    area_Location[count++] = new Location(38.822,-121.532);
    area_Location[count++]= new Location(38.794,-121.460);
    area_Location[count++] = new Location(38.762,-121.390);
    area_Location[count++] = new Location(38.727,-121.324);
    area_Location[count++] = new Location(38.688,-121.261);
    area_Location[count++] = new Location(38.645,-121.202);
    area_Location[count++] = new Location(38.600,-121.147);
    area_Location[count++] = new Location(38.551,-121.096);
    area_Location[count++]= new Location(38.500,-121.050);
    area_Location[count++] = new Location(38.446,-121.008);
    area_Location[count++] = new Location(38.390,-120.971);
    area_Location[count++]= new Location(38.333,-120.940);
    area_Location[count++] = new Location(38.273,-120.913);
    area_Location[count++] = new Location(38.213,-120.892);
    area_Location[count++] = new Location(38.151,-120.876);
    area_Location[count++] = new Location(38.089,-120.866);




    //right location
    area_Location[count++] = new Location(38.026,-120.862);
    area_Location[count++] = new Location(37.963,-120.863 );
    area_Location[count++]= new Location(37.900,-120.869);
    area_Location[count++] = new Location(37.838,-120.881);
    area_Location[count++] = new Location(37.777,-120.899);
    area_Location[count++] = new Location(37.717,-120.921);
    area_Location[count++] = new Location(37.658,-120.949);
    area_Location[count++] = new Location(37.601,-120.982);
    area_Location[count++] = new Location(37.545,-121.020);
    area_Location[count++] = new Location(37.492,-121.063);
    area_Location[count++] = new Location(37.442,-121.110);
    area_Location[count++] = new Location(37.394,-121.161);
    area_Location[count++]= new Location(37.349,-121.216);
    area_Location[count++] = new Location(37.308,-121.275);
    area_Location[count++] = new Location(37.269,-121.337);
    area_Location[count++] = new Location(37.234,-121.403);
    area_Location[count++] = new Location(37.203,-121.471);
    area_Location[count++] = new Location(37.176,-121.542);
    area_Location[count++] = new Location(37.153,-121.615);
    area_Location[count++] = new Location(37.133,-121.690);
    area_Location[count++] = new Location(37.118,-121.766);
    area_Location[count++] = new Location(37.118,-121.843);
    area_Location[count++] = new Location(37.101,-121.922);


    //bottom location
    area_Location[count++] = new Location(37.099,-122.0);
    area_Location[count++]= new Location(37.101,-122.78);
    area_Location[count++] = new Location(37.108,-122.157);
    area_Location[count++] = new Location(37.118,-122.234);
    area_Location[count++] = new Location(37.133,-122.310);
    area_Location[count++]= new Location(37.153,-122.385);
    area_Location[count++] = new Location(37.176,-122.458);
    area_Location[count++] = new Location(37.203,-122.529);
    area_Location[count++]= new Location(37.234,-122.597);
    area_Location[count++] = new Location(37.269,-122.663);
    area_Location[count++] = new Location(37.308,-122.725);
    area_Location[count++] = new Location(37.349,-122.784);
    area_Location[count++] = new Location(37.394,-122.839);
    area_Location[count++]= new Location(37.442,-122.890);
    area_Location[count++]= new Location(37.492,-122.937);
    area_Location[count++]= new Location(37.545,-122.980);
    area_Location[count++] = new Location(37.601,-123.018);
    area_Location[count++] = new Location(37.658,-123.051);
    area_Location[count++]= new Location(37.717,-123.079);
    area_Location[count++] = new Location(37.777,-123.101);
    area_Location[count++] = new Location(37.838,-123.119);
    area_Location[count++] = new Location(37.900,-123.131);
    area_Location[count++]= new Location(37.963,-123.137);

    //left location
    area_Location[count++]= new Location(38.026,-123.138);
    area_Location[count++] = new Location(38.089,-123.134);
    area_Location[count++] = new Location(38.151,-123.124);
    area_Location[count++]= new Location(38.123,-123.108);
    area_Location[count++]= new Location(38.273,-123.087);
    area_Location[count++] = new Location(38.333,-123.060);
    area_Location[count++] = new Location(38.390,-123.029);
    area_Location[count++]= new Location(38.446,-122.992);
    area_Location[count++] = new Location(38.500,-122.950);
    area_Location[count++] = new Location(38.551,-122.904);
    area_Location[count++]= new Location(38.600,-122.853);
    area_Location[count++] = new Location(38.645,-122.798);
    area_Location[count++] = new Location(38.688,-122.739);
    area_Location[count++] = new Location(38.727,-122.676);
    area_Location[count++] = new Location(38.762,-122.610);
    area_Location[count++]= new Location(38.794,-122.540);
    area_Location[count++]= new Location(38.822,-122.468);
    area_Location[count++] = new Location(38.846,-122.394);
    area_Location[count++] = new Location(38.866,-122.318);
    area_Location[count++] = new Location(38.881,-122.240);
    area_Location[count++] = new Location(38.892,-122.160);
    area_Location[count++] = new Location(38.899,-122.080);
  }
}
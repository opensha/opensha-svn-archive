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
 * Date : Oct 24 , 2002
 * @version 1.0
 */

public class Set1_Fault_Forecast extends EqkRupForecast
    implements ParameterChangeListener{

  /**
   * @todo variables
   */
  //for Debug purposes
  private static String  C = new String("FaultCaseTest1_Fault");
  private boolean D = true;

  /**
  * timespan field in yrs for now(but have to ultimately make it a TimeSpan class variable
  */
  private double timeSpan;
  private TimeSpan time;

  // save the source. Fault1 has only 1 source
  private Set1_Fault_Source source;

  //Param Name
  private final static String SIGMA_PARAM_NAME =  "Mag Length Sigma";
  private final static String TRUNCTYPE_PARAM_NAME =  "Trunc-Type";
  private final static String TRUNCLEVEL_PARAM_NAME =  "Trunc-Level";
  private final static String GRID_PARAM_NAME =  "Fault Grid Spacing (km)";
  private final static String OFFSET_PARAM_NAME =  "Offset (km)";
  private final static String MAG_DIST_PARAM_NAME = "Fault Mag Dist";
  private final static String RAKE_PARAM_NAME ="Rake";
  private final static String TIMESPAN_PARAM_NAME ="TimeSpan #years";
  // dip name
  private final static String DIP_PARAM_NAME = "Dip";

  // default grid spacing is 1km
  private Double DEFAULT_GRID_VAL = new Double(1);
  //default rupture offset is 1km
  private Double DEFAULT_OFFSET_VAL = new Double(1);

  // values for Mag length sigma
  private Double SIGMA_PARAM_MIN = new Double(0);
  private Double SIGMA_PARAM_MAX = new Double(1);
  private Double DEFAULT_SIGMA_VAL = new Double(0.5);
  private double UPPER_SEISMO_DEPTH = 0.0;
  private double LOWER_SEISMO_DEPTH = 25.0;

  // fault-1 name
  private String FAULT1_NAME = new String("Fault 1");
  private Location fault1_LOCATION1 = new Location(38.22480, -122, 0);
  private Location fault1_LOCATION2 = new Location(38.0, -122, 0);

  //fault-2 name
  private String FAULT2_NAME = new String("Fault 2");
  private Location fault2_LOCATION1 = new Location(38.22480, -122, 1);
  private Location fault2_LOCATION2 = new Location(38.0, -122, 1);


  // add the grid spacing field
  DoubleParameter gridParam=new DoubleParameter(this.GRID_PARAM_NAME,this.DEFAULT_GRID_VAL);

  // add the rupOffset spacing field
  DoubleParameter offsetParam = new DoubleParameter(OFFSET_PARAM_NAME, DEFAULT_OFFSET_VAL);


  // add sigma for maglength(0-1)
  DoubleParameter lengthSigmaParam = new DoubleParameter(SIGMA_PARAM_NAME,
                         SIGMA_PARAM_MIN, SIGMA_PARAM_MAX, DEFAULT_SIGMA_VAL);

  DoubleParameter rakeParam = new DoubleParameter(RAKE_PARAM_NAME);


  //add the dip parameter
  DoubleParameter timeSpanParam = new DoubleParameter(this.TIMESPAN_PARAM_NAME);

  //add the dip parameter
  DoubleParameter dipParam = new DoubleParameter(this.DIP_PARAM_NAME);

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
  public Set1_Fault_Forecast() {

    /* Now make the source in Fault 1 */
    adjustableParams.addParameter(gridParam);
    adjustableParams.addParameter(offsetParam);
    adjustableParams.addParameter(lengthSigmaParam);
    adjustableParams.addParameter(dipParam);
    adjustableParams.addParameter(rakeParam);
    adjustableParams.addParameter(timeSpanParam);

    // adding the supported MagDistclasses
    supportedMagDists.add(GaussianMagFreqDist.NAME);
    supportedMagDists.add(SingleMagFreqDist.NAME);
    supportedMagDists.add(GutenbergRichterMagFreqDist.NAME);
    supportedMagDists.add(YC_1985_CharMagFreqDist.NAME);
    magDistParam = new MagFreqDistParameter(MAG_DIST_PARAM_NAME, supportedMagDists);
    //add the magdist parameter
    adjustableParams.addParameter(this.magDistParam);


    // listen for change in the parameters
    gridParam.addParameterChangeListener(this);
    offsetParam.addParameterChangeListener(this);
    lengthSigmaParam.addParameterChangeListener(this);
    dipParam.addParameterChangeListener(this);
    rakeParam.addParameterChangeListener(this);
    magDistParam.addParameterChangeListener(this);
    //updateGUI();
  }


  /**
   * sets the timeSpan field
   * @param yrs : have to be modfied from the double varible to the timeSpan field variable
   */
  public void setTimeSpan(double yrs){
    timeSpan =yrs;
    source.setTimeSpan(yrs);
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

      parameterChangeFlag=true;
   }

  /**
   * update the sources based on the user paramters, only when user has changed any parameter
   */
/** @merge workspace: Changes in the Workspace */
   public void updateForecast(){
     String S = C + "updateForecast::";
/** @merge repository: Changes from the Repository
   public void updateForecast(){
     String S = C + "updateGUI::";
*/

     if(parameterChangeFlag) {

       // dip param value
       double dipValue = ((Double)dipParam.getValue()).doubleValue();
       // first build the fault trace, then add
       // add the location to the trace
       if(dipValue == 90){
         // fault1
         faultTrace = new FaultTrace(FAULT1_NAME);
         faultTrace.addLocation((Location)fault1_LOCATION1.clone());
         faultTrace.addLocation((Location)fault1_LOCATION2.clone());
       }

       else {
         //fault2
         faultTrace = new FaultTrace(FAULT2_NAME);
         faultTrace.addLocation((Location)fault2_LOCATION1.clone());
         faultTrace.addLocation((Location)fault2_LOCATION2.clone());
       }
       //make the fault data
       SimpleFaultData faultData= new SimpleFaultData(dipValue,
              LOWER_SEISMO_DEPTH,UPPER_SEISMO_DEPTH,faultTrace);
       if(D) System.out.println(S+"faultdata:"+faultData);
       FrankelGriddedFaultFactory factory =
           new FrankelGriddedFaultFactory(faultData,
                                         ((Double)gridParam.getValue()).doubleValue());
       // get the gridded surface
       GriddedSurfaceAPI surface = factory.getGriddedSurface();

       if(D) System.out.println(S+"Columns in surface:"+surface.getNumCols());
       if(D) System.out.println(S+"Rows in surface:"+surface.getNumRows());

       source = new  Set1_Fault_Source((IncrementalMagFreqDist)magDistParam.getValue(),((Double)rakeParam.getValue()).doubleValue() ,
                                       ((Double)offsetParam.getValue()).doubleValue(),(EvenlyGriddedSurface)surface);
       setTimeSpan(((Double)timeSpanParam.getValue()).doubleValue());
     }
     parameterChangeFlag = false;
   }

  /**
   * This method sets the time-span field
   * @param time
   */
  public void setTimeSpan(TimeSpan timeSpan){
    time = new TimeSpan();
    time= timeSpan;
    source.setTimeSpan(timeSpan);
  }


  /**
   * Get number of ruptures for source at index iSource
   * @param iSource index of source whose ruptures need to be found
   */
  public int getNumRuptures(int iSource) {

    // we have only one source
    if(iSource!=0)
      throw new RuntimeException(C+":getNumRuptures():"+
                                 "Only 1 source available, iSource should be equal to 0");

    return source.getNumRuptures();
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
    // we have only one source
    if(iSource!=0)
      throw new RuntimeException(C+":getNumRuptures():"+
                               "Only 1 source available, iSource should be equal to 0");

    // get the source and return its rupture
    return source.getRuptureClone(nRupture);
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
     // we have only one source
     if(iSource!=0)
       throw new RuntimeException(C+":getNumRuptures():"+
                               "Only 1 source available, iSource should be equal to 0");

      return source.getRupture(nRupture);
   }

   /**
    * Return the earhthquake source at index i. This methos returns the reference to
    * the class variable. So, when you call this method again, result from previous
    * method call is no longer valid.
    * this is  fast but dangerous method
    *
    * @param iSource : index of the source needed
    *
    * @return Returns the ProbEqkSource at index i
    *
    */
   public ProbEqkSource getSource(int iSource) {

     // we have only one source
   if(iSource!=0)
     throw new RuntimeException(C+":getNumRuptures():"+
                                "Only 1 source available, iSource should be equal to 0");

    return source;
   }


   /**
    * Get the number of earthquake sources
    *
    * @return integer value specifying the number of earthquake sources
    */
   public int getNumSources(){
     return 1;
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
   }

  /**
    * Return  iterator over all the earthquake sources
    *
    * @return Iterator over all earhtquake sources
    */
   public Iterator getSourcesIterator() {
     Iterator i = getSourceList().iterator();
     return i;
   }

    /**
     *  Clone is returned.
     * All the 3 different Vector source List are combined into the one Vector list
     * So, list can be save in Vector and this object subsequently destroyed
     *
     * @return Vector of Prob Earthquake sources
     */
    public Vector  getSourceList(){
      Vector v =new Vector();
      v.add(source);
      return v;
    }


  /**
   * Return the name for this class
   *
   * @return : return the name for this class
   */
   public String getName(){
     return C;
   }
}

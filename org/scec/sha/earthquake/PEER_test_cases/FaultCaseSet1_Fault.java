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


/**
 * <p>Title: FaultCaseSet1_Fault</p>
 * <p>Description: Fault 1 Equake rupture forecast. The Peer Group Test cases </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class FaultCaseSet1_Fault extends EqkRupForecast{

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

  // save the source. Fault1 has only 1 source
  private Frankel96_CharEqkSource source;

  //Param Name
  private final static String SIGMA_PARAM_NAME =  "Mag Length Sigma";
  private final static String TRUNCTYPE_PARAM_NAME =  "Trunc-Type";
  private final static String TRUNCLEVEL_PARAM_NAME =  "Trunc-Level";
  private final static String GRID_PARAM_NAME =  "Grid Spacing (km)";
  private final static String OFFSET_PARAM_NAME =  "Offset (km)";
  private final static String MAG_DIST_PARAM_NAME = "Mag Dist";

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

  // fault name
  private String FAULT_NAME = new String("Fault 1");
  private Location LOCATION1 = new Location(38.22480, -122, 0);
  private Location LOCATION2 = new Location(38.0, -122, 0);
  private double UPPER_SEISMO_DEPTH = 0.0;
  private double LOWER_SEISMO_DEPTH = 25.0;
  private double GRID_SPACING = 1.0;
  private double RAKE = 0;
  private double MAG =5.0;
  private double RATE = 0.0395;
  private double DIP = 90; // dip in degrees

  // add the grid spacing field
  DoubleParameter gridParam=new DoubleParameter(this.GRID_PARAM_NAME,this.DEFAULT_GRID_VAL);


  // add the rupOffset spacing field
  DoubleParameter offsetParam = new DoubleParameter(OFFSET_PARAM_NAME, DEFAULT_OFFSET_VAL);


  // add sigma for maglength(0-1)
  DoubleParameter lengthSigmaParam = new DoubleParameter(SIGMA_PARAM_NAME,
                         SIGMA_PARAM_MIN, SIGMA_PARAM_MAX, DEFAULT_SIGMA_VAL);
    // lengthSigmaParam.addParameterChangeFailListener(applet);


  //add the timespan parameter
  DoubleParameter dipParam = new DoubleParameter(this.DIP_PARAM_NAME);


 //   eqkSourceEditor.setParameterInvisible(this.GRID_PARAM_NAME, true);
   //   eqkSourceEditor.setParameterInvisible(this.OFFSET_PARAM_NAME, true);
    //  eqkSourceEditor.setParameterInvisible(this.SIGMA_PARAM_NAME, true);
     // eqkSourceEditor.setParameterInvisible(this.DIP_PARAM_NAME, true);

     //adding the supported MagDists
      Vector supportedMagDists=new Vector();


      MagFreqDistParameter magDistParam ;


  /**
   * This constructor constructs the source
   *
   * No argument constructor
   */
  public FaultCaseSet1_Fault() {

    /* Now make the source in Fault 1 */
    adjustableParams.addParameter(this.gridParam);
    adjustableParams.addParameter(this.offsetParam);
    adjustableParams.addParameter(this.lengthSigmaParam);
    adjustableParams.addParameter(this.dipParam);

    // adding the supported MagDistclasses
    supportedMagDists.add(GaussianMagFreqDist.NAME);
    supportedMagDists.add(SingleMagFreqDist.NAME);
    supportedMagDists.add(GutenbergRichterMagFreqDist.NAME);
    supportedMagDists.add(YC_1985_CharMagFreqDist.NAME);
    magDistParam = new MagFreqDistParameter(MAG_DIST_PARAM_NAME, supportedMagDists);

    adjustableParams.addParameter(this.magDistParam);

    // first build the fault trace
    FaultTrace faultTrace= new FaultTrace(FAULT_NAME);

    // add the location to the trace
    faultTrace.addLocation( (Location)LOCATION1.clone());
    faultTrace.addLocation( (Location)LOCATION2.clone());

    // value of gridspacing has been set to 1 km
    FrankelGriddedFaultFactory factory =
        new FrankelGriddedFaultFactory(faultTrace,DIP, UPPER_SEISMO_DEPTH,
                                       LOWER_SEISMO_DEPTH, GRID_SPACING);
   // get the gridded surface
    GriddedSurfaceAPI surface = factory.getGriddedSurface();
    source = new  Frankel96_CharEqkSource( RAKE , MAG, RATE,
                                                      (EvenlyGriddedSurface)surface);
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
   * This method sets the time-span field
   * @param time
   */
  public void setTimeSpan(TimeSpan timeSpan){
    time = new TimeSpan();
    time= timeSpan;
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

    return 1;
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
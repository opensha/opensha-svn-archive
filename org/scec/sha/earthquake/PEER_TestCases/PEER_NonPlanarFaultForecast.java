package org.scec.sha.earthquake.PEER_TestCases;


import java.util.Vector;
import java.util.Iterator;


import org.scec.data.TimeSpan;
import org.scec.data.Location;

import org.scec.param.*;
import org.scec.sha.fault.*;
import org.scec.sha.surface.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_CharEqkSource;
import org.scec.sha.magdist.parameter.MagFreqDistParameter;
import org.scec.sha.magdist.*;
import org.scec.param.event.*;


/**
 * <p>Title: PEER_NonPlanarFaultForecast </p>
 * <p>Description: Fault 1 Equake rupture forecast. The Peer Group Test cases </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field
 * Date : Nov 30, 2002
 * @version 1.0
 */

public class PEER_NonPlanarFaultForecast extends EqkRupForecast
    implements ParameterChangeListener {

  /**
   * @todo variables
   */
  //for Debug purposes
  private static String  C = new String("PEER Non-Planar Fault");
  private boolean D = false;

  //name for this classs
  public final static String  NAME = C;


  // the prob eqk source (only one)
  private PEER_FaultSource source;

  // grid spacing parameter stuff
  public final static String GRID_PARAM_NAME =  "Fault Grid Spacing";
  private Double DEFAULT_GRID_VAL = new Double(1);
  public final static String GRID_PARAM_UNITS = "kms";
  private final static double GRID_PARAM_MIN = .001;
  private final static double GRID_PARAM_MAX = 1000;

  //rupture offset parameter stuff
  public final static String OFFSET_PARAM_NAME =  "Offset";
  private Double DEFAULT_OFFSET_VAL = new Double(1);
  public final static String OFFSET_PARAM_UNITS = "kms";
  private final static double OFFSET_PARAM_MIN = .01;
  private final static double OFFSET_PARAM_MAX = 10000;

  // Mag-length sigma parameter stuff
  public final static String SIGMA_PARAM_NAME =  "Mag Length Sigma";
  private double SIGMA_PARAM_MIN = 0;
  private double SIGMA_PARAM_MAX = 1;
  public Double DEFAULT_SIGMA_VAL = new Double(0.0);

  // slip rate prameter stuff
  public final static String SLIP_RATE_NAME = "Slip Rate";
  public final static String SLIP_RATE_UNITS = "mm/yr";
  public final static double SLIP_RATE_MIN = 0.0;
  public final static double SLIP_RATE_MAX = 1e5;
  public final static Double SLIP_RATE_DEFAULT = new Double(2);

  // parameter for magUpper of the GR dist
  public static final String GR_MAG_UPPER=new String("Mag Upper");
  public static final String GR_MAG_UPPER_INFO=new String("Max mag of the GR distribution (must be an increment of 0.05)");
  public final static Double GR_MAG_UPPER_DEFAULT = new Double(7.15);

  // segmentation parameter stuff
  public final static String SEGMENTATION_NAME = new String ("Segmentation Model");
  public final static String SEGMENTATION_NONE = new String ("Unsegmented");
  public final static String SEGMENTATION_A = new String ("Segment A only");
  public final static String SEGMENTATION_B = new String ("Segment B only");
  public final static String SEGMENTATION_C = new String ("Segment C only");
  public final static String SEGMENTATION_D = new String ("Segment D only");
  public final static String SEGMENTATION_E = new String ("Segment E only");

  // fault-model parameter stuff
  public final static String FAULT_MODEL_NAME = new String ("Fault Model");
  public final static String FAULT_MODEL_FRANKEL = new String ("Frankel's");
  public final static String FAULT_MODEL_STIRLING = new String ("Stirling's");

  // make the grid spacing parameter
  private DoubleParameter gridParam=new DoubleParameter(GRID_PARAM_NAME,GRID_PARAM_MIN,
      GRID_PARAM_MAX,GRID_PARAM_UNITS,DEFAULT_GRID_VAL);

  // make the rupture offset parameter
  private DoubleParameter offsetParam = new DoubleParameter(OFFSET_PARAM_NAME,OFFSET_PARAM_MIN,
      OFFSET_PARAM_MAX,OFFSET_PARAM_UNITS,DEFAULT_OFFSET_VAL);

  // make the mag-length sigma parameter
  private DoubleParameter lengthSigmaParam = new DoubleParameter(SIGMA_PARAM_NAME,
      SIGMA_PARAM_MIN, SIGMA_PARAM_MAX, DEFAULT_SIGMA_VAL);


  // make the mag-length sigma parameter
  private DoubleParameter slipRateParam = new DoubleParameter(SLIP_RATE_NAME,
      SLIP_RATE_MIN, SLIP_RATE_MAX, SLIP_RATE_UNITS, SLIP_RATE_DEFAULT);

  // make the magUpper parameter
  private DoubleParameter magUpperParam = new DoubleParameter(GR_MAG_UPPER,GR_MAG_UPPER_DEFAULT);

  // make the segmetation model parameter
  private Vector segModelNamesStrings=new Vector();
  private StringParameter segModelParam;

  // make the fault-model parameter
  private Vector faultModelNamesStrings = new Vector();
  private StringParameter faultModelParam;

  // fault stuff
  private FaultTrace faultTrace;
  public final static double LOWER_SEISMO_DEPTH = 12.0;
  public final static  double UPPER_SEISMO_DEPTH = 1.0;
  public final static  double DIP=60.0;
  public final static  double RAKE=-90.0;
  // Fault trace locations
  private final static Location traceLoc1 = new Location(37.609531,-121.7168636,1.0);     // southern most point
  private final static Location traceLoc2 = new Location(37.804854,-121.8580591,1.0);
  private final static Location traceLoc3 = new Location(38.000000,-122.0000000,1.0);
  private final static Location traceLoc4 = new Location(38.224800,-122.0000000,1.0);
  private final static Location traceLoc5 = new Location(38.419959,-121.8568637,1.0);
  private final static Location traceLoc6 = new Location(38.614736,-121.7129562,1.0);     // northern most point

  // GR mag freq dist stuff
  private GutenbergRichterMagFreqDist grMagFreqDist;
  public final static  double GR_MIN = 0.05;
  public final static  double GR_MAX = 9.95;
  public final static  int GR_NUM = 100;
  public final static  double GR_BVALUE = 0.9;
  public final static  double GR_MAG_LOWER = 0.05;


  /**
   * This constructor makes the parameters and sets up the source
   *
   * No argument constructor
   */
  public PEER_NonPlanarFaultForecast() {

    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.YEARS,TimeSpan.YEARS);
    timeSpan.addParameterChangeListener(this);

    // make the segModelParam
    segModelNamesStrings.add(SEGMENTATION_NONE);
    segModelNamesStrings.add(SEGMENTATION_A);
    segModelNamesStrings.add(SEGMENTATION_B);
    segModelNamesStrings.add(SEGMENTATION_C);
    segModelNamesStrings.add(SEGMENTATION_D);
    segModelNamesStrings.add(SEGMENTATION_E);
    segModelParam = new StringParameter(SEGMENTATION_NAME,segModelNamesStrings,
                                      (String)segModelNamesStrings.get(0));

    // make the faultModelParam
    faultModelNamesStrings.add(FAULT_MODEL_FRANKEL);
    faultModelNamesStrings.add(FAULT_MODEL_STIRLING);
    faultModelParam = new StringParameter(FAULT_MODEL_NAME, faultModelNamesStrings,(String)faultModelNamesStrings.get(0));

    // now add the parameters to the adjustableParams list
    adjustableParams.addParameter(gridParam);
    adjustableParams.addParameter(offsetParam);
    adjustableParams.addParameter(lengthSigmaParam);
    adjustableParams.addParameter(slipRateParam);
    adjustableParams.addParameter(magUpperParam);
    adjustableParams.addParameter(segModelParam);
    adjustableParams.addParameter(faultModelParam);

    // listen for change in the parameters
    gridParam.addParameterChangeListener(this);
    offsetParam.addParameterChangeListener(this);
    lengthSigmaParam.addParameterChangeListener(this);
    slipRateParam.addParameterChangeListener(this);
    magUpperParam.addParameterChangeListener(this);
    segModelParam.addParameterChangeListener(this);
    faultModelParam.addParameterChangeListener(this);

    grMagFreqDist = new GutenbergRichterMagFreqDist(GR_MIN, GR_MAX, GR_NUM);

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
   public void updateForecast(){
     String S = C + "updateForecast::";

     if(parameterChangeFlag) {

       // make the fault trace based on the segmentation model
       String segModel = (String) segModelParam.getValue();
       faultTrace = new FaultTrace("Non Planar Fault");
       if(segModel.equals(SEGMENTATION_NONE)){
         faultTrace.addLocation(traceLoc1);
         faultTrace.addLocation(traceLoc2);
         faultTrace.addLocation(traceLoc3);
         faultTrace.addLocation(traceLoc4);
         faultTrace.addLocation(traceLoc5);
         faultTrace.addLocation(traceLoc6);
       }
       else if (segModel.equals(SEGMENTATION_E)){
         faultTrace.addLocation(traceLoc1);
         faultTrace.addLocation(traceLoc2);
       }
       else if (segModel.equals(SEGMENTATION_D)){
         faultTrace.addLocation(traceLoc2);
         faultTrace.addLocation(traceLoc3);
       }
       else if (segModel.equals(SEGMENTATION_C)){
         faultTrace.addLocation(traceLoc3);
         faultTrace.addLocation(traceLoc4);
       }
       else if (segModel.equals(SEGMENTATION_B)){
         faultTrace.addLocation(traceLoc4);
         faultTrace.addLocation(traceLoc5);
       }
       else if (segModel.equals(SEGMENTATION_A)){
         faultTrace.addLocation(traceLoc5);
         faultTrace.addLocation(traceLoc6);
       }


       // Now make the gridded surface
       double gridSpacing = ((Double)gridParam.getValue()).doubleValue();
       String faultModel = (String) faultModelParam.getValue();

       GriddedFaultFactory factory;

       if(faultModel.equals(FAULT_MODEL_FRANKEL)) {
         factory = new FrankelGriddedFaultFactory( faultTrace, DIP, UPPER_SEISMO_DEPTH,
                                                   LOWER_SEISMO_DEPTH, gridSpacing );
       }
       else {
         factory = new StirlingGriddedFaultFactory( faultTrace, DIP, UPPER_SEISMO_DEPTH,
                                                   LOWER_SEISMO_DEPTH, gridSpacing );
       }

       GriddedSurfaceAPI surface = factory.getGriddedSurface();

       // Now make the mag freq dist
       double magUpper = ((Double) magUpperParam.getValue()).doubleValue();
       double slipRate = ((Double) slipRateParam.getValue()).doubleValue() / 1000.0;  // last is to convert to meters/yr
       double ddw = (LOWER_SEISMO_DEPTH-UPPER_SEISMO_DEPTH)/Math.sin(DIP*Math.PI/180);
       double faultArea = faultTrace.getTraceLength() * ddw * 1e6;  // the last is to convert to meters
       double totMoRate = 3e10*faultArea*slipRate;
       grMagFreqDist.setAllButTotCumRate(GR_MAG_LOWER, magUpper, totMoRate,GR_BVALUE);

       // Now make the source
       source = new  PEER_FaultSource(grMagFreqDist, RAKE ,
                                        ((Double)offsetParam.getValue()).doubleValue(),
                                        (EvenlyGriddedSurface)surface,
                                        timeSpan.getDuration(),
                                        ((Double)lengthSigmaParam.getValue()).doubleValue() );
     }
     parameterChangeFlag = false;
   }




  /**
   * Get number of ruptures for source at index iSource
   * @param iSource index of source whose ruptures need to be found
   */
  public int getNumRuptures(int iSource) {

    // we have only one source
    if(iSource!=0)
      throw new RuntimeException("Only 1 source available, iSource should be equal to 0");

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
      throw new RuntimeException("Only 1 source available, iSource should be equal to 0");

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
       throw new RuntimeException("Only 1 source available, iSource should be equal to 0");

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
     throw new RuntimeException("Only 1 source available, iSource should be equal to 0");

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

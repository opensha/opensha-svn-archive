package org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases;


import java.util.Vector;
import java.util.Iterator;


import org.scec.data.TimeSpan;
import org.scec.data.Location;

import org.scec.param.*;
import org.scec.sha.fault.*;
import org.scec.sha.surface.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_CharEqkSource;
import org.scec.sha.param.MagFreqDistParameter;
import org.scec.sha.magdist.*;
import org.scec.param.event.*;

import org.scec.sha.param.SimpleFaultParameter;


/**
 * <p>Title: PEER_FaultForecast</p>
 * <p>Description: This forecast implements "Fault1" or "Fault2" defined in Set-1 of the PEER test
 * cases (used also for Set-2, cases 3 & 4)  </p>
 *
 * @author Nitin & Vipin Gupta, & Ned Field
 * Date : Oct 24 , 2002
 * @version 1.0
 */

public class PEER_FaultForecast extends EqkRupForecast
    implements ParameterChangeListener {

  //for Debug purposes
  private static String  C = new String("PEER Fault");
  private boolean D = false;

  //name for this classs
  public final static String  NAME = C;


  // this is the source (only 1 for this ERF)
  private PEER_FaultSource source;

  //Parameter Names
  public final static String SIGMA_PARAM_NAME =  "Mag Length Sigma";
  public final static String GRID_PARAM_NAME =  "Fault Grid Spacing";
  public final static String OFFSET_PARAM_NAME =  "Offset";
  public final static String MAG_DIST_PARAM_NAME = "Mag Dist";  // this is never shown by the MagFreqDistParameterEditor?
  public final static String RAKE_PARAM_NAME ="Rake";
  public final static String DIP_PARAM_NAME = "Dip";

  // grid spacing parameter stuff
  private Double DEFAULT_GRID_VAL = new Double(1);
  public final static String GRID_PARAM_UNITS = "kms";
  private final static double GRID_PARAM_MIN = .001;
  private final static double GRID_PARAM_MAX = 1000;


  // rupture offset parameter stuff
  private Double DEFAULT_OFFSET_VAL = new Double(1);
  public final static String OFFSET_PARAM_UNITS = "kms";
  private final static double OFFSET_PARAM_MIN = .01;
  private final static double OFFSET_PARAM_MAX = 10000;

  // Mag-length sigma parameter stuff
  private Double SIGMA_PARAM_MIN = new Double(0);
  private Double SIGMA_PARAM_MAX = new Double(1);
  public Double DEFAULT_SIGMA_VAL = new Double(0.0);

  // Default dip and rake-parameter values
  private Double DEFAULT_DIP_VAL = new Double(90);
  private Double DEFAULT_RAKE_VAL = new Double(0);

  // stuff for fault-1 (vertically dipping fault)
  private String FAULT1_NAME = new String("Fault 1");
  private double UPPER_SEISMO_DEPTH1 = 0.0;

  // stuff for fault-2 (60-degree dipping fault)
  private String FAULT2_NAME = new String("Fault 2");
  private double UPPER_SEISMO_DEPTH2 = 1.0;

  // stuff for both faults
  private Location fault_LOCATION1 = new Location(38.22480, -122, 0);
  private Location fault_LOCATION2 = new Location(38.0, -122, 0);
  private double LOWER_SEISMO_DEPTH = 12.0;

  // create the grid spacing param
  DoubleParameter gridParam=new DoubleParameter(GRID_PARAM_NAME,GRID_PARAM_MIN,
                                               GRID_PARAM_MAX,GRID_PARAM_UNITS,DEFAULT_GRID_VAL);

  // create the rupOffset spacing param
  DoubleParameter offsetParam = new DoubleParameter(OFFSET_PARAM_NAME,OFFSET_PARAM_MIN,
                                               OFFSET_PARAM_MAX,OFFSET_PARAM_UNITS,DEFAULT_OFFSET_VAL);

  // create the mag-length sigma param
  DoubleParameter lengthSigmaParam = new DoubleParameter(SIGMA_PARAM_NAME,
                         SIGMA_PARAM_MIN, SIGMA_PARAM_MAX, DEFAULT_SIGMA_VAL);

  // create the rake param
  DoubleParameter rakeParam = new DoubleParameter(RAKE_PARAM_NAME, DEFAULT_RAKE_VAL);


  //create the dip parameter
  DoubleParameter dipParam = new DoubleParameter(this.DIP_PARAM_NAME, DEFAULT_DIP_VAL);

  // list for the supported MagDists
  Vector supportedMagDists=new Vector();

  //Mag Freq Dist Parameter
  MagFreqDistParameter magDistParam;


  // TEST make SimpleFaultParameter
//  SimpleFaultParameter testParam;



  // Fault trace
  FaultTrace faultTrace;


  /**
   * Constructor for this source (no arguments)
   */
  public PEER_FaultForecast() {

    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);
    timeSpan.addParameterChangeListener(this);

    // add the adjustable parameters to the list
    adjustableParams.addParameter(gridParam);
    adjustableParams.addParameter(offsetParam);
    adjustableParams.addParameter(lengthSigmaParam);
    adjustableParams.addParameter(dipParam);
    adjustableParams.addParameter(rakeParam);

    // add the supported Mag-Freq Dist classes & make the associated parameter
    supportedMagDists.add(GaussianMagFreqDist.NAME);
    supportedMagDists.add(SingleMagFreqDist.NAME);
    supportedMagDists.add(GutenbergRichterMagFreqDist.NAME);
    supportedMagDists.add(YC_1985_CharMagFreqDist.NAME);
    magDistParam = new MagFreqDistParameter(MAG_DIST_PARAM_NAME, supportedMagDists);
    //add the magdist parameter
    adjustableParams.addParameter(this.magDistParam);

    // register the parameters that need to be listened to
    gridParam.addParameterChangeListener(this);
    offsetParam.addParameterChangeListener(this);
    lengthSigmaParam.addParameterChangeListener(this);
    dipParam.addParameterChangeListener(this);
    rakeParam.addParameterChangeListener(this);
    magDistParam.addParameterChangeListener(this);
  }


  /**
    *  This is the method called by any parameter whose value has been changed
    *
    * @param  event
    */
   public void parameterChange( ParameterChangeEvent event ) {

      parameterChangeFlag=true;
   }

  /**
   * update the source based on the paramters (only if a parameter value has changed)
   */
   public void updateForecast(){
     String S = C + "updateForecast::";

     if(parameterChangeFlag) {

       // check if magDist is null
       if(this.magDistParam.getValue()==null)
          throw new RuntimeException("Mag Dist is null");

       // dip param value
       double dipValue = ((Double)dipParam.getValue()).doubleValue();
       // first build the fault trace, then add add the location to the trace

       SimpleFaultData faultData;
       if(dipValue == 90){
         // fault1
         faultTrace = new FaultTrace(FAULT1_NAME);
         faultTrace.addLocation((Location)fault_LOCATION1.clone());
         faultTrace.addLocation((Location)fault_LOCATION2.clone());
         //make the fault data
         faultData= new SimpleFaultData(dipValue,
              LOWER_SEISMO_DEPTH,UPPER_SEISMO_DEPTH1,faultTrace);
         if(D) System.out.println(S+"faultdata:"+faultData);
       }

       else {
         //fault2
         faultTrace = new FaultTrace(FAULT2_NAME);
         faultTrace.addLocation((Location)fault_LOCATION1.clone());
         faultTrace.addLocation((Location)fault_LOCATION2.clone());
         //make the fault data
         faultData= new SimpleFaultData(dipValue,
              LOWER_SEISMO_DEPTH,UPPER_SEISMO_DEPTH2,faultTrace);
         if(D) System.out.println(S+"faultdata:"+faultData);
       }


       //  create a fault factory and make the surface
       FrankelGriddedFaultFactory factory =
           new FrankelGriddedFaultFactory(faultData,
                                         ((Double)gridParam.getValue()).doubleValue());

       GriddedSurfaceAPI surface = factory.getGriddedSurface();

       if(D) {
         System.out.println(S+"Columns in surface:"+surface.getNumCols());
         System.out.println(S+"Rows in surface:"+surface.getNumRows());
         System.out.println(S+"MagLenthSIgma:"+lengthSigmaParam.getValue());
       }

       // Now make the source
       source = new  PEER_FaultSource((IncrementalMagFreqDist)magDistParam.getValue(),
                                        ((Double)rakeParam.getValue()).doubleValue() ,
                                        ((Double)offsetParam.getValue()).doubleValue(),
                                        (EvenlyGriddedSurface)surface,
                                        timeSpan.getDuration(),
                                        ((Double)lengthSigmaParam.getValue()).doubleValue() );
     }
     parameterChangeFlag = false;

   }




   /**
    * Return the earhthquake source at index i.   Note that this returns a
    * pointer to the source held internally, so that if any parameters
    * are changed, and this method is called again, the source obtained
    * by any previous call to this method will no longer be valid.
    *
    * @param iSource : index of the desired source (only "0" allowed here).
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
    * Returns the number of earthquake sources (always "1" here)
    *
    * @return integer value specifying the number of earthquake sources
    */
   public int getNumSources(){
     return 1;
   }


    /**
     *  This returns a list of sources (contains only one here)
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
     return NAME;
   }

}

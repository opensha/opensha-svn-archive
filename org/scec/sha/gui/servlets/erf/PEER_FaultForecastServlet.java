package org.scec.sha.gui.servlets.erf;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.scec.sha.gui.servlets.erf.ERF_API;
import org.scec.sha.gui.servlets.erf.ERF_WebServiceAPI;
import org.scec.sha.gui.servlets.erf.PEER_FaultERFObject;
import org.scec.sha.gui.beans.ERF_ServletModeGuiBean;
import org.scec.param.*;
import org.scec.sha.fault.*;
import org.scec.sha.surface.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_CharEqkSource;
import org.scec.sha.param.MagFreqDistParameter;
import org.scec.sha.magdist.*;
import org.scec.param.event.*;
import org.scec.data.*;
import org.scec.data.region.*;
import org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_FaultSource;


/**
 * <p>Title: PEER_FaultForecastServlet </p>
 * <p>Description:This servlet generates the fault forecast for the PEER_Fault </p>
 * @author :Nitin Gupta and Vipin Gupta
 * @created June 13,2003
 * @version 1.0
 */

public class PEER_FaultForecastServlet extends HttpServlet implements ERF_WebServiceAPI{

  private static final String className ="PEER Fault";

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

  // Fault trace
  FaultTrace faultTrace;

  // adjustable params for each forecast
  protected ParameterList adjustableParams = null;
  // timespan object for each forecast
  protected TimeSpan timeSpan;

  //Process the HTTP Get request
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    try{
      // get an input stream from the applet
      ObjectInputStream inputFromApplet = new ObjectInputStream(request.getInputStream());
      //gets the object for the ERF Gui Bean
      String funcToCall = (String) inputFromApplet.readObject();
      System.out.println("Function to call:"+funcToCall);

      // return the  output stream back to the ERFGUI bean
      // It returns whatever the Gui asked it for
      ObjectOutputStream outputToApplet = new ObjectOutputStream(response.getOutputStream());

      //gets the Name of the ERF
      if(funcToCall.equalsIgnoreCase(ERF_ServletModeGuiBean.getName))
        outputToApplet.writeObject(this.getName());

      //gets the Adjustable Params for the ERF
      if(funcToCall.equalsIgnoreCase(ERF_ServletModeGuiBean.getAdjParams))
        outputToApplet.writeObject(this.getAdjustableParams());

      //gets the TimeSpan object for the ERF
      else if(funcToCall.equalsIgnoreCase(ERF_ServletModeGuiBean.getTimeSpan))
        outputToApplet.writeObject(this.getTimeSpan());

      //gets the EqkRupForecast object for the selected ERF model
      else if(funcToCall.equalsIgnoreCase(ERF_ServletModeGuiBean.getERF_API)){
        ParameterList paramList=(ParameterList)inputFromApplet.readObject();
        TimeSpan time=(TimeSpan)inputFromApplet.readObject();
        outputToApplet.writeObject(this.getERF_API(time,paramList));
      }
      outputToApplet.close();

    } catch (Exception e) {
      // report to the user whether the operation was successful or not
      e.printStackTrace();
    }

  }


  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // call the doPost method
    doGet(request,response);
  }

  /**
   * Return the name for this class
   *
   * @return : return the name for this class
   */
  public String getName(){
    return this.className;
  }

  /**
   * Get the region for which this forecast is applicable
   * @return : Geographic region object specifying the applicable region of forecast
   */
 public GeographicRegion getApplicableRegion() {
   return null;
   }

   /**
    * get the adjustable parameters for this forecast
    *
    * @return
    */
    public ParameterList getAdjustableParams() {
      if(this.adjustableParams == null){
	adjustableParams= new ParameterList();
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
      }
      return adjustableParams;
   }

   /**
    * This function finds whether a particular location lies in applicable
    * region of the forecast
    *
    * @param loc : location
    * @return: True if this location is within forecast's applicable region, else false
    */
   public boolean isLocWithinApplicableRegion(Location loc) {
     return true;
   }


   /**
    *
    * @param time : TimeSpan Param
    * @param param :ParameterList param
    * @returns the object for the EqkRupForecast with updated sources
    */
   public ERF_API getERF_API(TimeSpan time, ParameterList param){

     //object for the PEER_Fault that implements the ERF_API
     PEER_FaultERFObject peerFaultObject = new PEER_FaultERFObject();
     // check if magDist is null
     if(param.getParameter(this.MAG_DIST_PARAM_NAME).getValue()==null)
       throw new RuntimeException("Mag Dist is null");

     // dip param value
     double dipValue = ((Double)param.getParameter(this.DIP_PARAM_NAME).getValue()).doubleValue();
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
     }

     else {
       //fault2
       faultTrace = new FaultTrace(FAULT2_NAME);
       faultTrace.addLocation((Location)fault_LOCATION1.clone());
       faultTrace.addLocation((Location)fault_LOCATION2.clone());
       //make the fault data
       faultData= new SimpleFaultData(dipValue,
                                      LOWER_SEISMO_DEPTH,UPPER_SEISMO_DEPTH2,faultTrace);

     }

     //  create a fault factory and make the surface
     FrankelGriddedFaultFactory factory =
         new FrankelGriddedFaultFactory(faultData,
         ((Double)param.getParameter(this.GRID_PARAM_NAME).getValue()).doubleValue());

     GriddedSurfaceAPI surface = factory.getGriddedSurface();

     // Now make the source and set it as the PEER Forecats source object
     peerFaultObject.setSource(new PEER_FaultSource((IncrementalMagFreqDist)param.getParameter(this.MAG_DIST_PARAM_NAME).getValue(),
         ((Double)param.getParameter(this.RAKE_PARAM_NAME).getValue()).doubleValue() ,
         ((Double)param.getParameter(this.OFFSET_PARAM_NAME).getValue()).doubleValue(),
         (EvenlyGriddedSurface)surface,
         timeSpan.getDuration(),
         ((Double)param.getParameter(this.SIGMA_PARAM_NAME).getValue()).doubleValue() ));

     return peerFaultObject;
   }


 /**
  * return the time span object
  *
  * @return : time span object is returned which contains start time and duration
  */
 public TimeSpan getTimeSpan() {
   // create the timespan object with start time and duration in years
   timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);
   return this.timeSpan;
 }

}

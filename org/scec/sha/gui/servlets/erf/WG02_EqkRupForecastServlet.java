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
import org.scec.sha.magdist.parameter.MagFreqDistParameter;
import org.scec.sha.magdist.*;
import org.scec.param.event.*;
import org.scec.data.*;
import org.scec.data.region.*;
import org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_FaultSource;
import org.scec.util.FileUtils;
import org.scec.exceptions.FaultException;
import org.scec.sha.gui.servlets.erf.*;

/**
 * <p>Title: WG02_EqkRupForecastServlet </p>
 * <p>Description:This servlet generates the fault forecast for the WG02 Eqk Rup Forecast Model </p>
 * @author :Nitin Gupta and Vipin Gupta
 * @created June 19,2003
 * @version 1.0
 */

public class WG02_EqkRupForecastServlet extends HttpServlet implements ERF_ListWebServiceAPI{

  private static final String className ="WG02 ERF List";


  /**
   * Static variable for input file name
   */
  private final static String INPUT_FILE_NAME = "WG02_WRAPPER_INPUT.DAT";

  // vector to hold the line numbers where each iteration starts
  private Vector iterationLineNumbers;

  // Stuff for background & GR tail seismicity params
  public final static String BACK_SEIS_NAME = new String ("Background Seismicity");
  public final static String GR_TAIL_NAME = new String ("GR Tail Seismicity");
  public final static String SEIS_INCLUDE = new String ("Include");
  public final static String SEIS_EXCLUDE = new String ("Exclude");
  Vector backSeisOptionsStrings = new Vector();
  Vector grTailOptionsStrings = new Vector();
  StringParameter backSeisParam;
  StringParameter grTailParam;

  // For rupture offset along fault parameter
  private final static String RUP_OFFSET_PARAM_NAME ="Rupture Offset";
  private Double DEFAULT_RUP_OFFSET_VAL= new Double(5);
  private final static String RUP_OFFSET_PARAM_UNITS = "km";
  private final static String RUP_OFFSET_PARAM_INFO = "Length of offset for floating ruptures";
  private final static double RUP_OFFSET_PARAM_MIN = 1;
  private final static double RUP_OFFSET_PARAM_MAX = 50;
  DoubleParameter rupOffset_Param;

  // Grid spacing for fault discretization
  private final static String GRID_SPACING_PARAM_NAME ="Fault Discretization";
  private Double DEFAULT_GRID_SPACING_VAL= new Double(1.0);
  private final static String GRID_SPACING_PARAM_UNITS = "km";
  private final static String GRID_SPACING_PARAM_INFO = "Grid spacing of fault surface";
  private final static double GRID_SPACING_PARAM_MIN = 0.1;
  private final static double GRID_SPACING_PARAM_MAX = 5;
  DoubleParameter gridSpacing_Param;

  // For delta mag parameter (magnitude discretization)
  private final static String DELTA_MAG_PARAM_NAME ="Delta Mag";
  private Double DEFAULT_DELTA_MAG_VAL= new Double(0.1);
  private final static String DELTA_MAG_PARAM_INFO = "Discretization of magnitude frequency distributions";
  private final static double DELTA_MAG_PARAM_MIN = 0.005;
  private final static double DELTA_MAG_PARAM_MAX = 0.5;
  DoubleParameter deltaMag_Param;

  // For num realizations parameter
  private final static String NUM_REALIZATIONS_PARAM_NAME ="Num Realizations";
  private Integer DEFAULT_NUM_REALIZATIONS_VAL= new Integer(10);
  private final static String NUM_REALIZATIONS_PARAM_INFO = "Number of Monte Carlo ERF realizations";
  IntegerParameter numRealizationsParam;

  // This is an array holding each line of the input file
  private ArrayList inputFileLines = null;

  // adjustable params for each forecast
  protected ParameterList adjustableParams = null;

  private TimeSpan timeSpan;

  // adjustable parameter primitives
  private int numIterations;
  private double rupOffset;
  private double deltaMag;
  private double gridSpacing;
  private String backSeis;
  private String grTail;

  public void WG02_ERF_Epistemic_List() {

      // create the timespan object with start time and duration in years
      timeSpan = new TimeSpan(TimeSpan.YEARS,TimeSpan.YEARS);
      // create and add adj params to list
      initAdjParams();

      // read the lines of the input files into a list
      try{ inputFileLines = FileUtils.loadFile( INPUT_FILE_NAME ); }
      catch( FileNotFoundException e){ System.out.println(e.toString()); }
      catch( IOException e){ System.out.println(e.toString());}

      // Exit if no data found in list
      if( inputFileLines == null) throw new
             FaultException("No data loaded from "+INPUT_FILE_NAME+". File may be empty or doesn't exist.");

      // find the line numbers for the beginning of each iteration
      iterationLineNumbers = new Vector();
      StringTokenizer st;
      String test=null;
      for(int lineNum=0; lineNum < inputFileLines.size(); lineNum++) {
        st = new StringTokenizer((String) inputFileLines.get(lineNum));
        st.nextToken(); // skip the first token
        if(st.hasMoreTokens()) {
          test = st.nextToken();
          if(test.equals("ITERATIONS"))
            iterationLineNumbers.add(new Integer(lineNum));
        }
      }


      // set the constraint on the number of realizations now that we know the total number
      numRealizationsParam.setConstraint(new IntegerConstraint(1,iterationLineNumbers.size()));

      // set the timespan from the 2nd line of the file
      st = new StringTokenizer((String) inputFileLines.get(1));
      st.nextToken(); // skip first four tokens
      st.nextToken();
      st.nextToken();
      st.nextToken();
      int year = new Double(st.nextToken()).intValue();
      double duration = new Double(st.nextToken()).doubleValue();

      timeSpan.setDuractionConstraint(duration,duration);
      timeSpan.setDuration(duration);
      timeSpan.setStartTimeConstraint(TimeSpan.START_YEAR,year,year);
      timeSpan.setStartTime(year);
    }


    // make the adjustable parameters & the list
    private void initAdjParams() {

      backSeisOptionsStrings.add(SEIS_EXCLUDE);
      //  backSeisOptionsStrings.add(SEIS_INCLUDE);
      backSeisParam = new StringParameter(BACK_SEIS_NAME, backSeisOptionsStrings,SEIS_EXCLUDE);
      grTailOptionsStrings.add(SEIS_EXCLUDE);
      //  grTailOptionsStrings.add(SEIS_INCLUDE);
      grTailParam = new StringParameter(GR_TAIL_NAME, backSeisOptionsStrings,SEIS_EXCLUDE);
      rupOffset_Param = new DoubleParameter(RUP_OFFSET_PARAM_NAME,RUP_OFFSET_PARAM_MIN,
          RUP_OFFSET_PARAM_MAX,RUP_OFFSET_PARAM_UNITS,DEFAULT_RUP_OFFSET_VAL);
      rupOffset_Param.setInfo(RUP_OFFSET_PARAM_INFO);

      gridSpacing_Param = new DoubleParameter(GRID_SPACING_PARAM_NAME,GRID_SPACING_PARAM_MIN,
          GRID_SPACING_PARAM_MAX,GRID_SPACING_PARAM_UNITS,DEFAULT_GRID_SPACING_VAL);
      gridSpacing_Param.setInfo(GRID_SPACING_PARAM_INFO);

      deltaMag_Param = new DoubleParameter(DELTA_MAG_PARAM_NAME,DELTA_MAG_PARAM_MIN,
          DELTA_MAG_PARAM_MAX,null,DEFAULT_DELTA_MAG_VAL);
      deltaMag_Param.setInfo(DELTA_MAG_PARAM_INFO);

      numRealizationsParam = new IntegerParameter(NUM_REALIZATIONS_PARAM_NAME,DEFAULT_NUM_REALIZATIONS_VAL);
      numRealizationsParam.setInfo(NUM_REALIZATIONS_PARAM_INFO);

      // add adjustable parameters to the list
      adjustableParams = new ParameterList();
      adjustableParams.addParameter(rupOffset_Param);
      adjustableParams.addParameter(gridSpacing_Param);
      adjustableParams.addParameter(deltaMag_Param);
      adjustableParams.addParameter(backSeisParam);
      adjustableParams.addParameter(grTailParam);
      adjustableParams.addParameter(numRealizationsParam);

    }


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
      else if(funcToCall.equalsIgnoreCase(ERF_ServletModeGuiBean.getERF_ListAPI)){
        System.out.println("Getting the List object");
        ParameterList paramList=(ParameterList)inputFromApplet.readObject();
        TimeSpan time=(TimeSpan)inputFromApplet.readObject();
        outputToApplet.writeObject(this.getERF_ListAPI(time,paramList));
        System.out.println("Received the ParamList and TimeSpan");
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
    * @returns null for now
    */
    public ParameterList getAdjustableParams() {
      if(this.adjustableParams==null)
        //reads the WG-02 file and add the adjustable params to the ParamList
        this.WG02_ERF_Epistemic_List();

      return this.adjustableParams;
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
    * @returns the object for the ERF_List
    */
   public ERF_ListAPI getERF_ListAPI(TimeSpan time, ParameterList param){
     System.out.println("Inside the getERF_List function");
     numIterations = ((Integer)adjustableParams.getParameter(this.NUM_REALIZATIONS_PARAM_NAME).getValue()).intValue();
     rupOffset = ((Double)adjustableParams.getParameter(this.RUP_OFFSET_PARAM_NAME).getValue()).doubleValue();
     deltaMag = ((Double)adjustableParams.getParameter(this.DELTA_MAG_PARAM_NAME).getValue()).doubleValue();
     gridSpacing = ((Double)adjustableParams.getParameter(this.GRID_SPACING_PARAM_NAME).getValue()).doubleValue();
     backSeis = (String)adjustableParams.getParameter(this.BACK_SEIS_NAME).getValue();
     grTail = (String)adjustableParams.getParameter(this.GR_TAIL_NAME).getValue();
     System.out.println("numIter:"+numIterations+";rupOffset:"+rupOffset+";deltaMag:"+deltaMag+";gridSpacing:"+";backSies:"+backSeis+";grTail:"+grTail);
     WG02_ERF_ListObject wg02_ERF_List= new WG02_ERF_ListObject(this.inputFileLines,this.iterationLineNumbers,
                                    this.numIterations,this.rupOffset,this.deltaMag,this.gridSpacing,
                                    this.backSeis,this.grTail);
     wg02_ERF_List.setTimeSpan(time);
     return wg02_ERF_List;
   }

 /**
  * return the time span object
  *
  * @return : time span object is returned which contains start time and duration
  */
 public TimeSpan getTimeSpan() {
   return this.timeSpan;
 }

}

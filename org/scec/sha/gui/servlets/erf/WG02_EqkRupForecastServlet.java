package org.scec.sha.gui.servlets.erf;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.scec.sha.gui.servlets.erf.ERF_API;
import org.scec.sha.gui.servlets.erf.ERF_WebServiceAPI;
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
import org.scec.util.RunScript;
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
  private final static String WG02_CODE_PATH ="/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/wg99/wg99_src_v27/";
  private final static String WG02_INPUT_FILE ="base_mod_23_wgt_1K.inp";
  private final static String INPUT_FILE_NAME = WG02_CODE_PATH+"WG02_WRAPPER_INPUT.DAT";

  // vector to hold the line numbers where each iteration starts
  private Vector iterationLineNumbers;


  // This is an array holding each line of the input file
  private ArrayList inputFileLines = null;

  // adjustable params for each forecast
  protected ParameterList adjustableParams = null;

  //timespan object
  private TimeSpan timeSpan;

  //Instance for the WG-02 AdjustableParam Class
  private WG02_ERF_AdjustableParamsClass wg02_AdjustableParams;


  /**
   * Initialises the WG-02 Adjustable ParameterList
   */
  private void initWG02_List(){
    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.YEARS,TimeSpan.YEARS);
    // create and add adj params to list
    wg02_AdjustableParams = new WG02_ERF_AdjustableParamsClass();
    this.adjustableParams = wg02_AdjustableParams.getAdjustableParams();
  }

  /**
   * Creates the input file for the OpenSHA by running the fortran code for WG-02
   * It also reads that input file and sets value for timeSpan
   */
  private void WG02_ERF_Epistemic_List() {

    //get the fortran code for the WG-02 running
    initWG02_Code();

    // read the lines of the input files into a list
    try{ inputFileLines = FileUtils.loadFile( INPUT_FILE_NAME ); }
    catch( FileNotFoundException e){ System.out.println(e.toString()); }
    catch( IOException e){ System.out.println(e.toString());}

    // Exit if no data found in list
    if( inputFileLines == null) throw new
      FaultException("No data loaded from "+INPUT_FILE_NAME+". File may be empty or doesn't exist.");
    System.out.println("Input File Lines:"+inputFileLines.size());

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

    System.out.println("Number of Iterations:"+iterationLineNumbers.size());

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


  //runs the Fortran code for the WG-02 and generates the Output file that is read by OpenSHA
  private void initWG02_Code(){

    String realizationString ="number of Monte Carlo realizations";

    //flag to check if we have to run the WG-02 fortran code
    boolean runWG02_Code = true;
    int numRealization = ((Integer)adjustableParams.getParameter(WG02_ERF_AdjustableParamsClass.NUM_REALIZATIONS_PARAM_NAME).getValue()).intValue();

    try {
      FileReader fr = new FileReader(WG02_CODE_PATH+WG02_INPUT_FILE);
      BufferedReader  br = new BufferedReader(fr);
      String lineFromInputFile = br.readLine();
      ArrayList fileLines = new ArrayList();
      //reading each line of file until the end of file
      while(lineFromInputFile != null){
        //System.out.println("Inside the while loop");
        //reading each line from input wg02 file and checking if it is equals to
       // number of realization value setup
        if(lineFromInputFile.endsWith(realizationString)){
          //if the line is for the number of realization setup, check if the
          //parameter value is equal to the value in the file,
          //if so then don't run the WG-02 fortran code again, else replace this
          //value in the file with the value in the parameter
          StringTokenizer st = new StringTokenizer(lineFromInputFile);
          int realizationValue = (new Integer(st.nextToken())).intValue();
          System.out.println("RealizationValue: "+realizationValue+";;numRealization: "+numRealization);
          if(numRealization == realizationValue){
            runWG02_Code = false;
            fileLines= null;
            break;
          }
          else
            lineFromInputFile = (numRealization+1) +"  "+realizationString;
        }
        fileLines.add(lineFromInputFile);
        lineFromInputFile = br.readLine();
      }
      br.close();
      System.out.println("Flag: "+runWG02_Code);
      //generates the new input file and run the WG-02 fortran code only if
      //number of realizations have changed.
      if(runWG02_Code){
        System.out.println("Creating the input files");
        //overwriting the WG-02 input file with the changes in the file
        FileWriter fw = new FileWriter(WG02_CODE_PATH+WG02_INPUT_FILE);
        BufferedWriter bw = new BufferedWriter(fw);
        ListIterator it= fileLines.listIterator();
        while(it.hasNext())
          bw.write((String)it.next()+"\n");
        bw.close();

        //Command to be executed for the WG-02
        String wg02_Command="wg99_main "+WG02_INPUT_FILE;
        //creating the shell script  file to run the WG-02 code
        fw= new FileWriter(WG02_CODE_PATH+"wg02.sh");
        bw=new BufferedWriter(fw);
        bw.write("cd "+WG02_CODE_PATH+"\n");
        bw.write(wg02_Command+"\n");
        bw.close();
        //command to be executed during the runtime.
        String[] command ={"sh","-c","sh "+ WG02_CODE_PATH+"wg02.sh"};
        RunScript.runScript(command);
        //command[2]="rm "+WG02_CODE_PATH+"*.out*";
        //RunScript.runScript(command);
        command[2]="rm "+WG02_CODE_PATH+"wg02.sh";
        RunScript.runScript(command);
      }
    }catch(Exception e){
      e.printStackTrace();
    }
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
      if(this.adjustableParams==null){
        //reads the WG-02 file and add the adjustable params to the ParamList
        this.initWG02_List();
        this.WG02_ERF_Epistemic_List();
      }

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
   public ERF_ListAPI getERF_ListAPI(TimeSpan time, ParameterList params){
     System.out.println("Inside the getERF_List function");

     int numIterations = ((Integer)params.getParameter(WG02_ERF_AdjustableParamsClass.NUM_REALIZATIONS_PARAM_NAME).getValue()).intValue();
     double rupOffset = ((Double)params.getParameter(WG02_ERF_AdjustableParamsClass.RUP_OFFSET_PARAM_NAME).getValue()).doubleValue();
     double deltaMag = ((Double)params.getParameter(WG02_ERF_AdjustableParamsClass.DELTA_MAG_PARAM_NAME).getValue()).doubleValue();
     double gridSpacing = ((Double)params.getParameter(WG02_ERF_AdjustableParamsClass.GRID_SPACING_PARAM_NAME).getValue()).doubleValue();
     this.adjustableParams = params;
     //System.out.println("NumIterations:"+numIterations);
     //craetes the Input file for the OpenSHA from fortran code
     this.WG02_ERF_Epistemic_List();


     String backSeis = (String)params.getParameter(WG02_ERF_AdjustableParamsClass.BACK_SEIS_NAME).getValue();
     String grTail = (String)params.getParameter(WG02_ERF_AdjustableParamsClass.GR_TAIL_NAME).getValue();
     System.out.println("numIter:"+numIterations+";rupOffset:"+rupOffset+";deltaMag:"+deltaMag+";gridSpacing:"+";backSies:"+backSeis+";grTail:"+grTail);
     WG02_ERF_ListObject wg02_ERF_List= new WG02_ERF_ListObject(this.inputFileLines,this.iterationLineNumbers,
                                    numIterations,rupOffset,deltaMag,gridSpacing,
                                    backSeis,grTail);
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

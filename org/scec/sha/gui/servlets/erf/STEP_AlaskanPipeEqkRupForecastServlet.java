package org.scec.sha.gui.servlets.erf;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.net.*;

import org.scec.sha.gui.servlets.erf.ERF_API;
import org.scec.sha.gui.servlets.erf.ERF_WebServiceAPI;
import org.scec.sha.gui.beans.ERF_ServletModeGuiBean;
import org.scec.param.*;
import org.scec.sha.fault.*;
import org.scec.sha.surface.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.magdist.parameter.MagFreqDistParameter;
import org.scec.sha.magdist.*;
import org.scec.param.event.*;
import org.scec.data.*;
import org.scec.data.region.*;
import org.scec.util.FileUtils;
import org.scec.exceptions.FaultException;
import org.scec.calc.MomentMagCalc;
import org.scec.util.FileUtils;
import org.scec.sha.gui.servlets.erf.STEP_AlaskanPipeEqkRupForecastObject;
import org.scec.sha.gui.servlets.erf.STEP_ERF_AlaskanPipeAdjustableParamClass;


/**
 * <p>Title: STEP_AlaskanPipeEqkRupForecastServlet</p>
 * <p>Description: This is the Servlet mode implementation of the Alaskan Pipe STEP Forecast model</p>
 * @author : Nitin Gupta and Ned Field
 * @created : Aug 21,2003
 * @version 1.0
 */


public class STEP_AlaskanPipeEqkRupForecastServlet extends HttpServlet implements ERF_WebServiceAPI{

  private static final String className ="STEP Alaskan Pipeline ERF";

  // Input file name
  private final static String INPUT_FILE_NAME = "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/WEB-INF/dataFiles/PipelineGrid.txt";

  // ArrayList of input file lines
  private ArrayList inputFileLines;


  // adjustable params for each forecast
  protected ParameterList adjustableParams = null;
  // timespan object for each forecast
  protected TimeSpan timeSpan;

  //Instance of the STEP Adjustable Param class
  private STEP_ERF_AlaskanPipeAdjustableParamClass stepAdjParams;

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
   * Initialises the Forecast Param List and TimeSpan
   */

  private void STEP_AlaskanPipeEqkRupForecastServlet() {

    // read the lines of the input files into a list
    try{
      inputFileLines = FileUtils.loadFile(this.INPUT_FILE_NAME);
      //creates the instance of the Step Adjustable Param class
      this.stepAdjParams =new STEP_ERF_AlaskanPipeAdjustableParamClass();
    }catch(Exception e){
      e.printStackTrace();
    }
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
   * get the adjustable parameters for this forecast
   *
   * @return
   */
  public ParameterList getAdjustableParams(){
    if(this.inputFileLines == null)
      STEP_AlaskanPipeEqkRupForecastServlet();
    adjustableParams = stepAdjParams.getAdjustableParams();
    return this.adjustableParams;
  }

  /**
   * Get the region for which this forecast is applicable
   * @return : Geographic region object specifying the applicable region of forecast
   */
  public GeographicRegion getApplicableRegion() {
    return null;
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
    STEP_AlaskanPipeEqkRupForecastObject stepERF =
        new STEP_AlaskanPipeEqkRupForecastObject(time,this.inputFileLines);

    return stepERF;
  }


  /**
   * return the time span object
   *
   * @return : time span object is returned which contains start time and duration
   */
  public TimeSpan getTimeSpan() {
    // Create the timeSpan & set its constraints
    StringTokenizer st = new StringTokenizer(inputFileLines.get(0).toString());
    int year =  (new Integer(st.nextToken())).intValue();
    int month =  (new Integer(st.nextToken())).intValue();
    int day =  (new Integer(st.nextToken())).intValue();
    int hour =  (new Integer(st.nextToken())).intValue();
    int minute =  (new Integer(st.nextToken())).intValue();
    int second =  (new Integer(st.nextToken())).intValue();


    st = new StringTokenizer(inputFileLines.get(1).toString());
    double duration = (new Double(st.nextToken())).doubleValue();

    this.timeSpan = new TimeSpan(TimeSpan.SECONDS,TimeSpan.DAYS);
    timeSpan.setStartTime(year,month,day,hour,minute,second);
    timeSpan.setDuration(duration);
    timeSpan.setStartTimeConstraint(TimeSpan.START_YEAR, year,year);
    timeSpan.setStartTimeConstraint(TimeSpan.START_MONTH, month,month);
    timeSpan.setStartTimeConstraint(TimeSpan.START_DAY, day,day);
    timeSpan.setStartTimeConstraint(TimeSpan.START_HOUR, hour,hour);
    timeSpan.setStartTimeConstraint(TimeSpan.START_MINUTE, minute,minute);
    timeSpan.setStartTimeConstraint(TimeSpan.START_SECOND, second,second);
    timeSpan.setDuractionConstraint(duration,duration);

    return timeSpan;
  }

}

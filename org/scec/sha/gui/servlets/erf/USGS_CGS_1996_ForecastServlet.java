package org.scec.sha.gui.servlets.erf;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.scec.sha.gui.servlets.erf.ERF_API;
import org.scec.sha.gui.servlets.erf.ERF_WebServiceAPI;
import org.scec.sha.gui.servlets.erf.USGS_CGS_1996_ERF_Object;
import org.scec.sha.gui.beans.ERF_ServletModeGuiBean;
import org.scec.param.*;
import org.scec.sha.fault.*;
import org.scec.sha.surface.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.Frankel96.*;
import org.scec.sha.magdist.parameter.MagFreqDistParameter;
import org.scec.sha.magdist.*;
import org.scec.param.event.*;
import org.scec.data.*;
import org.scec.data.region.*;
import org.scec.util.FileUtils;
import org.scec.exceptions.FaultException;
import org.scec.calc.MomentMagCalc;
import org.scec.sha.gui.servlets.erf.USGS_CGS_1996_ERF_AdjustableParamsClass;

/**
 * <p>Title: USGS_CGS_1996_ForecastServlet</p>
 * <p>Description: This is the Servlet mode implementation of the USGS/CSG-1996
 * Forecast model</p>
 * @author : Nitin Gupta and Ned Field
 * @created : Aug 21,2003
 * @version 1.0
 */

public class USGS_CGS_1996_ForecastServlet extends HttpServlet implements ERF_WebServiceAPI{

  private static final String className ="USGS/CGS 1996 Adj. Cal. ERF";

  /**
   * Static variable for input file names
   */
  private final static String INPUT_FAULT_FILE_NAME = "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/WEB-INF/dataFiles/Frankel96_CAL_all.txt";
  private final static String INPUT_BACK_SEIS_FILE_NAME = "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/WEB-INF/dataFiles/CAagrid.asc";



  // This is an array holding each line of the input file
  private ArrayList inputFaultFileLines = null;
  private ArrayList inputBackSeisFileLines = null;



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
   * Initialises the Forecast Param List and TimeSpan
   */

  private void USGS_CGS_1996_EqkRupForecast() {

    // create the timespan object with start time and duration in years
    timeSpan = new TimeSpan(TimeSpan.NONE,TimeSpan.YEARS);

    //gets the adjustable Params from the USGS/CGS(1996) Param class
    USGS_CGS_1996_ERF_AdjustableParamsClass adjParamClass = new USGS_CGS_1996_ERF_AdjustableParamsClass();
    this.adjustableParams = adjParamClass.getAdjustableParams();

    // read the lines of the input files into a list
    try{ inputFaultFileLines = FileUtils.loadFile( INPUT_FAULT_FILE_NAME ); }
    catch( FileNotFoundException e){ System.out.println(e.toString()); }
    catch( IOException e){ System.out.println(e.toString());}


    // Exit if no data found in list
    if( inputFaultFileLines == null) throw new
           FaultException("No data loaded from "+INPUT_FAULT_FILE_NAME+". File may be empty or doesn't exist.");


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
    if(this.adjustableParams==null)
      // creates the Adjustable Parameters and reads the flat file
      this.USGS_CGS_1996_EqkRupForecast();

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
   String backSiesOption = (String)param.getParameter(USGS_CGS_1996_ERF_AdjustableParamsClass.BACK_SEIS_NAME).getValue();
   //checks when to read the BackSies File
   if(backSiesOption.equals(USGS_CGS_1996_ERF_AdjustableParamsClass.BACK_SEIS_INCLUDE)||
      backSiesOption.equals(USGS_CGS_1996_ERF_AdjustableParamsClass.BACK_SEIS_ONLY)){
     try{ inputBackSeisFileLines = FileUtils.loadFile( INPUT_BACK_SEIS_FILE_NAME ); }
     catch( FileNotFoundException e){ System.out.println(e.toString()); }
     catch( IOException e){ System.out.println(e.toString());}
     // Exit if no data found in list
     if( inputBackSeisFileLines == null) throw new
       FaultException("No data loaded from "+INPUT_BACK_SEIS_FILE_NAME+". File may be empty or doesn't exist.");
   }
   else{
     inputBackSeisFileLines= null;
   }


   USGS_CGS_1996_ERF_Object erf = new USGS_CGS_1996_ERF_Object(param,
       this.inputBackSeisFileLines, this.inputFaultFileLines,time);
   System.out.println("Successfully created the erf for the USGS/CGS (1996 model");
   return erf;
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


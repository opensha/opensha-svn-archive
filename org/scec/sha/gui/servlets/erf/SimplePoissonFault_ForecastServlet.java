package org.scec.sha.gui.servlets.erf;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.scec.sha.earthquake.ERF_API;
import org.scec.sha.gui.servlets.erf.ERF_WebServiceAPI;

import org.scec.sha.gui.beans.ERF_ServletModeGuiBean;
import org.scec.param.*;
import org.scec.sha.fault.*;
import org.scec.sha.surface.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.SimplePoissonFaultERF;
import org.scec.sha.param.MagFreqDistParameter;
import org.scec.sha.magdist.*;
import org.scec.param.event.*;
import org.scec.data.*;
import org.scec.data.region.*;
import org.scec.util.FileUtils;
import org.scec.exceptions.FaultException;
import org.scec.calc.MomentMagCalc;



/**
 * <p>Title: SimplePoissonFault_ForecastServlet</p>
 * <p>Description: This is the Servlet mode implementation of the SimplePoissonERF
 * Forecast model</p>
 * @author : Nitin Gupta and Ned Field
 * @created : Aug 21,2003
 * @version 1.0
 */

public class SimplePoissonFault_ForecastServlet extends HttpServlet implements ERF_WebServiceAPI{


  //object for the SimplePoisson Ekq Forecast
  SimplePoissonFaultERF simplePoissonForecast = new SimplePoissonFaultERF();

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
    return this.simplePoissonForecast.getName();
  }

  /**
   * get the adjustable parameters for this forecast
   *
   * @return
   */
  public ParameterList getAdjustableParams(){
    return simplePoissonForecast.getAdjustableParameterList();
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
 public ERF_API getERF_API(TimeSpan time, ParameterList params){
   SimplePoissonFaultERF simplePoissonERF = new SimplePoissonFaultERF();

   //getting the parameterList for the SimplePoisson EQforecast
   ParameterList paramList = simplePoissonERF.getAdjustableParameterList();
   //getting the iterators for the parameter passed as the argument in thgis function, called from applet
   ListIterator it = params.getParametersIterator();
   //setting the updated values for the adjustable params from the applet
   // in the parameterList.
   while(it.hasNext()){
     ParameterAPI tempParam = (ParameterAPI)it.next();
     paramList.getParameter(tempParam.getName()).setValue(tempParam.getValue());
   }
   simplePoissonERF.setParameterChangeFlag(true);
   simplePoissonERF.updateForecast();
   return (ERF_API)simplePoissonERF;
 }



 /**
  * return the time span object
  *
  * @return : time span object is returned which contains start time and duration
  */
 public TimeSpan getTimeSpan() {
   return simplePoissonForecast.getTimeSpan();
 }

}


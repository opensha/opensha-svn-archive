package org.scec.sha.gui.servlets.erf;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;



import org.scec.sha.gui.beans.ERF_ServletModeGuiBean;
import org.scec.param.*;
import org.scec.sha.fault.*;
import org.scec.sha.surface.*;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.WG02.WG02_FortranWrappedERF_EpistemicList;
import org.scec.sha.param.MagFreqDistParameter;
import org.scec.sha.magdist.*;
import org.scec.param.event.*;
import org.scec.data.*;
import org.scec.data.region.*;
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

  // Object for the WG-02 ERF
  WG02_FortranWrappedERF_EpistemicList wg02 = new WG02_FortranWrappedERF_EpistemicList();

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
    return wg02.getName();
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
      return wg02.getAdjustableParameterList();
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
     WG02_FortranWrappedERF_EpistemicList wg02Object = new WG02_FortranWrappedERF_EpistemicList();

     //getting the parameterList for the WG-02 forecast
     ParameterList paramList = wg02Object.getAdjustableParameterList();
     //getting the iterators for the parameter passed as the argument in thgis function, called from applet
     ListIterator it = params.getParametersIterator();
     //setting the updated values for the adjustable params from the applet
     // in the parameterList.
     while(it.hasNext()){
       ParameterAPI tempParam = (ParameterAPI)it.next();
       paramList.getParameter(tempParam.getName()).setValue(tempParam.getValue());
     }
     wg02Object.setTimeSpan(time);
     wg02Object.setParameterChangeFlag(true);
     wg02Object.updateForecast();
     return (ERF_ListAPI)wg02Object;
   }

 /**
  * return the time span object
  *
  * @return : time span object is returned which contains start time and duration
  */
 public TimeSpan getTimeSpan() {
   return wg02.getTimeSpan();
 }

}

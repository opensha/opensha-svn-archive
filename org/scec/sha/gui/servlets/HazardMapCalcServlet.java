package org.scec.sha.gui.servlets;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.sha.imr.AttenuationRelationshipAPI;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.sha.calc.HazardMapCalculator;

/**
 * <p>Title: HazardMapCalcServlet </p>
 * <p>Description: this servlet generates the data sets based on the parameters
 * set by the user in applet </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class HazardMapCalcServlet extends HttpServlet {

  //Process the HTTP Get request
 public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

   try {

     // get an input stream from the applet
     ObjectInputStream inputFromApplet = new ObjectInputStream(request.getInputStream());

     /**
      * get the vector of x values for cond prob. function
      */
     // get the vector of x-values for cond prob func
     Vector condProbVector  = (Vector) inputFromApplet.readObject();
     // now convert this vector to arbitrary discretized function
     ArbitrarilyDiscretizedFunc condProbFunc = convertToFunc(condProbVector);

     /**
      * get the vector of x-values for hazfunction
      * and fill it in array of doubles
      */
     Vector hazVector  = (Vector) inputFromApplet.readObject();
     int num = hazVector.size();
     double []xValues = new double[hazVector.size()];
     for(int i=0; i<num; ++i)
       xValues[i] = ((Double)hazVector.get(i)).doubleValue();


     /**
      * get the sites for which this needs to be calculated
      */
     SitesInGriddedRegion sites =
         (SitesInGriddedRegion) inputFromApplet.readObject();

     /**
      * get the selected IMR
      */
     AttenuationRelationshipAPI imr =
         (AttenuationRelationshipAPI) inputFromApplet.readObject();


     /**
      * get the selected EqkRupForecast
      */
     EqkRupForecast eqkRupForecast =
         (EqkRupForecast) inputFromApplet.readObject();

     /**
      * get the parameter values in String form needed to reproduce this
      */
     String mapParametersInfo = (String) inputFromApplet.readObject();

     // report to the user whether the operation was successful or not
     // get an ouput stream from the applet
     ObjectOutputStream outputToApplet = new ObjectOutputStream(response.getOutputStream());
     outputToApplet.writeObject(new String("Success"));
     outputToApplet.close();

     // now run the calculation
     HazardMapCalculator calc = new HazardMapCalculator();
     boolean flag = true;
     calc.getHazardMapCurves(flag, xValues, sites, imr,
                             eqkRupForecast, mapParametersInfo );

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
   * This function accepts the vector of Doubles.
   * These double values are the x-values and we  make
   * Arbitrarily Discretized func with that
   * @param input
   * @return
   */
 private ArbitrarilyDiscretizedFunc convertToFunc(Vector input) {
   int num = input.size();
   ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
   for(int i=0; i<num; ++i)
     func.set(((Double)input.get(i)).doubleValue(), 1);
   return func;
 }

}
package org.opensha.nshmp.sha.calc.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Title: HazardDataCalcServlet.java </p>
 * <p>Description: this class is called from the application. This servlet calls
 * the HazardDataCalcServletHelper which in turn calls the HazardDataCalc to
 * return the results to the application on user machine </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class HazardDataCalcServlet extends HttpServlet{
   private HazardDataCalcServletHelper hazardDataCalcServletHelper = new HazardDataCalcServletHelper();
   //Process the HTTP Get request
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws
   ServletException, IOException {

     try {
       // get an input stream from the applet
       ObjectInputStream inputFromApplet = new ObjectInputStream(request.
           getInputStream());
       //get method name and method parameters
       String mathodName = (String) inputFromApplet.readObject();
       ArrayList parameters = (ArrayList) inputFromApplet.readObject();
       //  get result based on method call
       Object result = hazardDataCalcServletHelper.getResult(mathodName, parameters);
       // return the result to the applet
       ObjectOutputStream outputToApplet = new ObjectOutputStream(response.
           getOutputStream());
       outputToApplet.writeObject(result);
       outputToApplet.close();
     }catch (Exception e) {
       // report to the user whether the operation was successful or not
       e.printStackTrace();
     }
   }



   //Process the HTTP Post request
   public void doPost(HttpServletRequest request, HttpServletResponse response) throws
       ServletException, IOException {
     // call the doPost method
     doGet(request, response);
   }


}
package org.scec.sha.gui.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.scec.data.region.*;
import org.scec.data.Location;
import org.scec.data.LocationList;
import org.scec.sha.gui.servlets.siteEffect.BasinDepthClass;

/**
 * <p>Title: SCEC_BasinDepthForGriddedRegionServlet  </p>
 * <p>Description: This Servlet finds the VS30 and Basin Depth for the given
 * region. this needs to be fixed with the implementation of the Appliacble Region
 * Object.
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class SCEC_BasinDepthForGriddedRegionServlet  extends HttpServlet {



  //Basin depth file
  private final String BASIN_DEPTH_FILENAME = "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/WEB-INF/dataFiles/basindepth_OpenSHA.txt";


  /**
   * method to get the basin depth as desired by the user
   *
   * @param request
   * @param response
   * @throws IOException
   * @throws ServletException
   */
  public void doGet(HttpServletRequest request,  HttpServletResponse response)
                                  throws IOException, ServletException {
    //Vectors for computing the lat and lons for the given gridded region
    ArrayList locationVector= new ArrayList();
    try {
      // get all the input stream from the applet
      ObjectInputStream inputFromApplication = new ObjectInputStream(request.getInputStream());

      //gets the locationlist from the application
      LocationList locList = (LocationList)inputFromApplication.readObject();

      //close of the input from the application
      inputFromApplication.close();
      //creating the object for the Basin Depth Class
      BasinDepthClass basinDepthClass = new  BasinDepthClass(locList,BASIN_DEPTH_FILENAME);
      //sending the output in the form of the arrayList back to the calling application.
      ObjectOutputStream output = new ObjectOutputStream(response.getOutputStream());
      output.writeObject(basinDepthClass.getBasinDepth());
      output.close();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * This method just calls the doPost method
   *
   * @param request : Request Object
   * @param response : Response Object
   * @throws IOException : Throws IOException during read-write from connection stream
   * @throws ServletException
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doGet(request,response);
  }
}

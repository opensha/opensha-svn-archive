package org.opensha.sha.gui.servlets;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.opensha.commons.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.commons.data.XYZ_DataSetAPI;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.mapping.GMT_MapGeneratorForShakeMaps;



/**
 * <p>Title: ScenarioShakeMapForHazusGeneratorServlet </p>
 * <p>Description: This servlet creates and runs the GMT script on the server.
 * It creates the script to make map and data for the Hazus.
 *  It returns back the URLs to the generated image files for Hazus.</p>
 * @author :Ned Field , Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class ScenarioShakeMapForHazusGeneratorServlet extends HttpServlet {


  //Process the HTTP Get request
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    try{
      // get an input stream from the applet
      ObjectInputStream inputFromApplet = new ObjectInputStream(request.getInputStream());

      //gets the GMT_MapGeneratorForShakeMaps object from the application
      GMT_MapGeneratorForShakeMaps gmtMap = (GMT_MapGeneratorForShakeMaps)inputFromApplet.readObject();

      //gets the file name for SA-0.3 XYZ data file for which we want to create the map for
      String sa_03xyzDataFileName = (String)inputFromApplet.readObject();

      //gets the file name for SA-1.0 XYZ data file for which we want to create the map for
      String sa_10xyzDataFileName = (String)inputFromApplet.readObject();

      //gets the file name for pga XYZ data file for which we want to create the map for
      String pga_xyzDataFileName = (String)inputFromApplet.readObject();

      //gets the file name for pgv XYZ data file for which we want to create the map for
      String pgv_xyzDataFileName = (String)inputFromApplet.readObject();

      //gets the Eqkrupture object
      EqkRupture rupture = (EqkRupture)inputFromApplet.readObject();

      //gets the metadata for the map parameters
      String metadata = (String)inputFromApplet.readObject();

      //receiving the name of the input directory
      String dirName = (String)inputFromApplet.readObject();

      //reading the sa-0.3 XYZ dataset from the file
      XYZ_DataSetAPI sa_03xyzData = (XYZ_DataSetAPI)FileUtils.loadObject(sa_03xyzDataFileName);

      //reading the sa-1.0 XYZ dataset from the file
      XYZ_DataSetAPI sa_10xyzData = (XYZ_DataSetAPI)FileUtils.loadObject(sa_10xyzDataFileName);

      //reading the pga XYZ dataset from the file
      XYZ_DataSetAPI pga_xyzData = (XYZ_DataSetAPI)FileUtils.loadObject(pga_xyzDataFileName);

      //reading the pgv XYZ dataset from the file
      XYZ_DataSetAPI pgvxyzData = (XYZ_DataSetAPI)FileUtils.loadObject(pgv_xyzDataFileName);


      //creates and run the GMT Script on the server and return back the URL to all the images
      Object webaddr = gmtMap.makeHazusFileSetUsingServlet(sa_03xyzData,sa_10xyzData,
          pga_xyzData,pgvxyzData,rupture,metadata,dirName);

      //making the XYZ dataset objects to be null.
      sa_03xyzData = null;
      sa_10xyzData = null;
      pga_xyzData = null;
      pgvxyzData = null;

      // get an ouput stream from the applet
      ObjectOutputStream outputToApplet = new ObjectOutputStream(response.getOutputStream());

      //name of the image file as the URL
      outputToApplet.writeObject(webaddr);
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

}

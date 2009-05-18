package org.opensha.sha.gui.servlets;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.opensha.param.ParameterList;
import org.opensha.commons.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.commons.data.XYZ_DataSetAPI;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.mapping.GMT_MapGeneratorForShakeMaps;
import org.opensha.util.FileUtils;



/**
 * <p>Title: ScenarioShakeMapGeneratorServlet </p>
 * <p>Description: This servlet creates and runs the GMT script on the server.
 *  It returns back the URL to the file.</p>
 * @author :Ned Field , Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class ScenarioShakeMapGeneratorServlet
    extends HttpServlet {

  //Process the HTTP Get request
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException {

    // get an ouput stream from the applet
    ObjectOutputStream outputToApplet = new ObjectOutputStream(response.
        getOutputStream());

    try {
      // get an input stream from the applet
      ObjectInputStream inputFromApplet = new ObjectInputStream(request.
          getInputStream());

      //gets the GMT_MapGeneratorForShakeMaps object from the application
      GMT_MapGeneratorForShakeMaps gmtMap = (GMT_MapGeneratorForShakeMaps)
          inputFromApplet.readObject();

      //gets the file name of the XYZ data file for which we want to create the map for
      String xyzDataFileName = (String) inputFromApplet.readObject();

      //gets the Eqkrupture object
      EqkRupture rupture = (EqkRupture) inputFromApplet.readObject();

      //gets the selected IMT
      String imt = (String) inputFromApplet.readObject();

      //gets the metadata for the map parameters
      String metadata = (String) inputFromApplet.readObject();

      //receiving the name of the input directory
      String dirName = (String) inputFromApplet.readObject();

      //reading the XYZ dataset from the file
      XYZ_DataSetAPI xyzData = (XYZ_DataSetAPI) FileUtils.loadObject(
          xyzDataFileName);

      //creates and run the GMT Script on the server
      Object webaddr = gmtMap.makeMapUsingServlet(xyzData, rupture, imt,
                                                  metadata,
                                                  dirName);



      //name of the image file as the URL
      outputToApplet.writeObject(webaddr);
      outputToApplet.close();
    }
    catch (Exception e) {
      //sending the error message back to the application
      outputToApplet.writeObject(new RuntimeException(e.getMessage()));
      outputToApplet.close();
    }
  }

  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException {
    // call the doPost method
    doGet(request, response);
  }

}

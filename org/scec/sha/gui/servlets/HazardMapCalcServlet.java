package org.scec.sha.gui.servlets;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;


import org.scec.data.region.SitesInGriddedRegion;
import org.scec.sha.imr.AttenuationRelationshipAPI;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.sha.calc.SubmitJobForGridComputation;

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
  // parent directory where each new calculation will have its own subdirectory
  private String PARENT_DIR = "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/HazardMapDatasets/";
  // filenames for IMR, ERF, Region, metadata
  private String IMR_FILE_NAME = "imr.obj";
  private String ERF_FILE_NAME = "erf.obj";
  private String REGION_FILE_NAME = "region.obj";
  private String METADATA_FILE_NAME = "metadata.txt";

  //Process the HTTP Get request
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException {

    try {

      // get an input stream from the applet
      ObjectInputStream inputFromApplet = new ObjectInputStream(request.
          getInputStream());

      //get the sites for which this needs to be calculated
      SitesInGriddedRegion sites = (SitesInGriddedRegion) inputFromApplet.
          readObject();
      //get the selected IMR
      AttenuationRelationshipAPI imr = (AttenuationRelationshipAPI)
          inputFromApplet.readObject();
      //get the selected EqkRupForecast
      EqkRupForecast eqkRupForecast =
          (EqkRupForecast) inputFromApplet.readObject();
      //get the parameter values in String form needed to reproduce this
      String mapParametersInfo = (String) inputFromApplet.readObject();

      // report to the user whether the operation was successful or not
      // get an ouput stream from the applet
      ObjectOutputStream outputToApplet = new ObjectOutputStream(response.
          getOutputStream());
      outputToApplet.writeObject(new String("Success"));
      outputToApplet.close();

      // write the objects to the files
      File mainDir = new File(PARENT_DIR);
      int newDirId = mainDir.list().length + 1;
      String newDir = this.PARENT_DIR+newDirId+"/";
      String regionFileName = newDir+this.REGION_FILE_NAME;
      String imrFileName = newDir+this.IMR_FILE_NAME;
      String erfFileName = newDir+this.ERF_FILE_NAME;
      writeObjectsToFiles(sites, imr, eqkRupForecast, regionFileName, imrFileName,
                          erfFileName);

      // now run the calculation on grid
      SubmitJobForGridComputation computation =
          new SubmitJobForGridComputation(IMR_FILE_NAME, ERF_FILE_NAME,
                                     REGION_FILE_NAME, newDir, newDirId, sites);
    }
    catch (Exception e) {
      // report to the user whether the operation was successful or not
      e.printStackTrace();
    }
  }

  /**
   * Write the objects to their respective files
   * @param sites
   * @param imr
   * @param eqkRupForecast
   * @param outputDirId
   */
  private void writeObjectsToFiles(SitesInGriddedRegion sites,
                                   AttenuationRelationshipAPI imr,
                                   EqkRupForecast eqkRupForecast,
                                   String regionFileName,
                                   String imrFileName,
                                   String erfFileName) {

    try {
      // write region object to the file
      FileOutputStream fileOutSites = new FileOutputStream(regionFileName);
      ObjectOutputStream objectStreamSites = new ObjectOutputStream(
          fileOutSites);
      objectStreamSites.writeObject(sites);
      objectStreamSites.close();
      fileOutSites.close();
    }catch(Exception e ) { e.printStackTrace(); }

    try {
      // write imr object to the file
      FileOutputStream fileOutIMR = new FileOutputStream(imrFileName);
      ObjectOutputStream objectStreamIMR = new ObjectOutputStream(
          fileOutIMR);
      objectStreamIMR.writeObject(imr);
      objectStreamIMR.close();
      fileOutIMR.close();
    }catch(Exception e) { e.printStackTrace();}

    try {
     // write erf object to the file
     FileOutputStream fileOutERF = new FileOutputStream(erfFileName);
     ObjectOutputStream objectStreamERF = new ObjectOutputStream(
         fileOutERF);
     objectStreamERF.writeObject(eqkRupForecast);
     objectStreamERF.close();
     fileOutERF.close();
   }catch(Exception e) { e.printStackTrace();}
  }


  //Process the HTTP Post request
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws
      ServletException, IOException {
    // call the doPost method
    doGet(request, response);
  }

}
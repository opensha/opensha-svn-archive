package org.scec.sha.gui.servlets;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import org.scec.util.FileUtils;


import org.scec.data.region.*;
import org.scec.sha.imr.*;
import org.scec.sha.earthquake.*;
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
  public final static boolean D =false;
  // parent directory where each new calculation will have its own subdirectory
  public static final String PARENT_DIR = "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/HazardMapDatasets/";
  // filenames for IMR, ERF, Region, metadata
  private static final String IMR_FILE_NAME = "imr.obj";
  private static final String ERF_FILE_NAME = "erf.obj";
  private static final String REGION_FILE_NAME = "region.obj";
  public  static final String METADATA_FILE_NAME = "metadata.txt";
  public  static final String SITES_FILE_NAME = "sites.txt";

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
      String  eqkRupForecastLocation =
          (String) inputFromApplet.readObject();
      //get the email address from the applet
      String emailAddr = (String) inputFromApplet.readObject();
      //get the parameter values in String form needed to reproduce this
      String mapParametersInfo = (String) inputFromApplet.readObject();

      // new directory that will be created
      long newDirId = System.currentTimeMillis();

      // report to the user whether the operation was successful or not
      // get an ouput stream from the applet
      ObjectOutputStream outputToApplet = new ObjectOutputStream(response.
          getOutputStream());
      outputToApplet.writeObject(new String(""+newDirId));
      outputToApplet.close();

      String newDir = this.PARENT_DIR+newDirId+"/";
      new File(newDir).mkdir();

      // write the metadata to the metadata file
      FileWriter fwMetadata = new FileWriter(newDir+this.METADATA_FILE_NAME);
      fwMetadata.write(mapParametersInfo);
      fwMetadata.close();

      // write site information in sites file
      FileWriter fwSites = new FileWriter(newDir+this.SITES_FILE_NAME);
      fwSites.write(sites.getMinLat()+" "+sites.getMaxLat()+" "+sites.getGridSpacing()+"\n");
      fwSites.write(sites.getMinLon()+" "+sites.getMaxLon()+" "+sites.getGridSpacing()+"\n");
      fwSites.close();


      FileUtils.saveObjectInFile(newDir+this.REGION_FILE_NAME, sites);
      FileUtils.saveObjectInFile(newDir+this.IMR_FILE_NAME, imr);

      if(D) System.out.println("ERF URL="+eqkRupForecastLocation);
      //EqkRupForecast eqkRupForecast = (EqkRupForecast)FileUtils.loadObjectFromURL(eqkRupForecastLocation);
      //FileUtils.saveObjectInFile(newDir+this.ERF_FILE_NAME, eqkRupForecast);
      String getERF_FileName = "getERF.sh";
      FileWriter fw = new FileWriter(newDir+getERF_FileName);
      fw.write("#!/bin/csh\n");
      fw.write("cd "+newDir+"\n");
      fw.write("cp  "+eqkRupForecastLocation+" "+newDir+this.ERF_FILE_NAME+"\n");
      fw.close();
      org.scec.util.RunScript.runScript(new String[]{"sh", "-c", "sh "+newDir+getERF_FileName});
      if(D) System.out.println("after wget");

      // now run the calculation on grid
      SubmitJobForGridComputation computation =
          new SubmitJobForGridComputation(IMR_FILE_NAME, ERF_FILE_NAME,
                                     REGION_FILE_NAME, newDir, newDirId, sites,
                                     emailAddr);
    }
    catch (Exception e) {
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
package org.scec.sha.gui.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.scec.data.region.*;
import org.scec.data.Location;

/**
 * <p>Title: WillsSiteClassServlet  </p>
 * <p>Description: This Servlet finds the VS30 and Basin Depth for the given
 * region. this needs to be fixed with the implementation of the Appliacble Region
 * Object.
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class WillsSiteClassServlet  extends HttpServlet {


  //File from which we get the Vs30
  private final String VS_30_INPUT_FILENAME = "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/WEB-INF/dataFiles/wills_siteTypeFile.xy";


  /**
   * method to get the wills site type as desired by the user
   *
   * @param request
   * @param response
   * @throws IOException
   * @throws ServletException
   */
  public void doGet(HttpServletRequest request,  HttpServletResponse response)
                                  throws IOException, ServletException {
    //Vectors for computing the lat and lons for the given gridded region
    Vector locationVector= new Vector();
    try {
      // get an input stream from the applet
      ObjectInputStream inputFromApplication = new ObjectInputStream(request.getInputStream());
      // read the function desired by the user
      String functionDesired = (String) inputFromApplication.readObject();
      // read the minlon, maxlon, minlat, maxlat, gridspacing
      double minLon = ((Double)inputFromApplication.readObject()).doubleValue();
      double maxLon = ((Double)inputFromApplication.readObject()).doubleValue();
      double minLat = ((Double)inputFromApplication.readObject()).doubleValue();
      double maxLat = ((Double)inputFromApplication.readObject()).doubleValue();
      double gridSpacing = ((Double)inputFromApplication.readObject()).doubleValue();
      inputFromApplication.close();
      //System.out.println(""+minLon+","+maxLon+","+minLat+","+maxLat+","+gridSpacing);
      prepareSitesInput(locationVector,minLon, maxLon, minLat, maxLat, gridSpacing);
      getVs30(locationVector,new ObjectOutputStream(response.getOutputStream()));

    }catch(Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * Prepare the input of the all the location in the gridded region and provide that input
   * to compute the Vs30 ro BasinDepth
   * @param locationVector : stores the locations
   * @param minLon
   * @param maxLon
   * @param minLat
   * @param maxLat
   * @param gridSpacing
   * @return
   */
  private void prepareSitesInput(Vector locationVector,double minLon, double maxLon, double minLat,
                                      double maxLat, double gridSpacing) {

    EvenlyGriddedRectangularGeographicRegion region = new EvenlyGriddedRectangularGeographicRegion(minLat,maxLat,minLon,maxLon,gridSpacing);
    //System.out.println("Number of Grid Locations:"+region.getNumGridLocs());
    ListIterator it= region.getGridLocationsIterator();
    while(it.hasNext())
      locationVector.add(it.next());
  }



  /**
   * Gets the Will's classified Site Type
   * @param locationVector: Stores all the gridded locations
   * @param output : returns the vector of the VS30 in form of the Will's classified
   * site type.
   */
  private void getVs30(Vector locationVector,ObjectOutputStream output) {

    //gridSpacing for the VS30 file and adding a small value to it.
    double gridSpacingForVs30InFile = .03334 +.001;
    try {

      //open the File Input Stream to read the file
      FileReader input = new FileReader(this.VS_30_INPUT_FILENAME);
      BufferedReader iBuf= new BufferedReader(input);
      String str;
      // parsing the file line by line
      //reading the first line from the file
      str=iBuf.readLine();

      int size= locationVector.size();

      Vector vs30= new Vector();

      //initialising the vs30 vector with the Double.NaN values
      for(int i=0;i<size;++i)
        vs30.add("NA");

      double prevLat=Double.NaN;
      for(int i=0;i<size;++i){
        double lat = ((Location)locationVector.get(i)).getLatitude();
        double lon = ((Location)locationVector.get(i)).getLongitude();
        boolean latFlag= false;
        while(str!=null) {
          StringTokenizer st = new StringTokenizer(str);

          // parse this line from the file
          //reading the Lons from the file
          double valLon = Double.parseDouble(st.nextToken());
          //reading the Lat from the file
          double valLat = Double.parseDouble(st.nextToken());

          if((valLat -lat) > gridSpacingForVs30InFile/2)
            // if this lat does not exist in file. Lat is always increasing in the file and the location vector
            break;

          // add Vs30 for new location
          if(Math.abs(lat-valLat) <= (gridSpacingForVs30InFile/2))
            //System.out.println("Lat:"+lat+";valLat:"+valLat+";valLatNext:"+valLatNext);
            latFlag=true;

          //iterating over lon's for each lat
          if(((Math.abs(lon-valLon)) <= gridSpacingForVs30InFile/2) && latFlag){
            //if we found the desired lon in the file,
            //we get the value of the VS30 for the nearest point
            //returns the site type based on the Will's classification for Site type.
            vs30.set(i,st.nextToken());
            break;

          }

           //this condition checks if the lat exists but lon does not exist
          if((valLon-lon) > (gridSpacingForVs30InFile/2 ) && latFlag)
            // if this location does not exist in this file
            break;

          // read next line
          str=iBuf.readLine();
        }
      }
      output.writeObject(vs30);
      output.close();
    }catch (Exception e) {
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

package org.scec.sha.gui.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.scec.data.region.*;
import org.scec.data.Location;

/**
 * <p>Title: SCEC_BasinDepthServlet  </p>
 * <p>Description: This Servlet finds the VS30 and Basin Depth for the given
 * region. this needs to be fixed with the implementation of the Appliacble Region
 * Object.
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class SCEC_BasinDepthServlet  extends HttpServlet {



  //Basin depth file
  private final String BASIN_DEPTH_FILENAME = "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/WEB-INF/dataFiles/fine_depth_25.xy";


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
    Vector locationVector= new Vector();
    try {
      // get an input stream from the applet
      ObjectInputStream inputFromApplication = new ObjectInputStream(request.getInputStream());
      // read the minlon, maxlon, minlat, maxlat, gridspacing
      double minLon = ((Double)inputFromApplication.readObject()).doubleValue();
      double maxLon = ((Double)inputFromApplication.readObject()).doubleValue();
      double minLat = ((Double)inputFromApplication.readObject()).doubleValue();
      double maxLat = ((Double)inputFromApplication.readObject()).doubleValue();
      double gridSpacing = ((Double)inputFromApplication.readObject()).doubleValue();
      inputFromApplication.close();
      prepareSitesInput(locationVector,minLon, maxLon, minLat, maxLat, gridSpacing);

      getBasinDepth(locationVector,new ObjectOutputStream(response.getOutputStream()));
    }catch(Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * Prepare the input of the all the location in the gridded region and provide that input
   * to compute BasinDepth
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
    System.out.println("Location size:"+locationVector.size());
  }


  /**
   *calculate the Basin Depth
   * @param locationVector: Stores all the gridded locations
   * @param output : returns the vector of the basin depth values for the gridded region
   */
  private void getBasinDepth(Vector locationVector,ObjectOutputStream output) {

    //gridSpacing for the basin depth file and adding a small amount ot it
    double gridSpacingForBasinDepthInFile = .0044+.0001;
    try {

      //open the File Input Stream to read the file
      FileReader input = new FileReader(this.BASIN_DEPTH_FILENAME);
      BufferedReader iBuf= new BufferedReader(input);
      String str;
      // parsing the file line by line
      //reading the first line from the file
      str=iBuf.readLine();

      int size= locationVector.size();

      Vector bd= new Vector();

      //initialising the bd vector with the Double.NaN values
      for(int i=0;i<size;++i)
        bd.add(new Double(Double.NaN));

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

          if((valLat -lat) > gridSpacingForBasinDepthInFile/2)
            // if this lat does not exist in file. Lat is always increasing in the file and the location vector
            break;

          // add basinDepth for new location
          if(Math.abs(lat-valLat) <= (gridSpacingForBasinDepthInFile/2))
            //System.out.println("Lat:"+lat+";valLat:"+valLat+";valLatNext:"+valLatNext);
            latFlag=true;

          //iterating over lon's for each lat
          if(((Math.abs(lon-valLon)) <= gridSpacingForBasinDepthInFile/2) && latFlag){
            //if we found the desired lon in the file ,
            //we get the value of the basinDepth for the nearest point
            //returns the actual value for the basinDepth
            bd.set(i,new Double(st.nextToken()));
            break;

          }

           //this condition checks if the lat exists but lon does not exist
          if((valLon-lon) > (gridSpacingForBasinDepthInFile/2 ) && latFlag)
            // if this location does not exist in this file
            break;

          // read next line
          str=iBuf.readLine();
        }
      }
      output.writeObject(bd);
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

package org.scec.sha.gui.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.scec.data.region.*;
import org.scec.data.Location;
import org.scec.calc.RelativeLocation;
/**
 * <p>Title: Vs30BasinDepthServlet  </p>
 * <p>Description: This Servlet finds the VS30 and Basin Depth for the given
 * region. this needs to be fixed with the implementation of the Appliacble Region
 * Object.
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class Vs30BasinDepthCalcServlet  extends HttpServlet {

  public static String VS_30 = "Vs30";
  public static String BASIN_DEPTH = "BasinDepth";

  //File from which we get the Vs30
  private final static String VS_30_INPUT_FILENAME = "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/WEB-INF/dataFiles/vs30_120s_class.xy";
  private final static String BASIN_DEPTH_FILENAME = "/opt/install/jakarta-tomcat-4.1.24/webapps/OpenSHA/WEB-INF/dataFiles/fine_depth_25.xy";


  /**
   * method to get the basin depth/vs30  as desired by the user
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
      // make input and output file name based on IP address of user
      //String inputfilename=new String(request.getRemoteHost()+".in");
      //String outputfilename=new String(request.getRemoteHost()+".out");
      // check the funcion desired by the useer
      if(functionDesired.equalsIgnoreCase(VS_30))
        getVs30(gridSpacing,locationVector,new ObjectOutputStream(response.getOutputStream()));
      else if(functionDesired.equalsIgnoreCase(this.BASIN_DEPTH))
        getBasinDepth(gridSpacing,locationVector,new ObjectOutputStream(response.getOutputStream()));
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
    System.out.println("Location size:"+locationVector.size());
  }



  /**
   * calculate the Vs30
   * @param locationVector: Stores all the gridded locations
   * @param minLon
   * @param maxLon
   * @param minLat
   * @param maxLat
   * @param gridSpacing
   * gets the Vs30
   *
   * */
  private void getVs30(double gridSpacing,Vector locationVector,ObjectOutputStream output) {

    try {
      double currLon=0;
      Vector smallestDistance = new Vector();
      int size= locationVector.size();
      //System.out.println("****Location Size****"+size);
      Vector vs30= new Vector();
      double gridSpacingCutOff = 1.5*gridSpacing;
      //initialising the vs30 vector with the Double.NaN values
      // also set the min distance to Positive infinity
      for(int i=0;i<size;++i) {
        vs30.add(new Double(Double.NaN));
        smallestDistance.add(new Double(Double.POSITIVE_INFINITY));
      }

      // open the VS30 file for reading
      FileReader input = new FileReader(this.VS_30_INPUT_FILENAME);
      BufferedReader iBuf= new BufferedReader(input);
      String str=iBuf.readLine();
      double lat, lon, valLat, valLon, distance, vs30Val;
      while(str!=null) {
        StringTokenizer st = new StringTokenizer(str);
        //reading the Lons from the file
        valLon = Double.parseDouble(st.nextToken());
        //reading the Lat from the file
        valLat = Double.parseDouble(st.nextToken());
        vs30Val = Double.parseDouble(st.nextToken());
        for(int i=0;i<size;++i){
          lat = ((Location)locationVector.get(i)).getLatitude();
          lon = ((Location)locationVector.get(i)).getLongitude();
          if((lat+gridSpacingCutOff)<valLat) continue;

          distance = Double.POSITIVE_INFINITY;

          // add Vs30 for new location
          if((Math.abs(lon)+gridSpacingCutOff) < Math.abs(valLon)){
            distance= RelativeLocation.latLonDistance(lat,lon,valLat,valLon);
            if(distance < ((Double)smallestDistance.get(i)).doubleValue()){
              smallestDistance.set(i,new Double(distance));
              vs30.set(i,new Double(vs30Val));
            }
          }
          //System.out.println("lon:"+lon+";valLon:"+val+";valLonNext:"+valNext);
          //System.out.print(";vs30_Curr:"+vs30_Curr+";vs30_Next:"+vs30_Next);
          //returns the actual value for the vs30
          // read next line
        }
          str= iBuf.readLine();
        }

      System.out.println("size of vs30 vector:"+vs30.size());
/*      for(int i=0;i<vs30.size();++i){
        System.out.println("Location:"+((Location)locationVector.get(i)).getLatitude()+";"+((Location)locationVector.get(i)).getLongitude()+"Vs30("+i+"):"+vs30.get(i));
      }*/
      output.writeObject(vs30);
      output.close();
    }catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
    * calculate the Basin depth
    * @param locationVector: Stores all the gridded locations
    * @param minLon
    * @param maxLon
    * @param minLat
    * @param maxLat
    * @param gridSpacing
    * calculate the BasinDepth
    */
   private void getBasinDepth(double gridSpacing,Vector locationVector,ObjectOutputStream output) {

     try {
       double currLon=0;
       Vector smallestDistance = new Vector();
       int size= locationVector.size();
       //System.out.println("****Location Size****"+size);
       Vector basinDepth= new Vector();
       double gridSpacingCutOff = 1.5*gridSpacing;
       //initialising the basin depth vector with the Double.NaN values
       // also set the min distance to Positive infinity
       for(int i=0;i<size;++i) {
         basinDepth.add(new Double(Double.NaN));
         smallestDistance.add(new Double(Double.POSITIVE_INFINITY));
       }

       // open the basin depth file for reading
       FileReader input = new FileReader(this.BASIN_DEPTH_FILENAME);
       BufferedReader iBuf= new BufferedReader(input);
       String str=iBuf.readLine();
       double distance, lat, lon, valLat, valLon, basinDepthVal;
       while(str!=null) {
         StringTokenizer st = new StringTokenizer(str);
         //reading the Lons from the file
         valLon = Double.parseDouble(st.nextToken());
         //reading the Lat from the file
         valLat = Double.parseDouble(st.nextToken());
         basinDepthVal = Double.parseDouble(st.nextToken());
         for(int i=0;i<size;++i){

           lat = ((Location)locationVector.get(i)).getLatitude();
           lon = ((Location)locationVector.get(i)).getLongitude();
           if((lat+gridSpacingCutOff)<valLat) continue;

           distance = Double.POSITIVE_INFINITY;

           // add basin depth for new location
           if((Math.abs(lon)+gridSpacingCutOff) < Math.abs(valLon)){
             distance= RelativeLocation.latLonDistance(lat,lon,valLat,valLon);
             if(distance < ((Double)smallestDistance.get(i)).doubleValue()){
               smallestDistance.set(i,new Double(distance));
               basinDepth.set(i,new Double(basinDepthVal));
             }
           }
         }
           str= iBuf.readLine();
        }

       System.out.println("size of basindepth vector:"+basinDepth.size());
       /*for(int i=0;i<vs30.size();++i){
         System.out.println("Location:"+((Location)locationVector.get(i)).getLatitude()+";"+((Location)locationVector.get(i)).getLongitude()+"BasinDepth("+i+"):"+BasinDepth.get(i));
       }*/
       output.writeObject(basinDepth);
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
package org.scec.sha.gui.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.scec.data.region.*;
import org.scec.data.Location;

/**
 * <p>Title: Vs30BasinDepthServlet  </p>
 * <p>Author: </p>
 * <p>Version: SCVM-version 0.1</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Vs30BasinDepthCalcServlet  extends HttpServlet {

  public static String VS_30 = "Vs30";
  public static String BASIN_DEPTH = "BasinDepth";

  //File from which we get the Vs30
  private final String VS_30_INPUT_FILENAME = "vs30class.xy";
  private final String BASIN_DEPTH_FILENAME = "fine_depth_25.xy";


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

       prepareSitesInput(locationVector,minLon, maxLon, minLat, maxLat, gridSpacing);
      // make input and output file name based on IP address of user
      //String inputfilename=new String(request.getRemoteHost()+".in");
      //String outputfilename=new String(request.getRemoteHost()+".out");
      // check the funcion desired by the useer
      if(functionDesired.equalsIgnoreCase(VS_30))
        getVs30(locationVector,new ObjectOutputStream(response.getOutputStream()));
      else if(functionDesired.equalsIgnoreCase(this.BASIN_DEPTH))
        getBasinDepth(locationVector,new ObjectOutputStream(response.getOutputStream()));
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

    EvenlyGriddedRectangularGeographicRegion region = new EvenlyGriddedRectangularGeographicRegion(
                                                      minLat,maxLat,minLon,maxLon,gridSpacing);
    System.out.println("Number of Grid Locations:"+region.getNumGridLocs());
    ListIterator it= region.getGridLocationsIterator();
    while(it.hasNext())
      locationVector.add(it.next());
    for(int i=0;i<locationVector.size();++i)
      System.out.println("Location:"+((Location)locationVector.get(i)).toString());
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
  private void getVs30(Vector locationVector,ObjectOutputStream output) {
    Vector vs30= new Vector();
    try {
      double currLon=0;
      //open the File Input Stream to read the file
      FileReader input = new FileReader(this.VS_30_INPUT_FILENAME);
      BufferedReader iBuf= new BufferedReader(input);
      String str,strNext;
      // pass the file line by line to the user end
      str=iBuf.readLine();
      strNext=iBuf.readLine();
      double total = 0;
      int lineCount = 0;
      int size= locationVector.size();
      double prevLat=Double.NaN;
      for(int i=0;i<size;++i){

        double lat = ((Location)locationVector.get(i)).getLatitude();
        double lon = ((Location)locationVector.get(i)).getLongitude();
        boolean latFlag= false;
        if(prevLat == lat)
          latFlag=true;
        while(strNext!=null) {
          ++lineCount;
          StringTokenizer st = new StringTokenizer(str);
          StringTokenizer stNext = new StringTokenizer(strNext);
          // parse this line from the file
          double val = Double.parseDouble(st.nextToken());
          double valNext = Double.parseDouble(stNext.nextToken());
          double valLat = Double.parseDouble(st.nextToken());
          double valLatNext = Double.parseDouble(stNext.nextToken());
          // add vs30 for new location
          if(lat>=valLat && lat<=valLatNext){
            System.out.println("Lat:"+lat+";valLat:"+valLat+";valLatNext:"+valLatNext);
            latFlag=true;
          }
          if(lon>=val && lon<=valNext && latFlag){
            double vs30_Curr =Double.parseDouble(st.nextToken());
            double vs30_Next = Double.parseDouble(stNext.nextToken());

            System.out.println("lon:"+lon+";valLon:"+val+";valLonNext:"+valNext);
            System.out.print(";vs30_Curr:"+vs30_Curr+";vs30_Next:"+vs30_Next);
            //returns the actual value for the vs30
            vs30.add(new Double(this.interpolateVs30OrBasinDepth(lon,val,valNext,vs30_Curr,vs30_Next)));
            break;
          }
          // read next line
          str=strNext;
          strNext= iBuf.readLine();
        }
         //stores the lat that was read currently from the vector and we are now going a new Lat from Vector
         prevLat =lat;
      }

      System.out.println("size of vs30 vector:"+vs30.size());
      output.writeObject(vs30);
      output.close();
    }catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
    * calculate the Vs30
    * @param locationVector: Stores all the gridded locations
    * @param minLon
    * @param maxLon
    * @param minLat
    * @param maxLat
    * @param gridSpacing
    * calculate the BasinDepth
    */
   private void getBasinDepth(Vector locationVector,ObjectOutputStream output) {
     Vector basinDepth= new Vector();
    try {
      double currLon=0;
      //open the File Input Stream to read the file
      FileReader input = new FileReader(this.BASIN_DEPTH_FILENAME);
      BufferedReader iBuf= new BufferedReader(input);
      String str,strNext;
      // pass the file line by line to the user end
      str=iBuf.readLine();
      strNext=iBuf.readLine();
      double total = 0;
      int lineCount = 0;
      int size= locationVector.size();
      double prevLat=Double.NaN;
      for(int i=0;i<size;++i){
        double lat = ((Location)locationVector.get(i)).getLatitude();
        double lon = ((Location)locationVector.get(i)).getLongitude();
        boolean latFlag=false;
        if(prevLat == lat)
          latFlag=true;
        while(strNext!=null) {
          ++lineCount;
          StringTokenizer st = new StringTokenizer(str);
          StringTokenizer stNext = new StringTokenizer(strNext);
          // parse this line from the file
          double val = Double.parseDouble(st.nextToken());
          double valNext = Double.parseDouble(stNext.nextToken());
          double valLat = Double.parseDouble(st.nextToken());
          double valLatNext = Double.parseDouble(stNext.nextToken());
          // add basinDepth for new location
          if(lat>=valLat && lat<=valLatNext){
            System.out.println("Lat:"+lat+";valLat:"+valLat+";valLatNext:"+valLatNext);
            latFlag=true;
          }
          if(lon>=val && lon<=valNext && latFlag){
            double bd_Curr =Double.parseDouble(st.nextToken());
            double bd_Next = Double.parseDouble(stNext.nextToken());
            System.out.println("lon:"+lon+";valLon:"+val+";valLonNext:"+valNext);
            System.out.print(";bd_Curr:"+bd_Curr+";bd_Next:"+bd_Next);
            //returns the actual value for the basinDepth
            basinDepth.add(new Double(this.interpolateVs30OrBasinDepth(lon,val,valNext,bd_Curr,bd_Next)));
            break;
          }
          // read next line
          str=strNext;
          strNext= iBuf.readLine();
        }
        //stores the lat that was read currently from the vector and we are now going a new Lat from Vector
         prevLat =lat;
      }

      System.out.println("size of basinDepth vector:"+basinDepth.size());
      output.writeObject(basinDepth);
      output.close();
    }catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * interpolating the Vs30 or BasinDepth values to get the final Vs30 or basinDepth
   * for the corresponding lat
   * @param x1=lat
   * @param x2=nextLat
   * @param y1=Vs30 at x1
   * @param y2=Vs30 at x2
   * @return Vs30 value for the givenLat
   */
  private double interpolateVs30OrBasinDepth(double givenLat, double x1,double x2,double y1,double y2){
    return ((givenLat-x1)/(x2-x1))*(y2-y1) +y1;
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

package org.opensha.commons.data.region;

import java.util.ListIterator;

import java.io.IOException;
import java.io.FileWriter;
import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.data.Direction;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;

/**
 * <p>Title: EvenlyGriddedCircularGeographicRegion</p>
 * <p>Description: This class creates a EvenlyGridded circular Geographical region.
 * All grid points are nice values in that lat/gridSpacing and lon/gridASpacing
 * are always whole numbers.
 * </p>
 * <p>
 * This class has been testde by having a main method that creates all the
 * locations within EvenlyGridded Circular Geographical region. It dumps out all
 * these locations in a file called  "CircularRegionFile.txt",
 * in the sha project home directory. File format is "lat,lon,depth" on each line
 * of file. One can take this file and plot it in some kind plotting tool to see
 * if region looks like a EvenlyGriddedCircular Geographical region.
 * </p>
 * @author : Edward Field
 * @created: March 5,2003
 * @version 1.0
 *
 * @see EvenlyGriddedGeographicRegionAPI
 */

public class EvenlyGriddedCircularGeographicRegion
    extends EvenlyGriddedGeographicRegion{

  private final static String C = "EvenlyGriddedCircularGeographicRegion";
  private final static boolean D = false;

  //List for storing starting lon for each lat
  private double[] firstLonPerLat;

  //Circle Radius
  private double circleRadius;
  //Circle Center Location
  private Location circleCenterLocation;


  /**
   * Class constructor that accepts the circle center-location, circle radius,
   * grid spacing for creation of a EvenlyGriddedCircularGeographicRegion.
   *
   * @param centerLoc - the location of the circle center
   * @param radius - radius of the region (km)
   * @param gridSpacing - grid spacing (degree)
   */
  public EvenlyGriddedCircularGeographicRegion(Location centerLoc, double radius, double gridSpacing) {
    circleRadius = radius;
    circleCenterLocation = centerLoc;
    Location tempLoc;
    Direction dir;

    //set min and max lat and lons
    dir = new Direction(0, radius, 180, 0);
    tempLoc = RelativeLocation.getLocation(centerLoc, dir);
    super.minLat = tempLoc.getLatitude();
    dir = new Direction(0, radius, 0, 180);
    tempLoc = RelativeLocation.getLocation(centerLoc, dir);
    super.maxLat = tempLoc.getLatitude();
    dir = new Direction(0, radius, 270, 90);
    tempLoc = RelativeLocation.getLocation(centerLoc, dir);
    super.minLon = tempLoc.getLongitude();
    dir = new Direction(0, radius, 90, 270);
    tempLoc = RelativeLocation.getLocation(centerLoc, dir);
    super.maxLon = tempLoc.getLongitude();

    if (minLon > maxLon)throw new RuntimeException(
        "Problem in CircularGeographicRegion" +
        "related to crossing the zero lat boundary");

    if (D) {
      System.out.println("minLat = " + minLat + ";  maxLat = " + maxLat +
                         ";  minLon = " + minLon + ";  maxLon = " + maxLon);
      double dist;
      dist = RelativeLocation.getHorzDistance(minLat,
                                              circleCenterLocation.getLongitude(),
                                              maxLat,
                                              circleCenterLocation.getLongitude());
      System.out.println("computed horz diameter = " + dist + "; circleRadius = " +
                         circleRadius);
      dist = RelativeLocation.getHorzDistance(circleCenterLocation.getLatitude(),
                                              minLon,
                                              circleCenterLocation.getLatitude(),
                                              maxLon);
      System.out.println("computed vert diameter = " + dist + "; circleRadius = " +
                         circleRadius);
    }

    // make the region outline (locList)
    makeRegionOutline(10.0);
    //set the gridSpacing for the region and creates the minimum information required
    //for retreiving the any location or index in the gridded region.
    setGridSpacing(gridSpacing);
  }


  /**
   * Class constructor that accepts the circle center-location, circle radius,grid spacing
   * and EvenlyGriddedGeographicRegionAPI,for creating the list of locations
   * in this region from passed in EvenlyGriddedGeographicRegionAPI,
   * for creation of a EvenlyGriddedCircularGeographicRegion.
   * This method is helpful as avoid creating same location more then once and just
   * refer to the location object in the passed in EvenlyGriddedGeographicRegionAPI.
   *
   * This class constructor allows the user to create list of locations for this
   * EvenlyGriddedGeographic object from passed in EvenlyGriddedGeographicRegionAPI.
   * Please refer to EvenlyGriddedGeographicRegionAPI for more details.
   *
   * @param centerLoc - the location of the circle center
   * @param radius - radius of the region (km)
   * @param gridSpacing - grid spacing (degree)
   * @param region EvenlyGriddedGeographicRegionAPI
   * @see EvenlyGriddedGeographicRegionAPI.createRegionLocationsList(EvenlyGriddedGeographicRegionAPI)
   * @see EvenlyGriddedGeographicRegionAPI
   */
  public EvenlyGriddedCircularGeographicRegion(Location centerLoc, double radius, double gridSpacing,
      EvenlyGriddedGeographicRegionAPI region) {
    this(centerLoc,radius,gridSpacing);
    createRegionLocationsList(region);
  }



  /**
   * This method checks whether the given location is within the region by seeing whether
   * the distance to the circle center is less than or equal to the circle radius.
   * @param location Location
   * @returns true if location is inside the circular region boundary
   * otherwise it return false.
   */
  public boolean isLocationInside(Location location){
    double horzDist = RelativeLocation.getHorzDistance(this.circleCenterLocation, location);

    if(horzDist <= this.circleRadius)
      return true;
    return false;

  }


  /**
   * this make the locList for the circular region outline
   * This creates the region outline for the circle, where there are 360/degreeIncrement
   * equally spaced points (the last two may be closer than this).  The constructor (default)
   * uses 10 degrees (36 points)
   *
   * @param degreeIncrement - difference in azimuth (from the center) between neighboring points
   */
  protected void makeRegionOutline(double degreeIncrement) {
    locList = new LocationList();
    Direction tempDir;
    for(double deg = 0; deg<360; deg += degreeIncrement) {
      tempDir = new Direction(0.0,circleRadius,deg,180-deg);
      locList.addLocation(RelativeLocation.getLocation(circleCenterLocation,tempDir));
    }
    if(D) {
      Location tempLoc;
      System.out.println("Region outline:");
      for(int i = 0; i < locList.size(); i++) {
        tempLoc = locList.getLocationAt(i);
        System.out.println(tempLoc.getLatitude()+"  "+tempLoc.getLongitude());
      }
    }
  }


  /**
   * Returns the EvenlyGriddedCircularGeographicRegion radius
   * @return double
   */
  public double getEvenlyGriddedCircularRegionRadius(){
    return circleRadius;
  }



  /**
   * Returns the center location EvenlyGriddedCircularRegionCenterLocation
   * @return Location Center Location
   */
  public Location getEvenlyGriddedCircularRegionCenterLocation(){
    return circleCenterLocation;
  }

  /*
   * this function creates a Lon Array for each gridLat. It also creates a
   * int array which tells how many locations are there below a given lat.
   */
  protected void initLatLonArray() {
    //getting the number of grid lats in the given region
    int numLats = (int)Math.rint((niceMaxLat - niceMinLat)/gridSpacing) + 1;
    //initialising the array for storing number of location below a given lat
    //first element is 0 as first lat has 0 locations below it and last num is the
    //total number of locations.
    locsBelowLat = new int[numLats+1];


    firstLonPerLat = new double[numLats];

    int locBelowIndex = 0;
    //initializing the first element of number of locations to be 0 location for
    //min lat.
    //For each lat the number of locations keeps increasing.
    locsBelowLat[locBelowIndex++] = 0;

    //looping over all grid lats in the region to get longitudes at each lat and
    // and number of locations below each starting lat.
    for(int iLat =0;iLat <numLats;++iLat) {
      double lat = niceMinLat +iLat*gridSpacing ;
      double lon = niceMinLon;
      //saves the first location for each lat
      double firstLocForLat = lon;
      //counts the number of lons per lat
      int lonCounter = 0;
      //gets the first Lon for each Lat
      boolean firstLon = true;
      while (lon <= niceMaxLon) {
        //creating the location object for the lat and lon that we got
        Location loc = new Location(lat, lon);
        //checking if this location lies in the given gridded region
        //once found the first lon on the lat that lies within the region
        //save it and jump to find first lon on the next lat.
        if (this.isLocationInside(loc)){
          ++lonCounter;
          if(firstLon){
            firstLocForLat = lon;
            firstLon = false;
          }
        }
        lon += gridSpacing;
      }
      //assigning number of locations below a grid lat to the grid Lat above this lat.
      locsBelowLat[locBelowIndex] = locsBelowLat[locBelowIndex - 1];
      locsBelowLat[locBelowIndex] += lonCounter;

      //just storing the first Lon for all the given grid Lats in the region.
      firstLonPerLat[locBelowIndex -1] = firstLocForLat;
      //incrementing the index counter for number of locations below a given latitude
      ++locBelowIndex;
    }
  }


  /**
   * Returns the index of the nearest location in the given gridded region, to
   * the provided Location.
   * @param loc Location Location to which we have to find the nearest location.
   * @return nearest location index
   * @see EvenlyGriddedGeographicRegionAPI.getNearestLocationIndex(Location)
   */
  public int getNearestLocationIndex(Location loc){

    double lat = loc.getLatitude();
    double lon = loc.getLongitude();

    //throw exception if location is outside the region lat bounds.
    if (!this.isLocationInside(loc))
      return -1;
    else { //location is inside the polygon bounds but is outside the nice min/max lat/lon
      //constraints then assign it to the nice min/max lat/lon.
      if (lat < niceMinLat)
        lat = niceMinLat;
      else if (lat > niceMaxLat)
        lat = niceMaxLat;
      if (lon < niceMinLon)
        lon = niceMinLon;
      else if (lon > niceMaxLon)
        lon = niceMaxLon;
    }

    //getting the lat index
    int latIndex = (int)Math.rint((lat - niceMinLat)/gridSpacing);
    //number of locations below this latitude
    int locIndex = locsBelowLat[latIndex];
    //getting the first Longitude on this Latitude
    double latFirstLon = firstLonPerLat[latIndex];

    //finding the nearest longitude to a given location longitude in the region
    //As we have checked earlier that this location is within the region bounds
    //so there must a longitude nearest to given location longitude. This
    //loop will go until location index is found. Once found it breaks out of
    //the loop.
    while(true){
      if (Math.abs(latFirstLon - lon) <= gridSpacing/2)
        break;
      latFirstLon +=gridSpacing;
      ++locIndex;
    }

    return locIndex;
  }




  /**
   * Returns the Gridded Location at a given index. It creates a
   * new location object whenever user request for a location a
   * given index. It does this on the fly without having to create all locations
   * in the EvenlyGridded Circular Region.
   * @param index
   * @returns the Grid Location object at that index.
   * @see EvenlyGriddedGeographicRegionAPI.getGridLocationClone(int)
   */
  public Location getGridLocationClone(int index)  {

      int size = locsBelowLat.length;
      int locIndex = 0;
      //iterating over all the lonsPerLat array to get the Lat index where given
      //index lies.
      int latIndex = 0;
      boolean locationFound = false;
      for (int i = 0; i < size - 1; ++i) {
        int locsIndex2 = locsBelowLat[i + 1];
        if (index < locsIndex2) {
          locIndex = locsBelowLat[i];
          latIndex = i;
          locationFound = true;
          break;
        }
      }

      if (!locationFound) return null;
      double firstLonForLat = firstLonPerLat[latIndex];
      //this defines the position whichlocation has to be retrieved for the lat
      int lonIndex = index - locIndex;
      //getting the lon on the given location index, this is retrieved by using
      //first Lon on a given Lat and then getting the lon at given index on that lat index.
      double lon = firstLonForLat+ lonIndex * gridSpacing;
      //getting the lat for the given location index
      double lat = niceMinLat + latIndex * gridSpacing;
      return new Location(lat, lon);
  }






  /**
   * Creates the list of location in the gridded region and keeps it in the
   * memory until cleared.
   */
  protected void createGriddedLocationList() {

    //creates a instance of new locationList
    gridLocsList = new LocationList();
    //Length of Array that stores the locations below a given gridLat
    int locBelowLatSize = locsBelowLat.length;
    //initialising the lat with the nice min lat
    double lat = niceMinLat;
    //iterating over all lons for each lat, and creating a Location list from it.
    for (int i = 0; i < locBelowLatSize-1; ++i) {
      double firstLonForLat =  firstLonPerLat[i];
      //number of grid Lons for a given Lat
      int numLonsForLat = locsBelowLat[i+1] - locsBelowLat[i];
      for (int j = 0; j < numLonsForLat; ++j) {
        //getting each longitude for a given lat
        double lon = firstLonForLat + j*gridSpacing;
        //creating a new location
        Location loc = new Location(lat, lon);
        gridLocsList.addLocation(loc);
      }
      //getting the next grid lat.
      lat += gridSpacing;
    }
  }


  /**
   * Main method to run the this class and produce a file with
   * evenly gridded location.
   */
  public static void main(String[] args) {
    EvenlyGriddedCircularGeographicRegion gridReg = new EvenlyGriddedCircularGeographicRegion(new Location(38,-122,0),100,0.02);
    LocationList outlineList = gridReg.getRegionOutline();
    // check diameter of circle:
    int numLocs = outlineList.size();
    for(int i=0; i<numLocs/2; i++)
    	System.out.println(i+"\t"+RelativeLocation.getHorzDistance(outlineList.getLocationAt(i), outlineList.getLocationAt(i+numLocs/2)));

    // write outline
    try {
      FileWriter fw = new FileWriter("CircularRegionFile.txt");
      ListIterator it = gridReg.getRegionOutline().listIterator();
//      ListIterator it = gridReg.getGridLocationsIterator();
      while(it.hasNext()){
        Location loc = (Location)it.next();
        fw.write(loc.toString()+"\n");
      }
      fw.close();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}

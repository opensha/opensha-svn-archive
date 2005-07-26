package org.opensha.data.region;

import java.util.ListIterator;
import java.text.DecimalFormat;

import org.opensha.data.LocationList;
import org.opensha.data.Location;
import org.opensha.exceptions.RegionConstraintException;

/**
 * <p>Title: EvenlyGriddedRectangularGrographicRegion</p>
 * <p>Description: It creates a evenly gridded geographic region for the
 * specified gridded region provided by the user</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Ned Field & Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class EvenlyGriddedRectangularGeographicRegion extends RectangularGeographicRegion
                                  implements EvenlyGriddedGeographicRegionAPI{

  /**
   * class variables
   */
  private double gridSpacing;

  private int numLatGridPoints;
  private int numLonGridPoints;

  private final static String C = "EvenlyGriddedRectangularGeographicRegion";
  private final static boolean D = false;
  private final static DecimalFormat format = new DecimalFormat(".000#");
  private LocationList gridLocsList;

  // this makes the first lat and lon grid points nice in that niceMinLat/gridSpacing
  // is and integer and the point is within the polygon
  private double niceMinLat ;
  private double niceMinLon ;
  // this makes the last lat and lon grid points nice in that niceMaxLat/gridSpacing
  // is and integer and the point is within the polygon.
  private double niceMaxLat ;
  private double niceMaxLon ;

  /**
   * class constructor
   * @param minLat
   * @param maxLat
   * @param minLon
   * @param maxLon
   */
  public EvenlyGriddedRectangularGeographicRegion(double minLat,double maxLat,
      double minLon,double maxLon, double gridSpacing) throws
      RegionConstraintException {
    super(minLat,maxLat,minLon,maxLon);


    if (D) System.out.println("numLatGridPoints="+numLatGridPoints+"; numLonGridPoints="+numLonGridPoints);
  }


  /**
   * It samples out the grids location points based on the grid spacing(in degrees)
   * chosen.
   * @param degrees: sets the grid spacing
   */
  public void setGridSpacing(double degrees){
    this.gridSpacing=gridSpacing;

    //getting the nice Min Lat and Lon , so that each Lat-Lon is a perfect
    //multiple of gridSpacing
    niceMinLat = Math.ceil(minLat/gridSpacing)*gridSpacing;
    niceMinLon = Math.ceil(minLon/gridSpacing)*gridSpacing;
    niceMaxLat = Math.floor(maxLat/gridSpacing)*gridSpacing;
    niceMaxLon = Math.floor(maxLat/gridSpacing)*gridSpacing;

    numLatGridPoints = (int)Math.ceil((niceMaxLat - niceMinLat)/gridSpacing)+1;
    numLonGridPoints = (int)Math.ceil((niceMaxLon - niceMinLon)/gridSpacing)+1;

  }

  /**
   *
   * @return  the grid spacing(in degrees)
   */
  public double getGridSpacing(){
    return gridSpacing;
  }

  /**
   *
   * @returns the number of GridLocation points
   */
  public int getNumGridLocs(){

    return numLatGridPoints*numLonGridPoints;
  }

  /**
   *
   * @returns the Grid Locations Iterator.
   */
  public ListIterator getGridLocationsIterator(){
    //only create the location list if null
    if(gridLocsList == null)
      //creating the instance of the locationList
      gridLocsList=createGriddedLocationList();
    //return the ListIterator for the locationList
    return gridLocsList.listIterator();
  }

  /**
   *
   * @returns a list of grid-site locations
   */
  public LocationList getGridLocationsList(){
    //only create the location list if null
    if(gridLocsList == null)
      //creating the instance of the locationList
      gridLocsList=createGriddedLocationList();

    return gridLocsList;
  }


  /**
   * Return the number of gridded Lats in the Rectangular region
   * @return
   */
  public int getNumGridLats(){
    return numLatGridPoints;
  }

  /**
   * Return the number of gridded Lons in the Rectangular region
   * @return
   */
  public int getNumGridLons(){
    return numLonGridPoints;
  }

  /**
   *
   * @param index (starts from zero)
   * @returns the Grid Location at that index.
   */
  public Location getGridLocation(int index){

    //gets the row for the latitude in which that index of grid exists
    int row=index/numLonGridPoints;

    //gets the column in the row (longitude point) where that index exists
    int col=index%numLonGridPoints;

    //lat and lon for that indexed point
    double newLat=getMinLat()+row*gridSpacing;
    double newLon=getMinLon()+col*gridSpacing;

    //new location at which that lat and lon exists
    Location location= new Location(newLat,newLon);

    //returns  the location at the specified index in the location list
    return location;
  }




  /**
   * Returns the index of the nearest location in the given gridded region, to
   * the provided Location.
   * @param loc Location Location to which we have to find the nearest location.
   * @return int index of the nearest location. User can use this index to
   * retrive the location from the locationlist.
   */
  public int getNearestLocationIndex(Location loc){
    //getting the Location lat and Lon
    double lat = loc.getLatitude();
    double lon = loc.getLongitude();

    //get the region's minLat and minLon
    double minLat = getMinLat();
    double minLon = getMinLon();

    //Considering the Locations to be store in a 2D container where lats are rows
    //and lons are the Cols.
    //getting the Row index
    int rowIndex = (int)Math.rint((lat - minLat)/gridSpacing);
    //getting the Col index
    int colIndex = (int)Math.rint((lon - minLon)/gridSpacing);

    //getting the number of Lons(columns per row) per Lat.
    int numCols = getNumGridLons();
    int index = rowIndex*numCols+colIndex;
    return index;
  }



  /**
   * Private method to create the Location List for the gridded Rectangular Geog. Region
   * @returns the LocationList
   */
  private LocationList createGriddedLocationList(){
    double lat,lon;

    //creates a instance of new locationList
    LocationList gridLocsList=new LocationList();
    for(int iLat=0;iLat < numLatGridPoints; iLat++){
      lat = niceMinLat + gridSpacing*(double)iLat;
      for(int iLon=0; iLon < this.numLonGridPoints; iLon++){
        lon=niceMinLon+gridSpacing*(double)iLon;
        gridLocsList.addLocation(new Location(lat,lon));
      }
    }
    return gridLocsList;
  }


  public static void main(String[] args) {

    EvenlyGriddedRectangularGeographicRegion geoReg = null;
    try {
      geoReg = new EvenlyGriddedRectangularGeographicRegion(33., 33.9, 120.,
          121.9, .05);
    }
    catch (RegionConstraintException ex) {
    }

    System.out.println(C+": numLocations="+ geoReg.getNumRegionOutlineLocations());

    System.out.println(C+": getMinLat ="+ geoReg.getMinLat());
    System.out.println(C+": getMaxLat ="+ geoReg.getMaxLat());
    System.out.println(C+": getMinLon ="+ geoReg.getMinLon());
    System.out.println(C+": getMaxLon ="+ geoReg.getMaxLon());

    System.out.println(C+": numGridLocs="+ geoReg.getNumGridLocs());

    LocationList list = geoReg.getGridLocationsList();

    System.out.print("numInList="+list.size()+"; getNumGridLocs="+geoReg.getNumGridLocs()+"\n");

    Location tempLoc;
    for(int i = 0; i < geoReg.getNumGridLocs(); i++) {
      tempLoc = (Location) list.getLocationAt(i);
      System.out.print("index="+i+"; Loc from list:  "+tempLoc.toString()+"; Loc from method:  "+ (geoReg.getGridLocation(i)).toString() +"\n" );
    }
  }



  /**
   * Returns the nearest location in the gridded region to the provided Location.
   * @param loc Location Location to which we have to find the nearest location.
   * @return Location Nearest Location
   */
  public Location getNearestLocation(Location loc) {
    //Getting the nearest Location to the rupture point location


    //getting the nearest Latitude. If this Lon is greater then MaxLat, then
    //niceMaxLat will be the nearest Lon. If it is less then the MinLat, then
    //niceMinLat will be the nearest Lat.
    double lat = Math.rint(loc.getLatitude() / gridSpacing) *
        gridSpacing;
    if (lat > getMaxLat())
      lat = niceMaxLat;
    else if (lat < getMinLat())
      lat = niceMinLat;

    //getting the nearest Longitude. If this Lon is greater then MaxLon, then
    //niceMaxLon will be the nearest Lon. If it is less then the MinLon, then
    //niceMinLon will be the nearest Lon
    double lon = Math.rint(loc.getLongitude() / gridSpacing) *
        gridSpacing;
    if (lon > getMaxLon())
      lat = niceMaxLon;
    else if (lon < getMinLon())
      lat = niceMinLon;

    lat = Double.parseDouble(EvenlyGriddedGeographicRegionAPI.latLonFormat.format(
        lat));
    lon = Double.parseDouble(EvenlyGriddedGeographicRegionAPI.latLonFormat.format(
        lon));
    return new Location(lat, lon);
  }

}

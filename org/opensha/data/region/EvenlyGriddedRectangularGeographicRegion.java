package org.opensha.data.region;

import java.util.ListIterator;


import org.opensha.data.LocationList;
import org.opensha.data.Location;
import org.opensha.exceptions.RegionConstraintException;

/**
 * <p>Title: EvenlyGriddedRectangularGrographicRegion</p>
 * <p>Description: It creates a evenly gridded geographic region for the
 * specified gridded region provided by the user.  All grid points are nice
 * valuesin that lat/gridSpacing and lon/gridASpacing are always whole numbers.</p>
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

    setGridSpacing(gridSpacing);

    if (D) System.out.println("numLatGridPoints="+numLatGridPoints+"; numLonGridPoints="+numLonGridPoints);
  }


  /**
   * It samples out the grids location points based on the grid spacing(in degrees)
   * chosen.
   * @param degrees: sets the grid spacing
   */
  public void setGridSpacing(double gridSpacing){
    this.gridSpacing=gridSpacing;

    //getting the nice Min Lat and Lon , so that each Lat-Lon is a perfect
    //multiple of gridSpacing
    niceMinLat = Math.ceil(minLat/gridSpacing)*gridSpacing;
    niceMinLon = Math.ceil(minLon/gridSpacing)*gridSpacing;
    niceMaxLat = Math.floor(maxLat/gridSpacing)*gridSpacing;
    niceMaxLon = Math.floor(maxLon/gridSpacing)*gridSpacing;

    numLatGridPoints = (int)Math.rint((niceMaxLat - niceMinLat)/gridSpacing)+1;
    numLonGridPoints = (int)Math.rint((niceMaxLon - niceMinLon)/gridSpacing)+1;

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
  public Location getGridLocation(int index) throws RegionConstraintException {

    //gets the row for the latitude in which that index of grid exists
    int row=index/numLonGridPoints;


    //gets the column in the row (longitude point) where that index exists
    int col=index%numLonGridPoints;
    if(row > numLatGridPoints || col > numLonGridPoints)
      throw new RegionConstraintException("Not a valid index in the region");
    //lat and lon for that indexed point
    double newLat=niceMinLat+row*gridSpacing;
    double newLon=niceMinLon+col*gridSpacing;

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
  public int getNearestLocationIndex(Location loc) throws
      RegionConstraintException {
    //getting the Location lat and Lon

    double lat = loc.getLatitude();
    double lon = loc.getLongitude();
    //throw exception if location is outside the region lat bounds.
    if (!this.isLocationInside(loc))
      throw new RegionConstraintException(
          "Location outside the given Gridded Region bounds");
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

    //Considering the Locations to be store in a 2D container where lats are rows
    //and lons are the Cols.
    //getting the Row index
    int rowIndex = (int)Math.rint((lat - niceMinLat)/gridSpacing);
    //getting the Col index
    int colIndex = (int)Math.rint((lon - niceMinLon)/gridSpacing);

    //System.out.println("rowIndex:"+ rowIndex+"  colIndex:"+colIndex);

    // getting the index of the location in the region.
    int index = rowIndex*numLonGridPoints+colIndex;
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
      lat = niceMinLat + gridSpacing*iLat;
      for(int iLon=0; iLon < this.numLonGridPoints; iLon++){
        lon=niceMinLon+gridSpacing*iLon;
        gridLocsList.addLocation(new Location(lat,lon));
      }
    }
    return gridLocsList;
  }


  public static void main(String[] args) {

    EvenlyGriddedRectangularGeographicRegion geoReg = null;
    try {
      geoReg = new EvenlyGriddedRectangularGeographicRegion(39.0, 39.99, 120.,
          121.0, .1);
    }
    catch (RegionConstraintException ex) {
    }

    System.out.println(C+": numLocations="+ geoReg.getNumRegionOutlineLocations());

    System.out.println(C+": getMinLat ="+ geoReg.getMinLat());
    System.out.println(C+": getMaxLat ="+ geoReg.getMaxLat());
    System.out.println(C+": getMinLon ="+ geoReg.getMinLon());
    System.out.println(C+": getMaxLon ="+ geoReg.getMaxLon());

    System.out.println(C+": numGridLocs="+ geoReg.getNumGridLocs());

    Location locIn=new Location(39.98, 120.5);
    Location locOut = null;
    try {
      locOut = geoReg.getNearestLocation(locIn);
    }catch (RegionConstraintException ex) {
    }
    System.out.println("locIn  inside = "+geoReg.isLocationInside(locIn));
    System.out.println("locOut inside = "+geoReg.isLocationInside(locOut));

    System.out.println("locOut = "+locOut.toString());


/*
    LocationList list = geoReg.getGridLocationsList();

    System.out.print("numInList="+list.size()+"; getNumGridLocs="+geoReg.getNumGridLocs()+"\n");

    Location tempLoc;
    for(int i = 0; i < geoReg.getNumGridLocs(); i++) {
      tempLoc = (Location) list.getLocationAt(i);
      try {
        System.out.print("index=" + i + "; Loc from list:  " + tempLoc.toString() +
                         "; Loc from method:  " +
                         (geoReg.getGridLocation(i)).toString() + "\n");
      }
      catch (RegionConstraintException ex1) {
        ex1.printStackTrace();
      }
    }
 */

  }



  /**
   * Returns the nearest location in the gridded region to the provided Location.
   * @param loc Location Location to which we have to find the nearest location.
   * @return Location Nearest Location
   */
  public Location getNearestLocation(Location loc) throws RegionConstraintException{
    //Getting the nearest Location to the rupture point location


    //getting the nearest Latitude.
    double lat = Math.rint(loc.getLatitude() / gridSpacing) *
        gridSpacing;
    //getting the nearest Longitude.
    double lon = Math.rint(loc.getLongitude() / gridSpacing) *
        gridSpacing;

    //throw exception if location  lat is outside the region lat bounds.
    if (!isLocationInside(loc))
      throw new RegionConstraintException(
          "Location outside the given Gridded Region bounds");

    return new Location(lat, lon);
  }

  /**
   * Returns the minimum Lat so that this gridLat/gridSpacing is an int,
   * and this min Lat is within the polygon;
   * @return double
   */
  public double getMinGridLat(){
    return niceMinLat;
  }


  /**
   * Returns the maximum Lat so that this gridLat/gridSpacing is an int,
   * and this max Lat is within the polygon;
   * @return double
   */
  public double getMaxGridLat(){
    return niceMaxLat;
  }

  /**
   * Returns the minimum Lon so that this gridLon/gridSpacing is an int,
   * and this min Lon is within the polygon;
   * @return double
   */
  public double getMinGridLon(){
    return niceMinLon;
  }

  /**
   * Returns the maximum Lon so that this gridLon/gridSpacing is an int,
   * and this max Lon is within the polygon;
   * @return double
   */
  public double getMaxGridLon(){
    return niceMaxLon ;
  }
}

package org.scec.data.region;

import java.util.ListIterator;

import org.scec.data.LocationList;
import org.scec.data.Location;

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


  /**
   * class constructor
   * @param minLat
   * @param maxLat
   * @param minLon
   * @param maxLon
   */
  public EvenlyGriddedRectangularGeographicRegion(double minLat,double maxLat,
      double minLon,double maxLon, double gridSpacing) {
    super(minLat,maxLat,minLon,maxLon);
    this.gridSpacing=gridSpacing;

    //set the number of grid points for lat and lon
    numLatGridPoints = (int) Math.ceil((getMaxLat()-getMinLat())/gridSpacing)+1;
    numLonGridPoints = (int) Math.ceil((getMaxLon()-getMinLon())/gridSpacing)+1;

    if (D) System.out.println("numLatGridPoints="+numLatGridPoints+"; numLonGridPoints="+numLonGridPoints);

  }


  /**
   * It samples out the grids location points based on the grid spacing(in degrees)
   * chosen.
   * @param degrees: sets the grid spacing
   */
  public void setGridSpacing(double degrees){
    gridSpacing = degrees;

    //set the number of grid points for lat and lon
    numLatGridPoints=(int)Math.ceil(getMaxLat()-getMinLat()/gridSpacing)+1;
    numLonGridPoints=(int)Math.ceil(getMaxLon()-getMinLon()/gridSpacing)+1;
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

    //creating the instance of the locationList
    LocationList gridLocsList=createGriddedLocationList();
    //return the ListIterator for the locationList
    return gridLocsList.listIterator();
  }

  /**
   *
   * @returns the GridLocations List
   */
  public LocationList getGridLocationsList(){
    //creating the instance of the locationList
    LocationList gridLocsList=createGriddedLocationList();
    return gridLocsList;
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
   * Private method to create the Location List for the gridded Rectangular Geog. Region
   * @returns the LocationList
   */
  private LocationList createGriddedLocationList(){
    double lat,lon;

    //creates a instance of new locationList
    LocationList gridLocsList=new LocationList();
    for(int iLat=0;iLat < numLatGridPoints; iLat++){
      lat = getMinLat() + gridSpacing*(double)iLat;
      for(int iLon=0; iLon < this.numLonGridPoints; iLon++){
        lon=getMinLon()+gridSpacing*(double)iLon;
        gridLocsList.addLocation(new Location(lat,lon));
      }
    }
    return gridLocsList;
  }


  public static void main(String[] args) {

    EvenlyGriddedRectangularGeographicRegion geoReg = new EvenlyGriddedRectangularGeographicRegion(33.,33.9,120.,121.9,1);

    System.out.println(C+": numLocations="+ geoReg.getNumLocations());

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


}
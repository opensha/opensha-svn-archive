package org.opensha.data.region;


import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.exceptions.RegionConstraintException;

/**
 * <p>Title: RectangularGeographicRegion</p>
 *
 * <p>Description: This class defines a Rectangular Geographical region.</p>
 *
 * @author : Nitin Gupta, Vipin Gupta, and Edward Field
 * @created: March 5,2003
 * @version 1.0
 */

public class RectangularGeographicRegion extends GeographicRegion {

  private static final long serialVersionUID = 0xDEC5F47;
  
  private final static String C = "RectangularGeographicRegion";
  private final static boolean D = false;
  
  public static RectangularGeographicRegion createEntireGlobeRegion() {
	  try {
		return new RectangularGeographicRegion(-90, 90, -180, 180);
	} catch (RegionConstraintException e) {
		// it should never get here
		e.printStackTrace();
		return null;
	}
  }


  /**
   * default constructor
   */
  public RectangularGeographicRegion(double minLat,double maxLat,double minLon,
                                     double maxLon) throws RegionConstraintException{

    //sets the class variable
    super.minLat=minLat;
    super.maxLat=maxLat;
    super.minLon=minLon;
    super.maxLon=maxLon;

  if(minLat > maxLat)
    throw new RegionConstraintException(
        "Min. Lat must be less then Max. Lat.\n");
  if(minLon > maxLon)
    throw new RegionConstraintException(
        "Min. Lon must be less then Max. Lon.\n");



    //creates the Location List for this rectangular region.
    locList=new LocationList();
    locList.addLocation(new Location(minLat,minLon));
    locList.addLocation(new Location(minLat,maxLon));
    locList.addLocation(new Location(maxLat,maxLon));
    locList.addLocation(new Location(maxLat,minLon));
  }



  /**
   * This method checks whether the given location is within the region using the definition of
   * insidedness used in the parent class (true if on lower or left-hand boundary, but false
   * if on the upper or right-hand boundary)
   * @param location
   * @return
   */
  public boolean isLocationInside(Location location){
    double tempLat=location.getLatitude();
    double tempLon=location.getLongitude();

    if(D) System.out.println(C +": minLat="+minLat+"; maxLat="+maxLat+"; minLon="+minLon+"; maxLon="+maxLon);

    if((tempLat >= minLat && tempLat < maxLat) && (tempLon >= minLon && tempLon < maxLon))
      return true;
    return false;

  }
}

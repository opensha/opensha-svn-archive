package scratch.matt.calc;

import java.util.*;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.region.EvenlyGriddedSausageGeographicRegion;


import org.opensha.sha.earthquake.griddedForecast.*;
import org.opensha.sha.earthquake.observedEarthquake.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class STEP_TypeIIAftershockZone_Calc {
  //private ObsEqkRupList newObsEventsList;
  private EvenlyGriddedSausageGeographicRegion typeIIAftershockZone;
  private LocationList faultSegments;
  private double zoneRadius, gridSpacing;

  public STEP_TypeIIAftershockZone_Calc(ObsEqkRupList newObsEventsList, STEP_CombineForecastModels aftershockModel) {
    ObsEqkRupture mainshock = aftershockModel.getMainShock();
    Location mainshockLoc = mainshock.getHypocenterLocation();
    double gridSpacing = aftershockModel.get_GridSpacing();
    double zoneRadius = aftershockModel.get_AftershockZoneRadius();
    double faultRadius = aftershockModel.get_AftershockZoneRadius();
    calc_SyntheticFault(newObsEventsList, mainshockLoc);
  }

  /**
   * calc_SyntheticFault
   * This method is not yet finished.  A sort method is needed in LocationList
   * to sort on lats and longs.
   */
  public static void calc_SyntheticFault(ObsEqkRupList newObsEventsList, Location mainshockLoc) {
    ListIterator eventIt = newObsEventsList.listIterator();
    int numEvents = newObsEventsList.size();
    double[] eLat = new double[numEvents];
    double[] eLong = new double[numEvents];
    ObsEqkRupture event = new ObsEqkRupture();
    Location eLoc = new Location();
    LocationList latLongList = new LocationList();

    int ind = 0;
    while (eventIt.hasNext()){
      event = (ObsEqkRupture)eventIt.next();
      eLoc = event.getHypocenterLocation();
      latLongList.addLocation(eLoc);
    }

    /**
     * sort the lat long pairs and ignore the extreme values (.01 and .99)
     */
    int minInd = (int)Math.round(0.01*numEvents);
    int maxInd = (int)Math.round(0.99*numEvents);
    int numIn = (int)Math.round(.8*numEvents);
    double maxLat_LatSort = eLat[maxInd];
    double minLat_LatSort = eLat[minInd];
    double maxLong_LongSort = eLong[maxInd];
    double minLong_LongSort = eLong[minInd];

    /**
     * THESE WILL NEED TO BE SET ONCE THE SORT METHOD IS
     * implemented in LocationList
     */
    double maxLong_LatSort = 0;
    double minLong_LatSort = 0;
    double maxLat_LongSort = 0;
    double minLat_LongSort = 0;
    double latDiff = maxLat_LatSort-minLat_LatSort;
    double longDiff = maxLong_LongSort-minLong_LongSort;

    /** find the largest dimension - either in Lat or in Long
     *  this needs to be improved
     */

    LocationList faultSegments = new LocationList();
    double topEventLat, topEventLong, bottomEventLat, bottomEventLong;
    Location topEndPoint = new Location();
    Location bottomEndPoint = new Location();
    if (latDiff > longDiff){
      topEndPoint.setLatitude(maxLat_LatSort);
      topEndPoint.setLongitude(maxLong_LatSort);
      bottomEndPoint.setLatitude(minLat_LatSort);
      bottomEndPoint.setLongitude(minLong_LatSort);
    }
    else {
      topEndPoint.setLatitude(maxLat_LongSort);
      topEndPoint.setLongitude(maxLong_LongSort);
      bottomEndPoint.setLatitude(minLat_LongSort);
      bottomEndPoint.setLongitude(minLong_LongSort);
    }

    /**
     * Create a two segment fault that passes thru the mainshock
     * using the extreme widths defined above
     */
    faultSegments.addLocation(topEndPoint);
    faultSegments.addLocation(mainshockLoc);
    faultSegments.addLocation(mainshockLoc);
    faultSegments.addLocation(bottomEndPoint);
  }

  /**
   * CreateAftershockZoneDef
   */
  public void CreateAftershockZoneDef() {
    EvenlyGriddedSausageGeographicRegion typeIIAftershockZone =
        new EvenlyGriddedSausageGeographicRegion(faultSegments,zoneRadius,gridSpacing);
    /**
     * The rest will have to be filled in for a "Sausage" Geographic
     * Region on a SausageGeographicRegion is defined.
     */
  }

  /**
   * get_TypeIIAftershockZone
   * This needs to be changed to return a sausage region once
   * this type of region is defined.
   */
  public EvenlyGriddedSausageGeographicRegion get_TypeIIAftershockZone() {
    return typeIIAftershockZone;
  }

  /**
   * getTypeIIFaultModel
   */
  public LocationList getTypeIIFaultModel() {
    return faultSegments;
  }

}

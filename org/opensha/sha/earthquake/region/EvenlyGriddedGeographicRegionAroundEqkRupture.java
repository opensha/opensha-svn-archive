package org.opensha.sha.earthquake.region;

import org.opensha.sha.earthquake.*;
import org.opensha.data.region.*;
import org.opensha.sha.surface.GriddedSurfaceAPI;
import org.opensha.data.*;

/**
 * <p>Title: EvenlyGriddedGeographicRegionAroundEqkRupture</p>
 *
 * <p>Description: This class allows to create a Evenly Gridded
 * GeographicRegion around a given EqkRupture.
 * It creates a polygon around the EqkRupture such that each point
 * on the polygon is ~radius from the edge of the horizontal projection of
 * the eqkRupture surface.
 * </p>
 *
 * @author Nitin Gupta , Edward (Ned) Field, Vipin Gupta
 * @version 1.0
 */
public class EvenlyGriddedGeographicRegionAroundEqkRupture
    extends EvenlyGriddedSausageGeographicRegion {


    /**
     * Class constructor that takes in a EqkRupture around which EvenlyGridded
     * Geographical Region is to be constructed.
     *
     * Note : This class only considers surface locations of the ruptures, which
     * can be considered as a straight line.
     * So in this case we just take 2 end-point locations one rupture surface
     * and create a EvenlyGridded Sausage Region of given radius and gridSpacing.
     *
     * @param rupture EqkRupture EqkRupture around which region is to be constructed
     * @param radius double maximum distance from any location on teh rupture surface.
     * @param gridSpacing double GridSpacing in degrees.
     */
    public EvenlyGriddedGeographicRegionAroundEqkRupture(EqkRupture rupture, double radius,
      double gridSpacing){
    GriddedSurfaceAPI surface = rupture.getRuptureSurface();
    LocationList locList = new LocationList();
    locList.addLocation(surface.getLocation(0,0));
    locList.addLocation(surface.getLocation(0,surface.getNumCols()-1));
    locList.addLocation(surface.getLocation(surface.getNumRows()-1,surface.getNumCols()-1));
    locList.addLocation(surface.getLocation(surface.getNumRows()-1,0));
    this.createEvenlyGriddedSausageGeographicRegion(locList, radius,gridSpacing);
  }



}

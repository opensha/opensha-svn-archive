/**
 * 
 */
package org.opensha.commons.data.region;



import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;

/**
 * Evenly Gridded Northern California Region.
 * This was defined based on Ned's email on Sep 14, 2007 that defined the line that cut the RELM polygon
 * 
 * @author vipingupta
 *
 */
@Deprecated
public class EvenlyGriddedNoCalRegion extends EvenlyGriddedGeographicRegion {
	  protected final static double GRID_SPACING = 0.1;

	  public EvenlyGriddedNoCalRegion() {
	    /**
	     * Location list for No Cal region
	     */
	    LocationList locList = getLocationList();
	    // make polygon from the location list
	    //createEvenlyGriddedGeographicRegion(locList, GRID_SPACING);
	  }

	  /**
	   * Location list which formas the outline of the ploygon for Northern California
	   */
	  protected LocationList getLocationList() {
	    LocationList locList = new LocationList();
	    locList.addLocation(new Location(	34.8	,	-122	));
	    locList.addLocation(new Location(	34.9	,	-122.1	));
	    locList.addLocation(new Location(	35	,	-122.1	));
	    locList.addLocation(new Location(	35.1	,	-122.2	));
	    locList.addLocation(new Location(	35.2	,	-122.3	));
	    locList.addLocation(new Location(	35.3	,	-122.3	));
	    locList.addLocation(new Location(	35.4	,	-122.4	));
	    locList.addLocation(new Location(	35.5	,	-122.4	));
	    locList.addLocation(new Location(	35.6	,	-122.5	));
	    locList.addLocation(new Location(	35.7	,	-122.6	));
	    locList.addLocation(new Location(	35.8	,	-122.6	));
	    locList.addLocation(new Location(	35.9	,	-122.7	));
	    locList.addLocation(new Location(	36	,	-122.8	));
	    locList.addLocation(new Location(	36.1	,	-122.8	));
	    locList.addLocation(new Location(	36.2	,	-122.9	));
	    locList.addLocation(new Location(	36.3	,	-123	));
	    locList.addLocation(new Location(	36.4	,	-123	));
	    locList.addLocation(new Location(	36.5	,	-123.1	));
	    locList.addLocation(new Location(	36.6	,	-123.1	));
	    locList.addLocation(new Location(	36.7	,	-123.2	));
	    locList.addLocation(new Location(	36.8	,	-123.3	));
	    locList.addLocation(new Location(	36.9	,	-123.3	));
	    locList.addLocation(new Location(	37	,	-123.4	));
	    locList.addLocation(new Location(	37.1	,	-123.5	));
	    locList.addLocation(new Location(	37.2	,	-123.5	));
	    locList.addLocation(new Location(	37.3	,	-123.6	));
	    locList.addLocation(new Location(	37.4	,	-123.6	));
	    locList.addLocation(new Location(	37.5	,	-123.7	));
	    locList.addLocation(new Location(	37.6	,	-123.8	));
	    locList.addLocation(new Location(	37.7	,	-123.8	));
	    locList.addLocation(new Location(	37.8	,	-123.9	));
	    locList.addLocation(new Location(	37.9	,	-124	));
	    locList.addLocation(new Location(	38	,	-124	));
	    locList.addLocation(new Location(	38.1	,	-124.1	));
	    locList.addLocation(new Location(	38.2	,	-124.2	));
	    locList.addLocation(new Location(	38.3	,	-124.2	));
	    locList.addLocation(new Location(	38.4	,	-124.3	));
	    locList.addLocation(new Location(	38.5	,	-124.3	));
	    locList.addLocation(new Location(	38.6	,	-124.4	));
	    locList.addLocation(new Location(	38.7	,	-124.5	));
	    locList.addLocation(new Location(	38.8	,	-124.5	));
	    locList.addLocation(new Location(	38.9	,	-124.6	));
	    locList.addLocation(new Location(	39	,	-124.7	));
	    locList.addLocation(new Location(	39.1	,	-124.7	));
	    locList.addLocation(new Location(	39.2	,	-124.8	));
	    locList.addLocation(new Location(	39.3	,	-124.9	));
	    locList.addLocation(new Location(	39.4	,	-124.9	));
	    locList.addLocation(new Location(	39.5	,	-125	));
	    locList.addLocation(new Location(	39.6	,	-125	));
	    locList.addLocation(new Location(	39.7	,	-125.1	));
	    locList.addLocation(new Location(	39.8	,	-125.2	));
	    locList.addLocation(new Location(	39.9	,	-125.2	));
	    locList.addLocation(new Location(	40	,	-125.3	));
	    locList.addLocation(new Location(	40.1	,	-125.4	));
	    locList.addLocation(new Location(	40.2	,	-125.4	));
	    locList.addLocation(new Location(	40.3	,	-125.4	));
	    locList.addLocation(new Location(	40.4	,	-125.4	));
	    locList.addLocation(new Location(	40.5	,	-125.4	));
	    locList.addLocation(new Location(	40.6	,	-125.4	));
	    locList.addLocation(new Location(	40.7	,	-125.4	));
	    locList.addLocation(new Location(	40.8	,	-125.4	));
	    locList.addLocation(new Location(	40.9	,	-125.4	));
	    locList.addLocation(new Location(	41	,	-125.4	));
	    locList.addLocation(new Location(	41.1	,	-125.4	));
	    locList.addLocation(new Location(	41.2	,	-125.3	));
	    locList.addLocation(new Location(	41.3	,	-125.3	));
	    locList.addLocation(new Location(	41.4	,	-125.3	));
	    locList.addLocation(new Location(	41.5	,	-125.3	));
	    locList.addLocation(new Location(	41.6	,	-125.3	));
	    locList.addLocation(new Location(	41.7	,	-125.3	));
	    locList.addLocation(new Location(	41.8	,	-125.3	));
	    locList.addLocation(new Location(	41.9	,	-125.3	));
	    locList.addLocation(new Location(	42	,	-125.3	));
	    locList.addLocation(new Location(	42.1	,	-125.3	));
	    locList.addLocation(new Location(	42.2	,	-125.3	));
	    locList.addLocation(new Location(	42.3	,	-125.3	));
	    locList.addLocation(new Location(	42.4	,	-125.3	));
	    locList.addLocation(new Location(	42.5	,	-125.2	));
	    locList.addLocation(new Location(	42.6	,	-125.2	));
	    locList.addLocation(new Location(	42.7	,	-125.2	));
	    locList.addLocation(new Location(	42.8	,	-125.2	));
	    locList.addLocation(new Location(	42.9	,	-125.2	));
	    locList.addLocation(new Location(	43	,	-125.2	));
	    locList.addLocation(new Location(	43	,	-119	));
	    locList.addLocation(new Location(	42.9	,	-119	));
	    locList.addLocation(new Location(	42.8	,	-119	));
	    locList.addLocation(new Location(	42.7	,	-119	));
	    locList.addLocation(new Location(	42.6	,	-119	));
	    locList.addLocation(new Location(	42.5	,	-119	));
	    locList.addLocation(new Location(	42.4	,	-119	));
	    locList.addLocation(new Location(	42.3	,	-119	));
	    locList.addLocation(new Location(	42.2	,	-119	));
	    locList.addLocation(new Location(	42.1	,	-119	));
	    locList.addLocation(new Location(	42	,	-119	));
	    locList.addLocation(new Location(	41.9	,	-119	));
	    locList.addLocation(new Location(	41.8	,	-119	));
	    locList.addLocation(new Location(	41.7	,	-119	));
	    locList.addLocation(new Location(	41.6	,	-119	));
	    locList.addLocation(new Location(	41.5	,	-119	));
	    locList.addLocation(new Location(	41.4	,	-119	));
	    locList.addLocation(new Location(	41.3	,	-119	));
	    locList.addLocation(new Location(	41.2	,	-119	));
	    locList.addLocation(new Location(	41.1	,	-119	));
	    locList.addLocation(new Location(	41	,	-119	));
	    locList.addLocation(new Location(	40.9	,	-119	));
	    locList.addLocation(new Location(	40.8	,	-119	));
	    locList.addLocation(new Location(	40.7	,	-119	));
	    locList.addLocation(new Location(	40.6	,	-119	));
	    locList.addLocation(new Location(	40.5	,	-119	));
	    locList.addLocation(new Location(	40.4	,	-119	));
	    locList.addLocation(new Location(	40.3	,	-119	));
	    locList.addLocation(new Location(	40.2	,	-119	));
	    locList.addLocation(new Location(	40.1	,	-119	));
	    locList.addLocation(new Location(	40	,	-119	));
	    locList.addLocation(new Location(	39.9	,	-119	));
	    locList.addLocation(new Location(	39.8	,	-119	));
	    locList.addLocation(new Location(	39.7	,	-119	));
	    locList.addLocation(new Location(	39.6	,	-119	));
	    locList.addLocation(new Location(	39.5	,	-119	));
	    locList.addLocation(new Location(	39.4	,	-118.9	));
	    locList.addLocation(new Location(	39.3	,	-118.8	));
	    locList.addLocation(new Location(	39.2	,	-118.7	));
	    locList.addLocation(new Location(	39.1	,	-118.5	));
	    locList.addLocation(new Location(	39	,	-118.4	));
	    locList.addLocation(new Location(	38.9	,	-118.3	));
	    locList.addLocation(new Location(	38.8	,	-118.1	));
	    locList.addLocation(new Location(	38.7	,	-118	));
	    locList.addLocation(new Location(	38.6	,	-117.9	));
	    locList.addLocation(new Location(	38.5	,	-117.7	));
	    locList.addLocation(new Location(	38.4	,	-117.6	));
	    return locList;
	  }
	  
	
	}

/**
 * 
 */
package org.opensha.commons.data.region;


import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;

/**
 * Evenly Gridded Southern California Region.
 * This was defined based on Ned's email on Sep 14, 2007 that defined the line that cut the RELM polygon
 * 
 * @author vipingupta
 *
 */
public class EvenlyGriddedSoCalRegion extends EvenlyGriddedGeographicRegion {
	  protected final static double GRID_SPACING = 0.1;

	  public EvenlyGriddedSoCalRegion() {
		  
	    /**
	     * Location list for So Cal region
	     */
	    LocationList locList = getLocationList();
	    // make polygon from the location list
	    createEvenlyGriddedGeographicRegion(locList, GRID_SPACING);
	  }

	  /**
	   * Location list which formas the outline of the ploygon for SoCal REgion
	   * This was obtained by reading RELM output file and finding min/max Longitude. then, Ned edited the file to minimize the number of points
	   */
	  protected LocationList getLocationList() {
	    LocationList locList = new LocationList();
	    
	    locList.addLocation(new Location(	31.5	,	-117.2	));
	    locList.addLocation(new Location(	31.6	,	-117.4	));
	    locList.addLocation(new Location(	31.7	,	-117.6	));
	    locList.addLocation(new Location(	31.8	,	-117.8	));
	    locList.addLocation(new Location(	31.9	,	-117.9	));
	    locList.addLocation(new Location(	32	,	-118	));
	    locList.addLocation(new Location(	32.1	,	-118	));
	    locList.addLocation(new Location(	32.2	,	-118.1	));
	    locList.addLocation(new Location(	32.3	,	-118.2	));
	    locList.addLocation(new Location(	32.4	,	-118.2	));
	    locList.addLocation(new Location(	32.5	,	-118.3	));
	    locList.addLocation(new Location(	32.6	,	-118.3	));
	    locList.addLocation(new Location(	32.7	,	-118.4	));
	    locList.addLocation(new Location(	32.8	,	-118.5	));
	    locList.addLocation(new Location(	32.9	,	-118.8	));
	    locList.addLocation(new Location(	33	,	-119.1	));
	    locList.addLocation(new Location(	33.1	,	-119.4	));
	    locList.addLocation(new Location(	33.2	,	-119.7	));
	    locList.addLocation(new Location(	33.3	,	-120	));
	    locList.addLocation(new Location(	33.4	,	-120.3	));
	    locList.addLocation(new Location(	33.5	,	-120.6	));
	    locList.addLocation(new Location(	33.6	,	-120.9	));
	    locList.addLocation(new Location(	33.7	,	-121.1	));
	    locList.addLocation(new Location(	33.8	,	-121.2	));
	    locList.addLocation(new Location(	33.9	,	-121.3	));
	    locList.addLocation(new Location(	34	,	-121.4	));
	    locList.addLocation(new Location(	34.1	,	-121.5	));
	    locList.addLocation(new Location(	34.2	,	-121.6	));
	    locList.addLocation(new Location(	34.3	,	-121.7	));
	    locList.addLocation(new Location(	34.4	,	-121.8	));
	    locList.addLocation(new Location(	34.5	,	-121.8	));
	    locList.addLocation(new Location(	34.6	,	-121.9	));
	    locList.addLocation(new Location(	34.7	,	-121.9	));
	    locList.addLocation(new Location(	34.8	,	-122	));
	    locList.addLocation(new Location(	38.4	,	-117.6	));
	    locList.addLocation(new Location(	38.3	,	-117.4	));
	    locList.addLocation(new Location(	38.2	,	-117.3	));
	    locList.addLocation(new Location(	38.1	,	-117.2	));
	    locList.addLocation(new Location(	38	,	-117	));
	    locList.addLocation(new Location(	37.9	,	-116.9	));
	    locList.addLocation(new Location(	37.8	,	-116.8	));
	    locList.addLocation(new Location(	37.7	,	-116.6	));
	    locList.addLocation(new Location(	37.6	,	-116.5	));
	    locList.addLocation(new Location(	37.5	,	-116.4	));
	    locList.addLocation(new Location(	37.4	,	-116.2	));
	    locList.addLocation(new Location(	37.3	,	-116.1	));
	    locList.addLocation(new Location(	37.2	,	-116	));
	    locList.addLocation(new Location(	37.1	,	-115.8	));
	    locList.addLocation(new Location(	37	,	-115.7	));
	    locList.addLocation(new Location(	36.9	,	-115.6	));
	    locList.addLocation(new Location(	36.8	,	-115.4	));
	    locList.addLocation(new Location(	36.7	,	-115.3	));
	    locList.addLocation(new Location(	36.6	,	-115.1	));
	    locList.addLocation(new Location(	36.5	,	-115	));
	    locList.addLocation(new Location(	36.4	,	-114.9	));
	    locList.addLocation(new Location(	36.3	,	-114.7	));
	    locList.addLocation(new Location(	36.2	,	-114.6	));
	    locList.addLocation(new Location(	36.1	,	-114.5	));
	    locList.addLocation(new Location(	36	,	-114.3	));
	    locList.addLocation(new Location(	35.9	,	-114.2	));
	    locList.addLocation(new Location(	35.8	,	-114.1	));
	    locList.addLocation(new Location(	35.7	,	-114	));
	    locList.addLocation(new Location(	35.6	,	-113.9	));
	    locList.addLocation(new Location(	35.5	,	-113.8	));
	    locList.addLocation(new Location(	35.4	,	-113.8	));
	    locList.addLocation(new Location(	35.3	,	-113.7	));
	    locList.addLocation(new Location(	35.2	,	-113.6	));
	    locList.addLocation(new Location(	35.1	,	-113.6	));
	    locList.addLocation(new Location(	35	,	-113.5	));
	    locList.addLocation(new Location(	34.9	,	-113.5	));
	    locList.addLocation(new Location(	34.8	,	-113.4	));
	    locList.addLocation(new Location(	34.7	,	-113.3	));
	    locList.addLocation(new Location(	34.6	,	-113.3	));
	    locList.addLocation(new Location(	34.5	,	-113.2	));
	    locList.addLocation(new Location(	34.4	,	-113.1	));
	    locList.addLocation(new Location(	34.3	,	-113.1	));
	    locList.addLocation(new Location(	34.2	,	-113.1	));
	    locList.addLocation(new Location(	34.1	,	-113.1	));
	    locList.addLocation(new Location(	34	,	-113.2	));
	    locList.addLocation(new Location(	33.9	,	-113.2	));
	    locList.addLocation(new Location(	33.8	,	-113.2	));
	    locList.addLocation(new Location(	33.7	,	-113.3	));
	    locList.addLocation(new Location(	33.6	,	-113.3	));
	    locList.addLocation(new Location(	33.5	,	-113.3	));
	    locList.addLocation(new Location(	33.4	,	-113.3	));
	    locList.addLocation(new Location(	33.3	,	-113.4	));
	    locList.addLocation(new Location(	33.2	,	-113.4	));
	    locList.addLocation(new Location(	33.1	,	-113.4	));
	    locList.addLocation(new Location(	33	,	-113.5	));
	    locList.addLocation(new Location(	32.9	,	-113.5	));
	    locList.addLocation(new Location(	32.8	,	-113.5	));
	    locList.addLocation(new Location(	32.7	,	-113.5	));
	    locList.addLocation(new Location(	32.6	,	-113.5	));
	    locList.addLocation(new Location(	32.5	,	-113.6	));
	    locList.addLocation(new Location(	32.4	,	-113.6	));
	    locList.addLocation(new Location(	32.3	,	-113.6	));
	    locList.addLocation(new Location(	32.2	,	-113.6	));
	    locList.addLocation(new Location(	32.1	,	-113.7	));
	    locList.addLocation(new Location(	32	,	-113.9	));
	    locList.addLocation(new Location(	31.9	,	-114.1	));
	    locList.addLocation(new Location(	31.8	,	-114.2	));
	    locList.addLocation(new Location(	31.7	,	-114.4	));
	    locList.addLocation(new Location(	31.6	,	-115.2	));
	    locList.addLocation(new Location(	31.5	,	-116.4	));
	    return locList;
	  }
	  
	}
package org.opensha.cybershake.db;

import java.util.ArrayList;
import java.util.ListIterator;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.CircularGeographicRegion;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.surface.EvenlyGridCenteredSurface;
import org.opensha.sha.surface.EvenlyGriddedSurfaceAPI;

public class CybershakeSiteInfo2DB {

	
	private static final double CUT_OFF_DISTANCE = 200;
	private SiteInfo2DBAPI site2db;
	
	public CybershakeSiteInfo2DB(DBAccess db){
		site2db = new SiteInfo2DB(db);
	}
	
	
	
	/**
	 * Puts the Cybershake locations in the database
	 * @param siteName
	 * @param siteShortName
	 * @param siteLat
	 * @param siteLon
	 * @returns the SiteId from the database
	 */
	public int  putCybershakeLocationInDB(String siteName,String siteShortName,double siteLat,double siteLon){
	    return site2db.insertSite(siteName, siteShortName, siteLat, siteLon);
	}
	
	/**
	 * Returns the CyberShake site id for the corresponding Cybershake short site name in the database
	 * @param cybershakeShortSiteName
	 * @return
	 */
	public int getCybershakeSiteId(String cybershakeShortSiteName){
		return site2db.getSiteId(cybershakeShortSiteName);
	}
	
	/**
	 * Returns the Cybershake site id for the corresponding location with given Lat and Lon.
	 * @param lat
	 * @param lon
	 * @return
	 */
	public int getCybershakeSiteId(double lat,double lon){
		return site2db.getSiteId(lat, lon);
	}
	

	/**
	 * Finds all the ruptures that have any location on their surface within Cybershake location
	 * circular regional bounds.
	 * @param erf
	 * @param erfId
	 * @param siteId
	 * @param locLat
	 * @param locLon
	 */
	public void putCyberShakeLocationSrcRupInfo(EqkRupForecastAPI eqkRupForecast,int erfId,
			int siteId,double locLat,double locLon){
		Location loc = new Location(locLat,locLon);
		CircularGeographicRegion region = new CircularGeographicRegion(loc,CUT_OFF_DISTANCE);
		int numSources = eqkRupForecast.getNumSources();

	    //Going over each and every source in the forecast
	    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
	      // get the ith source
	      ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
	      int numRuptures = source.getNumRuptures();

	      //going over all the ruptures in the source
	      for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {
	        ProbEqkRupture rupture = source.getRupture(rupIndex);

	        EvenlyGriddedSurfaceAPI rupSurface = new EvenlyGridCenteredSurface(rupture.getRuptureSurface());

	        //getting the iterator for all points on the rupture
	        ListIterator it = rupSurface.getAllByRowsIterator();
	        //looping over all the rupture pt locations and if any of those lies
	        //within the provided distance range then include the rupture in the list.
	        while (it.hasNext()) {
	          Location ptLoc = (Location) it.next();
	          if (region.isLocationInside(ptLoc)) {
	        	  this.site2db.insertSite_RuptureInfo(siteId, erfId, sourceIndex, rupIndex, CUT_OFF_DISTANCE);
	        	  break;
	          }
	        }
	      }
	    }
	}
	
	/**
	 * Computes the regional bounds for the given cybershake and puts in the database. Also put the ruptureId and
	 * sourceId from ERF that dictates the min/max lat/lon for the region.
	 * @param eqkRupForecast
	 * @param erfId
	 * @param siteId
	 * @param locLat
	 * @param locLon
	 */
	public void putCyberShakeLocationRegionalBounds(EqkRupForecastAPI eqkRupForecast,int erfId, int siteId,
			                                        double locLat,double locLon){
		
		Location loc = new Location(locLat,locLon);
		CircularGeographicRegion region = new CircularGeographicRegion(loc,CUT_OFF_DISTANCE);
		
	    int numSources = eqkRupForecast.getNumSources();

	    double minLat = Double.POSITIVE_INFINITY;
	    int minLatRupId = -1,minLatSrcId= -1;
	    double maxLat = Double.NEGATIVE_INFINITY;
	    int maxLatRupId =-1,maxLatSrcId=-1;
	    double minLon = Double.POSITIVE_INFINITY;
	    int minLonRupId =-1,minLonSrcId = -1;
	    double maxLon = Double.NEGATIVE_INFINITY;
	    int maxLonRupId = -1, maxLonSrcId =-1;

	    //Going over each and every source in the forecast
	    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
	      // get the ith source
	      ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
	      int numRuptures = source.getNumRuptures();

	      //going over all the ruptures in the source
	      for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {
	        ProbEqkRupture rupture = source.getRupture(rupIndex);

	        EvenlyGriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();

	        //getting the iterator for all points on the rupture
	        ListIterator it = rupSurface.getAllByRowsIterator();
	        boolean rupInside = false;
	        //looping over all the rupture pt locations and if any of those lies
	        //within the provided distance range then include the rupture in the list.
	        while (it.hasNext()) {
	          Location ptLoc = (Location) it.next();
	          if (region.isLocationInside(ptLoc)) {
	            rupInside = true;
	            break;
	          }
	        }
	        it = rupSurface.getAllByRowsIterator();
	        while (it.hasNext() && rupInside) {
	          Location ptLoc = (Location) it.next();
	          double lat = ptLoc.getLatitude();
	          double lon = ptLoc.getLongitude();
	          if (lat < minLat){
	            minLat = lat;
	            minLatRupId = rupIndex;
	            minLatSrcId = sourceIndex;
	          }
	          if (lat > maxLat){
	            maxLat = lat;
	            maxLatRupId = rupIndex;
	            maxLatSrcId = sourceIndex;
	          }
	          if (lon < minLon){
	            minLon = lon;
	            minLonRupId = rupIndex;
	            minLonSrcId = sourceIndex;
	          }
	          if (lon > maxLon){
	            maxLon = lon;
	            maxLonRupId = rupIndex;
	            maxLonSrcId = sourceIndex;
	          }
	        }
	      }
	    }	
	    
	    site2db.insertSiteRegionalBounds(siteId, erfId,CUT_OFF_DISTANCE,
	    		                         maxLat, maxLatSrcId, maxLatRupId, minLat,
	    		                         minLatSrcId, minLatRupId, maxLon, maxLonSrcId, 
	    		                         maxLonRupId, minLon, minLonSrcId, minLonRupId);
	}
	
	
	
	/**
	 * 
	 * @returns the list of cybershake sites
	 */
	public ArrayList<String> getCS_SitesList(){
		return site2db.getAllSites();
	}
	
	
	/**
	 * 
	 * @returns the list of all Cybershake Site Locations
	 */
	public LocationList getCS_SitesListLocations(){
		return site2db.getAllSitesLocation();
	}
	
	/**
	 * 
	 * @param siteShortName short site name as in database for Cybershake site
	 * @returns the Earthquake rupture forecast source id's for a given cybershake site.
	 */
	public ArrayList<Integer> getSrcIDsForSite(String csSiteName){
		return site2db.getSrcIdsForSite(csSiteName);
	}
	
	/**
	 * 
	 * @param siteShortName
	 * @param srcId
	 * @returns the list of rupture ids 
	 */
	public ArrayList<Integer> getRupIDsForSite(String csSiteName,int srcID){
		return site2db.getRupIdsForSite(csSiteName, srcID);
	}
	
	
	/**
	 * 
	 * @param csSiteName
	 * @returns the Geographic locaton for the given Cybershake site
	 */
	public Location getCyberShakeSiteLocation(String csSiteName){
		return site2db.getLocationForSite(csSiteName);
	}
	

}

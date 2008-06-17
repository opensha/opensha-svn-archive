package org.opensha.cybershake.db;

import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.LocationList;

public interface SiteInfo2DBAPI {
	
	/**
	 * Inserts the new site in the database table CyberShake_Sites
	 * @param siteName
	 * @param siteShortName
	 * @param lat
	 * @param lon
	 * @returns the Site Id for the last inserted Site
	 */
	public int insertSite(String siteName, String siteShortName,double lat,double lon);
	
	/**
	 * Inserts the rupture ids that correspond to a given site within gven cutoff distance
	 * inside the table Site_Ruptures.
	 * @param siteId
	 * @param erfId
	 * @param sourceId
	 * @param ruptureId
	 * @param cutoffDistance
	 */
	public void insertSite_RuptureInfo(int siteId,int erfId,int sourceId,int ruptureId,double cutoffDistance);
	
	/**
	 * Inserts thelist of rupture ids that correspond to a given site within given cutoff distance
	 * inside the table Site_Ruptures.
	 * @param siteId
	 * @param erfId
	 * @param sourceId
	 * @param ruptureId - list of Rupture IDs
	 * @param cutoffDistance
	 */
	public void insertSite_RuptureInfoList(int siteId, int erfId, int sourceId, ArrayList<Integer> ruptureId, double cutOffDistance);
	
	/**
	 * Inserts the regional bounds (min/max lat/lon) for all cybershake sites in table Site_Region.
	 * @param siteId
	 * @param erfId
	 * @param cutOffDistance
	 * @param maxLat
	 * @param maxLatSrcId
	 * @param maxLatRupId
	 * @param minLat
	 * @param minLatSrcId
	 * @param minLatRupId
	 * @param maxLon
	 * @param maxLonSrcId
	 * @param maxLonRupId
	 * @param minLon
	 * @param minLonSrcId
	 * @param minLonRupId
	 */
	public void insertSiteRegionalBounds(int siteId,int erfId,double cutOffDistance,double maxLat,int maxLatSrcId,
			                             int maxLatRupId,double minLat,int minLatSrcId,int minLatRupId,
			                             double maxLon,int maxLonSrcId,int maxLonRupId,double minLon,
			                             int minLonSrcId,int minLonRupId);
	
	/**
	 * Returns the site id of the cybershake site for the corresponding cybershake_short_site_name
	 * @param cybershakeShortSiteName
	 * @return
	 */
	public int getSiteId(String cybershakeShortSiteName);
	
	/**
	 * Returns the site id the cybershake site with given lat and lon
	 * @param lat
	 * @param lon
	 * @return
	 */
	public int getSiteId(double lat,double lon);
	
	/**
	 * 
	 * @returns the ArrayList of short site names for all Cybershake
	 */
	public ArrayList<String> getAllSites();
	
	/**
	 * Find out if the given rupture is already in the database
	 * @param erfID
	 * @param sourceID
	 * @param rupID
	 * @return
	 */
	public boolean isRupInDB(int erfID, int sourceID, int rupID);
	
	/**
	 * Find out if the given rupture is already in the database
	 * @param erfID
	 * @param sourceID
	 * @param rupID
	 * @param siteID
	 * @return
	 */
	public boolean isSiteRupInDB(int erfID, int sourceID, int rupID, int siteID);
	
	/**
	 * 
	 * @returns the Arraylist of all cybershake site locations
	 */
	public LocationList getAllSitesLocation();
	
	
	/**
	 * 
	 * @param siteShortName short site name as in database for Cybershake site
	 * @returns the Earthquake rupture forecast source id's for a given cybershake site.
	 */
	public ArrayList<Integer> getSrcIdsForSite(String siteShortName, int erf_ID);
	
	/**
	 * 
	 * @param siteShortName
	 * @param srcId
	 * @returns the list of rupture ids 
	 */
	public ArrayList<Integer> getRupIdsForSite(String siteShortName, int erf_ID, int srcId);
	
	/**
	 * 
	 * @param site
	 * @returns the Location for the given cybershake site location
	 */
	public Location getLocationForSite(String site);
	
	/**
	 * Gets a CybershakeSite from the Database
	 * @param shortName
	 * @return
	 */
	public CybershakeSite getSiteFromDB(String shortName);
	
	/**
	 * Gets all CybershakeSite's from the Database
	 * @return
	 */
	public ArrayList<CybershakeSite> getAllSitesFromDB();

}

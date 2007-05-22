package org.opensha.cybershake.db;

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

}

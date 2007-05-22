package org.opensha.cybershake.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SiteInfo2DB implements SiteInfo2DBAPI {

	private DBAccess dbaccess;

	
	public SiteInfo2DB(DBAccess dbaccess){
		this.dbaccess = dbaccess;
	}
	
	
	/**
	 * Inserts the new site in the database table CyberShake_Sites
	 * @param siteName
	 * @param siteShortName
	 * @param lat
	 * @param lon
	 * @returns the Site Id for the last inserted Site
	 */
	public int insertSite(String siteName, String siteShortName, double lat,
			double lon) {
//		generate the SQL to be inserted in the Sites table
		String sql = "INSERT into CyberShake_Sites"+ 
		             "(CS_Site_Name,CS_Short_Name,CS_Site_Lat,CS_Site_Lon)"+
		             "VALUES('"+siteName+"','"+siteShortName+"','"+
		(float)lat+"','"+(float)lon+"')";
		dbaccess.insertData(sql);
		
		return getSiteId(siteShortName);
		
	}

	/**
	 * Returns the site id of the cybershake site for the corresponding cybershake_short_site_name
	 * @param cybershakeShortSiteName
	 * @return
	 */
	public int getSiteId(String cybershakeShortSiteName){
//		 gets the last auto increment id from Sites table
		 String sql = "SELECT CS_Site_ID from CyberShake_Sites where CS_Short_Name = "+"'"+cybershakeShortSiteName+"'";
		 ResultSet rs =  dbaccess.selectData(sql);
			try {
				rs.first();
				return Integer.parseInt(rs.getString("CS_Site_ID"));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		 return -1;
	}
	
	/**
	 * Returns the site id the cybershake site with given lat and lon
	 * @param lat
	 * @param lon
	 * @return
	 */
	public int getSiteId(double lat,double lon){
//		 gets the last auto increment id from Sites table
		 String sql = "SELECT CS_Site_ID from CyberShake_Sites where CS_Site_Lat = "+"'"+(float)lat+"' and "+
		              "CS_Site_Lon = "+"'"+(float)lon+"'";
		 ResultSet rs =  dbaccess.selectData(sql);
			try {
				rs.first();
				return Integer.parseInt(rs.getString(0));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		 return -1;
	}
	
	/**
	 * Inserts the regional bounds (min/max lat/lon) for all cybershake sites in table CyberShake_Site_Regions
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
	public void insertSiteRegionalBounds(int siteId, int erfId,
			double cutOffDistance, double maxLat, int maxLatSrcId,
			int maxLatRupId, double minLat, int minLatSrcId, int minLatRupId,
			double maxLon, int maxLonSrcId, int maxLonRupId, double minLon,
			int minLonSrcId, int minLonRupId) {
		
//		generate the SQL to be inserted in the Site_Region table
		String sql = "INSERT into CyberShake_Site_Regions VALUES('"+siteId+"','"+erfId+"','"+
		(float)cutOffDistance+"','"+(float)maxLat+"','"+maxLatSrcId+"','"+maxLatRupId+"','"+(float)maxLon+"','"+
		maxLatSrcId+"','"+maxLonRupId+"','"+(float)minLat+"','"+minLatSrcId+"','"+minLatRupId+"','"+
		(float)minLon+"','"+minLonSrcId+"','"+minLonRupId+"')";
		dbaccess.insertData(sql);

	}

	/**
	 * Inserts the rupture ids that correspond to a given site within given cutoff distance
	 * inside the table CyberShake_Site_Ruptures
	 * @param siteId
	 * @param erfId
	 * @param sourceId
	 * @param ruptureId
	 * @param cutoffDistance
	 */
	public void insertSite_RuptureInfo(int siteId, int erfId, int sourceId,
			int ruptureId, double cutOffDistance) {
//		generate the SQL to be inserted in the Sites table
		String sql = "INSERT into CyberShake_Site_Ruptures VALUES('"+siteId+"','"+erfId+"','"+
		sourceId+"','"+ruptureId+"','"+cutOffDistance+"')";
		dbaccess.insertData(sql);

	}

}

package org.opensha.cybershake.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SiteInfo2DB implements SiteInfo2DBAPI {

	private DBAccess dbaccess;

	
	public SiteInfo2DB(DBAccess dbaccess){
		this.dbaccess = dbaccess;
	}
	
	
	/**
	 * Inserts the new site in the database table Sites
	 * @param siteName
	 * @param siteShortName
	 * @param lat
	 * @param lon
	 * @returns the Site Id for the last inserted Site
	 */
	public int insertSite(String siteName, String siteShortName, double lat,
			double lon) {
//		generate the SQL to be inserted in the Sites table
		String sql = "INSERT into Sites VALUES('"+siteName+"','"+siteShortName+"','"+
		lat+"','"+lon+"')";
		dbaccess.insertData(sql);
		
//		 gets the last auto increment id from Sites table
		 sql = "SELECT LAST_INSERT_ID() from Sites ";
		 ResultSet rs =  dbaccess.selectData(sql);
			try {
				return rs.getInt(0);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 return -1;
		
		
	}

	/**
	 * Inserts the regional bounds (min/max lat/lon) for all cybershake sites in table Site_Region
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
		String sql = "INSERT into Site_Region VALUES('"+siteId+"','"+erfId+"','"+
		cutOffDistance+"','"+maxLat+"','"+maxLatSrcId+"','"+maxLatRupId+"','"+maxLon+"','"+
		maxLatSrcId+"','"+maxLonRupId+"','"+minLat+"','"+minLatSrcId+"','"+minLatRupId+"','"+
		minLon+"','"+minLonSrcId+"','"+minLonRupId+"')";
		dbaccess.insertData(sql);

	}

	/**
	 * Inserts the rupture ids that correspond to a given site within given cutoff distance
	 * inside the table Site_Ruptures
	 * @param siteId
	 * @param erfId
	 * @param sourceId
	 * @param ruptureId
	 * @param cutoffDistance
	 */
	public void insertSite_RuptureInfo(int siteId, int erfId, int sourceId,
			int ruptureId, double cutOffDistance) {
//		generate the SQL to be inserted in the Sites table
		String sql = "INSERT into Site_Rptures VALUES('"+siteId+"','"+erfId+"','"+
		sourceId+"','"+ruptureId+"','"+cutOffDistance+"')";
		dbaccess.insertData(sql);

	}

}

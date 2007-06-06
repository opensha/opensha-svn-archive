package org.opensha.cybershake.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.LocationList;

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

	/**
	 * 
	 * @returns the list of all cybershake site locations
	 */
	public LocationList getAllSitesLocation() {
//		 gets the last auto increment id from Sites table
		 String sql = "SELECT CS_Site_Lat,CS_Site_Lon from CyberShake_Sites";
		 LocationList siteLocationList = new LocationList();
		 ResultSet rs =  dbaccess.selectData(sql);
			try {
				rs.first();
				while(!rs.isAfterLast()){
				  double lat = Double.parseDouble(rs.getString(0));	
				  double lon = Double.parseDouble(rs.getString(1));
				  Location loc = new Location(lat,lon);
				  siteLocationList.addLocation(loc);
				  rs.next();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		 return siteLocationList;
   }


	/**
	 * 
	 * @returns the ArrayList of short site names for all Cybershake
	 */
	public ArrayList getAllSites() {	
//		 gets the last auto increment id from Sites table
		 String sql = "SELECT CS_Short_Name from CyberShake_Sites";
		 ArrayList<String> siteList = new ArrayList<String>();
		 ResultSet rs =  dbaccess.selectData(sql);
			try {
				rs.first();
				while(!rs.isAfterLast()){
				  siteList.add(rs.getString(0));
				  rs.next();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		 return siteList;	
     }


	/**
	 * 
	 * @param siteShortName
	 * @param srcId
	 * @returns the list of rupture ids 
	 */
	public ArrayList<Integer> getRupIdsForSite(String siteShortName, int srcId) {
		// TODO Auto-generated method stub
		int siteId = this.getSiteId(siteShortName);
		String sql = "Select Rupture_ID from CyberShake_Site_Ruptures where CS_Site_ID = "+"'"+siteId+"'"+
		             " and Source_ID ='"+srcId+"' order by Rupture_ID asc";
		ArrayList<Integer> rupList = new ArrayList<Integer>();
		ResultSet rs =  dbaccess.selectData(sql);
		try {
			rs.first();
			while(!rs.isAfterLast()){
			  int rupId = Integer.parseInt(rs.getString(0));	
			  rupList.add(rupId);
			  rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rupList;
	}

	/**
	 * 
	 * @param siteShortName short site name as in database for Cybershake site
	 * @returns the Earthquake rupture forecast source id's for a given cybershake site.
	 */
	public ArrayList<Integer> getSrcIdsForSite(String siteShortName) {
		// TODO Auto-generated method stub
		int siteId = this.getSiteId(siteShortName);
		String sql = "Select Source_ID from CyberShake_Site_Ruptures where CS_Site_ID = "+"'"+siteId+"'"+
		             " order by Source_ID asc";
		ArrayList<Integer> srcIdList = new ArrayList<Integer>();
		ResultSet rs =  dbaccess.selectData(sql);
		try {
			rs.first();
			while(!rs.isAfterLast()){
			  int srcId = Integer.parseInt(rs.getString(0));	
			  srcIdList.add(srcId);
			  rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return srcIdList;
	}

	/**
	 * 
	 * @param site
	 * @returns the Location for the given cybershake site location
	 */
	public Location getLocationForSite(String site){
		 String sql = "SELECT CS_Site_Lat,CS_Site_Lon from CyberShake_Sites WHERE CS_Short_Name = '"+site+"'";
		 Location loc = null;
		 ResultSet rs =  dbaccess.selectData(sql);
			try {
				rs.first();
				double lat = Double.parseDouble(rs.getString(0));	
				double lon = Double.parseDouble(rs.getString(1));
				loc = new Location(lat,lon);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		 return loc;
	}
	
	
}

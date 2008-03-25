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
		try {
			dbaccess.insertUpdateOrDeleteData(sql);
		} catch (SQLException e) {
			if (e.getMessage().contains("Duplicate")) {
				System.out.println("Duplicate");
			} else {
				e.printStackTrace();
			}
		}		
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
		 ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			try {
				rs.first();
				String siteID = rs.getString("CS_Site_ID");
				rs.close();;
				return Integer.parseInt(siteID);
				
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
		 ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			try {
				rs.first();
				String siteID = rs.getString("CS_Site_ID");
				rs.close();
				return Integer.parseInt(siteID);
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
		System.out.println(sql);
		try {
			dbaccess.insertUpdateOrDeleteData(sql);
		} catch (SQLException e) {
			if (e.getMessage().contains("Duplicate")) {
				System.out.println("Duplicate");
			} else {
				e.printStackTrace();
			}
		}
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
		//System.out.println(sql);
		try {
			dbaccess.insertUpdateOrDeleteData(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			if (e.getMessage().contains("Duplicate")) {
				System.out.println("Duplicate");
			} else {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	/**
	 * Find out if the given rupture is already in the database
	 * @param erfID
	 * @param sourceID
	 * @param rupID
	 * @return
	 */
	public boolean isRupInDB(int erfID, int sourceID, int rupID) {
		long start = 0;
		if (Cybershake_OpenSHA_DBApplication.timer) {
			start = System.currentTimeMillis();
		}
		String sql = "SELECT * FROM Ruptures WHERE ERF_ID="+erfID+" and Source_ID="+sourceID+" and Rupture_ID="+rupID;
		try {
			ResultSet rs = dbaccess.selectData(sql);
			if (Cybershake_OpenSHA_DBApplication.timer) {
				System.out.println("Got the select result at " + (System.currentTimeMillis() - start) + " milliseconds");
			}
			boolean result = rs.next();
			if (Cybershake_OpenSHA_DBApplication.timer) {
				long total = (System.currentTimeMillis() - start);
				System.out.println("Took " + total + " miliseconds to check if the rupture exists!!");
			}
		    return result;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (Cybershake_OpenSHA_DBApplication.timer) {
			long total = (System.currentTimeMillis() - start);
			System.out.println("Took " + total + " miliseconds to check if the rupture exists!!");
		}
		return false;
	}
	
	/**
	 * Find out if the given rupture is already in the database
	 * @param erfID
	 * @param sourceID
	 * @param rupID
	 * @param siteID
	 * @return
	 */
	public boolean isSiteRupInDB(int erfID, int sourceID, int rupID, int siteID) {
		long start = 0;
		if (Cybershake_OpenSHA_DBApplication.timer) {
			start = System.currentTimeMillis();
		}
		String sql = "SELECT * FROM CyberShake_Site_Ruptures WHERE ERF_ID="+erfID+" and Source_ID="+sourceID+" and Rupture_ID="+rupID+" and CS_Site_ID="+siteID;
		try {
			ResultSet rs = dbaccess.selectData(sql);
			if (Cybershake_OpenSHA_DBApplication.timer) {
				System.out.println("Got the select result at " + (System.currentTimeMillis() - start) + " milliseconds");
			}
			boolean result = rs.next();
			if (Cybershake_OpenSHA_DBApplication.timer) {
				long total = (System.currentTimeMillis() - start);
				System.out.println("Took " + total + " miliseconds to check if the rupture exists!!");
			}
		    return result;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (Cybershake_OpenSHA_DBApplication.timer) {
			long total = (System.currentTimeMillis() - start);
			System.out.println("Took " + total + " miliseconds to check if the rupture exists!!");
		}
		return false;
	}

	/**
	 * 
	 * @returns the list of all cybershake site locations
	 */
	public LocationList getAllSitesLocation() {
//		 gets the last auto increment id from Sites table
		 String sql = "SELECT CS_Site_Lat,CS_Site_Lon from CyberShake_Sites";
		 LocationList siteLocationList = new LocationList();
		 ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			try {
				rs.first();
				while(!rs.isAfterLast()){
				  double lat = Double.parseDouble(rs.getString("CS_Site_Lat"));	
				  double lon = Double.parseDouble(rs.getString("CS_Site_Lon"));
				  Location loc = new Location(lat,lon);
				  siteLocationList.addLocation(loc);
				  rs.next();
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		 return siteLocationList;
   }


	/**
	 * 
	 * @returns the ArrayList of short site names for all Cybershake
	 */
	public ArrayList<String> getAllSites() {	
//		 gets the last auto increment id from Sites table
		 String sql = "SELECT CS_Short_Name from CyberShake_Sites";
		 ArrayList<String> siteList = new ArrayList<String>();
		 ResultSet rs =null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			try {
				rs.first();
				while(!rs.isAfterLast()){
				  siteList.add(rs.getString("CS_Short_Name"));
				  rs.next();
				}
				rs.close();
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
		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			rs.first();
			while(!rs.isAfterLast()){
			  int rupId = Integer.parseInt(rs.getString("Rupture_ID"));	
			  rupList.add(rupId);
			  rs.next();
			}
			rs.close();
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
		             " group by Source_ID order by Source_ID asc";
		ArrayList<Integer> srcIdList = new ArrayList<Integer>();
		ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			rs.first();
			while(!rs.isAfterLast()){
			  int srcId = Integer.parseInt(rs.getString("Source_ID"));	
			  srcIdList.add(srcId);
			  rs.next();
			}
			rs.close();
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
		 ResultSet rs = null;
		try {
			rs = dbaccess.selectData(sql);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			try {
				rs.first();
				double lat = Double.parseDouble(rs.getString("CS_Site_Lat"));	
				double lon = Double.parseDouble(rs.getString("CS_Site_Lon"));
				loc = new Location(lat,lon);
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		 return loc;
	}
	
	/**
	 * tester main function
	 * @param args
	 */
	public static void main(String args[]) {
		DBAccess db = new DBAccess("intensity.usc.edu","CyberShake");
		SiteInfo2DB siteDB = new SiteInfo2DB(db);
		siteDB.isRupInDB(33, 0, 9);
		System.out.println("DONE!");
		db.destroy();
	}
}
package org.opensha.cybershake.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ERF2DB implements ERF2DBAPI{
	
	
	private DBAccess dbaccess;

	
	public ERF2DB(DBAccess dbaccess){
		this.dbaccess = dbaccess;
	}
	
	/**
	 * Inserts ERF Parameters info in the "ERF_Metadata"
	 * @param erfId
	 * @param attrName
	 * @param attrVal
	 */
	public void insertERFParams(int erfId, String attrName, String attrVal, String attrType,String attrUnits) {
		
		//generate the SQL to be inserted in the ERF_Metadata table
		String sql = "INSERT into ERF_Metadata VALUES('"+erfId+"','"+attrName+"','"+
		             attrVal+"','"+attrType+"','"+attrUnits+"')";
		dbaccess.insertData(sql);
		
	}

	
	/**
	 * Inserts source rupture information for the ERF in table "Rupture"
	 * @param erfName
	 * @param sourceId
	 * @param ruptureId
	 * @param sourceName
	 * @param sourcetype
	 * @param magnitude
	 * @param probability
	 * @param gridSpacing
	 * @param numRows
	 * @param numCols
	 * @param numPoints
	 */
	public void insertERFRuptureInfo(int erfId, int sourceId, int ruptureId, 
			                        String sourceName, String sourceType, double magnitude, 
			                        double probability, double gridSpacing, double surfaceStartLat, 
			                        double surfaceStartLon, double surfaceStartDepth,
			                        double surfaceEndLat, double surfaceEndLon,double surfaceEndDepth, 
			                        int numRows, int numCols, int numPoints) {
//		generate the SQL to be inserted in the ERF_Metadata table
		String sql = "INSERT into Rupture VALUES('"+erfId+"','"+sourceId+"','"+
		             ruptureId+"','"+sourceName+"','"+sourceType+"','"+magnitude+"','"+
		             probability+"','"+gridSpacing+"','"+numRows+"','"+numCols+
		             "','"+numPoints+"','"+surfaceStartLat+"','"+surfaceStartLon+"','"+surfaceStartDepth+
		             "','"+surfaceEndLat+"','"+surfaceEndLon+"','"+surfaceEndDepth+"')";
		dbaccess.insertData(sql);
		
	}

	/**
	 * Inserts surface locations information for each rupture in table "Points"
	 * @param erfName
	 * @param sourceId
	 * @param ruptureId
	 * @param lat
	 * @param lon
	 * @param depth
	 * @param rake
	 * @param dip
	 * @param strike
	 */
	public void insertRuptureSurface(int erfId, int sourceId, int ruptureId, 
			                         double lat, double lon, double depth, double rake, 
			                         double dip, double strike) {
//		generate the SQL to be inserted in the ERF_Metadata table
		String sql = "INSERT into Points VALUES('"+erfId+"','"+sourceId+"','"+
		             ruptureId+"','"+lat+"','"+lon+"','"+depth+"','"+
		             rake+"','"+dip+"','"+strike+"')";
		dbaccess.insertData(sql);
		
	}

	/**
	 * 
	 * Inserts ERF name and description in table ERF_IDs
	 * @param erfName
	 * @param erfDesc
	 * @return Autoincremented Id from the table for the last inserted ERF
	 */
	public int insertERFId(String erfName, String erfDesc) {
//		generate the SQL to be inserted in the ERF_Metadata table
		String sql = "INSERT into ERF_IDs VALUES('"+erfName+"','"+erfDesc+"')";
		dbaccess.insertData(sql);
		
//		 TODO Auto-generated method stub
		 sql = "SELECT LAST_INSERT_ID() from ERF_IDs ";
		 ResultSet rs =  dbaccess.selectData(sql);
			try {
				return rs.getInt(0);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 return -1;
		
	}

}

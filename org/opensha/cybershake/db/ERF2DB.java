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
		String sql = "INSERT into ERF_Metadata" +
		    "(ERF_ID,ERF_Attr_Name,ERF_Attr_Value,ERF_Attr_Type,ERF_Attr_Units)"+
			"VALUES('"+erfId+"','"+attrName+"','"+
		             attrVal+"','"+attrType+"','"+attrUnits+"')";
		dbaccess.insertData(sql);
		
	}

	
	/**
	 * Inserts source rupture information for the ERF in table "Ruptures"
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
		String sql = "INSERT into Ruptures" +
		             "(ERF_ID,Source_ID,Rupture_ID,Source_Name,Source_Type,Mag,Prob,"+
		             "Grid_Spacing,Num_Rows,Num_Columns,Num_Points,Start_Lat,Start_Lon,"+
		             "Start_Depth,End_Lat,End_Lon,End_Depth)"+
		            "VALUES('"+erfId+"','"+sourceId+"','"+
		             ruptureId+"','"+sourceName+"','"+sourceType+"','"+(float)magnitude+"','"+
		             (float)probability+"','"+(float)gridSpacing+"','"+numRows+"','"+numCols+
		             "','"+numPoints+"','"+(float)surfaceStartLat+"','"+(float)surfaceStartLon+"','"+(float)surfaceStartDepth+
		             "','"+(float)surfaceEndLat+"','"+(float)surfaceEndLon+"','"+(float)surfaceEndDepth+"')";
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
		String sql = "INSERT into Points"+ 
		             "(ERF_ID,Source_ID,Rupture_ID,Lat,Lon,Depth,Rake,Dip,Strike)"+
		            "VALUES('"+erfId+"','"+sourceId+"','"+
		             ruptureId+"','"+(float)lat+"','"+(float)lon+"','"+(float)depth+"','"+
		             (float)rake+"','"+(float)dip+"','"+(float)strike+"')";
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
		String sql = "INSERT into ERF_IDs"+ 
		             "(ERF_Name,ERF_Description)"+
		              "VALUES('"+erfName+"','"+erfDesc+"')";
		dbaccess.insertData(sql);
		
		return getInserted_ERF_ID(erfName);
		
	}

	/**
	 * Retrives the id of the ERF from the table ERF_IDs  for the corresponding ERF_Name.
	 * @param erfName
	 * @return
	 */
	public int getInserted_ERF_ID(String erfName){
		 String sql = "SELECT ERF_ID from ERF_IDs WHERE ERF_Name = "+"'"+erfName+"'";
		 ResultSet rs =  dbaccess.selectData(sql);		 
			try {
				rs.first();
				String erfId = rs.getString("ERF_ID");
				return Integer.parseInt(erfId);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		 return -1;
	}

	/**
	 * Retrives the rupture probability
	 * @param erfId
	 * @param sourceId
	 * @param rupId
	 * @return
	 */
	public double getRuptureProb(int erfId,int sourceId,int rupId){
		String sql = "SELECT Prob from Ruptures WHERE ERF_ID = "+"'"+erfId+"' and "+
		             "Source_ID = '"+sourceId+"' and Rupture_ID = '"+rupId+"'";
		ResultSet rs =  dbaccess.selectData(sql);
		double rupProb = Double.NaN;
		try{
			rs.first();
			rupProb = Double.parseDouble(rs.getString(0));
			
		}catch (SQLException e) {
			e.printStackTrace();
		}
	 return rupProb;
	}
	
}

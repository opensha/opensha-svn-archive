package org.opensha.cybershake.db;

public interface ERF2DBAPI {
	
	
	/**
	 * Inserts the ERF parameters info in the table "ERF_Metadata"
	 * @param erfName
	 * @param attrName
	 * @param attrVal
	 */
	public void insertERFParams(int erfId,String attrName, String attrVal, String attrType,String attrUnits);
	
	/**
	 * 
	 * Inserts ERF name and description in table ERF_IDs
	 * @param erfName
	 * @param erfDesc
	 * @return
	 */
	public void insertERFId(String erfName, String erfDesc);
	
	/**
	 * Returns the ERF_Id from the database table ERF_IDs for provided erfName
	 * @param erfName
	 * @return
	 */
	public int getERFId(String erfName);
	
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
	public void insertERFRuptureInfo(int erfId ,int sourceId,int ruptureId,
			                        String sourceName,String sourcetype,double magnitude,
			                        double probability,double gridSpacing,
			                        double surfaceStartLat,double surfaceStartLon,double surfaceStartDepth,
			                        double surfaceEndLat,double surfaceEndLon,double surfaceEndDepth,
			                        int numRows,int numCols,int numPoints);
	
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
	public void insertRuptureSurface(int erfId,int sourceId,int ruptureId,
			                         double lat,double lon,double depth,double rake,
			                         double dip,double strike);
	
	

}

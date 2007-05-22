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
	public int insertERFId(String erfName, String erfDesc);
	

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
	
	/**
	 * Retrives the id of the ERF from the table ERF_IDs  for the corresponding ERF_Name.
	 * @param erfName
	 * @return
	 */
	public int getInserted_ERF_ID(String erfName);

}

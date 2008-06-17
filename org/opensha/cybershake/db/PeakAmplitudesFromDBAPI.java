package org.opensha.cybershake.db;

import java.sql.SQLException;
import java.util.ArrayList;

public interface PeakAmplitudesFromDBAPI {

	/**
	 * @returns the supported SA Period as list of strings.
	 */
	public ArrayList<String>  getSupportedSA_PeriodList();
	
	/**
	 * @returns the supported SA Period as list of strings.
	 */
	public ArrayList<String>  getSupportedSA_PeriodList(int siteID, int erfID, int sgtVariation, int rupVarID);
	
	/**
	 * 
	 * @param erfId
	 * @param srcId
	 * @param rupId
	 * @returns the rupture variation ids for the rupture
	 */
	public ArrayList<Integer> getRupVarationsForRupture(int erfId,int srcId, int rupId);
	
	/**
	 * 
	 * @param siteId
	 * @param erfId
	 * @param srcId
	 * @param rupId
	 * @param rupVarId
	 * @returns the IM Value for the particular IM type
	 */
	public double getIM_Value(int siteId,int erfId,int sgtVariation, int rvid, int srcId,int rupId,int rupVarId, String imType);
	
	/**
	 * 
	 * @param siteId
	 * @param erfId
	 * @param srcId
	 * @param rupId
	 * @throws SQLException 
	 * @returns the a list of IM Values for the particular IM type
	 */
	public ArrayList<Double> getIM_Values(int siteId,int erfId,int sgtVariation, int rvid, int srcId,int rupId, String imType) throws SQLException;
	
	/**
	  * @return all possible SGT Variation IDs
	  */
	public ArrayList<Integer> getSGTVarIDs();
	
	/**
	 * @return all possible Rup Var Scenario IDs
	 */
	public ArrayList<Integer> getRupVarScenarioIDs();
}

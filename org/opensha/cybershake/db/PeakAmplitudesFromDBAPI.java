package org.opensha.cybershake.db;

import java.sql.SQLException;
import java.util.ArrayList;

public interface PeakAmplitudesFromDBAPI {

	/**
	 * @returns the supported SA Period as list of strings.
	 */
	public ArrayList<CybershakeIM>  getSupportedIMs();
	
	/**
	 * @returns the supported SA Period as list of strings.
	 */
	public ArrayList<CybershakeIM>  getSupportedIMs(int runID);
	
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
	public double getIM_Value(int runID, int srcId,int rupId,int rupVarId, CybershakeIM im);
	
	/**
	 * 
	 * @param siteId
	 * @param erfId
	 * @param srcId
	 * @param rupId
	 * @throws SQLException 
	 * @returns the a list of IM Values for the particular IM type
	 */
	public ArrayList<Double> getIM_Values(int runID, int srcId,int rupId, CybershakeIM im) throws SQLException;
	
	/**
	  * @return all possible SGT Variation IDs
	  */
	public ArrayList<Integer> getSGTVarIDs();
	
	/**
	 * @return all possible Rup Var Scenario IDs
	 */
	public ArrayList<Integer> getRupVarScenarioIDs();
	
	/**
	 * delete all peak amplitudes for a given site
	 * @param siteId
	 * @return
	 */
	public int deleteAllAmpsForSite(int siteID);
	
	/**
	 * delete all peak amplitudes for a given run
	 * @param siteId
	 * @return
	 */
	public int deleteAmpsForRun(int runID);
}

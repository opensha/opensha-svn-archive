package org.opensha.cybershake.db;

import java.util.ArrayList;

public interface PeakAmplitudesFromDBAPI {

	/**
	 * @returns the supported SA Period as list of strings.
	 */
	public ArrayList<String>  getSupportedSA_PeriodList();
	
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
	
	
}

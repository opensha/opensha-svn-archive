package scratchJavaDevelopers.interns;

import org.opensha.refFaultParamDb.vo.EstimateInstances;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;

import java.util.ArrayList;

/**
 * This  is the interface to access Fault Section Data. This interface is currently implemented  so as to get
 * Fault section ver 1 data(USGS data) and Fault section ver 2 data.
 * 
 * @author vipingupta
 *
 */
public interface FaultSectionSurfaces {
	
	/**
	 * Get Frankel representation of a gridded surface for specified fault section Id
	 * 
	 * @param faultSectionId
	 * @return
	 */
	public EvenlyGriddedSurfaceAPI getFrankelSurface(int faultSectionId);
	
	/**
	 *  Get Stirling's representation of a gridded surface for a specified fault section Id
	 * @param faultSectionId
	 * @return
	 */
	public EvenlyGriddedSurfaceAPI getStirlingSurface(int faultSectionId);
	
	/**
	 * Get a list of all fault section Id and their corresponding names.
	 * It returns a list of FaultSectionSummary objects
	 * @return
	 */
	public ArrayList getAllFaultSectionsSummary();
	
	/**
	 * This function allows the user to refresh the fault section data for this Id from the database.
	 *  This is used for DB Ver 2 only because ver 1 data is  loaded from text files only.
	 *  
	 * @param faultSectionId
	 */
	public void reloadFaultSectionFromDatabase(int faultSectionId);
	
	/**
	 * Refresh all the fault sections which are currently in cache
	 *
	 */
	public void reloadAllFaultSectionsFromDatabase();
	
	/**
	 * Get the Minimum value for slip rate 
	 * @return
	 */
	public double getMinSlipRate() ;
	
	/**
	 * Get the maximum value for slip rate
	 * @return
	 */
	public double getMaxSlipRate();
	
	/**
	 * Get the slip rate for a fault section Id
	 * @param faultSectionId
	 * @return
	 */
	public double getSlipRate(int faultSectionId) ;


}

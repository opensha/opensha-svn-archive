package javaDevelopers.interns;

import org.opensha.sha.surface.EvenlyGriddedSurfaceAPI;
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

}

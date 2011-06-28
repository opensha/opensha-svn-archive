/**
 * 
 */
package scratch.UCERF3;

import java.util.ArrayList;

import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;


/**
 * This interface represents the attributes of ruptures in a fault system, 
 * where the latter is composed of some number of fault sections.
 * 
 * @author Field, Milner, Page, & Powers
 *
 */
public interface FaultSystemRupSet {
	
	/**
	 * The total number of ruptures in the fault system
	 * @return
	 */
	public int getNumRupRuptures();
	
	/**
	 * The total number of fault sections in the fault system
	 * @return
	 */
	public int getNumSections();
	
	/**
	 * This returns which sections are used by the rth rupture
	 * @param rupIndex
	 * @return
	 */
	public ArrayList<Integer> getSectionsIndicesForRup(int rupIndex);

	/**
	 * This gives the magnitude for each rth rupture
	 * @return
	 */
	public double[] getMagForAllRups();

	/**
	 * This gives the magnitude for the rth rupture
	 * @param rupIndex
	 * @return
	 */
	public double getMagForRup(int rupIndex);

	/**
	 * This gives the average slip (meters) for all ruptures
	 * @return
	 */
	public double[] getAveSlipForAllRups();
	
	/**
	 * This gives the average slip (meters) for the rth rupture
	 * @return
	 */
	public double getAveSlipForRup(int rupIndex);
	
	/**
	 * This gives the average slip on each section for all ruptures
	 * @return
	 */
	public ArrayList<double[]> getSlipOnSectionsForAllRups();
	
	/**
	 * This gives the average slip on each section for the rth rupture
	 * @return
	 */
	public double[] getSlipOnSectionsForRup(int rthRup);
	
	/**
	 * This gives the average rake for the rth rupture
	 * @param rupIndex
	 * @return
	 */
	public double getAveRakeForRup(int rupIndex);
	
	/**
	 * @param rupIndex
	 * @return Area (SI units: sq-m)
	 */
	public double getAreaForRup(int rupIndex);
	
	/**
	 * @param sectIndex
	 * @return Area (SI units: sq-m)
	 */
	public double getAreaForSection(int sectIndex);
	
	/**
	 * This returns an array of all fault-section data
	 * @return
	 */
	public ArrayList<FaultSectionPrefData> getFaultSectionList();
	
	/**
	 * The returns the fault-section data for the sth section
	 * @param sectIndex
	 * @return
	 */
	public FaultSectionPrefData getFaultSectionData(int sectIndex);
	
	/**
	 * This differs from what is returned by getFaultSectionData(int).getAveLongTermSlipRate()
	 * where there has been a modification (i.e., moment rate reductions for smaller events).
	 * @return
	 */
	public double getSlipRateForSection(int sectIndex);
	
	/**
	 * This differs from what is returned by getFaultSectionData(int).getAveLongTermSlipRate()
	 * where there has been a modification (i.e., moment rate reductions for smaller events).
	 * @return
	 */
	public double[] getSlipRateForAllSections();

	/**
	 * This is a general info String
	 * @return
	 */
	public String getInfoString();
	
}

/**
 * 
 */
package scratch.UCERF3;

import java.util.List;

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
	public int getNumRuptures();
	
	/**
	 * The total number of ruptures in the fault system
	 * @return
	 */
	public int getNumSections();
	
	/**
	 * This returns which sections are used by the each rupture
	 * @param rupIndex
	 * @return
	 */
	public List<List<Integer>> getSectionIndicesForAllRups();
	
	/**
	 * This returns which sections are used by the rth rupture
	 * @param rupIndex
	 * @return
	 */
	public List<Integer> getSectionsIndicesForRup(int rupIndex);
	
	/**
	 * This returns the a list of all ruptures that occur on each section
	 * @param secIndex
	 * @return
	 */
	public List<Integer> getRupturesForSection(int secIndex);

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
	public List<double[]> getSlipOnSectionsForAllRups();
	
	/**
	 * This gives the average slip on each section for the rth rupture
	 * @return
	 */
	public double[] getSlipOnSectionsForRup(int rthRup);
	
	/**
	 * This gives the average rake for all ruptures
	 * @return
	 */
	public double[] getAveRakeForAllRups();
	
	/**
	 * This gives the average rake for the rth rupture
	 * @param rupIndex
	 * @return
	 */
	public double getAveRakeForRup(int rupIndex);
	
	/**
	 * @return Area (SI units: sq-m)
	 */
	public double[] getAreaForAllRups();
	
	/**
	 * @param rupIndex
	 * @return Area (SI units: sq-m)
	 */
	public double getAreaForRup(int rupIndex);
	
	/**
	 * @return Area (SI units: sq-m)
	 */
	public double[] getAreaForAllSections();
	
	/**
	 * @param sectIndex
	 * @return Area (SI units: sq-m)
	 */
	public double getAreaForSection(int sectIndex);
	
	/**
	 * This returns a list of all fault-section data
	 * @return
	 */
	public List<FaultSectionPrefData> getFaultSectionDataList();
	
	/**
	 * The returns the fault-section data for the sth section
	 * @param sectIndex
	 * @return
	 */
	public FaultSectionPrefData getFaultSectionData(int sectIndex);
	
	/**
	 * This returns the fault-section data list for the given rupture
	 * @param rupIndex
	 * @return
	 */
	public List<FaultSectionPrefData> getFaultSectionDataForRupture(int rupIndex);
	
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
	 * This differs from what is returned by getFaultSectionData(int).getSlipRateStdDev()
	 * where there has been a modification (i.e., moment rate reductions for smaller events).
	 * @return
	 */
	public double getSlipRateStdDevForSection(int sectIndex);
	
	/**
	 * This differs from what is returned by getFaultSectionData(int).getSlipRateStdDev()
	 * where there has been a modification (i.e., moment rate reductions for smaller events).
	 * @return
	 */
	public double[] getSlipRateStdDevForAllSections();

	/**
	 * This is a general info String
	 * @return
	 */
	public String getInfoString();
	
	/**
	 * This fetches a list of all of the close sections to this section, as defined by the rupture set.
	 * @param sectIndex index of the section to retrieve
	 * @return close sections, or null if not defined
	 */
	public List<Integer> getCloseSectionsList(int sectIndex);
	
	/**
	 * This returns a list of lists of close sections for each section.
	 * @return list of all close sections, or null if not defined
	 */
	public List<List<Integer>> getCloseSectionsListList();
	
	/*		CLUSTER RELATED METHODS		*/
	
	/**
	 * 
	 * @return the number of clusters, or 0 if not a cluster based model
	 */
	public int getNumClusters();
	
	/**
	 * 
	 * @return true if the rup set is cluster based, false otherwise
	 */
	public boolean isClusterBased();
	
	/**
	 * 
	 * @param index index of the cluster to get
	 * @return number of ruptures in the given cluster
	 */
	public int getNumRupturesForCluster(int index);
	
	/**
	 * 
	 * @param index index of the cluster to get
	 * @return list of section IDs in the cluster at the given index
	 */
	public List<Integer> getSectionsForCluster(int index);
	
	/**
	 * 
	 * @param index index of the cluster to get
	 * @return list of rupture indexes for the cluster at the given index
	 * @throws IndexOutOfBoundsException if the index is invalid
	 */
	public List<Integer> getRupturesForCluster(int index) throws IndexOutOfBoundsException;
	
}

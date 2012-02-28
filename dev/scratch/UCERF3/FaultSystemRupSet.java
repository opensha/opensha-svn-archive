/**
 * 
 */
package scratch.UCERF3;

import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.geo.Region;
import org.opensha.commons.geo.RegionUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.utils.MFD_InversionConstraint;


/**
 * This abstract class represents the attributes of ruptures in a fault system, 
 * where the latter is composed of some number of fault sections.
 * 
 * @author Field, Milner, Page, & Powers
 *
 */
public abstract class FaultSystemRupSet {
	
	// for caching
	protected boolean showProgress = false;
	
	/**
	 * This enables/disables visible progress bars for long calculations
	 * 
	 * @param showProgress
	 */
	public void setShowProgress(boolean showProgress) {
		this.showProgress = showProgress;
	}
	
	public void clearCache() {
		rupturesForSectionCache.clear();
	}
	
	/**
	 * The total number of ruptures in the fault system
	 * @return
	 */
	public abstract int getNumRuptures();
	
	/**
	 * The total number of ruptures in the fault system
	 * @return
	 */
	public abstract int getNumSections();
	
	/**
	 * This returns which sections are used by the each rupture
	 * @param rupIndex
	 * @return
	 */
	public abstract List<List<Integer>> getSectionIndicesForAllRups();
	
	/**
	 * This returns which sections are used by the rth rupture
	 * @param rupIndex
	 * @return
	 */
	public abstract List<Integer> getSectionsIndicesForRup(int rupIndex);

	/**
	 * This gives the magnitude for each rth rupture
	 * @return
	 */
	public abstract double[] getMagForAllRups();

	/**
	 * This gives the magnitude for the rth rupture
	 * @param rupIndex
	 * @return
	 */
	public abstract double getMagForRup(int rupIndex);

	/**
	 * This gives the average slip (meters) for all ruptures
	 * @return
	 */
	public abstract double[] getAveSlipForAllRups();
	
	/**
	 * This gives the average slip (meters) for the rth rupture
	 * @return
	 */
	public abstract double getAveSlipForRup(int rupIndex);
	
	/**
	 * This gives the average slip (SI untis: m) on each section for all ruptures
	 * @return
	 */
	public abstract List<double[]> getSlipOnSectionsForAllRups();
	
	/**
	 * This gives the average slip (SI untis: m) on each section for the rth rupture
	 * @return
	 */
	public abstract double[] getSlipOnSectionsForRup(int rthRup);
	
	/**
	 * This gives the average rake for all ruptures
	 * @return
	 */
	public abstract double[] getAveRakeForAllRups();
	
	/**
	 * This gives the average rake for the rth rupture
	 * @param rupIndex
	 * @return
	 */
	public abstract double getAveRakeForRup(int rupIndex);
	
	/**
	 * @return Area (SI units: sq-m)
	 */
	public abstract double[] getAreaForAllRups();
	
	/**
	 * @param rupIndex
	 * @return Area (SI units: sq-m)
	 */
	public abstract double getAreaForRup(int rupIndex);
	
	/**
	 * @return Area (SI units: sq-m)
	 */
	public abstract double[] getAreaForAllSections();
	
	/**
	 * @param sectIndex
	 * @return Area (SI units: sq-m)
	 */
	public abstract double getAreaForSection(int sectIndex);
	
	/**
	 * This returns a list of all fault-section data
	 * @return
	 */
	public abstract List<FaultSectionPrefData> getFaultSectionDataList();
	
	/**
	 * The returns the fault-section data for the sth section
	 * @param sectIndex
	 * @return
	 */
	public abstract FaultSectionPrefData getFaultSectionData(int sectIndex);
	
	/**
	 * This returns the fault-section data list for the given rupture
	 * @param rupIndex
	 * @return
	 */
	public abstract List<FaultSectionPrefData> getFaultSectionDataForRupture(int rupIndex);
	
	/**
	 * This differs from what is returned by getFaultSectionData(int).getAveLongTermSlipRate()
	 * where there has been a modification (i.e., moment rate reductions for smaller events).
	 * @return
	 */
	public abstract double getSlipRateForSection(int sectIndex);
	
	/**
	 * This differs from what is returned by getFaultSectionData(int).getAveLongTermSlipRate()
	 * where there has been a modification (i.e., moment rate reductions for smaller events).
	 * @return
	 */
	public abstract double[] getSlipRateForAllSections();
	
	/**
	 * This differs from what is returned by getFaultSectionData(int).getSlipRateStdDev()
	 * where there has been a modification (i.e., moment rate reductions for smaller events).
	 * @return
	 */
	public abstract double getSlipRateStdDevForSection(int sectIndex);
	
	/**
	 * This differs from what is returned by getFaultSectionData(int).getSlipRateStdDev()
	 * where there has been a modification (i.e., moment rate reductions for smaller events).
	 * @return
	 */
	public abstract double[] getSlipRateStdDevForAllSections();

	/**
	 * This is a general info String
	 * @return
	 */
	public abstract String getInfoString();
	
	/**
	 * This fetches a list of all of the close sections to this section, as defined by the rupture set.
	 * @param sectIndex index of the section to retrieve
	 * @return close sections, or null if not defined
	 */
	public abstract List<Integer> getCloseSectionsList(int sectIndex);
	
	/**
	 * This returns a list of lists of close sections for each section.
	 * @return list of all close sections, or null if not defined
	 */
	public abstract List<List<Integer>> getCloseSectionsListList();
	
	/*		CLUSTER RELATED METHODS		*/
	
	/**
	 * 
	 * @return the number of clusters, or 0 if not a cluster based model
	 */
	public abstract int getNumClusters();
	
	/**
	 * 
	 * @return true if the rup set is cluster based, false otherwise
	 */
	public abstract boolean isClusterBased();
	
	/**
	 * 
	 * @param index index of the cluster to get
	 * @return number of ruptures in the given cluster
	 */
	public abstract int getNumRupturesForCluster(int index);
	
	/**
	 * 
	 * @param index index of the cluster to get
	 * @return list of section IDs in the cluster at the given index
	 */
	public abstract List<Integer> getSectionsForCluster(int index);
	
	/**
	 * 
	 * @param index index of the cluster to get
	 * @return list of rupture indexes for the cluster at the given index
	 * @throws IndexOutOfBoundsException if the index is invalid
	 */
	public abstract List<Integer> getRupturesForCluster(int index) throws IndexOutOfBoundsException;
	
	/**
	* This returns the deformation model
	* @return
	*/
	public abstract DeformationModels getDeformationModel();
	
	/**
	* This returns the fault model
	* @return
	*/
	public abstract FaultModels getFaultModel();
	
	private List<MFD_InversionConstraint> prev_mfdConstraints;
	protected double[][] fractRupsInsideMFD_Regions = null;
	
	/**
	 * This computes the fraction of each rupture inside each region in the given mfdConstraints, where results
	 * are stored in the fractRupsInsideMFD_Regions[iRegion][iRup] double array
	 * @param mfdConstraints
	 */
	public double[][] computeFractRupsInsideMFD_Regions(List<MFD_InversionConstraint> mfdConstraints) {
		if(fractRupsInsideMFD_Regions == null || mfdConstraints != prev_mfdConstraints) {
			// do only if not already done
			prev_mfdConstraints = mfdConstraints;
			
			System.out.println("Computing fraction rups in MDS regions ...");
			int numRuptures = getNumRuptures();
			fractRupsInsideMFD_Regions = new double[mfdConstraints.size()][numRuptures];
			double[][] fractSectionInsideMFD_Regions = new double[mfdConstraints.size()][getNumSections()];
			int[] numPtsInSection = new int[getNumSections()];
			double gridSpacing=1; // km; this will be faster if this is increased, or if we used the section trace rather than the whole surface
			// first fill in fraction of section in each region (do each only once)
			for(int s=0;s<getNumSections(); s++) {
				StirlingGriddedSurface surf = getFaultSectionData(s).getStirlingGriddedSurface(gridSpacing, false, true);
				numPtsInSection[s] = surf.getNumCols()*surf.getNumRows();
				for(int i=0;i<mfdConstraints.size(); i++) {
					Region region = mfdConstraints.get(i).getRegion();
					fractSectionInsideMFD_Regions[i][s] = RegionUtils.getFractionInside(region, surf.getEvenlyDiscritizedListOfLocsOnSurface());
				}
			}
			// now fill in fraction of rupture in each region
			for (int i=0; i < mfdConstraints.size(); i++) {  // Loop over all MFD constraints in different regions
				IncrementalMagFreqDist targetMagFreqDist=mfdConstraints.get(i).getMagFreqDist();	
				for(int rup=0; rup<numRuptures; rup++) {
					double mag = getMagForRup(rup);
					List<Integer> sectionsIndicesForRup = getSectionsIndicesForRup(rup);
					double fractionRupInRegion=0;
					int totNumPts = 0;
					for(Integer s:sectionsIndicesForRup) {
						fractRupsInsideMFD_Regions[i][rup] += fractSectionInsideMFD_Regions[i][s]*numPtsInSection[s];
						totNumPts += numPtsInSection[s];
					}
					fractRupsInsideMFD_Regions[i][rup] /= totNumPts;
				}
				System.out.println("Done with MFD constraint #"+i);
			}
		}
		return fractRupsInsideMFD_Regions;
	}
	
	/**
	 * this caches the ruptures involving each section
	 */
	private ArrayList<ArrayList<Integer>> rupturesForSectionCache = null;
	
	/**
	 * This returns the a list of all ruptures that occur on each section
	 * @param secIndex
	 * @return
	 */
	public final List<Integer> getRupturesForSection(int secIndex) {
		if (rupturesForSectionCache == null) {
			CalcProgressBar p = null;
			if (showProgress) {
				p = new CalcProgressBar("Calculating Ruptures for each Section", "Calculating Ruptures for each Section");
			}
			rupturesForSectionCache = new ArrayList<ArrayList<Integer>>();
			for (int secID=0; secID<getNumSections(); secID++)
				rupturesForSectionCache.add(new ArrayList<Integer>());
			
			int numRups = getNumRuptures();
			for (int rupID=0; rupID<numRups; rupID++) {
				if (p != null) p.updateProgress(rupID, numRups);
				for (int secID : getSectionsIndicesForRup(rupID)) {
					rupturesForSectionCache.get(secID).add(rupID);
				}
			}
			if (p != null) p.dispose();
		}
		
		return rupturesForSectionCache.get(secIndex);
	}
	
}

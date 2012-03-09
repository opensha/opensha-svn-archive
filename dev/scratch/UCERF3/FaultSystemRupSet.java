/**
 * 
 */
package scratch.UCERF3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Region;
import org.opensha.commons.geo.RegionUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import com.google.common.base.Preconditions;

import scratch.UCERF3.analysis.DeformationModelsCalc;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
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
		rupSectionSlipsCache.clear();
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
	 * This returns the magnitude of the smallest rupture involving this section or NaN
	 * if no ruptures involve this section.
	 * @param sectIndex
	 * @return
	 */
	public double getMinMagForSection(int sectIndex) {
		List<Integer> rups = getRupturesForSection(sectIndex);
		if (rups.isEmpty())
			return Double.NaN;
		double minMag = Double.POSITIVE_INFINITY;
		for (int rupIndex : getRupturesForSection(sectIndex)) {
			double mag = getMagForRup(rupIndex);
			if (mag < minMag)
				minMag = mag;
		}
		return minMag;
	}
	
	/**
	 * This returns the magnitude of the largest rupture involving this section or NaN
	 * if no ruptures involve this section.
	 * @param sectIndex
	 * @return
	 */
	public double getMaxMagForSection(int sectIndex) {
		List<Integer> rups = getRupturesForSection(sectIndex);
		if (rups.isEmpty())
			return Double.NaN;
		double maxMag = 0;
		for (int rupIndex : getRupturesForSection(sectIndex)) {
			double mag = getMagForRup(rupIndex);
			if (mag > maxMag)
				maxMag = mag;
		}
		return maxMag;
	}
	
	
	/**
	 * This computes the moment rate reduction, if any, that was applied to the slip rate (as returned by
	 * getSlipRateForSection).<br>
	 * <br>
	 * This is computed as: <code>moRateReduction = 1 - (reducedSlip / origSlip)</code>
	 * where origSlip is first converted to meters.
	 * @param sectIndex
	 * @return
	 */
	public double getMomentRateReductionForSection(int sectIndex) {
		double origSlip = getFaultSectionData(sectIndex).getReducedAveSlipRate() * 1e-3; // convert to meters
		double moReducedSlip = getSlipRateForSection(sectIndex);
		return 1d - moReducedSlip/origSlip;
	}

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
	public List<double[]> getSlipOnSectionsForAllRups() {
		ArrayList<double[]> slips = new ArrayList<double[]>();
		for (int rupIndex=0; rupIndex<getNumRuptures(); rupIndex++)
			slips.add(getSlipOnSectionsForRup(rupIndex));
		return slips;
	}
	
	protected HashMap<Integer, double[]> rupSectionSlipsCache = new HashMap<Integer, double[]>();
	
	/**
	 * This gives the average slip (SI untis: m) on each section for the rth rupture
	 * @return
	 */
	public double[] getSlipOnSectionsForRup(int rthRup) {
		double[] slips = rupSectionSlipsCache.get(rthRup);
		if (slips == null) {
			slips = calcSlipOnSectionsForRup(rthRup);
			rupSectionSlipsCache.put(rthRup, slips);
		}
		return slips;
	}
	
	private static EvenlyDiscretizedFunc taperedSlipPDF, taperedSlipCDF;
	
	/**
	 * This represents the total moment rate available to the rupture, assuming it is the only
	 * event to occur along the sections it uses.
	 * @param rupIndex
	 * @return
	 */
	protected double calcTotalAvailableMomentRate(int rupIndex) {
		List<Integer> sectsInRup = getSectionsIndicesForRup(rupIndex);
		double totMoRate = 0;
		for(Integer sectID:sectsInRup) {
			double area = getAreaForSection(sectID);
			totMoRate += FaultMomentCalc.getMoment(area, getSlipRateForSection(sectID));
		}
		return totMoRate;
	}
	
	public abstract SlipAlongRuptureModels getSlipAlongRuptureModel();
	
	/**
	 * This gets the slip on each section based on the value of slipModelType.
	 * The slips are in meters.  Note that taper slipped model wts slips by area
	 * to maintain moment balance (so it doesn't plot perfectly); do something about this?
	 * 
	 * Note that for two parallel faults that have some overlap, the slip won't be reduced
	 * along the overlap the way things are implemented here.
	 * 
	 * This has been spot checked, but needs a formal test.
	 *
	 */
	protected double[] calcSlipOnSectionsForRup(int rthRup) {
		
		SlipAlongRuptureModels slipModelType = getSlipAlongRuptureModel();
		Preconditions.checkNotNull(slipModelType);
		
		List<Integer> sectionIndices = getSectionsIndicesForRup(rthRup);
		int numSects = sectionIndices.size();

		double[] slipsForRup = new double[numSects];
		
		// compute rupture area
		double[] sectArea = new double[numSects];
		double[] sectMoRate = new double[numSects];
		int index=0;
		for(Integer sectID: sectionIndices) {	
			FaultSectionPrefData sectData = getFaultSectionData(sectID);
			sectArea[index] = sectData.getTraceLength()*sectData.getReducedDownDipWidth()*1e6;	// aseismicity reduces area; 1e6 for sq-km --> sq-m
			sectMoRate[index] = FaultMomentCalc.getMoment(sectArea[index], getSlipRateForSection(sectID));
			index += 1;
		}
			 		
		double aveSlip = getAveSlipForRup(rthRup);  // in meters
		
		// for case segment slip is independent of rupture (constant), and equal to slip-rate * MRI
		if(slipModelType == SlipAlongRuptureModels.CHAR) {
			throw new RuntimeException("SlipModelType.CHAR_SLIP_MODEL not yet supported");
		}
		// for case where ave slip computed from mag & area, and is same on all segments 
		else if (slipModelType == SlipAlongRuptureModels.UNIFORM) {
			for(int s=0; s<slipsForRup.length; s++)
				slipsForRup[s] = aveSlip;
		}
		// this is the model where section slip is proportional to section slip rate 
		// (bumped up or down based on ratio of seg slip rate over wt-ave slip rate (where wts are seg areas)
		else if (slipModelType == SlipAlongRuptureModels.WG02) {
			double totMoRateForRup = calcTotalAvailableMomentRate(rthRup);
			for(int s=0; s<slipsForRup.length; s++) {
				slipsForRup[s] = aveSlip*sectMoRate[s]*getAreaForRup(rthRup)/(totMoRateForRup*sectArea[s]);
			}
		}
		else if (slipModelType == SlipAlongRuptureModels.TAPERED) {
			// note that the ave slip is partitioned by area, not length; this is so the final model is moment balanced.

			// make the taper function if hasn't been done yet
			if(taperedSlipCDF == null) {
				taperedSlipCDF = new EvenlyDiscretizedFunc(0, 5001, 0.0002);
				taperedSlipPDF = new EvenlyDiscretizedFunc(0, 5001, 0.0002);
				double x,y, sum=0;
				int num = taperedSlipPDF.getNum();
				for(int i=0; i<num;i++) {
					x = taperedSlipPDF.getX(i);
					y = Math.pow(Math.sin(x*Math.PI), 0.5);
					taperedSlipPDF.set(i,y);
					sum += y;
				}
				// now make final PDF & CDF
				y=0;
				for(int i=0; i<num;i++) {
						y += taperedSlipPDF.getY(i);
						taperedSlipCDF.set(i,y/sum);
						taperedSlipPDF.set(i,taperedSlipPDF.getY(i)/sum);
//						System.out.println(taperedSlipCDF.getX(i)+"\t"+taperedSlipPDF.getY(i)+"\t"+taperedSlipCDF.getY(i));
				}
			}
			double normBegin=0, normEnd, scaleFactor;
			for(int s=0; s<slipsForRup.length; s++) {
				normEnd = normBegin + sectArea[s]/getAreaForRup(rthRup);
				// fix normEnd values that are just past 1.0
				if(normEnd > 1 && normEnd < 1.00001) normEnd = 1.0;
				scaleFactor = taperedSlipCDF.getInterpolatedY(normEnd)-taperedSlipCDF.getInterpolatedY(normBegin);
				scaleFactor /= (normEnd-normBegin);
				slipsForRup[s] = aveSlip*scaleFactor;
				normBegin = normEnd;
			}
		}
/*		*/
		// check the average
//		if(D) {
//			double aveCalcSlip =0;
//			for(int s=0; s<slipsForRup.length; s++)
//				aveCalcSlip += slipsForRup[s]*sectArea[s];
//			aveCalcSlip /= rupArea[rthRup];
//			System.out.println("AveSlip & CalcAveSlip:\t"+(float)aveSlip+"\t"+(float)aveCalcSlip);
//		}

//		if (D) {
//			System.out.println("\tsectionSlip\tsectSlipRate\tsectArea");
//			for(int s=0; s<slipsForRup.length; s++) {
//				FaultSectionPrefData sectData = faultSectionData.get(sectionIndices.get(s));
//				System.out.println(s+"\t"+(float)slipsForRup[s]+"\t"+(float)sectData.getAveLongTermSlipRate()+"\t"+sectArea[s]);
//			}
//					
//		}
		return slipsForRup;		
	}
	
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
	
	/**
	 * This computes the total moment rate for the fault system, taking into account the
	 * aseismicity factors and coupling coefficients.  
	 * @return moment in SI units
	 */
	public double getTotalMomentRateForAllSections() {
		return DeformationModelsCalc.calculateTotalMomentRate((ArrayList)getFaultSectionDataList(), true);
	}
	
}

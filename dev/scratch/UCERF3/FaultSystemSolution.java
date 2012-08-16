/**
 * 
 */
package scratch.UCERF3;


import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.geo.RegionUtils;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.FileUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.CompoundGriddedSurface;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionInputGenerator;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.PaleoProbabilityModel;
import scratch.UCERF3.utils.UCERF2_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.OLD_UCERF3_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.OLD_UCERF3_MFD_ConstraintFetcher.TimeAndRegion;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;

/**
 * This abstract class is intended to represent an Earthquake Rate Model solution 
 * for a fault system, coming from either the Grand Inversion or from a physics-based
 * earthquake simulator.
 * 
 * In addition to adding two methods to the FaultSystemRupSet interface (to get the rate of 
 * each rupture), this class contains many common utility methods for both types of subclass.
 * 
 * Notes:
 * 
 * 1) the getProbPaleoVisible(mag) method may become more complicated (e.g., site specific)
 * 
 * 2) calc methods here are untested
 * 
 * 
 * @author Field, Milner, Page, and Powers
 *
 */
public abstract class FaultSystemSolution extends FaultSystemRupSet {
	
	/**
	 * These gives the long-term rate (events/yr) of the rth rupture
	 * @param rupIndex
	 * @return
	 */
	public abstract double getRateForRup(int rupIndex);
	
	/**
	 * This gives the long-term rate (events/yr) of all ruptures
	 * @param rupIndex
	 * @return
	 */
	public abstract double[] getRateForAllRups();
	
	/**
	 * This returns the total long-term rate (events/yr) of all fault-based ruptures
	 * (fault based in case off-fault ruptures are added to subclass)
	 * @return
	 */
	public double getTotalRateForAllFaultSystemRups() {
		double totRate=0;
		for(double rate:getRateForAllRups())
			totRate += rate;
		return totRate;
	}
	
	/**
	 * This enables/disables visible progress bars for long calculations
	 * 
	 * @param showProgress
	 */
	public void setShowProgress(boolean showProgress) {
		this.showProgress = showProgress;
	}
	
	public void clearCache() {
		super.clearCache();
		clearSolutionCacheOnly();
	}
	
	public void clearSolutionCacheOnly() {
		slipPDFMap.clear();
		particRatesCache.clear();
		nucleationRatesCache.clear();
		totParticRatesCache = null;
		paleoVisibleRatesCache = null;
		slipRatesCache = null;
	}
	
	private HashMap<Integer, ArbDiscrEmpiricalDistFunc> slipPDFMap =
		new HashMap<Integer, ArbDiscrEmpiricalDistFunc>();
	
	/**
	 * This creates an empirical PDF (ArbDiscrEmpiricalDistFunc) of slips for the 
	 * specified section index, where the rate of each rupture is taken into account.
	 * @param sectIndex
	 * @return
	 */
	public synchronized ArbDiscrEmpiricalDistFunc calcSlipPFD_ForSect(int sectIndex) {
		ArbDiscrEmpiricalDistFunc slipPDF = slipPDFMap.get(sectIndex);
		if (slipPDF != null)
			return slipPDF;
		slipPDF = new ArbDiscrEmpiricalDistFunc();
		for (int r : getRupturesForSection(sectIndex)) {
			List<Integer> sectIndices = getSectionsIndicesForRup(r);
			double[] slips = this.getSlipOnSectionsForRup(r);
			for(int s=0; s<sectIndices.size(); s++) {
				if(sectIndices.get(s) == sectIndex) {
					slipPDF.set(slips[s], getRateForRup(r));
					break;
				}
			}
		}
		slipPDFMap.put(sectIndex, slipPDF);
		return slipPDF;
	}
	
	
	/**
	 * This returns the rate that pairs of section rupture together.  
	 * Most entries are zero because the sections are far from each other, 
	 * so a sparse matrix might be in order if this bloats memory.
	 * @return
	 */
	public double[][] getSectionPairRupRates() {
		double[][] rates = new double[getNumSections()][getNumSections()];
		for(int r=0; r< getNumRuptures(); r++) {
			List<Integer> indices = getSectionsIndicesForRup(r);
			double rate = getRateForRup(r);
			if (rate == 0)
				continue;
			for(int s=1;s<indices.size();s++) {
				rates[indices.get(s-1)][indices.get(s)] += rate;
				rates[indices.get(s)][indices.get(s-1)] += rate;    // fill in the symmetric point
			}
		}
		return rates;
	}

	private HashMap<String, double[]> particRatesCache = new HashMap<String, double[]>();
	
	/**
	 * This computes the participation rate (events/yr) of the sth section for magnitudes 
	 * greater and equal to magLow and less than magHigh.
	 * @param sectIndex
	 * @param magLow
	 * @param magHigh
	 * @return
	 */
	public double calcParticRateForSect(int sectIndex, double magLow, double magHigh) {
		return calcParticRateForAllSects(magLow, magHigh)[sectIndex];
	}
		
	private double doCalcParticRateForSect(int sectIndex, double magLow, double magHigh) {
		double partRate=0;
		for (int r : getRupturesForSection(sectIndex)) {
			double mag = this.getMagForRup(r);
			if(mag>=magLow && mag<magHigh)
				partRate += getRateForRup(r);
		}
		return partRate;
	}
	
	
	/**
	 * This computes the participation rate (events/yr) of all sections for magnitudes 
	 * greater and equal to magLow and less than magHigh.
	 * @param sectIndex
	 * @param magLow
	 * @param magHigh
	 * @return
	 */
	public synchronized double[] calcParticRateForAllSects(double magLow, double magHigh) {
		String key = (float)magLow+"_"+(float)magHigh;
		if (!particRatesCache.containsKey(key)) {
			double[] particRates = new double[getNumSections()];
			CalcProgressBar p = null;
			if (showProgress) {
				p = new CalcProgressBar("Calculating Participation Rates", "Calculating Participation Rates");
			}
			for (int i=0; i<particRates.length; i++) {
				if (p != null) p.updateProgress(i, particRates.length);
				particRates[i] = doCalcParticRateForSect(i, magLow, magHigh);
			}
			if (p != null) p.dispose();
			particRatesCache.put(key, particRates);
		}
		return particRatesCache.get(key);
	}

	private HashMap<String, double[]> nucleationRatesCache = new HashMap<String, double[]>();
	
	/**
	 * This computes the nucleation rate (events/yr) of the sth section for magnitudes 
	 * greater and equal to magLow and less than magHigh.
	 * @param sectIndex
	 * @param magLow
	 * @param magHigh
	 * @return
	 */
	public double calcNucleationRateForSect(int sectIndex, double magLow, double magHigh) {
		return calcNucleationRateForAllSects(magLow, magHigh)[sectIndex];
	}
		
	private double doCalcNucleationRateForSect(int sectIndex, double magLow, double magHigh) {
		double nucleationRate=0;
		for (int r : getRupturesForSection(sectIndex)) {
			double mag = this.getMagForRup(r);
			if(mag>=magLow && mag<magHigh) {
				double rate = getRateForRup(r);
				double sectArea = getAreaForSection(sectIndex);
				double rupArea = getAreaForRup(r);
				nucleationRate += rate * (sectArea / rupArea);
			}
		}
		return nucleationRate;
	}
	
	
	/**
	 * This computes the nucleation rate (events/yr) of all sections for magnitudes 
	 * greater and equal to magLow and less than magHigh.
	 * @param sectIndex
	 * @param magLow
	 * @param magHigh
	 * @return
	 */
	public synchronized double[] calcNucleationRateForAllSects(double magLow, double magHigh) {
		String key = (float)magLow+"_"+(float)magHigh;
		if (!nucleationRatesCache.containsKey(key)) {
			double[] nucleationRates = new double[getNumSections()];
			CalcProgressBar p = null;
			if (showProgress) {
				p = new CalcProgressBar("Calculating Nucleation Rates", "Calculating Participation Rates");
			}
			for (int i=0; i<nucleationRates.length; i++) {
				if (p != null) p.updateProgress(i, nucleationRates.length);
				nucleationRates[i] = doCalcNucleationRateForSect(i, magLow, magHigh);
			}
			if (p != null) p.dispose();
			nucleationRatesCache.put(key, nucleationRates);
		}
		return nucleationRatesCache.get(key);
	}
	
	private double[] totParticRatesCache;
	
	/**
	 * This computes the total participation rate (events/yr) of the sth section.
	 * 
	 * @param sectIndex
	 * @return
	 */
	public double calcTotParticRateForSect(int sectIndex) {
		return calcTotParticRateForAllSects()[sectIndex];
	}
	
	private double doCalcTotParticRateForSect(int sectIndex) {
		double partRate=0;
		for (int r : getRupturesForSection(sectIndex))
			partRate += getRateForRup(r);
		return partRate;
	}
	
	
	/**
	 * This computes the total participation rate (events/yr) for all sections.
	 * 
	 * @return
	 */
	public synchronized double[] calcTotParticRateForAllSects() {
		if (totParticRatesCache == null) {
			totParticRatesCache = new double[getNumSections()];
			CalcProgressBar p = null;
			if (showProgress) {
				p = new CalcProgressBar("Calculating Total Participation Rates", "Calculating Total Participation Rates");
			}
			for (int i=0; i<totParticRatesCache.length; i++) {
				if (p != null) p.updateProgress(i, totParticRatesCache.length);
				totParticRatesCache[i] = doCalcTotParticRateForSect(i);
			}
			if (p != null) p.dispose();
		}
		return totParticRatesCache;
	}
	
	private double[] paleoVisibleRatesCache;
	
	/**
	 * This gives the total paleoseismically observable rate (events/yr) of the sth section.
	 * the probability of observing an event is given by the getProbPaleoVisible(mag)
	 * method.
	 * 
	 * @param sectIndex
	 * @return
	 */
	public double calcTotPaleoVisibleRateForSect(int sectIndex) {
		return calcTotPaleoVisibleRateForAllSects()[sectIndex];
	}
	
	public double doCalcTotPaleoVisibleRateForSect(int sectIndex) {
		double partRate=0;
		for (int r : getRupturesForSection(sectIndex))
			partRate += getRateForRup(r)*getProbPaleoVisible(getMagForRup(r));
		return partRate;
	}

	
	/**
	 * This gives the total paleoseismically observable rate of all sections.
	 * the probability of observing an event is given by the getProbPaleoVisible(mag)
	 * method
	 * 
	 * @return
	 */
	public synchronized double[] calcTotPaleoVisibleRateForAllSects() {
		if (paleoVisibleRatesCache == null) {
			paleoVisibleRatesCache = new double[getNumSections()];
			CalcProgressBar p = null;
			if (showProgress) {
				p = new CalcProgressBar("Calculating Paleo Visible Rates", "Calculating Paleo Visible Rates");
			}
			for (int i=0; i<paleoVisibleRatesCache.length; i++) {
				if (p != null) p.updateProgress(i, paleoVisibleRatesCache.length);
				paleoVisibleRatesCache[i] = doCalcTotPaleoVisibleRateForSect(i);
			}
			if (p != null) p.dispose();
		}
		return paleoVisibleRatesCache;
	}
	
	private double[] slipRatesCache;
	
	/**
	 * This computes the slip rate of the sth section (meters/year))
	 * 
	 * @param sectIndex
	 * @return
	 */
	public double calcSlipRateForSect(int sectIndex) {
		return calcSlipRateForAllSects()[sectIndex];
	}
	
	private double doCalcSlipRateForSect(int sectIndex) {
		double slipRate=0;
		for (int r : getRupturesForSection(sectIndex)) {
			int ind = getSectionsIndicesForRup(r).indexOf(sectIndex);
//			slipRate += getRateForRup(r)*getAveSlipForRup(r);
			slipRate += getRateForRup(r)*getSlipOnSectionsForRup(r)[ind];
		}
		return slipRate;
	}
	
	/**
	 * This computes the slip rate of all sections (meters/year))
	 * 
	 * @return
	 */
	public synchronized double[] calcSlipRateForAllSects() {
		if (slipRatesCache == null) {
			slipRatesCache = new double[getNumSections()];
			CalcProgressBar p = null;
			if (showProgress) {
				p = new CalcProgressBar("Calculating Slip Rates", "Calculating Slip Rates");
			}
			for (int i=0; i<slipRatesCache.length; i++) {
				if (p != null) p.updateProgress(i, slipRatesCache.length);
				slipRatesCache[i] = doCalcSlipRateForSect(i);
			}
			if (p != null) p.dispose();
		}
		return slipRatesCache;
	}
	
	public SummedMagFreqDist calcNucleationMFD_forParentSect(int parentSectionID, double minMag, double maxMag, int numMag) {
		SummedMagFreqDist mfd = new SummedMagFreqDist(minMag, maxMag, numMag);
		
		for (int sectIndex=0; sectIndex<getNumSections(); sectIndex++) {
			if (getFaultSectionData(sectIndex).getParentSectionId() != parentSectionID)
				continue;
			IncrementalMagFreqDist subMFD = calcNucleationMFD_forSect(sectIndex, minMag, maxMag, numMag);
			mfd.addIncrementalMagFreqDist(subMFD);
		}
		
		return mfd;
	}

	/**
	 * This give a Nucleation Mag Freq Dist (MFD) for the specified section.  Nucleation probability 
	 * is defined as the area of the section divided by the area of the rupture.  
	 * This preserves rates rather than moRates (can't have both)
	 * @param sectIndex
	 * @param minMag - lowest mag in MFD
	 * @param maxMag - highest mag in MFD
	 * @param numMag - number of mags in MFD
	 * @return IncrementalMagFreqDist
	 */
	public  IncrementalMagFreqDist calcNucleationMFD_forSect(int sectIndex, double minMag, double maxMag, int numMag) {
		ArbIncrementalMagFreqDist mfd = new ArbIncrementalMagFreqDist(minMag, maxMag, numMag);
		for (int r : getRupturesForSection(sectIndex)) {
			double nucleationRate = getRateForRup(r)*getAreaForSection(sectIndex)/getAreaForRup(r);
			mfd.addResampledMagRate(getMagForRup(r), nucleationRate, true);
		}
		return mfd;
	}
	
	
	/**
	 * This give a Participation Mag Freq Dist for the specified section.
	 * This preserves rates rather than moRates (can't have both).
	 * @param sectIndex
	 * @param minMag - lowest mag in MFD
	 * @param maxMag - highest mag in MFD
	 * @param numMag - number of mags in MFD
	 * @return IncrementalMagFreqDist
	 */
	public IncrementalMagFreqDist calcParticipationMFD_forSect(int sectIndex, double minMag, double maxMag, int numMag) {
		ArbIncrementalMagFreqDist mfd = new ArbIncrementalMagFreqDist(minMag, maxMag, numMag);
		for (int r : getRupturesForSection(sectIndex))
			mfd.addResampledMagRate(getMagForRup(r), getRateForRup(r), true);
		return mfd;
	}
	
	/**
	 * This gives the total nucleation Mag Freq Dist of this solution.  
	 * This preserves rates rather than moRates (can't have both).
	 * @param minMag - lowest mag in MFD
	 * @param maxMag - highest mag in MFD
	 * @param delta - width of each mfd bin
	 * @return IncrementalMagFreqDist
	 */
	public IncrementalMagFreqDist calcTotalNucleationMFD(double minMag, double maxMag, double delta) {
		return calcNucleationMFD_forRegion(null, minMag, maxMag, delta, true);
	}

	/**
	 * This gives the total nucleation Mag Freq Dist inside the supplied region.  
	 * If <code>traceOnly == true</code>, only the rupture trace is examined in computing the fraction of the rupture 
	 * inside the region.  This preserves rates rather than moRates (can't have both).
	 * @param region - a Region object
	 * @param minMag - lowest mag in MFD
	 * @param maxMag - highest mag in MFD
	 * @param delta - width of each mfd bin
	 * @param traceOnly - if true only fault traces will be used for fraction inside region calculations, otherwise the
	 * entire rupture surfaces will be used (slower)
	 * @return IncrementalMagFreqDist
	 */
	public IncrementalMagFreqDist calcNucleationMFD_forRegion(Region region, double minMag, double maxMag, double delta, boolean traceOnly) {
		int numMag = (int)((maxMag - minMag) / delta+0.5) + 1;
		return calcNucleationMFD_forRegion(region, minMag, maxMag, numMag, traceOnly);
	}

	/**
	 * This gives the total nucleation Mag Freq Dist inside the supplied region.  
	 * If <code>traceOnly == true</code>, only the rupture trace is examined in computing the fraction of the rupture
	 * inside the region.  This preserves rates rather than moRates (can't have both).
	 * @param region - a Region object
	 * @param minMag - lowest mag in MFD
	 * @param maxMag - highest mag in MFD
	 * @param numMag - number of mags in MFD
	 * @param traceOnly - if true only fault traces will be used for fraction inside region calculations, otherwise the
	 * entire rupture surfaces will be used (slower)
	 * @return IncrementalMagFreqDist
	 */
	public IncrementalMagFreqDist calcNucleationMFD_forRegion(Region region, double minMag, double maxMag, int numMag, boolean traceOnly) {
		ArbIncrementalMagFreqDist mfd = new ArbIncrementalMagFreqDist(minMag, maxMag, numMag);
		double[] fractRupsInside = null;
		if (region != null)
			fractRupsInside = getFractRupsInsideRegion(region, traceOnly);
		for(int r=0;r<getNumRuptures();r++) {
			double fractInside = 1;
			if (region != null)
				fractInside = fractRupsInside[r];
			double rateInside=getRateForRup(r)*fractInside;
//			if (fractInside < 1)
//				System.out.println("inside: "+fractInside+"\trate: "+rateInside+"\tID: "+r);
			mfd.addResampledMagRate(getMagForRup(r), rateInside, true);
		}
		return mfd;
	}
	
	
	/**
	 * This returns the probability that the given magnitude event will be
	 * observed at the ground surface. This is based on equation 4 of Youngs et
	 * al. [2003, A Methodology for Probabilistic Fault Displacement Hazard
	 * Analysis (PFDHA), Earthquake Spectra 19, 191-219] using the coefficients
	 * they list in their appendix for "Data from Wells and Coppersmith (1993)
	 * 276 worldwide earthquakes". Their function has the following
	 * probabilities:
	 * 
	 * mag prob 5 0.10 6 0.45 7 0.87 8 0.98 9 1.00
	 * 
	 * @return
	 */
	public double getProbPaleoVisible(double mag) {
		System.out.println("WARNING! You are using the OLD UCERF2 paleo probability model!!!!");
		// TODO
		return Math.exp(-12.51 + mag * 2.053)
				/ (1.0 + Math.exp(-12.51 + mag * 2.053));
		/*
		 * Ray & Glenn's equation if(mag <= 5) return 0.0; else if (mag <= 7.6)
		 * return -0.0608*mag*mag + 1.1366*mag + -4.1314; else return 1.0;
		 */
	}
	
	/**
	 * This returns the normalized distance along a rupture that a paleoseismic trench is located (Glenn's x/L).  It is between 0 and 0.5.
	 * This currently puts the trench in the middle of the subsection.
	 * We need this for the UCERF3 probability of detecting a rupture in a trench.
	 * @return
	 */
	public static double getDistanceAlongRupture(List<Integer> sectsInRup, List<FaultSectionPrefData> sectionDataList, int paleoSectIndex) {
		double distanceAlongRup = 0;
		
		double totalLength = 0;
		double lengthToRup = 0;
		boolean reachConstraintLoc = false;
		
		// Find total length (km) of fault trace and length (km) from one end to the paleo trench location
		for (int i=0; i<sectsInRup.size(); i++) {
			int sectIndex = sectsInRup.get(i);
			double sectLength = sectionDataList.get(i).getFaultTrace().getTraceLength();
			totalLength+=sectLength;
			if (sectIndex == paleoSectIndex) {
				reachConstraintLoc = true;
				lengthToRup+=sectLength/2;  // We're putting the trench in the middle of the subsection for now
			}
			if (reachConstraintLoc == false) // We haven't yet gotten to the trench subsection so keep adding to lengthToRup
				lengthToRup+=sectLength;
		}
		
		if (!reachConstraintLoc) // check to make sure we came across the trench subsection in the rupture
			throw new IllegalStateException("Paleo site subsection was not included in rupture subsections");
		
		// Normalized distance along the rainbow (Glenn's x/L) - between 0 and 1
		distanceAlongRup = lengthToRup/totalLength;
		// Adjust to be between 0 and 0.5 (since rainbow is symmetric about 0.5)
		if (distanceAlongRup>0.5)
			distanceAlongRup=1-distanceAlongRup;
		
		return distanceAlongRup;
		
		
	}
	
	
	
	
	
	
	/**
	 * This plots the rupture rates (rate versus rupture index)
	 */
	public void plotRuptureRates() {
		// Plot the rupture rates
		ArrayList funcs = new ArrayList();		
		EvenlyDiscretizedFunc ruprates = new EvenlyDiscretizedFunc(0,(double)getNumRuptures()-1,getNumRuptures());
		for(int i=0; i<getNumRuptures(); i++)
			ruprates.set(i,getRateForRup(i));
		funcs.add(ruprates); 	
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Inverted Rupture Rates"); 
		graph.setX_AxisLabel("Rupture Index");
		graph.setY_AxisLabel("Rate");

	}
	
	/**
	 * This plots original and final slip rates versus section index.
	 * This also plot these averaged over parent sections.
	 */
	public void plotSlipRates() {
		int numSections = this.getNumSections();
		int numRuptures = this.getNumRuptures();
		List<FaultSectionPrefData> faultSectionData = this.getFaultSectionDataList();

		ArrayList funcs2 = new ArrayList();		
		EvenlyDiscretizedFunc syn = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);
		EvenlyDiscretizedFunc data = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);
		for (int i=0; i<numSections; i++) {
			data.set(i, this.getSlipRateForSection(i));
			syn.set(i,0);
		}
		for (int rup=0; rup<numRuptures; rup++) {
			double[] slips = getSlipOnSectionsForRup(rup);
			List<Integer> sects = getSectionsIndicesForRup(rup);
			for (int i=0; i < slips.length; i++) {
				int row = sects.get(i);
				syn.add(row,slips[i]*getRateForRup(rup));
			}
		}
		for (int i=0; i<numSections; i++) data.set(i, this.getSlipRateForSection(i));
		funcs2.add(syn);
		funcs2.add(data);
		GraphiWindowAPI_Impl graph2 = new GraphiWindowAPI_Impl(funcs2, "Slip Rate Synthetics (blue) & Data (black)"); 
		graph2.setX_AxisLabel("Fault Section Index");
		graph2.setY_AxisLabel("Slip Rate");
		
		String info = "index\tratio\tpredSR\tdataSR\tParentSectionName\n";
		String parentSectName = "";
		double aveData=0, aveSyn=0, numSubSect=0;
		ArrayList<Double> aveDataList = new ArrayList<Double>();
		ArrayList<Double> aveSynList = new ArrayList<Double>();
		for (int i = 0; i < numSections; i++) {
			if(!faultSectionData.get(i).getParentSectionName().equals(parentSectName)) {
				if(i != 0) {
					double ratio  = aveSyn/aveData;
					aveSyn /= numSubSect;
					aveData /= numSubSect;
					info += aveSynList.size()+"\t"+(float)ratio+"\t"+(float)aveSyn+"\t"+(float)aveData+"\t"+faultSectionData.get(i-1).getParentSectionName()+"\n";
//					System.out.println(ratio+"\t"+aveSyn+"\t"+aveData+"\t"+faultSectionData.get(i-1).getParentSectionName());
					aveSynList.add(aveSyn);
					aveDataList.add(aveData);
				}
				aveSyn=0;
				aveData=0;
				numSubSect=0;
				parentSectName = faultSectionData.get(i).getParentSectionName();
			}
			aveSyn +=  syn.getY(i);
			aveData +=  data.getY(i);
			numSubSect += 1;
		}
		ArrayList funcs5 = new ArrayList();		
		EvenlyDiscretizedFunc aveSynFunc = new EvenlyDiscretizedFunc(0,(double)aveSynList.size()-1,aveSynList.size());
		EvenlyDiscretizedFunc aveDataFunc = new EvenlyDiscretizedFunc(0,(double)aveSynList.size()-1,aveSynList.size());
		for(int i=0; i<aveSynList.size(); i++ ) {
			aveSynFunc.set(i, aveSynList.get(i));
			aveDataFunc.set(i, aveDataList.get(i));
		}
		aveSynFunc.setName("Predicted ave slip rates on parent section");
		aveDataFunc.setName("Original (Data) ave slip rates on parent section");
		aveSynFunc.setInfo(info);
		funcs5.add(aveSynFunc);
		funcs5.add(aveDataFunc);
		GraphiWindowAPI_Impl graph5 = new GraphiWindowAPI_Impl(funcs5, "Average Slip Rates on Parent Sections"); 
		graph5.setX_AxisLabel("Parent Section Index");
		graph5.setY_AxisLabel("Slip Rate");

	}
	
	/**
	 * This compares observed section rates (supplied) with those implied by the
	 * Fault System Solution.
	 * 
	 */
	public void plotPaleoObsAndPredPaleoEventRates(List<PaleoRateConstraint> paleoRateConstraints, InversionFaultSystemRupSet rupSet) {
		int numSections = this.getNumSections();
		int numRuptures = this.getNumRuptures();
		ArrayList funcs3 = new ArrayList();		
		EvenlyDiscretizedFunc finalEventRateFunc = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);
		EvenlyDiscretizedFunc finalPaleoVisibleEventRateFunc = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);	
		for (int r=0; r<numRuptures; r++) {
			List<Integer> sectsInRup= getSectionsIndicesForRup(r);
			for (int i=0; i<sectsInRup.size(); i++) {			
				finalEventRateFunc.add(sectsInRup.get(i),getRateForRup(r));  
				
				// UCERF2 Paleo Prob Model
//				finalPaleoVisibleEventRateFunc.add(rup.get(i),this.getProbPaleoVisible(this.getMagForRup(r))*getRateForRup(r));  
				
				// UCERF3 Paleo Prob Model
				try {
					PaleoProbabilityModel paleoProbModel = InversionInputGenerator.loadDefaultPaleoProbabilityModel();
					double distAlongRup = getDistanceAlongRupture(sectsInRup, rupSet.getFaultSectionDataList(), sectsInRup.get(i));
					finalPaleoVisibleEventRateFunc.add(sectsInRup.get(i),paleoProbModel.getForMag(this.getMagForRup(r), distAlongRup)*getRateForRup(r));  
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
			}
		}	
		finalEventRateFunc.setName("Total Event Rates oer Section");
		finalPaleoVisibleEventRateFunc.setName("Paleo Visible Event Rates oer Section");
		funcs3.add(finalEventRateFunc);
		funcs3.add(finalPaleoVisibleEventRateFunc);	
		int num = paleoRateConstraints.size();
		ArbitrarilyDiscretizedFunc func;
		ArrayList obs_er_funcs = new ArrayList();
		PaleoRateConstraint constraint;
		double totalError=0;
		for (int c = 0; c < num; c++) {
			func = new ArbitrarilyDiscretizedFunc();
			constraint = paleoRateConstraints.get(c);
			int sectIndex = constraint.getSectionIndex();
			func.set((double) sectIndex - 0.0001, constraint.getLower95ConfOfRate());
			func.set((double) sectIndex, constraint.getMeanRate());
			func.set((double) sectIndex + 0.0001, constraint.getUpper95ConfOfRate());
			func.setName(constraint.getFaultSectionName());
			funcs3.add(func);
			double r=(constraint.getMeanRate()-finalPaleoVisibleEventRateFunc.getClosestY(sectIndex))/(constraint.getUpper95ConfOfRate()-constraint.getLower95ConfOfRate());
			// System.out.println("Constraint #"+c+" misfit: "+r);
			totalError+=Math.pow(r,2);
		}			
		System.out.println("Event-rate constraint error = "+totalError);
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(
				PlotLineType.SOLID, 2f, Color.BLACK));
		plotChars.add(new PlotCurveCharacterstics(
				PlotLineType.SOLID, 2f, Color.BLUE));
		for (int c = 0; c < num; c++)
			plotChars.add(new PlotCurveCharacterstics(
					PlotLineType.SOLID, 1f, PlotSymbol.FILLED_CIRCLE, 4f, Color.RED));
		GraphiWindowAPI_Impl graph3 =
				new GraphiWindowAPI_Impl(funcs3,
						"Synthetic Event Rates (total - black & paleo visible - blue) and Paleo Data (red)",
						plotChars);
		graph3.setX_AxisLabel("Fault Section Index");
		graph3.setY_AxisLabel("Event Rate (per year)");

	}
	
	/**
	 * This compares the MFDs in the given MFD constraints with the MFDs 
	 * implied by the Fault System Solution
	 * @param mfdConstraints
	 */
	public void plotMFDs(List<MFD_InversionConstraint> mfdConstraints) {
		// Add ALL_CA to bring up another plot
//		mfdConstraints.add(UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.ALL_CA_1850));
		
		for (int i=0; i<mfdConstraints.size(); i++) {  // Loop over each MFD constraint 	
			MFD_InversionConstraint mfdConstraint = mfdConstraints.get(i);
			Region region = mfdConstraint.getRegion();
			
//			boolean traceOnly = region.getArea() > (300 * 300);
			boolean traceOnly = true;
			IncrementalMagFreqDist magHist = calcNucleationMFD_forRegion(region, 5.05, 9.05, 0.1, traceOnly);
			
			System.out.println("Total solution moment/yr for "+mfdConstraints.get(i).getRegion().getName()+" region = "+magHist.getTotalMomentRate());
			ArrayList<IncrementalMagFreqDist> funcs4 = new ArrayList<IncrementalMagFreqDist>();
			magHist.setName("Magnitude Distribution of SA Solution");
			magHist.setInfo("(number in each mag bin)");
			funcs4.add(magHist);
			IncrementalMagFreqDist targetMagFreqDist = mfdConstraints.get(i).getMagFreqDist();; 
			targetMagFreqDist.setName("Target Magnitude Distribution");
			targetMagFreqDist.setInfo(mfdConstraints.get(i).getRegion().getName());
			funcs4.add(targetMagFreqDist);
			
			// OPTIONAL: Add UCERF2 plots for comparison (Target minus off-fault component with aftershocks added back in & Background Seismicity)
			UCERF2_MFD_ConstraintFetcher ucerf2Constraints = new UCERF2_MFD_ConstraintFetcher();
			ucerf2Constraints.setRegion(mfdConstraints.get(i).getRegion());
			IncrementalMagFreqDist ucerf2_OnFaultTargetMFD = ucerf2Constraints.getTargetMinusBackgroundMFD();
			ucerf2_OnFaultTargetMFD.setTolerance(0.1); 
			ucerf2_OnFaultTargetMFD.setName("UCERF2 Target minus background+aftershocks");
			ucerf2_OnFaultTargetMFD.setInfo(mfdConstraints.get(i).getRegion().getName());
			IncrementalMagFreqDist ucerf2_OffFaultMFD = ucerf2Constraints.getBackgroundSeisMFD();
			ucerf2_OffFaultMFD.setName("UCERF2 Background Seismicity MFD"); 
			funcs4.add(ucerf2_OnFaultTargetMFD); funcs4.add(ucerf2_OffFaultMFD);
			
			// OPTIONAL: Plot implied off-fault MFD % Total Target
			if (mfdConstraints.get(i).getRegion().getName()=="RELM_NOCAL Region") {
				IncrementalMagFreqDist totalTargetMFD = OLD_UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.NO_CA_1850).getMagFreqDist();
				IncrementalMagFreqDist offFaultMFD = new IncrementalMagFreqDist(totalTargetMFD.getMinX(), totalTargetMFD.getNum(), totalTargetMFD.getDelta());
				for (double m=totalTargetMFD.getMinX(); m<=totalTargetMFD.getMaxX(); m+=totalTargetMFD.getDelta()) {
					offFaultMFD.set(m, totalTargetMFD.getClosestY(m) - magHist.getClosestY(m));		
				}
				offFaultMFD.setName("Implied Off-fault MFD for Solution"); totalTargetMFD.setName("Total Seismicity Rate for Region");
				offFaultMFD.setInfo("Total Target minus on-fault solution");totalTargetMFD.setInfo("Northern CA 1850-2007");
				funcs4.add(totalTargetMFD); funcs4.add(offFaultMFD);
			}
			if (mfdConstraints.get(i).getRegion().getName()=="RELM_SOCAL Region") {
				IncrementalMagFreqDist totalTargetMFD = OLD_UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.SO_CA_1850).getMagFreqDist();
				IncrementalMagFreqDist offFaultMFD = new IncrementalMagFreqDist(totalTargetMFD.getMinX(), totalTargetMFD.getNum(), totalTargetMFD.getDelta());
				for (double m=totalTargetMFD.getMinX(); m<=totalTargetMFD.getMaxX(); m+=totalTargetMFD.getDelta()) {
					offFaultMFD.set(m, totalTargetMFD.getClosestY(m) - magHist.getClosestY(m));
					
				}
				offFaultMFD.setName("Implied Off-fault MFD for Solution"); totalTargetMFD.setName("Total Seismicity Rate for Region");
				offFaultMFD.setInfo("Total Target minus on-fault solution");totalTargetMFD.setInfo("Southern CA 1850-2007");
				funcs4.add(totalTargetMFD); funcs4.add(offFaultMFD);
			}
			if (mfdConstraints.get(i).getRegion().getName()=="RELM_TESTING Region") {
				IncrementalMagFreqDist totalTargetMFD = OLD_UCERF3_MFD_ConstraintFetcher.getTargetMFDConstraint(TimeAndRegion.ALL_CA_1850).getMagFreqDist();
				IncrementalMagFreqDist offFaultMFD = new IncrementalMagFreqDist(totalTargetMFD.getMinX(), totalTargetMFD.getNum(), totalTargetMFD.getDelta());
				for (double m=totalTargetMFD.getMinX(); m<=totalTargetMFD.getMaxX(); m+=totalTargetMFD.getDelta()) {
					offFaultMFD.set(m, totalTargetMFD.getClosestY(m) - magHist.getClosestY(m));
					
				}
				offFaultMFD.setName("Implied Off-fault MFD for Solution"); totalTargetMFD.setName("Total Seismicity Rate for Region");
				offFaultMFD.setInfo("Total Target minus on-fault solution");totalTargetMFD.setInfo("All CA 1850-2007");
				funcs4.add(totalTargetMFD); funcs4.add(offFaultMFD);
			}
			
			
			
			GraphiWindowAPI_Impl graph4 = new GraphiWindowAPI_Impl(funcs4, "Magnitude Histogram for Final Rates"); 
			graph4.setX_AxisLabel("Magnitude");
			graph4.setY_AxisLabel("Frequency (per bin)");
			graph4.setYLog(true);
			graph4.setY_AxisRange(1e-6, 1.0);
		}
	}
	
	/**
	 * This returns the total moment of the solution (this does not include any off fault moment).<br>
	 * <br>
	 * This is calculated as the sum of the rates or each rupture times its moment (which is calculated form the magnitude)
	 * @return
	 */
	public double getTotalFaultSolutionMomentRate() {
		// calculate the moment
		double totalSolutionMoment = 0;
		for (int rup=0; rup<getNumRuptures(); rup++) 
			totalSolutionMoment += getRateForRup(rup)*MagUtils.magToMoment(getMagForRup(rup));
		return totalSolutionMoment;
	}

}

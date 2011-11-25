/**
 * 
 */
package scratch.UCERF3;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.geo.Region;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

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
public abstract class FaultSystemSolution implements FaultSystemRupSet {
	
	private boolean showProgress = false;
	
	/**
	 * These gives the long-term rate (events/yr) of the rth rupture
	 * @param rupIndex
	 * @return
	 */
	public abstract double getRateForRup(int rupIndex);
	
	/**
	 * Thise gives the long-term rate (events/yr) of all ruptures
	 * @param rupIndex
	 * @return
	 */
	public abstract double[] getRateForAllRups();
	
	/**
	 * This enables/disables visible progress bars for long calculations
	 * 
	 * @param showProgress
	 */
	public void setShowProgress(boolean showProgress) {
		this.showProgress = showProgress;
	}
	
	public void clearCache() {
		slipPDFMap.clear();
		particRatesCache.clear();
		totParticRatesCache = null;
		paleoVisibleRatesCache = null;
		slipRatesCache = null;
		rupturesForSectionCache.clear();
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
				p.displayProgressBar();
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
				p.displayProgressBar();
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
				p.displayProgressBar();
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
		for (int r : getRupturesForSection(sectIndex))
			slipRate += getRateForRup(r)*getAveSlipForRup(r);
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
				p.displayProgressBar();
			}
			for (int i=0; i<slipRatesCache.length; i++) {
				if (p != null) p.updateProgress(i, slipRatesCache.length);
				slipRatesCache[i] = doCalcSlipRateForSect(i);
			}
			if (p != null) p.dispose();
		}
		return slipRatesCache;
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
	 * This gives the total nucleation Mag Freq Dist inside the supplied region.  
	 * Only the rupture trace is examined in computing the fraction of the rupture 
	 * inside the region.  This preserves rates rather than moRates (can't have both).
	 * @param region - a Region object
	 * @param minMag - lowest mag in MFD
	 * @param maxMag - highest mag in MFD
	 * @param numMag - number of mags in MFD
	 * @return IncrementalMagFreqDist
	 */
	public IncrementalMagFreqDist calcNucleationMFD_forRegion(Region region, double minMag, double maxMag, int numMag) {
		ArbIncrementalMagFreqDist mfd = new ArbIncrementalMagFreqDist(minMag, maxMag, numMag);
		for(int r=0;r<getNumRuptures();r++) {
			double numInside=0, totNum=0;
			for(Integer s:getSectionsIndicesForRup(r)) {
				StirlingGriddedSurface sectSurf = getFaultSectionData(s).getStirlingGriddedSurface(1.0);
				for(int col=0; col<sectSurf.getNumCols();col++) {
					totNum +=1;
					if(region.contains(sectSurf.get(0, col))) 
						numInside+=1;
				}
			}
			double rateInside=getRateForRup(r)*numInside/totNum;
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
		return Math.exp(-12.51 + mag * 2.053)
				/ (1.0 + Math.exp(-12.51 + mag * 2.053));
		/*
		 * Ray & Glenn's equation if(mag <= 5) return 0.0; else if (mag <= 7.6)
		 * return -0.0608*mag*mag + 1.1366*mag + -4.1314; else return 1.0;
		 */
	}
	
	@Override
	public List<FaultSectionPrefData> getFaultSectionDataForRupture(int rupIndex) {
		List<Integer> inds = getSectionsIndicesForRup(rupIndex);
		ArrayList<FaultSectionPrefData> datas = new ArrayList<FaultSectionPrefData>();
		for (int ind : inds)
			datas.add(getFaultSectionData(ind));
		return datas;
	}
	
	private ArrayList<ArrayList<Integer>> rupturesForSectionCache = null;
	
	@Override
	public List<Integer> getRupturesForSection(int secIndex) {
		if (rupturesForSectionCache == null) {
			CalcProgressBar p = null;
			if (showProgress) {
				p = new CalcProgressBar("Calculating Ruptures for each Section", "Calculating Ruptures for each Section");
				p.displayProgressBar();
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

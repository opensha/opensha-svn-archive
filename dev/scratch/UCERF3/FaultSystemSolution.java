/**
 * 
 */
package scratch.UCERF3;


import org.opensha.commons.geo.Region;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * This abstract class is intended to represent an Earthquake Rate Model solution 
 * for a fault system, coming from either the Grand Inversion or from a physics-based
 * earthquake simulator.
 * 
 * This class contains many common utility methods for the subclasses.
 * 
 * 
 * @author Field, Milner, Page, and Powers
 *
 */
public abstract class FaultSystemSolution implements FaultSystemRupSet {
	
		
	public abstract double getRateForRup(int rupIndex);
	
	
	public abstract double[] getRateForAllRups();

	
	public double getParticRateForSect(int sectIndex, double magLow, double magHigh) {
		double partRate=0;
		for(int r=0;r<this.getNumRupRuptures();r++) {
			double mag = this.getMagForRup(r);
			if(mag>=magLow && mag<magHigh)
				if(getSectionsIndicesForRup(r).contains(sectIndex))
					partRate += getRateForRup(r);
		}
		return partRate;
	}
	
	
	public double[] getParticRateForAllSects(double magLow, double magHigh) {
		double[] partRate = new double[getNumSections()];
		for(int s=0;s<partRate.length;s++) {
			partRate[s]=getParticRateForSect(s, magLow, magHigh);
		}
		return partRate;
	}
	
	
	public double getTotParticRateForSect(int sectIndex) {
		double partRate=0;
		for(int r=0;r<this.getNumRupRuptures();r++) {
			if(getSectionsIndicesForRup(r).contains(sectIndex))
				partRate += getRateForRup(r);
		}
		return partRate;
	}
	
	
	public double[] getTotParticRateForAllSects() {
		double[] partRate = new double[getNumSections()];
		for(int s=0;s<partRate.length;s++) {
			partRate[s]=getTotParticRateForSect(s);
		}
		return partRate;
	}
	
	
	public double getTotPaleoVisibleRateForSect(int sectIndex) {
		double partRate=0;
		for(int r=0;r<getNumRupRuptures();r++) {
			if(getSectionsIndicesForRup(r).contains(sectIndex))
				partRate += getRateForRup(r)*getProbPaleoVisible(getMagForRup(r));
		}
		return partRate;
	}

	
	public double[] getTotPaleoVisibleRateForAllSects() {
		double[] partRate = new double[getNumSections()];
		for(int s=0;s<partRate.length;s++) {
			partRate[s]=getTotPaleoVisibleRateForSect(s);
		}
		return partRate;
	}
	
	
	public double getSlipRateForSect(int sectIndex) {
		double slipRate=0;
		for(int r=0;r<getNumRupRuptures();r++)
			if(getSectionsIndicesForRup(r).contains(sectIndex))
				slipRate += getRateForRup(r)*getAveSlipForRup(r);
		return slipRate;

	}

	
	public double[] getSlipRateForAllSects()  {
		double[] slipRate = new double[getNumSections()];
		for(int s=0;s<slipRate.length;s++) {
			slipRate[s]=getSlipRateForSect(s);
		}
		return slipRate;
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
	public  IncrementalMagFreqDist getNucleationMFD_forSect(int sectIndex, double minMag, double maxMag, int numMag) {
		ArbIncrementalMagFreqDist mfd = new ArbIncrementalMagFreqDist(minMag, maxMag, numMag);
		for(int r=0;r<getNumRupRuptures();r++)
			if(getSectionsIndicesForRup(r).contains(sectIndex)) {
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
	public IncrementalMagFreqDist getParticipationMFD_forSect(int sectIndex, double minMag, double maxMag, int numMag) {
		ArbIncrementalMagFreqDist mfd = new ArbIncrementalMagFreqDist(minMag, maxMag, numMag);
		for(int r=0;r<getNumRupRuptures();r++)
			if(getSectionsIndicesForRup(r).contains(sectIndex))
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
	public IncrementalMagFreqDist getNucleationMFD_forRegion(Region region, double minMag, double maxMag, int numMag) {
		ArbIncrementalMagFreqDist mfd = new ArbIncrementalMagFreqDist(minMag, maxMag, numMag);
		for(int r=0;r<getNumRupRuptures();r++) {
			double numInside=0, totNum=0;
			for(Integer s:getSectionsIndicesForRup(r)) {
				StirlingGriddedSurface sectSurf = getFaultSectionData(s).getStirlingGriddedSurface(true, 1.0);
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


}

package scratch.ned.ETAS_ERF.sandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;

import scratch.UCERF3.erf.FaultSystemSolutionPoissonERF;
import scratch.ned.ETAS_ERF.IntegerPDF_FunctionSampler;

/**
 * This class store information about all ruptures that nucleate inside this geographic block.
 * @author field
 *
 */
public class EqksAtPointTest {
	
	int[] rupIndexN_Array;
	double[] rateInsideArray;
	double[] fractInsideArray;

	double totalRateInside = -1;
	
	IntegerPDF_FunctionSampler randomEqkRupSampler;	// this is for random sampling of ruptures


	/**
	 * Constructor
	 */
	public EqksAtPointTest(int numRups) {
		rupIndexN_Array = new int[numRups];
		rateInsideArray = new double[numRups];
		fractInsideArray = new double[numRups];
	}	
	
	
	/**
	 * This gives the total rate at which ruptures nucleate inside the block
	 * @return
	 */
	public double getTotalRateInside() {
		// check to see whether it's already been calculated
		if(totalRateInside == -1) {
			totalRateInside=0;
			for(double rate:rateInsideArray) 
				totalRateInside += rate;
		}
		return totalRateInside;
	}
	
		
	
	
	/**
	 * This changes the rate for the specified rupture
	 * @param totRupRate - total rate, which will get reduced by the faction inside value
	 * @param nthRupIndex - the index of the nth rupture in the ERF
	 */
	public void changeRupRate(double totRupRate, int nthRupIndex) {
		int localIndex = Arrays.binarySearch(rupIndexN_Array, nthRupIndex);;
		if(localIndex < 0)	
			throw new RuntimeException("Index not found (was rate previously zero?)");;
		double oldRate = rateInsideArray[localIndex];
		double newRate = totRupRate*fractInsideArray[localIndex];
		// update totalRate
		if(totalRateInside != -1)
			totalRateInside += newRate-oldRate;
		// update sampler
		if(randomEqkRupSampler != null)
				randomEqkRupSampler.set(localIndex,newRate);
		rateInsideArray[localIndex] = newRate;
	}
	
	
	/**
	 * This returns the number of ruptures that nucleate inside the block
	 * @return
	 */
	public int getNumRupsInside() {
		return rupIndexN_Array.length;
	}
	
	
	/**
	 * This returns the index N of a randomly sampled rupture.
	 * @return
	 */
	public int getRandomRuptureIndexN() {
		// make random sampler if it doesn't already exist
		getRandomSampler();
		return rupIndexN_Array[randomEqkRupSampler.getRandomInt()];
	}
	
	/**
	 * This creates (if not already existant) and returns the randomEqkRupSampler
	 * @return
	 */
	public IntegerPDF_FunctionSampler getRandomSampler() {
		if(randomEqkRupSampler == null) {
			randomEqkRupSampler = new IntegerPDF_FunctionSampler(rupIndexN_Array.length);
			for(int i=0;i<rupIndexN_Array.length;i++) 
				randomEqkRupSampler.set(i,rateInsideArray[i]);
		}
		return randomEqkRupSampler;
	}
	
	/** This computes the expected, normalized mag-freq dist for the block (total rate is 1.0)
	 * 
	 * @return
	 */
	public ArbIncrementalMagFreqDist getMagProbDist(FaultSystemSolutionPoissonERF erf) {
		ArbIncrementalMagFreqDist magDist = new ArbIncrementalMagFreqDist(2.05, 8.95, 70);
		for(int j=0; j<rupIndexN_Array.length; j++) {
			double mag = erf.getNthRupture(rupIndexN_Array[j]).getMag();
			magDist.addResampledMagRate(mag, rateInsideArray[j], true);
		}
		magDist.scaleToCumRate(2.05, 1);
		return magDist;
	}
}

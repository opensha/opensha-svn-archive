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
public class EqksAtPoint {
	
	ArrayList<Integer> rupIndexN_List;	// this stores the Nth index of the rupture inside the ERF
	ArrayList<Double> rupRateInsideList;	// this holds the nucleation rate for each rupture inside the block
	ArrayList<Integer> srcIndexN_List;	// this stores the Nth index of the rupture inside the ERF
	ArrayList<Double> srcRateInsideList;	// this holds the nucleation rate for each rupture inside the block

	int[] rupIndexN_Array;
	double[] rupRateInsideArray;
	double[] rupFractInsideArray;
	int[] srcIndexN_Array;
	double[] srcRateInsideArray;
	double[] srcFractInsideArray;

	double totalRateInside = -1;
	
	IntegerPDF_FunctionSampler randomSampler;	// this is for random sampling of ruptures


	/**
	 * Constructor
	 */
	public EqksAtPoint() {
		rupIndexN_List = new ArrayList<Integer>();
		rupRateInsideList = new ArrayList<Double>();
		srcIndexN_List = new ArrayList<Integer>();
		srcRateInsideList = new ArrayList<Double>();
	}	
	
	
	public EqksAtPoint(	int[] rupIndexN_Array, double[] rupRateInsideArray, double[] rupFractInsideArray,
			int[] srcIndexN_Array, double[] srcRateInsideArray, double[] srcFractInsideArray) {

		this.rupIndexN_Array = rupIndexN_Array;
		this.rupRateInsideArray = rupRateInsideArray;
		this.rupFractInsideArray = rupFractInsideArray;
		this.srcIndexN_Array = srcIndexN_Array;
		this.srcRateInsideArray = srcRateInsideArray;
		this.srcFractInsideArray = srcFractInsideArray;

	}
	
	/**
	 * This gives the total rate at which ruptures nucleate inside the block
	 * @return
	 */
	public double getTotalRateInside() {
		// check to see whether it's already been calculated
		if(totalRateInside == -1) {
			totalRateInside=0;
			for(double rate:rupRateInsideArray) 
				totalRateInside += rate;
			for(double rate:srcRateInsideArray) 
				totalRateInside += rate;
		}
		return totalRateInside;
	}
	
		
	
	/**
	 * This adds the rate for the given nthRupIndex
	 * @param rate
	 * @param nthRupIndex
	 */
	public void addRupRate(double rate, int nthRupIndex) {
		int localIndex = rupRateInsideList.indexOf(nthRupIndex);		// faster with hashmap?
		if(localIndex<0) {	// index does not exist
			int size = rupIndexN_List.size();
			if(size>0) 
				if(nthRupIndex < rupIndexN_List.get(size-1))
					throw new RuntimeException("Ruptures must be entered in order");
			if(rate>0) {
				rupRateInsideList.add(rate);
				rupIndexN_List.add(nthRupIndex);
			}			
		}
		else {	// index exists; add the rate
			double newRate = rupRateInsideList.get(localIndex)+rate;
			rupRateInsideList.set(localIndex, newRate);

		}
	}

	
	/**
	 * This adds the rate for the given point src
	 * @param rate
	 * @param nthRupIndex
	 */
	public void addSrcRate(double rate, int srcIndex) {
		int localIndex = srcRateInsideList.indexOf(srcIndex);		// faster with hashmap?
		if(localIndex<0) {	// index does not exist
			int size = srcIndexN_List.size();
			if(size>0) 
				if(srcIndex < srcIndexN_List.get(size-1))
					throw new RuntimeException("Sources must be entered in order");
			if(rate>0) {
				srcRateInsideList.add(rate);
				srcIndexN_List.add(srcIndex);
			}			
		}
		else {	// index exists; add the rate
			double newRate = srcRateInsideList.get(localIndex)+rate;
			srcRateInsideList.set(localIndex, newRate);

		}
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
		double oldRate = rupRateInsideArray[localIndex];
		double newRate = totRupRate*rupFractInsideArray[localIndex];
		// update totalRate
		if(totalRateInside != -1)
			totalRateInside += newRate-oldRate;
		// update sampler
		if(randomSampler != null)
				randomSampler.set(localIndex,newRate);
		rupRateInsideArray[localIndex] = newRate;
	}
	
	
	/**
	 * This returns a 2-element array of ints, where the first value indicates
	 * the index type (0 for rup index, or 1 for src index), and the second element
	 * is the index (nth rup or ith src).
	 * @return
	 */
	public int[] getRandomRupOrSrc() {
		// make random sampler if it doesn't already exist
		getRandomSampler();
		int[] toReturn = new int[2];
		int localIndex = randomSampler.getRandomInt();
		if (localIndex < rupIndexN_Array.length) {
			toReturn[0] = 0;
			toReturn[1] = rupIndexN_Array[localIndex];
		}
		else {
			toReturn[0] = 1;
			toReturn[1] = srcIndexN_Array[localIndex-rupIndexN_Array.length];
		}
		return toReturn;
	}
	
	/**
	 * This creates (if not already existent) and returns the randomEqkRupSampler
	 * @return
	 */
	public IntegerPDF_FunctionSampler getRandomSampler() {
		if(randomSampler == null) {
			randomSampler = new IntegerPDF_FunctionSampler(rupIndexN_Array.length+srcIndexN_Array.length);
			for(int i=0;i<rupIndexN_Array.length;i++) 
				randomSampler.set(i,rupRateInsideArray[i]);
			for(int i=0;i<srcIndexN_Array.length;i++) 
				randomSampler.set(i,srcRateInsideArray[i]);
		}
		return randomSampler;
	}
	

	/**
	 * This converts the array lists to arrays (to reduce memory usage)
	 */
	public void finishAndShrinkSize(FaultSystemSolutionPoissonERF erf) {
		
		double duration = erf.getTimeSpan().getDuration();
		
		int num = rupIndexN_List.size();
		rupIndexN_Array = new int[num];
		rupRateInsideArray = new double[num];
		rupFractInsideArray = new double[num];
		
		for(int i=0;i<num;i++) {
			rupIndexN_Array[i] = rupIndexN_List.get(i);
			rupRateInsideArray[i] = rupRateInsideList.get(i);
			rupFractInsideArray[i] = rupRateInsideArray[i]/erf.getNthRupture(rupIndexN_Array[i]).getMeanAnnualRate(duration);
		}
				
		num = srcIndexN_List.size();
		srcIndexN_Array = new int[num];
		srcRateInsideArray = new double[num];
		srcFractInsideArray = new double[num];
		
		for(int i=0;i<num;i++) {
			srcIndexN_Array[i] = srcIndexN_List.get(i);
			srcRateInsideArray[i] = srcRateInsideList.get(i);
			srcFractInsideArray[i] = srcRateInsideArray[i]/erf.getSource(srcIndexN_Array[i]).computeTotalEquivMeanAnnualRate(duration);
		}
				
		rupIndexN_List = null;
		rupRateInsideList = null;
		srcIndexN_List = null;
		srcRateInsideList = null;

	}
	
	public int[] getRupIndexN_Array() {
		return rupIndexN_Array;
	}
	
	public double[] getRupRateInsideArray() {
		return rupRateInsideArray;
	}
	
	public double[] getRupFractInsideArray() {
		return rupFractInsideArray;
	}
	
	public int[] getSrcIndexN_Array() {
		return srcIndexN_Array;
	}
	
	public double[] getSrcRateInsideArray() {
		return srcRateInsideArray;
	}
	
	public double[] getSrcFractInsideArray() {
		return srcFractInsideArray;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		

	}
	
	/** This computes the expected, normalized mag-freq dist for the block (total rate is 1.0)
	 * 
	 * @return
	 */
	public ArbIncrementalMagFreqDist getMagProbDist(FaultSystemSolutionPoissonERF erf) {
		ArbIncrementalMagFreqDist magDist = new ArbIncrementalMagFreqDist(2.05, 8.95, 70);
		for(int j=0; j<rupIndexN_Array.length; j++) {
			double mag = erf.getNthRupture(rupIndexN_Array[j]).getMag();
			magDist.addResampledMagRate(mag, rupRateInsideArray[j], true);
		}
		magDist.scaleToCumRate(2.05, 1);
		return magDist;
	}
}

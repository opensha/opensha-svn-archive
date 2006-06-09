package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults;

import java.util.ArrayList;
import java.util.Iterator;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.surface.EvenlyGriddedSurface;
import org.opensha.data.*;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.calc.*;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.surface.*;
import org.opensha.sha.magdist.*;
import org.opensha.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.WC1994_MagAreaRelationship;

/**
 * <p>Title: A_FaultSource </p>
 * <p>Description: 
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field
 * @date Sept, 2003
 * @version 1.0
 */

public class A_FaultSource extends ProbEqkSource {

  //for Debug purposes
  private static String C = new String("A_FaultSource");
  private final static boolean D = true;
  private final static double KM_TO_METERS_CONVERT=1e6;

  //name for this classs
  protected String NAME = "Type-A Fault Source";

  protected double duration;

  private ArrayList ruptureList; // keep this in case we add more mags later
  private ArrayList faultCornerLocations = new ArrayList(); // used for the getMinDistance(Site) method
  
  private int num_seg, num_rup, num_scen;
  
  // x-axis attributes for the MagFreqDists
  private final static double MIN_MAG = 6;
  private final static double MAX_MAG = 8.5;
  private final static double DELTA_MAG = 0.01;
  private final static int NUM_MAG = 251;
  
  private final static double TOLERANCE = 1e6;
  
  private final static int[][] rupInSeg = {
	  // 1,2,3,4,5
		{1,0,0,0,0,0}, // rup 1
		{0,1,0,0,0,0}, // rup 2
		{1,1,0,0,0,0}, // rup 3
		{0,0,1,0,0,0}, // rup 4
		{0,1,1,0,0,0}, // rup 5
		{1,1,1,0,0,0}, // rup 6
		{0,0,0,1,0,0}, // rup 7
		{0,0,1,1,0,0}, // rup 8
		{0,1,1,1,0,0}, // rup 9
		{1,1,1,1,0,0}, // rup 10
		{0,0,0,0,1,0}, // rup 11
		{0,0,0,1,1,0}, // rup 12
		{0,0,1,1,1,0}, // rup 13
		{0,1,1,1,1,0}, // rup 14
		{1,1,1,1,1,0}, // rup 15
		{0,0,0,0,0,1}, // rup 16
		{0,0,0,0,1,1}, // rup 17
		{0,0,0,1,1,1}, // rup 18
		{0,0,1,1,1,1}, // rup 19
		{0,1,1,1,1,1}, // rup 20
		{1,1,1,1,1,1}  // rup 21
  	};
  
  
  	// array giving which scen (row) has each rupture (column)
  private final static int[][] scenHasRup = {
	//_ rup  1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1
			{1,1,0,1,0,0,1,0,0,0,1,0,0,0,0,1,0,0,0,0,0}, // scen 1
			{0,0,1,1,0,0,1,0,0,0,1,0,0,0,0,1,0,0,0,0,0}, // scen 2
			{1,0,0,0,1,0,1,0,0,0,1,0,0,0,0,1,0,0,0,0,0}, // scen 3
			{0,0,0,0,0,1,1,0,0,0,1,0,0,0,0,1,0,0,0,0,0}, // scen 4
			{1,1,0,0,0,0,0,1,0,0,1,0,0,0,0,1,0,0,0,0,0}, // scen 5
			{0,0,1,0,0,0,0,1,0,0,1,0,0,0,0,1,0,0,0,0,0}, // scen 6
			{1,0,0,0,0,0,0,0,1,0,1,0,0,0,0,1,0,0,0,0,0}, // scen 7
			{0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,1,0,0,0,0,0}, // scen 8
			{1,1,0,1,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0}, // scen 9
			{0,0,1,1,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0}, // scen 10
			{1,0,0,0,1,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0}, // scen 11
			{0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0}, // scen 12
			{1,1,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0}, // scen 13
			{0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0}, // scen 14
			{1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0}, // scen 15
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0}, // scen 16
			{1,1,0,1,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0}, // scen 17
			{0,0,1,1,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0}, // scen 18
			{1,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0}, // scen 19
			{0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0}, // scen 20
			{1,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0}, // scen 21
			{0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0}, // scen 22
			{1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0}, // scen 23
			{0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0}, // scen 24
			{1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0}, // scen 25
			{0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0}, // scen 26
			{1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0}, // scen 27
			{0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0}, // scen 28
			{1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0}, // scen 29
			{0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0}, // scen 30
			{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0}, // scen 31
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1}  // scen 32

  };
  
  	private String[] segName;  // segment name
  	private double[] segArea;  // segment area
  	private double[] segRate; // segment rate 
  	private ArbDiscrEmpiricalDistFunc[] segSlipDist;  // segment slip dist
  	private double[] rupMeanMag ; // rupture mean mae
  	private String[] rupName;
  	private GaussianMagFreqDist[] rupMagFreqDist; // MFD for rupture
  	private IncrementalMagFreqDist floaterMFD; // Mag Freq dist for floater


  /**
   * Description:
   * Notes: might want to add sigma, truncType, and truncLevel for characteristic events
   * 
   * @param segmentData - an ArrayList containing n ArrayLists (one for each segment), 
   * where the arrayList for each segment contains some number of FaultSectionPrefData objects.
   * It is assumed that these are in proper order such that concatenating the FaultTraces will produce
   * a total FaultTrace with locations in the proper order.
   * @param magAreaRel - any MagAreaRelationship
   * @param magSigma - the aleatory uncertainty on the magnitude for a given area
   * @param magTruncLevel - the last non-zero rate (in units of magSigma) on the magFreqDist for ruptures
   * @param magTruncType - the truncation type: 0=none; 1=upper; and 2= upper and lower
   * @param scenarioWts - should have 2^(n-1) + 1 elements, where the last is for the floaters
   * @param aseisReducesArea - if true apply asiesmicFactor as reduction of area, otherwise as reduction of slip rate
   * @param floatingRup_PDF - mag PDF of floaters; this is normalized internally to make sure it's a PDF
   * @
   */
  public A_FaultSource(ArrayList segmentData, MagAreaRelationship magAreaRel, double magSigma,
          double magTruncLevel,int magTruncType, double[] scenarioWts,
		  boolean aseisReducesArea, IncrementalMagFreqDist floatingRup_PDF) {

	  this.isPoissonian = true;
 
	  // make sure scenarioWts sum to 1;  make sure trunc type is 0, 1, or 2; magSigma is positive; magTruncLevel us positive
	  checkValidVals(magSigma, magTruncLevel, magTruncType, scenarioWts);

	  // calculte the number of segments, ruptures and scenarios. 
	  // Make sure that the number of scenario wts pased to this function are equal to number of scenarios for these segments
	  calcNumSegsRupsScenarios(segmentData, scenarioWts);
    
	  int seg,rup,scen; // for loops
   
	  segArea = new double[num_seg];
	  double[] segMoRate = new double[num_seg];
	  segName = new String[num_seg];
    
	  // calculate the Area, Name and Moment Rate for each segment
	  calcSegArea_Name_MoRate(segmentData, aseisReducesArea, segMoRate);
    
	  // calculate the total Area and Moment Rate for all the segments
	  double totalMoRate = 0;
	  double totalArea = 0;
	  for(seg=0; seg<num_seg; seg++) {
		  totalMoRate += segMoRate[seg];
		  totalArea += segArea[seg];
	  }
    
    // get the moRate for the floater using the last element in the scenarioWts array.
    double floaterWt = scenarioWts[num_scen];
    double floaterMoRate = totalMoRate*floaterWt;
    
    // get MFD for floater 
    floaterMFD = getMFD_ForFloater(floatingRup_PDF, floaterMoRate);
    
    // System.out.println(floaterMoRate +"  "+floaterMFD.getTotalMomentRate());
    
    double[] rupArea = new double[num_rup];
    rupMeanMag = new double[num_rup];
    double[] rupMaxMoRate = new double[num_rup]; // if all moment rate could go into this rupture
    double[] rupMoRate = new double[num_rup];
    double[] rupRate = new double[num_rup];
    rupMagFreqDist = new GaussianMagFreqDist[num_rup];
    rupName = getRuptureNames(segName);
    
  	// compute rupArea, rupMaxMoRate, and rupMag for each rupture
    getRupArea_MaxMoRate_Mag(magAreaRel, segArea, segMoRate, rupArea, rupMaxMoRate);
    
    // make summed Mag FreqDist
    SummedMagFreqDist summedMagFreqDist = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
    summedMagFreqDist.addIncrementalMagFreqDist(floaterMFD);
    
    // compute the actual rupture MoRate (considering floater weight as well)
    double totMoRateTest = computeRupMoRate(magSigma, magTruncLevel, magTruncType, scenarioWts, rupMaxMoRate, rupMoRate, rupRate, summedMagFreqDist);
    String []scenNames = this.getScenarioNames(rupName, num_seg);
    
    // check total moment rates
    totMoRateTest += floaterMoRate;
    double totMoRateTest2  = summedMagFreqDist.getTotalMomentRate();
    
    if(D) {
    		System.out.println("TotMoRate from segs = "+(float) totalMoRate);
    		System.out.println("TotMoRate from ruptures = "+(float) totMoRateTest);
    		System.out.println("TotMoRate from summed = "+(float) totMoRateTest2);
    }
    
    // find the rate for each segment
     segRate = new double[num_seg];
     computeSegRate(rupRate);
    
    // find the slip distribution of each rupture
    ArbitrarilyDiscretizedFunc[] rupSlipDist = new ArbitrarilyDiscretizedFunc[num_rup];
    computeRupSlipDist( rupArea, rupSlipDist);
    
    // find the slip distribution of each segment
    segSlipDist = new ArbDiscrEmpiricalDistFunc[num_seg];
    computeSegSlipDist(rupSlipDist);
   /* if(D) {
    	// print the slip distribution of each segment
    	for(int i=0; i<num_seg; ++i) {
    		System.out.println("Slip for segment "+i+":");
    		System.out.println(segSlipDist[i]);
    	}
    }*/
  }
  
  
  /**
   * Get the number of segments
   * 
   * @return
   */
  public int getNumSegments() {
	  return this.num_seg;
  }
  
  /**
   * Get name for ithSegment
   * @param ithSegment
   * @return
   */
  public String getSegmentName(int ithSegment) {
	  return this.segName[ithSegment];
  }
  
  /**
   * Get area for ith Segment
   * 
   * @param ithSegment
   * @return
   */
  public double getSegmentArea(int ithSegment) {
	  return this.segArea[ithSegment];
  }
  
  /**
   * Get rate for ith segment
   * 
   * @param ithSegment
   * @return
   */
  public double getSegmentRate(int ithSegment) {
	  return this.segRate[ithSegment];
  }
  
  /**
   * Get recurrence interval for the ith Segment
   * 
   * @param ithSegment
   * @return
   */
  public double getSegmentRecurrenceInterval(int ithSegment) {
	  return 1.0/this.segRate[ithSegment];
  }
  
  /**
   * Get Slip Distribution for this segment
   * 
   * @param ithSegment
   * @return
   */
  public ArbDiscrEmpiricalDistFunc getSegmentSlipDist(int ithSegment) {
	  return this.segSlipDist[ithSegment];
  }
  
  
  /**
   * Get mean mag for ith Rupture
   * @param ithRup
   * @return
   */
  public double getRupMeanMag(int ithRup) {
	  return this.rupMeanMag[ithRup];
  }
  
  /**
   * Get name for ith Rup
   * @param ithRup
   * @return
   */
  public String getRupName(int ithRup) {
	  return this.rupName[ithRup];
  }
  
  /**
   * Get MagFreqDist for ith Rup
   * @param ithRup
   * @return
   */
  public GaussianMagFreqDist getRupMagFreqDist(int ithRup) {
	  return this.rupMagFreqDist[ithRup];
  }
  
  /**
   * Get the Mag Freq Dist for floater ruptures
   * 
   * @return
   */
  public IncrementalMagFreqDist getFloaterMagFreqDist() {
	 return this.floaterMFD; 
  }
  
  /**
   * Compute the  slip distribution for segment
   * 
   * @param rupSlipDist
   * @param segSlipDist
   */
  private void computeSegSlipDist(ArbitrarilyDiscretizedFunc[] rupSlipDist) {
	  for(int seg=0; seg<num_seg; ++seg) {
		  segSlipDist[seg]=new ArbDiscrEmpiricalDistFunc();
		  // Add the rates of all ruptures which are part of a segment
		  for(int rup=0; rup<num_rup; rup++)
			  if(rupInSeg[rup][seg]==1) {
				  for(int i=0; i<rupSlipDist[rup].getNum(); ++i)
					  segSlipDist[seg].set(rupSlipDist[rup].getX(i), rupSlipDist[rup].getY(i));
			  }
	  }
  }
  
  /**
   * Compute slip distribution from Mag Distribution
   * 
   * @param rupMagFreqDist
   * @param rupArea
   * @param rupSlipDist
   */
  private void computeRupSlipDist( double[] rupArea,
		  ArbitrarilyDiscretizedFunc[] rupSlipDist) {
	  for(int rup=0; rup<num_rup; ++rup) {
		  rupSlipDist[rup] = new ArbitrarilyDiscretizedFunc();
		  for(int mag=0; mag<rupMagFreqDist[rup].getNum(); ++mag) {
			  if(rupMagFreqDist[rup].getY(mag)==0) continue; // if rate is 0, do not find the slip for this mag
			  double moRate = MomentMagCalc.getMoment(rupMagFreqDist[rup].getX(mag));
			  double slip = FaultMomentCalc.getSlip(rupArea[rup], moRate);
			  rupSlipDist[rup].set(slip, rupMagFreqDist[rup].getY(mag));
		  }
	  }
  }
  
  /**
   * Compute the rate for all segments.
   *  
   * @param segRate
   * @param rupRate
   */
  private void computeSegRate( double[] rupRate) {
	  for(int seg=0; seg<num_seg; ++seg) {
		  segRate[seg]=0.0;
		  // Sum the rates of all ruptures which are part of a segment
		  for(int rup=0; rup<num_rup; rup++)
			  if(rupInSeg[rup][seg]==1) segRate[seg]+=rupRate[rup];
	  }
  }
  
/**
 * Compute the actual rupture MoRate (considering floater weight as well)
 * @param magSigma
 * @param magTruncLevel
 * @param magTruncType
 * @param scenarioWts
 * @param rupMeanMag
 * @param rupMaxMoRate
 * @param rupMoRate
 * @param rupRate
 * @param rupMagFreqDist
 * @param rupName
 * @param summedMagFreqDist
 * @return
 */
private double computeRupMoRate(double magSigma, double magTruncLevel, int magTruncType, 
		double[] scenarioWts, double[] rupMaxMoRate, 
		double[] rupMoRate, double[] rupRate, SummedMagFreqDist summedMagFreqDist) {
	int rup;
	int scen;
	double totMoRateTest = 0;
    for(rup=0; rup<num_rup; rup++){
		rupMoRate[rup] = 0;
		for(scen=0; scen < num_scen; scen++) {
			if(scenHasRup[scen][rup]==1) { // if this rupture is included in current scenario
				rupMoRate[rup] += rupMaxMoRate[rup]*scenarioWts[scen];
			}
		}
		
		rupMagFreqDist[rup] = new GaussianMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG, rupMeanMag[rup], magSigma,
				rupMoRate[rup], magTruncLevel, magTruncType);
		summedMagFreqDist.addIncrementalMagFreqDist(rupMagFreqDist[rup]);
		// this next one should be replaced with the tot rate of the MFD
		rupRate[rup] = rupMoRate[rup]/MomentMagCalc.getMoment(rupMeanMag[rup]);
		totMoRateTest += rupMoRate[rup];
    }
	return totMoRateTest;
}
	
	/**
	 * Get the rupture names based on segment names
	 * @param sectionNames
	 * @return
	 */
	public final static String[] getRuptureNames(String[] segmentNames) {
		int seg;
		int numRups = getNumRuptures(segmentNames.length);
		String[] rupName = new String[numRups];
		for(int rup=0; rup<numRups; rup++){
			boolean isFirst = true;
			for(seg=0; seg < segmentNames.length; seg++) {
				if(rupInSeg[rup][seg]==1) { // if this rupture is included in this segment
					if(isFirst) { // append the section name to rupture name
						rupName[rup] = ""+(seg+1);
						isFirst = false;
					} else rupName[rup] += (seg+1);
				}
			}
		}
		return rupName;
	}
	
	
	/**
	 * Get the scenario names based on rupture names
	 * 
	 * @param rupNames
	 * @return
	 */
	public final static String[] getScenarioNames(String[] rupNames, int numSegments) {
		int numScenarios = getNumScenarios(numSegments);
		String[] scenName = new String[numScenarios];
		for(int scen=0; scen < numScenarios; scen++) {
			boolean isFirst = true;
			for(int rup=0; rup<rupNames.length; rup++){
				if(scenHasRup[scen][rup]==1) { // if this rupture is included in current scenario
					if(isFirst) { // append the rupture name to scenario name
						scenName[scen]=rupNames[rup];
						isFirst = false;
					} else scenName[scen]+="; "+rupNames[rup];
				}
			}
	    }
	    return scenName;
	}

	  /**
	   * compute rupArea, rupMaxMoRate, and rupMag for each rupture
	   * @param magAreaRel
	   * @param segArea
	   * @param segMoRate
	   * @param segName
	   * @param rupArea
	   * @param rupMeanMag
	   * @param rupMaxMoRate
	   * @param rupName
	   */
	private void getRupArea_MaxMoRate_Mag(MagAreaRelationship magAreaRel, double[] segArea, 
			double[] segMoRate, double[] rupArea, double[] rupMaxMoRate) {
		int seg;
		int rup;
		for(rup=0; rup<num_rup; rup++){
	    		rupArea[rup] = 0;
	    		rupMaxMoRate[rup] = 0;
	    		for(seg=0; seg < num_seg; seg++) {
	    			
	    			if(rupInSeg[rup][seg]==1) { // if this rupture is included in this segment	
	    				rupArea[rup] += segArea[seg];
	            		rupMaxMoRate[rup] += segMoRate[seg];
	    			}
	    			
	    		}
	    		// compute magnitude, rounded to nearest MFD x-axis point
	    		// convert area to km-sqr
	    		rupMeanMag[rup] = Math.round(magAreaRel.getMedianMag(rupArea[rup]/KM_TO_METERS_CONVERT)/DELTA_MAG) * DELTA_MAG;
	    }
	}

  /**
   * Get MFD for floater 
   * @param floatingRup_PDF
   * @param floaterMoRate
   * @return
   */
private IncrementalMagFreqDist getMFD_ForFloater(IncrementalMagFreqDist floatingRup_PDF, double floaterMoRate) {
	// get magFreqDist for floater
    IncrementalMagFreqDist floaterMFD = new IncrementalMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
    EvenlyDiscretizedFunc cumDist = floatingRup_PDF.getCumRateDist();
    for(int i=0; i<floaterMFD.getNum(); ++i) {
    	double x = floaterMFD.getX(i);
    	double x1 = x-DELTA_MAG/2;
    	double x2 = x + DELTA_MAG/2;
    	double cumRate1=0, cumRate2=0;
    	//check that the Mag lies within the range of cumDist
    	if(x1<cumDist.getMinX()) cumRate1 = 0.0;
    	else if(x1>cumDist.getMaxX()) cumRate1 = cumDist.getMaxY() ;
    	else cumRate1 = cumDist.getInterpolatedY(x1);
    	if(x2<cumDist.getMinX()) cumRate2 = 0.0;
    	else if(x2>cumDist.getMaxX()) cumRate2 = cumDist.getMaxY();
    	else cumRate2 = cumDist.getInterpolatedY(x2);
    	floaterMFD.set(i, cumRate2-cumRate1);
    }
    floaterMFD.scaleToTotalMomentRate(floaterMoRate);
	return floaterMFD;
}
 
	  /**
	   * Calculate  Area, MoRate and Name for each segment
	   * @param segmentData
	   * @param aseisReducesArea
	   * @param segArea
	   * @param segMoRate
	   * @param segName
	   * @return
	   */
	private void calcSegArea_Name_MoRate(ArrayList segmentData, boolean aseisReducesArea, double[] segMoRate) {
	
		// fill in segName, segArea and segMoRate
		  for(int seg=0;seg<num_seg;seg++) {
	    	segArea[seg]=0;
	    	segMoRate[seg]=0;
	    	ArrayList segmentDatum = (ArrayList) segmentData.get(seg);
	    	Iterator it = segmentDatum.iterator();
	    	ArrayList faultSectionNames = new ArrayList();
	    	while(it.hasNext()) {
	    		FaultSectionPrefData sectData = (FaultSectionPrefData) it.next();
	    		faultSectionNames.add(sectData.getSectionName());
	    		//set the area & moRate
	    		double length = sectData.getFaultTrace().getTraceLength(); // km
	    		double ddw = (sectData.getAveLowerDepth()-sectData.getAveUpperDepth())/Math.sin( sectData.getAveDip()*Math.PI/ 180); //km
	    		if(aseisReducesArea) {
	    			segArea[seg] += length*ddw*(1-sectData.getAseismicSlipFactor())*KM_TO_METERS_CONVERT; // meters-squared
	    			segMoRate[seg] += FaultMomentCalc.getMoment(segArea[seg], 
	    					sectData.getAveLongTermSlipRate()*1e-3); // SI units
	    		}
	    		else {
	    			segArea[seg] += length*ddw*KM_TO_METERS_CONVERT; // meters-squared
	    			segMoRate[seg] += FaultMomentCalc.getMoment(segArea[seg], 
	    					sectData.getAveLongTermSlipRate()*1e-3*(1-sectData.getAseismicSlipFactor())); // SI units
	    		}
	    	}
	    	segName[seg] = getSegmentName(faultSectionNames);
	    }
		return ;
	}
	
	/**
	 * Get the segment name based on fault section names
	 * @param sectionNames
	 * @return
	 */
	public final static String getSegmentName(ArrayList sectionNames) {
		String segName=null;
		for(int i=0; i<sectionNames.size(); ++i) {
			if(i==0) segName = (String)sectionNames.get(i);
			else segName += " -- "+(String)sectionNames.get(i);
		}
		return segName;
	}
	
	  /**
	   * Calculte the number of segments, ruptures and scenarios.
	   * Make sure that the number of scenario wts paased to this function are
	   * equal to number of scenarios for these segments
	   * @param segmentData
	   * @param scenarioWts
	   */
		private void calcNumSegsRupsScenarios(ArrayList segmentData, double[] scenarioWts) {
			num_seg = segmentData.size();
			num_rup = getNumRuptures(num_seg);
			num_scen = getNumScenarios(num_seg);
			if(num_seg > 5 || num_seg < 2)
				throw new RuntimeException("Error: num segments must be between 2 and 5");
			if(num_scen+1 != scenarioWts.length)  // the plus 1 is for the floater
				throw new RuntimeException("Error: number of segments incompatible with number of scenarioWts");
		}
		
		/**
		 * Get number of ruptures based on number of segments
		 * @param numSegs
		 * @return
		 */
		private final static int getNumRuptures(int numSegs) {
			return numSegs*(numSegs+1)/2;
		}
		
		/**
		 * Get the number of scenarios based on number of segments
		 * 
		 * @param numSegs
		 * @return
		 */
		private final static int getNumScenarios(int numSegs) {
			return (int) Math.pow(2,numSegs-1);
		}

	  /**
	   *  make sure scenarioWts sum to 1; 
	   *  make sure trunc type is 0, 1, or 2; 
	   *  magSigma is positive;
	   *  magTruncLevel us positive
	   *  
	   * @param magSigma
	   * @param magTruncLevel
	   * @param magTruncType
	   * @param scenarioWts
	   */
	private void checkValidVals(double magSigma, double magTruncLevel, int magTruncType, double[] scenarioWts) {
		double sum = 0.0;
		  for(int i=0; i<scenarioWts.length; ++i) sum+=scenarioWts[i];
		  if(Math.abs(sum-1.0)>TOLERANCE) throw new RuntimeException("Scenario Weights do not sum to 1"); 
	    
		  // make sure trunc type is 0, 1, or 2; 
		  if(magTruncType!=0 && magTruncType!=2 && magTruncType!=3)
			  throw new RuntimeException("Invalid truncation type value. Value values are 0, 1 and 2");
	    
		  //magSigma is positive; 
		  if(magSigma<0) throw new RuntimeException ("Magnitude Sigma should be � 0 ");
	     
		  // magTruncLevel us positive
		  if(magTruncLevel<0) throw new RuntimeException("Truncation Level for magnitude should � 0");
	}

  /**
   * Returns the Source Surface.
   * @return GriddedSurfaceAPI
   */
  public EvenlyGriddedSurfaceAPI getSourceSurface() {
    return null;
  }

  /**
   * It returns a list of all the locations which make up the surface for this
   * source.
   *
   * @return LocationList - List of all the locations which constitute the surface
   * of this source
   */
  public LocationList getAllSourceLocs() {
    LocationList locList = new LocationList();
    Iterator it = ( (EvenlyGriddedSurface) getSourceSurface()).
        getAllByRowsIterator();
    while (it.hasNext()) locList.addLocation( (Location) it.next());
    return locList;
  }


  /**
   * This changes the duration.
   * @param newDuration
   */
  public void setDuration(double newDuration) {
    if (this.isPoissonian != true)
      throw new RuntimeException(C +
          " Error - the setDuration method can only be used for the Poisson case");
    ProbEqkRupture eqkRup;
    double oldProb, newProb;
    for (int i = 0; i < ruptureList.size(); i++) {
      eqkRup = (ProbEqkRupture) ruptureList.get(i);
      oldProb = eqkRup.getProbability();
      newProb = 1.0 - Math.pow( (1.0 - oldProb), newDuration / duration);
      eqkRup.setProbability(newProb);
    }
    duration = newDuration;
  }

  /**
   * @return the total num of rutures for all magnitudes
   */
  public int getNumRuptures() {
    return num_rup;
  }

  /**
   * This method returns the nth Rupture in the list
   */
  public ProbEqkRupture getRupture(int nthRupture) {
    return (ProbEqkRupture) ruptureList.get(nthRupture);
  }

  /**
   * This returns the shortest dist to either end of the fault trace, or to the
   * mid point of the fault trace (done also for the bottom edge of the fault).
   * @param site
   * @return minimum distance in km
   */
  public double getMinDistance(Site site) {

    double min = Double.MAX_VALUE;
    double tempMin;

    Iterator it = faultCornerLocations.iterator();

    while (it.hasNext()) {
      tempMin = RelativeLocation.getHorzDistance(site.getLocation(),
                                                 (Location) it.next());
      if (tempMin < min) min = tempMin;
    }
//System.out.println(C+" minDist for source "+this.NAME+" = "+min);
    return min;
  }

  /**
   * This makes the vector of fault corner location used by the getMinDistance(site)
   * method.
   * @param faultSurface
   */
  private void makeFaultCornerLocs(EvenlyGriddedSurface faultSurface) {

    int nRows = faultSurface.getNumRows();
    int nCols = faultSurface.getNumCols();
    faultCornerLocations.add(faultSurface.get(0, 0));
    faultCornerLocations.add(faultSurface.get(0, (int) (nCols / 2)));
    faultCornerLocations.add(faultSurface.get(0, nCols - 1));
    faultCornerLocations.add(faultSurface.get(nRows - 1, 0));
    faultCornerLocations.add(faultSurface.get(nRows - 1, (int) (nCols / 2)));
    faultCornerLocations.add(faultSurface.get(nRows - 1, nCols - 1));

  }

  /**
   * set the name of this class
   *
   * @return
   */
  public void setName(String name) {
    NAME = name;
  }

  /**
   * get the name of this class
   *
   * @return
   */
  public String getName() {
    return NAME;
  }
  
  
  public static void main(String[] args) {
	  FaultSectionVer2_DB_DAO faultSectionDAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection);
	  FaultSectionPrefData santaCruz  = faultSectionDAO.getFaultSection(56).getFaultSectionPrefData(); // San Andreas - Santa Cruz
	  FaultSectionPrefData peninsula  = faultSectionDAO.getFaultSection(67).getFaultSectionPrefData(); // San Andreas - Peninsula
	  FaultSectionPrefData northCoastSouth  = faultSectionDAO.getFaultSection(27).getFaultSectionPrefData(); // San Andreas - North Coast South
	  FaultSectionPrefData northCoastNorth  = faultSectionDAO.getFaultSection(26).getFaultSectionPrefData(); // San Andreas - North Coast North
	  if(D) System.out.println("After retrieving fault sections from database");
	  // segment1
	  ArrayList santaCruzList = new ArrayList();
	  santaCruzList.add(santaCruz);
	  
	  //segment2
	  ArrayList peninsulaList = new ArrayList();
	  peninsulaList.add(peninsula);
	  
	  //segment3
	  ArrayList northCoastSouthList = new ArrayList();
	  northCoastSouthList.add(northCoastSouth);
	  
	  //segment4
	  ArrayList northCoastNorthList = new ArrayList();
	  northCoastNorthList.add(northCoastNorth);
	  
	  // list of all segments
	  ArrayList segmentData = new ArrayList();
	  segmentData.add(santaCruzList);
	  segmentData.add(peninsulaList);
	  segmentData.add(northCoastSouthList);
	  segmentData.add(northCoastNorthList);
	  
	  
	  double[] scenarioWts = { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.2};
	  GutenbergRichterMagFreqDist grMagFreqDist = new GutenbergRichterMagFreqDist(1, 1.0, 6, 8, 21);
	  A_FaultSource aFaultSource = new A_FaultSource(segmentData,  new WC1994_MagAreaRelationship(), 
			  0.12, 2.0, 2, scenarioWts, true, grMagFreqDist);
  }
}


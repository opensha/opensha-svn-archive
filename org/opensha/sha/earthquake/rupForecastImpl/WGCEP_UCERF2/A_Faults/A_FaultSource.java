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
import org.opensha.calc.nnls.cj.NNLSWrapper;

import sun.tools.tree.ThisExpression;

/**
 * <p>Title: WG_02FaultSource </p>
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

  private ArrayList ruptureList; 
  private ArrayList faultCornerLocations = new ArrayList(); // used for the getMinDistance(Site) method
  
  private int num_seg, num_rup;
  
  // x-axis attributes for the MagFreqDists
  private final static double MIN_MAG = 6;
  private final static double MAX_MAG = 8.5;
  private final static double DELTA_MAG = 0.1;
  private final static int NUM_MAG = 26;
  
  // slip model: 0 = Characteristic; 1 = Uniform/Boxcar; 2 = WGCEP-2002
  private int slipModelType;
  
  // rupture-model solution type: 0 = Min Rate; 1 = Max Rate; 2 = Equal Rate; 3 = Geol. Insight
  private int rupModelSolutionType;
  
  // for rounding magnitudes
  private final static double ROUND_MAG_TO = 0.01;
  
  private final static double TOLERANCE = 1e6;
  
  private int[][] rupInSeg;
  
  private final static int[][] rupInSeg2 = {
	// 1  2  3
	  {1, 0, 1},	// seg 1
	  {0, 1, 1}	// seg 2
  };
  
  private final static int[][] rupInSeg3 = {
	// 1  2  3  4  5  6
	  {1, 0, 0, 1, 0, 1},	// seg 1
	  {0, 1, 0, 1, 1, 1},	// seg 2
	  {0, 0, 1, 0, 1, 1}// seg 3
  };
  
  private final static int[][] rupInSeg4 = {
	// 1  2  3  4  5  6  7  8  9  10
	  {1, 0, 0, 0, 1, 0, 0, 1, 0, 1},	// seg 1
	  {0, 1, 0, 0, 1, 1, 0, 1, 1, 1},	// seg 2
	  {0, 0, 1, 0, 0, 1, 1, 1, 1, 1},	// seg 3
	  {0, 0, 0, 1, 0, 0, 1, 0, 1, 1}	// seg 4
  };
  
  private final static int[][] rupInSeg5 = {
	// 1  2  3  4  5  6  7  8  9  10 11 12 13 14 15   rups
	  {1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 1},	// seg 1
	  {0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1},	// seg 2
	  {0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1},	// seg 3
	  {0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1},	// seg 4
	  {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 1, 1}	// seg 5
  };
  
  private final static int[][] rupInSeg6 = {
	// 1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21   rups
	  {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 1},	// seg 1
	  {0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1},	// seg 2
	  {0, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1},	// seg 3
	  {0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1},	// seg 4
	  {0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1},	// seg 5
	  {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 1, 1}	// seg 6
  };
 
  	private String[] segName;  // segment name
  	private double[] segArea, segLength;  // segment area
  	private double[] segRate; // segment rate 
  	private double[] segAveSlipRate; // ave slip rate for segment
  	private ArbDiscrEmpiricalDistFunc[] segSlipDist;  // segment slip dist

  	private double[] rupMeanMag ; // rupture mean mae
  	private String[] rupName;
  	private GaussianMagFreqDist[] rupMagFreqDist; // MFD for rupture
//  	private IncrementalMagFreqDist floaterMFD; // Mag Freq dist for floater
  	private double[] totRupRate; // total rate of char ruptures
  	private SummedMagFreqDist summedMagFreqDist;
  	private double totalMoRateFromSegments;
  	private double totalMoRateFromRups;


  	/**
  	 * Description:
  	 * 
  	 * @param segmentData - an ArrayList containing N ArrayLists (one for each segment), 
  	 * where the arrayList for each segment contains some number of FaultSectionPrefData objects.
  	 * It is assumed that these are in proper order such that concatenating the FaultTraces will produce
  	 * a total FaultTrace with locations in the proper order.
  	 * @param magAreaRel - any MagAreaRelationship
  	 * @param aseisReducesArea - if true apply asiesmicFactor as reduction of area, otherwise as reduction of slip rate
  	 * @
  	 */
  	public A_FaultSource(ArrayList segmentData, MagAreaRelationship magAreaRel,
  			boolean aseisReducesArea, int slipModelType, int rupModelSolutionType,
  			double[] segMRIs, double[] segAveSlips, double[] aPrioriRupRates) {
  		
  		this.isPoissonian = true;
  		
  		num_seg = segmentData.size();
  		num_rup = num_seg*(num_seg+1)/2;
  		
  		rupInSeg = getRupInSegMatrix(num_seg);
  		
  		// do some checks
  		if(num_seg != segMRIs.length)
  			throw new RuntimeException("Error: number of segments is incompatible with number of elements in segMRIs");
  		if(num_seg != segAveSlips.length)
  			throw new RuntimeException("Error: number of segments is incompatible with number of elements in segAveSlips");
  		if(num_rup != aPrioriRupRates.length)
  			throw new RuntimeException("Error: number of ruptures is incompatible with number of elements in aPrioriRupRates");
  		
  		segArea = new double[num_seg];
  		segLength = new double[num_seg];
  		double[] segMoRate = new double[num_seg];

  		segName = new String[num_seg];
  		segAveSlipRate = new double[num_seg];
  		
  		// calculate the Area, Name and Moment Rate for each segment
  		calcSegArea_Name_MoRate(segmentData, aseisReducesArea, segMoRate, segLength);
  		
  		// calculate the total Area and Moment Rate for all the segments
  		totalMoRateFromSegments = 0;
  		double totalArea = 0;
  		for(int seg=0; seg<num_seg; seg++) {
  			totalMoRateFromSegments += segMoRate[seg];
  			totalArea += segArea[seg];
  		}
  		
  		double[] rupArea = new double[num_rup];
  		rupMeanMag = new double[num_rup];
  		double[] rupMoRate = new double[num_rup];
  		totRupRate = new double[num_rup];
//		rupMagFreqDist = new GaussianMagFreqDist[num_rup];
  		rupName = getRuptureNames(segName);
  		
  		// compute rupAreas
  		getRupAreas(segArea, rupArea);
  		
  		getRupMagsAndRates();
  		
  		// create the summed Mag FreqDist object
  		summedMagFreqDist = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
  		
  		// compute the actual rupture MoRates and MFDs (and add each to summedMagFreqDist)
  		// totalMoRateFromRups = computeRupRates(magSigma, magTruncLevel, magTruncType, scenarioWts, rupMaxMoRate, rupMoRate, totRupRate, summedMagFreqDist);
  		//String[] scenNames = this.getScenarioNames(rupName, num_seg);
  		
  		
  		// check total moment rates
  		double totMoRateTest2  = summedMagFreqDist.getTotalMomentRate();
  		
  		// add info to the summed dist
  		String summed_info = "\n\nMoment Rate: "+(float)totMoRateTest2+"\n\nTotal Rate: "+(float)summedMagFreqDist.getCumRate(0);
  		summedMagFreqDist.setInfo(summed_info);
  		
  		
  		/*if(D) {
  		 System.out.println("TotMoRate from segs = "+(float) this.totalMoRateFromSegments);
  		 System.out.println("TotMoRate from ruptures = "+(float) this.totalMoRateFromRups);
  		 System.out.println("TotMoRate from summed = "+(float) totMoRateTest2);
  		 }*/
  		
  		// find the rate of ruptures for each segment
  		segRate = new double[num_seg];
  		computeSegRate(totRupRate);
  		
  		// find the slip distribution of each rupture & segment
  		ArbitrarilyDiscretizedFunc[] rupSlipDist = new ArbitrarilyDiscretizedFunc[num_rup];
  		computeRupSlipDist( rupArea, rupSlipDist);
  		
  		
  		// get the increase/decrease factor for the ave slip on a segment, given a rupture,
  		// relative to the ave slip for the entire rupture
  		double[][] segRupSlipFactor = getSegRupSlipFactor(segMoRate, segArea);
  		
  		
  		// find the slip distribution of each segment
  		segSlipDist = new ArbDiscrEmpiricalDistFunc[num_seg];
  		computeSegSlipDist(rupSlipDist, magAreaRel, segRupSlipFactor);
  		/**/ 
  		if(D) {
  			// print the slip distribution of each segment
  			for(int i=0; i<num_seg; ++i) {
  				System.out.println("Slip for segment "+i+":");
  				System.out.println(segSlipDist[i]);
  			}
  		}
  	}
  	
  	
  	
  	private void getRupMagsAndRates() {
  		
  		NNLSWrapper nnls = new NNLSWrapper();
  		// we need to solve Xf=d, where f is the rupture rate vector
  		
  		// first compute the rupture mags and slips
  		// slipModelType: 0 = Characteristic; 1 = Uniform/Boxcar; 2 = WGCEP-2002
  		// rupModelSolutionType: 0 = Min Rate; 1 = Max Rate; 2 = Equal Rate; 3 = Geol. Insight

  		
  		
  		
  		
  	}
  
  	
  	private final static int[][] getRupInSegMatrix(int num_seg) {
  		if(num_seg == 2)
  			return rupInSeg2;
  		else if(num_seg == 3)
  			return rupInSeg3;
  		else if(num_seg == 4)
  			return rupInSeg4;
  		else if(num_seg == 5)
  			return rupInSeg5;
  		else if(num_seg == 6)
  			return rupInSeg6;
  		else
  			throw new RuntimeException("Error: num segments must be between 2 and 6");
  	}
  		

  public double getTotalMoRateFromSegs() {
	  return totalMoRateFromSegments;
  }
  
  public double getTotalMoRateFromRups() {
	  return totalMoRateFromRups;
  }
  
  public double getTotalMoRateFromSummedMFD() {
	  return summedMagFreqDist.getTotalMomentRate();
  }
  
  
  /**
   * Get total rupture rate of the ith char rupture
   * 
   * @param ithRup
   * @return
   */
  public double getRupRate(int ithRup) {
	  return totRupRate[ithRup];
  }
  
  /**
   * Get total Mag Freq dist for ruptures (including floater)
   *
   */
  public IncrementalMagFreqDist getTotalRupMFD() {
	  return this.summedMagFreqDist;
  }
  
  
  /**
   * Get the number of segments
   * 
   * @return
   */
  public int getNumSegments() {
	  return num_seg;
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
   * Initial Ave Segment slip rate
   * 
   * @param ithSegment
   * @return
   */
  public double getSegAveSlipRate(int ithSegment) {
	  return this.segAveSlipRate[ithSegment];
  }
  
  /**
   * Final ave segment slip rate
   */
  public double getFinalAveSegSlipRate(int ithSegment) {
	  ArbDiscrEmpiricalDistFunc segmenstSlipDist = getSegmentSlipDist(ithSegment);
	  double slipRate=0;
	  for(int i=0; i<segmenstSlipDist.getNum(); ++i)
		  slipRate+=segmenstSlipDist.getX(i)*segmenstSlipDist.getY(i);
	  return slipRate;
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
   * Compute the slip distribution for each segment
   * The average slip for each event is partitioned among the different segments
   * according to segRupSlipFactor.

   * 
   * @param rupSlipDist
   * @param segSlipDist
   */
  private void computeSegSlipDist(ArbitrarilyDiscretizedFunc[] rupSlipDist, 
		  MagAreaRelationship magAreaRel, double[][] segRupSlipFactor) {
	  for(int seg=0; seg<num_seg; ++seg) {
		  segSlipDist[seg]=new ArbDiscrEmpiricalDistFunc();
		  // Add the rates of all ruptures which are part of a segment
		  for(int rup=0; rup<num_rup; rup++)
			  if(rupInSeg[seg][rup]==1) {
				  for(int i=0; i<rupSlipDist[rup].getNum(); ++i)
					  segSlipDist[seg].set(segRupSlipFactor[rup][seg]*rupSlipDist[rup].getX(i), 
							  rupSlipDist[rup].getY(i));
			  }
	  }
  }
  
  
  /**
   * This computes the increase/decrease factor for the ave slip on a segment relative to the
   * ave slip for the entire rupture (based on moment rates and areas).  The idea being, 
   * for example, that if only full fault rupture is allowed on a fuult where the segments 
   * have different slip rates, then the amount of slip on each segment for that rupture
   * must vary to match the long-term slip rates).
   * @param segAveSlipRate
   */
  private double[][] getSegRupSlipFactor(double[] segMoRate, double[] segArea) {
	  double[][] segRupSlipFactor = new double[num_rup][num_seg];
	  for(int rup=0; rup<num_rup; ++rup) {
		  double totMoRate = 0;
		  double totArea = 0;
		  for(int seg=0; seg<num_seg; seg++) {
			  if(rupInSeg[seg][rup]==1) {
				  totMoRate += segMoRate[seg];
				  totArea += segArea[seg];
			  }
		  }
		  for(int seg=0; seg<num_seg; seg++) {
			  segRupSlipFactor[rup][seg] = rupInSeg[seg][rup]*segMoRate[seg]*totArea/(totMoRate*segArea[seg]);
		  }
	  }
	  return segRupSlipFactor;
  }
  
  /**
   * This computes the rates of slips for each rupture.
   * 
   * @param rupMagFreqDist
   * @param rupArea
   * @param rupSlipDist
   */
  private void computeRupSlipDist( double[] rupArea, ArbitrarilyDiscretizedFunc[] rupSlipDist) {

	  for(int rup=0; rup<num_rup; ++rup) {
		  rupSlipDist[rup] = new ArbitrarilyDiscretizedFunc();
		  for(int imag=0; imag<rupMagFreqDist[rup].getNum(); ++imag) {
				if(rupMagFreqDist[rup].getY(imag)==0) continue; // if rate is 0, do not find the slip for this mag
				double moment = MomentMagCalc.getMoment(rupMagFreqDist[rup].getX(imag));
				double slip = FaultMomentCalc.getSlip(rupArea[rup], moment);
				rupSlipDist[rup].set(slip, rupMagFreqDist[rup].getY(imag));
		  }
	  }
  }
  
  /**
   * Compute the rate for all segments.
   *  
   * @param segRate
   * @param totRupRate
   */
  private void computeSegRate( double[] totRupRate) {
	  for(int seg=0; seg<num_seg; ++seg) {
		  segRate[seg]=0.0;
		  // Sum the rates of all ruptures which are part of a segment
		  for(int rup=0; rup<num_rup; rup++) 
			  if(rupInSeg[seg][rup]==1) segRate[seg]+=totRupRate[rup];
	  }
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
		int[][] rupInSeg = getRupInSegMatrix(segmentNames.length);
		for(int rup=0; rup<numRups; rup++){
			boolean isFirst = true;
			for(seg=0; seg < segmentNames.length; seg++) {
				if(rupInSeg[seg][rup]==1) { // if this rupture is included in this segment
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
	   * compute rupArea, rupMaxMoRate, and rupMag for each rupture
	   * @param segArea
	   * @param rupArea
	   */
	private void getRupAreas(double[] segArea, double[] rupArea) {
		int seg;
		int rup;
		for(rup=0; rup<num_rup; rup++){
	    		rupArea[rup] = 0;
	    		for(seg=0; seg < num_seg; seg++) {
	    			if(rupInSeg[seg][rup]==1) { // if this rupture is included in this segment	
	    				rupArea[rup] += segArea[seg];
	    			}
	    		}
	    }
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
	private void calcSegArea_Name_MoRate(ArrayList segmentData, boolean aseisReducesArea, 
			                             double[] segMoRate, double[] segLengths) {
		
		// fill in segName, segArea and segMoRate
		for(int seg=0;seg<num_seg;seg++) {
			segArea[seg]=0;
			segLengths[seg]=0;
			segMoRate[seg]=0;
			ArrayList segmentDatum = (ArrayList) segmentData.get(seg);
			Iterator it = segmentDatum.iterator();
			ArrayList faultSectionNames = new ArrayList();
			while(it.hasNext()) {
				FaultSectionPrefData sectData = (FaultSectionPrefData) it.next();
				faultSectionNames.add(sectData.getSectionName());
				//set the area & moRate
				double length = sectData.getLength(); // km
				segLengths[seg]+=length;
				double ddw = sectData.getDownDipWidth(); //km
				if(aseisReducesArea) {
					double area = length*ddw*(1-sectData.getAseismicSlipFactor())*KM_TO_METERS_CONVERT; // meters-squared
					segArea[seg] += area;
					segMoRate[seg] += FaultMomentCalc.getMoment(area, 
							sectData.getAveLongTermSlipRate()*1e-3); // SI units
				}
				else {
					double area  = length*ddw*KM_TO_METERS_CONVERT;
					segArea[seg] +=  area;// meters-squared
					segMoRate[seg] += FaultMomentCalc.getMoment(area, 
							sectData.getAveLongTermSlipRate()*1e-3*(1-sectData.getAseismicSlipFactor())); // SI units
				}
				
			}
			segAveSlipRate[seg] = FaultMomentCalc.getSlip(segArea[seg], segMoRate[seg]);
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
			else segName += " + "+(String)sectionNames.get(i);
		}
		return segName;
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
   * Get number of ruptures based on number of segments
   * @param numSegs
   * @return
   */
  private final static int getNumRuptures(int numSegs) {
	  return numSegs*(numSegs+1)/2;
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
	  
	  
	  /*
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
	  WG_02FaultSource faultSource = new WG_02FaultSource(segmentData,  new WC1994_MagAreaRelationship(), 
			  0.12, 2.0, 2, scenarioWts, true, grMagFreqDist);
			  
	 */
  }
}


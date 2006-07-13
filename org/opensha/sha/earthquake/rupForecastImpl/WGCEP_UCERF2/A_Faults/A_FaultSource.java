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
  
  private SegmentedFaultData segmentData;
 
  	private ArbDiscrEmpiricalDistFunc[] segSlipDist;  // segment slip dist
  	
  	private double[] segRate;

  	private String[] rupNameShort, rupNameLong;
   	private double[] rupArea, rupMeanMag, rupMoRate, totRupRate; // rupture mean mag
  	private GaussianMagFreqDist[] rupMagFreqDist; // MFD for rupture

  	private SummedMagFreqDist summedMagFreqDist;
  	private double totalMoRateFromSegments, totalMoRateFromRups;

  	/**
  	 * Description:
  	 * 
  	 * @param segmentData - SegmentedFaultData, where it is assumed that these are in proper order such 
  	 * that concatenating the FaultTraces will produce a total FaultTrace with locations in the proper order.
  	 * @param magAreaRel - any MagAreaRelationship
  	 * @
  	 */
  	public A_FaultSource(SegmentedFaultData segmentData, MagAreaRelationship magAreaRel, 
  			int slipModelType, int rupModelSolutionType,
  			double[] segMRIs, double[] segAveSlips, double[] aPrioriRupRates) {
  		
  		this.segmentData = segmentData;
  		this.isPoissonian = true;
  		
  		num_seg = segmentData.getNumSegments();
  		num_rup = num_seg*(num_seg+1)/2;
  		
  		// do some checks
  		if(num_seg != segMRIs.length)
  			throw new RuntimeException("Error: number of segments is incompatible with number of elements in segMRIs");
  		if(num_seg != segAveSlips.length)
  			throw new RuntimeException("Error: number of segments is incompatible with number of elements in segAveSlips");
  		if(num_rup != aPrioriRupRates.length)
  			throw new RuntimeException("Error: number of ruptures is incompatible with number of elements in aPrioriRupRates");
  		  		
  //		rupMagFreqDist = new GaussianMagFreqDist[num_rup];
  		
  		// get the RupInSeg Matrix for the given number of segments
  		// rupInSeg = getRupInSegMatrix(num_seg);


  		getRuptureNames();
  		getRupAreas();
  		
  		// this is where the work is done (calculates totalMoRateFromRups too?)
  		getRupMagsAndRates();
  		
  		// create the summed Mag FreqDist object
  		summedMagFreqDist = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
  		// now add the rates of each rupture to this ...
  		// ?????????????????????????????????????????????
  		
  		// add info to the summed dist
  		String summed_info = "\n\nMoment Rate: "+(float) getTotalMoRateFromSummedMFD() +
  							"\n\nTotal Rate: "+(float)summedMagFreqDist.getCumRate(0);
  		summedMagFreqDist.setInfo(summed_info);
 		
  		// get total rate of events on each segment
  		computeSegRates();
  		
  		// find the slip distribution of each rupture & segment
  		// ?????????????????????????????????????????????????????
  		ArbitrarilyDiscretizedFunc[] rupSlipDist = new ArbitrarilyDiscretizedFunc[num_rup];
  		computeRupSlipDist( rupArea, rupSlipDist);
  		
  		
  		// get the increase/decrease factor for the ave slip on a segment, given a rupture,
  		// relative to the ave slip for the entire rupture
  		// ????????????????????????????????????????????????
  		double[][] segRupSlipFactor = getWG02_SegRupSlipFactor();
  		
  		
  		// find the slip distribution of each segment
  		// ????????????????????????????????????????????????
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
  		
  		// solve for the following:
  		rupMeanMag = new double[num_rup];
  		rupMoRate = new double[num_rup];
  		totRupRate = new double[num_rup];  // if rupture given an MFD

  		NNLSWrapper nnls = new NNLSWrapper();
  		// we need to solve Xf=d, where f is the rupture rate vector
  		
  		// first compute the rupture mags and slips
  		// slipModelType: 0 = Characteristic; 1 = Uniform/Boxcar; 2 = WGCEP-2002
  		// rupModelSolutionType: 0 = Min Rate; 1 = Max Rate; 2 = Equal Rate; 3 = Geol. Insight

  		
  		
  		
  		
  	}
  
  	
  	private final static void getRupInSegMatrix(int num_seg) {
  		
  		int num_rup = num_seg*(num_seg+1)/2;
  		int[][] rupInSeg = new int[num_seg][num_rup];
  		
  		int n_rup_wNseg = num_seg;
  		int remain_rups = num_seg;
  		int nSegInRup = 1;
  		int startSeg = 0;
  		for(int rup = 0; rup < num_rup; rup += 1) {
  			for(int seg = startSeg; seg < startSeg+nSegInRup; seg += 1)
  				rupInSeg[seg][rup] = 1;
  			startSeg += 1;
  			remain_rups -= 1;
  			if(remain_rups == 0) {
  				startSeg = 0;
  				nSegInRup += 1;
  				n_rup_wNseg -= 1;
  				remain_rups = n_rup_wNseg;
  			}
  		}
  		
  		// check result
  		for(int seg = 0; seg < num_seg; seg+=1) {
  			System.out.print("\n");
  			for(int rup = 0; rup < num_rup; rup += 1)
  				System.out.print(rupInSeg[seg][rup]+"  ");
  		}
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
   * This returns the final, implied slip rate for each segment
   */
  public double getFinalAveSegSlipRate(int ithSegment) {
	  ArbDiscrEmpiricalDistFunc segmenstSlipDist = getSegmentSlipDist(ithSegment);
	  double slipRate=0;
	  for(int i=0; i<segmenstSlipDist.getNum(); ++i)
		  slipRate+=segmenstSlipDist.getX(i)*segmenstSlipDist.getY(i);
	  return slipRate;
  }
  
  /**
   * Get rate of events for ith segment
   * 
   * @param ithSegment
   * @return
   */
  public double getSegmentRate(int ithSegment) {
	  return segRate[ithSegment];
  }
  
  /**
   * Get recurrence interval for the ith Segment
   * 
   * @param ithSegment
   * @return
   */
  public double getSegmentRecurrenceInterval(int ithSegment) {
	  return 1.0/getSegmentRate(ithSegment);
  }
  
  /**
   * Get the final Slip Distribution for the ith segment
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
	  return rupMeanMag[ithRup];
  }
  
  /**
   * Get the long name for ith Rup (the segment names combined)
   * @param ithRup
   * @return
   */
  public String getRupNameLong(int ithRup) {
	  return rupNameLong[ithRup];
  }
  
  
  /**
   * Get the short name for ith Rup (segment numbers combined; e.g., "123" is the
   * rupture that involves segments 1, 2, and 3).
   * @param ithRup
   * @return
   */
  public String getRupNameShort(int ithRup) {
	  return rupNameShort[ithRup];
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
   * This computes the WG02 increase/decrease factor for the ave slip on a segment relative to the
   * ave slip for the entire rupture (based on moment rates and areas).  The idea being, 
   * for example, that if only full fault rupture is allowed on a fuult where the segments 
   * have different slip rates, then the amount of slip on each segment for that rupture
   * must vary to match the long-term slip rates).
   * @param segAveSlipRate
   */
  private double[][] getWG02_SegRupSlipFactor() {
	  double[][] segRupSlipFactor = new double[num_rup][num_seg];
	  for(int rup=0; rup<num_rup; ++rup) {
		  double totMoRate = 0;
		  double totArea = 0;
		  for(int seg=0; seg<num_seg; seg++) {
			  if(rupInSeg[seg][rup]==1) {
				  totMoRate += segmentData.getSegmentMomentRate(seg);
				  totArea += segmentData.getSegmentArea(seg);
			  }
		  }
		  for(int seg=0; seg<num_seg; seg++) {
			  segRupSlipFactor[rup][seg] = rupInSeg[seg][rup]*segmentData.getSegmentMomentRate(seg)*totArea/(totMoRate*segmentData.getSegmentArea(seg));
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
   * Compute the rate for all segments (segRate[]).
   *  
    */
  private void computeSegRates() {
	  segRate = new double[num_seg];
	  for(int seg=0; seg<num_seg; ++seg) {
		  segRate[seg]=0.0;
		  // Sum the rates of all ruptures which are part of a segment
		  for(int rup=0; rup<num_rup; rup++) 
			  if(rupInSeg[seg][rup]==1) segRate[seg]+=totRupRate[rup];
	  }
  }
  

	
	/**
	 * Get the rupture names based on segment numbers & names
	 */
	private void getRuptureNames() {
		String[] rupNameShort = new String[num_rup];
		String[] rupNameLong = new String[num_rup];
		for(int rup=0; rup<num_rup; rup++){
			boolean isFirst = true;
			for(int seg=0; seg < num_seg; seg++) {
				if(rupInSeg[seg][rup]==1) { // if this rupture is included in this segment
					if(isFirst) { // append the section name to rupture name
						rupNameShort[rup] = ""+(seg+1);
						rupNameLong[rup] = segmentData.getSegmentName(seg);
						isFirst = false;
					} else {
						rupNameShort[rup] += (seg+1);
						rupNameLong[rup] += "; "+segmentData.getSegmentName(seg);
					}
				}
			}
		}
	}
	


	  /**
	   * compute rupArea
	   */
	private void getRupAreas() {
  		rupArea = new double[num_rup];
		for(int rup=0; rup<num_rup; rup++){
	    		rupArea[rup] = 0;
	    		for(int seg=0; seg < num_seg; seg++) {
	    			if(rupInSeg[seg][rup]==1) { // if this rupture is included in this segment	
	    				rupArea[rup] += segmentData.getSegmentArea(seg);
	    			}
	    		}
	    }
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
	  
	  A_FaultSource.getRupInSegMatrix(2);
	  System.out.println(" ");
	  A_FaultSource.getRupInSegMatrix(3);
	  System.out.println(" ");
	  A_FaultSource.getRupInSegMatrix(4);
	  System.out.println(" ");
	  A_FaultSource.getRupInSegMatrix(5);
	  System.out.println(" ");
	  A_FaultSource.getRupInSegMatrix(6);
	  System.out.println(" ");
	  A_FaultSource.getRupInSegMatrix(7);
	  System.out.println(" ");
	  A_FaultSource.getRupInSegMatrix(8);
	  
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


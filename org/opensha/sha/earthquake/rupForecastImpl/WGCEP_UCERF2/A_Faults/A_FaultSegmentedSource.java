package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.surface.EvenlyGriddedSurface;
import org.opensha.data.*;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.calc.*;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.*;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.EqkRateModel2_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.data.A_FaultsFetcher;
import org.opensha.sha.surface.*;
import org.opensha.sha.magdist.*;
import org.opensha.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.WC1994_MagAreaRelationship;

import cj.math.nnls.NNLSWrapper;

import sun.tools.tree.ThisExpression;

/**
 * <p>Title: A_FaultSource </p>
 * <p>Description: This has been verified as follows: 1) The constructor for non-characteristic slip
 * (the general, inverse solution) gives the same results as the constructor for char slip; 2) the final
 * segment slip rates are matched for all solution types; 3) the correct mag-areas are obtained where a
 * mag-area relationship is used (seen in a GUI).
 * 
 * To Do: Matlab inversion tests
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field
 * @date Sept, 2003
 * @version 1.0
 */


public class A_FaultSegmentedSource extends ProbEqkSource {
	
	//for Debug purposes
	private static String C = new String("A_FaultSource");
	private final static boolean D = false;
	private final static boolean MATLAB_TEST = false;
	
	//name for this classs
	protected String NAME = "Type-A Fault Source";
	
	protected double duration;
	
	private ArrayList ruptureList; 
	private ArrayList faultCornerLocations = new ArrayList(); // used for the getMinDistance(Site) method
	
	private int num_seg, num_rup;
	
	// x-axis attributes for the MagFreqDists
	private final static double MIN_MAG = EqkRateModel2_ERF.MIN_MAG;
	private final static double MAX_MAG = EqkRateModel2_ERF.MAX_MAG;
	private final static double DELTA_MAG = EqkRateModel2_ERF.DELTA_MAG;
	private final static int NUM_MAG = EqkRateModel2_ERF.NUM_MAG;
	
	private double magSigma, magTruncLevel;

	
	// OLD: slip model: 0 = Characteristic; 1 = Uniform/Boxcar; 2 = WGCEP-2002
	private String slipModelType;
	public final static String CHAR_SLIP_MODEL = "Characteristic (Dsr=Ds)";
	public final static String UNIFORM_SLIP_MODEL = "Uniform/Boxcar (Dsr=Dr)";
	public final static String WG02_SLIP_MODEL = "WGCEP-2002 model (Dsr prop to Vs)";
	public final static String TAPERED_SLIP_MODEL = "Tapered Ends (????)";
	
	// for rounding magnitudes
	private final static double ROUND_MAG_TO = 0.01;
	
	private final static double TOLERANCE = 1e6;
	
	private int[][] rupInSeg;
	private double[][] segSlipInRup;
	
	private FaultSegmentData segmentData;
	
	private ArbDiscrEmpiricalDistFunc[] segSlipDist;  // segment slip dist
	private ArbitrarilyDiscretizedFunc[] rupSlipDist;
	
	private double[] finalSegRate, segRateFromApriori;
	
	private String[] rupNameShort, rupNameLong;
	private double[] rupArea, rupMeanMag, rupMoRate, rupRate, totRupRate; // rupture mean mag
	private IncrementalMagFreqDist[] rupMagFreqDist; // MFD for rupture
	
	private SummedMagFreqDist summedMagFreqDist;
	private double totalMoRateFromSegments, totalMoRateFromRups;
	
	private ValueWeight[] aPrioriRupRates;
	
	// NNLS inversion solver - static to save time and memory
	private static NNLSWrapper nnls = new NNLSWrapper();

	
	/**
	 * This constructor is for a model that assumed characteristic slip for computing mags and
	 * applies the rates of each rupture given in aPrioriRupRates (ignoring the weights since they are
	 * useless here). Note that the segment MRIs implied by the a-priori rates may not be consistent
	 * with those in the FaultSegmentData; the former are used.
	 * If sigma is ~zero, then the magnitude in the MFD is rounded to the nearest discrete mag value 
	 * (moment is preserved; and the mag returned by getRupMeanMag is not rounded).
	 */
	public A_FaultSegmentedSource(FaultSegmentData segmentData, ValueWeight[] aPrioriRupRates,
			double magSigma, double magTruncLevel) {
		
		this.aPrioriRupRates = aPrioriRupRates;
		this.segmentData = segmentData;
		this.isPoissonian = true;
		this.magSigma = magSigma;
		this.magTruncLevel = magTruncLevel;
		
		num_seg = segmentData.getNumSegments();
		
		// get the RupInSeg Matrix for the given number of segments
		if(segmentData.getFaultName().equals("San Jacinto")) {
			rupInSeg = getSanJacintoRupInSeg();	// special case for this branching fault
			num_rup = 25;
		}
		else {
			rupInSeg = getRupInSegMatrix(num_seg);
			num_rup = getNumRuptures(segmentData);
		}
	
		// do some checks
		if(num_rup != aPrioriRupRates.length)
			throw new RuntimeException("Error: number of ruptures is incompatible with number of elements in aPrioriRupRates");
		
		// make the name strings
		rupNameShort = getAllShortRuptureNames(segmentData);
		rupNameLong = getAllLongRuptureNames(segmentData);
		
		getRupAreas();
		
		// get rates on each segment implied by a-priori rates
		// (which might be different from what's in FaultSegmentData) 
		// this one is used to compute char mags
		computeSegRatesFromAprioriRates();

		getRupMeanMagsAssumingCharSlip();
		
		// Make MFD for each rupture & the total sum
		totRupRate = new double[num_rup];
		rupMoRate = new double[num_rup];
		totalMoRateFromRups = 0.0;
		summedMagFreqDist = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
		// this boolean tells us if there is only one non-zero mag in the MFD
		boolean singleMag = (magSigma*magTruncLevel < DELTA_MAG/2);
		rupMagFreqDist = new GaussianMagFreqDist[num_rup];
		double mag;
		for(int i=0; i<num_rup; ++i) {
			// we conserve moment rate (final rupture rates will differ from a-priori
			// rates due to finite mag sigma or rounding if that sigma=0)
			rupMoRate[i] = aPrioriRupRates[i].getValue() * MomentMagCalc.getMoment(rupMeanMag[i]);
			totalMoRateFromRups+=rupMoRate[i];
			// round the magnitude if need be
			if(singleMag)
				mag = Math.round(rupMeanMag[i]/DELTA_MAG) * DELTA_MAG;
			else
				mag = rupMeanMag[i];
			rupMagFreqDist[i] = new GaussianMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG, 
					mag, magSigma, rupMoRate[i], magTruncLevel, 2);
			summedMagFreqDist.addIncrementalMagFreqDist(rupMagFreqDist[i]);
			totRupRate[i] = rupMagFreqDist[i].getTotalIncrRate();
//if(this.segmentData.getFaultName().equals("N. San Andreas"))
//				System.out.println(i+"  "+mag+"  "+MomentMagCalc.getMoment(rupMeanMag[i])+"  "+
//						aPrioriRupRates[i].getValue()+"  "+rupMoRate[i]+"  "+this.getLongRupName(i));

		}

		
		// add info to the summed dist
		/**/
		String summed_info = "\n\nMoment Rate: "+(float) getTotalMoRateFromSummedMFD() +
		"\n\nTotal Rate: "+(float)summedMagFreqDist.getCumRate(0);
		summedMagFreqDist.setInfo(summed_info);
		
		
		// get final rate of events on each segment (this takes into account mag rounding of MFDs)
		computeFinalSegRates();		

		// find the slip distribution for each rupture (rupSlipDist{})
		computeRupSlipDist();
		
		// find the slip distribution of each segment
		//computeSegSlipDist(rupSlipDist, magAreaRel, segRupSlipFactor);
		/*
		if(D) {
			// print the slip distribution of each segment
			for(int i=0; i<num_seg; ++i) {
				System.out.println("Slip for segment "+i+":");
				System.out.println(segSlipDist[i]);
			}
		}
		*/
	}
	
	
	
	
	/**
	 * Description:
	 * 
	 * @param segmentData - SegmentedFaultData, where it is assumed that these are in proper order such 
	 * that concatenating the FaultTraces will produce a total FaultTrace with locations in the proper order.
	 * This constructor is a work in progress - for the full blown inversion model.
	 * @param magAreaRel - any MagAreaRelationship
	 * @
	 */
	public A_FaultSegmentedSource(FaultSegmentData segmentData, MagAreaRelationship magAreaRel, 
			String slipModelType, ValueWeight[] aPrioriRupRates, double magSigma, double magTruncLevel) {
		
		this.segmentData = segmentData;
		this.slipModelType = slipModelType;
		this.aPrioriRupRates = aPrioriRupRates;
		
		this.isPoissonian = true;
		
		num_seg = segmentData.getNumSegments();
		
		// get the RupInSeg Matrix for the given number of segments
		if(segmentData.getFaultName().equals("San Jacinto")) {
			rupInSeg = getSanJacintoRupInSeg();	// special case for this branching fault
			num_rup = 25;
		}
		else {
			rupInSeg = getRupInSegMatrix(num_seg);
			num_rup = getNumRuptures(segmentData);
		}
	

		// do some checks
		if(num_rup != aPrioriRupRates.length)
			throw new RuntimeException("Error: number of ruptures is incompatible with number of elements in aPrioriRupRates");

		rupNameShort = getAllShortRuptureNames(segmentData);
		rupNameLong = getAllLongRuptureNames(segmentData);
		
		getRupAreas();
		
		// get rates on each segment implied by a-priori rates
		// (which might be different from what's in FaultSegmentData) 
		// this one is used to compute char mags
		computeSegRatesFromAprioriRates();

		// compute rupture mean mags
		if(slipModelType.equals(CHAR_SLIP_MODEL))
			getRupMeanMagsAssumingCharSlip();
		else {
			// compute from mag-area relationship
			rupMeanMag = new double[num_rup];
			for(int rup=0; rup <num_rup; rup++)
				rupMeanMag[rup] = magAreaRel.getMedianMag(rupArea[rup]/1e6);
		}
		
		// compute matrix of Dsr (slip on each segment in each rupture)
		getSegSlipInRupMatrix();
		
		// now solve the inverse problem
		double[][] C = new double[num_seg+num_rup][num_rup];
		double[] d = new double[num_seg+num_rup];  // the data vector
		double wt = 1000;
		// first fill in the slip-rate constraints with wt
		// I'm dividing by wt on second set because only this approach converges in Matlab
		for(int row = 0; row < num_seg; row ++) {
			d[row] = segmentData.getSegmentSlipRate(row);
			for(int col=0; col<num_rup; col++)
				C[row][col] = segSlipInRup[row][col];
		}
		// now fill in the a-priori rates
		for(int rup=0; rup < num_rup; rup++) {
			d[rup+num_seg] = aPrioriRupRates[rup].getValue()/wt;
			C[rup+num_seg][rup]=1.0/wt;
		}
		if(MATLAB_TEST) {
			// remove white space in name for Matlab
			StringTokenizer st = new StringTokenizer(segmentData.getFaultName());
			String tempName = st.nextToken();;
			while(st.hasMoreTokens())
				tempName += "_"+st.nextToken();
			System.out.println("display "+tempName);
		}
		double[] rupRate = getNNLS_solution(C, d);
		
		
//		System.out.println("NNLS rates:");
//		for(int rup=0; rup < rupRate.length; rup++)
//			System.out.println((float) rupRate[rup]);
		
		
		if(D) {
//			 check slip rates to make sure they match exactly
			double tempSlipRate;
			System.out.println("Check of segment slip rates for "+segmentData.getFaultName()+":");
			for(int seg=0; seg < num_seg; seg++) {
				tempSlipRate = 0;
				for(int rup=0; rup < num_rup; rup++)
					tempSlipRate += rupRate[rup]*segSlipInRup[seg][rup];
				System.out.println(seg+"  "+(float) tempSlipRate+"  "+
						(float)segmentData.getSegmentSlipRate(seg)+"  "+
						(float)(tempSlipRate/segmentData.getSegmentSlipRate(seg)));
			}
		}
		
		// Make MFD for each rupture & the total sum
		totRupRate = new double[num_rup];
		rupMoRate = new double[num_rup];
		totalMoRateFromRups = 0.0;
		summedMagFreqDist = new SummedMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
		// this boolean tells us if there is only one non-zero mag in the MFD
		boolean singleMag = (magSigma*magTruncLevel < DELTA_MAG/2);
		rupMagFreqDist = new GaussianMagFreqDist[num_rup];
		double mag;
		for(int i=0; i<num_rup; ++i) {
			// we conserve moment rate (final rupture rates will differ from rupRate[] 
			// due to finite mag sigma or rounding if that sigma=0)
			rupMoRate[i] = rupRate[i] * MomentMagCalc.getMoment(rupMeanMag[i]);
			totalMoRateFromRups+=rupMoRate[i];
			// round the magnitude if need be
			if(singleMag)
				mag = Math.round(rupMeanMag[i]/DELTA_MAG) * DELTA_MAG;
			else
				mag = rupMeanMag[i];
			rupMagFreqDist[i] = new GaussianMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG, 
					mag, magSigma, rupMoRate[i], magTruncLevel, 2);
			summedMagFreqDist.addIncrementalMagFreqDist(rupMagFreqDist[i]);
			totRupRate[i] = rupMagFreqDist[i].getTotalIncrRate();
		}
		// add info to the summed dist
		/**/
		String summed_info = "\n\nMoment Rate: "+(float) getTotalMoRateFromSummedMFD() +
		"\n\nTotal Rate: "+(float)summedMagFreqDist.getCumRate(0);
		summedMagFreqDist.setInfo(summed_info);
		
		
		// get final rate of events on each segment (this takes into account mag rounding of MFDs)
		computeFinalSegRates();		

		// find the slip distribution for each rupture (rupSlipDist{})
		computeRupSlipDist();
		
		// find the slip distribution of each segment
		//computeSegSlipDist(rupSlipDist, magAreaRel, segRupSlipFactor);
		/*
		if(D) {
			// print the slip distribution of each segment
			for(int i=0; i<num_seg; ++i) {
				System.out.println("Slip for segment "+i+":");
				System.out.println(segSlipDist[i]);
			}
		}
		*/
	}
	
	
	public final static ArrayList getSupportedSlipModels() {
		ArrayList models = new ArrayList();
		models.add(CHAR_SLIP_MODEL);
		models.add(UNIFORM_SLIP_MODEL);
		models.add(WG02_SLIP_MODEL);
//		models.add(TAPERED_SLIP_MODEL);

		return models;
	}
	
	/**
	 * Return the fault segment data
	 * 
	 * @return
	 */
	public FaultSegmentData getFaultSegmentData() {
		return this.segmentData;
	}
	
	/**
	 * This computes rupture magnitudes assuming characteristic slip (not an M(A) relationship)
	 *
	 */
	private void getRupMeanMagsAssumingCharSlip() {
		rupMeanMag = new double[num_rup];
		//double[] testSegRates = new double[num_seg];
		double[] rupMo = new double[num_rup];
		// first compute the total moment for each rupture
		double area, slip;
		for(int rup=0; rup<num_rup; rup++){
			for(int seg=0; seg < num_seg; seg++) {
				if(rupInSeg[seg][rup]==1) { // if this rupture is included in this segment	
					area = segmentData.getSegmentArea(seg);
					slip = segmentData.getSegmentSlipRate(seg)/segRateFromApriori[seg];
					rupMo[rup] += area*slip*FaultMomentCalc.SHEAR_MODULUS;
					//testSegRates[seg] += aPrioriRupRates[rup].getValue();
//if(this.segmentData.getFaultName().equals("N. San Andreas"))
//	System.out.println(rup+"  "+seg+"  "+area+"  "+slip+"  "+segmentData.getSegmentSlipRate(seg)+"  "+segmentData.getRecurInterval(seg));
				}
			}
			rupMeanMag[rup] = MomentMagCalc.getMag(rupMo[rup]);
//if(this.segmentData.getFaultName().equals("Garlock"))
//	System.out.println(rup+"  "+rupMo[rup]+"  "+rupMeanMag[rup]);

		}
		/*if(this.segmentData.getFaultName().equals("N. San Andreas"))
			for(int seg=0; seg < num_seg; seg++) {
				System.out.println(testSegRates[seg]+"  ,  "+1/testSegRates[seg]);
			}*/
/*
		// now convert moment to mag
		for(int rup=0; rup<num_rup; rup++){
			// compute magnitude (rounded to nearest MFD x-axis point if magSigma=0)
			//if(magSigma == 0)
				rupMeanMag[rup] = Math.round(MomentMagCalc.getMag(rupMo[rup])/DELTA_MAG) * DELTA_MAG;
			//else
				//rupMeanMag[rup] = Math.round(MomentMagCalc.getMag(rupMo[rup])/ROUND_MAG_TO) * ROUND_MAG_TO;
		}
*/
	}
	
	
	private void getRupRates() {
		
		// solve for the following:
		rupMoRate = new double[num_rup];
		totRupRate = new double[num_rup];  // if rupture given an MFD
		
		// NNLSWrapper nnls = new NNLSWrapper();
		// we need to solve Xf=d, where f is the rupture rate vector
		
		// first compute the rupture mags and slips
		// slipModelType: 0 = Characteristic; 1 = Uniform/Boxcar; 2 = WGCEP-2002
		// rupModelSolutionType: 0 = Min Rate; 1 = Max Rate; 2 = Equal Rate; 3 = Geol. Insight
	
	}
	
	
	private final static int[][] getRupInSegMatrix(int num_seg) {
		
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
		if(D) {
			for(int seg = 0; seg < num_seg; seg+=1) {
				System.out.print("\n");
				for(int rup = 0; rup < num_rup; rup += 1)
					System.out.print(rupInSeg[seg][rup]+"  ");
			}
			System.out.print("\n");
		}
		
		return rupInSeg;
	}
	
	
	private final static int[][] getSanJacintoRupInSeg() {
		int num_seg = 7;
		int num_rup = 25;
		int[][] sjfRupInSeg = {
				  // 1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5
					{1,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,1,1,0,0,1,0,1}, // seg 1
					{0,1,0,0,0,0,0,1,1,0,0,0,0,1,1,1,0,0,1,1,1,0,1,1,1}, // seg 2
					{0,0,1,0,0,0,0,0,1,1,1,0,0,1,1,1,1,0,1,1,1,1,1,1,1}, // seg 3
					{0,0,0,1,0,0,0,0,0,1,0,0,0,0,1,0,0,0,1,0,0,0,0,0,1}, // seg 4
					{0,0,0,0,1,0,0,0,0,0,1,1,0,0,0,1,1,1,0,1,1,1,1,1,1}, // seg 5
					{0,0,0,0,0,1,0,0,0,0,0,1,1,0,0,0,1,1,0,0,1,1,1,1,1}, // seg 6
					{0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,1,0,0,0,1,0,1,1}, // seg 7
			  	};
		
		// check result
		if(D) {
			for(int seg = 0; seg < num_seg; seg+=1) {
				System.out.print("\n");
				for(int rup = 0; rup < num_rup; rup += 1)
					System.out.print(sjfRupInSeg[seg][rup]+"  ");
			}
		}
		
		return sjfRupInSeg;
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
	 * Get rupture moment rate of the ith char rupture
	 * 
	 * @param ithRup
	 * @return
	 */
	public double getRupMoRate(int ithRup) {
		return rupMoRate[ithRup];
	}
	
	/**
	 * Get a priori rupture rate of the ith char rupture
	 * 
	 * @param ithRup
	 * @return
	 */
	public double getAPrioriRupRate(int ithRup) {
		return aPrioriRupRates[ithRup].getValue();
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
	 * Get final rate of events for ith segment
	 * 
	 * @param ithSegment
	 * @return
	 */
	public double getSegRateFromAprioriRates(int ithSegment) {
		return segRateFromApriori[ithSegment];
	}
	
	/**
	 * Get final rate of events for ith segment
	 * 
	 * @param ithSegment
	 * @return
	 */
	public double getFinalSegmentRate(int ithSegment) {
		return finalSegRate[ithSegment];
	}
	
	/**
	 * Get recurrence interval for the ith Segment
	 * 
	 * @param ithSegment
	 * @return
	 */
	public double getFinalSegRecurInt(int ithSegment) {
		return 1.0/getFinalSegmentRate(ithSegment);
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
	 * Get area for ith Rupture
	 * @param ithRup
	 * @return
	 */
	public double getRupArea(int ithRup) {
		return rupArea[ithRup];
	}
	
	
	/**
	 * Get the long name for ith Rup (the segment names combined)
	 * @param ithRup
	 * @return
	 */
	public String getLongRupName(int ithRup) {
		return rupNameLong[ithRup];
	}
	
	
	/**
	 * Get the short name for ith Rup (segment numbers combined; e.g., "123" is the
	 * rupture that involves segments 1, 2, and 3).
	 * @param ithRup
	 * @return
	 */
	public String getShortRupName(int ithRup) {
		return rupNameShort[ithRup];
	}
	
	
	/**
	 * Compute the slip distribution for each segment
	 * The average slip for each event is partitioned among the different segments
	 * according to segRupSlipFactor.
	 */
	private void computeSegSlipDist(double[][] segRupSlipFactor) {
		segSlipDist = new ArbDiscrEmpiricalDistFunc[num_seg];
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
	 * This creates the segSlipInRup (Dsr) matrix based on the value of slipModelType.
	 * This slips are in meters.
	 *
	 */
	private void getSegSlipInRupMatrix() {
		segSlipInRup = new double[num_seg][num_rup];
		
		// for case segment slip is independent of rupture, and equal to slip-rate * MRI
		if(slipModelType.equals(CHAR_SLIP_MODEL)) {
			for(int seg=0; seg<num_seg; seg++) {
				double segCharSlip = segmentData.getSegmentSlipRate(seg)/segRateFromApriori[seg];
				for(int rup=0; rup<num_rup; ++rup) {
					segSlipInRup[seg][rup] = rupInSeg[seg][rup]*segCharSlip;
				}
			}

		}
		// for case where ave slip computed from mag & area, and is same on all segments 
		else if (slipModelType.equals(UNIFORM_SLIP_MODEL)) {
			for(int rup=0; rup<num_rup; ++rup) {
				double aveSlip = MomentMagCalc.getMoment(rupMeanMag[rup])/(rupArea[rup]*FaultMomentCalc.SHEAR_MODULUS);
				for(int seg=0; seg<num_seg; seg++) {
					segSlipInRup[seg][rup] = rupInSeg[seg][rup]*aveSlip;
				}
			}
		}
		// this is the model where seg slip is proportional to segment slip rate 
		// (bumped up or down based on ratio of seg slip rate over wt-ave slip rate (where wts are seg areas)
		else if (slipModelType.equals(WG02_SLIP_MODEL)) {
			for(int rup=0; rup<num_rup; ++rup) {
				double aveSlip = MomentMagCalc.getMoment(rupMeanMag[rup])/(rupArea[rup]*FaultMomentCalc.SHEAR_MODULUS);
				double totMoRate = 0;	// a proxi for slip-rate times area
				double totArea = 0;
				for(int seg=0; seg<num_seg; seg++) {
					if(rupInSeg[seg][rup]==1) {
						totMoRate += segmentData.getSegmentMomentRate(seg); // a proxi for Vs*As
						totArea += segmentData.getSegmentArea(seg);
					}
				}
				for(int seg=0; seg<num_seg; seg++) {
					segSlipInRup[seg][rup] = aveSlip*rupInSeg[seg][rup]*segmentData.getSegmentMomentRate(seg)*totArea/(totMoRate*segmentData.getSegmentArea(seg));
				}
			}
		}
		else if (slipModelType.equals(TAPERED_SLIP_MODEL)) {
			throw new RuntimeException(TAPERED_SLIP_MODEL + " is not yet supported");
		}
		else throw new RuntimeException("slip model not supported");
	}
	
	/*
	 * This computes the WG02 increase/decrease factor for the ave slip on a segment relative to the
	 * ave slip for the entire rupture (based on moment rates and areas).  The idea being, 
	 * for example, that if only full fault rupture is allowed on a fuult where the segments 
	 * have different slip rates, then the amount of slip on each segment for that rupture
	 * must vary to match the long-term slip rates).
	 * @param segAveSlipRate
	 
	private double[][] getWG02_SegRupSlipFactor() {
		double[][] segRupSlipFactor = new double[num_rup][num_seg];
		for(int rup=0; rup<num_rup; ++rup) {
			double totMoRate = 0;
			double totArea = 0;
			for(int seg=0; seg<num_seg; seg++) {
				if(rupInSeg[seg][rup]==1) {
					totMoRate += segmentData.getSegmentMomentRate(seg); // this is a proxi for Vs*As
					totArea += segmentData.getSegmentArea(seg);
				}
			}
			for(int seg=0; seg<num_seg; seg++) {
				segRupSlipFactor[rup][seg] = rupInSeg[seg][rup]*segmentData.getSegmentMomentRate(seg)*totArea/(totMoRate*segmentData.getSegmentArea(seg));
			}
		}
		return segRupSlipFactor;
	}
	*/
	
	/**
	 * This computes the rate of discrete ave-slips for each rupture (rupSlipDist) 
	 * by converting the x-axis magnitudes (of the rupture MFD) to slip amounts.
	 * 
	 */
	private void computeRupSlipDist() {
		rupSlipDist = new ArbitrarilyDiscretizedFunc[num_rup];
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
	 * Compute the rate for all segments (segRate[]) by summing totRupRate[rup] over all ruptures
	 * that involve each segment.
	 *  
	 */
	private void computeFinalSegRates() {
		finalSegRate = new double[num_seg];
		for(int seg=0; seg<num_seg; ++seg) {
			finalSegRate[seg]=0.0;
			// Sum the rates of all ruptures which are part of a segment
			for(int rup=0; rup<num_rup; rup++) 
				if(rupInSeg[seg][rup]==1) finalSegRate[seg]+=totRupRate[rup];
		}
	}
	
	
	/**
	 * Compute the rate of each segment from the a priori rupture rates 
	 * (e.g., used to check whether segments a priori rates are consistent with
	 * the seg rates in FaultSegmentData).
	 *  
	 */
	private void computeSegRatesFromAprioriRates() {
		segRateFromApriori = new double[num_seg];
		for(int seg=0; seg<num_seg; ++seg) {
			segRateFromApriori[seg]=0.0;
			// Sum the rates of all ruptures which are part of a segment
			for(int rup=0; rup<num_rup; rup++) 
				if(rupInSeg[seg][rup]==1) segRateFromApriori[seg]+=aPrioriRupRates[rup].getValue();
		}
	}
	
	/**
	 * This gives an array of short names for each rupture (this is static so that GUIs can get this info
	 * without having to instantiate the object).  The short names are defined as the combination of
	 * segment numbers involved in the rupture (e.g., "23" is the rupture that involves segments 2 and 3).
	 * Here, segment indices start at 1 (not 0).
	 */
	public final static String[] getAllShortRuptureNames(FaultSegmentData segmentData) {
		int nSeg = segmentData.getNumSegments();
		int nRup = getNumRuptures(segmentData);
		int[][] rupInSeg;
		// get the RupInSeg Matrix for the given number of segments
		if(segmentData.getFaultName().equals("San Jacinto"))
			rupInSeg = getSanJacintoRupInSeg();	// special case for this branching fault
		else
			rupInSeg = getRupInSegMatrix(nSeg);
		String[] rupNameShort = new String[nRup];
		for(int rup=0; rup<nRup; rup++){
			boolean isFirst = true;
			for(int seg=0; seg < nSeg; seg++) {
				if(rupInSeg[seg][rup]==1) { // if this rupture is included in this segment
					if(isFirst) { // append the section name to rupture name
						rupNameShort[rup] = ""+(seg+1);
						isFirst = false;
					} else {
						rupNameShort[rup] += (seg+1);
					}
				}
			}
		}
		return rupNameShort;
	}
	
	
	
	/**
	 * This gives an array of long names for each rupture (this is static so that GUIs can get this info
	 * without having to instantiate the object).  The long names are defined as the combination of
	 * segment names (combined with "; ").
	 */
	public final static String[] getAllLongRuptureNames(FaultSegmentData segmentData) {
		int nSeg = segmentData.getNumSegments();
		int nRup = getNumRuptures(segmentData);
		int[][] rupInSeg;
		// get the RupInSeg Matrix for the given number of segments
		if(segmentData.getFaultName().equals("San Jacinto"))
			rupInSeg = getSanJacintoRupInSeg();	// special case for this branching fault
		else
			rupInSeg = getRupInSegMatrix(nSeg);
		String[] rupNameLong = new String[nRup];
		for(int rup=0; rup<nRup; rup++){
			boolean isFirst = true;
			for(int seg=0; seg < nSeg; seg++) {
				if(rupInSeg[seg][rup]==1) { // if this rupture is included in this segment
					if(isFirst) { // append the section name to rupture name
						rupNameLong[rup] = segmentData.getSegmentName(seg);
						isFirst = false;
					} else {
						rupNameLong[rup] += "+"+segmentData.getSegmentName(seg);
					}
				}
			}
		}
		return rupNameLong;
	}
	
	
	
	/**
	 * compute rupArea (meters)
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
	 * This static method can be used (e.g., in GUIs) to get the number of ruptures
	 * without having to instantiate the object.
	 * @param segmentData
	 * @return
	 */
	public final static int getNumRuptures(FaultSegmentData segmentData) {
		int nSeg = segmentData.getNumSegments();
		if(segmentData.getFaultName().equals("San Jacinto"))
			return 25;
		else
			return nSeg*(nSeg+1)/2;
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
//		System.out.println(C+" minDist for source "+this.NAME+" = "+min);
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
	
	
	/**
	 * This gets the non-negative least squares solution more the matrix C
	 * and data vector d.
	 * @param C
	 * @param d
	 * @return
	 */
	private static double[] getNNLS_solution(double[][] C, double[] d) {

		int nRow = C.length;
		int nCol = C[0].length;
		
		double[] A = new double[nRow*nCol];
		double[] x = new double[nCol];
		
		int i,j,k=0;
	
		if(MATLAB_TEST) {
			System.out.println("C = [");
			for(i=0; i<nRow;i++) {
				for(j=0;j<nCol;j++) 
					System.out.print((float)C[i][j]+"   ");
				System.out.print("\n");
			}
			System.out.println("];");
			System.out.println("d = [");
			for(i=0; i<nRow;i++)
				System.out.println((float) d[i]);
			System.out.println("];");
		}
/////////////////////////////////////
		
		for(j=0;j<nCol;j++) 
			for(i=0; i<nRow;i++)	{
				A[k]=C[i][j];
				k+=1;
			}
		nnls.update(A,nRow,nCol);
		nnls.solve(d,x);
		
		if(MATLAB_TEST) {
			System.out.println("x = [");
			for(i=0; i<x.length;i++)
				System.out.println(x[i]);
			System.out.println("];");
			System.out.println("max(abs(x-lsqnonneg(C,d)))");
		}
		
		return x;
	}
	
	
	public static void main(String[] args) {
		
//		System.out.println("Starting - loading data");
		A_FaultsFetcher aFaultsFetcher = new A_FaultsFetcher();
		// Defomration model D2.1 ID =  42
		ArrayList aFaultSegmentData = aFaultsFetcher.getFaultSegmentDataList(42, true);
//		for(int i=0; i<aFaultSegmentData.size(); i++)
//			System.out.println(i+"  "+ ((FaultSegmentData)aFaultSegmentData.get(i)).getFaultName());
		FaultSegmentData segData = (FaultSegmentData) aFaultSegmentData.get(2);
//		System.out.println("Done getting data for "+segData.getFaultName());
		Ellsworth_B_WG02_MagAreaRel magAreaRel = new Ellsworth_B_WG02_MagAreaRel();
		ValueWeight[] aPrioriRates = aFaultsFetcher.getAprioriRupRates(segData.getFaultName(), 
				A_FaultsFetcher.GEOL_INSIGHT_RUP_MODEL);
//		System.out.println("now creating source");
		A_FaultSegmentedSource src = new A_FaultSegmentedSource(segData, magAreaRel,
				A_FaultSegmentedSource.WG02_SLIP_MODEL, aPrioriRates, 0.12, 2);
		
		
		/*
		double[][] C = {
		//_ rup  1,2,3,4,5,6,7,8,9,0,1,2,3,4,5
				{1,0,0,0,0,1,0,0,0,1,0,0,1,0,1},
				{0,1,0,0,0,1,1,0,0,1,1,0,1,1,1},
				{0,0,1,0,0,0,1,1,0,1,1,1,1,1,1},
				{0,0,0,1,0,0,0,1,1,0,1,1,1,1,1},
				{0,0,0,0,1,0,0,0,1,0,0,1,0,1,1},
		};
		final double[] d = {1/1.380,1/.250,1/.600,1/2.000,1/.9333333};  // AKA "b" vector		
		/*
		double[][] C = {
				{0.0372,0.2869},
			    {0.6861,0.7071},
			    {0.6233,0.6245},
			    {0.6344,0.6170},
			    };
		final double[] d = {0.8587,0.1781,0.0747,0.8405};  // AKA "b" vector		
		
		System.out.println("num rows  "+C.length);
		System.out.println("num cols  "+C[0].length);

		double[] x = getNNLS_solution(C,d);
		
		for(int i=0; i<x.length;i++)
			System.out.println(i+"  "+x[i]);
		*/

		
		/*
		A_FaultSegmentedSource.getSanJacintoRupInSeg();
		System.out.println(" ");
		A_FaultSegmentedSource.getRupInSegMatrix(2);
		System.out.println(" ");
		A_FaultSegmentedSource.getRupInSegMatrix(4);
		System.out.println(" ");
		A_FaultSegmentedSource.getRupInSegMatrix(6);
		System.out.println(" ");
		A_FaultSegmentedSource.getRupInSegMatrix(8);
		*/
		
	}
}


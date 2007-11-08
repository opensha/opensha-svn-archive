/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.rupCalc;

import java.util.ArrayList;
import java.util.StringTokenizer;

import nnls.NNLSWrapper;

import org.opensha.calc.FaultMomentCalc;
import org.opensha.calc.MomentMagCalc;
import org.opensha.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.data.ValueWeight;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelPrefDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.data.SegRateConstraint;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

/**
 * This class finds the subsections for S. San Andreas fault 
 * 
 * @author vipingupta
 *
 */
public class SoSAF_SubSectionInversion {
	
	private boolean D = true;
	private final static int MAX_SUBSECTION_LEN = 10;
	private DeformationModelPrefDataDB_DAO deformationModelPrefDB_DAO = new DeformationModelPrefDataDB_DAO(DB_AccessAPI.dbConnection);
	private ArrayList<FaultSectionPrefData> subSectionList;
	
	private int num_seg, num_rup;
	
	ArrayList<SegRateConstraint> segRateConstraints;
	
	// x-axis attributes for the MagFreqDists
//	private final static double MIN_MAG = UCERF2.MIN_MAG;
//	private final static double MAX_MAG = UCERF2.MAX_MAG;
//	private final static double DELTA_MAG = UCERF2.DELTA_MAG;
//	private final static int NUM_MAG = UCERF2.NUM_MAG;
	
	// this is used to filter out infrequent ruptures (approx age of earth)
//	private final static double MIN_RUP_RATE = 1e-10;
	
//	private double magSigma, magTruncLevel;
	
//	private boolean preserveMinAFaultRate;		// don't let any post inversion rates be below the minimum a-priori rate
//	private double minRates[]; // the minimum rate constraint
	private boolean wtedInversion;	// weight the inversion according to slip rate and segment rate uncertainties
	private double relativeSegRate_wt, aPrioriRupWt, minNonZeroAprioriRate;
//	public final static double MIN_A_PRIORI_ERROR = 1e-6;
	
	// slip model:
	private String slipModelType;
	public final static String CHAR_SLIP_MODEL = "Characteristic (Dsr=Ds)";
	public final static String UNIFORM_SLIP_MODEL = "Uniform/Boxcar (Dsr=Dr)";
	public final static String WG02_SLIP_MODEL = "WGCEP-2002 model (Dsr prop to Vs)";
	public final static String TAPERED_SLIP_MODEL = "Tapered Ends ([Sin(x)]^0.5)";
	
	private static EvenlyDiscretizedFunc taperedSlipPDF, taperedSlipCDF;
	
	private int[][] rupInSeg;
	private double[][] segSlipInRup;
	
//	private FaultSegmentData segmentData;
	
//	private ArbDiscrEmpiricalDistFunc[] segSlipDist;  // segment slip dist
//	private ArbitrarilyDiscretizedFunc[] rupSlipDist;
	
	private double[] finalSegRate, finalSegSlipRate;
//	private double[] segRateFromApriori, segRateFromAprioriWithMinRateConstr, aPrioriSegSlipRate;
	
	private String[] rupNameShort, rupNameLong;
	private double[] rupArea, rupMeanMag, rupMeanMo, rupMoRate, totRupRate, segArea, segSlipRate, segMoRate;
	double[] rupRateSolution; // these are the rates from the inversion (not total rate of MFD)
//	private IncrementalMagFreqDist[] rupMagFreqDist; // MFD for rupture
	
//	private SummedMagFreqDist summedMagFreqDist;
	private double totalMoRateFromRups;
	
//	private ValueWeight[] aPrioriRupRates;
	
	// the following is the total moment-rate reduction, including that which goes to the  
	// background, sfterslip, events smaller than the min mag here, and aftershocks and foreshocks.
	private double moRateReduction;  
	
	 // The following is the ratio of the average slip for the Gaussian MFD divided by the slip of the average magnitude.
	private double aveSlipCorr;
	private double meanMagCorrection;
	
	private MagAreaRelationship magAreaRel;
	
	// NNLS inversion solver - static to save time and memory
	private static NNLSWrapper nnls = new NNLSWrapper();

	// list of sources
//	private ArrayList<FaultRuptureSource> sourceList;
	
//	private final static double DEFAULT_GRID_SPACING = UCERF2.GRID_SPACING;
	
//	private Boolean isTimeDeptendent;
	
//	private double[] segProb, segGain, segAperiodicity, segTimeSinceLast, rupProb, rupGain;

	
	public SoSAF_SubSectionInversion() {
		
		// set slip model as one of: CHAR_SLIP_MODEL, UNIFORM_SLIP_MODEL, WG02_SLIP_MODEL, TAPERED_SLIP_MODEL
		slipModelType = TAPERED_SLIP_MODEL;
		
		// set the mag-area relationship
		magAreaRel = new HanksBakun2002_MagAreaRel();
		
		// chop the SSAF into many sub-sections
		computeAllSubsections();
		
		// get the RupInSeg Matrix for the given number of segments
		num_seg = subSectionList.size();
		rupInSeg = getRupInSegMatrix(num_seg);
		num_rup = num_seg*(num_seg+1)/2;
		if(D) System.out.println("num_seg="+num_seg+"; num_rup="+num_rup);
		
		// make short rupture names
		String[] rupNameShort = new String[num_rup];
		for(int rup=0; rup<num_rup; rup++){
			boolean isFirst = true;
			for(int seg=0; seg < num_seg; seg++) {
				if(rupInSeg[seg][rup]==1) { // if this rupture is included in this segment
					if(isFirst) { // append the section name to rupture name
						rupNameShort[rup] = ""+(seg+1);
						isFirst = false;
					} else {
						rupNameShort[rup] += "+"+(seg+1);
					}
				}
			}
//			if(D) System.out.println(rup+"\t"+rupNameShort[rup]);
		}

		// compute rupture areas
		computeSegAndRupStuff();
			
		// compute aveSlipCorr (ave slip is greater than slip of ave mag if MFD sigma non zero)
//		setAveSlipCorrection();
//		System.out.println("AVE ratio: "+ aveSlipCorr);
		aveSlipCorr=1;
			
		// correction to what's given by M(A) relationship (added epistemic uncertianty)
		meanMagCorrection=0;
		
		// compute rupture mean mags
		if(slipModelType.equals(CHAR_SLIP_MODEL))
			// getRupMeanMagsAssumingCharSlip();
			throw new RuntimeException(CHAR_SLIP_MODEL+" is not yet supported");
		else {
			// compute from mag-area relationship
			rupMeanMag = new double[num_rup];
			rupMeanMo = new double[num_rup];
			for(int rup=0; rup <num_rup; rup++) {
				rupMeanMag[rup] = magAreaRel.getMedianMag(rupArea[rup]/1e6) + meanMagCorrection;
				rupMeanMo[rup] = aveSlipCorr*MomentMagCalc.getMoment(rupMeanMag[rup]);   // increased if magSigma >0
			}
		}


	}
	
	
	public void doInversion() {

		// compute matrix of Dsr (slip on each segment in each rupture)
		computeSegSlipInRupMatrix();
			
			
		// NOW SOLVE THE INVERSE PROBLEM
					
		// get the segment rate constraints
		computeSegRateConstraints();
		int numRateConstraints = segRateConstraints.size();
		/*

			// set number of rows as one for each slip-rate/segment (the minimum)
			int totNumRows = num_seg;
			// add segment rate constrains if needed
			if(relativeSegRate_wt > 0.0)	totNumRows += numRateConstraints;
			// add a-priori rate constrains if needed
			if(aPrioriRupWt > 0.0)  totNumRows += num_rup;
			
			int numRowsBeforeSegRateData = num_seg;
			if(aPrioriRupWt > 0.0) numRowsBeforeSegRateData += num_rup;
			
			double[][] C = new double[totNumRows][num_rup];
			double[] d = new double[totNumRows];  // the data vector
			
			// CREATE THE MODEL AND DATA MATRICES
			// first fill in the slip-rate constraints
			for(int row = 0; row < num_seg; row ++) {
				d[row] = segSlipRate[row]*(1-moRateReduction);
				for(int col=0; col<num_rup; col++)
					C[row][col] = segSlipInRup[row][col];
			}
			// now fill in the a-priori rates if needed
			if(aPrioriRupWt > 0.0) {
				for(int rup=0; rup < num_rup; rup++) {
					d[rup+num_seg] = aPrioriRupRates[rup].getValue();
					C[rup+num_seg][rup]=1.0;
				}
			}
			// now fill in the segment recurrence interval constraints if requested
			if(relativeSegRate_wt > 0.0) {
				SegRateConstraint constraint;
				for(int row = 0; row < numRateConstraints; row ++) {
					constraint = segRateConstraints.get(row);
					int seg = constraint.getSegIndex();
					d[row+numRowsBeforeSegRateData] = constraint.getMean(); // this is the average segment rate
					for(int col=0; col<num_rup; col++)
						C[row+numRowsBeforeSegRateData][col] = rupInSeg[seg][col];
				}
			}
			
			// CORRECT IF MINIMUM RATE CONSTRAINT DESIRED
			double[] Cmin = new double[totNumRows];  // the data vector
			// correct the data vector
			for(int row=0; row <totNumRows; row++) {
				for(int col=0; col < num_rup; col++)
					Cmin[row]+=minRates[col]*C[row][col];
				d[row] -= Cmin[row];
			}

			// APPLY DATA WEIGHTS IF DESIRED
			if(wtedInversion) {
				double data_wt;
				for(int row = 0; row < num_seg; row ++) {
//					data_wt = Math.pow((1-moRateReduction)*segmentData.getSegSlipRateStdDev(row), -2);
					data_wt = 1/((1-moRateReduction)*segmentData.getSegSlipRateStdDev(row));
					d[row] *= data_wt;
					for(int col=0; col<num_rup; col++)
						C[row][col] *= data_wt;
				}
				// now fill in the segment recurrence interval constraints if requested
				if(relativeSegRate_wt > 0.0) {
					SegRateConstraint constraint;
					for(int row = 0; row < numRateConstraints; row ++) {
						constraint = segRateConstraints.get(row);
//						data_wt = Math.pow(constraint.getStdDevOfMean(), -2);
						data_wt = 1/constraint.getStdDevOfMean();
						d[row+numRowsBeforeSegRateData] *= data_wt; // this is the average segment rate
						for(int col=0; col<num_rup; col++)
							C[row+numRowsBeforeSegRateData][col] *= data_wt;
					}
				}
			}
			
			// APPLY EQUATION-SET WEIGHTS
			// for the a-priori rates:
			if(aPrioriRupWt > 0.0) {
				double wt;
				for(int rup=0; rup < num_rup; rup++) {
					if(aPrioriRupRates[rup].getValue() > 0)
						wt = aPrioriRupWt/aPrioriRupRates[rup].getValue();
					else
						wt = aPrioriRupWt/minNonZeroAprioriRate; // make it the same as for smallest non-zero rate
//						wt = MIN_A_PRIORI_ERROR;

					// Hard code special constraints
//					if(this.segmentData.getFaultName().equals("N. San Andreas") && rup==9) wt = 1e10/aPrioriRupRates[rup].getValue();
					if(this.segmentData.getFaultName().equals("San Jacinto") && rup==3) wt = 1e10/minNonZeroAprioriRate;			
						
//					wt = aPrioriRupWt;
					d[rup+num_seg] *= wt;
					C[rup+num_seg][rup] *= wt;
				}
			}
			// for the segment recurrence interval constraints if requested:
			if(relativeSegRate_wt > 0.0) {
				for(int row = 0; row < numRateConstraints; row ++) {
					d[row+numRowsBeforeSegRateData] *= relativeSegRate_wt;
					for(int col=0; col<num_rup; col++)
						C[row+numRowsBeforeSegRateData][col] *= relativeSegRate_wt;
				}
			}

			// manual check of matrices
			if(segmentData.getFaultName().equals("Elsinore")) {
				System.out.println("Elsinore");
				int nRow = C.length;
				int nCol = C[0].length;
				System.out.println("C = [");
				for(int i=0; i<nRow;i++) {
					for(int j=0;j<nCol;j++) 
						System.out.print(C[i][j]+"   ");
					System.out.print("\n");
				}
				System.out.println("];");
				System.out.println("d = [");
				for(int i=0; i<nRow;i++)
					System.out.println(d[i]);
				System.out.println("];");
			}

			
			if(MATLAB_TEST) {
				// remove white space in name for Matlab
				StringTokenizer st = new StringTokenizer(segmentData.getFaultName());
				String tempName = st.nextToken();;
				while(st.hasMoreTokens())
					tempName += "_"+st.nextToken();
				System.out.println("display "+tempName);
			}
			
			
			// SOLVE THE INVERSE PROBLEM
			rupRateSolution = getNNLS_solution(C, d);
			
			
			// CORRECT FINAL RATES IF MINIMUM RATE CONSTRAINT APPLIED
			for(int rup=0; rup<num_rup;rup++)
				rupRateSolution[rup] += minRates[rup];
			
//			System.out.println("NNLS rates:");
//			for(int rup=0; rup < rupRate.length; rup++)
//				System.out.println((float) rupRateSolution[rup]);
			

			if(D) {
//				 check slip rates to make sure they match exactly
				double tempSlipRate;
				//System.out.println("Check of segment slip rates for "+segmentData.getFaultName()+":");
				for(int seg=0; seg < num_seg; seg++) {
					tempSlipRate = 0;
					for(int rup=0; rup < num_rup; rup++)
						tempSlipRate += rupRateSolution[rup]*segSlipInRup[seg][rup];
					double absFractDiff = Math.abs(tempSlipRate/(segmentData.getSegmentSlipRate(seg)*(1-this.moRateReduction)) - 1.0);				System.out.println("SlipRateCheck:  "+(float) (tempSlipRate/(segmentData.getSegmentSlipRate(seg)*(1-this.moRateReduction))));
					if(absFractDiff > 0.001)
						throw new RuntimeException("ERROR - slip rates differ!!!!!!!!!!!!");
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
				// we conserve moment rate exactly (final rupture rates will differ from rupRateSolution[] 
				// due to MFD discretization or rounding if magSigma=0)
				rupMoRate[i] = rupRateSolution[i] * rupMeanMo[i];
				totalMoRateFromRups+=rupMoRate[i];
				// round the magnitude if need be
				if(singleMag)
					mag = Math.round((rupMeanMag[i]-MIN_MAG)/DELTA_MAG) * DELTA_MAG + MIN_MAG;
				else
					mag = rupMeanMag[i];
				rupMagFreqDist[i] = new GaussianMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG, 
						mag, magSigma, rupMoRate[i], magTruncLevel, 2);
							
				summedMagFreqDist.addIncrementalMagFreqDist(rupMagFreqDist[i]);
				totRupRate[i] = rupMagFreqDist[i].getTotalIncrRate();
				
	//double testRate = rupMoRate[i]/MomentMagCalc.getMoment(rupMeanMag[i]);
	//System.out.println((float)(testRate/rupRateSolution[i]));
	// if(((String)getLongRupName(i)).equals("W")) System.out.print(totRupRate[i]+"  "+rupRateSolution[i]);
			}
			// add info to the summed dist
			String summed_info = "\n\nMoment Rate: "+(float) getTotalMoRateFromSummedMFD() +
			"\n\nTotal Rate: "+(float)summedMagFreqDist.getCumRate(0);
			summedMagFreqDist.setInfo(summed_info);
			
			// Computer final segment slip rate
			computeFinalSegSlipRate();
			
			// get final rate of events on each segment (this takes into account mag rounding of MFDs)
			computeFinalSegRates();		

			// find the slip distribution for each rupture (rupSlipDist{})
			computeRupSlipDist();
*/
	}
	
	/**
	 * Get a list of all subsections
	 * 
	 * @return
	 */
	private void computeAllSubsections() {
		/** Choose a deformation model
		 * D2.1 = 82
		 * D2.2 = 83
		 * D2.3 = 84
		 * D2.4 = 85
		 * D2.5 = 86
		 * D2.6 = 87
		 */
		int deformationModelId = 82; //
		
		/*32:San Andreas (Parkfield)
		285:San Andreas (Cholame) rev
		300:San Andreas (Carrizo) rev
		287:San Andreas (Big Bend)
		286:San Andreas (Mojave N)
		301:San Andreas (Mojave S)
		282:San Andreas (San Bernardino N)
		283:San Andreas (San Bernardino S)
		284:San Andreas (San Gorgonio Pass-Garnet HIll)
		295:San Andreas (Coachella) rev*/
		ArrayList<Integer> faultSectionIds = new ArrayList<Integer>();
		faultSectionIds.add(32);
		faultSectionIds.add(285);
		faultSectionIds.add(300);
		faultSectionIds.add(287);
		faultSectionIds.add(286);
		faultSectionIds.add(301);
		faultSectionIds.add(282);
		faultSectionIds.add(283);
		faultSectionIds.add(284);
		faultSectionIds.add(295);
		
		subSectionList = new ArrayList<FaultSectionPrefData>();		
		for(int i=0; i<faultSectionIds.size(); ++i) {
			FaultSectionPrefData faultSectionPrefData = 
				deformationModelPrefDB_DAO.getFaultSectionPrefData(deformationModelId, faultSectionIds.get(i));
			subSectionList.addAll(faultSectionPrefData.getSubSectionsList(this.MAX_SUBSECTION_LEN));
		}
	}

	
	
	/**
	 * Get a list of all subsections
	 * 
	 * @return
	 */
	public ArrayList<FaultSectionPrefData> getAllSubsections() {
		return this.subSectionList;
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
		/*
		if(D) {
			for(int seg = 0; seg < num_seg; seg+=1) {
				System.out.print("\n");
				for(int rup = 0; rup < num_rup; rup += 1)
					System.out.print(rupInSeg[seg][rup]+"  ");
			}
			System.out.print("\n");
		}
		*/
		
		return rupInSeg;
	}
	
	/**
	 * compute rupArea (meters)
	 */
	private void computeSegAndRupStuff() {
		rupArea = new double[num_rup];
		segArea = new double[num_seg];
		segSlipRate = new double[num_seg];
		segMoRate = new double[num_seg];
		FaultSectionPrefData segData;
		for(int seg=0; seg < num_seg; seg++) {
				segData = subSectionList.get(seg);
				segArea[seg] += segData.getDownDipWidth()*segData.getLength(); // note this ignores aseismicity!
				segSlipRate[seg] = segData.getAveLongTermSlipRate();
//				if(D) System.out.println(seg+" slip rate = "+segSlipRate[seg]);
				segMoRate[seg] = FaultMomentCalc.getMoment(segArea[seg]*1e6, segSlipRate[seg]*1e-3); // area km-->m; slipRate mm/yr --> m/yr
		}

		for(int rup=0; rup<num_rup; rup++){
			rupArea[rup] = 0;
			for(int seg=0; seg < num_seg; seg++) {
				if(rupInSeg[seg][rup]==1) { // if this rupture is included in this segment	
					segData = subSectionList.get(seg);
					rupArea[rup] += segData.getDownDipWidth()*segData.getLength(); // note this ignores aseismicity!
				}
			}
		}
	}
	
	
	/**
	 * This creates the segSlipInRup (Dsr) matrix based on the value of slipModelType.
	 * This slips are in meters.
	 *
	 */
	private void computeSegSlipInRupMatrix() {
		segSlipInRup = new double[num_seg][num_rup];
		FaultSectionPrefData segData;
		
		// for case segment slip is independent of rupture, and equal to slip-rate * MRI
		// note that we're using the event rates that include the min constraint (segRateFromAprioriWithMinRateConstr)
		if(slipModelType.equals(CHAR_SLIP_MODEL)) {
			throw new RuntimeException(CHAR_SLIP_MODEL+ " not yet supported");
			/*
			for(int seg=0; seg<num_seg; seg++) {
				double segCharSlip = segmentData.getSegmentSlipRate(seg)*(1-moRateReduction)/segRateFromAprioriWithMinRateConstr[seg];
				for(int rup=0; rup<num_rup; ++rup) {
					segSlipInRup[seg][rup] = rupInSeg[seg][rup]*segCharSlip;
				}
			}
			*/
		}
		// for case where ave slip computed from mag & area, and is same on all segments 
		else if (slipModelType.equals(UNIFORM_SLIP_MODEL)) {
			for(int rup=0; rup<num_rup; ++rup) {
				double aveSlip = rupMeanMo[rup]/(rupArea[rup]*FaultMomentCalc.SHEAR_MODULUS);  // inlcudes aveSlipCorr
				for(int seg=0; seg<num_seg; seg++) {
					segSlipInRup[seg][rup] = rupInSeg[seg][rup]*aveSlip;
				}
			}
		}
		// this is the model where seg slip is proportional to segment slip rate 
		// (bumped up or down based on ratio of seg slip rate over wt-ave slip rate (where wts are seg areas)
		else if (slipModelType.equals(WG02_SLIP_MODEL)) {
			for(int rup=0; rup<num_rup; ++rup) {
				double aveSlip = rupMeanMo[rup]/(rupArea[rup]*FaultMomentCalc.SHEAR_MODULUS);    // inlcudes aveSlipCorr
				double totMoRate = 0;	// a proxi for slip-rate times area
				double totArea = 0;
				for(int seg=0; seg<num_seg; seg++) {
					if(rupInSeg[seg][rup]==1) {
						totMoRate += segMoRate[seg]; // a proxi for Vs*As
						totArea += segArea[seg];
					}
				}
				for(int seg=0; seg<num_seg; seg++) {
					segSlipInRup[seg][rup] = aveSlip*rupInSeg[seg][rup]*segMoRate[seg]*totArea/(totMoRate*segArea[seg]);
				}
			}
		}
		else if (slipModelType.equals(TAPERED_SLIP_MODEL)) {
			// note that the ave slip is partitioned by area, not length; this is so the final model is moment balanced.
			mkTaperedSlipFuncs();
			for(int rup=0; rup<num_rup; ++rup) {
				double aveSlip = rupMeanMo[rup]/(rupArea[rup]*FaultMomentCalc.SHEAR_MODULUS);    // inlcudes aveSlipCorr

				double normBegin=0, normEnd, scaleFactor;
				for(int seg=0; seg<num_seg; seg++) {
					if(rupInSeg[seg][rup]==1) {
						normEnd = normBegin + segArea[seg]/rupArea[rup];
						// fix normEnd values that are just past 1.0
						if(normEnd > 1 && normEnd < 1.00001) normEnd = 1.0;
						scaleFactor = taperedSlipCDF.getInterpolatedY(normEnd)-taperedSlipCDF.getInterpolatedY(normBegin);
						scaleFactor /= (normEnd-normBegin);
						segSlipInRup[seg][rup] = aveSlip*scaleFactor;
						normBegin = normEnd;
					}
				}
				if(D) { // check results
					double d_aveTest=0;
					for(int seg=0; seg<num_seg; seg++)
						d_aveTest += segSlipInRup[seg][rup]*segArea[seg]/rupArea[rup];
					System.out.println("AveSlipCheck: " + (float) (d_aveTest/aveSlip));
				}
			}
		}
		else throw new RuntimeException("slip model not supported");
	}
	
	
	/**
	 * This makes a tapered slip function based on the [Sin(x)]^0.5 fit of 
	 * Biasi & Weldon (in prep; pesonal communication), which is based on  
	 * the data comilation of Biasi & Weldon (2006, "Estimating Surface  
	 * Rupture Length and Magnitude of Paleoearthquakes from Point 
	 * Measurements of Rupture Displacement", Bull. Seism. Soc. Am. 96, 
	 * 1612-1623, doi: 10.1785/0120040172 E)
	 *
	 */
	private static void mkTaperedSlipFuncs() {
		
		// only do if another instance has not already done this
		if(taperedSlipCDF != null) return;
		
		taperedSlipCDF = new EvenlyDiscretizedFunc(0, 51, 0.02);
		taperedSlipPDF = new EvenlyDiscretizedFunc(0, 51, 0.02);
		double x,y, sum=0;
		int num = taperedSlipPDF.getNum();
		for(int i=0; i<num;i++) {
			x = taperedSlipPDF.getX(i);
			// y = Math.sqrt(1-(x-0.5)*(x-0.5)/0.25);
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
//				System.out.println(taperedSlipCDF.getX(i)+"\t"+taperedSlipPDF.getY(i)+"\t"+taperedSlipCDF.getY(i));
		}
	}

	
	// This gets the seg-rate constraints by associating locations from Appendix C to those sub-sections created here
	private void computeSegRateConstraints() {
		
	}
	
	
	
	/**
	 * It gets all the subsections for SoSAF and prints them on console
	 * @param args
	 */
	public static void main(String []args) {
		SoSAF_SubSectionInversion soSAF_SubSections = new  SoSAF_SubSectionInversion();
		ArrayList<FaultSectionPrefData> subsectionList = soSAF_SubSections.getAllSubsections();
		for(int i=0; i<subsectionList.size(); ++i) {
			FaultSectionPrefData subSection = subsectionList.get(i);
//			System.out.println(i+"\t"+subSection.getSectionName()+"\t"+(float)subSection.getLength());
//			System.out.println(subSection.getFaultTrace());
		}
	}

}

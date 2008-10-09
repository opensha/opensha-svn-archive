/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.rupCalc;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import nnls.NNLSWrapper;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.calc.FaultMomentCalc;
import org.opensha.calc.MomentMagCalc;
import org.opensha.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.data.Location;
import org.opensha.data.ValueWeight;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelPrefDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.EventRates;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.SegRateConstraint;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

/**
 * This class does an inversion for the rate of events in an unsegmented model:
 * 
 * TO DO:
 * 
 * 3) sample MRIs via monte carlo simulations (same for slip rates?)
 * 4) subsection to 5 km?
 *
 */
public class SoSAF_SubSectionInversion {
	private final static String SEG_RATE_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_Final/data/Appendix_C_Table7_091807.xls";

	private boolean D = true;
	private DeformationModelPrefDataDB_DAO deformationModelPrefDB_DAO = new DeformationModelPrefDataDB_DAO(DB_AccessAPI.dbConnection);
	private ArrayList<FaultSectionPrefData> subSectionList;
	
	private int num_seg, num_rup;
	
	private int[] numSegInRup, firstSegOfRup;
	private SummedMagFreqDist aveOfSegPartMFDs;
	
	private boolean transitionAseisAtEnds = true;

	private   int maxSubsectionLength;
	private int numSegForSmallestRups;  // this sets the number of segments for the smallest ruptures (either 1 or 2 for now).. e.g., if subsections are ~5km, then we want at least two rupturing at once.

	ArrayList<SegRateConstraint> segRateConstraints;
	
	int[] numSubSections;  // this contains the number of subsections for each section
	
	// a-priori rate constraints
	int[] aPriori_rupIndex;
	double[] aPriori_rate, aPriori_wt;
	
	private static boolean MATLAB_TEST = false;
	double[][] C_wted, C;
	double[] d, d_wted, data_wt, full_wt, d_pred;  // the data vector
	
	private double minRates[]; // the minimum rate constraint for each rupture
	private double minRupRate;
	private boolean wtedInversion;	// weight the inversion according to slip rate and segment rate uncertainties
	private double relativeSegRateWt, relative_aPrioriRupWt, relative_smoothnessWt;
	
	private int  firstRowSegSlipRateData=-1,firstRowSegEventRateData=-1, firstRowAprioriData=-1, firstRowSmoothnessData=-1;
	private int  lastRowSegSlipRateData=-1,lastRowSegEventRateData=-1, lastRowAprioriData=-1, lastRowSmoothnessData=-1;
	private int totNumRows;
	
	// slip model:
	private String slipModelType;
	public final static String CHAR_SLIP_MODEL = "Characteristic (Dsr=Ds)";
	public final static String UNIFORM_SLIP_MODEL = "Uniform/Boxcar (Dsr=Dr)";
	public final static String WG02_SLIP_MODEL = "WGCEP-2002 model (Dsr prop to Vs)";
	public final static String TAPERED_SLIP_MODEL = "Tapered Ends ([Sin(x)]^0.5)";
	
	private static EvenlyDiscretizedFunc taperedSlipPDF, taperedSlipCDF;
	
	SummedMagFreqDist magFreqDist, smoothedMagFreqDist;
	
	ArrayList<SummedMagFreqDist> segmentNucleationMFDs;
	ArrayList<SummedMagFreqDist> segmentParticipationMFDs;
	
	
	private int[][] rupInSeg;
	private double[][] segSlipInRup;
	
	private double[] finalSegEventRate, finalSegSlipRate;
	private double[] segSlipRateResids, segEventRateResids;
	
	private String[] rupNameShort;
	private double[] rupArea, rupMeanMag, rupMeanMo, rupMoRate, totRupRate, segArea, segSlipRate, segSlipRateStdDev, segMoRate, rateOfRupEndsOnSeg;
	double[] rupRateSolution; // these are the rates from the inversion (not total rate of MFD)
	double totMoRate;
	
	// the following is the total moment-rate reduction, including that which goes to the  
	// background, sfterslip, events smaller than the min mag here, and aftershocks and foreshocks.
	private double moRateReduction;  
	
	private MagAreaRelationship magAreaRel;
	
	// NNLS inversion solver - static to save time and memory
	private static NNLSWrapper nnls = new NNLSWrapper();

	
	public SoSAF_SubSectionInversion(int maxSubsectionLength, int numSegForSmallestRups) {
		
		this.maxSubsectionLength = maxSubsectionLength;
		this.numSegForSmallestRups = numSegForSmallestRups;
		
		if (numSegForSmallestRups != 1 &&  numSegForSmallestRups != 2)
			throw new RuntimeException("Error: numSegForSmallestRups must be 1 or 2!");
		
		// chop the SSAF into many sub-sections
		computeAllSubsections();
		
		
		if(transitionAseisAtEnds)
			transitionAseisAtEnds();
		
		transitionSlipRateAtEnds();
		
		smoothSlipRates(5);
		
		// get the RupInSeg Matrix for the given number of segments
		num_seg = subSectionList.size();
		rupInSeg = getRupInSegMatrix(num_seg);
		num_rup = rupInSeg[1].length;
		if(D) System.out.println("num_seg="+num_seg+"; num_rup="+num_rup);
		
		// make short rupture names
		rupNameShort = new String[num_rup];
		for(int rup=0; rup<num_rup; rup++){
			boolean isFirst = true;
			for(int seg=0; seg < num_seg; seg++) {
				if(rupInSeg[seg][rup]==1) { // if this rupture is included in this segment
					if(isFirst) { // append the section name to rupture name
						rupNameShort[rup] = ""+(seg);
						isFirst = false;
					} else {
						rupNameShort[rup] += "+"+(seg);
					}
				}
			}
//			if(D) System.out.println(rup+"\t"+rupNameShort[rup]);
		}

		// compute rupture areas
		computeInitialStuff();
		
		// create the segRateConstraints
		getSegRateConstraints();
		
		System.out.println("Parkfield rup index = "+getParkfieldRuptureIndex());
		
	}
	
	
	/**
	 * Write Short Rup names to a file
	 * 
	 * @param fileName
	 */
	public void writeRupNamesToFile(String fileName) {
		try{
			FileWriter fw = new FileWriter("org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_Final/rupCalc/"+fileName);
			fw.write("rup_index\trupNameShort\n");
			for(int i=0; i<rupNameShort.length; ++i)
				fw.write(i+"\t"+rupNameShort[i]+"\n");
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Write Short Rup names to a file
	 * 
	 * @param fileName
	 */
	public void writeSegPartMFDsDataToFile(String fileName) {
		try{
			FileWriter fw = new FileWriter("org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_Final/rupCalc/"+fileName);
			fw.write("seg_index\tmag\tpart_rate\n");
			for(int s=0; s<segmentParticipationMFDs.size(); s++) {
				SummedMagFreqDist mfd = segmentParticipationMFDs.get(s);
				for(int m=0; m<mfd.getNum();m++)
					fw.write(s+"\t"+(float)mfd.getX(m)+"\t"+(float)Math.log(mfd.getY(m))+"\n");
			}	
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * 
	 * @param slipModelType
	 * @param magAreaRel
	 * @param relativeSegRateWt
	 * @param relative_aPrioriRupWt
	 * @param relative_smoothnessWt
	 * @param wtedInversion
	 * @param minRupRate - force all rup rates to be greater than this value
	 */
	public void doInversion(String slipModelType, MagAreaRelationship magAreaRel, double relativeSegRateWt, 
			double relative_aPrioriRupWt, double relative_smoothnessWt, boolean wtedInversion, double minRupRate) {
		
		this.slipModelType = slipModelType;
		this.magAreaRel = magAreaRel;
		this.relativeSegRateWt = relativeSegRateWt;
		this.relative_aPrioriRupWt = relative_aPrioriRupWt;
		this.wtedInversion = wtedInversion;
		this.relative_smoothnessWt = relative_smoothnessWt;
		this.minRupRate = minRupRate;

		// hard coded moment-rate reduction
		moRateReduction=0;
				
		// compute rupture mean mags
		if(slipModelType.equals(CHAR_SLIP_MODEL))
			// getRupMeanMagsAssumingCharSlip();
			throw new RuntimeException(CHAR_SLIP_MODEL+" is not yet supported");
		else {
			// compute from mag-area relationship
			rupMeanMag = new double[num_rup];
			rupMeanMo = new double[num_rup];
			for(int rup=0; rup <num_rup; rup++) {
				rupMeanMag[rup] = magAreaRel.getMedianMag(rupArea[rup]/1e6);
				rupMeanMo[rup] = MomentMagCalc.getMoment(rupMeanMag[rup]);   // increased if magSigma >0
			}
		}
		
		// set the a-priori rup rates & wts now that mags are set
		setAprioriRateData();


		// compute matrix of Dsr (slip on each segment in each rupture)
		computeSegSlipInRupMatrix();
					
		// get the number of segment rate constraints
		int numRateConstraints = segRateConstraints.size();
		
		// get the number of a-priori rate constraints
		int num_aPriori_constraints = aPriori_rupIndex.length;
		
		// set the minimum rupture rate constraints
		if(minRupRate >0.0) {
			minRates = new double[num_rup]; // this sets them all to zero
			for(int rup=0; rup<num_rup; rup++) minRates[rup] = minRupRate;			
		}

		// SET NUMBER OF ROWS AND IMPORTANT INDICES
		
		// segment slip-rates always used (for now)
		firstRowSegSlipRateData = 0;
		totNumRows = num_seg;
		lastRowSegSlipRateData = totNumRows-1;
		
		// add segment rate constrains if needed
		if(relativeSegRateWt > 0.0) {
			firstRowSegEventRateData = totNumRows;
			totNumRows += numRateConstraints;
			lastRowSegEventRateData = totNumRows-1;
		}
		
		// add a-priori rate constrains if needed
		if(relative_aPrioriRupWt > 0.0) {
			firstRowAprioriData  = totNumRows;
			totNumRows += num_aPriori_constraints;
			lastRowAprioriData = totNumRows-1;
		}
		
		// add number of smoothness constraints
		if(relative_smoothnessWt > 0) {
			firstRowSmoothnessData=totNumRows;
			if(numSegForSmallestRups==1)
				totNumRows += num_rup-num_seg;
			else // the case where numSegForSmallestRups=2
				totNumRows += num_rup-(num_seg-1);
			lastRowSmoothnessData = totNumRows-1;
		}
		
		System.out.println("firstRowSegEventRateData="+firstRowSegEventRateData+
				"\nfirstRowAprioriData="+firstRowAprioriData+
				"\nfirstRowSmoothnessData="+firstRowSmoothnessData+
				"\ntotNumRows="+totNumRows);
			
		C = new double[totNumRows][num_rup];
		d = new double[totNumRows];  // data vector
		C_wted = new double[totNumRows][num_rup]; // wted version
		d_wted = new double[totNumRows];  // wted data vector

		data_wt = new double[totNumRows];  // data weights
		full_wt = new double[totNumRows];  // data weights
		d_pred = new double[totNumRows];  // predicted data vector
		
		// initialize wts to 1.0
		for(int i=0;i<data_wt.length;i++)  data_wt[i]=1.0;
			
		// CREATE THE MODEL AND DATA MATRICES
		// first fill in the slip-rate constraints & wts
		for(int row = firstRowSegSlipRateData; row <= lastRowSegSlipRateData; row ++) {
			d[row] = segSlipRate[row]*(1-moRateReduction);
			if(wtedInversion)
				data_wt[row] = 1/((1-moRateReduction)*segSlipRateStdDev[row]);
			for(int col=0; col<num_rup; col++)
				C[row][col] = segSlipInRup[row][col];
		}
		// now fill in the segment event rate constraints if requested
		if(relativeSegRateWt > 0.0) {
			SegRateConstraint constraint;
			for(int i = 0; i < numRateConstraints; i ++) {
				constraint = segRateConstraints.get(i);
				int seg = constraint.getSegIndex();
				int row = i+firstRowSegEventRateData;
				d[row] = constraint.getMean(); // this is the average sub-section rate
				if(wtedInversion)
					data_wt[row] = 1/constraint.getStdDevOfMean();
				for(int col=0; col<num_rup; col++)
					C[row][col] = rupInSeg[seg][col];
			}
		}
		// now fill in the a-priori rates if needed
		if(relative_aPrioriRupWt > 0.0) {
			for(int i=0; i < num_aPriori_constraints; i++) {
				int row = i+firstRowAprioriData;
				int col = aPriori_rupIndex[i];
				d[row] = aPriori_rate[i];
				if(wtedInversion)
					data_wt[row] = aPriori_wt[i];
				C[row][col]=1.0;
			}
		}
		// add the smoothness constraint
		if(relative_smoothnessWt > 0.0) {
			int row = firstRowSmoothnessData;
			int counter = 0;
			for(int rup=0; rup < num_rup; rup++) {
//				if(rup==0) System.out.println("row="+row+"\trup="+rup+"\tcounter="+counter);
				// check to see if the last segment is used by the rupture (skip this last rupture if so)
				if(rupInSeg[num_seg-1][rup] != 1) {
//					System.out.println("row="+row+"\trup="+rup+"\tcounter="+counter);
					d[row] = 0;
					C[row][rup]=1.0;
					C[row][rup+1]=-1.0;
					row += 1;
					counter +=1;
//					num_smooth_constrints += 1;
				}
//				else
//					System.out.println("REJECT: row="+row+"\trup="+rup+"\tcounter="+counter);
			}
		}
//		System.out.println("num_smooth_constrints="+num_smooth_constrints);
		
		// copy un-wted data to wted versions (wts added below)
		for(int row=0;row<totNumRows; row++){
			d_wted[row] = d[row];
			for(int col=0;col<num_rup; col++)
				C_wted[row][col] = C[row][col];
		}
			
		

		// CORRECT IF MINIMUM RATE CONSTRAINT DESIRED
		if(minRupRate >0.0) {
			double[] Cmin = new double[totNumRows];  // the data vector
			// correct the data vector
			for(int row=0; row <totNumRows; row++) {
				for(int col=0; col < num_rup; col++)
					Cmin[row]+=minRates[col]*C_wted[row][col];
				d_wted[row] -= Cmin[row];
			}
		}
		
		// APPLY WEIGHTS

		// segment slip rates first (no equation-set weight because others are relative)
		for(int row = firstRowSegSlipRateData; row <= lastRowSegSlipRateData; row ++) {
			if(wtedInversion) 
				full_wt[row] = data_wt[row];
			else
				full_wt[row] = 1.0;
			d_wted[row] *= full_wt[row];
			for(int col=0; col<num_rup; col++)
				C_wted[row][col] *= full_wt[row];
		}
		// segment event rate wts
		if(relativeSegRateWt > 0.0) {
			for(int i = 0; i < numRateConstraints; i ++) {
				int row = i+firstRowSegEventRateData;
				full_wt[row] = relativeSegRateWt;
				if(wtedInversion) full_wt[row] *= data_wt[row];
				d_wted[row] *= full_wt[row];
				for(int col=0; col<num_rup; col++)
					C_wted[row][col] *= full_wt[row];
			}
		}
		// a-priori rate wts
		if(relative_aPrioriRupWt > 0.0) {
			for(int i=0; i < num_aPriori_constraints; i++) {
				int row = i+firstRowAprioriData;
				int col = aPriori_rupIndex[i];
				full_wt[row] = relative_aPrioriRupWt;
				if(wtedInversion) full_wt[row] *= data_wt[row];
				d_wted[row] *= full_wt[row];
				C_wted[row][col]=full_wt[row];
			}
		}
		// smoothness constraint wts
		if(relative_smoothnessWt > 0.0) {
			int row = firstRowSmoothnessData;
			for(int rup=0; rup < num_rup; rup++) {
				// check to see if the last segment is used by the rupture (skip this last rupture if so)
				if(rupInSeg[num_seg-1][rup] != 1) {
					full_wt[row] = relative_smoothnessWt;
					d_wted[row] *= full_wt[row];
					C_wted[row][rup] *= full_wt[row];
					C_wted[row][rup+1] *= full_wt[row];
					row += 1;
//					num_smooth_constrints += 1;
				}
			}
		}


		
//		for(int row=0;row<totNumRows; row++)
//			System.out.println(row+"\t"+(float)d[row]);

/*
		// manual check of matrices
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
*/

		// SOLVE THE INVERSE PROBLEM
		rupRateSolution = getNNLS_solution(C_wted, d_wted);

		// CORRECT FINAL RATES IF MINIMUM RATE CONSTRAINT APPLIED
		if(minRupRate >0.0)
			for(int rup=0; rup<num_rup;rup++) rupRateSolution[rup] += minRates[rup];

		
		// compute predicted data
		for(int row=0;row<totNumRows; row++)
			for(int col=0; col <num_rup; col++)
				d_pred[row] += rupRateSolution[col]*C[row][col];
				
		// Compute final segment slip rates and event rates
		computeFinalStuff();
		
		computeSegMFDs();
		
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
		
		/*
		32:San Andreas (Parkfield)
		285:San Andreas (Cholame) rev
		300:San Andreas (Carrizo) rev
		287:San Andreas (Big Bend)
		286:San Andreas (Mojave N)
		301:San Andreas (Mojave S)
		282:San Andreas (San Bernardino N)
		283:San Andreas (San Bernardino S)
		284:San Andreas (San Gorgonio Pass-Garnet HIll)
		295:San Andreas (Coachella) rev
		*/
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
		
		// this will store the number of subsections for the ith section in the list
		numSubSections = new int[faultSectionIds.size()];
		
		subSectionList = new ArrayList<FaultSectionPrefData>();	
		int lastNum=0;
		for(int i=0; i<faultSectionIds.size(); ++i) {
			FaultSectionPrefData faultSectionPrefData = 
				deformationModelPrefDB_DAO.getFaultSectionPrefData(deformationModelId, faultSectionIds.get(i));
			subSectionList.addAll(faultSectionPrefData.getSubSectionsList(this.maxSubsectionLength));
			// compute & write the number of subsections for this section
			numSubSections[i] = subSectionList.size()-lastNum;
// System.out.println(faultSectionPrefData.getSectionName()+"\t"+numSubSections[i]);
			lastNum = subSectionList.size();
		}
		
	}
	
	
	private void transitionAseisAtEnds() {
		FaultSectionPrefData segData;
		
		/**/
		// transition aseismicity factors for Parkfield sections
		// the math here represents a linear trend intersecting the zero value in the subsection just after 
		// the last one here, plus the constraint the the total ave value equal the original

		int numSubSectForParkfield=numSubSections[0]; // Coachella
		double origAseis = subSectionList.get(0).getAseismicSlipFactor();  // the value is currently the same for all subsections
		double sumOfIndex = 0;
		for(int i=0; i<numSubSectForParkfield;i++) sumOfIndex += i;
		double slope = (origAseis-1)/(sumOfIndex/numSubSectForParkfield - numSubSectForParkfield);
		double intercept = 1-slope*numSubSectForParkfield;
		for(int i=0; i<numSubSectForParkfield;i++) {
			segData = subSectionList.get(i);
			segData.setAseismicSlipFactor(slope*(numSubSectForParkfield-1-i)+intercept);
		}
		// check values
		double totProd=0, totArea=0;
		for(int seg=0; seg < numSubSectForParkfield; seg++) {
			segData = subSectionList.get(seg);
			totArea += segData.getLength()*segData.getDownDipWidth();
			totProd += segData.getLength()*segData.getDownDipWidth()*segData.getAseismicSlipFactor();
//			System.out.println(seg+"\t"+(float)segData.getAveLongTermSlipRate()+"\t"+(float)segData.getAseismicSlipFactor()+
//					"\t"+(float)segData.getLength()+ "\t"+(float)segData.getDownDipWidth());
		}
		System.out.println("Check on orig and final aseis for Parkfield (values should be equal): "+(float)origAseis+"\t"+(float)(totProd/totArea));
		

		
		// transition aseismicity factors for Coachella sections
		// the math here represents a linear trend from the zero value in the subsection just before 
		// the first one here, plus the constraint the the total ave value equal the original
		int totNumSubSections = subSectionList.size();
		int numSubSectForCoachella=numSubSections[numSubSections.length-1]; // Coachella
		origAseis = subSectionList.get(totNumSubSections-1).getAseismicSlipFactor();  // the value is currently the same for all subsections
		sumOfIndex = 0;
		for(int i=0; i<numSubSectForCoachella;i++) sumOfIndex += i;
		slope = origAseis/(sumOfIndex/numSubSectForCoachella +1);
		intercept = slope;
		for(int i=totNumSubSections-numSubSectForCoachella; i<totNumSubSections;i++) {
			segData = subSectionList.get(i);
			segData.setAseismicSlipFactor(slope*(i-totNumSubSections+numSubSectForCoachella)+intercept);
		}
		// check values
		totProd=0; totArea=0;
		for(int seg=totNumSubSections-numSubSectForCoachella; seg < totNumSubSections; seg++) {
			segData = subSectionList.get(seg);
			totArea += segData.getLength()*segData.getDownDipWidth();
			totProd += segData.getLength()*segData.getDownDipWidth()*segData.getAseismicSlipFactor();
//			System.out.println(seg+"\t"+(float)segData.getAveLongTermSlipRate()+"\t"+(float)segData.getAseismicSlipFactor()+
//					"\t"+(float)segData.getLength()+ "\t"+(float)segData.getDownDipWidth());
		}
		System.out.println("Check on orig and final aseis for Coachella (values should be equal): "+(float)origAseis+"\t"+(float)(totProd/totArea));

		
		/* write out segment data 
		for(int seg=0; seg < subSectionList.size(); seg++) {
			segData = subSectionList.get(seg);
			System.out.println(seg+"\t"+(float)segData.getAveLongTermSlipRate()+"\t"+(float)segData.getAseismicSlipFactor()+
					"\t"+(float)segData.getLength()+ "\t"+(float)segData.getDownDipWidth());
		}
		*/
	}

	
	
	
	
	/**
	 * linearly transition the slip rates at the ends of the fault.  This goes from the max slip rate 
	 * at the most inner subsection point to a value of slipRate/N at the last end point, where N is the
	 * number of points in the subsection.
	 */
	private void transitionSlipRateAtEnds() {
		FaultSectionPrefData segData;
		
		// write out the original data
		/*
		for(int seg=0; seg < subSectionList.size(); seg++) {
			segData = subSectionList.get(seg);
			System.out.println(seg+"\t"+(float)segData.getAveLongTermSlipRate()+"\t"+(float)segData.getAseismicSlipFactor()+
					"\t"+(float)segData.getLength()+ "\t"+(float)segData.getDownDipWidth());
		}
		*/

		int numSubSectForParkfield=numSubSections[0]; // Coachella
		double origSlipRate = subSectionList.get(0).getAveLongTermSlipRate();  // the value is currently the same for all subsections
		for(int i=0; i<numSubSectForParkfield;i++) {
			segData = subSectionList.get(i);
			segData.setAveLongTermSlipRate(origSlipRate*i/numSubSectForParkfield);
//			System.out.println(i+"\t"+origSlipRate+"\t"+segData.getAveLongTermSlipRate());
		}

		int totNumSubSections = subSectionList.size();
		int numSubSectForCoachella=numSubSections[numSubSections.length-1]; // Coachella
		origSlipRate = subSectionList.get(totNumSubSections-1).getAveLongTermSlipRate();  // the value is currently the same for all subsections
		for(int i=totNumSubSections-numSubSectForCoachella; i<totNumSubSections;i++) {
			segData = subSectionList.get(i);
			segData.setAveLongTermSlipRate(origSlipRate*(totNumSubSections-i)/numSubSectForCoachella);
//			System.out.println(i+"\t"+origSlipRate+"\t"+segData.getAveLongTermSlipRate());
		}
	}

	
	private void smoothSlipRates(int numPts) {
		
		double[] slipRate = new double[subSectionList.size()];
		double[] smoothSlipRate = new double[subSectionList.size()];
		
		int n_seg = subSectionList.size();
		
		for(int seg=0; seg < n_seg; seg++) {
			slipRate[seg] = subSectionList.get(seg).getAveLongTermSlipRate();
			smoothSlipRate[seg] = subSectionList.get(seg).getAveLongTermSlipRate();
		}

		for(int seg=numPts; seg < n_seg; seg++) {
			double ave=0;
			for(int i=seg-numPts;i<seg; i++) ave+=slipRate[i];
			int index = seg - (numPts+1)/2;
			smoothSlipRate[index] = ave/numPts;
		}

		for(int seg=0; seg < n_seg; seg++)
			subSectionList.get(seg).setAveLongTermSlipRate(smoothSlipRate[seg]);

		// plot orig and final slip rates
		double min = 0, max = n_seg-1;
		EvenlyDiscretizedFunc origSlipRateFunc = new EvenlyDiscretizedFunc(min, max, n_seg);
		EvenlyDiscretizedFunc finalSlipRateFunc = new EvenlyDiscretizedFunc(min, max, n_seg);
		for(int seg=0; seg<n_seg;seg++) {
			origSlipRateFunc.set(seg,slipRate[seg]);
			finalSlipRateFunc.set(seg,smoothSlipRate[seg]);
		}
		ArrayList sr_funcs = new ArrayList();
		origSlipRateFunc.setName("Orig Slip Rates");
		finalSlipRateFunc.setName("Smooth Slip Rates");
		sr_funcs.add(origSlipRateFunc);
		sr_funcs.add(finalSlipRateFunc);
		GraphiWindowAPI_Impl sr_graph = new GraphiWindowAPI_Impl(sr_funcs, "Slip Rates");   

			

	}
	
	
	/**
	 * Get a list of all subsections
	 * 
	 * @return
	 */
	public ArrayList<FaultSectionPrefData> getAllSubsections() {
		return this.subSectionList;
	}
	
	
	private final int[][] getRupInSegMatrix(int num_seg) {

		int num_rup, nSegInRup, n_rup_wNseg;
		if(numSegForSmallestRups == 1 ) {
			num_rup = num_seg*(num_seg+1)/2;
			nSegInRup = 1;	// the number of segments in rupture (initialized as 1)
			n_rup_wNseg = num_seg; // the number of ruptures with the above N segments
		}
		else { // numSegForSmallestRups == 2
			num_rup = num_seg*(num_seg+1)/2 - num_seg;
			nSegInRup = 2;	// the number of segments in rupture (initialized as 1)
			n_rup_wNseg = num_seg-1; // the number of ruptures with the above N segments
		}
		
		numSegInRup = new int[num_rup];
		firstSegOfRup = new int[num_rup];

		int remain_rups = n_rup_wNseg;
		int startSeg = 0;
		int[][] rupInSeg = new int[num_seg][num_rup];
		for(int rup = 0; rup < num_rup; rup += 1) {
			numSegInRup[rup] = nSegInRup;
			firstSegOfRup[rup] = startSeg;
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
	    try{
	        FileWriter fw = new FileWriter("TestRupInSegMatrix.txt");
	        BufferedWriter br = new BufferedWriter(fw);
	        String line;
	        for(int rup = 0; rup < num_rup; rup += 1) {
				line = new String("\n");
				for(int seg = 0; seg < num_seg; seg+=1) line += rupInSeg[seg][rup]+"\t";
				br.write(line);
	        }
	        br.close();
	    }catch(Exception e){
	      e.printStackTrace();
	    }
	    */
		
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
	 * 
	 */
	private void computeInitialStuff() {
		rupArea = new double[num_rup];
		segArea = new double[num_seg];
		segSlipRate = new double[num_seg];
		segSlipRateStdDev = new double[num_seg];
		segMoRate = new double[num_seg];
		double minLength = Double.MAX_VALUE;
		double maxLength = 0;
		double minArea = Double.MAX_VALUE;
		double maxArea = 0;
		FaultSectionPrefData segData;
		totMoRate = 0;
		
		for(int seg=0; seg < num_seg; seg++) {
				segData = subSectionList.get(seg);
				segArea[seg] = segData.getDownDipWidth()*segData.getLength()*1e6*(1.0-segData.getAseismicSlipFactor()); // km --> m 
				segSlipRate[seg] = segData.getAveLongTermSlipRate()*1e-3; // mm/yr --> m/yr
				segSlipRateStdDev[seg] = segData.getSlipRateStdDev()*1e-3; // mm/yr --> m/yr
//				System.out.println(seg+":  segData.getLength()=\t"+segData.getLength());


//				if(D) System.out.println(seg+" slip rate = "+segSlipRate[seg]);
				segMoRate[seg] = FaultMomentCalc.getMoment(segArea[seg], segSlipRate[seg]); // 
				totMoRate += segMoRate[seg];
				
				if(segData.getLength() < minLength) minLength = segData.getLength();
				if(segData.getLength() > maxLength) maxLength = segData.getLength();
				if(segArea[seg]/1e6 < minArea) minArea = segArea[seg]/1e6;
				if(segArea[seg]/1e6 > maxArea) maxArea = segArea[seg]/1e6;
		}
		if(D) System.out.println("minSegArea="+(float)minArea+"\nmaxSegArea="+(float)maxArea+"\nminSegLength="+(float)minLength+"\nmaxSegLength="+(float)maxLength);
		
		for(int rup=0; rup<num_rup; rup++){
			rupArea[rup] = 0;
			for(int seg=0; seg < num_seg; seg++)
				if(rupInSeg[seg][rup]==1)  rupArea[rup] += segArea[seg];
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
//System.out.println(rup+"\t"+(float)aveSlip+" m");
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
				/*
				if(D) { // check results
					double d_aveTest=0;
					for(int seg=0; seg<num_seg; seg++)
						d_aveTest += segSlipInRup[seg][rup]*segArea[seg]/rupArea[rup];
					System.out.println("AveSlipCheck: " + (float) (d_aveTest/aveSlip));
				}
				*/
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
	private void getSegRateConstraints() {
		segRateConstraints   = new ArrayList<SegRateConstraint>();
		try {				
			POIFSFileSystem fs = new POIFSFileSystem(getClass().getClassLoader().getResourceAsStream(SEG_RATE_FILE_NAME));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			int lastRowIndex = sheet.getLastRowNum();
			double lat, lon, rate, sigma, lower95Conf, upper95Conf;
			String siteName;
			for(int r=1; r<=lastRowIndex; ++r) {	
				
				// read the event from the file
				HSSFRow row = sheet.getRow(r);
				if(row==null) continue;
				HSSFCell cell = row.getCell( (short) 1);
				if(cell==null || cell.getCellType()==HSSFCell.CELL_TYPE_STRING) continue;
				lat = cell.getNumericCellValue();
				siteName = row.getCell( (short) 0).getStringCellValue().trim();
				lon = row.getCell( (short) 2).getNumericCellValue();
				rate = row.getCell( (short) 3).getNumericCellValue();
				sigma =  row.getCell( (short) 4).getNumericCellValue();
				lower95Conf = row.getCell( (short) 7).getNumericCellValue();
				upper95Conf =  row.getCell( (short) 8).getNumericCellValue();
				
				// get Closest sub section
				double minDist = Double.MAX_VALUE, dist;
				int closestFaultSectionIndex=-1;
				Location loc = new Location(lat,lon);
				for(int sectionIndex=0; sectionIndex<subSectionList.size(); ++sectionIndex) {
					dist  = subSectionList.get(sectionIndex).getFaultTrace().getMinHorzDistToLine(loc);
					if(dist<minDist) {
						minDist = dist;
						closestFaultSectionIndex = sectionIndex;
					}
				}
				if(minDist>2) continue; // closest fault section is at a distance of more than 2 km
				
				// add to Seg Rate Constraint list
				String name = subSectionList.get(closestFaultSectionIndex).getSectionName()+" --"+siteName;
				SegRateConstraint segRateConstraint = new SegRateConstraint(name);
				segRateConstraint.setSegRate(closestFaultSectionIndex, rate, sigma, lower95Conf, upper95Conf);
				segRateConstraints.add(segRateConstraint);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private int getParkfieldRuptureIndex() {
		int num = numSubSections[0];
		int target=-1;
		for(int r=0;r<num_rup;r++)
			if(rupInSeg[0][r]==1 && rupInSeg[num-1][r]==1 && rupInSeg[num][r]==0) {
				target = r;
				break;
			}
		return target;
	}
	
	/**
	 * This constrains the rate of the parkfield event to be the observed rate, 
	 * plus any events smaller than parkfield to have zero rate
	 */
	private void setAprioriRateData() {
				
		double parkfieldMag = rupMeanMag[getParkfieldRuptureIndex()];
		System.out.println("Parkfield magnitude: "+(float)parkfieldMag);
		
		int numMagsBelowParkfield=0;
		for(int r=0;r<num_rup;r++)
			if(rupMeanMag[r]<parkfieldMag) {
				numMagsBelowParkfield +=1;
//				System.out.println(r+"\t"+rupMeanMag[r]);
			}

		int num_constraints = numMagsBelowParkfield + 1; // add one for the parkfield rupture itself
		aPriori_rupIndex = new int[num_constraints];
		aPriori_rate = new double[num_constraints];
		aPriori_wt = new double[num_constraints];

		// set the zero constraints
		int counter=0;
		for(int r=0;r<num_rup;r++)
			if(rupMeanMag[r]<parkfieldMag) {
				aPriori_rupIndex[counter] = r;
				aPriori_rate[counter] = 0.0;
				aPriori_wt[counter] = 1e6;
				counter += 1;
			}

		
		// Set parkfield rate to ~25 years
		aPriori_rupIndex[num_constraints-1] = getParkfieldRuptureIndex();
		aPriori_rate[num_constraints-1] = 0.04; // 1/25
		aPriori_wt[num_constraints-1] = 1.0/0.01; 
	
	}

	
	
	/**
	 * This gets the non-negative least squares solution for the matrix C
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
			System.out.println("display "+"SSAF Inversion test");
			System.out.println("C = [");
			for(i=0; i<nRow;i++) {
				for(j=0;j<nCol;j++) 
					System.out.print(C[i][j]+"   ");
				System.out.print("\n");
			}
			System.out.println("];");
			System.out.println("d = [");
			for(i=0; i<nRow;i++)
				System.out.println(d[i]);
			System.out.println("];");
		}
/////////////////////////////////////
		
		for(j=0;j<nCol;j++) 
			for(i=0; i<nRow;i++)	{
				A[k]=C[i][j];
				k+=1;
			}
		nnls.update(A,nRow,nCol);
		
		boolean converged = nnls.solve(d,x);
		if(!converged)
			throw new RuntimeException("ERROR:  NNLS Inversion Failed");
		
		if(MATLAB_TEST) {
			System.out.println("x = [");
			for(i=0; i<x.length;i++)
				System.out.println(x[i]);
			System.out.println("];");
			System.out.println("max(abs(x-lsqnonneg(C,d)))");
		}
		
		return x;
	}
	
	/**
	 * Computer Final Slip Rate for each segment (& aPrioriSegSlipRate)
	 *
	 */
	private void computeFinalStuff() {
		
		// compute segment slip and event rates
		finalSegSlipRate = new double[num_seg];
		finalSegEventRate = new double[num_seg];
		for(int seg=0; seg < num_seg; seg++) {
			finalSegSlipRate[seg] = 0;
			finalSegEventRate[seg] = 0;
			for(int rup=0; rup < num_rup; rup++) 
				if(rupInSeg[seg][rup]==1) {
					finalSegSlipRate[seg] += rupRateSolution[rup]*segSlipInRup[seg][rup];
					finalSegEventRate[seg]+=rupRateSolution[rup];
				}
//			System.out.println((float)(finalSegSlipRate[seg]/this.d_pred[seg]));
//			double absFractDiff = Math.abs(finalSegSlipRate[seg]/(segSlipRate[seg]*(1-this.moRateReduction)) - 1.0);	
		}
		
		// compute predicted data
		
		// compute segment slip-rate and event rate residuals
		
//		segSlipRateResids, segEventRateResids
		
		magFreqDist = new SummedMagFreqDist(5,41,0.1);
		magFreqDist.setTolerance(1.0); // set to large value so it becomes a histogram
		for(int rup=0; rup<num_rup;rup++) {
			if(rupMeanMag[rup] >= 5.0)
//				magFreqDist.add(rupMeanMag[rup], rupRateSolution[rup]);
				// changed to preserve moment rates
				magFreqDist.addResampledMagRate(rupMeanMag[rup], rupRateSolution[rup], false);
		}
		magFreqDist.setInfo("Incremental Mag Freq Dist");
		
		smoothedMagFreqDist = new SummedMagFreqDist(5,41,0.1);
//		magFreqDist.setTolerance(1.0); // set to large value so it becomes a histogram
		for(int rup=0; rup<num_rup;rup++) {
//			if(rupMeanMag[rup] >= 5.0)
			double momentRate = MomentMagCalc.getMoment(rupMeanMag[rup])*rupRateSolution[rup];
			GaussianMagFreqDist gDist = new GaussianMagFreqDist(5.0,9.0,41,rupMeanMag[rup],0.12,momentRate,2.0,2);
			smoothedMagFreqDist.addIncrementalMagFreqDist(gDist);
		}
		smoothedMagFreqDist.setInfo("Smoothed Incremental Mag Freq Dist");
		
		
		// COMPUTE RATE AT WHICH SECTION BOUNDARIES CONSTITUTE RUPTURE ENDPOINTS
		rateOfRupEndsOnSeg = new double[num_seg+1];  // there is one more boundary than segments
		for(int rup=0; rup<num_rup;rup++) {
			int beginBoundary = firstSegOfRup[rup];
			int endBoundary = firstSegOfRup[rup]+numSegInRup[rup];
			rateOfRupEndsOnSeg[beginBoundary] += rupRateSolution[rup];
			rateOfRupEndsOnSeg[endBoundary] += rupRateSolution[rup];
		}


	}
	
	
	
	private void computeSegMFDs() {
		segmentNucleationMFDs = new ArrayList<SummedMagFreqDist>();
		segmentParticipationMFDs = new ArrayList<SummedMagFreqDist>();
		SummedMagFreqDist sumOfSegNuclMFDs = new SummedMagFreqDist(5,41,0.1);
		SummedMagFreqDist sumOfSegPartMFDs = new SummedMagFreqDist(5,41,0.1);
		aveOfSegPartMFDs = new SummedMagFreqDist(5,41,0.1);
		
		SummedMagFreqDist segPartMFD, segNuclMFD;
		double mag, rate;
		for(int seg=0; seg < num_seg; seg++) {
			segPartMFD = new SummedMagFreqDist(5,41,0.1);
			segNuclMFD = new SummedMagFreqDist(5,41,0.1);
			for(int rup=0; rup < num_rup; rup++) {
				if(this.rupInSeg[seg][rup] == 1) {
					mag = this.rupMeanMag[rup];
					rate = rupRateSolution[rup];
					segNuclMFD.addResampledMagRate(mag, rate/numSegInRup[rup], false); // uniform probability that any sub-section will nucleate
					segPartMFD.addResampledMagRate(mag, rate, false);
				}
			}
			segmentNucleationMFDs.add(segNuclMFD);
			segmentParticipationMFDs.add(segPartMFD);
			sumOfSegNuclMFDs.addIncrementalMagFreqDist(segNuclMFD);
			sumOfSegPartMFDs.addIncrementalMagFreqDist(segPartMFD);
		}
		// compute aveOfSegPartMFDs from sumOfSegPartMFDs
		for(int m=0; m<sumOfSegPartMFDs.getNum();m++) aveOfSegPartMFDs.add(m, sumOfSegPartMFDs.getY(m)/num_seg);
		aveOfSegPartMFDs.setInfo("Average Seg Participation MFD");
		
		// test the sum of segmentNucleationMFDs (checks out for both 5 and 10 km subsection lengths)
		/*
		System.out.println("TEST SUMMED MFDs");
		for(int m=0; m<sumOfSegNuclMFDs.getNum();m++)
			System.out.println(m+"\t"+sumOfSegNuclMFDs.getX(m)+"\t"+(float)sumOfSegNuclMFDs.getY(m)+
					"\t"+(float)magFreqDist.getY(m)+"\t"+ (float)(sumOfSegNuclMFDs.getY(m)/magFreqDist.getY(m)));
		*/
	}
	
	public void writeFinalStuff() {
		
		// write out rupture rates and mags
//		System.out.println("Final Rupture Rates & Mags:");
//		for(int rup=0; rup < num_rup; rup++)
//			System.out.println(rup+"\t"+(float)rupRateSolution[rup]+"\t"+(float)rupMeanMag[rup]);
		
		//write out number of ruptures that have rates above minRupRate
		int numAbove = 0;
		for(int rup=0; rup<this.rupRateSolution.length; rup++)
			if(rupRateSolution[rup] > minRupRate) numAbove += 1;
		System.out.println("Num Ruptures above minRupRate = "+numAbove+"\t(out of "+rupRateSolution.length+")");

		// write out final segment slip rates
			System.out.println("Segment Slip Rates: index, final, orig, and final/orig)");
			for(int seg = 0; seg < num_seg; seg ++) {
				double slipRate = segSlipRate[seg]*(1-this.moRateReduction);
				System.out.println(seg+"\t"+(float)finalSegSlipRate[seg]+"\t"+(float)slipRate+"\t"+(float)(finalSegSlipRate[seg]/slipRate));
			}
		
		// write out final segment event rates
		if(relativeSegRateWt > 0.0) {
			System.out.println("Segment Rates: index, final, orig, and final/orig)");
			SegRateConstraint constraint;
			for(int i = 0; i < segRateConstraints.size(); i ++) {
//				int row = firstRowSegEventRateData+i;
				constraint = segRateConstraints.get(i);
				int seg = constraint.getSegIndex();
//				System.out.println(seg+"\t"+(float)finalSegEventRate[seg]+"\t"+d_pred[row]+"\t"+(float)(finalSegEventRate[seg]/d_pred[row]));
				System.out.println(seg+"\t"+(float)finalSegEventRate[seg]+"\t"+(float)constraint.getMean()+"\t"+(float)(finalSegEventRate[seg]/constraint.getMean()));
			}
		}
		
		// write out final rates for ruptures with an a-priori constraint
		if(this.relative_aPrioriRupWt >0.0)
			System.out.println("A Priori Rates: index, final, orig, and final/orig)");
			for(int i=0; i<this.aPriori_rate.length;i++) {
				double ratio;
				if(rupRateSolution[aPriori_rupIndex[i]] > 1e-14 && aPriori_rate[i] > 1e-14)  // if both are not essentially zero
					ratio = (rupRateSolution[aPriori_rupIndex[i]]/aPriori_rate[i]);
				else
					ratio = 1;
				System.out.println(aPriori_rupIndex[i]+"\t"+(float)rupRateSolution[aPriori_rupIndex[i]]+"\t"+aPriori_rate[i]+"\t"+
						(float)ratio);				
			}
			
	}
	
	public void writePredErrorInfo() {
		
		// First without equation weights
		double totPredErr=0, slipRateErr=0, eventRateErr=0, aPrioriErr=0, smoothnessErr=0;
		for(int row=firstRowSegSlipRateData; row <= lastRowSegSlipRateData; row++)
			slipRateErr += (d[row]-d_pred[row])*(d[row]-d_pred[row])*data_wt[row]*data_wt[row];
		
		if(relativeSegRateWt >0)
			for(int row=firstRowSegEventRateData; row <= lastRowSegEventRateData; row++)
				eventRateErr += (d[row]-d_pred[row])*(d[row]-d_pred[row])*data_wt[row]*data_wt[row];
		if(relative_aPrioriRupWt > 0)
			for(int row=firstRowAprioriData; row <= lastRowAprioriData; row++)
				aPrioriErr += (d[row]-d_pred[row])*(d[row]-d_pred[row])*data_wt[row]*data_wt[row];
		if(relative_smoothnessWt>0)
			for(int row=firstRowSmoothnessData; row <= lastRowSmoothnessData; row++)
				smoothnessErr += (d[row]-d_pred[row])*(d[row]-d_pred[row])*data_wt[row]*data_wt[row];
		totPredErr = slipRateErr+eventRateErr+aPrioriErr+smoothnessErr;
		System.out.println("\nTotal Prediction Error =\t"+(float)totPredErr+"\n\t"+
				"Slip Rate Err =\t\t"+(float)slipRateErr+"\trel. wt = 1.0\n\t"+
				"Event Rate Err =\t"+(float)eventRateErr+"\trel. wt = "+relativeSegRateWt+"\n\t"+
				"A Priori Err =\t\t"+(float)aPrioriErr+"\trel. wt = "+relative_aPrioriRupWt+"\n\t"+
				"Smoothness Err =\t"+(float)smoothnessErr+"\trel. wt = "+relative_smoothnessWt+"\n\t");
		
		// Now with equation weights
		totPredErr=0; slipRateErr=0; eventRateErr=0; aPrioriErr=0; smoothnessErr=0;
		for(int row=firstRowSegSlipRateData; row <= lastRowSegSlipRateData; row++)
			slipRateErr += (d[row]-d_pred[row])*(d[row]-d_pred[row])*full_wt[row]*full_wt[row];
		if(relativeSegRateWt >0)
			for(int row=firstRowSegEventRateData; row <= lastRowSegEventRateData; row++)
				eventRateErr += (d[row]-d_pred[row])*(d[row]-d_pred[row])*full_wt[row]*full_wt[row];
		if(relative_aPrioriRupWt > 0)
			for(int row=firstRowAprioriData; row <= lastRowAprioriData; row++)
				aPrioriErr += (d[row]-d_pred[row])*(d[row]-d_pred[row])*full_wt[row]*full_wt[row];
		if(relative_smoothnessWt>0)
			for(int row=firstRowSmoothnessData; row <= lastRowSmoothnessData; row++)
				smoothnessErr += (d[row]-d_pred[row])*(d[row]-d_pred[row])*full_wt[row]*full_wt[row];
		totPredErr = slipRateErr+eventRateErr+aPrioriErr+smoothnessErr;
		System.out.println("\nTotal Pred Err w/ Eq Wts =\t"+(float)totPredErr+"\n\t"+
				"Slip Rate Err =\t\t"+(float)slipRateErr+"\trel. wt = 1.0\n\t"+
				"Event Rate Err =\t"+(float)eventRateErr+"\trel. wt = "+relativeSegRateWt+"\n\t"+
				"A Priori Err =\t\t"+(float)aPrioriErr+"\trel. wt = "+relative_aPrioriRupWt+"\n\t"+
				"Smoothness Err =\t"+(float)smoothnessErr+"\trel. wt = "+relative_smoothnessWt+"\n\t");
			
	}
	

	public void plotStuff() {
		
		// plot orig and final slip rates	
		double min = 0, max = num_seg-1;
		EvenlyDiscretizedFunc origSlipRateFunc = new EvenlyDiscretizedFunc(min, max, num_seg);
		EvenlyDiscretizedFunc finalSlipRateFunc = new EvenlyDiscretizedFunc(min, max, num_seg);
		for(int seg=0; seg<num_seg;seg++) {
			origSlipRateFunc.set(seg,segSlipRate[seg]*(1-moRateReduction));
			finalSlipRateFunc.set(seg,finalSegSlipRate[seg]);
		}
		ArrayList sr_funcs = new ArrayList();
		origSlipRateFunc.setName("Orig Slip Rates");
		finalSlipRateFunc.setName("Final Slip Rates");
		sr_funcs.add(origSlipRateFunc);
		sr_funcs.add(finalSlipRateFunc);
		GraphiWindowAPI_Impl sr_graph = new GraphiWindowAPI_Impl(sr_funcs, "Slip Rates");   

		// plot orig and final seg event rates	
		ArrayList er_funcs = new ArrayList();
		// now fill in final event rates
		EvenlyDiscretizedFunc finalEventRateFunc = new EvenlyDiscretizedFunc(min, max, num_seg);
		for(int seg=0;seg < num_seg; seg++)
			finalEventRateFunc.set(seg,finalSegEventRate[seg]);
		finalEventRateFunc.setName("Final Event Rates");
		er_funcs.add(finalEventRateFunc);

		int num = segRateConstraints.size();
		ArbitrarilyDiscretizedFunc func;
		SegRateConstraint constraint;
		for(int c=0;c<num;c++) {
			func = new ArbitrarilyDiscretizedFunc();
			constraint = segRateConstraints.get(c);
			int seg = constraint.getSegIndex();
			func.set((double)seg-0.0001, constraint.getLower95Conf());
			func.set((double)seg, constraint.getMean());
			func.set((double)seg+0.0001, constraint.getUpper95Conf());
			func.setName(constraint.getFaultName());
			er_funcs.add(func);
		}
		
		GraphiWindowAPI_Impl er_graph = new GraphiWindowAPI_Impl(er_funcs, "Event Rates"); 
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE, Color.BLUE, 2));
		for(int c=0;c<num;c++)
			plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS, Color.RED, 6));
		er_graph.setPlottingFeatures(plotChars);
		
		// plot the final rupture rates
		max = num_rup-1;
		EvenlyDiscretizedFunc rupRateFunc = new EvenlyDiscretizedFunc(min, max, num_rup);
		for(int rup=0; rup<num_rup;rup++) {
			rupRateFunc.set(rup,rupRateSolution[rup]);
		}
		ArrayList rup_funcs = new ArrayList();
		rupRateFunc.setName("Rupture Rates");
		rup_funcs.add(rupRateFunc);
		GraphiWindowAPI_Impl rup_graph = new GraphiWindowAPI_Impl(rup_funcs, "Rupture Rates");   


		// PLOT MFDs
		ArrayList mfd_funcs = new ArrayList();
		mfd_funcs.add(magFreqDist);
		EvenlyDiscretizedFunc cumMagFreqDist = magFreqDist.getCumRateDistWithOffset();
		cumMagFreqDist.setInfo("Cumulative Mag Freq Dist");
		mfd_funcs.add(cumMagFreqDist);
		mfd_funcs.add(smoothedMagFreqDist);
		EvenlyDiscretizedFunc SmoothedCumMagFreqDist = smoothedMagFreqDist.getCumRateDistWithOffset();
		smoothedMagFreqDist.setInfo("Smoothed Cumulative Mag Freq Dist");
		mfd_funcs.add(SmoothedCumMagFreqDist);	
		// add average seg participation MFD
		mfd_funcs.add(aveOfSegPartMFDs);
		EvenlyDiscretizedFunc cumAveOfSegPartMFDs = aveOfSegPartMFDs.getCumRateDistWithOffset();
		cumAveOfSegPartMFDs.setInfo("cumulative "+aveOfSegPartMFDs.getInfo());
		mfd_funcs.add(cumAveOfSegPartMFDs);
// the following is just a check		
//		System.out.println("orig/smoothed MFD moRate ="+ (float)(magFreqDist.getTotalMomentRate()/smoothedMagFreqDist.getTotalMomentRate()));
		// add a GR dist matched at M=6.5 and with a bValue=1
/*		ArbitrarilyDiscretizedFunc GR_dist = new ArbitrarilyDiscretizedFunc();
		GR_dist.set(6.0,smoothedMagFreqDist.getIncrRate(6.5)*Math.pow(10,0.5));
		GR_dist.set(8.25,smoothedMagFreqDist.getIncrRate(6.5)*Math.pow(10,-1.75));
		GR_dist.setName("GR Dist fit at M=6.5 and /w b=1");
*/
		GutenbergRichterMagFreqDist gr = getGR_Dist_fit();
		mfd_funcs.add(gr);
		EvenlyDiscretizedFunc cumGR = gr.getCumRateDistWithOffset();
		cumGR.setName("Cum GR Dist fit at cum M=6.5, matched moment rate, and /w b=1");
		mfd_funcs.add(cumGR);
		GraphiWindowAPI_Impl mfd_graph = new GraphiWindowAPI_Impl(mfd_funcs, "Mag Freq Dists");   
		mfd_graph.setYLog(true);
		mfd_graph.setY_AxisRange(1e-5, 1);
		mfd_graph.setX_AxisRange(5.5, 9.0);
		
		// PLOT RATE AT WHICH SECTIONS ENDS REPRESENT RUPTURE ENDS
		min = 0; max = num_seg;
		EvenlyDiscretizedFunc rateOfRupEndsOnSegFunc = new EvenlyDiscretizedFunc(min, max, num_seg+1);
		for(int seg=0; seg<num_seg+1;seg++) {
			rateOfRupEndsOnSegFunc.set(seg,rateOfRupEndsOnSeg[seg]);
		}
		ArrayList seg_funcs = new ArrayList();
		rateOfRupEndsOnSegFunc.setName("Rate that section ends represent rupture ends");
		seg_funcs.add(rateOfRupEndsOnSegFunc);
		seg_funcs.add(finalSlipRateFunc);
		GraphiWindowAPI_Impl seg_graph = new GraphiWindowAPI_Impl(seg_funcs, "Rate of Rupture Ends");   


	}
	
	
	public GutenbergRichterMagFreqDist getGR_Dist_fit() {
		double magMin = (double)Math.round(10*rupMeanMag[this.getParkfieldRuptureIndex()]) / 10;
		double magMax = (double)Math.round(10*rupMeanMag[rupMeanMag.length-1]) / 10;
System.out.println("minMag="+magMin+"\tmaxMag="+magMax+"\ttotMoRate="+(float)totMoRate+"\tmfdMoRate="+(float)magFreqDist.getTotalMomentRate());
		double cumRate = magFreqDist.getCumRate(6.5)*Math.pow(10, 6.5-magMin); // assumes b-value of 1
		int num = (int)Math.round((9.0-magMin)/0.1 + 1);
		GutenbergRichterMagFreqDist gr = new GutenbergRichterMagFreqDist(magMin,num,0.1);
		gr.setAllButMagUpper(magMin, totMoRate, cumRate, 1.0, false);
		gr.setName("GR Dist fit at cum M=6.5, matched moment rate, and /w b=1");
		return gr;
	}

	
	/**
	 * It gets all the subsections for SoSAF and prints them on console
	 * @param args
	 */
	public static void main(String []args) {

		SoSAF_SubSectionInversion soSAF_SubSections = new  SoSAF_SubSectionInversion(10,1);
//		SoSAF_SubSectionInversion soSAF_SubSections = new  SoSAF_SubSectionInversion(5,2);
		
/**/
		System.out.println("Starting Inversion");
		String slipModelType = TAPERED_SLIP_MODEL;
		MagAreaRelationship magAreaRel = new HanksBakun2002_MagAreaRel();
		double relativeSegRateWt=1;
		double relative_aPrioriRupWt = 1;
		double relative_smoothnessWt = 1;
		boolean wtedInversion = true;
		soSAF_SubSections.doInversion(slipModelType, magAreaRel, relativeSegRateWt, relative_aPrioriRupWt, 
				relative_smoothnessWt, wtedInversion, 0);
		System.out.println("Done with Inversion");
		soSAF_SubSections.writeFinalStuff();
		soSAF_SubSections.writePredErrorInfo();
		soSAF_SubSections.plotStuff();
		soSAF_SubSections.writeSegPartMFDsDataToFile("segPartMFDsData.txt");

		
/*		
		ArrayList<FaultSectionPrefData> subsectionList = soSAF_SubSections.getAllSubsections();
		for(int i=0; i<subsectionList.size(); ++i) {
			FaultSectionPrefData subSection = subsectionList.get(i);
			System.out.println(i+"\t"+subSection.getSectionName()+"\t"+(float)subSection.getLength());
//			System.out.println(subSection.getFaultTrace());
		}
*/
		/*		
		// write rup names to a file
		System.out.println("Writing file for short rupture names");
		soSAF_SubSections.writeRupNames("ShortRupNames.txt");
		
		// computer and print seg Rate constraints
		System.out.println("Writing Seg Rate constraints");
		soSAF_SubSections.computeSegRateConstraints();
		for(int i=0; i<soSAF_SubSections.segRateConstraints.size(); ++i) {
			SegRateConstraint segRateConstraint = soSAF_SubSections.segRateConstraints.get(i);
			System.out.println(segRateConstraint.getFaultName()+","+segRateConstraint.getMean());
		}
*/	
		
	}

}

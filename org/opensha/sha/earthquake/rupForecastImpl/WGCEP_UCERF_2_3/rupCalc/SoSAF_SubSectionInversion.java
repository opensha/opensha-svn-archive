/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.rupCalc;

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
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.data.EventRates;
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
	private final static String SEG_RATE_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_3/data/Appendix_C_Table7_091807.xls";

	private boolean D = true;
	private final static int MAX_SUBSECTION_LEN = 10;
	private DeformationModelPrefDataDB_DAO deformationModelPrefDB_DAO = new DeformationModelPrefDataDB_DAO(DB_AccessAPI.dbConnection);
	private ArrayList<FaultSectionPrefData> subSectionList;
	
	private int num_seg, num_rup;
	
	ArrayList<SegRateConstraint> segRateConstraints;
	
	// a-priori rate constraints
	int[] aPriori_rup;
	double[] aPriori_rate, aPriori_wt;
	
	private static boolean MATLAB_TEST = false;
	
	private double minRates[]; // the minimum rate constraint for each rupture
	private boolean wtedInversion;	// weight the inversion according to slip rate and segment rate uncertainties
	private double relativeSegRate_wt, aPrioriRupWt;
	
	// slip model:
	private String slipModelType;
	public final static String CHAR_SLIP_MODEL = "Characteristic (Dsr=Ds)";
	public final static String UNIFORM_SLIP_MODEL = "Uniform/Boxcar (Dsr=Dr)";
	public final static String WG02_SLIP_MODEL = "WGCEP-2002 model (Dsr prop to Vs)";
	public final static String TAPERED_SLIP_MODEL = "Tapered Ends ([Sin(x)]^0.5)";
	
	private static EvenlyDiscretizedFunc taperedSlipPDF, taperedSlipCDF;
	
	private int[][] rupInSeg;
	private double[][] segSlipInRup;
	
	private double[] finalSegRate, finalSegSlipRate;
	
	private String[] rupNameShort;
	private double[] rupArea, rupMeanMag, rupMeanMo, rupMoRate, totRupRate, segArea, segSlipRate, segSlipRateStdDev, segMoRate;
	double[] rupRateSolution; // these are the rates from the inversion (not total rate of MFD)
	
	// the following is the total moment-rate reduction, including that which goes to the  
	// background, sfterslip, events smaller than the min mag here, and aftershocks and foreshocks.
	private double moRateReduction;  
	
	private MagAreaRelationship magAreaRel;
	
	// NNLS inversion solver - static to save time and memory
	private static NNLSWrapper nnls = new NNLSWrapper();

	
	public SoSAF_SubSectionInversion() {
		
		// set slip model as one of: CHAR_SLIP_MODEL, UNIFORM_SLIP_MODEL, WG02_SLIP_MODEL, TAPERED_SLIP_MODEL
		slipModelType = TAPERED_SLIP_MODEL;
		
		// set the mag-area relationship
		magAreaRel = new HanksBakun2002_MagAreaRel();
		
		// relative weights on the segment rates and a-priori rates
		relativeSegRate_wt=1;
		aPrioriRupWt = 1;
		
		// use segment slip-rate and event-rate uncertainties to weight inversion:
		wtedInversion = false;
		
		// chop the SSAF into many sub-sections
		computeAllSubsections();
		
		// get the RupInSeg Matrix for the given number of segments
		num_seg = subSectionList.size();
		rupInSeg = getRupInSegMatrix(num_seg);
		num_rup = num_seg*(num_seg+1)/2;
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
		computeSegAndRupStuff();
			
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
	}
	
	
	/**
	 * Write Short Rup names to a file
	 * 
	 * @param fileName
	 */
	public void writeRupNames(String fileName) {
		try{
			FileWriter fw = new FileWriter("org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_3/rupCalc/"+fileName);
			fw.write("rup_index\trupNameShort\n");
			for(int i=0; i<rupNameShort.length; ++i)
				fw.write(i+"\t"+rupNameShort[i]+"\n");
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void doInversion() {
		
		int numRowsBeforeSegRateData=-1, numRowsBeforeAprioriData=-1;

		// compute matrix of Dsr (slip on each segment in each rupture)
		computeSegSlipInRupMatrix();
			
			
		// NOW SOLVE THE INVERSE PROBLEM
					
		// get the segment rate constraints
		computeSegRateConstraints();
		int numRateConstraints = segRateConstraints.size();
		
		// set the a-priori rates
		setAprioriRateData();
		int num_aPriori_constraints = aPriori_rup.length;
		
		// set the minimum rupture rate constraints
		setMinRates();

		// set number of rows as one for each slip-rate/segment (the minimum)
		int totNumRows = num_seg;
		
		// add segment rate constrains if needed
		if(relativeSegRate_wt > 0.0) {
			numRowsBeforeSegRateData = totNumRows;
			totNumRows += numRateConstraints;
		}
		
		// add a-priori rate constrains if needed
		if(aPrioriRupWt > 0.0) {
			numRowsBeforeAprioriData  = totNumRows;
			totNumRows += num_aPriori_constraints;
		}
			
//		int numRowsBeforeSegRateData = num_seg;
//		if(aPrioriRupWt > 0.0) numRowsBeforeSegRateData += num_rup;
			
		double[][] C = new double[totNumRows][num_rup];
		double[] d = new double[totNumRows];  // the data vector
			
		// CREATE THE MODEL AND DATA MATRICES
		// first fill in the slip-rate constraints
		for(int row = 0; row < num_seg; row ++) {
			d[row] = segSlipRate[row]*(1-moRateReduction);
			for(int col=0; col<num_rup; col++)
				C[row][col] = segSlipInRup[row][col];
		}
		// now fill in the segment rate constraints if requested
		if(relativeSegRate_wt > 0.0) {
			SegRateConstraint constraint;
			for(int i = 0; i < numRateConstraints; i ++) {
				constraint = segRateConstraints.get(i);
				int seg = constraint.getSegIndex();
				int row = i+numRowsBeforeSegRateData;
				d[row] = constraint.getMean(); // this is the average segment rate
				for(int col=0; col<num_rup; col++)
					C[row][col] = rupInSeg[seg][col];
			}
		}
		// now fill in the a-priori rates if needed
		if(aPrioriRupWt > 0.0) {
			for(int i=0; i < num_aPriori_constraints; i++) {
				int row = i+numRowsBeforeAprioriData;
				int col = aPriori_rup[i];
				d[row] = aPriori_rate[i];
				C[row][col]=1.0;
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
//				data_wt = Math.pow((1-moRateReduction)*segmentData.getSegSlipRateStdDev(row), -2);
				data_wt = 1/((1-moRateReduction)*segSlipRateStdDev[row]);
				d[row] *= data_wt;
				for(int col=0; col<num_rup; col++)
					C[row][col] *= data_wt;
			}
			// now fill in the segment recurrence interval constraints if requested
			if(relativeSegRate_wt > 0.0) {
				SegRateConstraint constraint;
				for(int row = 0; row < numRateConstraints; row ++) {
					constraint = segRateConstraints.get(row);
//					data_wt = Math.pow(constraint.getStdDevOfMean(), -2);
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
			for(int i=0; i < num_aPriori_constraints; i++) {
				int row = i+numRowsBeforeAprioriData;
				int col = aPriori_rup[i];
				d[row] *= aPrioriRupWt;
				C[row][col] *= aPrioriRupWt;
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
		rupRateSolution = getNNLS_solution(C, d);


		// CORRECT FINAL RATES IF MINIMUM RATE CONSTRAINT APPLIED
		for(int rup=0; rup<num_rup;rup++)
			rupRateSolution[rup] += minRates[rup];

//		System.out.println("NNLS rates:");
//		for(int rup=0; rup < rupRate.length; rup++)
//		System.out.println((float) rupRateSolution[rup]);


		/**/
		if(D) {
//			check slip rates to make sure they match exactly
			double tempSlipRate;
			//System.out.println("Check of segment slip rates for "+segmentData.getFaultName()+":");
			for(int seg=0; seg < num_seg; seg++) {
				tempSlipRate = 0;
				for(int rup=0; rup < num_rup; rup++)
					tempSlipRate += rupRateSolution[rup]*segSlipInRup[seg][rup];
				double absFractDiff = Math.abs(tempSlipRate/(segSlipRate[seg]*(1-this.moRateReduction)) - 1.0);				
				
				System.out.println(seg+"\t"+(segSlipRate[seg]*(1-this.moRateReduction))+"\t"+(float)tempSlipRate+"\t"+absFractDiff);
//				if(absFractDiff > 0.001)
//					throw new RuntimeException("ERROR - slip rates differ!!!!!!!!!!!!");
			}
		}

		
		// Computer final segment slip rate
//		computeFinalSegSlipRate();

		// get final rate of events on each segment (this takes into account mag rounding of MFDs)
//		computeFinalSegRates();		

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
		segSlipRateStdDev = new double[num_seg];
		segMoRate = new double[num_seg];
		FaultSectionPrefData segData;
		for(int seg=0; seg < num_seg; seg++) {
				segData = subSectionList.get(seg);
				segArea[seg] += segData.getDownDipWidth()*segData.getLength(); // note this ignores aseismicity!
				segSlipRate[seg] = segData.getAveLongTermSlipRate();
				segSlipRateStdDev[seg] = segData.getSlipRateStdDev();
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
	private void computeSegRateConstraints() {
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
				SegRateConstraint segRateConstraint = new SegRateConstraint(subSectionList.get(closestFaultSectionIndex).getSectionName());
				segRateConstraint.setSegRate(closestFaultSectionIndex, rate, sigma, lower95Conf, upper95Conf);
				segRateConstraints.add(segRateConstraint);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	private void setAprioriRateData() {
		int num_constraints = 1;
		aPriori_rup = new int[num_constraints];
		aPriori_rate = new double[num_constraints];
		aPriori_wt = new double[num_constraints];
		
		// Set parkfield rate to ~25 years
		aPriori_rup[0] = 171;
		aPriori_rate[0] = 1/25;
		aPriori_wt[0] = 0;
	}

	
	private void setMinRates() {
		minRates = new double[num_rup];
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
	 * It gets all the subsections for SoSAF and prints them on console
	 * @param args
	 */
	public static void main(String []args) {
		SoSAF_SubSectionInversion soSAF_SubSections = new  SoSAF_SubSectionInversion();
		System.out.println("Starting Inversion");
		soSAF_SubSections.doInversion();
		System.out.println("Done with Inversion");
		
		
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

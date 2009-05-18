package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.analysis;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.A_Faults.A_FaultSegmentedSourceGenerator;

/**
 * This class writes Ruptures probabilities and gains into an excel sheet. It loops over logic tree branches and writes prob and gains for each branch
 * 
 * @author vipingupta
 *
 */
public class WriteTimeDepRupProbAndGain {
	
	
	private final static String README_TEXT = "This Excel spreadsheet tabulates Rupture Probability, Rupture Gain, Segment Probability,"+
		" Segment Gain and Segment Rate (each on a different sheet) for all Type-A fault segmented models,"+
		" and for all 12 relevant logic-tree branches (in columns B through M) described in Appendix N."+
		" The exact parameter settings for each logic-tree branch are listed in the \"Parameter Settings\""+
		" sheet, where those that vary between branches are in bold typeface.  The total aggregated"+
		" rupture probability for each fault is given at the bottom of the list for each fault."+
		" Column N gives the weighted average value (over all logic tree branches, where the weights"+
		" are given on row 147 on the Rupture Probability & Gain sheet and row 52 on the Segment Probability"+
		" and Gain sheet.  Columns O and P give the Min and Max, respectively, among all the logic-tree"+
		" branches. \"Gain\" is defined as the ratio of the probability to the Poisson probability.  Note" +
		" that the weighted averages for the gains are"+
		" the individual ratios averaged, which is not the same as the weight-averaged probability divided by"+
		" the weight-averaged Poisson probability (the latter is more correct & what is listed in tables in"+
		" Appendix N). The \"Segment Rate\" sheet gives data on the annual rate of events on each segment.";
	private ArrayList<String> paramNames;
	private ArrayList<ParamOptions> paramValues;
	private int lastParamIndex;
	private UCERF2 ucerf2;
	private HSSFSheet rupProbSheet, rupGainSheet, segProbSheet, segGainSheet;
	private HSSFSheet adjustableParamsSheet, readmeSheet, segRateSheet;
	private int loginTreeBranchIndex = 0;
	
	private ArrayList<String> adjustableParamNames;
	private ArrayList<Double> segProbWtAve, segProbMin, segProbMax;
	private ArrayList<Double> rupProbWtAve, rupProbMin, rupProbMax;
	private ArrayList<Double> segGainWtAve, segGainMin, segGainMax;
	private ArrayList<Double> rupGainWtAve, rupGainMin, rupGainMax;
	private ArrayList<Double> segRateWtAve, segRateMin, segRateMax;
	private HSSFCellStyle boldStyle;
	
	// change these parameters for every run
	//private final static String FILENAME = "RupProbs_BPT_30yr_SegDepAper.xls";
	//private final static String FILENAME = "RupProbs_BPT_5yr_SegDepAper.xls";
	//private final static String FILENAME = "RupProbs_BPT_30yr_ConstAper.xls";
	//private final static String FILENAME = "RupProbs_BPT_5yr_ConstAper.xls";
	//private final static String FILENAME = "RupProbs_Pois_30yr.xls";
	private final static String FILENAME = "RupProbs_Pois_5yr.xls";
	
	private static double DURATION = 5;
	private final static Boolean SEG_DEP_APERIODICITY = new Boolean(false);
	private final static String PROB_MODEL_VAL = UCERF2.PROB_MODEL_POISSON;
	
	
	public WriteTimeDepRupProbAndGain() {
		this(new UCERF2());
	}
	

	public WriteTimeDepRupProbAndGain(UCERF2 ucerf2) {
		this.ucerf2 = ucerf2;
		fillAdjustableParams();
		lastParamIndex = paramNames.size()-1;
		HSSFWorkbook wb  = new HSSFWorkbook();
		readmeSheet = wb.createSheet("README");
		adjustableParamsSheet = wb.createSheet("Parameter Settings");
		rupProbSheet = wb.createSheet("Rupture Probability");
		rupGainSheet = wb.createSheet("Rupture Gain");
		segProbSheet = wb.createSheet("Segment Probability");
		segGainSheet = wb.createSheet("Segment Gain");
		segRateSheet = wb.createSheet("Segment Rate");
		ucerf2.getParameter(UCERF2.SEG_DEP_APERIODICITY_PARAM_NAME).setValue(SEG_DEP_APERIODICITY);
		ucerf2.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).setValue(PROB_MODEL_VAL);
		ucerf2.getTimeSpan().setDuration(DURATION); // Set duration 
		
		// Save names of all adjustable parameters
		ParameterList adjustableParams = ucerf2.getAdjustableParameterList();
		Iterator it = adjustableParams.getParametersIterator();
		adjustableParamNames = new ArrayList<String>();
		while(it.hasNext()) {
			 ParameterAPI param = (ParameterAPI)it.next();
			 adjustableParamNames.add(param.getName());
		 }
		
		
		// add timespan parameters
		it = ucerf2.getTimeSpan().getAdjustableParams().getParametersIterator();
		while(it.hasNext()) {
			 ParameterAPI param = (ParameterAPI)it.next();
			 adjustableParamNames.add(param.getName());
		 }
		
		// create bold font style
		HSSFFont boldFont = wb.createFont();
		boldFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		boldStyle = wb.createCellStyle();
		boldStyle.setFont(boldFont);
		
		calcLogicTreeBranch(0, 1);
		
		// write weight averaged/min/max columns
		writeWeightAvMinMaxCols();
		
		// write README
		readmeSheet.setColumnWidth((short)0,(short) (51200)); // 256 * number of desired characters
		HSSFCellStyle wrapCellStyle = wb.createCellStyle();
		wrapCellStyle.setWrapText(true);
		readmeSheet.createRow(0).createCell((short)0).setCellStyle(wrapCellStyle);
		readmeSheet.getRow(0).getCell((short)0).setCellValue(README_TEXT);
		
		
		// write  excel sheet
		try {
			FileOutputStream fileOut = new FileOutputStream(FILENAME);
			wb.write(fileOut);
			fileOut.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Write Weight averaged and min/max columns
	 *
	 */
	private void writeWeightAvMinMaxCols() {
		int rupRowIndex = 0, segRowIndex=0;
		int colIndex = loginTreeBranchIndex+1;
		
		boolean isPoisson = ((String)(ucerf2.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).getValue())).equalsIgnoreCase(UCERF2.PROB_MODEL_POISSON);
		
		rupProbSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Weighted Average");
		rupProbSheet.createRow(rupRowIndex).createCell((short)(colIndex+1)).setCellValue("Min");
		rupProbSheet.createRow(rupRowIndex).createCell((short)(colIndex+2)).setCellValue("Max");
		rupGainSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Weighted Average");
		rupGainSheet.createRow(rupRowIndex).createCell((short)(colIndex+1)).setCellValue("Min");
		rupGainSheet.createRow(rupRowIndex).createCell((short)(colIndex+2)).setCellValue("Max");
		segProbSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Weighted Average");
		segProbSheet.createRow(segRowIndex).createCell((short)(colIndex+1)).setCellValue("Min");
		segProbSheet.createRow(segRowIndex).createCell((short)(colIndex+2)).setCellValue("Max");
		segGainSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Weighted Average");
		segGainSheet.createRow(segRowIndex).createCell((short)(colIndex+1)).setCellValue("Min");
		segGainSheet.createRow(segRowIndex).createCell((short)(colIndex+2)).setCellValue("Max");
		segRateSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Weighted Average");
		segRateSheet.createRow(segRowIndex).createCell((short)(colIndex+1)).setCellValue("Min");
		segRateSheet.createRow(segRowIndex).createCell((short)(colIndex+2)).setCellValue("Max");
		++rupRowIndex;
		++segRowIndex;
		++rupRowIndex;
		++segRowIndex;
		
		// loop over all faults
		int totRupsIndex=0, totSegsIndex=0;
		ArrayList<A_FaultSegmentedSourceGenerator> aFaultGenerators = ucerf2.get_A_FaultSourceGenerators();
		for(int fltGenIndex=0; fltGenIndex<aFaultGenerators.size(); ++fltGenIndex, ++rupRowIndex, ++segRowIndex) {
			A_FaultSegmentedSourceGenerator sourceGen = aFaultGenerators.get(fltGenIndex);
			
			int numRups = sourceGen.getNumRupSources();
			++rupRowIndex;
			// loop over all ruptures
			for(int rupIndex=0; rupIndex<numRups; ++rupIndex, ++totRupsIndex) {
				rupProbSheet.getRow(rupRowIndex).createCell((short)colIndex).setCellValue(rupProbWtAve.get(totRupsIndex));
				rupProbSheet.getRow(rupRowIndex).createCell((short)(colIndex+1)).setCellValue(rupProbMin.get(totRupsIndex));
				rupProbSheet.getRow(rupRowIndex).createCell((short)(colIndex+2)).setCellValue(rupProbMax.get(totRupsIndex));
				rupGainSheet.getRow(rupRowIndex).createCell((short)colIndex).setCellValue(rupGainWtAve.get(totRupsIndex));
				rupGainSheet.getRow(rupRowIndex).createCell((short)(colIndex+1)).setCellValue(rupGainMin.get(totRupsIndex));
				rupGainSheet.getRow(rupRowIndex).createCell((short)(colIndex+2)).setCellValue(rupGainMax.get(totRupsIndex));
				++rupRowIndex;
			}

			rupProbSheet.getRow(rupRowIndex).createCell((short)colIndex).setCellValue(rupProbWtAve.get(totRupsIndex));
			rupProbSheet.getRow(rupRowIndex).createCell((short)(colIndex+1)).setCellValue(rupProbMin.get(totRupsIndex));
			rupProbSheet.getRow(rupRowIndex).createCell((short)(colIndex+2)).setCellValue(rupProbMax.get(totRupsIndex));
			rupGainSheet.getRow(rupRowIndex).createCell((short)colIndex).setCellValue(rupGainWtAve.get(totRupsIndex));
			rupGainSheet.getRow(rupRowIndex).createCell((short)(colIndex+1)).setCellValue(rupGainMin.get(totRupsIndex));
			rupGainSheet.getRow(rupRowIndex).createCell((short)(colIndex+2)).setCellValue(rupGainMax.get(totRupsIndex));
			++totRupsIndex;
			++rupRowIndex;
			
			
			FaultSegmentData faultSegData = sourceGen.getFaultSegmentData();
			int numSegs = faultSegData.getNumSegments();
			++segRowIndex;
			
			// loop over all segments
			for(int segIndex=0; segIndex<numSegs; ++segIndex, ++totSegsIndex) {
				segProbSheet.getRow(segRowIndex).createCell((short)colIndex).setCellValue(segProbWtAve.get(totSegsIndex));
				segProbSheet.getRow(segRowIndex).createCell((short)(colIndex+1)).setCellValue(segProbMin.get(totSegsIndex));
				segProbSheet.getRow(segRowIndex).createCell((short)(colIndex+2)).setCellValue(segProbMax.get(totSegsIndex));
				segGainSheet.getRow(segRowIndex).createCell((short)colIndex).setCellValue(segGainWtAve.get(totSegsIndex));
				segGainSheet.getRow(segRowIndex).createCell((short)(colIndex+1)).setCellValue(segGainMin.get(totSegsIndex));
				segGainSheet.getRow(segRowIndex).createCell((short)(colIndex+2)).setCellValue(segGainMax.get(totSegsIndex));
				segRateSheet.getRow(segRowIndex).createCell((short)colIndex).setCellValue(segRateWtAve.get(totSegsIndex));
				segRateSheet.getRow(segRowIndex).createCell((short)(colIndex+1)).setCellValue(segRateMin.get(totSegsIndex));
				segRateSheet.getRow(segRowIndex).createCell((short)(colIndex+2)).setCellValue(segRateMax.get(totSegsIndex));

				++segRowIndex;
			}
		}
	}
	
		
	/**
	 * Paramters that are adjusted in the runs
	 *
	 */
	private void fillAdjustableParams() {
		this.paramNames = new ArrayList<String>();
		this.paramValues = new ArrayList<ParamOptions>();
		
		
		// Mag Area Rel
		paramNames.add(UCERF2.MAG_AREA_RELS_PARAM_NAME);
		ParamOptions options = new ParamOptions();
		options.addValueWeight(Ellsworth_B_WG02_MagAreaRel.NAME, 0.5);
		options.addValueWeight(HanksBakun2002_MagAreaRel.NAME, 0.5);
		paramValues.add(options);
		
		// Aprioti wt param
		paramNames.add(UCERF2.REL_A_PRIORI_WT_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(new Double(1e-4), 0.5);
		options.addValueWeight(new Double(1e10), 0.5);
		paramValues.add(options);
		
		// Mag Correction
		paramNames.add(UCERF2.MEAN_MAG_CORRECTION);
		options = new ParamOptions();
		options.addValueWeight(new Double(-0.1), 0.2);
		options.addValueWeight(new Double(0), 0.6);
		options.addValueWeight(new Double(0.1), 0.2);
		paramValues.add(options);
		
		// Aperiodicity
		/*paramNames.add(UCERF2.SEG_DEP_APERIODICITY_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(new Boolean(false), 0.5);
		options.addValueWeight(new Boolean(true), 0.5);
		paramValues.add(options);*/
	
	}
	
	
	/**
	 * Calculate MFDs
	 * 
	 * @param paramIndex
	 * @param weight
	 */
	private void calcLogicTreeBranch(int paramIndex, double weight) {
		
		ParamOptions options = paramValues.get(paramIndex);
		String paramName = paramNames.get(paramIndex);
		int numValues = options.getNumValues();
		for(int i=0; i<numValues; ++i) {
			if(ucerf2.getAdjustableParameterList().containsParameter(paramName)) {
				ucerf2.getParameter(paramName).setValue(options.getValue(i));	
				if(paramName.equalsIgnoreCase(UCERF2.REL_A_PRIORI_WT_PARAM_NAME)) {
					ParameterAPI param = ucerf2.getParameter(UCERF2.REL_A_PRIORI_WT_PARAM_NAME);
					if(((Double)param.getValue()).doubleValue()==1e10) {
						ucerf2.getParameter(UCERF2.MIN_A_FAULT_RATE_1_PARAM_NAME).setValue(new Double(0.0));
						ucerf2.getParameter(UCERF2.MIN_A_FAULT_RATE_2_PARAM_NAME).setValue(new Double(0.0));	
					} else {
						ucerf2.getParameter(UCERF2.MIN_A_FAULT_RATE_1_PARAM_NAME).setValue(UCERF2.MIN_A_FAULT_RATE_1_DEFAULT);
						ucerf2.getParameter(UCERF2.MIN_A_FAULT_RATE_2_PARAM_NAME).setValue(UCERF2.MIN_A_FAULT_RATE_2_DEFAULT);	
					}
				}
			}
			double newWt = weight * options.getWeight(i);
			if(paramIndex==lastParamIndex) { // if it is last paramter in list, write the Rupture Rates to excel sheet
				
				
				System.out.println("Doing run:"+(this.loginTreeBranchIndex+1));
				ucerf2.updateForecast();
				ArrayList<A_FaultSegmentedSourceGenerator> aFaultGenerators = ucerf2.get_A_FaultSourceGenerators();
				if(this.loginTreeBranchIndex==0) {
					
					segProbWtAve = new ArrayList<Double>();
					segProbMin = new ArrayList<Double>(); 
					segProbMax = new ArrayList<Double>();
					
					segGainWtAve = new ArrayList<Double>();
					segGainMin = new ArrayList<Double>(); 
					segGainMax = new ArrayList<Double>();
					
					segRateWtAve = new ArrayList<Double>();
					segRateMin = new ArrayList<Double>(); 
					segRateMax = new ArrayList<Double>();
					
					rupProbWtAve = new ArrayList<Double>();
					rupProbMin = new ArrayList<Double>();
					rupProbMax = new ArrayList<Double>();
					
					rupGainWtAve = new ArrayList<Double>();
					rupGainMin = new ArrayList<Double>();
					rupGainMax = new ArrayList<Double>();
					
					
					int rupRowIndex = 0, segRowIndex=0;
					int colIndex = loginTreeBranchIndex;
					rupProbSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Rupture Name");
					rupGainSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Rupture Name");
					segProbSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Segment Name");
					segGainSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Segment Name");
					segRateSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Segment Name");
					++rupRowIndex;
					++segRowIndex;
					++rupRowIndex;
					++segRowIndex;
					
					// loop over all faults
					for(int fltGenIndex=0; fltGenIndex<aFaultGenerators.size(); ++fltGenIndex, ++rupRowIndex, ++segRowIndex) {
						A_FaultSegmentedSourceGenerator sourceGen = aFaultGenerators.get(fltGenIndex);
						
						rupProbSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue(sourceGen.getFaultSegmentData().getFaultName());
						rupGainSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue(sourceGen.getFaultSegmentData().getFaultName());
						int numRups = sourceGen.getNumRupSources();
						++rupRowIndex;
						// loop over all ruptures
						for(int rupIndex=0; rupIndex<numRups; ++rupIndex) {
							rupProbSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue(sourceGen.getLongRupName(rupIndex));
							rupGainSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue(sourceGen.getLongRupName(rupIndex));
							++rupRowIndex;
							rupProbWtAve.add(0.0);
							rupProbWtAve.add(0.0);
							rupProbMin.add(Double.MAX_VALUE);
							rupProbMax.add(0.0);
							
							rupGainWtAve.add(0.0);
							rupGainWtAve.add(0.0);
							rupGainMin.add(Double.MAX_VALUE);
							rupGainMax.add(0.0);
						}
						rupProbSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Total Probability");
						rupGainSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Total Gain");
						
						rupProbWtAve.add(0.0);
						rupProbMin.add(Double.MAX_VALUE);
						rupProbMax.add(0.0);
						
						rupGainWtAve.add(0.0);
						rupGainMin.add(Double.MAX_VALUE);
						rupGainMax.add(0.0);
						
						++rupRowIndex;
						
						segProbSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(sourceGen.getFaultSegmentData().getFaultName());
						segGainSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(sourceGen.getFaultSegmentData().getFaultName());
						segRateSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(sourceGen.getFaultSegmentData().getFaultName());
						FaultSegmentData faultSegData = sourceGen.getFaultSegmentData();
						int numSegs = faultSegData.getNumSegments();
						++segRowIndex;
						// loop over all segments
						for(int segIndex=0; segIndex<numSegs; ++segIndex) {
							segProbSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(faultSegData.getSegmentName(segIndex));
							segGainSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(faultSegData.getSegmentName(segIndex));
							segRateSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(faultSegData.getSegmentName(segIndex));
							++segRowIndex;
							
							segProbWtAve.add(0.0);
							segProbMin.add(Double.MAX_VALUE);
							segProbMax.add(0.0);
							
							segGainWtAve.add(0.0);
							segGainMin.add(Double.MAX_VALUE);
							segGainMax.add(0.0);
							
							segRateWtAve.add(0.0);
							segRateMin.add(Double.MAX_VALUE);
							segRateMax.add(0.0);
						}
						
					}
					rupProbSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Branch Weight");
					rupGainSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Branch Weight");
					segProbSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Branch Weight");
					segGainSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Branch Weight");
					segRateSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Branch Weight");
					
					// write adjustable parameters
					// add a row for each parameter name. Also add a initial blank row for writing Branch names
					HSSFRow row = this.adjustableParamsSheet.createRow(0);
					row.createCell((short)0).setCellValue("Parameters"); 
					for(int p=1; p<=adjustableParamNames.size(); ++p) {
						String adjParamName = adjustableParamNames.get(p-1);
						HSSFCell cell = adjustableParamsSheet.createRow(p).createCell((short)0);
						if(this.paramNames.contains(adjParamName) ||
								adjParamName.equalsIgnoreCase(UCERF2.MIN_A_FAULT_RATE_1_PARAM_NAME) ||
								adjParamName.equalsIgnoreCase(UCERF2.MIN_A_FAULT_RATE_2_PARAM_NAME)) 
							cell.setCellStyle(this.boldStyle);
						cell.setCellValue(adjParamName);
					}
					
					
				}
				
				loginTreeBranchIndex++;
				int rupRowIndex = 0, segRowIndex=0;
				int colIndex = loginTreeBranchIndex;
				rupProbSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Branch "+loginTreeBranchIndex);
				rupGainSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Branch "+loginTreeBranchIndex);
				segProbSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Branch "+loginTreeBranchIndex);
				segGainSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Branch "+loginTreeBranchIndex);
				segRateSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Branch "+loginTreeBranchIndex);
				adjustableParamsSheet.createRow(0).createCell((short)colIndex).setCellValue("Branch "+loginTreeBranchIndex);
				
				++rupRowIndex;
				++segRowIndex;
				++rupRowIndex;
				++segRowIndex;
				
				// loop over all faults
				int totRupsIndex=0, totSegsIndex=0;
				for(int fltGenIndex=0; fltGenIndex<aFaultGenerators.size(); ++fltGenIndex, ++rupRowIndex, ++segRowIndex) {
					A_FaultSegmentedSourceGenerator sourceGen = aFaultGenerators.get(fltGenIndex);
					
					int numRups = sourceGen.getNumRupSources();
					++rupRowIndex;
					// loop over all ruptures
					double rupProb, rupGain;
					for(int rupIndex=0; rupIndex<numRups; ++rupIndex, ++totRupsIndex) {
						rupProb = sourceGen.getRupSourceProb(rupIndex);
						
						// wt and min/max columns
						rupProbWtAve.set(totRupsIndex, rupProbWtAve.get(totRupsIndex)+newWt*rupProb);
						if(rupProbMin.get(totRupsIndex) > rupProb) rupProbMin.set(totRupsIndex, rupProb);
						if(rupProbMax.get(totRupsIndex) < rupProb) rupProbMax.set(totRupsIndex, rupProb);
						rupProbSheet.getRow(rupRowIndex).createCell((short)colIndex).setCellValue(rupProb);
						
						rupGain = sourceGen.getRupSourcProbGain(rupIndex);
						rupGainWtAve.set(totRupsIndex, rupGainWtAve.get(totRupsIndex)+newWt*rupGain);
						if(rupGainMin.get(totRupsIndex) > rupGain) rupGainMin.set(totRupsIndex, rupGain);
						if(rupGainMax.get(totRupsIndex) < rupGain) rupGainMax.set(totRupsIndex, rupGain);
						rupGainSheet.getRow(rupRowIndex).createCell((short)colIndex).setCellValue(rupGain);
						
						++rupRowIndex;
					}
					
					rupProb = sourceGen.getTotFaultProb();
					// wt and min/max columns
					rupProbWtAve.set(totRupsIndex, rupProbWtAve.get(totRupsIndex)+newWt*rupProb);
					if(rupProbMin.get(totRupsIndex) > rupProb) rupProbMin.set(totRupsIndex, rupProb);
					if(rupProbMax.get(totRupsIndex) < rupProb) rupProbMax.set(totRupsIndex, rupProb);
					rupProbSheet.getRow(rupRowIndex).createCell((short)colIndex).setCellValue(rupProb);
					
					rupGain = sourceGen.getTotFaultProbGain();
					rupGainWtAve.set(totRupsIndex, rupGainWtAve.get(totRupsIndex)+newWt*rupGain);
					if(rupGainMin.get(totRupsIndex) > rupGain) rupGainMin.set(totRupsIndex, rupGain);
					if(rupGainMax.get(totRupsIndex) < rupGain) rupGainMax.set(totRupsIndex, rupGain);
					rupGainSheet.getRow(rupRowIndex).createCell((short)colIndex).setCellValue(rupGain);
					
					++totRupsIndex;
					++rupRowIndex;
					
					
					FaultSegmentData faultSegData = sourceGen.getFaultSegmentData();
					int numSegs = faultSegData.getNumSegments();
					++segRowIndex;
					double segProb, segGain, segRate;
					// loop over all segments
					for(int segIndex=0; segIndex<numSegs; ++segIndex, ++totSegsIndex) {
						segProb = sourceGen.getSegProb(segIndex);
						//	wt and min/max columns
						segProbWtAve.set(totSegsIndex, segProbWtAve.get(totSegsIndex)+newWt*segProb);
						if(segProbMin.get(totSegsIndex) > segProb) segProbMin.set(totSegsIndex, segProb);
						if(segProbMax.get(totSegsIndex) < segProb) segProbMax.set(totSegsIndex, segProb);
						segProbSheet.getRow(segRowIndex).createCell((short)colIndex).setCellValue(segProb);

						segGain = sourceGen.getSegProbGain(segIndex);
						//	wt and min/max columns
						segGainWtAve.set(totSegsIndex, segGainWtAve.get(totSegsIndex)+newWt*segGain);
						if(segGainMin.get(totSegsIndex) > segGain) segGainMin.set(totSegsIndex, segGain);
						if(segGainMax.get(totSegsIndex) < segGain) segGainMax.set(totSegsIndex, segGain);
						segGainSheet.getRow(segRowIndex).createCell((short)colIndex).setCellValue(segGain);
						segGainSheet.getRow(segRowIndex).createCell((short)colIndex).setCellValue(segGain);
						
						segRate = sourceGen.getFinalSegmentRate(segIndex);
						//	wt and min/max columns
						segRateWtAve.set(totSegsIndex, segRateWtAve.get(totSegsIndex)+newWt*segRate);
						if(segRateMin.get(totSegsIndex) > segRate) segRateMin.set(totSegsIndex, segRate);
						if(segRateMax.get(totSegsIndex) < segRate) segRateMax.set(totSegsIndex, segRate);
						segRateSheet.getRow(segRowIndex).createCell((short)colIndex).setCellValue(segRate);
						segRateSheet.getRow(segRowIndex).createCell((short)colIndex).setCellValue(segRate);
						++segRowIndex;
					}
				}
				
				rupProbSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue(newWt);
				rupGainSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue(newWt);
				segProbSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(newWt);
				segGainSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(newWt);
				segRateSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(newWt);
				
				// Write adjustable parameters
				// add a row for each parameter name. Also add a initial blank row for writing Branch names 
				ParameterList paramList = ucerf2.getAdjustableParameterList();
				ParameterList timeSpanParamList = ucerf2.getTimeSpan().getAdjustableParams();
				for(int p=1; p<=adjustableParamNames.size(); ++p) {
					String parameterName = adjustableParamNames.get(p-1);
					if(paramList.containsParameter(parameterName)) {
						HSSFCell cell = adjustableParamsSheet.getRow(p).createCell((short)loginTreeBranchIndex);
						if(this.paramNames.contains(parameterName) ||
								parameterName.equalsIgnoreCase(UCERF2.MIN_A_FAULT_RATE_1_PARAM_NAME) ||
								parameterName.equalsIgnoreCase(UCERF2.MIN_A_FAULT_RATE_2_PARAM_NAME)) 
							cell.setCellStyle(this.boldStyle);
						cell.setCellValue(paramList.getValue(parameterName).toString());
					}
					else if(timeSpanParamList.containsParameter(parameterName))
						adjustableParamsSheet.getRow(p).createCell((short)loginTreeBranchIndex).setCellValue(timeSpanParamList.getValue(parameterName).toString());
				}

			} else { // recursion 
				calcLogicTreeBranch(paramIndex+1, newWt);
			}
		}
	}
	
	public static void main(String []args) {
		WriteTimeDepRupProbAndGain rupProbWriter = new WriteTimeDepRupProbAndGain();
	}
}

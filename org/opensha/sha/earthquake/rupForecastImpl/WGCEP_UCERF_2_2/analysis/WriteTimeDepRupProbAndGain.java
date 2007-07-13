package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.analysis;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
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
	private ArrayList<String> paramNames;
	private ArrayList<ParamOptions> paramValues;
	private int lastParamIndex;
	private UCERF2 ucerf2;
	private HSSFSheet rupProbSheet, rupGainSheet, segProbSheet, segGainSheet, adjustableParamsSheet;
	private int loginTreeBranchIndex = 0;
	private final static String FILENAME = "RupSegProbGain.xls";
	private static double DURATION = 30;
	private ArrayList<String> adjustableParamNames;
	
	public WriteTimeDepRupProbAndGain() {
		this(new UCERF2());
	}
	

	public WriteTimeDepRupProbAndGain(UCERF2 ucerf2) {
		this.ucerf2 = ucerf2;
		fillAdjustableParams();
		lastParamIndex = paramNames.size()-1;
		HSSFWorkbook wb  = new HSSFWorkbook();
		adjustableParamsSheet = wb.createSheet("Parameter Settings");
		rupProbSheet = wb.createSheet("Rupture Probability");
		rupGainSheet = wb.createSheet("Rupture Gain");
		segProbSheet = wb.createSheet("Segment Probability");
		segGainSheet = wb.createSheet("Segment Gain");
		ucerf2.getTimeSpan().setDuration(DURATION); // Set duration to be 30 years
		
		// Save names of all adjustable parameters
		ParameterList adjustableParams = ucerf2.getAdjustableParameterList();
		Iterator it = adjustableParams.getParametersIterator();
		adjustableParamNames = new ArrayList<String>();
		while(it.hasNext()) {
			 ParameterAPI param = (ParameterAPI)it.next();
			 adjustableParamNames.add(param.getName());
		 }
		
	
		calcLogicTreeBranch(0, 1);
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
		paramNames.add(UCERF2.SEG_DEP_APERIODICITY_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(new Boolean(false), 0.5);
		options.addValueWeight(new Boolean(true), 0.5);
		paramValues.add(options);
	
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
					
					int rupRowIndex = 0, segRowIndex=0;
					int colIndex = loginTreeBranchIndex;
					rupProbSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Rupture Name");
					rupGainSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Rupture Name");
					segProbSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Segment Name");
					segGainSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Segment Name");
					++rupRowIndex;
					++segRowIndex;
					
					// loop over all faults
					for(int fltGenIndex=0; fltGenIndex<aFaultGenerators.size(); ++fltGenIndex) {
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
						}
						
						segProbSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(sourceGen.getFaultSegmentData().getFaultName());
						segGainSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(sourceGen.getFaultSegmentData().getFaultName());
						FaultSegmentData faultSegData = sourceGen.getFaultSegmentData();
						int numSegs = faultSegData.getNumSegments();
						++segRowIndex;
						// loop over all segments
						for(int segIndex=0; segIndex<numSegs; ++segIndex) {
							segProbSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(faultSegData.getSegmentName(segIndex));
							segGainSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(faultSegData.getSegmentName(segIndex));
							++segRowIndex;
						}
					}
					rupProbSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Branch Weight");
					rupGainSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Branch Weight");
					segProbSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Branch Weight");
					segGainSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Branch Weight");
					
					// write adjustable parameters
					// add a row for each parameter name. Also add a initial blank row for writing Branch names
					HSSFRow row = this.adjustableParamsSheet.createRow(0);
					row.createCell((short)0).setCellValue("Parameters"); 
					for(int p=1; p<=adjustableParamNames.size(); ++p) {
						adjustableParamsSheet.createRow(p).createCell((short)0).setCellValue(adjustableParamNames.get(p-1));
					}
					
					
				}
				
				loginTreeBranchIndex++;
				int rupRowIndex = 0, segRowIndex=0;
				int colIndex = loginTreeBranchIndex;
				rupProbSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Branch "+loginTreeBranchIndex);
				rupGainSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue("Branch "+loginTreeBranchIndex);
				segProbSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Branch "+loginTreeBranchIndex);
				segGainSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue("Branch "+loginTreeBranchIndex);
				adjustableParamsSheet.createRow(0).createCell((short)colIndex).setCellValue("Branch "+loginTreeBranchIndex);
				
				++rupRowIndex;
				++segRowIndex;
				
				// loop over all faults
				for(int fltGenIndex=0; fltGenIndex<aFaultGenerators.size(); ++fltGenIndex) {
					A_FaultSegmentedSourceGenerator sourceGen = aFaultGenerators.get(fltGenIndex);
					
					int numRups = sourceGen.getNumRupSources();
					++rupRowIndex;
					// loop over all ruptures
					for(int rupIndex=0; rupIndex<numRups; ++rupIndex) {
						rupProbSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue(sourceGen.getRupSourceProb(rupIndex));
						rupGainSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue(sourceGen.getRupSourcGain(rupIndex));
						++rupRowIndex;
					}
					
					FaultSegmentData faultSegData = sourceGen.getFaultSegmentData();
					int numSegs = faultSegData.getNumSegments();
					++segRowIndex;
					// loop over all segments
					for(int segIndex=0; segIndex<numSegs; ++segIndex) {
						segProbSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(sourceGen.getSegProb(segIndex));
						segGainSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(sourceGen.getSegGain(segIndex));
						++segRowIndex;
					}
				}
				
				rupProbSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue(newWt);
				rupGainSheet.createRow(rupRowIndex).createCell((short)colIndex).setCellValue(newWt);
				segProbSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(newWt);
				segGainSheet.createRow(segRowIndex).createCell((short)colIndex).setCellValue(newWt);
				
				// Write adjustable parameters
				// add a row for each parameter name. Also add a initial blank row for writing Branch names 
				ParameterList paramList = ucerf2.getAdjustableParameterList();
				for(int p=1; p<=adjustableParamNames.size(); ++p) {
					String parameterName = adjustableParamNames.get(p-1);
					if(paramList.containsParameter(parameterName))
						adjustableParamsSheet.getRow(p).createCell((short)loginTreeBranchIndex).setCellValue(paramList.getValue(parameterName).toString());
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

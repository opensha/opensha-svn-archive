/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.param.ParameterListParameter;
import org.opensha.param.StringConstraint;
import org.opensha.param.StringParameter;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.A_Faults.A_FaultSegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.data.A_FaultsFetcher;

/**
 * Generate the test excel sheets
 * @author vipingupta
 *
 */
public class GenerateTestExcelSheets {
	private EqkRateModel2_ERF eqkRateModelERF;
	private ParameterAPI magAreaRelParam, slipModelParam;
	private ParameterListParameter segmentedRupModelParam;
	private ParameterList adjustableParams;
	private ArrayList aFaultSources ;
	private A_FaultsFetcher aFaultsFetcher;
	private ArrayList magAreaOptions, slipModelOptions;
	
	
	public GenerateTestExcelSheets(EqkRateModel2_ERF eqkRateModelERF) {
		this.eqkRateModelERF = eqkRateModelERF;
		adjustableParams = eqkRateModelERF.getAdjustableParameterList();
		magAreaRelParam = eqkRateModelERF.getParameter(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME);
		segmentedRupModelParam = (ParameterListParameter)eqkRateModelERF.getParameter(EqkRateModel2_ERF.SEGMENTED_RUP_MODEL_TYPE_NAME);
		slipModelParam = eqkRateModelERF.getParameter(EqkRateModel2_ERF.SLIP_MODEL_TYPE_NAME);
		aFaultsFetcher = eqkRateModelERF.getA_FaultsFetcher();
		magAreaOptions = ((StringConstraint)magAreaRelParam.getConstraint()).getAllowedStrings();
		slipModelOptions = ((StringConstraint)slipModelParam.getConstraint()).getAllowedStrings();

	}
	
	/**
	 * Generate Excel sheet for each fault.
	 * Each sheet will have all Rup solution Types
	 * 
	 */
	public void generateExcelSheetsForRupMagRates(String outputFileName) {		
		System.out.println(outputFileName);
		int numA_Faults = this.aFaultsFetcher.getAllFaultNames().size();	
//		 Create Excel Workbook and sheets if they do not exist already
		
		HSSFWorkbook wb  = new HSSFWorkbook();
		HSSFCellStyle cellStyle = wb.createCellStyle();
		HSSFFont font = wb.createFont();
		font.setColor(HSSFFont.COLOR_RED);
		cellStyle.setFont(font);
		//currRow = new int[aFaultSources.size()];
		{
		// METADATA SHEET
		HSSFSheet metadataSheet = wb.createSheet(); // Sheet for displaying the Total Rates
		wb.setSheetName(0, "README");
		Iterator it = this.adjustableParams.getParametersIterator();
		String str = "This file contains final (post-inversion) rupture rates for the four " +
				"different magnitude-area relationships, the four slip models, " +
				"and the three solution types (min rate, max rate, and geologic insight).  " +
				"All other parameters were set as listed below.  The sheet for each fault lists" +
				" the following for each solution type: rupture name; the a-prior rate; " +
				"the characteristic magnitude and characteristic rate resulting from the " +
				"characteristic-slip model (which does not use a magnitude-area relationship); " +
				"and the rates for the other three slip models for each magnitude-area relationship" +
				" (twelve columns).  Listed at the bottom of the sheet for each fault are the " +
				"following total-rate ratios: min/geol, max/geol, and max/min " +
				"(useful for seeing the extent to which the different a-priori models " +
				"converge to the same final rates).  The \"Total Rates\" sheet lists the " +
				"total rates (summed over all faults) for each case.  The \"Gen. Pred. Err\" sheet" +
				" lists the generalized prediction errors for each case (smaller values mean " +
				"a better overall fit to the slip-rate and total segment event-rate data)." ;
		metadataSheet.createRow(0).createCell((short)0).setCellValue(str);
		
		int row = 1;
		while(it.hasNext()) {
			ParameterAPI param = (ParameterAPI)it.next();
			if(param.getName().equals(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME) || param.getName().equals(EqkRateModel2_ERF.SLIP_MODEL_TYPE_NAME) ||
					param.getName().equals(EqkRateModel2_ERF.SEGMENTED_RUP_MODEL_TYPE_NAME)) continue;
			metadataSheet.createRow(row++).createCell((short)0).setCellValue(param.getMetadataString());
		}
		}
		
		// SHEETS FOR EACH A-FAULT
		for(int i=0; i<numA_Faults; ++i) {
			wb.createSheet();
			//currRow[i]=0;
		}
		HSSFSheet genPredErrSheet = wb.createSheet(); // Sheet for displaying the General Prediction error
		wb.setSheetName(wb.getNumberOfSheets()-1, "Gen. Pred. Err");
		HSSFSheet totalRatesSheet = wb.createSheet(); // Sheet for displaying the Total Rates
		wb.setSheetName(wb.getNumberOfSheets()-1, "Total Rates");
		int currRow[] = new int[numA_Faults];
		for(int irup=0; irup<3;irup++) {
			int rupStartRow[] = new int[numA_Faults];	
			Iterator it = this.segmentedRupModelParam.getParametersIterator();
			while(it.hasNext()) { // set the specfiied rup model in each A fault
				StringParameter param = (StringParameter)it.next();
				ArrayList<String> allowedVals = param.getAllowedStrings();
				param.setValue(allowedVals.get(irup));
			}
			
			for(int imag=0; imag<magAreaOptions.size();imag++) {
				//int numSlipModels = slipModelOptions.size();
				//double magRate[][] = new double[numSlipModels][2];
				for(int islip=0; islip<slipModelOptions.size();islip++) {
			
						magAreaRelParam.setValue(magAreaOptions.get(imag));
						
						slipModelParam.setValue(slipModelOptions.get(islip));
						this.eqkRateModelERF.updateForecast();
						aFaultSources = eqkRateModelERF.get_A_FaultSources();
						this.genPredErrAndTotRateSheet( genPredErrSheet, totalRatesSheet, imag, islip,irup, cellStyle);
						// Write header for each Rup Solution Types
						if(imag==0 && islip==0) {
							// do for each fault
							for(int i=0; i<aFaultSources.size(); ++i) {
								HSSFSheet sheet = wb.getSheetAt(i+1); // first sheet is metadata sheet
								String sheetName = ((A_FaultSegmentedSource)aFaultSources.get(i)).getFaultSegmentData().getFaultName();
								wb.setSheetName(i+1, sheetName);
								HSSFRow row;
								generateExcelSheetHeader(cellStyle, currRow[i], irup, sheet);		
								currRow[i]+=3;
								 // write Rup Names and Apriori Rates
								 A_FaultSegmentedSource source = (A_FaultSegmentedSource) aFaultSources.get(i);
								 rupStartRow[i] = currRow[i];
								 for(int rup=0; rup<source.getNumRuptures(); ++rup) {
									 row = sheet.createRow((short)currRow[i]++);
									 row.createCell((short)0).setCellValue(source.getLongRupName(rup));
									 row.createCell((short)1).setCellValue(source.getAPrioriRupRate(rup));
								 }
								 // write totals
								 row = sheet.createRow((short)currRow[i]++);
								 int totRow1=currRow[i];
								 int totRow2 = 2*totRow1+2;
								 int totRow3 = 3*totRow1+4;
								 int ratioRowIndex1 = totRow3+2;
								 createTotalRow( row, rupStartRow[i], rupStartRow[i]+source.getNumRuptures());
								 if(irup==0){
									 this.createRatioRows(sheet, ratioRowIndex1, totRow1, totRow2, totRow3);
								 }	 
							}
						}
					
						// write the rup Mag and rates
						for(int i=0; i<this.aFaultSources.size(); ++i) {
							 HSSFSheet sheet = wb.getSheetAt(i+1);// first sheet is metadata
							 A_FaultSegmentedSource source = (A_FaultSegmentedSource) aFaultSources.get(i);
							 int magCol = this.getMagCol(islip, imag);
							 int rateCol = this.getRateCol(islip, imag);
							 for(int rup=0; rup<source.getNumRuptures(); ++rup) {
								 sheet.getRow(rup+rupStartRow[i]).createCell((short)magCol).setCellValue(source.getRupMeanMag(rup));
								 sheet.getRow(rup+rupStartRow[i]).createCell((short)rateCol).setCellValue(source.getRupRate(rup));
							 }
						}
					}
			}
			// 
			for(int i=1; i<(wb.getNumberOfSheets()-2); ++i) { // do not do for Gen Pred Error and Tot rates
				HSSFSheet sheet = wb.getSheetAt(i);
				sheet.createRow((short)currRow[i-1]++);
				sheet.createRow((short)currRow[i-1]++);
			}
			
		}
		try {
			FileOutputStream fileOut = new FileOutputStream(outputFileName);
			wb.write(fileOut);
			fileOut.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private int getRateCol(int islip, int imag) {
		 if(slipModelOptions.get(islip).equals(A_FaultSegmentedSource.CHAR_SLIP_MODEL)) 
			 return 3;
		 else 
			 return getMagCol(islip,imag)+islip;
			
	}
	
	private int getMagCol(int islip, int imag) {
		 if(slipModelOptions.get(islip).equals(A_FaultSegmentedSource.CHAR_SLIP_MODEL))
			 return  2; 
		 else 
			 return 4 + imag*slipModelOptions.size();
	}
	
	
	private void createRatioRows(HSSFSheet sheet, int ratioRowIndex1, int totRow1, int totRow2, int totRow3) {
		HSSFRow ratioRow1=null, ratioRow2=null, ratioRow3=null;
		ratioRow1 = sheet.createRow((short)ratioRowIndex1);
		 ratioRow2 = sheet.createRow((short)(ratioRowIndex1+1));
		 ratioRow3 = sheet.createRow((short)(ratioRowIndex1+2));				 
		 ratioRow1.createCell((short)0).setCellValue("min/geol");	
		 ratioRow2.createCell((short)0).setCellValue("max/geol");	
		 ratioRow3.createCell((short)0).setCellValue("max/min");	
		 // a priori rate ratio
		 String colStr="B";
		 HSSFCell cell = ratioRow1.createCell((short)1);
		 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
		 cell.setCellFormula(colStr+totRow2+"/"+colStr+totRow1);
		 cell = ratioRow2.createCell((short)1);
		 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
		 cell.setCellFormula(colStr+totRow3+"/"+colStr+totRow1);
		 cell = ratioRow3.createCell((short)1);
		 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
		 cell.setCellFormula(colStr+totRow3+"/"+colStr+totRow2);
		 // Char rate ratio
		 colStr="D";
		 cell = ratioRow1.createCell((short)3);
		 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
		 cell.setCellFormula(colStr+totRow2+"/"+colStr+totRow1);
		 cell = ratioRow2.createCell((short)3);
		 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
		 cell.setCellFormula(colStr+totRow3+"/"+colStr+totRow1);
		 cell = ratioRow3.createCell((short)3);
		 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
		 cell.setCellFormula(colStr+totRow3+"/"+colStr+totRow2);
		 // totals for other rates
		 for(int k=0; k<slipModelOptions.size(); ++k) {
			 if(slipModelOptions.get(k).equals(A_FaultSegmentedSource.CHAR_SLIP_MODEL)) continue;
			 for(int j=0; j<magAreaOptions.size(); ++j) {
				 int totCol = 4 + j*slipModelOptions.size()+k;
				 colStr=""+(char)('A'+totCol);
				 cell = ratioRow1.createCell((short)totCol);
				 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
				 cell.setCellFormula(colStr+totRow2+"/"+colStr+totRow1);
				 cell = ratioRow2.createCell((short)totCol);
				 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
				 cell.setCellFormula(colStr+totRow3+"/"+colStr+totRow1);
				 cell = ratioRow3.createCell((short)totCol);
				 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
				 cell.setCellFormula(colStr+totRow3+"/"+colStr+totRow2);
			 }
		 }
	}
	
	private void createTotalRow(HSSFRow row, int sumStartIndex, int sumEndIndex) {
	

		 row.createCell((short)0).setCellValue("Totals");							 
		 // a priori rate total
		 HSSFCell cell = row.createCell((short)1);
		 String colStr="B";
		 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
		 cell.setCellFormula("SUM("+colStr+sumStartIndex+":"+colStr+(sumEndIndex+")"));
		 // Char rate total
		 cell = row.createCell((short)3);
		 colStr="D";
		 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
		 cell.setCellFormula("SUM("+colStr+sumStartIndex+":"+colStr+(sumEndIndex+")"));
		 // totals for other rates
		 for(int k=0; k<slipModelOptions.size(); ++k) {
			 if(slipModelOptions.get(k).equals(A_FaultSegmentedSource.CHAR_SLIP_MODEL)) continue;
			 for(int j=0; j<magAreaOptions.size(); ++j) {
				 int totCol = 4 + j*slipModelOptions.size()+k;
				 cell = row.createCell((short)totCol);
				 colStr=""+(char)('A'+totCol);
				 //System.out.println(colStr);
				 cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
				 cell.setCellFormula("SUM("+colStr+sumStartIndex+":"+colStr+(sumEndIndex+")"));
			 }
		 }
		 

	}
	

	/**
	 * Create header lines - mag area names, etc
	 * @param cellStyle
	 * @param rowIndex
	 * @param irup
	 * @param sheet
	 */
	private void generateExcelSheetHeader(HSSFCellStyle cellStyle, 
			int rowIndex, int irup,  HSSFSheet sheet) {
		
		String[] models = {"Geological Insight", "Min Rate", "Max Rate"};
		
		//System.out.println(currRow[i]);
		 HSSFRow row = sheet.createRow((short)rowIndex++);
		 // Write Rup solution Type
		 HSSFCell cell = row.createCell((short)0);
		 cell.setCellValue(models[irup]);
		 cell.setCellStyle(cellStyle);
		 row = sheet.createRow((short)rowIndex++);
		 int col=4;
		 
		 // Write All Mag Areas in appropriate columns
		 for(int j=0; j<magAreaOptions.size(); ++j, col+=slipModelOptions.size()) {
			 cell = row.createCell((short)col);
			 cell.setCellValue((String)magAreaOptions.get(j));
			 cell.setCellStyle(cellStyle);
		 }
		 // write the headers
		 row = sheet.createRow((short)rowIndex++);
		 col=0;
		 cell = row.createCell((short)col++);
		 cell.setCellValue("Rup_Name");
		 cell.setCellStyle(cellStyle);
		 cell = row.createCell((short)col++);
		 cell.setCellValue("A-Priori Rate");
		 cell.setCellStyle(cellStyle);
		 cell = row.createCell((short)col++);
		 cell.setCellValue("Char Mag");
		 cell.setCellStyle(cellStyle);
		 cell = row.createCell((short)col++);
		 cell.setCellValue("Char Rate");
		 cell.setCellStyle(cellStyle);
		 for(int j=0; j<magAreaOptions.size(); ++j) {
			 cell = row.createCell((short)col++);
			 cell.setCellValue("Mag");
			 cell.setCellStyle(cellStyle);
			 for(int k=0; k<slipModelOptions.size(); ++k) {
				 String slipModel = (String)slipModelOptions.get(k);
				 if(!slipModel.equals(A_FaultSegmentedSource.CHAR_SLIP_MODEL)) {
					 cell = row.createCell((short)col++);
					 cell.setCellValue((String)slipModelOptions.get(k));
					 cell.setCellStyle(cellStyle);
				 }
			 }
		 }
	}
	
	/**
	 * Generate the excel sheet to save the general prediction error
	 * @param sheet
	 */
	private void genPredErrAndTotRateSheet(HSSFSheet predErrSheet, HSSFSheet totRateSheet, int imag, int islip, int irup, HSSFCellStyle cellStyle) {
		int numA_Faults = this.aFaultsFetcher.getAllFaultNames().size();	
		//Create Excel Workbook and sheets if they do not exist already
		double totRate=0;
		
		// Write header for each Rup Solution Types
		int currRow  = irup*(numA_Faults+6);
		int faultNamesStartRow = currRow+3;
		
		if(irup==0) { // ratios in total rate sheet	 
			int ratioRowIndex1 = 2*(numA_Faults+6)+3+3;
			int totRow1 = faultNamesStartRow+1; 
			int totRow2  = (numA_Faults+6)+3+1;
			int totRow3  = 2*(numA_Faults+6)+3+1;
			this.createRatioRows(totRateSheet, ratioRowIndex1, totRow1, totRow2, totRow3);
		}
		
		if(imag==0 && islip==0) { // Write the headers and fault names for the first time	
			generateExcelSheetHeader(cellStyle, currRow, irup, predErrSheet);	
			generateExcelSheetHeader(cellStyle, currRow, irup, totRateSheet);	
			currRow+=3;	
			// write Source Names
			totRate=0;
			HSSFRow row1;
			for(int iSource=0; iSource<aFaultSources.size(); ++iSource) {
				row1 = predErrSheet.createRow((short)(faultNamesStartRow+iSource));
				A_FaultSegmentedSource src = (A_FaultSegmentedSource)aFaultSources.get(iSource);
				for(int rupIndex=0; rupIndex<src.getNumRuptures(); ++rupIndex) totRate+=src.getAPrioriRupRate(rupIndex);
				row1.createCell((short)0).setCellValue(src.getFaultSegmentData().getFaultName());
			}
			
			totRateSheet.createRow(faultNamesStartRow).createCell((short)0).setCellValue("Total Rate");
			// write the sum of apriori rates
			totRateSheet.getRow(faultNamesStartRow).createCell((short)1).setCellValue(totRate);
			
			currRow+=aFaultSources.size();
			// write totals
			row1 = predErrSheet.createRow((short)currRow++);
			this.createTotalRow(row1, faultNamesStartRow, faultNamesStartRow+numA_Faults);
		}
		
		
		int col = this.getRateCol(islip, imag);				
		// write the Gen. Pred. Error
		totRate=0;
		for(int i=0; i<this.aFaultSources.size(); ++i) {
			A_FaultSegmentedSource source = (A_FaultSegmentedSource) aFaultSources.get(i);
			for(int rupIndex=0; rupIndex<source.getNumRuptures(); ++rupIndex) totRate+=source.getRupRate(rupIndex);
			predErrSheet.getRow(i+faultNamesStartRow).createCell((short)col).setCellValue(source.getGeneralizedPredictionError());
		}			
		totRateSheet.getRow(faultNamesStartRow).createCell((short)(col)).setCellValue(totRate);
	}
}
